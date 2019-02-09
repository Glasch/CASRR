package services;

import exchanges.Exchange;
import model.Deal;
import model.Route;
import model.Trader;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Copyright (c) Anton on 05.02.2019.
 */
public class Reporter {
    private Map<String, Integer> pairRepeats;

    public void showPairRepeats() {
        pairRepeats = new HashMap <>();
        Updater updater = Updater.getInstance();
        List <Exchange> exchanges = updater.getExchanges();

        for (Exchange exchange : exchanges) {
            for (String pair : exchange.getPairs()) {
                pairRepeats.merge(pair, 1, Integer::sum);
            }
        }
        printPairRepeats(exchanges);
    }

    public void showAcceptedRoutes(Trader trader, boolean withDeals){
        for (Route route : trader.getAcceptedRoutes()) {
            System.out.println(route);
            if (withDeals){
                for (Deal deal : route.getSortedEVDeals()) {
                    System.out.println(deal);
                }
            }
        }
    }

    public void showExchangeAccounts(Boolean withDiff){
        Updater updater = Updater.getInstance();
        List <Exchange> exchanges = updater.getExchanges();
        for (Exchange exchange : exchanges) {
            System.out.println(exchange);
            for ( String pair : exchange.getExchangeAccount().getBalances().keySet()) {
                if (withDiff){
                System.out.println(pair + " " + exchange.getExchangeAccount().getBalances().get(pair).subtract(BigDecimal.valueOf(1000000000)));
                }else {
                System.out.println(pair + " " + exchange.getExchangeAccount().getBalances().get(pair));
                }
            }
        }
    }

    public  void showConverterData(UsdConverter usdConverter){
        for (String currency : usdConverter.getCurrencyPrices().keySet()) {
            System.out.println(currency + " " + usdConverter.getCurrencyPrices().get(currency) + " USD");
        }
    }

    private void printPairRepeats(List<Exchange> exchanges) {
        for (String pair : pairRepeats.keySet()) {
            System.out.println(pair + " : " + pairRepeats.get(pair) + checkExchanges(pair, exchanges));
        }
    }

    private String checkExchanges(String pair, List<Exchange> exchanges) {
        StringBuilder res = new StringBuilder();
        for (Exchange exchange : exchanges) {
            if (exchange.getPairs().contains(pair)) {
                res.append(" ").append(exchange).append(",");
            }
        }
        return res.toString();
    }
}
