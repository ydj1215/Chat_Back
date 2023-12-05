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
public class ChatRoomReqDto {
    private String email; // 채팅방 생성을 요청한 사용자의 이메일
    private String name; // 생성할 채팅방의 이름
}
