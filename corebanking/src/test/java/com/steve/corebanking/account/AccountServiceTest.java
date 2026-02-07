package com.steve.corebanking.account;

import com.steve.corebanking.exception.InsufficientFundsException;
import com.steve.corebanking.exception.NotFoundException;
import com.steve.corebanking.account.Account;
import com.steve.corebanking.account.AccountType;
import com.steve.corebanking.customer.Customer;
import com.steve.corebanking.account.AccountRepository;
import com.steve.corebanking.customer.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private AccountService accountService;

    private Account account;
    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(1L);

        account = new Account();
        account.setId(1L);
        account.setAccountNumber("012123456");
        account.setBalance(BigDecimal.valueOf(1000));
        account.setCustomer(customer);
    }

    // =========================
    // GET BY ACCOUNT NUMBER
    // =========================
    @Test
    void shouldReturnAccountWhenAccountNumberExists() {
        when(accountRepository.findByAccountNumber("012123456"))
                .thenReturn(Optional.of(account));

        Account result = accountService.getByAccountNumber("012123456");

        assertThat(result).isNotNull();
        assertThat(result.getAccountNumber()).isEqualTo("012123456");
    }

    @Test
    void shouldThrowExceptionWhenAccountNumberNotFound() {
        when(accountRepository.findByAccountNumber("000"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                accountService.getByAccountNumber("000"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Account not found");
    }

    // =========================
    // CREDIT ACCOUNT
    // =========================
    @Test
    void shouldCreditAccountSuccessfully() {
        when(accountRepository.findByAccountNumber("012123456"))
                .thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class)))
                .thenReturn(account);

        Account result = accountService.creditAccount(
                "012123456", BigDecimal.valueOf(500));

        assertThat(result.getBalance())
                .isEqualByComparingTo("1500");
    }

    @Test
    void shouldRejectCreditWithInvalidAmount() {
        assertThatThrownBy(() ->
                accountService.creditAccount("012123456", BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // =========================
    // DEBIT ACCOUNT
    // =========================
    @Test
    void shouldDebitAccountSuccessfully() {
        when(accountRepository.findByAccountNumber("012123456"))
                .thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class)))
                .thenReturn(account);

        Account result = accountService.debitAccount(
                "012123456", BigDecimal.valueOf(300));

        assertThat(result.getBalance())
                .isEqualByComparingTo("700");
    }

    @Test
    void shouldThrowExceptionWhenInsufficientFunds() {
        when(accountRepository.findByAccountNumber("012123456"))
                .thenReturn(Optional.of(account));

        assertThatThrownBy(() ->
                accountService.debitAccount("012123456", BigDecimal.valueOf(2000)))
                .isInstanceOf(InsufficientFundsException.class);
    }

    // =========================
    // CREATE ACCOUNT
    // =========================
    @Test
    void shouldCreateNewAccountForCustomer() {
        when(customerRepository.findById(1L))
                .thenReturn(Optional.of(customer));
        when(accountRepository.existsByAccountNumber(anyString()))
                .thenReturn(false);
        when(accountRepository.save(any(Account.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Account result = accountService.createAccount(1L, AccountType.SAVINGS);

        assertThat(result).isNotNull();
        assertThat(result.getBalance())
                .isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getAccountType())
                .isEqualTo(AccountType.SAVINGS);
    }

    // =========================
    // GET ACCOUNT BY ID
    // =========================
    @Test
    void shouldGetAccountById() {
        when(accountRepository.findById(1L))
                .thenReturn(Optional.of(account));

        Account result = accountService.getAccount(1L);

        assertThat(result).isNotNull();
    }

    // =========================
    // GET ALL ACCOUNTS
    // =========================
    @Test
    void shouldReturnAllAccounts() {
        when(accountRepository.findAll())
                .thenReturn(List.of(account));

        List<Account> result = accountService.getAllAccounts();

        assertThat(result).hasSize(1);
    }
}