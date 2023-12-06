package com.sample.chat.controller;

import com.sample.chat.dto.ChatRoomReqDto;
import com.sample.chat.entity.ChatMessage;
import com.sample.chat.entity.ChatRoom;
import com.sample.chat.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.sample.chat.utils.Common.CORS_ORIGIN;

@Slf4j
@RestController
@CrossOrigin(origins = CORS_ORIGIN)
@RequestMapping("/chat")
public class ChatController {
    private final ChatService chatService;

    ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    // 채팅방 생성
    @PostMapping("/new")
    public ResponseEntity<String> createRoom(@RequestBody ChatRoomReqDto chatRoomReqDto) {
        ChatRoom room = chatService.createRoom(chatRoomReqDto.getName());
        log.warn(room.getName());
        return new ResponseEntity<>(room.getName(), HttpStatus.OK);
        // return new ResponseEntity<>; 는 HTTP 응답의 본문이 비어있다는 것을 의미
        // return new ResponseEntity<>(room.getRoomId(), HttpStatus.OK); 는 room.getRoomId()를 본문으로 가지는 ResponseEntity 객체를 생성하고, 상태 코드를 200으로 설정
    }

    // 모든 채팅방의 목록 반환
    @GetMapping("/list")
    public ResponseEntity<List<ChatRoom>> findAllRoom() {
        List<ChatRoom> rooms = chatService.findAllRoom();
        return ResponseEntity.ok(rooms);
        // return new ResponseEntity<>(rooms, HttpStatus.OK); 와 같은 의미 이다.

        // 정적 메서드란 클래스 이름을 통해 바로 호출할 수 있는 메서드를 의미한다. (static method)
        // 위는 ResponseEntity 클래스의 정적 메서드 ok()를 사용하기 때문에 new가 없어도 되는 것이고,
        // 아래의 경우는 새로운 객체를 생성하는 것이기 때문에 new 키워드가 필요하다.

    }
    // @RestController 를 사용하면, 메서드의 반환값을 자동으로 HTTP 응답 본문으로 변환해준다.
    // 이 경우 별도로 ResponseEntity를 사용하지 않아도 200 ok와 같은 기본 응답 코드와 함께 응답 데이터를 클라이언트로 전송이 가능하다.
    // ResponseEntity~ 타입으로 반환하는 이유는 HTTP 응답의 상태 코드 및 헤더 등의 제어가 가능하기 때문이다.

    // 방 정보 가져오기
    @GetMapping("/room/{roomId}")
    public ResponseEntity<ChatRoom> findRoomById(@PathVariable Long roomId) {
        ChatRoom room = chatService.findRoomById(roomId);
        if (room != null) {
            return ResponseEntity.ok(room);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // 채팅방 삭제
    @DeleteMapping("/room/{roomId}")
    public ResponseEntity<String> removeRoom(@PathVariable Long roomId) {
        chatService.removeRoom(roomId);
        return ResponseEntity.ok("채팅방이 삭제되었습니다.");
    }
    
    // 이전 채팅 로그 불러오기
    @GetMapping("/{roomId}/messages")
    public ResponseEntity<List<ChatMessage>> getPreviousMessages(@PathVariable Long roomId) {
        List<ChatMessage> messages = chatService.getPreviousMessages(roomId);
        return ResponseEntity.ok(messages);
    }
    // Q. List <> 안의 형식을 엔티티로 했을 때와 Dto로 했을 때의 차이?
    // 엔티티는 데이터 베이스와 직접적으로 매핑되는 객체이기 때문에,
    // 데이터 베이스의 구조가 변경되면 엔티티도 함께 변경되어야 하지만,
    // Dto는 사용자에게 보여주는 화면에 맞추어 설계되기 때문에, 변경되어도 데이터 베이스에 영향을 주지 않는다.
    // 엔티티와 달리 Dto를 사용하면 필요한 데이터만 선택적으로 전달해 데이터 보안을 강화할 수 있다.
    // 또한 엔티티는 사용하지 않는 필드를 가지고 있을 수도 있기 때문에,
    // Dto를 사용하여 필요한 필드만을 클라이언트에게 전달한다면 성능을 향상시킬 수 있다.

}
