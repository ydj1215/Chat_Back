package com.sample.chat.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sample.chat.dto.ChatMessageDto;
import com.sample.chat.service.ChatService;
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
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

// 스프링은 Text 타입과 Binary 타입의 핸들러를 지원하며,
// 채팅 서비스를 만들기 위해 아래와 같은 상속 과정을 거친다.

// 클라이언트에서 메시지를 JSON 형식으로 웹 소켓을 통하여 서버로 보내면,
// Handler는 이를 받아

@Slf4j
@Component
public class WebSocketHandler extends TextWebSocketHandler {
    private final ObjectMapper objectMapper;  // JSON 문자열을 객체로, 객체를 JSON 문자열로 변환하기 위해 객체
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    WebSocketHandler(ObjectMapper objectMapper, ApplicationEventPublisher eventPublisher){
        this.objectMapper = objectMapper;
        this.eventPublisher = eventPublisher;
    }

    // 웹소켓 세션과 채팅방 ID를 매핑하는 Map
    private final Map<WebSocketSession, String> sessionRoomIdMap = new ConcurrentHashMap<>(); // 동시성 문제를 예방 가능한 Map

    public class SessionEnteredEvent extends SessionEvent {
        public SessionEnteredEvent(WebSocketSession session, ChatMessageDto chatMessage) {
            super(session, chatMessage);
        }
    }

    public class SessionExitedEvent extends SessionEvent {
        public SessionExitedEvent(WebSocketSession session, ChatMessageDto chatMessage) {
            super(session, chatMessage);
        }
    }

    public class MessageReceivedEvent extends SessionEvent {
        public MessageReceivedEvent(WebSocketSession session, ChatMessageDto chatMessage) {
            super(session, chatMessage);
        }
    }

    public abstract class SessionEvent extends ApplicationEvent {
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


    // 1
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.warn("{}", payload);
        ChatMessageDto chatMessage = objectMapper.readValue(payload, ChatMessageDto.class);

        // 세션에 발신자의 식별자(email)을 memberId로 저장
        session.getAttributes().put("memberEmail", chatMessage.getSender());

        // 세션과 채팅방 ID를 매핑
        sessionRoomIdMap.put(session, chatMessage.getRoomId());

        // 이벤트 발행
        if (chatMessage.getType() == ChatMessageDto.MessageType.ENTER) {
            eventPublisher.publishEvent(new SessionEnteredEvent(session, chatMessage));
        } else if (chatMessage.getType() == ChatMessageDto.MessageType.CLOSE) {
            eventPublisher.publishEvent(new SessionExitedEvent(session, chatMessage));
        } else {
            eventPublisher.publishEvent(new MessageReceivedEvent(session, chatMessage));
        }
    }

    // 웹 소켓 연결이 종료되면 호출되는 메서드
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.warn("afterConnectionClosed : {}", session);
        String roomId = sessionRoomIdMap.remove(session);
        if (roomId != null) {
            ChatMessageDto chatMessage = new ChatMessageDto();
            chatMessage.setType(ChatMessageDto.MessageType.CLOSE);
            chatMessage.setRoomId(roomId);
            eventPublisher.publishEvent(new SessionDisconnectedEvent(session, chatMessage));
        }
    }

    public class SessionDisconnectedEvent extends SessionEvent {
        public SessionDisconnectedEvent(WebSocketSession session, ChatMessageDto chatMessage) {
            super(session, chatMessage);
        }
    }

    public WebSocketSession findSessionByMemberId(Long memberId) {
        for (Map.Entry<WebSocketSession, String> entry : sessionRoomIdMap.entrySet()) {
            WebSocketSession session = entry.getKey();
            Long sessionId = (Long) session.getAttributes().get("memberId");
            if (memberId.equals(sessionId)) {
                return session;
            }
        }
        return null;
    }
}
