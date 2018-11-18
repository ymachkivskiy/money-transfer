package integration;

import org.jm.interview.mtr.service.Account;
import org.jm.interview.mtr.service.AccountId;
import org.jm.interview.mtr.service.Money;
import org.junit.Assert;
import org.junit.Test;

import static integration.TestServer.createNewAccount;
import static integration.TestServer.getExistingAccount;
import static integration.TestServer.rechargeAccount;
import static integration.TestServer.transferMoney;
import static integration.TestServer.transferMoneyAssert;
import static org.assertj.core.api.Assertions.assertThat;

public class MoneyTransferTests {

    @Test
    public void should_transfer_money_from_one_account_to_another() {
        //given
        Account src_account_100 = createAccountWithBalance(100);
        Account tgt_account_10 = createAccountWithBalance(10);

        //when
        transferMoney(src_account_100.getAccountId(), tgt_account_10.getAccountId(), Money.fromValue(22.5));

        Account src_account_updated = getExistingAccount(src_account_100.getAccountId());
        Account tgt_account_updated = getExistingAccount(tgt_account_10.getAccountId());

        //then
        assertThat(tgt_account_updated.getBalance()).isEqualByComparingTo(Money.fromValue(10 + 22.5));
        assertThat(src_account_updated.getBalance()).isEqualByComparingTo(Money.fromValue(100 - 22.5));
    }

    @Test
    public void should_fail_404_to_transfer_from_non_existing_account() {
        //given
        Account target_account = createNewAccount();

        //when
        transferMoneyAssert(AccountId.fromString("non-present-account"), target_account.getAccountId(), Money.fromValue(50))
                //then
                .statusCode(404);
    }

    @Test
    public void should_fail_404_to_transfer_to_non_existing_account() {
        //given
        Account source_account = createAccountWithBalance(150);

        //when
        transferMoneyAssert(source_account.getAccountId(), AccountId.fromString("non-present-account"), Money.fromValue(75))
                //then
                .statusCode(404);
    }

    @Test
    public void should_fail_to_transfer_when_not_enough_money_on_source_account() {
        //given
        Account src_account_15 = createAccountWithBalance(15);
        Account tgt_account_10 = createAccountWithBalance(10);

        //when
        transferMoneyAssert(src_account_15.getAccountId(), tgt_account_10.getAccountId(), Money.fromValue(25))
                //then
                .statusCode(400);
    }

    @Test
    public void should_fail_transfer_from_account_to_same_account() {
        //given
        Account existingAccount = createAccountWithBalance(25);
        //when
        transferMoneyAssert(existingAccount.getAccountId(), existingAccount.getAccountId(), Money.fromValue(10))
                //then
                .statusCode(400);
    }

    private static Account createAccountWithBalance(int i) {
        Account account = createNewAccount();
        rechargeAccount(account.getAccountId(), Money.fromValue(i));
        return account;
    }
}
