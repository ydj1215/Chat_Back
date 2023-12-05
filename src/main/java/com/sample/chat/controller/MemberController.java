package com.sample.chat.controller;

import com.sample.chat.dto.MemberDto;
import com.sample.chat.service.MemberService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.sample.chat.utils.Common.CORS_ORIGIN;

@Slf4j
@CrossOrigin(origins = CORS_ORIGIN)
@RestController
@RequestMapping("/member")
public class MemberController {
    private final MemberService memberService;

    @Autowired
    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    // 회원 가입 여부 확인
    /*
    스프링 부트가 제공하는 클래스 중에 HttpEntity 라는 클래스가 존재한다.
    이 클래스는 HTTP 요청(Request), 응답(Response)에 해당하는 클래스를 포함한다.
    그리고 이것을 상속 받아 구현한 클래스가 RequestEntity, ResponseEntity 클래스이다.
     */
    @GetMapping("/check") // True / False 를 반환하기 때문에 Get
    // @Request Param : URL에서 정보를 추출, key값과 변수의 이름이 같으면 스프링 부트가 자동으로 매칭해주고, 다를 경우,
    // @RequestParam("email") String e 와 같이 직접 매칭을 해줘야한다.
    public ResponseEntity<Boolean> isMember(@RequestParam String email) {
        // ResponseEntity : 스프링 프레임워크에서 Http 통신의 응답을 표현하는 클래스
        /*
        프론트 쪽에서 예를 들자면 aaa0520@naver.com이라는 이메일을 입력하면, axios 통신을 통하여 이를 key = value 형태로 전송한다.
        (Get 방식이기 때문에, POST / PUT / PATCH 방식이였다면 JSON 형태로 데이터를 포장해서 전송한다.
        즉 http://localhost:8080/check?email=aaa0520@naver.com 와 같이 직접적으로 보낸다.

        */
        log.info("email: {}", email); // @Slf4j : Severe > Warning > Info 순으로 높은 심각도를 보유
        boolean isReg = memberService.isMember(email);
        return ResponseEntity.ok(!isReg);
        // isReg = true : 회원이 이미 존재, false : 새로운 회원 가입이 가능
        // Q. 왜 return !isReg; 를 하지 않고 return ResponseEntity.ok(!isReg); 와 같이 반환하는가?
        /*
        A. HTTP 요청/응답 모두 단순히 한 가지의 값을 담고 있는 것이 아니라, 헤더, 본문 등의 정보를 담고 있다.
        헤더에는 일반적으로 메타데이터(이 데이터가 어떤 데이터인지를 설명하는 데이터) 가 담긴다.
         또한 HTTP 응답에는 상태 코드 정보가 담기나, HTTP 요청에는 담기지 않는다.
         */
        // JSON 데이터는 본문(BODY)에 담기지만, GET 요청의 HTTP 데이터의 본문은 비어있다.
        // URL 데이터는 HTTP 요청의 가장 첫 줄에 담겨서 보내진다. 이를 요청 라인이라고 한다.
        // GET방식도 응답은 본문에 담겨서 온다.
        // 200 : 응답 본문에 요청의 결과가 포함되어 있다는 것을 의미, 주로 GET, PUT, DELETE에서 사용된다.
        // 201 : 요청의 결과로 새로운 리소스가 생성되었다는 것을 의미, 주로 POST에서 사용된다.
        // 201 : 이때 응답 헤더의 'Location' 필드에는 생성된 리소스의 URL이 포함되어 있어야 한다.
    }

    // 회원 가입
    @PostMapping("/new")
    // @RequestBody : POST, PUT, PATCH에서는 주로 데이터가 본문에 담기는데, 이 JSON 형식의 본문을 자바 객체로 자동으로 변환해준다.
    public ResponseEntity<Boolean> memberRegister(@RequestBody MemberDto memberDto) {
        boolean isTrue = memberService.saveMember(memberDto);
        return ResponseEntity.ok(isTrue);
    }

    // 회원 전체 조회
    @GetMapping("/list")
    public ResponseEntity<List<MemberDto>> memberList() {
        List<MemberDto> list = memberService.getMemberList();
        return ResponseEntity.ok(list);
    }

    // 회원 상세 조회
    @GetMapping("/detail/{email}")
    public ResponseEntity<MemberDto> memberDetail(@PathVariable String email) {
        // @PathVariable : HTTP 요청 URL의 일부를 메서드에 매개변수로서 넘겨준다.
        // @RequestParam : /check?key=value 와 구분된다.
        MemberDto memberDto = memberService.getMemberDetail(email);
        return ResponseEntity.ok(memberDto);
    }

    // 회원 조회 페이지네이션
    @GetMapping("/list/page")
    public ResponseEntity<List<MemberDto>> memberList(@RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "20") int size) {
        // 한 번에 모든 데이터를 요청하는 것이 아닌, 사용자가 볼 만큼만 서버에 요청
        List<MemberDto> list = memberService.getMemberList(page, size);
        return ResponseEntity.ok(list);
    }

    // 총 페이지 수
    @GetMapping("/list/count")
    // .../list/count => page = 0, size = 20
    // .../list/count/page=1&size=10 => page = 1, size = 10
    public ResponseEntity<Integer> memberCount(@RequestParam(defaultValue = "20") int page,
                                               @RequestParam(defaultValue = "0") int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        // Pageable = 인터페이스, PageRequest = 인터페이스를 상속 받은 클래스
        // 인터페이스 타입의 변수를 선언하는 경우의 장점은 유연성이고,
        // 인터페이스를 상속 받은 클래스 타입의 변수를 선언하는 경우는 상속 받은 기능이 아닌,
        // 해당 클래스 만의 고유한 기능을 사용해야 할 때이다.
        int pageCnt = memberService.getMemberPage(pageRequest);
        return ResponseEntity.ok(pageCnt);
    }

    // 회원 정보 수정
    @PutMapping("/modify")
    // PUT 방식 : 주로 리소스의 수정을 위해 사용되는 방식으로, POST방식과 마찬가지로 본문에 JSON 형태로 데이터를 보낸다.
    // POST 방식과의 차이점 :
    // 기존 리소스를 수정하거나, 지정한 URL에 리소스가 없다면 새로운 리소스를 생성한다.
    // 동일한 PUT 요청을 여러번 수행해도 서버의 상태가 동일하게 유지되는데 이를 멱등성이라고 한다.
    // 반면, POST 방식은 동일한 요청을 여러번 수행하면 서버의 상태가 변하게 된다.
    public ResponseEntity<Boolean> memberModify(@RequestBody MemberDto memberDto) {
        log.info("memberDto: {}", memberDto.getEmail());
        boolean isTrue = memberService.modifyMember(memberDto);
        return ResponseEntity.ok(isTrue);
    }

    // 회원 탈퇴
    @DeleteMapping("/del/{email}")
    // DELETE 방식 : 멱등성을 가진다.
    // 대부분의 경우 본문이 아닌 URL로 삭제할 리소스를 지정한다. (항상 X)
    public ResponseEntity<Boolean> memberDelete(@PathVariable String email) {
        boolean isTrue = memberService.deleteMember(email);
        return ResponseEntity.ok(isTrue); // 성공시 주로 204를 반환
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<Boolean> memberLogin(@RequestBody MemberDto memberDto) {
        boolean isTrue = memberService.login(memberDto.getEmail(), memberDto.getPassword());
        return ResponseEntity.ok(isTrue);
    }
}
