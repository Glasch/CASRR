package model;

import exchanges.Exchange;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (c) Anton on 06.12.2018.
 */
public class Trader {
    private List<Route> acceptedRoutes = new ArrayList<>();

    public void makeRealMoneyDeal(Route route) {
        String marketCurrency = route.getPairName().split("/")[0];
        String dealCurrency = route.getPairName().split("/")[1];
        Exchange exchangeFrom = route.getExchangeFrom();
        Exchange exchangeTo = route.getExchangeTo();
        BigDecimal exchangeFromMarketBalance = exchangeFrom.getBalance(marketCurrency);
        BigDecimal exchangeToDealBalance = exchangeTo.getBalance(dealCurrency);

        System.out.println(exchangeFrom + " " + exchangeFromMarketBalance + " " + marketCurrency);
        System.out.println(exchangeFrom + " " + exchangeFrom.getBalance(dealCurrency) + " " + dealCurrency);
        System.out.println(exchangeTo + " " + exchangeTo.getBalance(marketCurrency) + " " + marketCurrency);
        System.out.println(exchangeTo + " " + exchangeToDealBalance + " " + dealCurrency);
        System.out.println(route.getAmount());
        System.out.println();

        BigDecimal realAmount = findRealAmount(route, exchangeFromMarketBalance, exchangeToDealBalance);


    }

    private BigDecimal findRealAmount(Route route, BigDecimal exchangeFromMarketBalance, BigDecimal exchangeToDealBalance) {
        BigDecimal realAmount = BigDecimal.ZERO;

        /*
        TODO
        we should sort by desc
         1. route.getEffectiveAmount()
         2. exchangeFromMarketBalance
         3. castExchangeToDealBalance 2 route.getEffectiveAmountCurrency()

         Then return the lowest
        */

        ArrayList<BigDecimal> amounts = new ArrayList<>();
        amounts.add(route.getAmount());
        amounts.add(exchangeFromMarketBalance);
        amounts.add(castDealBalanceToMarketCurrency(route,exchangeToDealBalance));

        return realAmount;
    }

    private BigDecimal castDealBalanceToMarketCurrency(Route route, BigDecimal exchangeToDealBalance) {
        BigDecimal res;

        /*
        in fact we should find how much market currency we can buy at exchangeTo trading exchangeToDealBalance
         */


        return null;
    }

    public void makeDeal(Route route) {

        BigDecimal totFromTax = BigDecimal.ZERO;
        BigDecimal totToTax = BigDecimal.ZERO;

        for (Deal deal : route.getSortedEVDeals()) {
            deal.setEffectiveAmount(checkBalances(deal, route));
            calcTax(deal);
            totFromTax = totFromTax.add(deal.getTaxFrom());
            totToTax = totToTax.add(deal.getTaxTo());
            trade(deal, route);
        }
        route.setTaxFrom(totFromTax);
        route.setTaxTo(totToTax);
        acceptTax(route);

        route.calcRouteSpread(route.getSortedEVDeals());
        if (route.getAmount().compareTo(BigDecimal.ZERO) > 0) {
            acceptedRoutes.add(route);
        }
    }

    private void calcTax(Deal deal) {
        BigDecimal fromTax = deal.getEffectiveAmount().multiply(deal.getBid().getPrice()).multiply(deal.getBid().getExchange().getTakerTax());
        BigDecimal toTax = deal.getEffectiveAmount().multiply(deal.getAsk().getExchange().getTakerTax());

        deal.setTaxFrom(fromTax);
        deal.setTaxTo(toTax);
    }


    private void acceptTax(Route route) {
        BigDecimal fromMarketBalance = route.getExchangeFrom().getExchangeAccount().getBalances().get(getMarketCurrency(route));
        fromMarketBalance = fromMarketBalance.subtract(route.getTaxFrom());
        route.getExchangeFrom().getExchangeAccount().getBalances().replace(getMarketCurrency(route), fromMarketBalance);

        BigDecimal toDealBalance = route.getExchangeTo().getExchangeAccount().getBalances().get(getDealCurrency(route));
        toDealBalance = toDealBalance.subtract(route.getTaxTo());
        route.getExchangeTo().getExchangeAccount().getBalances().replace(getDealCurrency(route), toDealBalance);
    }

    private void trade(Deal deal, Route route) {
        BigDecimal exchangeFromDealAccount = route.getExchangeFrom().getExchangeAccount().getBalances().get(getDealCurrency(route));
        BigDecimal exchangeFromMarketAccount = route.getExchangeFrom().getExchangeAccount().getBalances().get(getMarketCurrency(route));
        BigDecimal exchangeToDealAccount = route.getExchangeTo().getExchangeAccount().getBalances().get(getDealCurrency(route));
        BigDecimal exchangeToMarketAccount = route.getExchangeTo().getExchangeAccount().getBalances().get(getMarketCurrency(route));

        exchangeFromDealAccount = exchangeFromDealAccount.subtract(deal.getEffectiveAmount());
        route.getExchangeFrom().getExchangeAccount().getBalances().replace(getDealCurrency(route), exchangeFromDealAccount);

        exchangeFromMarketAccount = exchangeFromMarketAccount.add(deal.getEffectiveAmount().multiply(deal.getBid().getPrice()));
        route.getExchangeFrom().getExchangeAccount().getBalances().replace(getMarketCurrency(route), exchangeFromMarketAccount);

        exchangeToDealAccount = exchangeToDealAccount.add(deal.getEffectiveAmount());
        route.getExchangeTo().getExchangeAccount().getBalances().replace(getDealCurrency(route), exchangeToDealAccount);

        exchangeToMarketAccount = exchangeToMarketAccount.subtract(deal.getEffectiveAmount().multiply(deal.getAsk().getPrice()));
        route.getExchangeTo().getExchangeAccount().getBalances().replace(getMarketCurrency(route), exchangeToMarketAccount);
    }

    private BigDecimal checkBalances(Deal deal, Route route) {

        BigDecimal fromDealBalance = route.getExchangeFrom().getExchangeAccount().getBalances().get(getDealCurrency(route));
        if (deal.getEffectiveAmount().compareTo(fromDealBalance) > 0) {
            System.out.println("Low Balance!");
            deal.setEffectiveAmount(fromDealBalance);
        }

        BigDecimal toMarketBalance = route.getExchangeTo().getExchangeAccount().getBalances().get(getMarketCurrency(route));
        if (deal.getEffectiveAmount().multiply(deal.getAsk().getPrice()).compareTo(toMarketBalance) > 0) {
            System.out.println("Low Balance!");
            BigDecimal res = toMarketBalance.divide(deal.getAsk().getPrice(), 10, BigDecimal.ROUND_FLOOR);
            deal.setEffectiveAmount(res);
        }
        return deal.getEffectiveAmount();
    }

    private String getDealCurrency(Route route) {
        return route.getPairName().split("/")[0];
    }

    private String getMarketCurrency(Route route) {
        return route.getPairName().split("/")[1];
    }

    public List<Route> getAcceptedRoutes() {
        return acceptedRoutes;
    }
}



