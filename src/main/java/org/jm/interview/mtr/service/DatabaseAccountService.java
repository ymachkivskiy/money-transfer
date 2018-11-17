package org.jm.interview.mtr.service;

import java.math.BigDecimal;
import java.util.UUID;

public class DatabaseAccountService implements AccountService {

    @Override
    public Account getAccount(AccountId accountId) throws AccountNotFoundException {
        throw new AccountNotFoundException(accountId.getId().toString());
    }

    @Override
    public Account createAccount() {
        return new Account(AccountId.fromString(UUID.randomUUID().toString()), BigDecimal.ZERO);
    }

    @Override
    public Account rechargeAccount(AccountId accountId, Money money) {
        return null;
    }
}
