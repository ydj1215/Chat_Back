package com.sample.chat.entity;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "chat_message")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    @Id
    @Column(name = "message_id")
    @GeneratedValue
    private Long id;
    private MessageType type;
    private String message;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "room_id")
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "member_id")
    private Member sender;
    /*
    해당 필드는 JPA 상에서는 Member 타입의 sender라는 이름의 필드지만,
    데이터 베이스 상에서는 Long(Member.id의 데이터 타입) 타입의 member_id라는 이름의 필드로서 조회가 된다.
    이는 JPA가 알아서 변환을 해주기 때문에 가능한 일이다.
    */

    public enum MessageType {
        ENTER, TALK, CLOSE
    }
}
