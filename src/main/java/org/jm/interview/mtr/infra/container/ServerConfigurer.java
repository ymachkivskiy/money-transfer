package org.jm.interview.mtr.infra.container;

import lombok.RequiredArgsConstructor;
import org.jm.interview.mtr.service.AccountService;
import org.jm.interview.mtr.service.MoneyTransferService;
import org.jm.interview.mtr.web.controller.AccountsController;
import org.jm.interview.mtr.web.controller.TransfersController;
import org.jm.interview.mtr.web.convert.Converter;
import org.jm.interview.mtr.web.exception.ExceptionMapper;
import org.jooby.Jooby;
import org.jooby.exec.Exec;
import org.jooby.json.Jackson;

import javax.inject.Inject;
import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ServerConfigurer {

    private final AccountService accountService;
    private final MoneyTransferService moneyTransferService;

    private final Set<Converter<?>> converters;
    private final Set<ExceptionMapper<? extends Throwable>> exceptionMappers;

    public void configure(Jooby jooby) {

        jooby.use(new Jackson());
        jooby.use(new Exec());

        setupControllers(jooby);
        setupConverters(jooby);
        setupErrorMappers(jooby);

        jooby.bind(AccountService.class, () -> accountService);
        jooby.bind(MoneyTransferService.class, () -> moneyTransferService);
    }

    private void setupControllers(Jooby jooby) {
        jooby.use(AccountsController.class);
        jooby.use(TransfersController.class);
    }

    private void setupConverters(Jooby jooby) {
        jooby.parser((type, ctx) -> {

            Optional<Object> convertedValue = converters.stream()
                    .filter(c -> c.supports(type))
                    .findFirst()
                    .map(c -> ctx.param(values -> c.convert(values.first())));

            if (convertedValue.isPresent()) {
                return convertedValue.get();
            }
            return ctx.next();

        });
    }

    private void setupErrorMappers(Jooby jooby) {
        jooby.err((req, rsp, ex) -> {

            exceptionMappers.stream()
                    .filter(mapper -> mapper.supports(ex))
                    .findFirst()
                    .ifPresent(mapper -> rsp.status(mapper.getStatusCode()).end());

        });
    }
}
