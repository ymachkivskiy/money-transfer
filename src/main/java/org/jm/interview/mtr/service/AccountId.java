package org.jm.interview.mtr.service;

import lombok.Data;

@Data(staticConstructor = "fromString")
public class AccountId {
    private final String id;
}
