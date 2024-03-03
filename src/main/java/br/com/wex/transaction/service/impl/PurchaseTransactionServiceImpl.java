package br.com.wex.transaction.service.impl;

import br.com.wex.transaction.dto.ConvertedPurchaseTransactionDto;
import br.com.wex.transaction.dto.PurchaseTransactionDto;
import br.com.wex.transaction.exception.PurchaseTransactionException;
import br.com.wex.transaction.itg.ExchangeRateItg;
import br.com.wex.transaction.itg.dto.CurrencyDto;
import br.com.wex.transaction.repository.PurchaseTransactionRepository;
import br.com.wex.transaction.repository.entity.PurchaseTransactionEntity;
import br.com.wex.transaction.service.PurchaseTransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/**
 * Responsible for storing, retrieving and converting values of purchase transactions.
 */
@Slf4j
@Service
public class PurchaseTransactionServiceImpl implements PurchaseTransactionService {

    private static final int MONTHS_TO_SUBTRACT = 6;
    private final PurchaseTransactionRepository purchaseTransactionRepository;
    private final ExchangeRateItg exchangeRateItg;

    @Autowired
    public PurchaseTransactionServiceImpl(final PurchaseTransactionRepository purchaseTransactionRepository,
        final ExchangeRateItg exchangeRateItg) {
        this.purchaseTransactionRepository = purchaseTransactionRepository;
        this.exchangeRateItg = exchangeRateItg;
    }

    @Override
    public Long storePurchaseTransaction(final PurchaseTransactionDto purchaseTransaction) {
        return this.purchaseTransactionRepository.save(PurchaseTransactionEntity.builder()
            .description(purchaseTransaction.getDescription())
            .transactionDate(purchaseTransaction.getTransactionDate())
            .purchaseAmount(purchaseTransaction.getPurchaseAmount())
            .build()).getId();
    }

    @Override
    public ConvertedPurchaseTransactionDto retrieveAndConvertPurchaseTransactionValue(final Long id, final String country,
        final String currency) {
        //Retrieving transaction by id
        final PurchaseTransactionDto purchaseTransaction = this.retrievePurchaseTransactionValue(id);
        final LocalDate transactionDate = purchaseTransaction.getTransactionDate();

        //Get the newest currency: from six previous months until the purchase date
        final List<CurrencyDto> latestExchangeRate = this.exchangeRateItg.getLatestExchangeRate(country, currency,
            transactionDate.minusMonths(MONTHS_TO_SUBTRACT).toString(), transactionDate.toString());

        if (latestExchangeRate.isEmpty()) {
            throw new PurchaseTransactionException("The purchase cannot be converted to the target country's currency.");
        }

        final var rate = latestExchangeRate.getFirst().getExchangeRate();

        return ConvertedPurchaseTransactionDto.builder()
            .id(purchaseTransaction.getId())
            .description(purchaseTransaction.getDescription())
            .transactionDate(purchaseTransaction.getTransactionDate())
            .originalPurchaseAmount(purchaseTransaction.getPurchaseAmount())
            .exchangeRateApplied(rate)
            .convertedPurchaseAmount(purchaseTransaction.getPurchaseAmount().multiply(rate).setScale(2, RoundingMode.UP))
            .build();
    }

    private PurchaseTransactionDto retrievePurchaseTransactionValue(final Long id) {
        log.info("Retrieving transaction on database.");
        return this.purchaseTransactionRepository.findById(id)
            .map(resultEntity -> PurchaseTransactionDto.builder()
                .id(id)
                .description(resultEntity.getDescription())
                .purchaseAmount(resultEntity.getPurchaseAmount())
                .transactionDate(resultEntity.getTransactionDate())
                .build())
            .orElseThrow(() -> new IllegalArgumentException("Purchase transaction not found with id: " + id + "."));
    }
}
