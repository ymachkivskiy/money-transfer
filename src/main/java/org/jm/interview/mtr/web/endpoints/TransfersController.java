package org.jm.interview.mtr.web.endpoints;

import lombok.RequiredArgsConstructor;
import org.jm.interview.mtr.service.AccountId;
import org.jm.interview.mtr.service.Money;
import org.jm.interview.mtr.service.MoneyTransferService;
import org.jooby.Result;
import org.jooby.Results;
import org.jooby.mvc.POST;
import org.jooby.mvc.Path;

import javax.inject.Inject;

@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class TransfersController {

    private final MoneyTransferService transferService;

    @POST
    @Path("/transfers/:sourceAccount/:destinationAccount/:money")
    public Result transferMoney(AccountId sourceAccount, AccountId destinationAccount, Money money) {
        transferService.transferMoney(sourceAccount, destinationAccount, money);
        return Results.ok();
    }
}
