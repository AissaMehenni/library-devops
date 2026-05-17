package com.library.book.data;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class BookRepositoryTest {

    @Autowired
    private BookRepository bookRepository;

    @Test
    void shouldSaveAndFindBookById() {
        Book saved = bookRepository.save(new Book("Clean Code", "Robert Martin", true));

        Optional<Book> found = bookRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Clean Code");
        assertThat(found.get().getAuthor()).isEqualTo("Robert Martin");
        assertThat(found.get().isAvailable()).isTrue();
    }

    @Test
    void shouldFindAllBooks() {
        bookRepository.save(new Book("Effective Java", "Joshua Bloch", true));
        bookRepository.save(new Book("Refactoring", "Martin Fowler", false));

        List<Book> books = bookRepository.findAll();

        assertThat(books).hasSize(2);
    }

    @Test
    void shouldFindBooksByAvailability() {
        bookRepository.save(new Book("DDD", "Eric Evans", true));
        bookRepository.save(new Book("TDD", "Kent Beck", false));
        bookRepository.save(new Book("XP", "Kent Beck", true));

        List<Book> availableBooks = bookRepository.findByAvailable(true);
        List<Book> unavailableBooks = bookRepository.findByAvailable(false);

        assertThat(availableBooks).hasSize(2);
        assertThat(unavailableBooks).hasSize(1);
    }

    @Test
    void shouldFindBooksByAuthor() {
        bookRepository.save(new Book("TDD", "Kent Beck", true));
        bookRepository.save(new Book("XP", "Kent Beck", true));
        bookRepository.save(new Book("DDD", "Eric Evans", true));

        List<Book> beckBooks = bookRepository.findByAuthor("Kent Beck");

        assertThat(beckBooks).hasSize(2);
    }

    @Test
    void shouldDeleteBook() {
        Book saved = bookRepository.save(new Book("To Delete", "Author", true));

        bookRepository.deleteById(saved.getId());

        assertThat(bookRepository.findById(saved.getId())).isEmpty();
    }
}
