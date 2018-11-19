package org.jm.interview.mtr.web.exception;

import org.jm.interview.mtr.service.exception.InsufficientMoneyAmountException;

public class InsufficientMoneyAmountMapper extends ExceptionMapper<InsufficientMoneyAmountException> {

    public InsufficientMoneyAmountMapper() {
        super(400);
    }
}
