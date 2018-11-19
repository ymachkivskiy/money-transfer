package org.jm.interview.mtr.web.convert;

import org.jm.interview.mtr.service.Money;

import java.math.BigDecimal;

public class MoneyConverter extends Converter<Money> {

    @Override
    public Money convert(String value) {
        return Money.fromValue(new BigDecimal(value));
    }
}
