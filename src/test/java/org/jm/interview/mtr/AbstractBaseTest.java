package org.jm.interview.mtr;

import com.jayway.restassured.internal.mapper.ObjectMapperType;
import org.jm.interview.mtr.service.Account;
import org.jm.interview.mtr.service.AccountId;
import org.junit.Test;

import java.net.ServerSocket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.jayway.restassured.RestAssured.given;

public class AbstractBaseTest {

    public static final String BASE_URI;

    static {
        final App app = new App();

        final ExecutorService exec = Executors.newFixedThreadPool(2);
        final CountDownLatch started = new CountDownLatch(1);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            app.stop();
            exec.shutdown();
        }));

        // lambda is not working here when passing runnable
        app.onStarted(new Runnable() {
            @Override
            public void run() {
                started.countDown();
            }
        });

        int port;
        app.port(port = findRandomOpenPort(2));

        exec.execute(app::run);

        try {
            started.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.exit(1);
        }


        BASE_URI = "http://localhost:" + port;
    }

    private static Integer findRandomOpenPort(int retry) {
        if (retry <= 0) {
            System.exit(1);
        }

        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (Exception e) {
            e.printStackTrace();
            return findRandomOpenPort(retry - 1);
        }
    }

    @Test
    public void SHould() {

        createNewAccount();
    }

    protected Account createNewAccount() {

        Account as = given()
                .baseUri(BASE_URI)
                .when().post("/accounts")
                .then()
//                .statusCode(201)
                .extract()
                .response()
                .as(Account.class, ObjectMapperType.GSON);


        return null;
    }

}
