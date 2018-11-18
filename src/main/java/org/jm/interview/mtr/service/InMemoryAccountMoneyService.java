package org.jm.interview.mtr.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jm.interview.mtr.service.exception.AccountNotFoundException;
import org.jm.interview.mtr.service.exception.InsufficientMoneyAmountException;
import org.jm.interview.mtr.service.exception.InvalidOperationException;

import javax.inject.Inject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class InMemoryAccountMoneyService implements AccountService, MoneyTransferService {

    private final AccountIdGenerator accountIdGenerator;

    private final Map<AccountId, AccountLocker> accounts = new ConcurrentHashMap<>();

    @Override
    public Account getAccount(AccountId accountId) throws AccountNotFoundException {
        return getAccountLocker(accountId).account.get();
    }

    @Override
    public Account createAccount() {

        Account account = new Account(accountIdGenerator.generateNewId(), Money.NONE);
        accounts.put(account.getAccountId(), new AccountLocker(account));
        return account;
    }

    @Override
    public Account rechargeAccount(AccountId accountId, Money additionalMoney) {
        log.info("Recharge account {} :: {}", accountId, additionalMoney);
        return getAccountLocker(accountId).updateAccount(account -> account.updateMoney(additionalMoney::add));
    }

    @Override
    public void transferMoney(AccountId sourceAccountId, AccountId destinationAccountId, Money money) {

        log.info("Transfer {} -> {} :: {}", sourceAccountId, destinationAccountId, money);

        if (sourceAccountId.equals(destinationAccountId)) {
            throw new InvalidOperationException();
        }

        AccountLocker sourceLocker = getAccountLocker(sourceAccountId);
        AccountLocker destinationLocker = getAccountLocker(destinationAccountId);

        TransactionMember sourceAccountOperation = sourceLocker.asPartOfTransaction(
                account -> {
                    if (account.getBalance().isLessThan(money)) {
                        throw new InsufficientMoneyAmountException(account.getAccountId());
                    }
                },
                account -> account.updateMoney(money::subtractFrom)
        );

        TransactionMember destinationAccountOperation = destinationLocker.asPartOfTransaction(
                account -> account.updateMoney(money::add)
        );

        new AccountsTransaction(sourceAccountOperation, destinationAccountOperation).perform();
    }

    private AccountLocker getAccountLocker(AccountId accountId) {
        AccountLocker locker = accounts.get(accountId);
        if (locker == null) {
            throw new AccountNotFoundException(accountId);
        }
        return locker;
    }

    private static class AccountLocker {

        private final Lock lock = new ReentrantLock();

        private final AccountId accountId;
        private final AtomicReference<Account> account;

        public AccountLocker(Account account) {
            this.accountId = account.getAccountId();
            this.account = new AtomicReference<>(account);
        }

        public Account updateAccount(UnaryOperator<Account> accountUnaryOperator) {
            try {
                lock.lock();

                Account updatedAccount = accountUnaryOperator.apply(account.get());
                account.set(updatedAccount);
                return updatedAccount;

            } finally {
                lock.unlock();
            }
        }

        public void visit(Consumer<Account> accountConsumer) {
            try {
                lock.lock();

                accountConsumer.accept(account.get());

            } finally {
                lock.unlock();
            }
        }

        public TransactionMember asPartOfTransaction(UnaryOperator<Account> accountUnaryOperator) {
            return new TransactionMember(this, accountUnaryOperator);
        }

        public TransactionMember asPartOfTransaction(Consumer<Account> preconditionsChecker, UnaryOperator<Account> accountUnaryOperator) {
            return new TransactionMember(this, accountUnaryOperator, preconditionsChecker);
        }

    }

    private static class AccountsTransaction {

        private final TransactionMember m1;
        private final TransactionMember m2;

        public AccountsTransaction(TransactionMember m1, TransactionMember m2) {
            // use accountId for ordering to avoid dead locks
            if (m1.accountLocker.accountId.isLessThan(m2.accountLocker.accountId)) {
                this.m1 = m1;
                this.m2 = m2;
            } else {
                this.m1 = m2;
                this.m2 = m1;
            }
        }

        public void perform() {
            try {
                m1.accountLocker.lock.lock();
                m2.accountLocker.lock.lock();

                m1.checkPreconditions();
                m2.checkPreconditions();

                m1.commit();
                m2.commit();

            } finally {
                m2.accountLocker.lock.unlock();
                m1.accountLocker.lock.unlock();
            }

        }

    }

    private static class TransactionMember {

        private static final Consumer<Account> NOOP = account -> {
        };

        private final AccountLocker accountLocker;
        private final UnaryOperator<Account> operation;
        private final Consumer<Account> preconditionChecker;

        public TransactionMember(AccountLocker accountLocker, UnaryOperator<Account> operation, Consumer<Account> preconditionChecker) {
            this.accountLocker = accountLocker;
            this.operation = operation;
            this.preconditionChecker = preconditionChecker;
        }

        public TransactionMember(AccountLocker accountLocker, UnaryOperator<Account> operation) {
            this(accountLocker, operation, NOOP);
        }

        public void checkPreconditions() {
            accountLocker.visit(preconditionChecker);
        }

        public void commit() {
            accountLocker.updateAccount(operation);
        }
    }
}
