package com.steve.corebanking.transaction;

import com.steve.corebanking.account.AccountService;
import com.steve.corebanking.ledger.LedgerService;
import com.steve.corebanking.transaction.dto.TransactionDto;
import com.steve.corebanking.transaction.dto.TransactionResponseDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountService accountService;

    @Mock
    private LedgerService ledgerService;

    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private TransactionService transactionService;

    private TransactionDto validDepositDto() {
        TransactionDto dto = new TransactionDto();
        dto.setAccountNumber("0123456789");
        dto.setAmount(new BigDecimal("1000"));
        dto.setType(TransactionType.DEPOSIT);
        dto.setNarration("Initial deposit");
        return dto;
    }

    @Test
    void createTransaction_Deposit_ShouldCreditAccountAndSaveTransaction() {
        TransactionDto dto = validDepositDto();

        Transaction savedTx = new Transaction();
        savedTx.setReferenceId("ref-123");
        savedTx.setAccountNumber(dto.getAccountNumber());
        savedTx.setAmount(dto.getAmount());
        savedTx.setType(dto.getType());
        savedTx.setNarration(dto.getNarration());
        savedTx.setCreatedAt(LocalDateTime.now());

        when(transactionRepository.existsByReferenceId(anyString()))
                .thenReturn(false);

        when(transactionRepository.save(any(Transaction.class)))
                .thenReturn(savedTx);

        TransactionResponseDto response =
                transactionService.createTransaction(dto);

        // ✅ verify business actions
        verify(accountService)
                .creditAccount(dto.getAccountNumber(), dto.getAmount());

        verify(ledgerService)
                .createDoubleEntry(
                        eq("BANK_CASH_ACCOUNT"),
                        eq(dto.getAccountNumber()),
                        eq(dto.getAmount()),
                        anyString(),
                        eq(dto.getNarration())
                );

        verify(transactionRepository).save(any(Transaction.class));

        // ✅ verify response
        assertEquals(dto.getAccountNumber(), response.getAccountNumber());
        assertEquals(dto.getAmount(), response.getAmount());
        assertEquals(TransactionType.DEPOSIT, response.getType());
    }

    @Test
    void createTransaction_Withdrawal_ShouldDebitAccount() {
        TransactionDto dto = new TransactionDto();
        dto.setAccountNumber("0123456789");
        dto.setAmount(new BigDecimal("500"));
        dto.setType(TransactionType.WITHDRAWAL);
        dto.setNarration("ATM withdrawal");

        when(transactionRepository.existsByReferenceId(anyString()))
                .thenReturn(false);

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        transactionService.createTransaction(dto);

        verify(accountService)
                .debitAccount(dto.getAccountNumber(), dto.getAmount());

        verify(ledgerService)
                .createDoubleEntry(
                        eq(dto.getAccountNumber()),
                        eq("BANK_CASH_ACCOUNT"),
                        eq(dto.getAmount()),
                        anyString(),
                        eq(dto.getNarration())
                );
    }
    @Test
    void createTransaction_TransferSameAccount_ShouldThrowException() {
        TransactionDto dto = new TransactionDto();
        dto.setAccountNumber("0123456789");
        dto.setTargetAccount("0123456789");
        dto.setAmount(new BigDecimal("200"));
        dto.setType(TransactionType.TRANSFER);

        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class,
                        () -> transactionService.createTransaction(dto));

        assertEquals(
                "Source and target accounts cannot be the same",
                ex.getMessage()
        );

        verifyNoInteractions(accountService, ledgerService, transactionRepository);
    }

    @Test
    void createTransaction_InvalidAmount_ShouldThrowException() {
        TransactionDto dto = validDepositDto();
        dto.setAmount(BigDecimal.ZERO);

        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class,
                        () -> transactionService.createTransaction(dto));

        assertEquals("Amount must be > 0", ex.getMessage());
    }
    @Test
    void createTransaction_DuplicateReference_ShouldThrowException() {
        TransactionDto dto = validDepositDto();

        when(transactionRepository.existsByReferenceId(anyString()))
                .thenReturn(true);

        IllegalStateException ex =
                assertThrows(IllegalStateException.class,
                        () -> transactionService.createTransaction(dto));

        assertEquals("Duplicate transaction attempt", ex.getMessage());
    }

    @Test
    void getAllTransactions_ShouldReturnList() {
        when(transactionRepository.findAll())
                .thenReturn(List.of(new Transaction(), new Transaction()));

        List<Transaction> result =
                transactionService.getAllTransactions();

        assertEquals(2, result.size());
    }




}
