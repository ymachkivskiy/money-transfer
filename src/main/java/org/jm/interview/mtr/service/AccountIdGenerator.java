package org.jm.interview.mtr.service;

import java.util.UUID;

public class AccountIdGenerator {

    public AccountId generateNewId() {
        return AccountId.fromString(UUID.randomUUID().toString());
    }

}
