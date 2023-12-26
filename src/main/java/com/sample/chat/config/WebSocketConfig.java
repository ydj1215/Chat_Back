package com.sample.chat.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.WebSocketHandler;

// 웹 소켓 서버 설정을 위한 클래스
@Configuration // 설정
@EnableWebSocket // 웹소켓

/*
기존의 REST API 는 클라이언트가 서버에 데이터를 요청하고, 서버가 응답을 반환하는 단방향 통신이다.
Web Socket 은 양방향 통신으로, 클라이언트가 WebSocket 프로토콜을 통해 서버에 요청을 하면, 즉 메시지를 보내면, 서버가 WebSocketHandler 를 통해 메서지를 수신 및 처리한다.
*/

public class WebSocketConfig implements WebSocketConfigurer {
    private final WebSocketHandler webSocketHandler;

    @Autowired
    WebSocketConfig(WebSocketHandler webSocketHandler){
        this.webSocketHandler = webSocketHandler;
    }

    @Override
    // 웹 소켓 통신을 구현하기 위해서는 특정 엔드포인트에 대한 핸들러가 필요하다.
    // 이 핸들러는 클라이언트로부터의 연결 요청을 받아들이고 메시지를 받고 보내는 등의 역할을 수행한다.
    // 아래 코드는 웹소켓 핸들러를 특정 엔드 포인트에 연결 후, 모든 도메인에서의 접속을 허용한다.
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry){
        registry.addHandler(webSocketHandler, "/ws/chat").setAllowedOrigins("*");
    }
}

// 즉, 해당 클래스는 웹 소켓 서버를 설정하고,
// 클라이언트의 웹 소켓 연결을 처리할 핸들러를 등록하는 역할을 수행한다.
// 이를 통해 클라이언트와 서버 간의 실시간 양방향 통신이 가능하다.