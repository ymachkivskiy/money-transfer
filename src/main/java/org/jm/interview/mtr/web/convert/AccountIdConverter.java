package org.jm.interview.mtr.web.convert;

import org.jm.interview.mtr.service.AccountId;

public class AccountIdConverter extends Converter<AccountId> {

    @Override
    public AccountId convert(String value) {
        return AccountId.create(value);
    }
}
