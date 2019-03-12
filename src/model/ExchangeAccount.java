package model;

import exchanges.Exchange;
import org.json.JSONArray;
import services.ConnectionManager;

import java.math.BigDecimal;
import java.util.*;

/**
 * Copyright (c) Anton on 14.01.2019.
 */
public class ExchangeAccount {
    private Map <String, BigDecimal> balances = new HashMap <>();

    public ExchangeAccount(Exchange exchange) {
        Set <String> currencies = getCurrencies(exchange);
        for (String currency : currencies) {
            balances.put(currency, BigDecimal.valueOf(1000000000));
        }
    }

    private Set <String> getCurrencies(Exchange exchange) {
        Set <String> currencies = new HashSet <>();
        List<String> pairs = exchange.getPairs();
        for (String pair : pairs) {
            String[] split = pair.split("/");
            currencies.add(split[0]);
            currencies.add(split[1]);
        }
        return currencies;
    }

    public Map <String, BigDecimal> getBalances() {
        return balances;
    }


}
