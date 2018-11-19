package org.jm.interview.mtr.web.exception;

import com.google.common.reflect.TypeToken;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class ExceptionMapper<E extends Throwable> {

    private final TypeToken<E> typeToken = new TypeToken<E>(getClass()) {
    };

    @Getter
    private final int statusCode;

    public final boolean supports(Throwable throwable) {
        return throwable.getClass().isAssignableFrom(typeToken.getRawType())
                ||
                throwable.getCause() != null && throwable.getCause().getClass().isAssignableFrom(typeToken.getRawType());
    }
}
