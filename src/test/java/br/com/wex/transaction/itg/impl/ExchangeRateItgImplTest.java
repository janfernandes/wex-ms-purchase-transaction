package br.com.wex.transaction.itg.impl;

import br.com.wex.transaction.itg.ExchangeRateItg;
import br.com.wex.transaction.itg.dto.CurrencyDto;
import br.com.wex.transaction.itg.dto.CurrencyItgResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class ExchangeRateItgImplTest {

    private ExchangeRateItg exchangeRateItg;

    @Mock
    private RestTemplate restTemplate;

    @BeforeEach
    void setup() {
        this.restTemplate = Mockito.mock(RestTemplate.class);
        this.exchangeRateItg = new ExchangeRateItgImpl("some.url", this.restTemplate);
    }

    @Test
    void should_returnLatestRate_when_thereIsDataWithinTheInformedDatesRange() {
        when(this.restTemplate.getForObject(any(URI.class), eq(CurrencyItgResponseDto.class))).thenReturn(
            CurrencyItgResponseDto.builder()
                .data(Collections.singletonList(CurrencyDto.builder().exchangeRate(BigDecimal.valueOf(0.905)).build()))
                .build());

        List<CurrencyDto> currencyList = this.exchangeRateItg.getLatestExchangeRate("Argentina", "Peso", "2023-07-01",
            "2024-01-01");

        assertFalse(currencyList.isEmpty());
        assertEquals(BigDecimal.valueOf(0.905), currencyList.get(0).getExchangeRate());
    }

    @Test
    void should_returnEmptyList_when_thereIsNoDataWithinTheInformedDatesRange() {
        when(this.restTemplate.getForObject(any(URI.class), eq(CurrencyItgResponseDto.class))).thenReturn(
            CurrencyItgResponseDto.builder().data(Collections.emptyList()).build());

        List<CurrencyDto> currencyList = this.exchangeRateItg.getLatestExchangeRate("Australia", null, "2023-07-01",
            "2024-01-01");

        assertTrue(currencyList.isEmpty());
    }
}
