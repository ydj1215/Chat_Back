package com.sample.chat.service;

import com.sample.chat.dto.MemberDto;
import com.sample.chat.entity.Member;
import com.sample.chat.repository.MemberRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Slf4j
@Service
public class MemberService {
    private final MemberRepository memberRepository; // 객체의 불변성을 위해 final 설정

    @Autowired // 단일 생성자만을 가질때는 생략 가능
    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }
    // 위와 같이 선언하면, 스프링 컨테이너가 Service 객체를 생성 시에 Repository 타입의 Bean을 찾아서 주입
    // 만약 Repository Bean이 등록되어있지 않으면 NoSuchBeanDefinitionException이 발생

    // 회원 가입 여부 확인
    public boolean isMember(String email) {
        return memberRepository.existsByEmail(email);
    }

    // 회원 가입
    public boolean saveMember(MemberDto memberDto) {
        Member member = new Member();
        // 프론트에서 받아온 데이터들을 Controller에서 DTO 객체에 담고, 이것을 member 엔티티에 저장해준다.
        member.setEmail(memberDto.getEmail());
        member.setName(memberDto.getName());
        member.setPassword(memberDto.getPassword());
        member.setImage(memberDto.getImage());
        member.setRegDate(memberDto.getRegDate());
        memberRepository.save(member);
        /*
        JPA 에서 엔티티 매니저는 엔티티를 저장, 수정, 조회, 삭제 하는 등 엔티티와 관련된 모든 일을 처리한다.
        또한 영속성 컨텍스트(Persistence Context)를 통해 데이터의 상태 변화를 감지하고 필요한 쿼리를 자동으로 수행한다.
        스프링에서 사용하는 JPA, Spring DATA JPA에서는 스프링이 알아서 엔티티 매니저를 관리해준다.

        Q : 왜 바로 데이터 베이스에 적용을 하지 않고 영속성 컨텍스트라는 개념이 존재할까?
        A : 실제 데이터 베이스로의 접근을 최소화하여 성능을 최적화할 수 있다.
        쿼리문 한개를 실행할 때마다 데이터베이스에 접근하는 것이 아니라, 쿼리문 여러개를 모아서 한 번에 데이터 베이스로의 접근을 행한다.

        save() : 엔티티를 영속성 컨텍스트에 저장한다. 트랙잭션이 커밋되는 시점에 데이터 베이스에 반영된다.
        즉 save()를 하는 시점에 데이터 베이스에 저장되는 것은 아니다. 영속성 컨텍스트 => 데이터베이스에 적용되는 시점은 엔티티 매니저, 즉 스프링이 해준다.
        flush() : 영속성 컨텍스트의 변경 사항을 즉시 데이터 베이스에 적용한다.
        일반적으로, flush()이후에
        EntityManager em;
        em.clear()
        를 해줘야한다. 영속석 컨텍스트를 초기화 해주는 것이다. (버퍼 지우기와 유사)
         */
        return true;
    }

    // 회원 엔티티에서 회원 정보를 꺼내 회원 DTO 에 담는 메서드
    private MemberDto convertEntityToDto(Member member) {
        MemberDto memberDto = new MemberDto();
        memberDto.setEmail(member.getEmail());
        memberDto.setName(member.getName());
        memberDto.setPassword(member.getPassword());
        memberDto.setImage(member.getImage());
        memberDto.setRegDate(member.getRegDate());
        return memberDto;
    }

    // 회원 전체 조회
    public List<MemberDto> getMemberList() {
        List<Member> memberList = memberRepository.findAll(); // 모든 회원 정보를 가져와 리스트에 저장
        List<MemberDto> memberDtoList = new ArrayList<>();
        for (Member member : memberList) {
            memberDtoList.add(convertEntityToDto(member));
            // 첫번째 회원의 데이터는 memberDtoList[0]에, 두 번째 회원의 데이터는 memberDtoList[1]에,
            // 이런식으로 순차적으로 담긴다.
        }
        return memberDtoList;
    }

    // 회원 상세 조회
    public MemberDto getMemberDetail(String email) {
        Member member = memberRepository.findByEmail(email).orElseThrow(
                () -> new RuntimeException("해당 회원이 존재하지 않습니다.")
        );
        return convertEntityToDto(member);
    }

    // 회원 조회 : 페이지 네이션 (메서드 오버로딩)
    public List<MemberDto> getMemberList(int page, int size) {
        // Pageable = 인터페이스 이므로, 실제로 사용할 때에는 인터페이스를 구현한 PageRequest 객체를 사용
        // pageable에는 페이지 번호(0부터 시작)와, 페이지 크기(한 페이지에 보여줄 데이터의 수)가 담긴다.
        Pageable pageable = PageRequest.of(page, size);

        // finaALL를 통하여 해당 페이지에서 지정된 크기 만큼의 데이터만을 조회하고 이를 List<Member> 형태로 반환
        List<Member> memberList = memberRepository.findAll(pageable).getContent();
        List<MemberDto> memberDtoList = new ArrayList<>();
        for (Member member : memberList) {
            memberDtoList.add(convertEntityToDto(member));
        }
        return memberDtoList;
    }

    // 총 페이지 수
    public int getMemberPage(Pageable pageable) {
        return memberRepository.findAll(pageable).getTotalPages();
    }

    // 회원 수정
    public boolean modifyMember(MemberDto memberDto) {
        try {
            Member member = memberRepository.findByEmail(memberDto.getEmail()).orElseThrow(
                    () -> new RuntimeException("해당 회원이 존재하지 않습니다.")
            );
            member.setName(memberDto.getName());
            member.setImage(memberDto.getImage());
            memberRepository.save(member);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 회원 삭제
    public boolean deleteMember(String email) {
        try {
            Member member = memberRepository.findByEmail(email).orElseThrow(
                    () -> new RuntimeException("해당 회원이 존재하지 않습니다.")
            );
            memberRepository.delete(member);
            return true; // 회원이 존재하면 true 반환
        } catch (RuntimeException e) {
            return false; // 회원이 존재하지 않으면 false 반환
        }
    }

    // 로그인
    public boolean login(String email, String password) {
        log.info("email: {}, password: {}", email, password);
        Optional<Member> member = memberRepository.findByEmailAndPassword(email, password);
        log.info("member: {}", member);
        return member.isPresent();
    }

    public Member findByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("No member found with email: " + email));
    }
}

