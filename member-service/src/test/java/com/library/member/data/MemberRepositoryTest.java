package com.library.member.data;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Test
    void shouldSaveAndFindMemberById() {
        Member saved = memberRepository.save(new Member("Alice", "alice@example.com"));

        Optional<Member> found = memberRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Alice");
        assertThat(found.get().getEmail()).isEqualTo("alice@example.com");
    }

    @Test
    void shouldFindAllMembers() {
        memberRepository.save(new Member("Alice", "alice@example.com"));
        memberRepository.save(new Member("Bob", "bob@example.com"));

        List<Member> members = memberRepository.findAll();

        assertThat(members).hasSize(2);
    }

    @Test
    void shouldFindMemberByEmail() {
        memberRepository.save(new Member("Carol", "carol@example.com"));

        Optional<Member> found = memberRepository.findByEmail("carol@example.com");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Carol");
    }

    @Test
    void existsByEmailShouldReturnTrueWhenPresent() {
        memberRepository.save(new Member("Dan", "dan@example.com"));

        assertThat(memberRepository.existsByEmail("dan@example.com")).isTrue();
        assertThat(memberRepository.existsByEmail("missing@example.com")).isFalse();
    }

    @Test
    void shouldDeleteMember() {
        Member saved = memberRepository.save(new Member("Eve", "eve@example.com"));

        memberRepository.deleteById(saved.getId());

        assertThat(memberRepository.findById(saved.getId())).isEmpty();
    }
}
