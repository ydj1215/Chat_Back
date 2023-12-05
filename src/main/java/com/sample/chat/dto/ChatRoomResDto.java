package com.sample.chat.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.WebSocketSession;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Setter
@Getter
@Slf4j
public class ChatRoomResDto {
    private String roomId; // 채팅방 Id
    private String name; // 채팅방 이름
    private LocalDateTime regDate; // 채팅방 등록 일자

    // 직렬화 : 객체를 다른 형식으로 변환하는 과정을 의미하며, 주로 데이터를 문자열 혹은 바이트 스트림으로 변환하는 경우가 빈번하다.
    @JsonIgnore // 해당 필드를 JSON 직렬화 및 역직렬화에서 제외한다.
    // 즉 해당 객체는 ChatRoomResDto 객체를 전체 JSON 으로 변환하거나,
    // JSON를  ChatRoomResDto 객체로 변환할 때 무시된다.
    private Set<WebSocketSession> sessionSet; // 채팅방에 입장한 사용자들의 세션 정보를 저장하는 Set 이며, 해당 필드는 JSON 응답에 포함X

    @Builder
    // 다른 클래스에서 ChatRoomResDto 객체를 생성했을 때, 간단하고 편리하게 인자를 넣을 수 있게 해주는 어노테이션이다.
    public ChatRoomResDto(String roomId, String name, LocalDateTime regDate) {
        this.roomId = roomId;
        this.name = name;
        this.regDate = regDate;
        this.sessionSet = Collections.newSetFromMap(new ConcurrentHashMap<>());
        // 자바에서 Collection이란 데이터의 집합, 그룹을 의미하며,
        // Collections는 이러한 데이터, 자료 구조 컬렉션과 이를 구현하는 클래스를 정의하는 인터페이스를 제공한다.
        // Collection에는 List와 Set이 존재한다.
        // 여담으로, Map 에는 Hashtable, HashMap, SortedMap이 존재한다.

        // newSetFromMap : Collections 클래스의 메서드로, 주어진 Map을 백 엔드로 하는 새로운 Set를 생성한다.
        // 즉 원하는 특성을 가진 Map을 이용하여 (위 코드에서는 ConcurrentHashMap), 동일한 특성을 가진 Set을 생성이 가능하다.

        // ConcurrentHashMap : 여러 스레드에서 동시에 접근해도 안전하게 동작하는 HashMap이다.
        // HaspMap : key, value의 구조를 가지고, 각각에 NULL을 허용한다.

        // 버킷 : 해시 테이블은 각각의 key 값에 해시 함수를 적용하여 배열의 고유한 index를 생성하고,
        // 이 index를 활용하여 값을 저장하거나 검색하는데, 여기서 실제 값이 저장되는 장소를 버킷, 혹은 슬롯이라고 한다.

        // ConcurrentHashMap은 각 버킷에 대해서 독립적인 Lock 을 걸어 동시성을 관리한다.
        // 예를 들자면 스레드 A가 버킷 1에서 데이터를 수정하는 작업을 하려고 한다면, 버킷 1에 Lock을 건다.
        // 이때 스레드 B가 버킷 1에 접근하려고 하면, 스레드 A의 작업이 끝날때까지 대기해야 한다.
        // 그러나, 스레드 B가 버킷 2에 접근하려한다면, 락이 걸린것은 버킷 1 뿐이기 때문에 버킷 2에는 자유롭게 접근이 가능하다.
        // 즉 각각이 Lock이 독립적으로 관리된다.

        // 여러 개의 스레드가 동일한 리소스, 즉 버킷에 동시에 접근하려고 할 때 발생하는 문제가 동시성 문제이다.
        // 동시성 문제는 예상치 못한 결과를 초래한다.
    }

    // 세션의 수가 0인지를, 즉 세션이 존재하지 않는지를 확인하는 메서드
    // 즉 이 채팅방에 입장한 사용자가 없을 때를 의미한다.
    public boolean isSessionEmpty() {
        return this.sessionSet.isEmpty();
    }
}
