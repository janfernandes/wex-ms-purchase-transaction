package br.com.wex.transaction.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Endpoint request data info to store a purchase transaction.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request data info to store a purchase transaction")
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PurchaseTransactionRequestDto {

    @NotBlank(message = "Description must not be empty.")
    @Size(max = 50, message = "Description must not exceed 50 characters.")
    @Schema(description = "Description", example = "Jim's Snacks")
    private String description;

    @NotNull(message = "Transaction date is required.")
    @PastOrPresent(message = "Transaction date must be in the past or present.")
    @Schema(description = "Transaction date", example = "01/30/2024")
    @JsonFormat(pattern = "MM/dd/yyyy")
    @DateTimeFormat(pattern = "MM/dd/yyyy")
    private LocalDate transactionDate;

    @Schema(description = "Purchase amount in United States dollars", example = "65.98")
    @NotNull(message = "Amount must not be empty.")
    @DecimalMin(value = "0.01", message = "Purchase amount must be a positive amount")
    @DecimalMax(value = "9999999999.99", message = "Purchase amount is too large")
    @Digits(integer = 10, message = "Purchase amount must be rounded to the nearest cent.", fraction = 2)
    private BigDecimal purchaseAmount;
}
