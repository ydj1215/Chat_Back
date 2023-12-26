package com.sample.chat.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/*
웹소켓 세션을 관리하는 클래스이다.
웹소켓 세션은 클라이언트아 서버 간의 실시간 양방향 통신을 가능하게 하는 연결을 나타낸다.
해당 클래스에서 웹소켓 세션과 해당 세션이 연결된 채팅방의 ID 를 관리한다.
*/
@Slf4j
@Transactional(readOnly = true)
@Service // 웹소켓 세션 관련 로직을 수행
public class SessionService {
    private final Map<WebSocketSession, String> sessionRoomIdMap = new ConcurrentHashMap<>();

    // 사용자가 채팅방에 입장할 때 호출, 새로운 웹소켓 세션을 생성하고, 해당 세션을 특정 채팅방 ID와 연결
    @Transactional
    public void putSession(WebSocketSession session, String roomId) {
        sessionRoomIdMap.put(session, roomId);
    }

    // 사용자가 채팅방에서 퇴장할 때 호출, 주어진 웹소켓 세션을 제거
    @Transactional
    public String removeSession(WebSocketSession session) {
        return sessionRoomIdMap.remove(session);
    }

    // 특정 사용자의 웹소켓 세션을 조회할 때 사용, 주어진 사용자 ID 에 해당하는 웹소켓 세션을 탐색
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

