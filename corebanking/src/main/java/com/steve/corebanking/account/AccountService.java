package com.steve.corebanking.account;

import com.steve.corebanking.customer.Customer;
import com.steve.corebanking.customer.CustomerRepository;
import com.steve.corebanking.exception.NotFoundException;
import com.steve.corebanking.exception.InsufficientFundsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;

    public AccountService(AccountRepository accountRepository,
                          CustomerRepository customerRepository) {
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
    }

    // ============================
    //   GET ACCOUNT BY NUMBER
    // ============================
    public Account getByAccountNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() ->
                        new NotFoundException("Account not found with number: " + accountNumber));
    }


    //      CREDIT ACCOUNT
    @Transactional
    public Account creditAccount(String accountNumber, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }

        Account acc = getByAccountNumber(accountNumber);

        acc.setBalance(acc.getBalance().add(amount));
        return accountRepository.save(acc);
    }

    // ============================
    //      DEBIT ACCOUNT
    // ============================
    @Transactional
    public Account debitAccount(String accountNumber, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }

        Account acc = getByAccountNumber(accountNumber);

        BigDecimal newBalance = acc.getBalance().subtract(amount);

        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new InsufficientFundsException("Insufficient funds for account: " + accountNumber);
        }

        acc.setBalance(newBalance);
        return accountRepository.save(acc);
    }

    // ============================
    //   GENERATE ACCOUNT NUMBER
    // ============================
    private String generateAccountNumber() {
        Random random = new Random();
        String number;

        do {
            number = "012" + (100000 + random.nextInt(900000));  // Example: 012543212
        } while (accountRepository.existsByAccountNumber(number));

        return number;
    }

    // ============================
    //     CREATE NEW ACCOUNT
    // ============================
    public Account createAccount(Long customerId, AccountType type) {

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() ->
                        new NotFoundException("Customer not found with ID: " + customerId));

        Account account = new Account();
        account.setCustomer(customer);
        account.setAccountType(type);
        account.setAccountNumber(generateAccountNumber());
        account.setBalance(BigDecimal.ZERO);

        return accountRepository.save(account);
    }

    // ============================
    //     GET ALL ACCOUNTS
    // ============================
    public List<Account> getAllAccounts() {

        return accountRepository.findAll();
    }

    // ============================
    //   GET ACCOUNT BY ID
    // ============================
    public Account getAccount(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() ->
                        new NotFoundException("Account not found with ID: " + id));
    }
}
