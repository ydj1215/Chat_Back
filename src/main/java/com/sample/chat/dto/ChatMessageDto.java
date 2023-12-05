package com.sample.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class ChatMessageDto {
    public enum MessageType{ // enum : 상수를 정의할때 사용
        ENTER, // 채팅방에 입장 
        TALK, // 실제 채팅 메시지를 전송
        CLOSE // 채팅방에서 퇴장
    }

    private MessageType type; // 메시지의 종류
    private String roomId; // 채팅방 아이디
    private String sender; // 메시지를 보낸 사용자의 식별 정보
    private String message; // 실제 전송된 메시지의 내용

}
