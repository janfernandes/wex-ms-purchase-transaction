package br.com.wex.transaction.api;

import br.com.wex.transaction.dto.ConvertedPurchaseTransactionResponseDto;
import br.com.wex.transaction.dto.PurchaseTransactionDto;
import br.com.wex.transaction.dto.PurchaseTransactionRequestDto;
import br.com.wex.transaction.dto.PurchaseTransactionResponseDto;
import br.com.wex.transaction.service.PurchaseTransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "WexTransactionsApiController", description = "API containing the endpoints for accessing the Purchase Transactions microservice")
@RestController
@RequestMapping("/api/ms/purchase-transactions/v1")
public class PurchaseTransactionController {

    private final PurchaseTransactionService purchaseTransactionService;

    @Autowired
    public PurchaseTransactionController(final PurchaseTransactionService purchaseTransactionService) {
        this.purchaseTransactionService = purchaseTransactionService;
    }

    @PostMapping("/store")
    @Operation(summary = "Endpoint responsible for storing  a new Purchase Transaction containing a description, transaction date, and a purchase amount in United States dollars")
    @ApiResponse(responseCode = "201", description = "Purchase Transaction created")
    public ResponseEntity<PurchaseTransactionResponseDto> storePurchaseTransaction(
        @Parameter(description = "Dados para consulta dos produtos", required = true) @RequestBody @Valid final PurchaseTransactionRequestDto request) {
        log.info("New Purchase Transaction requested.");
        final Long id = this.purchaseTransactionService.storePurchaseTransaction(PurchaseTransactionDto.builder()
            .description(request.getDescription())
            .transactionDate(request.getTransactionDate())
            .purchaseAmount(request.getPurchaseAmount())
            .build());
        log.info("Purchase Transaction processed: {}", id);
        return ResponseEntity.status(HttpStatus.CREATED).body(PurchaseTransactionResponseDto.builder().id(id).build());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Endpoint responsible for retrieving a Purchase Transaction in a Specified Country’s Currency")
    public ResponseEntity<ConvertedPurchaseTransactionResponseDto> retrieveAndConvertPurchaseTransactionValue(
        @PathVariable("id") final Long id, @RequestParam(value = "country") final String country,
        @RequestParam(value = "currency", required = false) final String currency) {
        //mostly the countries have just one currency, so the country name is more suitable entry;
        //also because a currency can have different values for different countries, like Pound currency is different of
        //Egypt Pound and United Kingdom Pound;
        //also some countries have more than one currency, like netherlands, then the currency is an optional entry.
        log.info("Retrieving Purchase Transaction requested.");
        final var retrievedConvertedTransaction = this.purchaseTransactionService.retrieveAndConvertPurchaseTransactionValue(id,
            country, currency);

        return ResponseEntity.ok(ConvertedPurchaseTransactionResponseDto.builder()
            .id(retrievedConvertedTransaction.getId())
            .description(retrievedConvertedTransaction.getDescription())
            .transactionDate(retrievedConvertedTransaction.getTransactionDate())
            .originalPurchaseAmount(retrievedConvertedTransaction.getOriginalPurchaseAmount())
            .exchangeRateApplied(retrievedConvertedTransaction.getExchangeRateApplied())
            .convertedPurchaseAmount(retrievedConvertedTransaction.getConvertedPurchaseAmount())
            .build());
    }
}
