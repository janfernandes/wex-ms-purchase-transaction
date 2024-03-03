package br.com.wex.transaction.exception;

import java.io.Serial;

/**
 * Business custom exception.
 */
public class PurchaseTransactionException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public PurchaseTransactionException(final String message) {
        super(message);
    }

}
