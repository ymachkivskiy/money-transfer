package org.jm.interview.mtr.service;

import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.jm.interview.mtr.utils.FluentComparableMixin;

@Data
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class AccountId implements Comparable<AccountId>, FluentComparableMixin<AccountId> {

    private final String id;

    public static AccountId create(String id) {
        return new AccountId(id);
    }

    @Override
    public int compareTo(AccountId o) {
        return id.compareTo(o.id);
    }
}
