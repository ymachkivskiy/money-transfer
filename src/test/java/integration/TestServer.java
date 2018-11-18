package integration;

import com.jayway.restassured.internal.mapper.ObjectMapperType;
import com.jayway.restassured.response.ValidatableResponse;
import org.jm.interview.mtr.App;
import org.jm.interview.mtr.service.Account;
import org.jm.interview.mtr.service.AccountId;
import org.jm.interview.mtr.service.Money;

import java.net.ServerSocket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.jayway.restassured.RestAssured.given;

class TestServer {

    private static final String BASE_URI;

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

    public static Account createNewAccount() {
        return given()
                .baseUri(BASE_URI)
                .when().post("/accounts")
                .then()
                .statusCode(201)
                .extract()
                .response()
                .as(Account.class, ObjectMapperType.GSON);
    }

    public static Account getExistingAccount(AccountId accountId) {
        return getAccountAssert(accountId)
                .statusCode(200)
                .extract().response()
                .as(Account.class, ObjectMapperType.GSON);
    }

    public static Account rechargeAccount(AccountId accountId, Money money) {
        return rechargeAccountAssert(accountId, money)
                .statusCode(200)
                .extract().response()
                .as(Account.class, ObjectMapperType.GSON);
    }

    public static void transferMoney(AccountId sourceAccount, AccountId destinationAccount, Money money) {
        transferMoneyAssert(sourceAccount, destinationAccount, money)
                .statusCode(200);
    }

    public static ValidatableResponse getAccountAssert(AccountId accountId) {
        return given()
                .baseUri(BASE_URI)
                .when().get("/accounts/{accountId}", accountId.getId())
                .then();
    }

    public static ValidatableResponse rechargeAccountAssert(AccountId accountId, Money money) {
        return given()
                .baseUri(BASE_URI)
                .when().post("/accounts/{accountId}/{money}", accountId.getId(), money.getValue())
                .then();
    }

    public static ValidatableResponse transferMoneyAssert(AccountId sourceAccount, AccountId destinationAccount, Money money) {
        return given()
                .baseUri(BASE_URI)
                .when().post("/transfers/{sourceAccount}/{destinationAccount}/{money}", sourceAccount.getId(), destinationAccount.getId(), money.getValue())
                .then();
    }


}
