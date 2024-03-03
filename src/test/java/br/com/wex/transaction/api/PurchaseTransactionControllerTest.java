package br.com.wex.transaction.api;

import br.com.wex.transaction.dto.ConvertedPurchaseTransactionDto;
import br.com.wex.transaction.dto.PurchaseTransactionDto;
import br.com.wex.transaction.dto.PurchaseTransactionRequestDto;
import br.com.wex.transaction.exception.PurchaseTransactionException;
import br.com.wex.transaction.handler.RestExceptionHandler;
import br.com.wex.transaction.service.PurchaseTransactionService;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Locale;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PurchaseTransactionControllerTest {

    @Mock
    private PurchaseTransactionService purchaseTransactionService;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private PurchaseTransactionController purchaseTransactionController;

    private MockMvc mockMvc;

    private ObjectMapper mapper;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule())
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        ;
        this.mockMvc = MockMvcBuilders.standaloneSetup(purchaseTransactionController)
            .setControllerAdvice(new RestExceptionHandler(messageSource))
            .build();
    }

    @Test
    void should_returnCreated_when_storingValidPurchaseTransaction() throws Exception {
        when(this.purchaseTransactionService.storePurchaseTransaction(any(PurchaseTransactionDto.class))).thenReturn(1L);

        this.mockMvc.perform(post("/api/ms/purchase-transactions/v1/store").contentType(MediaType.APPLICATION_JSON)
                .content(this.mapper.writeValueAsString(PurchaseTransactionRequestDto.builder()
                    .description("Test")
                    .transactionDate(LocalDate.of(2024, 1, 30))
                    .purchaseAmount(BigDecimal.valueOf(456.88))
                    .build())))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(MockMvcResultMatchers.jsonPath("$", notNullValue()))
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void should_throwMethodArgumentNotValidException_when_storingEmptyPurchaseTransaction() throws Exception {
        when(this.messageSource.getMessage(anyString(), eq(null), any(Locale.class))).thenReturn("Errors found: ");

        this.mockMvc.perform(post("/api/ms/purchase-transactions/v1/store").contentType(MediaType.APPLICATION_JSON)
                .content(this.mapper.writeValueAsString(PurchaseTransactionRequestDto.builder().build())))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(MockMvcResultMatchers.jsonPath("$", notNullValue()))
            .andExpect(jsonPath("$.message", containsString("Errors found:")))
            .andExpect(jsonPath("$.message", containsString("Transaction date is required.")))
            .andExpect(jsonPath("$.message", containsString("Description must not be empty.")))
            .andExpect(jsonPath("$.message", containsString("Amount must not be empty.")))
            .andExpect(result -> assertInstanceOf(MethodArgumentNotValidException.class, result.getResolvedException()))
            .andReturn();
    }

    @Test
    void should_throwHttpMessageNotReadableException_when_storingInvalidDateFormatData() throws Exception {
        // @formatter:off
        String body = "{\n" +
            "  \"description\": \"Test\",\n"
            + "  \"transactionDate\": \"20/01/2024\",\n"
            + "  \"purchaseAmount\": \"350\"\n"
            + "}";
        // @formatter:on

        this.mockMvc.perform(post("/api/ms/purchase-transactions/v1/store").contentType(MediaType.APPLICATION_JSON).content(body))
            .andDo(print())
            .andExpect(status().isNotAcceptable())
            .andExpect(MockMvcResultMatchers.jsonPath("$", notNullValue()))
            .andExpect(jsonPath("$.message",
                containsString("Cannot deserialize value of type `java.time.LocalDate` from String \"20/01/2024\"")))
            .andExpect(result -> assertInstanceOf(HttpMessageNotReadableException.class, result.getResolvedException()))
            .andReturn();
    }

    @Test
    void should_throwHttpMessageNotReadableException_when_storingInvalidAmountFormatData() throws Exception {
        // @formatter:off
        String body = "{\n" +
            "  \"description\": \"Test\",\n"
            + "  \"transactionDate\": \"01/01/2024\",\n"
            + "  \"purchaseAmount\": \"aaaa\"\n"
            + "}";
        // @formatter:on

        this.mockMvc.perform(post("/api/ms/purchase-transactions/v1/store").contentType(MediaType.APPLICATION_JSON).content(body))
            .andDo(print())
            .andExpect(status().isNotAcceptable())
            .andExpect(MockMvcResultMatchers.jsonPath("$", notNullValue()))
            .andExpect(jsonPath("$.message", containsString(
                "JSON parse error: Cannot deserialize value of type `java.math.BigDecimal` from String \"aaaa\": not a valid representation")))
            .andExpect(result -> assertInstanceOf(HttpMessageNotReadableException.class, result.getResolvedException()))
            .andReturn();
    }

    @Test
    void should_returnOk_when_retrievingAndConvertingValidPurchaseTransaction() throws Exception {

        when(this.purchaseTransactionService.retrieveAndConvertPurchaseTransactionValue(anyLong(), anyString(),
            anyString())).thenReturn(ConvertedPurchaseTransactionDto.builder()
            .id(1L)
            .description("Jim's Snacks")
            .transactionDate(LocalDate.of(2024, 1, 30))
            .originalPurchaseAmount(BigDecimal.valueOf(99.45))
            .exchangeRateApplied(BigDecimal.valueOf(0.905))
            .convertedPurchaseAmount(BigDecimal.valueOf(90.01))
            .build());

        this.mockMvc.perform(get("/api/ms/purchase-transactions/v1/1?country=Italy&currency=Euro").contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$", notNullValue()))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.description").value("Jim's Snacks"))
            .andExpect(jsonPath("$.transactionDate").value("01/30/2024"))
            .andExpect(jsonPath("$.originalPurchaseAmount").value(99.45))
            .andExpect(jsonPath("$.exchangeRateApplied").value(0.905))
            .andExpect(jsonPath("$.convertedPurchaseAmount").value(90.01));
    }

    @Test
    void should_throwMissingServletRequestParameterException_when_doesNotInformRequestParameter() throws Exception {
        when(this.messageSource.getMessage(anyString(), any(), any(Locale.class))).thenReturn(
            "Request parameter currency is mandatory.");

        this.mockMvc.perform(get("/api/ms/purchase-transactions/v1/1").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(MockMvcResultMatchers.jsonPath("$", notNullValue()))
            .andExpect(jsonPath("$.message").value("Request parameter currency is mandatory."))
            .andExpect(result -> assertInstanceOf(MissingServletRequestParameterException.class, result.getResolvedException()))
            .andReturn();
    }

    @Test
    void should_throwMethodArgumentTypeMismatchException_when_doesNotInformValidPathVariable() throws Exception {
        when(this.messageSource.getMessage(anyString(), any(), any(Locale.class))).thenReturn(
            "There was a error converting the value a to type Long.");

        this.mockMvc.perform(get("/api/ms/purchase-transactions/v1/a?Currency=euro").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(MockMvcResultMatchers.jsonPath("$", notNullValue()))
            .andExpect(jsonPath("$.message").value("There was a error converting the value a to type Long."))
            .andExpect(result -> assertInstanceOf(MethodArgumentTypeMismatchException.class, result.getResolvedException()))
            .andReturn();
    }

    @Test
    void should_throwResourceNotFoundException_when_transactionIsNotFound() throws Exception {
        when(this.purchaseTransactionService.retrieveAndConvertPurchaseTransactionValue(anyLong(), anyString(),
            anyString())).thenThrow(new IllegalArgumentException("Purchase transaction not found with id: 5."));

        this.mockMvc.perform(get("/api/ms/purchase-transactions/v1/5?country=Italy&currency=Euro").contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(MockMvcResultMatchers.jsonPath("$", notNullValue()))
            .andExpect(jsonPath("$.message").value("Purchase transaction not found with id: 5."))
            .andExpect(result -> assertInstanceOf(IllegalArgumentException.class, result.getResolvedException()))
            .andReturn();
    }

    @Test
    void should_throwPurchaseTransactionException_when_currencyWithinSixMonthsIsNotFound() throws Exception {
        when(this.purchaseTransactionService.retrieveAndConvertPurchaseTransactionValue(anyLong(), anyString(),
            anyString())).thenThrow(new PurchaseTransactionException("The purchase cannot be converted to the target currency."));

        this.mockMvc.perform(get("/api/ms/purchase-transactions/v1/1?country=Italy&currency=Euro").contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isPreconditionFailed())
            .andExpect(MockMvcResultMatchers.jsonPath("$", notNullValue()))
            .andExpect(jsonPath("$.message").value("The purchase cannot be converted to the target currency."))
            .andExpect(result -> assertInstanceOf(PurchaseTransactionException.class, result.getResolvedException()))
            .andReturn();
    }

    @Test
    void should_throwRuntimeException_when_unexpectedErrorOccurs() throws Exception {
        when(this.purchaseTransactionService.retrieveAndConvertPurchaseTransactionValue(anyLong(), anyString(),
            anyString())).thenThrow(new RuntimeException("unexpected error."));

        this.mockMvc.perform(get("/api/ms/purchase-transactions/v1/1?country=Italy&currency=Euro").contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(MockMvcResultMatchers.jsonPath("$", notNullValue()))
            .andExpect(jsonPath("$.message").value("unexpected error."))
            .andExpect(result -> assertInstanceOf(RuntimeException.class, result.getResolvedException()))
            .andReturn();
    }
}
