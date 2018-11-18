package integration;

import org.jm.interview.mtr.service.Account;
import org.jm.interview.mtr.service.Money;
import org.junit.Test;

import static integration.TestServer.createNewAccount;
import static integration.TestServer.getExistingAccount;
import static integration.TestServer.rechargeAccount;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static utils.ConcurrentUtils.duplicateWorker;
import static utils.ConcurrentUtils.multiplyAction;
import static utils.ConcurrentUtils.performConcurrentWork;

public class MultiUseTests {

    @Test(timeout = 20_000)
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

    @Test(timeout = 30_000)
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

    private static Runnable transferMoneyAction(Account sourceAccount, Account destinationAccount, long money) {
        return () -> TestServer.transferMoney(sourceAccount.getAccountId(), destinationAccount.getAccountId(), Money.fromValue(money));
    }

    private static Account createAccountWithBalance(int i) {
        Account account = createNewAccount();
        rechargeAccount(account.getAccountId(), Money.fromValue(i));
        return account;
    }
}
