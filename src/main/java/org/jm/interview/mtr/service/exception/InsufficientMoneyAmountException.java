package org.jm.interview.mtr.service.exception;

import org.jm.interview.mtr.service.AccountId;

public class InsufficientMoneyAmountException extends RuntimeException {

    public InsufficientMoneyAmountException(AccountId accountId) {
        super("Account has insufficient mount amount to perform operation: " + accountId.getId());
    }
}
