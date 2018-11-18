package org.jm.interview.mtr.service;

public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(AccountId accountId) {
        super("Account " + accountId.getId() + " not found");
    }
}
