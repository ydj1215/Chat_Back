package com.sample.chat.entity;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "chat_room")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoom {
    @Id
    @Column(name = "room_id")
    @GeneratedValue
    private Long id;
    private String name;
    private LocalDateTime regDate;

    /*
    // 다대다 관계
    @ManyToMany(fetch = FetchType.EAGER)
    // @ManytoMany는 기본값이 (fetch = FetchType.LAZY) 로 설정되어 있다.
    @JoinTable(
            name = "chatroom_member", // 관계 테이블의 이름을 설정
            joinColumns = @JoinColumn(name = "room_id"), // 현재의 엔티티를 참조하는 외래키
            inverseJoinColumns = @JoinColumn(name = "member_id")) // 반대편 엔티티를 참조하는 외래키
    private Set<Member> members = new HashSet<>();
    */

    // Q. Set<Member> 과 Set<Long> 중 뭐가 나을까?
    /*
    A. 전자는 Member 엔티티를 직접 참조하기 때문에 직접적인 참조가 가능하며,
    JPA의 연관관계 관리 (지연 로딩 같은)을 사용이 가능하다.
    후자는 아이디만을 저장하기 때문에 성능 향상에 도움이 되나, Member 엔티티의 필드를
    사용하기 위해서는 매번 Member 엔티티를 조회해야 하기 때문에 코드가 복잡해질 수 있다.
    또한 JPA 의 연관관계 관리 기능을 사용할 수 없다.
    */
}
