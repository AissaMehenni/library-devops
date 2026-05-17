package com.library.member.data;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MemberTest {

    @Test
    void noArgsConstructorShouldCreateEmptyMember() {
        Member member = new Member();

        assertThat(member.getId()).isNull();
        assertThat(member.getName()).isNull();
        assertThat(member.getEmail()).isNull();
    }

    @Test
    void shouldExposeAllFieldsThroughGettersAndSetters() {
        Member member = new Member();
        member.setId(7L);
        member.setName("Alice");
        member.setEmail("alice@example.com");

        assertThat(member.getId()).isEqualTo(7L);
        assertThat(member.getName()).isEqualTo("Alice");
        assertThat(member.getEmail()).isEqualTo("alice@example.com");
    }

    @Test
    void allArgsConstructorShouldPopulateMember() {
        Member member = new Member(1L, "Bob", "bob@example.com");

        assertThat(member.getId()).isEqualTo(1L);
        assertThat(member.getName()).isEqualTo("Bob");
        assertThat(member.getEmail()).isEqualTo("bob@example.com");
    }

    @Test
    void equalsAndHashCodeShouldRelyOnAllFields() {
        Member a = new Member(1L, "A", "a@example.com");
        Member b = new Member(1L, "A", "a@example.com");
        Member c = new Member(2L, "A", "a@example.com");

        assertThat(a).isEqualTo(b);
        assertThat(a).hasSameHashCodeAs(b);
        assertThat(a).isNotEqualTo(c);
        assertThat(a).isNotEqualTo(null);
        assertThat(a).isNotEqualTo("string");
        assertThat(a).isEqualTo(a);
    }

    @Test
    void toStringShouldContainAllFields() {
        Member member = new Member(1L, "Alice", "alice@example.com");

        assertThat(member.toString()).contains("1", "Alice", "alice@example.com");
    }
}
