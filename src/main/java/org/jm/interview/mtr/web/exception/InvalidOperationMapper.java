package org.jm.interview.mtr.web.exception;

import org.jm.interview.mtr.service.exception.InvalidOperationException;

public class InvalidOperationMapper extends ExceptionMapper<InvalidOperationException> {

    public InvalidOperationMapper() {
        super(400);
    }
}
