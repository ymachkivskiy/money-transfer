package org.jm.interview.mtr.service;

import lombok.Data;

import java.util.function.UnaryOperator;

@Data
public class Account {
    private final AccountId accountId;
    private final Money balance;

    public Account updateMoney(UnaryOperator<Money> moneyUpdateOperator) {
        return new Account(accountId, moneyUpdateOperator.apply(balance));
    }
}
