package org.jm.interview.mtr.service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DatabaseAccountService implements AccountService {

    private final Map<AccountId, Account> accounts = new ConcurrentHashMap<>();

    @Override
    public Account getAccount(AccountId accountId) throws AccountNotFoundException {
        Account account = accounts.get(accountId);
        if (account == null) {
            throw new AccountNotFoundException(accountId);
        }
        return account;
    }

    @Override
    public Account createAccount() {

        Account account = new Account(AccountId.fromString(UUID.randomUUID().toString()), Money.NONE);
        accounts.put(account.getAccountId(), account);
        return account;
    }

    @Override
    public Account rechargeAccount(AccountId accountId, Money additionalMoney) {

        if (!accounts.containsKey(accountId)) {
            throw new AccountNotFoundException(accountId);
        }

        return accounts.computeIfPresent(accountId,
                (accId, account) -> account.updateMoney(additionalMoney::add)
        );

    }
}
