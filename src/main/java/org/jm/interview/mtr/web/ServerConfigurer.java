package org.jm.interview.mtr.web;

import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.RequiredArgsConstructor;
import org.jm.interview.mtr.service.AccountNotFoundException;
import org.jm.interview.mtr.service.AccountService;
import org.jm.interview.mtr.service.MoneyTransferService;
import org.jooby.Jooby;
import org.jooby.Status;
import org.jooby.json.Jackson;

import javax.inject.Inject;
import java.util.concurrent.Executors;

@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ServerConfigurer {

    private final AccountService accountService;
    private final MoneyTransferService moneyTransferService;

    public void configure(Jooby jooby) {

        jooby.use(new Jackson());

        jooby.use(AccountsController.class);
        jooby.bind(AccountService.class, () -> accountService);

        jooby.use(TransfersController.class);
        jooby.bind(MoneyTransferService.class, () -> moneyTransferService);

        jooby.err(AccountNotFoundException.class, (req, rsp, ex) -> rsp.status(Status.NOT_FOUND).end());


        jooby.executor(Executors.newFixedThreadPool(10, new DefaultThreadFactory("http-worker-%d")));
    }
}
