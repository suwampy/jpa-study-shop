package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
//@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    // 회원 가입
    @Transactional
    public Long join(Member member) {
        validateDuplicateMember(member); // 중복회원 검증
        memberRepository.save(member);
        return member.getId();
    }

    private void validateDuplicateMember(Member member) {
        // EXCCEPTION
        List<Member> findMembers = memberRepository.findByName(member.getName());
        if (!findMembers.isEmpty()) {
            throw new IllegalStateException("이미 존재하는 회원입니다.");
        }
    }

    // 전체 회원 조회
    public List<Member> findMembers() {
        return memberRepository.findAll();
    }

    public Member findOne(Long memberId) {
        return memberRepository.findOne(memberId);
    }

    // 회원 수정
    // CQRS~ 커맨드와 쿼리를 분리하자
    @Transactional
    public void update(Long id, String name) {
        // 영속성 컨텍스트에서 끌어온다~
        Member member = memberRepository.findOne(id);
        // 영속 상태의 member를 베꿔줌
        member.setName(name);
        // 트랜잭션 어노테이션이 끝나는 시점에 jpa가 flush~ 영속성 컨텍스트가 commit~
    }
}
