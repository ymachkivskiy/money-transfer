package org.jm.interview.mtr.web;

import com.google.inject.TypeLiteral;
import lombok.RequiredArgsConstructor;
import org.jm.interview.mtr.service.AccountService;
import org.jm.interview.mtr.service.Money;
import org.jm.interview.mtr.service.MoneyTransferService;
import org.jm.interview.mtr.service.exception.AccountNotFoundException;
import org.jm.interview.mtr.web.endpoints.AccountsController;
import org.jm.interview.mtr.web.endpoints.TransfersController;
import org.jooby.Jooby;
import org.jooby.Parser;
import org.jooby.Status;
import org.jooby.exec.Exec;
import org.jooby.json.Jackson;

import javax.inject.Inject;
import java.math.BigDecimal;

@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ServerConfigurer {

    private final AccountService accountService;
    private final MoneyTransferService moneyTransferService;

    public void configure(Jooby jooby) {

        jooby.use(new Jackson());
        jooby.use(new Exec());


        //3
        jooby.use(AccountsController.class);
        jooby.bind(AccountService.class, () -> accountService);

        jooby.use(TransfersController.class);
        jooby.bind(MoneyTransferService.class, () -> moneyTransferService);

        //1
        jooby.err(AccountNotFoundException.class, (req, rsp, ex) -> rsp.status(Status.NOT_FOUND).end());

        //2
        jooby.parser(new Parser() {
            @Override
            public Object parse(TypeLiteral<?> type, Context ctx) throws Throwable {

                if (type.getRawType() == Money.class) {

                    return ctx.param(values -> Money.fromValue(new BigDecimal(values.get(0))));
                }

                return ctx.next();

            }
        });

    }
}
