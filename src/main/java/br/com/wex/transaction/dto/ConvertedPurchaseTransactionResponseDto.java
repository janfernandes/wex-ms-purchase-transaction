package br.com.wex.transaction.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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
 * Endpoint response data for a retrieved and converted purchase transaction.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConvertedPurchaseTransactionResponseDto {

    private Long id;
    private String description;
    @JsonFormat(pattern = "MM/dd/yyyy")
    private LocalDate transactionDate;
    private BigDecimal originalPurchaseAmount;
    private BigDecimal exchangeRateApplied;
    private BigDecimal convertedPurchaseAmount;
}
