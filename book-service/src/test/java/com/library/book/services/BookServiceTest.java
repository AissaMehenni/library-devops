package com.library.book.services;

import com.library.book.data.Book;
import com.library.book.data.BookRepository;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookService bookService;

    private Book book;

    @BeforeEach
    void setUp() {
        book = new Book(1L, "Clean Code", "Robert Martin", true);
    }

    @Test
    void findAllShouldReturnAllBooks() {
        Book second = new Book(2L, "TDD", "Kent Beck", false);
        when(bookRepository.findAll()).thenReturn(List.of(book, second));

        List<Book> result = bookService.findAll();

        assertThat(result).hasSize(2).containsExactly(book, second);
    }

    @Test
    void findByIdShouldReturnBookWhenPresent() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        Book result = bookService.findById(1L);

        assertThat(result).isEqualTo(book);
    }

    @Test
    void findByIdShouldThrowWhenAbsent() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.findById(99L))
                .isInstanceOf(BookNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void createShouldNullifyIdAndPersist() {
        Book input = new Book(123L, "X", "Y", true);
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> {
            Book toSave = invocation.getArgument(0);
            toSave.setId(10L);
            return toSave;
        });

        Book result = bookService.create(input);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getTitle()).isEqualTo("X");
        verify(bookRepository).save(input);
    }

    @Test
    void updateShouldOverrideFieldsAndPersist() {
        Book updated = new Book(null, "New Title", "New Author", false);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        Book result = bookService.update(1L, updated);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("New Title");
        assertThat(result.getAuthor()).isEqualTo("New Author");
        assertThat(result.isAvailable()).isFalse();
    }

    @Test
    void updateShouldThrowWhenBookMissing() {
        when(bookRepository.findById(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.update(7L, book))
                .isInstanceOf(BookNotFoundException.class);
        verify(bookRepository, never()).save(any());
    }

    @Test
    void deleteShouldRemoveBook() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        bookService.delete(1L);

        verify(bookRepository, times(1)).delete(book);
    }

    @Test
    void deleteShouldThrowWhenBookMissing() {
        when(bookRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.delete(5L))
                .isInstanceOf(BookNotFoundException.class);
        verify(bookRepository, never()).delete(any());
    }

    @Test
    void borrowShouldMarkUnavailableWhenAvailable() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        Book result = bookService.borrow(1L);

        assertThat(result.isAvailable()).isFalse();
        verify(bookRepository).save(book);
    }

    @Test
    void borrowShouldThrowWhenAlreadyUnavailable() {
        book.setAvailable(false);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        assertThatThrownBy(() -> bookService.borrow(1L))
                .isInstanceOf(BookUnavailableException.class)
                .hasMessageContaining("1");
        verify(bookRepository, never()).save(any());
    }

    @Test
    void borrowShouldThrowWhenBookMissing() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.borrow(99L))
                .isInstanceOf(BookNotFoundException.class);
    }

    @Test
    void returnBookShouldMarkAvailableWhenUnavailable() {
        book.setAvailable(false);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        Book result = bookService.returnBook(1L);

        assertThat(result.isAvailable()).isTrue();
    }

    @Test
    void returnBookShouldKeepAvailableTrueWhenAlreadyAvailable() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        Book result = bookService.returnBook(1L);

        assertThat(result.isAvailable()).isTrue();
    }

    @Test
    void returnBookShouldThrowWhenBookMissing() {
        when(bookRepository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.returnBook(42L))
                .isInstanceOf(BookNotFoundException.class);
    }
}
