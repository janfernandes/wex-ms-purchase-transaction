package br.com.wex.transaction.itg.impl;

import br.com.wex.transaction.itg.ExchangeRateItg;
import br.com.wex.transaction.itg.dto.CurrencyDto;
import br.com.wex.transaction.itg.dto.CurrencyItgResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * Responsible for accessing the Fical Data service api.
 */
@Slf4j
@Component
public class ExchangeRateItgImpl implements ExchangeRateItg {

    private final String rateUrl;
    private final RestTemplate restTemplate;

    public ExchangeRateItgImpl(@Value("${url.fiscaldata.rates}") final String rateUrl, final RestTemplate restTemplate) {
        this.rateUrl = rateUrl;
        this.restTemplate = restTemplate;
    }

    @Override
    public List<CurrencyDto> getLatestExchangeRate(final String country, final String currency, final String initialDate,
        final String finalDate) {
        log.info("Requesting exchange rate.");

        final String filter =
            currency != null ? String.format("country:eq:%s,currency:eq:%s,record_date:gte:%s,record_date:lte:%s", country,
                currency, initialDate, finalDate)
                : String.format("country:eq:%s,record_date:gte:%s,record_date:lte:%s", country, initialDate, finalDate);

        final URI uri = UriComponentsBuilder.fromUriString(this.rateUrl)
            .queryParam("fields", "record_date, exchange_rate")
            .queryParam("sort", "-record_date")
            .queryParam("page[size]", "1")
            .queryParam("filter", filter)
            .build(false)
            .toUri();

        var forObject = this.restTemplate.getForObject(uri, CurrencyItgResponseDto.class);
        return forObject.getData();
    }
}
