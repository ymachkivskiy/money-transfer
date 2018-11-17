package org.jm.interview.mtr.infra.container;

import com.google.inject.Provider;
import lombok.RequiredArgsConstructor;
import org.jm.interview.mtr.web.ServerConfigurer;
import org.jooby.Jooby;

import javax.inject.Inject;


@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ServerProvider implements Provider<Jooby> {

    private final ServerConfigurer configurer;

    @Override
    public Jooby get() {

        Jooby jooby = new Jooby();


        configurer.configure(jooby);

        return jooby;
    }


}
