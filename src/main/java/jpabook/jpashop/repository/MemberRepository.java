package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Member;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class MemberRepository {
    @PersistenceContext
    private EntityManager em;

    // 영속성 컨텍스트에 member 객체를 ㅓㄶ는다
    public void save(Member member) {
        em.persist(member);
    }

    // 단건 조히
    public Member findOne(Long id) {
        return em.find(Member.class, id);
    }

    // from의 대상은 entity
    public List<Member> findAll() {
        return  em.createQuery("select m from Member m", Member.class).
                getResultList();
    }

    public List<Member> findByName(String name) {
        return em.createQuery("select m from Member m where m.name =:name", Member.class)
                .setParameter("name",name)
                .getResultList();
    }
}
