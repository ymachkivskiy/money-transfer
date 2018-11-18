package org.jm.interview.mtr.infra.container;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.jm.interview.mtr.service.AccountService;
import org.jm.interview.mtr.service.InMemoryAccountMoneyService;
import org.jm.interview.mtr.service.MoneyTransferService;
import org.jm.interview.mtr.web.ServerConfigurer;
import org.jooby.Jooby;

public class MoneyTransferModule extends AbstractModule {

    @Override
    protected void configure() {

//        bind(ServerConfigurer.class);
//        bind(AccountIdGenerator.class);

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
