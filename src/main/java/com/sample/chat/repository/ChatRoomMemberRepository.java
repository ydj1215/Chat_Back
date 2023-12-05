package com.sample.chat.repository;

import com.sample.chat.entity.ChatRoom;
import com.sample.chat.entity.ChatRoomMember;
import com.sample.chat.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, Long> {
    ChatRoomMember findByChatRoomAndMember(ChatRoom chatRoom, Member member);
    List<ChatRoomMember> findByChatRoom(ChatRoom chatRoom);
}
