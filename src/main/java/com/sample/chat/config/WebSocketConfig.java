package com.sample.chat.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.WebSocketHandler;

// 웹 소켓 서버 설정을 위한 클래스
// REST API = 클라이언트가 서버에 데이터를 요청하고, 서버가 응답을 반환하는 단방향 통신
// Web Socket  = 양방향 통신
// 클라이언트가 WebSocket 프로토콜을 통해 서버에 요청, 서버가 WebSockeyHandler를 통해 메서지를 수신 및 처리한다.

@Configuration // 스프링의 설정 클래스임을 나타내는 어노테이션, 스프링 bean 등록
@EnableWebSocket // 웹 소켓 서버를 활성화하기 위해 사용, 스프링 bean 등록

public class WebSocketConfig implements WebSocketConfigurer {
    // WebSocketConfig는 스프링의 WebSocketConfigurer 인터페이스를 구현하여, WebSocketHandelr를 구성한다.
    // 클라이언트의 WebSocket 핸들러를 구성한다.
    private final WebSocketHandler webSocketHandler;

    @Autowired
    WebSocketConfig(WebSocketHandler webSocketHandler){
        this.webSocketHandler = webSocketHandler;
    }

    @Override // 재정의
    // 부모 클래스 혹은 인터페이스에게 상속받은 메서드를 오버라이딩, 즉 재정의 한다는 뜻이다.
    // 웹 소켓 통신을 구현하기 위해서는 특정 엔드포인트에 대한 핸들러가 필요하다.
    // 이 핸들러는 클라이언트로부터의 연결 요청을 받아들이고 메시지를 받고 보내는 등의 역할을 수행한다.
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry){
        registry.addHandler(webSocketHandler, "/ws/chat").setAllowedOrigins("*");
        // addHandler라는 메서드를 통해 webSocketHandler 라는 핸들러를 /ws/chat이라는 엔드 포인트에 연결,
        // 그 이후, 모든 도미엔에서의 접속을 허용하도록 설정
        // 즉 이 메서드는 웹소켓 통신을 위한 핸들러를 특정 엔드 포인트에 등록하고, 이 엔드포인트를 모든 도메인에서 접근 가능하도록 설정하는 역할을 수행한다.
    }
}

// 즉, 해당 클래스는 웹 소켓 서버를 설정하고,
// 클라이언트의 웹 소켓 연결을 처리할 핸들러를 등록하는 역할을 수행한다.
// 이를 통해 클라이언트와 서버 간의 실시간 양방향 통신이 가능하다.