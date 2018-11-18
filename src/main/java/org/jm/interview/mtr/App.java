package org.jm.interview.mtr;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.jm.interview.mtr.infra.container.MoneyTransferModule;
import org.jooby.Jooby;

public class App {

    private final Jooby instance;

    public App() {
        Injector injector = Guice.createInjector(new MoneyTransferModule());
        instance = injector.getInstance(Jooby.class);
    }

    public void run() {
        instance.start();
    }

    public void stop() {
        instance.stop();
    }

    public App port(int port) {
        instance.port(port);
        return this;
    }

    public App onStarted(Runnable callback) {
        instance.onStarted(callback::run);
        return this;
    }

    public static void main(final String[] args) {
        new App().run();

    }

}
