package com.sample.chat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sample.chat.config.WebSocketHandler;
import com.sample.chat.dto.ChatMessageDto;
import com.sample.chat.entity.ChatMessage;
import com.sample.chat.entity.ChatRoom;
import com.sample.chat.entity.ChatRoomMember;
import com.sample.chat.entity.Member;
import com.sample.chat.repository.ChatMessageRepository;
import com.sample.chat.repository.ChatRoomMemberRepository;
import com.sample.chat.repository.ChatRoomRepository;
import com.sample.chat.repository.MemberRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class ChatService {
    // ObjectMapper : 객체와 JSON 문자열을 서로 직렬화 및 역직렬화하기 위해 사용되는 클래스
    private final ObjectMapper objectMapper;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final MemberRepository memberRepository;
    private final WebSocketHandler webSocketHandler;
    private final MemberService memberService;
    private final ChatRoomMemberRepository chatRoomMemberRepository;

    @Autowired
    ChatService(ObjectMapper objectMapper, ChatRoomRepository chatRoomRepository, ChatMessageRepository chatMessageRepository, MemberRepository memberRepository, WebSocketHandler webSocketHandler, MemberService memberService, ChatRoomMemberRepository chatRoomMemberRepository) {
        this.objectMapper = objectMapper;
        this.chatRoomRepository = chatRoomRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.memberRepository = memberRepository;
        this.webSocketHandler = webSocketHandler;
        this.memberService = memberService;
        this.chatRoomMemberRepository = chatRoomMemberRepository;
    }

    // @PostConstruct : 객체 생성후 자동으로 호출이 되게 설정, 해당 클래스가 스프링의 Bean일때 사용 가능
    // ChatService 클래스의 객체가 생성될 때, 자동으로 init 메서드가 호출 되며 chatRooms가 초기화된다.
    // LinkedHashMap : HashMap과 마찬가지로 키-값의 형태를 가지지만, 삽입 순서를 유지한다는 특성이 존재한다.
    // 삽입된 순서대로 요소를 순회하거나 출력할 때 사용한다. 먼저 삽입된 요소가 먼저 출력된다.
    /*
    @PostConstruct
    private void init() { // 채팅방 정보를 담을 맵을 초기화
        chatRooms = new LinkedHashMap<>(); // 채팅방 정보를 담을 맵
    }
    */

    public List<ChatRoom> findAllRoom() {
        return chatRoomRepository.findAll();
    }

    // 주어진 roomId에 해당하는 채팅방 정보를 반환한다.
    public ChatRoom findRoomById(Long roomId) {
        return chatRoomRepository.findById(roomId).orElse(null);
    }

    @Transactional
    public ChatRoom createRoom(String roomName) {
        UUID uuid = UUID.randomUUID();
        long randomId = uuid.getMostSignificantBits();
        log.warn("Random Long from UUID: " + randomId);


        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setId(randomId);
        chatRoom.setName(roomName);
        chatRoom.setRegDate(LocalDateTime.now());

        return chatRoomRepository.save(chatRoom);
    }

    // 채팅방 삭제
    @Transactional
    public void removeRoom(Long roomId) {
        chatRoomRepository.deleteById(roomId);
    }


    // 3
// 채팅방에 입장한 세션 추가하고 입장 메시지를 전송
    @Transactional
    public void addSessionAndHandleEnter(Long roomId, WebSocketSession session, Long memberId, ChatMessageDto chatMessageDto) {
        ChatRoom room = findRoomById(roomId);
        if (room != null) {
            Member member = memberRepository.findById(memberId).orElse(null);
            if (member != null) {
                // 채팅방에 입장한 회원을 ChatRoomMember에 추가하기 전에 중복 여부 확인
                ChatRoomMember existingMember = chatRoomMemberRepository.findByChatRoomAndMember(room, member);
                if (existingMember == null) {
                    ChatRoomMember chatRoomMember = new ChatRoomMember();
                    chatRoomMember.setChatRoom(room);
                    chatRoomMember.setMember(member);
                    chatRoomMemberRepository.save(chatRoomMember);

                    // MemberId와 사용자 이름을 세션의 속성으로 저장
                    session.getAttributes().put("memberId", member.getId());
                    session.getAttributes().put("memberName", member.getName()); // 새로운 라인 추가
                    log.warn("addSessionAndHandleEnter 로그 : " + member.getId());
                } else {
                    log.warn("addSessionAndHandleEnter: Member already in the room.");

                    // 중복된 회원이 채팅방에 이미 있으면 해당 정보로 sender를 설정
                    chatMessageDto.setSender(existingMember.getMember().getName());
                }
            }
            if (chatMessageDto.getSender() != null) {
                chatMessageDto.setMessage(chatMessageDto.getSender() + "님이 입장했습니다.");
                ChatMessage chatMessage = new ChatMessage();
                chatMessage.setType(ChatMessage.MessageType.ENTER);
                chatMessage.setMessage(chatMessageDto.getMessage());
                chatMessage.setChatRoom(room);
                chatMessage.setSender(memberRepository.findByName(chatMessageDto.getSender()));
                log.warn("으악 : " + chatMessage.getSender());

                // chatMessageDto에서 이미 sender가 설정되어 있으므로 추가 확인은 필요 없음
                chatMessageRepository.save(chatMessage);
            }
            log.debug("New session added: " + session);
        }
    }


    // 채팅방에서 퇴장한 세션을 제거하고 퇴장 메시지를 전송
    public void removeSessionAndHandleExit(Long roomId, WebSocketSession session, ChatMessageDto chatMessageDto) {
        ChatRoom room = findRoomById(roomId);
        if (room != null) {
            // 세션에서 memberId를 가져옵니다.
            Long memberId = (Long) session.getAttributes().get("memberId");
            Member member = memberRepository.findById(memberId).orElse(null);

            if (member != null) {
                // ChatRoomMember에서 해당 Member를 찾아서 삭제
                ChatRoomMember chatRoomMember = chatRoomMemberRepository.findByChatRoomAndMember(room, member);
                if (chatRoomMember != null) {
                    chatRoomMemberRepository.delete(chatRoomMember);
                }

                chatMessageDto.setMessage(member.getName() + "님이 퇴장했습니다.");

                ChatMessage chatMessage = new ChatMessage();
                chatMessage.setType(ChatMessage.MessageType.CLOSE);
                chatMessage.setMessage(chatMessageDto.getMessage());
                chatMessage.setChatRoom(room);
                chatMessage.setSender(member);

                chatMessageRepository.save(chatMessage);
                sendMessageToAll(roomId, chatMessageDto);
                log.debug("Member removed: " + member.getName());
            } else {
                log.debug("Member not found for Member ID: " + memberId);
            }
        }
    }

    // 채팅방에 존재하는 모든 회원에게 메시지를 전송
    // 채팅방에 존재하는 회원은 각각 다른 세션을 가지고 있을 것이다.
    // 고로 채팅방에 있는 모두가 메시지를 보기 위해서는 sendMessage를 여러번 수행해야 한다.
    public void sendMessageToAll(Long roomId, ChatMessageDto messageDto) {
        ChatRoom room = findRoomById(roomId);
        if (room != null) {
            // ChatRoomMember에서 해당 ChatRoom에 속한 모든 Member를 찾는다.
            List<ChatRoomMember> chatRoomMembers = chatRoomMemberRepository.findByChatRoom(room);
            for (ChatRoomMember chatRoomMember : chatRoomMembers) {
                Member member = chatRoomMember.getMember();
                WebSocketSession session = webSocketHandler.findSessionByMemberId(member.getId());
                if (session != null) {
                    sendMessage(session, messageDto);
                    log.warn("리액트로 보내는 메시지 분석 : " + messageDto.getSender());
                }
            }
        }
    }


    // 웹소켓 세션에 메시지를 전송
    public <T> void sendMessage(WebSocketSession session, T message) {
        try {
            String messageStr = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(messageStr));
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    // 2
    @EventListener
    @Async
    public void handleSessionEnteredEvent(WebSocketHandler.SessionEnteredEvent event) {
        WebSocketSession session = event.getSession();
        ChatMessageDto chatMessage = event.getChatMessage();
        String roomId = chatMessage.getRoomId();
        String memberEmail = (String) session.getAttributes().get("memberEmail");

        // memberEmail 을 통해서 memberId 찾기
        Member member = memberService.findByEmail(memberEmail);
        Long memberId = member.getId();
        log.warn("로그 찍어보기" + memberId + ", 룸 아이디는 " + roomId);
        this.addSessionAndHandleEnter(Long.valueOf(roomId), session, memberId, chatMessage);
    }


    @EventListener
    @Async
    public void handleSessionExitedEvent(WebSocketHandler.SessionExitedEvent event) {
        WebSocketSession session = event.getSession();
        ChatMessageDto chatMessage = event.getChatMessage();
        String roomId = chatMessage.getRoomId();

        this.removeSessionAndHandleExit(Long.valueOf(roomId), session, chatMessage);
    }

    @EventListener
    @Async
    public void handleMessageReceivedEvent(WebSocketHandler.MessageReceivedEvent event) {
        ChatMessageDto chatMessage = event.getChatMessage();
        String roomId = chatMessage.getRoomId();

        ChatMessage chatMessageEntity = new ChatMessage();
        chatMessageEntity.setType(ChatMessage.MessageType.valueOf(chatMessage.getType().name()));
        chatMessageEntity.setMessage(chatMessage.getMessage());
        chatMessageEntity.setChatRoom(findRoomById(Long.valueOf(roomId)));

        String senderEmail = chatMessage.getSender();
        log.warn("handleMessageReceivedEvent senderName : " + senderEmail);
        Optional<Member> senderOpt = memberRepository.findByEmail(senderEmail);
        if (senderOpt.isPresent()) {
            Member sender = senderOpt.get();
            chatMessageEntity.setSender(sender);
        } else {
            log.error("handleMessageReceivedEvent sender = null 에러 발생!");
        }
        chatMessageRepository.save(chatMessageEntity);

        this.sendMessageToAll(Long.valueOf(roomId), chatMessage);
    }

    @EventListener
    @Async
    public void handleSessionDisconnectedEvent(WebSocketHandler.SessionDisconnectedEvent event) {
        WebSocketSession session = event.getSession();
        ChatMessageDto chatMessage = event.getChatMessage();
        String roomId = chatMessage.getRoomId();

        this.removeSessionAndHandleExit(Long.valueOf(roomId), session, chatMessage);
    }

    // 이전 채팅 로그 불러오기
    public List<ChatMessage> getPreviousMessages(Long roomId) {
        return chatMessageRepository.findByChatRoom_IdWithSender(roomId);
    }
}