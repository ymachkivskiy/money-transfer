package org.jm.interview.mtr.service;

public interface MoneyTransferService {

    void transferMoney(AccountId sourceAccountId, AccountId destinationAccountId, Money money);
}
