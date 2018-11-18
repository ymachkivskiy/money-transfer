package org.jm.interview.mtr.web.endpoints;

import lombok.RequiredArgsConstructor;
import org.jm.interview.mtr.service.Account;
import org.jm.interview.mtr.service.AccountId;
import org.jm.interview.mtr.service.AccountService;
import org.jm.interview.mtr.service.Money;
import org.jooby.MediaType;
import org.jooby.Result;
import org.jooby.Results;
import org.jooby.Status;
import org.jooby.mvc.GET;
import org.jooby.mvc.POST;
import org.jooby.mvc.Path;

import javax.inject.Inject;

@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class AccountsController {

    private final AccountService accountService;

    @POST
    @Path("/accounts")
    public Result createNewAccount() {
        Account newAccount = accountService.createAccount();
        return Results.with(newAccount, Status.CREATED).type(MediaType.json);
    }

    @GET
    @Path("/accounts/:accountId")
    public Account getAccount(AccountId accountId) {
        return accountService.getAccount(accountId);
    }

    @POST
    @Path("/accounts/:accountId/:money")
    public Account rechargeAccount(AccountId accountId, Money money) {
        return accountService.rechargeAccount(accountId, money);
    }


}
