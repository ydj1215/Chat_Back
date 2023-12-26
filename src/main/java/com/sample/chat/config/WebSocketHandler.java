package com.sample.chat.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sample.chat.dto.ChatMessageDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class WebSocketHandler extends TextWebSocketHandler {
    // JSON 문자열을 객체로, 객체를 JSON 문자열로 변환하기 위해 필요한 객체
    private final ObjectMapper objectMapper;

    // 이벤트 발행을 위한 객체
    private final ApplicationEventPublisher eventPublisher;

    // 웹소켓 세션과 채팅방 ID를 매핑하기 위한 Map (동시성 문제를 예방하기 위해 ConcurrentHashMap 사용)
    private final Map<WebSocketSession, String> sessionRoomIdMap = new ConcurrentHashMap<>();

    @Autowired
    WebSocketHandler(ObjectMapper objectMapper, ApplicationEventPublisher eventPublisher){
        this.objectMapper = objectMapper;
        this.eventPublisher = eventPublisher;
    }

    //
    // 클라이언트에서 사용자가 채팅방에 입장해서, 서버로 메시지를 전송할 때 발생하는 이벤트를 처리하는 메서드
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 메시지를 추출하고, JSON 형태의 문자열을 ChatMessageDto 객체로 변환
        String payload = message.getPayload(); // 페이로드 : 순수한 데이터를 의미
        ChatMessageDto chatMessage = objectMapper.readValue(payload, ChatMessageDto.class);

        // 세션에 발신자의 식별자(email)을 memberId로 저장
        session.getAttributes().put("memberEmail", chatMessage.getSender());

        // 세션과 채팅방 ID를 매핑
        sessionRoomIdMap.put(session, chatMessage.getRoomId());

        // 메시지 타입에 따라 해당하는 이벤트를 발행
        if (chatMessage.getType() == ChatMessageDto.MessageType.ENTER) {
            eventPublisher.publishEvent(new SessionEnteredEvent(session, chatMessage));
        } else if (chatMessage.getType() == ChatMessageDto.MessageType.CLOSE) {
            eventPublisher.publishEvent(new SessionExitedEvent(session, chatMessage));
        } else {
            eventPublisher.publishEvent(new MessageReceivedEvent(session, chatMessage));
        }
    }

    // 웹소켓 연결이 종료되면 호출되는 메서드
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.warn("afterConnectionClosed : {}", session);
        // 연결이 종료된 세션을 Map에서 제거
        String roomId = sessionRoomIdMap.remove(session);
        if (roomId != null) {
            // 연결 종료 메시지 생성 후 이벤트 발행
            ChatMessageDto chatMessage = new ChatMessageDto();
            chatMessage.setType(ChatMessageDto.MessageType.CLOSE);
            chatMessage.setRoomId(roomId);
            eventPublisher.publishEvent(new SessionDisconnectedEvent(session, chatMessage));
        }
    }

    // 이벤트를 처리하기 위한 이벤트 클래스들 정의
    public abstract class SessionEvent extends ApplicationEvent { // 내부 클래스
        private final WebSocketSession session;
        private final ChatMessageDto chatMessage;

        public SessionEvent(WebSocketSession session, ChatMessageDto chatMessage) {
            super(session);
            this.session = session;
            this.chatMessage = chatMessage;
        }

        public WebSocketSession getSession() {
            return this.session;
        }
        public ChatMessageDto getChatMessage() {
            return this.chatMessage;
        }
    }
    // 채팅방 입장 이벤트 클래스
    public class SessionEnteredEvent extends SessionEvent {
        public SessionEnteredEvent(WebSocketSession session, ChatMessageDto chatMessage) {
            super(session, chatMessage);
        }
    }
    // 채팅방 퇴장 이벤트 클래스
    public class SessionExitedEvent extends SessionEvent {
        public SessionExitedEvent(WebSocketSession session, ChatMessageDto chatMessage) {
            super(session, chatMessage);
        }
    }
    // 메시지 수신 이벤트 클래스
    public class MessageReceivedEvent extends SessionEvent {
        public MessageReceivedEvent(WebSocketSession session, ChatMessageDto chatMessage) {
            super(session, chatMessage);
        }
    }
    // 연결 종료 이벤트 클래스
    public class SessionDisconnectedEvent extends SessionEvent {
        public SessionDisconnectedEvent(WebSocketSession session, ChatMessageDto chatMessage) {
            super(session, chatMessage);
        }
    }
}
