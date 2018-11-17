package org.jm.interview.mtr.service;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class Account {
    private final AccountId accountId;
    private final BigDecimal balance;
}
