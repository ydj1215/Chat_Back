package com.sample.chat.repository;

import com.sample.chat.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    @Query("SELECT cm FROM ChatMessage cm LEFT JOIN FETCH cm.sender WHERE cm.chatRoom.id = :roomId")
    List<ChatMessage> findByChatRoom_IdWithSender(@Param("roomId") Long roomId);
    /*
    JOIN FETCH : Hibernate에서 사용하는 Qurey Hint (지시 구문) 이다.
    지연 로딩 관련 문제를 해결하고, 성능을 최적화하기 위해 사용된다.

    일반적으로 @ManyToOne이나 @OneToOne과 같은 연관 관계에서 FetchType이 LAZY로 설정되어 있을 때,
    엔티티를 조회하는 경우 연관된 엔티티는 실제로 필요할 때까지 로딩을 미뤘다가,
    해당 연관된 엔티티가 실제로 사용될 때 로딩이 되게 된다.

    하지만 때로는 특정 상황에서 지연 로딩을 사용하는 것은 성능에 부정적인 영향을 미칠 수 있다.
    예를 들자면 N+1 쿼리 문제 등이 발생이 가능하다.
    이때 JOIN FETCH 힌트를 사용하여 성능 최적화가 가능하다.

    Q. FetchType.Eager과의 차이?
    A. 둘 모두 연관된 엔티티를 한 번에 불러오는 방식이지만, 적용되는 상황과 동작 방식에 차이가 존재한다.
    FetchType.EAGER : 부모 엔티티를 조회할 때 자식 엔티티들을 함께 불러오며,
    연관된 엔티티를 항상 사용할 것으로 예상되는 경우에 사용하는 것이 좋다.
    즉, 전역성을 가진다.
    JOIN FETCH : 해당 쿼리가 실행될 때만 연관된 엔티티를 함께 불러오기 때문에,
    특정 쿼리에서만 연관된 엔티티를 함께 불러오려고 할 때 유용하다.
    즉, 지역성을 가진다.

    혹시 두 설정을 동시에 사용하면,
    */
}
