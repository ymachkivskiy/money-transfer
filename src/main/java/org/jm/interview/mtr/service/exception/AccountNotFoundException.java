package org.jm.interview.mtr.service.exception;

import org.jm.interview.mtr.service.AccountId;

public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(AccountId accountId) {
        super("Account " + accountId.getId() + " not found");
    }
}
