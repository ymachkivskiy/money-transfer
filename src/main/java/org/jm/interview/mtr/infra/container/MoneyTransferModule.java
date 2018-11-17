package org.jm.interview.mtr.infra.container;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import org.jm.interview.mtr.service.AccountService;
import org.jm.interview.mtr.service.DatabaseAccountService;
import org.jm.interview.mtr.service.MoneyTransferService;
import org.jm.interview.mtr.service.TransactionalMoneyTransferService;
import org.jm.interview.mtr.web.ServerConfigurer;
import org.jooby.Jooby;

public class MoneyTransferModule extends AbstractModule {

    @Override
    protected void configure() {

//        bind(Jooby.class).toProvider(ServerProvider.class);
        bind(ServerConfigurer.class);

        bind(AccountService.class).to(DatabaseAccountService.class);
        bind(MoneyTransferService.class).to(TransactionalMoneyTransferService.class);

    }


    @Provides
    @Inject
    public Jooby joobyServer(ServerConfigurer serverConfigurer) {
        Jooby joobyServer = new Jooby();
        serverConfigurer.configure(joobyServer);
        return joobyServer;
    }
}
