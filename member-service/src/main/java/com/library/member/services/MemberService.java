package com.library.member.services;

import com.library.member.data.Member;
import com.library.member.data.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Transactional(readOnly = true)
    public List<Member> findAll() {
        return memberRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Member findById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new MemberNotFoundException(id));
    }

    public Member create(Member member) {
        if (memberRepository.existsByEmail(member.getEmail())) {
            throw new DuplicateEmailException(member.getEmail());
        }
        member.setId(null);
        return memberRepository.save(member);
    }

    public Member update(Long id, Member updated) {
        Member existing = findById(id);
        if (!existing.getEmail().equals(updated.getEmail())
                && memberRepository.existsByEmail(updated.getEmail())) {
            throw new DuplicateEmailException(updated.getEmail());
        }
        existing.setName(updated.getName());
        existing.setEmail(updated.getEmail());
        return memberRepository.save(existing);
    }

    public void delete(Long id) {
        Member existing = findById(id);
        memberRepository.delete(existing);
    }
}
