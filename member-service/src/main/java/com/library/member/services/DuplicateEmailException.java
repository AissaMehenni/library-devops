package com.library.member.services;

public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException(String email) {
        super("Member with email " + email + " already exists");
    }
}
