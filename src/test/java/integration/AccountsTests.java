package integration;

import org.jm.interview.mtr.service.Account;
import org.jm.interview.mtr.service.AccountId;
import org.jm.interview.mtr.service.Money;
import org.junit.Test;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static integration.TestServer.createNewAccount;
import static integration.TestServer.getAccountAssert;
import static integration.TestServer.getExistingAccount;
import static integration.TestServer.rechargeAccount;
import static integration.TestServer.rechargeAccountAssert;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;

public class AccountsTests {

    @Test
    public void should_create_new_account_with_zero_balance() {
        //when
        Account newAccount = createNewAccount();
        //then
        assertThat(newAccount.getBalance()).isEqualTo(Money.NONE);
    }

    @Test
    public void should_create_new_accounts_with_unique_ids() {
        //given
        final int accounts_num = 150;

        //when
        Set<AccountId> accountIds = IntStream.range(0, accounts_num)
                .mapToObj(i -> createNewAccount())
                .map(Account::getAccountId)
                .collect(toSet());

        //then
        assertThat(accountIds).hasSize(accounts_num);
    }

    @Test
    public void should_get_existing_account() {
        //given
        Account newAccount = createNewAccount();

        //when
        Account existingAccount = getExistingAccount(newAccount.getAccountId());

        //then
        assertThat(existingAccount).isEqualTo(newAccount);
    }

    @Test
    public void should_fail_404_when_getting_non_existing_account() {
        //when
        getAccountAssert(AccountId.create("non-existing-id"))
                //then
                .statusCode(404);
    }

    @Test
    public void should_recharge_existing_account() {
        //given
        Account newAccount = createNewAccount();

        //when
        Account rechargedAccount = rechargeAccount(newAccount.getAccountId(), Money.fromValue(150));

        //then
        assertThat(rechargedAccount.getAccountId()).isEqualTo(newAccount.getAccountId());
        assertThat(rechargedAccount.getBalance()).isEqualByComparingTo(Money.fromValue(150));
    }

    @Test
    public void should_commit_all_account_recharges() {
        //given
        List<Money> recharges = IntStream.range(0, 100)
                .mapToObj(i -> Money.fromValue(ThreadLocalRandom.current().nextInt(1, 100)))
                .collect(Collectors.toList());

        Account newAccount = createNewAccount();

        //when
        for (Money recharge : recharges) {
            rechargeAccount(newAccount.getAccountId(), recharge);
        }
        Account rechargedAccount = getExistingAccount(newAccount.getAccountId());

        //then
        assertThat(rechargedAccount.getBalance())
                .isEqualByComparingTo(recharges.stream().reduce(Money.NONE, Money::add));
    }

    @Test
    public void should_fail_404_when_recharging_non_existing_account() {
        //when
        rechargeAccountAssert(AccountId.create("some-404-account-id"), Money.fromValue(50))
                //then
                .statusCode(404);

    }
}
