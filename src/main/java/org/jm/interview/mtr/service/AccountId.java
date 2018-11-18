package org.jm.interview.mtr.service;

import lombok.Data;
import org.jm.interview.mtr.utils.FluentComparableMixin;

@Data(staticConstructor = "fromString")
public class AccountId implements Comparable<AccountId>, FluentComparableMixin<AccountId> {

    private final String id;

    @Override
    public int compareTo(AccountId o) {
        return id.compareTo(o.id);
    }
}
