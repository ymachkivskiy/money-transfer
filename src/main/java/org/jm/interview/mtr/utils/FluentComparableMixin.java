package org.jm.interview.mtr.utils;

public interface FluentComparableMixin<T> extends Comparable<T> {


    default boolean isLessThan(T other) {
        return compareTo(other) < 0;
    }

    default boolean isGreaterThan(T other) {
        return compareTo(other) > 0;
    }

    default boolean isEqualOrLessThan(T other) {
        return compareTo(other) <= 0;
    }

    default boolean isEqualOrGreaterThan(T other) {
        return compareTo(other) >= 0;
    }
}
