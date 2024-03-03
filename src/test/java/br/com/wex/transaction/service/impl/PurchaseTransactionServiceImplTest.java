package br.com.wex.transaction.service.impl;

import br.com.wex.transaction.dto.ConvertedPurchaseTransactionDto;
import br.com.wex.transaction.dto.PurchaseTransactionDto;
import br.com.wex.transaction.exception.PurchaseTransactionException;
import br.com.wex.transaction.itg.ExchangeRateItg;
import br.com.wex.transaction.itg.dto.CurrencyDto;
import br.com.wex.transaction.repository.PurchaseTransactionRepository;
import br.com.wex.transaction.repository.entity.PurchaseTransactionEntity;
import br.com.wex.transaction.service.PurchaseTransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class PurchaseTransactionServiceImplTest {

    private PurchaseTransactionService purchaseTransactionService;
    @Mock
    private PurchaseTransactionRepository purchaseTransactionRepository;

    @Mock
    private ExchangeRateItg exchangeRateItg;

    @BeforeEach
    void setup() {
        this.exchangeRateItg = Mockito.mock(ExchangeRateItg.class);
        this.purchaseTransactionRepository = Mockito.mock(PurchaseTransactionRepository.class);
        this.purchaseTransactionService = new PurchaseTransactionServiceImpl(this.purchaseTransactionRepository,
            this.exchangeRateItg);
    }

    @Test
    void should_storePurchaseTransaction_when_dataInputIsCorrect() {
        when(this.purchaseTransactionRepository.save(any(PurchaseTransactionEntity.class))).thenReturn(
            PurchaseTransactionEntity.builder().id(1L).build());

        Long id = this.purchaseTransactionService.storePurchaseTransaction(PurchaseTransactionDto.builder().build());

        assertNotNull(id);
        assertEquals(1, id);
    }

    @Test
    void should_retrieveAndConvertPurchaseTransactionValue_when_currencyIsFound() {
        when(this.purchaseTransactionRepository.findById(anyLong())).thenReturn(Optional.ofNullable(
            PurchaseTransactionEntity.builder()
                .id(1L)
                .description("Jim's Snacks")
                .transactionDate(LocalDate.of(2024, 1, 30))
                .purchaseAmount(BigDecimal.valueOf(99.45))
                .build()));

        when(this.exchangeRateItg.getLatestExchangeRate(anyString(), anyString(), anyString(), anyString())).thenReturn(
            Collections.singletonList(CurrencyDto.builder().exchangeRate(BigDecimal.valueOf(0.905)).build()));

        ConvertedPurchaseTransactionDto converted = this.purchaseTransactionService.retrieveAndConvertPurchaseTransactionValue(1L,
            "Netherlands", "Euro");

        assertEquals(1, converted.getId());
        assertEquals("Jim's Snacks", converted.getDescription());
        assertEquals(LocalDate.of(2024, 1, 30), converted.getTransactionDate());
        assertEquals(BigDecimal.valueOf(99.45), converted.getOriginalPurchaseAmount());
        assertEquals(BigDecimal.valueOf(0.905), converted.getExchangeRateApplied());
        assertEquals(BigDecimal.valueOf(90.01), converted.getConvertedPurchaseAmount());
    }

    @Test
    void should_throwPurchaseTransactionException_when_currencyIsNotFound() {
        when(this.purchaseTransactionRepository.findById(anyLong())).thenReturn(Optional.ofNullable(
            PurchaseTransactionEntity.builder()
                .id(1L)
                .description("Jim's Snacks")
                .transactionDate(LocalDate.of(2024, 1, 30))
                .purchaseAmount(BigDecimal.valueOf(99.45))
                .build()));

        when(this.exchangeRateItg.getLatestExchangeRate(anyString(), anyString(), anyString(), anyString())).thenReturn(
            Collections.emptyList());

        Exception exception = assertThrows(PurchaseTransactionException.class,
            () -> this.purchaseTransactionService.retrieveAndConvertPurchaseTransactionValue(1L, "Netherlands", "Euro"));

        String expectedMessage = "The purchase cannot be converted to the target country's currency.";
        String actualMessage = exception.getMessage();
        assertEquals(actualMessage, expectedMessage);
    }

    @Test
    void should_throwResourceNotFoundException_when_transactionIsNotFound() {
        when(this.purchaseTransactionRepository.findById(anyLong())).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class,
            () -> this.purchaseTransactionService.retrieveAndConvertPurchaseTransactionValue(1L, "Netherlands", "Euro"));

        String expectedMessage = "Purchase transaction not found with id: 1.";
        String actualMessage = exception.getMessage();
        assertEquals(actualMessage, expectedMessage);
    }
}
