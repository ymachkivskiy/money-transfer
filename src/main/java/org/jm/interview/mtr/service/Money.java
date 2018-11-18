package org.jm.interview.mtr.service;

import lombok.*;
import org.jm.interview.mtr.utils.FluentComparableMixin;

import java.math.BigDecimal;

import static com.google.common.base.Preconditions.checkArgument;

@Data
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Money implements Comparable<Money>, FluentComparableMixin<Money> {

    public static final Money NONE = new Money(BigDecimal.ZERO);

    private final BigDecimal value;


    public static Money fromValue(BigDecimal value) {
        checkArgument(value.compareTo(BigDecimal.ZERO) >= 0);
        return new Money(value);
    }

    public static Money fromValue(long value) {
        return fromValue(BigDecimal.valueOf(value));
    }

    public static Money fromValue(double value) {
        return fromValue(BigDecimal.valueOf(value));
    }

    public Money add(Money money) {
        return new Money(value.add(money.value));
    }

    public Money subtract(Money money) {
        checkArgument(money.isEqualOrLessThan(this), "Subtraction of " + money + " from " + this + " is illegal");
        return new Money(value.subtract(money.value));
    }

    public Money subtractFrom(Money base) {
        return base.subtract(this);
    }

    @Override
    public int compareTo(Money money) {
        return value.compareTo(money.value);
    }
}
