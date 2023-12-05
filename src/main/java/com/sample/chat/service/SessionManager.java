package com.sample.chat.service;

import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SessionManager {
    private final Map<WebSocketSession, String> sessionRoomIdMap = new ConcurrentHashMap<>();

    public void putSession(WebSocketSession session, String roomId) {
        sessionRoomIdMap.put(session, roomId);
    }

    public String removeSession(WebSocketSession session) {
        return sessionRoomIdMap.remove(session);
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

