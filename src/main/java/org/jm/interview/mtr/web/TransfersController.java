package org.jm.interview.mtr.web;

import lombok.RequiredArgsConstructor;
import org.jm.interview.mtr.service.AccountId;
import org.jm.interview.mtr.service.Money;
import org.jm.interview.mtr.service.MoneyTransferService;
import org.jooby.mvc.POST;
import org.jooby.mvc.Path;

import javax.inject.Inject;

@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class TransfersController {

    private final MoneyTransferService transferService;

    @POST
    @Path("/transfers/:sourceAccount/:destinationAccount/:money")
    public void transferMoney(AccountId sourceAccount, AccountId destinationAccount, Money money) {
        transferService.transferMoney(sourceAccount, destinationAccount, money);
    }
}
