package com.library.book.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.book.data.Book;
import com.library.book.services.BookNotFoundException;
import com.library.book.services.BookService;
import com.library.book.services.BookUnavailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookController.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookService bookService;

    @Autowired
    private ObjectMapper objectMapper;

    private Book book;

    @BeforeEach
    void setUp() {
        book = new Book(1L, "Clean Code", "Robert Martin", true);
    }

    @Test
    void getAllShouldReturnList() throws Exception {
        given(bookService.findAll()).willReturn(List.of(book));

        mockMvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Clean Code"))
                .andExpect(jsonPath("$[0].author").value("Robert Martin"))
                .andExpect(jsonPath("$[0].available").value(true));
    }

    @Test
    void getByIdShouldReturnBook() throws Exception {
        given(bookService.findById(1L)).willReturn(book);

        mockMvc.perform(get("/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Clean Code"));
    }

    @Test
    void getByIdShouldReturn404WhenMissing() throws Exception {
        given(bookService.findById(99L)).willThrow(new BookNotFoundException(99L));

        mockMvc.perform(get("/books/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Book not found with id 99"))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void createShouldReturn201AndPersistedBook() throws Exception {
        Book input = new Book(null, "DDD", "Eric Evans", true);
        Book saved = new Book(2L, "DDD", "Eric Evans", true);
        given(bookService.create(any(Book.class))).willReturn(saved);

        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.title").value("DDD"));
    }

    @Test
    void createShouldReturn400WhenTitleIsBlank() throws Exception {
        Book invalid = new Book(null, "", "Author", true);

        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateShouldReturnUpdatedBook() throws Exception {
        Book input = new Book(null, "New", "Author", false);
        Book updated = new Book(1L, "New", "Author", false);
        given(bookService.update(eq(1L), any(Book.class))).willReturn(updated);

        mockMvc.perform(put("/books/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("New"))
                .andExpect(jsonPath("$.available").value(false));
    }

    @Test
    void updateShouldReturn404WhenMissing() throws Exception {
        Book input = new Book(null, "T", "A", true);
        given(bookService.update(eq(99L), any(Book.class)))
                .willThrow(new BookNotFoundException(99L));

        mockMvc.perform(put("/books/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteShouldReturn204() throws Exception {
        willDoNothing().given(bookService).delete(1L);

        mockMvc.perform(delete("/books/1"))
                .andExpect(status().isNoContent());

        verify(bookService).delete(1L);
    }

    @Test
    void deleteShouldReturn404WhenMissing() throws Exception {
        willThrow(new BookNotFoundException(99L)).given(bookService).delete(99L);

        mockMvc.perform(delete("/books/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void borrowShouldReturnBorrowedBook() throws Exception {
        Book borrowed = new Book(1L, "Clean Code", "Robert Martin", false);
        given(bookService.borrow(1L)).willReturn(borrowed);

        mockMvc.perform(post("/books/borrow/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false));
    }

    @Test
    void borrowShouldReturn409WhenUnavailable() throws Exception {
        given(bookService.borrow(1L)).willThrow(new BookUnavailableException(1L));

        mockMvc.perform(post("/books/borrow/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Book with id 1 is currently unavailable"));
    }

    @Test
    void borrowShouldReturn404WhenMissing() throws Exception {
        given(bookService.borrow(42L)).willThrow(new BookNotFoundException(42L));

        mockMvc.perform(post("/books/borrow/42"))
                .andExpect(status().isNotFound());
    }

    @Test
    void returnShouldReturnAvailableBook() throws Exception {
        Book returned = new Book(1L, "Clean Code", "Robert Martin", true);
        given(bookService.returnBook(1L)).willReturn(returned);

        mockMvc.perform(post("/books/return/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void returnShouldReturn404WhenMissing() throws Exception {
        given(bookService.returnBook(42L)).willThrow(new BookNotFoundException(42L));

        mockMvc.perform(post("/books/return/42"))
                .andExpect(status().isNotFound());
    }
}
