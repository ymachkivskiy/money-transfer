package org.jm.interview.mtr.service;

public interface MoneyTransferService {
    void transferMoney(AccountId sourceAccount, AccountId destinationAccount, Money money);
}
