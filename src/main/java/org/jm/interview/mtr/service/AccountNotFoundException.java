package org.jm.interview.mtr.service;

public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(String s) {
        super(s);
    }
}
