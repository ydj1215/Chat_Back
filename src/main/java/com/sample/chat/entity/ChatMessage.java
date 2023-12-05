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

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "room_id")
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member sender;

    public enum MessageType {
        ENTER, TALK, CLOSE
    }
}
