package com.steve.corebanking.ledger;

import com.steve.corebanking.account.Account;
import com.steve.corebanking.account.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LedgerServiceTest {

    @Mock
    private LedgerRepository ledgerRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private LedgerService ledgerService;

    /* ============================
       CREATE SINGLE ENTRY
    ============================ */
    @Test
    void shouldCreateLedgerEntry() {
        LedgerEntry entry = new LedgerEntry();
        entry.setAccountNumber("012345678");
        entry.setAmount(BigDecimal.valueOf(1000));
        entry.setEntryType(LedgerEntry.EntryType.CREDIT);

        when(ledgerRepository.save(any(LedgerEntry.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        LedgerEntry saved = ledgerService.createEntry(entry);

        assertNotNull(saved.getCreatedAt());
        verify(ledgerRepository).save(entry);
    }

    /* ============================
       DOUBLE ENTRY CREATION
    ============================ */
    @Test
    void shouldCreateDoubleEntry() {
        ledgerService.createDoubleEntry(
                "ACC_DEBIT",
                "ACC_CREDIT",
                BigDecimal.valueOf(500),
                "TXN123",
                "Transfer"
        );

        ArgumentCaptor<LedgerEntry> captor =
                ArgumentCaptor.forClass(LedgerEntry.class);

        verify(ledgerRepository, times(2)).save(captor.capture());

        List<LedgerEntry> entries = captor.getAllValues();

        LedgerEntry debit = entries.get(0);
        LedgerEntry credit = entries.get(1);

        assertEquals(LedgerEntry.EntryType.DEBIT, debit.getEntryType());
        assertEquals(BigDecimal.valueOf(500), debit.getAmount());

        assertEquals(LedgerEntry.EntryType.CREDIT, credit.getEntryType());
        assertEquals(BigDecimal.valueOf(500), credit.getAmount());
    }

    /* ============================
       CALCULATE LEDGER BALANCE
    ============================ */
    @Test
    void shouldCalculateLedgerBalanceCorrectly() {
        List<LedgerEntry> entries = List.of(
                createEntry(LedgerEntry.EntryType.CREDIT, 1000.0),
                createEntry(LedgerEntry.EntryType.DEBIT, 400.0),
                createEntry(LedgerEntry.EntryType.CREDIT, 200.0)
        );

        when(ledgerRepository.findByAccountNumberOrderByCreatedAtDesc("ACC123"))
                .thenReturn(entries);

        BigDecimal balance =
                ledgerService.calculateLedgerBalance("ACC123");

        assertEquals(BigDecimal.valueOf(800.0), balance);
    }

    /* ============================
       RECONCILE ACCOUNT BALANCE
    ============================ */
    @Test
    void shouldReturnTrueWhenLedgerMatchesAccountBalance() {
        Account account = new Account();
        account.setBalance(BigDecimal.valueOf(500));

        when(accountRepository.findByAccountNumber("ACC123"))
                .thenReturn(Optional.of(account));

        when(ledgerRepository.findByAccountNumberOrderByCreatedAtDesc("ACC123"))
                .thenReturn(List.of(
                        createEntry(LedgerEntry.EntryType.CREDIT, 700),
                        createEntry(LedgerEntry.EntryType.DEBIT, 200)
                ));

        boolean reconciled =
                ledgerService.reconcileAccountBalance("ACC123");

        assertTrue(reconciled);
    }

    @Test
    void shouldReturnFalseWhenLedgerDoesNotMatchAccountBalance() {
        Account account = new Account();
        account.setBalance(BigDecimal.valueOf(300));

        when(accountRepository.findByAccountNumber("ACC123"))
                .thenReturn(Optional.of(account));

        when(ledgerRepository.findByAccountNumberOrderByCreatedAtDesc("ACC123"))
                .thenReturn(List.of(
                        createEntry(LedgerEntry.EntryType.CREDIT, 500)
                ));

        boolean reconciled =
                ledgerService.reconcileAccountBalance("ACC123");

        assertFalse(reconciled);
    }

    /* ============================
       HELPER METHOD
    ============================ */
    private LedgerEntry createEntry(
            LedgerEntry.EntryType type,
            double amount
    ) {
        LedgerEntry entry = new LedgerEntry();
        entry.setEntryType(type);
        entry.setAmount(BigDecimal.valueOf(amount));
        return entry;
    }
}
