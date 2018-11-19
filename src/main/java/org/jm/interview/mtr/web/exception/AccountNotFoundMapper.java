package org.jm.interview.mtr.web.exception;

import org.jm.interview.mtr.service.exception.AccountNotFoundException;

public class AccountNotFoundMapper extends ExceptionMapper<AccountNotFoundException> {

    public AccountNotFoundMapper() {
        super(404);
    }
}
