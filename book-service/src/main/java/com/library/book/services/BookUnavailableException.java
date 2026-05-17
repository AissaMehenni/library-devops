package com.library.book.services;

public class BookUnavailableException extends RuntimeException {
    public BookUnavailableException(Long id) {
        super("Book with id " + id + " is currently unavailable");
    }
}
