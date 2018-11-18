package org.jm.interview.mtr.web;

import com.google.inject.TypeLiteral;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.RequiredArgsConstructor;
import org.jm.interview.mtr.service.AccountNotFoundException;
import org.jm.interview.mtr.service.AccountService;
import org.jm.interview.mtr.service.Money;
import org.jm.interview.mtr.service.MoneyTransferService;
import org.jm.interview.mtr.web.endpoints.AccountsController;
import org.jm.interview.mtr.web.endpoints.TransfersController;
import org.jooby.Jooby;
import org.jooby.Parser;
import org.jooby.Status;
import org.jooby.exec.Exec;
import org.jooby.json.Jackson;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.concurrent.Executors;

@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ServerConfigurer {

    private final AccountService accountService;
    private final MoneyTransferService moneyTransferService;

    public void configure(Jooby jooby) {

        jooby.use(new Jackson());
        jooby.use(new Exec());

        jooby.use(AccountsController.class);
        jooby.bind(AccountService.class, () -> accountService);

        jooby.use(TransfersController.class);
        jooby.bind(MoneyTransferService.class, () -> moneyTransferService);

        jooby.err(AccountNotFoundException.class, (req, rsp, ex) -> rsp.status(Status.NOT_FOUND).end());

        jooby.parser(new Parser() {
            @Override
            public Object parse(TypeLiteral<?> type, Context ctx) throws Throwable {

                if (type.getRawType() == Money.class) {

                    return ctx.param(values -> Money.fromValue(new BigDecimal(values.get(0))));
                }

                return ctx.next();

            }
        });

        jooby.executor(Executors.newFixedThreadPool(10, new DefaultThreadFactory("http-worker-%d")));
    }
}
