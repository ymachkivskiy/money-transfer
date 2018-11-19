package org.jm.interview.mtr.web.convert;

import com.google.common.reflect.TypeToken;
import com.google.inject.TypeLiteral;

public abstract class Converter<T> {

    private final TypeToken<T> typeToken = new TypeToken<T>(getClass()) {
    };

    public final boolean supports(TypeLiteral<?> typeLiteral) {
        return typeLiteral.getRawType() == typeToken.getRawType();
    }

    public abstract T convert(String value);
}
