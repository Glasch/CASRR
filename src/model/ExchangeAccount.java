package model;

import exchanges.Exchange;

import java.math.BigDecimal;
import java.util.*;

/**
 * Copyright (c) Anton on 14.01.2019.
 */
public class ExchangeAccount {
    HashMap <String, BigDecimal> balances = new HashMap <>();

    public ExchangeAccount(Exchange exchange) {
        Set <String> currencies = getCurrencies(exchange);
        for (String currency : currencies) {
            if (Objects.equals(currency, "BTC")) {
                balances.put(currency, getCurrencyBalance(BigDecimal.valueOf(10)));
            }else
            if (Objects.equals(currency, "ETH")){
                balances.put(currency,getCurrencyBalance(BigDecimal.valueOf(1000)));
            }else
            if (Objects.equals(currency, "LTC")){
                balances.put(currency,getCurrencyBalance(BigDecimal.valueOf(2000)));
            }else
            if (Objects.equals(currency, "XRP")) {
                balances.put(currency, getCurrencyBalance(BigDecimal.valueOf(50000)));
            }else
            if (Objects.equals(currency, "ZEC")) {
                balances.put(currency, getCurrencyBalance(BigDecimal.valueOf(200)));
            }
            else{
                balances.put(currency,getCurrencyBalance(BigDecimal.valueOf(400000)));
            }

        }
    }

    private BigDecimal getCurrencyBalance(BigDecimal balance) {
        return balance;
    }

    private Set <String> getCurrencies(Exchange exchange) {
        Set <String> currencies = new HashSet <>();
        ArrayList <String> pairs = exchange.getPairs();
        for (String pair : pairs) {
            String[] split = pair.split("/");
            currencies.add(split[0]);
            currencies.add(split[1]);
        }
        return currencies;
    }

    public HashMap <String, BigDecimal> getBalances() {
        return balances;
    }
}
