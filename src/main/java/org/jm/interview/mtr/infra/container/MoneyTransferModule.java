package org.jm.interview.mtr.infra.container;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import org.jm.interview.mtr.service.AccountService;
import org.jm.interview.mtr.service.InMemoryAccountMoneyService;
import org.jm.interview.mtr.service.MoneyTransferService;
import org.jm.interview.mtr.web.convert.AccountIdConverter;
import org.jm.interview.mtr.web.convert.Converter;
import org.jm.interview.mtr.web.convert.MoneyConverter;
import org.jm.interview.mtr.web.exception.AccountNotFoundMapper;
import org.jm.interview.mtr.web.exception.ExceptionMapper;
import org.jm.interview.mtr.web.exception.InsufficientMoneyAmountMapper;
import org.jm.interview.mtr.web.exception.InvalidOperationMapper;
import org.jooby.Jooby;

public class MoneyTransferModule extends AbstractModule {

    @Override
    protected void configure() {

        Multibinder<Converter<?>> converterBinder = Multibinder.newSetBinder(binder(), new TypeLiteral<Converter<?>>() {
        });
        converterBinder.addBinding().to(MoneyConverter.class);
        converterBinder.addBinding().to(AccountIdConverter.class);

        Multibinder<ExceptionMapper<? extends Throwable>> emBinder = Multibinder.newSetBinder(binder(), new TypeLiteral<ExceptionMapper<? extends Throwable>>() {
        });
        emBinder.addBinding().to(AccountNotFoundMapper.class);
        emBinder.addBinding().to(InvalidOperationMapper.class);
        emBinder.addBinding().to(InsufficientMoneyAmountMapper.class);

        bind(InMemoryAccountMoneyService.class).in(Singleton.class);
        bind(AccountService.class).to(InMemoryAccountMoneyService.class);
        bind(MoneyTransferService.class).to(InMemoryAccountMoneyService.class);

    }

    @Provides
    @Inject
    public Jooby joobyServer(ServerConfigurer serverConfigurer) {
        Jooby joobyServer = new Jooby();
        serverConfigurer.configure(joobyServer);
        return joobyServer;
    }
}
