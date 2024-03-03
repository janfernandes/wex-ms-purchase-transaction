package br.com.wex.transaction.service;

import br.com.wex.transaction.dto.ConvertedPurchaseTransactionDto;
import br.com.wex.transaction.dto.PurchaseTransactionDto;

/**
 * Responsible for storing, retrieving and converting values of purchase transactions.
 */
public interface PurchaseTransactionService {

    /**
     * Method for storing a new Purchase Transaction containing a description, transaction date, and a purchase amount in United
     * States dollars.
     * @param request {@link PurchaseTransactionDto} required information for storing data in the database.
     * @return Long saved id
     */
    Long storePurchaseTransaction(PurchaseTransactionDto request);

    /**
     * Method for retrieving and converting a purchase transaction value.
     * @param id required information for retrieving a transaction in the database.
     * @param country country to obtain the currency used.
     * @param currency specific currency.
     * @return ConvertedPurchaseTransactionDto {@link ConvertedPurchaseTransactionDto} retrieved and converted transaction.
     */
    ConvertedPurchaseTransactionDto retrieveAndConvertPurchaseTransactionValue(Long id, String country, String currency);
}
