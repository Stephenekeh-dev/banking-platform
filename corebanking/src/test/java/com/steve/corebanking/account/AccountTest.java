package com.steve.corebanking.account;

import com.steve.corebanking.customer.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class AccountTest {

    private Account account;
    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(1L);

        account = new Account();
        account.setId(1L);
        account.setAccountNumber("012123456");
        account.setAccountType(AccountType.SAVINGS);
        account.setBalance(BigDecimal.valueOf(1000));
        account.setCustomer(customer);
    }

    // =====================
    // TEST GETTERS/SETTERS
    // =====================
    @Test
    void shouldSetAndGetId() {
        assertThat(account.getId()).isEqualTo(1L);
        account.setId(2L);
        assertThat(account.getId()).isEqualTo(2L);
    }

    @Test
    void shouldSetAndGetAccountNumber() {
        assertThat(account.getAccountNumber()).isEqualTo("012123456");
        account.setAccountNumber("012654321");
        assertThat(account.getAccountNumber()).isEqualTo("012654321");
    }

    @Test
    void shouldSetAndGetAccountType() {
        assertThat(account.getAccountType()).isEqualTo(AccountType.SAVINGS);
        account.setAccountType(AccountType.CURRENT);
        assertThat(account.getAccountType()).isEqualTo(AccountType.CURRENT);
    }

    @Test
    void shouldSetAndGetBalance() {
        assertThat(account.getBalance()).isEqualByComparingTo("1000");
        account.setBalance(BigDecimal.valueOf(500));
        assertThat(account.getBalance()).isEqualByComparingTo("500");
    }

    @Test
    void shouldHaveCustomer() {
        assertThat(account.getCustomer()).isEqualTo(customer);
        Customer newCustomer = new Customer();
        newCustomer.setId(2L);
        account.setCustomer(newCustomer);
        assertThat(account.getCustomer()).isEqualTo(newCustomer);
    }

    // =====================
    // DEFAULT VALUE TEST
    // =====================
    @Test
    void shouldHaveDefaultBalanceZero() {
        Account newAccount = new Account();
        assertThat(newAccount.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}