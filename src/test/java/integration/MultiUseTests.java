package integration;

import io.netty.util.concurrent.DefaultThreadFactory;
import org.jm.interview.mtr.service.Account;
import org.jm.interview.mtr.service.Money;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.google.common.base.Preconditions.checkArgument;
import static integration.TestServer.createNewAccount;
import static integration.TestServer.getExistingAccount;
import static integration.TestServer.rechargeAccount;
import static java.util.Arrays.asList;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

public class MultiUseTests {

    @Test
    public void should_commit_all_money_to_recharged_account() throws InterruptedException {
        //given
        Account newAccount = createNewAccount();

        final Money piece = Money.fromValue(5);
        final int workerCount = 10;
        final int workerIterations = 100;

        //when
        performConcurrentWork(duplicateWorker(workerCount, () -> {
            for (int j = 0; j < workerIterations; j++) {
                rechargeAccount(newAccount.getAccountId(), piece);
            }
        }));

        //then
        assertThat(getExistingAccount(newAccount.getAccountId()).getBalance())
                .isEqualByComparingTo(Money.fromValue(5 * workerCount * workerIterations));
    }

    @Test
    public void should_commit_all_money_transfers_between_different_accounts() throws InterruptedException {
        //given
        Account account_A = createAccountWithBalance(5000);
        Account account_B = createAccountWithBalance(10000);
        Account account_C = createAccountWithBalance(15000);

        /*                              A   |   B   |   C
           A -> B  (5  units)  x 50    -250 |  +250 |
           A -> C  (6  units)  x 50    -300 |       | +300
           B -> A  (7  units)  x 50    +350 |  -350 |
           B -> C  (10 units)  x 50         |  -500 | +500
           C -> A  (15 units)  x 50    +750 |       | -750
           C -> B  (20 units)  x 50         | +1000 | -1000
         */

        //when

        performConcurrentWork(asList(
                multiplyAction(50, transferMoneyAction(account_A, account_B, 5)),
                multiplyAction(50, transferMoneyAction(account_A, account_C, 6)),
                multiplyAction(50, transferMoneyAction(account_B, account_A, 7)),
                multiplyAction(50, transferMoneyAction(account_B, account_C, 10)),
                multiplyAction(50, transferMoneyAction(account_C, account_A, 15)),
                multiplyAction(50, transferMoneyAction(account_C, account_B, 20))
        ));

        //then
        assertThat(getExistingAccount(account_A.getAccountId()).getBalance())
                .isEqualByComparingTo(Money.fromValue(5000 - 250 - 300 + 350 + 750));
        assertThat(getExistingAccount(account_B.getAccountId()).getBalance())
                .isEqualByComparingTo(Money.fromValue(10000 + 250 - 350 - 500 + 1000));
        assertThat(getExistingAccount(account_C.getAccountId()).getBalance())
                .isEqualByComparingTo(Money.fromValue(15000 + 300 + 500 - 750 - 1000));


    }

    private static void performConcurrentWork(Collection<Runnable> workers) throws InterruptedException {

        ExecutorService executorService = newFixedThreadPool(workers.size(), new DefaultThreadFactory("test-worker-%d"));

        try {

            final CountDownLatch start = new CountDownLatch(1);
            final CountDownLatch end = new CountDownLatch(workers.size());

            for (Runnable w : workers) {
                executorService.execute(() -> {
                    try {
                        start.await();

                        w.run();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        end.countDown();
                    }
                });
            }

            start.countDown();
            end.await();

        } finally {
            executorService.shutdown();
            executorService.awaitTermination(10, TimeUnit.SECONDS);
        }

    }

    private static Runnable transferMoneyAction(Account sourceAccount, Account destinationAccount, long money) {
        return () -> TestServer.transferMoney(sourceAccount.getAccountId(), destinationAccount.getAccountId(), Money.fromValue(money));
    }

    private static Collection<Runnable> duplicateWorker(int count, Runnable single) {
        return IntStream.range(0, count)
                .mapToObj(i -> single)
                .collect(toList());
    }

    private static Runnable multiplyAction(int multiplier, Runnable action) {
        checkArgument(multiplier > 0);
        return () -> {
            for (int i = 0; i < multiplier; i++) {
                action.run();
            }
        };
    }

    private static Account createAccountWithBalance(int i) {
        Account account = createNewAccount();
        rechargeAccount(account.getAccountId(), Money.fromValue(i));
        return account;
    }
}
