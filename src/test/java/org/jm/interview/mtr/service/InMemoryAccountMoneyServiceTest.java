package org.jm.interview.mtr.service;

import org.jm.interview.mtr.service.exception.AccountNotFoundException;
import org.jm.interview.mtr.service.exception.InsufficientMoneyAmountException;
import org.jm.interview.mtr.service.exception.InvalidOperationException;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static utils.ConcurrentUtils.duplicateWorker;
import static utils.ConcurrentUtils.multiplyAction;
import static utils.ConcurrentUtils.performConcurrentWork;
import static utils.ListUtils.merge;

public class InMemoryAccountMoneyServiceTest {

    private InMemoryAccountMoneyService service;

    @Before
    public void setUp() throws Exception {
        service = new InMemoryAccountMoneyService(new AccountIdGenerator());
    }

    //region HP tests

    @Test
    public void should_create_account() {
        //given
        //when
        Account account = service.createAccount();

        //then
        assertThat(account.getBalance()).isEqualByComparingTo(Money.NONE);
    }

    @Test
    public void should_get_existing_account() {
        //given
        Account existingAccount = service.createAccount();

        //when
        Account account = service.getAccount(existingAccount.getAccountId());

        //then
        assertThat(account).isEqualTo(existingAccount);
    }

    @Test
    public void should_recharge_account() {
        //given
        Account account = service.createAccount();

        //when
        Account updated = service.rechargeAccount(account.getAccountId(), Money.fromValue(150));

        //then
        assertThat(updated.getBalance()).isEqualByComparingTo(Money.fromValue(150));
    }

    @Test
    public void should_transfer_money_between_accounts() {
        //given
        Account srcAccount = service.createAccount();
        Account tgtAccount = service.createAccount();

        service.rechargeAccount(srcAccount.getAccountId(), Money.fromValue(100));

        //when
        service.transferMoney(srcAccount.getAccountId(), tgtAccount.getAccountId(), Money.fromValue(95));

        //then
        assertThat(getBalance(srcAccount)).isEqualByComparingTo(Money.fromValue(5));
        assertThat(getBalance(tgtAccount)).isEqualByComparingTo(Money.fromValue(95));
    }

    //endregion

    //region exceptional cases

    @Test
    public void should_throw_account_not_found_when_getting_not_existing_account() {
        //when
        Throwable throwable = catchThrowable(() -> service.getAccount(AccountId.create("404-account")));

        //then
        assertThat(throwable)
                .isNotNull()
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessageContaining("404-account");
    }

    @Test
    public void should_throw_account_not_found_when_recharging_not_existing_account() {
        //when
        Throwable throwable = catchThrowable(() -> service.rechargeAccount(AccountId.create("404-account"), Money.fromValue(55)));

        //then
        assertThat(throwable)
                .isNotNull()
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessageContaining("404-account");
    }

    @Test
    public void should_throw_account_not_found_when_transfer_from_non_existing_account() {
        //given
        Account tgt = service.createAccount();

        //when
        Throwable throwable = catchThrowable(() -> service.transferMoney(AccountId.create("source-404-account"), tgt.getAccountId(), Money.fromValue(55)));

        //then
        assertThat(throwable)
                .isNotNull()
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessageContaining("source-404-account");
    }

    @Test
    public void should_throw_account_not_found_when_transfer_to_non_existing_account() {
        //given
        Account src = service.createAccount();
        service.rechargeAccount(src.getAccountId(), Money.fromValue(1000));

        //when
        Throwable throwable = catchThrowable(() -> service.transferMoney(src.getAccountId(), AccountId.create("target-404-account"), Money.fromValue(55)));

        //then
        assertThat(throwable)
                .isNotNull()
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessageContaining("target-404-account");
    }

    @Test
    public void should_throw_insufficient_money_amount_when_transfer_from_account_without_enough_money() {
        //given
        Account src = service.createAccount();
        service.rechargeAccount(src.getAccountId(), Money.fromValue(50));
        Account tgt = service.createAccount();

        //when
        Throwable throwable = catchThrowable(transferAction(src, tgt, 55)::run);

        //then
        assertThat(throwable)
                .isNotNull()
                .isInstanceOf(InsufficientMoneyAmountException.class)
                .hasMessageContaining(src.getAccountId().getId());
    }

    @Test
    public void should_throw_invalid_operation_when_transfer_to_same_account() {
        //given
        Account account = service.createAccount();
        service.rechargeAccount(account.getAccountId(), Money.fromValue(50));

        //when
        Throwable throwable = catchThrowable(transferAction(account, account, 5)::run);

        //then
        assertThat(throwable)
                .isNotNull()
                .isInstanceOf(InvalidOperationException.class);
    }

    //endregion

    @Test(timeout = 15_000)
    public void should_commit_all_account_recharges() throws InterruptedException {
        //given
        Account account = service.createAccount();

        //when
        performConcurrentWork(
                duplicateWorker(10, multiplyAction(25, rechargeAction(account, 5)))
        );

        //then
        assertThat(getBalance(account))
                .isEqualByComparingTo(Money.fromValue(5 * 10 * 25));
    }

