package br.com.wex.transaction.itg;

import java.util.List;

import br.com.wex.transaction.itg.dto.CurrencyDto;

/**
 * Responsible for accessing the Fical Data service api.
 */
public interface ExchangeRateItg {

    /**
     * Responsible for obtaining the latest rate of a currency within the informed period.
     * @param country desired country.
     * @param currency desired currency(optional).
     * @param initialDate initial date of search.
     * @param finalDate final date of search.
     * @return List response of currency rate found list.
     */
    List<CurrencyDto> getLatestExchangeRate(String country, String currency, String initialDate, String finalDate);
}
