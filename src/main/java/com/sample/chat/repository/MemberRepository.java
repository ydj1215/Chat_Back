package com.sample.chat.repository;

import com.sample.chat.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);
    // Optional<Member>
    // Member 객체가 NULL일수도 있다는 것을 명시적으로 나타낸다.
    boolean existsByEmail(String email);
    Optional<Member> findByEmailAndPassword(String email, String password);
    Member findByName(String name);
}
