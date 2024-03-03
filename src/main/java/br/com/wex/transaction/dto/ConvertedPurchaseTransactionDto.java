package br.com.wex.transaction.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Retrieved and converted Purchase transaction model dto containg its related attributes.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConvertedPurchaseTransactionDto {

    private Long id;
    private String description;
    private LocalDate transactionDate;
    private BigDecimal originalPurchaseAmount;
    private BigDecimal exchangeRateApplied;
    private BigDecimal convertedPurchaseAmount;
}
