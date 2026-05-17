package com.library.book.services;

import com.library.book.data.Book;
import com.library.book.data.BookRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Transactional(readOnly = true)
    public List<Book> findAll() {
        return bookRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Book findById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));
    }

    public Book create(Book book) {
        book.setId(null);
        return bookRepository.save(book);
    }

    public Book update(Long id, Book updated) {
        Book existing = findById(id);
        existing.setTitle(updated.getTitle());
        existing.setAuthor(updated.getAuthor());
        existing.setAvailable(updated.isAvailable());
        return bookRepository.save(existing);
    }

    public void delete(Long id) {
        Book existing = findById(id);
        bookRepository.delete(existing);
    }

    public Book borrow(Long id) {
        Book book = findById(id);
        if (!book.isAvailable()) {
            throw new BookUnavailableException(id);
        }
        book.setAvailable(false);
        return bookRepository.save(book);
    }

    public Book returnBook(Long id) {
        Book book = findById(id);
        book.setAvailable(true);
        return bookRepository.save(book);
    }
}
