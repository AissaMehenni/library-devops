package com.library.member.services;

import com.library.member.data.Member;
import com.library.member.data.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberService memberService;

    private Member member;

    @BeforeEach
    void setUp() {
        member = new Member(1L, "Alice", "alice@example.com");
    }

    @Test
    void findAllShouldReturnAllMembers() {
        Member second = new Member(2L, "Bob", "bob@example.com");
        when(memberRepository.findAll()).thenReturn(List.of(member, second));

        List<Member> result = memberService.findAll();

        assertThat(result).hasSize(2).containsExactly(member, second);
    }

    @Test
    void findByIdShouldReturnMemberWhenPresent() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        Member result = memberService.findById(1L);

        assertThat(result).isEqualTo(member);
    }

    @Test
    void findByIdShouldThrowWhenAbsent() {
        when(memberRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> memberService.findById(99L))
                .isInstanceOf(MemberNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void createShouldPersistWhenEmailIsFree() {
        Member input = new Member(123L, "Carol", "carol@example.com");
        when(memberRepository.existsByEmail("carol@example.com")).thenReturn(false);
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> {
            Member toSave = invocation.getArgument(0);
            toSave.setId(10L);
            return toSave;
        });

        Member result = memberService.create(input);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getName()).isEqualTo("Carol");
    }

    @Test
    void createShouldThrowWhenEmailAlreadyExists() {
        Member input = new Member(null, "Carol", "carol@example.com");
        when(memberRepository.existsByEmail("carol@example.com")).thenReturn(true);

        assertThatThrownBy(() -> memberService.create(input))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessageContaining("carol@example.com");
        verify(memberRepository, never()).save(any());
    }

    @Test
    void updateShouldOverrideFieldsAndPersist() {
        Member updated = new Member(null, "Alice Updated", "alice@example.com");
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(memberRepository.save(any(Member.class))).thenAnswer(inv -> inv.getArgument(0));

        Member result = memberService.update(1L, updated);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Alice Updated");
        assertThat(result.getEmail()).isEqualTo("alice@example.com");
        verify(memberRepository, never()).existsByEmail(any());
    }

    @Test
    void updateShouldCheckEmailUniquenessWhenEmailChanges() {
        Member updated = new Member(null, "Alice", "new@example.com");
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(memberRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(memberRepository.save(any(Member.class))).thenAnswer(inv -> inv.getArgument(0));

        Member result = memberService.update(1L, updated);

        assertThat(result.getEmail()).isEqualTo("new@example.com");
    }

    @Test
    void updateShouldThrowWhenNewEmailAlreadyExists() {
        Member updated = new Member(null, "Alice", "taken@example.com");
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(memberRepository.existsByEmail("taken@example.com")).thenReturn(true);

        assertThatThrownBy(() -> memberService.update(1L, updated))
                .isInstanceOf(DuplicateEmailException.class);
        verify(memberRepository, never()).save(any());
    }

    @Test
    void updateShouldThrowWhenMemberMissing() {
        when(memberRepository.findById(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> memberService.update(7L, member))
                .isInstanceOf(MemberNotFoundException.class);
    }

    @Test
    void deleteShouldRemoveMember() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        memberService.delete(1L);

        verify(memberRepository).delete(member);
    }

    @Test
    void deleteShouldThrowWhenMemberMissing() {
        when(memberRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> memberService.delete(5L))
                .isInstanceOf(MemberNotFoundException.class);
        verify(memberRepository, never()).delete(any());
    }
}