    @Test
    public void should_commit_all_ping_pong_transfers() throws InterruptedException {
        //given
        Account acc_A = createAccountWithBalance(10000);
        Account acc_B = createAccountWithBalance(20000);

        //when
        // A -> B 10units * 5 * 50
        // B -> A 13units * 5 * 50
        performConcurrentWork(
                merge(
                        duplicateWorker(5, multiplyAction(50, transferAction(acc_A, acc_B, 10))),
                        duplicateWorker(5, multiplyAction(50, transferAction(acc_B, acc_A, 13)))
                )
        );

        //then
        assertThat(getBalance(acc_A))
                .isEqualByComparingTo(Money.fromValue(10000 - (5 * 50 * 10) + (5 * 50 * 13)));
        assertThat(getBalance(acc_B))
                .isEqualByComparingTo(Money.fromValue(20000 + (5 * 50 * 10) - (5 * 50 * 13)));
    }

    @Test
    public void should_commit_all_tree_party_with_mediator_transfers() throws InterruptedException {
        //given
        Account acc_A = createAccountWithBalance(100_000);
        Account acc_B = createAccountWithBalance(200_000);
        Account acc_C = createAccountWithBalance(300_000);

        //when
        // A -> B 10units * 5 * 50
        // B -> C 13units * 5 * 50
        performConcurrentWork(
                merge(
                        duplicateWorker(5, multiplyAction(500, transferAction(acc_A, acc_B, 10))),
                        duplicateWorker(5, multiplyAction(500, transferAction(acc_B, acc_C, 13)))
                )
        );

        //then
        assertThat(getBalance(acc_A))
                .isEqualByComparingTo(Money.fromValue(100_000 - (5 * 500 * 10)));
        assertThat(getBalance(acc_B))
                .isEqualByComparingTo(Money.fromValue(200_000 - (5 * 500 * 13) + (5 * 500 * 10) ));
        assertThat(getBalance(acc_C))
                .isEqualByComparingTo(Money.fromValue(300_000 + (5 * 500 * 13)));
    }

    @Test
    public void should_commit_all_not_related_transfers() throws InterruptedException {
        //given
        Account acc_A = createAccountWithBalance(10000);
        Account acc_B = createAccountWithBalance(20000);
        Account acc_C = createAccountWithBalance(30000);
        Account acc_D = createAccountWithBalance(40000);

        //when
        // A -> B 10units * 5 * 50
        // B -> C 13units * 5 * 50
        performConcurrentWork(
                merge(
                        duplicateWorker(5, multiplyAction(50, transferAction(acc_A, acc_B, 10))),
                        duplicateWorker(5, multiplyAction(50, transferAction(acc_C, acc_D, 15)))
                )
        );

        //then
        assertThat(getBalance(acc_A))
                .isEqualByComparingTo(Money.fromValue(10000 - (5 * 50 * 10)));
        assertThat(getBalance(acc_B))
                .isEqualByComparingTo(Money.fromValue(20000 + (5 * 50 * 10)));
        assertThat(getBalance(acc_C))
                .isEqualByComparingTo(Money.fromValue(30000 - (5 * 50 * 15)));
        assertThat(getBalance(acc_D))
                .isEqualByComparingTo(Money.fromValue(40000 + (5 * 50 * 15)));
    }

    @Test
    public void should_commit_all_transfers_and_recharges() throws InterruptedException {
        //given
        Account acc_A = createAccountWithBalance(100_000);
        Account acc_B = createAccountWithBalance(200_000);

        //when
        // A -> B 10units * 5 * 50
        // B -> A 13units * 5 * 50
        performConcurrentWork(
                merge(
                        duplicateWorker(5, multiplyAction(500, transferAction(acc_A, acc_B, 10))),
                        duplicateWorker(5, multiplyAction(500, transferAction(acc_B, acc_A, 13))),
                        //
                        duplicateWorker(3, multiplyAction(500, rechargeAction(acc_A, 5))),
                        duplicateWorker(3, multiplyAction(500, rechargeAction(acc_B, 4)))
                )
        );

        //then
        assertThat(getBalance(acc_A))
                .isEqualByComparingTo(Money.fromValue(100_000 - (5 * 500 * 10) + (5 * 500 * 13) + (3 * 500 * 5)));
        assertThat(getBalance(acc_B))
                .isEqualByComparingTo(Money.fromValue(200_000 + (5 * 500 * 10) - (5 * 500 * 13) + (3 * 500 * 4)));
    }

    private Runnable transferAction(Account src, Account tgt, int money) {
        return () -> service.transferMoney(src.getAccountId(), tgt.getAccountId(), Money.fromValue(money));
    }

    private Runnable rechargeAction(Account account, int additionalMoney) {
        return () -> service.rechargeAccount(account.getAccountId(), Money.fromValue(additionalMoney));
    }

    private Account createAccountWithBalance(int money) {
        Account account = service.createAccount();
        service.rechargeAccount(account.getAccountId(), Money.fromValue(money));
        return account;
    }

    private Money getBalance(Account account) {
        return service.getAccount(account.getAccountId()).getBalance();
    }
}