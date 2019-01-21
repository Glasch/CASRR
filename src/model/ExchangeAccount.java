package model;

import exchanges.Exchange;
import services.Updater;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Copyright (c) Anton on 14.01.2019.
 */
public class ExchangeAccount {
    HashMap <String, BigDecimal> balances = new HashMap <>();

    public ExchangeAccount(Exchange exchange) {
        Set<String> currencies = getCurrencies(exchange);
        for (String currency : currencies) {
            balances.put(currency, getCurrencyBalance());
        }
    }

    private BigDecimal getCurrencyBalance() {
        return  BigDecimal.valueOf(100000);
    }

    private Set<String> getCurrencies(Exchange exchange) {
        Set<String> currencies = new HashSet<>();
                ArrayList <String> pairs = exchange.getPairs();
                for (String pair : pairs) {
                    String[] split = pair.split("/");
                    currencies.add(split[0]);
                    currencies.add(split[1]);
                }
        return currencies;
    }
}
