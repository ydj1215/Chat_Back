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
@Transactional(readOnly = true)
@Service // 채팅 관련 로직을 수행
public class ChatService {
    // ObjectMapper : 객체와 JSON 문자열을 서로 직렬화 및 역직렬화하기 위해 사용되는 클래스
    private final ObjectMapper objectMapper;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final MemberRepository memberRepository;
    private final MemberService memberService;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final SessionService sessionService;

    @Autowired
    public ChatService(ObjectMapper objectMapper, ChatRoomRepository chatRoomRepository, ChatMessageRepository chatMessageRepository, MemberRepository memberRepository, MemberService memberService, ChatRoomMemberRepository chatRoomMemberRepository, SessionService sessionService) {
        this.objectMapper = objectMapper;
        this.chatRoomRepository = chatRoomRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.memberRepository = memberRepository;
        this.memberService = memberService;
        this.chatRoomMemberRepository = chatRoomMemberRepository;
        this.sessionService = sessionService;
    }

    // [1] 채팅방 관리 메서드
    public List<ChatRoom> findAllRoom() {
        return chatRoomRepository.findAll();
    } // [1-1] 모든 채팅방을 탐색

    // [1-2] 특정 ID를 가진 채팅방을 탐색
    public ChatRoom findRoomById(Long roomId) {
        return chatRoomRepository.findById(roomId).orElse(null);
    }

    // 1. 클라이언트에서 채팅방을 생성하고, 해당 채팅방의 정보를 반환한다.
    // [1-3] 새로운 채팅방을 생성
    @Transactional
    public ChatRoom createRoom(String roomName) {
        UUID uuid = UUID.randomUUID();
        long randomId = uuid.getMostSignificantBits();
        log.warn("Random Long from UUID: " + randomId);

        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setId(randomId);
        chatRoom.setName(roomName);
        chatRoom.setRegDate(LocalDateTime.now());

        return chatRoom;
    }

    // [1-4] 이전 채팅 로그를 호출
    public List<ChatMessage> getPreviousMessages(Long roomId) {
        return chatMessageRepository.findByChatRoom_IdWithSender(roomId);
    }

    // [1-5] 채팅방을 삭제
    @Transactional
    public void removeRoom(Long roomId) {
        chatRoomRepository.deleteById(roomId);
    }



    // [2] 채팅 세션 관리 메서드
    // [2-1] 채팅방에 입장한 세션을 추가하고 입장 메시지를 전송
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
            }
            log.debug("New session added: " + session);
        }
    }

    // [2-2] 채팅방에서 퇴장한 세션을 제거하고 퇴장 메시지를 전송
    @Transactional
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

                sendMessageToAll(roomId, chatMessageDto);
                log.debug("Member removed: " + member.getName());
            } else {
                log.debug("Member not found for Member ID: " + memberId);
            }
        }
    }

    // [2-3] 웹소켓 세션에 메시지를 전송
    @Transactional
    public <T> void sendMessage(WebSocketSession session, T message) {
        try {
            String messageStr = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(messageStr));
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    // [2-4] 각각 다른 세션을 가지고 있는, 채팅방에 있는 모든 회원에게 메시지를 전송
    @Transactional
    public void sendMessageToAll(Long roomId, ChatMessageDto messageDto) {
        ChatRoom room = findRoomById(roomId);
        if (room != null) {
            // ChatRoomMember에서 해당 ChatRoom에 속한 모든 Member를 찾는다.
            List<ChatRoomMember> chatRoomMembers = chatRoomMemberRepository.findByChatRoom(room);
            for (ChatRoomMember chatRoomMember : chatRoomMembers) {
                Member member = chatRoomMember.getMember();
                WebSocketSession session = sessionService.findSessionByMemberId(member.getId());
                if (session != null) {
                    sendMessage(session, messageDto); // 반복 수행
                    log.warn("리액트로 보내는 메시지 분석 : " + messageDto.getSender());
                }
            }
        }
    }

    // [3] 이벤트 핸들러 관련 메서드
    // [3-1] 새로운 세션이 채팅방에 입장했을 때의 이벤트를 처리
    @Transactional
    @EventListener // 이벤트 처리
    @Async
    public void handleSessionEnteredEvent(WebSocketHandler.SessionEnteredEvent event) {
        WebSocketSession session = event.getSession();
        ChatMessageDto chatMessage = event.getChatMessage();
        String roomId = chatMessage.getRoomId();
        String memberEmail = (String) session.getAttributes().get("memberEmail");

        Member member = memberService.findByEmail(memberEmail);
        Long memberId = member.getId();
        log.warn("로그 찍어보기" + memberId + ", 룸 아이디는 " + roomId);
        addSessionAndHandleEnter(Long.valueOf(roomId), session, memberId, chatMessage);
    }

    // [3-2] 채팅 메시지가 수신되었을 때의 이벤트를 처리
    @Transactional
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
        sendMessageToAll(Long.valueOf(roomId), chatMessage);
    }

    // [3-3] 세션이 채탕방에서 퇴장했을 때의 이벤트를 처리
    @Transactional
    @EventListener
    @Async
    public void handleSessionExitedEvent(WebSocketHandler.SessionExitedEvent event) {
        WebSocketSession session = event.getSession();
        ChatMessageDto chatMessage = event.getChatMessage();
        String roomId = chatMessage.getRoomId();

        removeSessionAndHandleExit(Long.valueOf(roomId), session, chatMessage);
    }

    // [3-3] 세션이 연결이 끊어졌을 때 이벤트를 처리
    @Transactional
    @EventListener
    @Async
    public void handleSessionDisconnectedEvent(WebSocketHandler.SessionDisconnectedEvent event) {
        WebSocketSession session = event.getSession();
        ChatMessageDto chatMessage = event.getChatMessage();
        String roomId = chatMessage.getRoomId();
        removeSessionAndHandleExit(Long.valueOf(roomId), session, chatMessage);
    }
}