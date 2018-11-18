package org.jm.interview.mtr.service;

import org.jm.interview.mtr.service.exception.AccountNotFoundException;

public interface AccountService {

    Account getAccount(AccountId accountId) throws AccountNotFoundException;

    Account createAccount();

    Account rechargeAccount(AccountId accountId, Money money) throws AccountNotFoundException;
}
