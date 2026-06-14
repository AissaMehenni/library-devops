package com.library.book.data;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BookTest {

    @Test
    void noArgsConstructorShouldCreateEmptyBook() {
        Book book = new Book();

        assertThat(book.getId()).isNull();
        assertThat(book.getTitle()).isNull();
        assertThat(book.getAuthor()).isNull();
        assertThat(book.isAvailable()).isTrue();
    }

    @Test
    void shouldExposeAllFieldsThroughGettersAndSetters() {
        Book book = new Book();
        book.setId(42L);
        book.setTitle("Title");
        book.setAuthor("Author");
        book.setAvailable(false);

        assertThat(book.getId()).isEqualTo(42L);
        assertThat(book.getTitle()).isEqualTo("Title");
        assertThat(book.getAuthor()).isEqualTo("Author");
        assertThat(book.isAvailable()).isFalse();
    }

    @Test
    void allArgsConstructorShouldPopulateBook() {
        Book book = new Book(1L, "T", "A", false);

        assertThat(book.getId()).isEqualTo(1L);
        assertThat(book.getTitle()).isEqualTo("T");
        assertThat(book.getAuthor()).isEqualTo("A");
        assertThat(book.isAvailable()).isFalse();
    }

    @Test
    void equalsAndHashCodeShouldRelyOnAllFields() {
        Book a = new Book(1L, "T", "A", true);
        Book b = new Book(1L, "T", "A", true);
        Book c = new Book(1L, "Other", "A", true);

        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
        assertThat(a).isNotEqualTo(c);
    }

    @Test
    void toStringShouldContainAllFields() {
        Book book = new Book(1L, "T", "A", true);
        String repr = book.toString();
        assertThat(repr).contains("1", "T", "A", "true");
    }
}
