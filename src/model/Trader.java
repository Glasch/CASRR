package model;

import exchanges.Exchange;

import java.math.BigDecimal;

/**
 * Copyright (c) Anton on 06.12.2018.
 */
public class Trader {
    private Router router;
    private BigDecimal totalValueInDollars;


    public Trader(Router router) {
        this.router = router;
    }

    public Trader(){}

    public void calcValueInDollars() {
        System.out.println("-------------MASTER ROUTES------------------");
        for (Route route : router.getMasterRoutes()) {
            if (route.getSortedEVDeals().isEmpty()) continue;
            printRoute(route);
        }
        System.out.println("-------------INDIVIDUAL ROUTES------------------");
        totalValueInDollars = BigDecimal.ZERO;
        for (Route route : router.getResultingRoutes()) {
            if (route.getSortedEVDeals().isEmpty()) continue;
            printRoute(route);
            totalValueInDollars = totalValueInDollars.add(route.getRouteValueInDollars());
        }
        System.out.println("WE WILL GET RICHER BY: " + totalValueInDollars);

    }

    private void printRoute(Route route) {
        System.out.println(route);
        for (Deal deal : route.getSortedEVDeals()) {
            System.out.println(deal);
        }
        System.out.println("----------------------------------------------");
    }
    public void makeDeal(Route route) {
        System.out.println();

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

    }

    private  void calcTax(Deal deal){
        BigDecimal fromTax = deal.getEffectiveAmount().multiply(deal.getBid().getPrice()).multiply(BigDecimal.valueOf(0.001));
        BigDecimal toTax =  deal.getEffectiveAmount().multiply(BigDecimal.valueOf(0.001));

        deal.setTaxFrom(fromTax);
        deal.setTaxTo(toTax);
    }


    private void acceptTax(Route route) {
        BigDecimal fromMarketBalance = route.getExchangeFrom().getExchangeAccount().balances.get(getMarketCurrency(route));
        fromMarketBalance = fromMarketBalance.subtract(route.getTaxFrom());
        route.getExchangeFrom().getExchangeAccount().balances.replace(getMarketCurrency(route), fromMarketBalance);

        BigDecimal toDealBalance = route.getExchangeTo().getExchangeAccount().balances.get(getDealCurrency(route));
        toDealBalance = toDealBalance.subtract(route.getTaxTo());
        route.getExchangeTo().getExchangeAccount().balances.replace(getDealCurrency(route), toDealBalance);

    }

    private void trade(Deal deal, Route route) {
        BigDecimal exchangeFromDealAccount = route.getExchangeFrom().getExchangeAccount().balances.get(getDealCurrency(route));
        BigDecimal exchangeFromMarketAccount = route.getExchangeFrom().getExchangeAccount().balances.get(getMarketCurrency(route));
        BigDecimal exchangeToDealAccount = route.getExchangeTo().getExchangeAccount().balances.get(getDealCurrency(route));
        BigDecimal exchangeToMarketAccount = route.getExchangeTo().getExchangeAccount().balances.get(getMarketCurrency(route));



        System.out.println(deal.getSpread());
        exchangeFromDealAccount = exchangeFromDealAccount.subtract(deal.getEffectiveAmount());
        route.getExchangeFrom().getExchangeAccount().balances.replace(getDealCurrency(route), exchangeFromDealAccount);

        exchangeFromMarketAccount = exchangeFromMarketAccount.add(deal.getEffectiveAmount().multiply(deal.getBid().getPrice()));
        route.getExchangeFrom().getExchangeAccount().balances.replace(getMarketCurrency(route), exchangeFromMarketAccount);

        exchangeToDealAccount = exchangeToDealAccount.add(deal.getEffectiveAmount());
        route.getExchangeTo().getExchangeAccount().balances.replace(getDealCurrency(route), exchangeToDealAccount);

        exchangeToMarketAccount = exchangeToMarketAccount.subtract(deal.getEffectiveAmount().multiply(deal.getAsk().getPrice()));
        route.getExchangeTo().getExchangeAccount().balances.replace(getMarketCurrency(route), exchangeToMarketAccount);


    }

    private BigDecimal checkBalances(Deal deal, Route route) {
        BigDecimal test = deal.getEffectiveAmount();


        BigDecimal fromDealBalance = route.getExchangeFrom().getExchangeAccount().balances.get(getDealCurrency(route));
        if (deal.getEffectiveAmount().compareTo(fromDealBalance) > 0) {
            System.out.println("Low Balance!");
            deal.setEffectiveAmount(fromDealBalance);
        }

        BigDecimal toMarketBalance = route.getExchangeTo().getExchangeAccount().balances.get(getMarketCurrency(route));
        if (deal.getEffectiveAmount().multiply(deal.getAsk().getPrice()).compareTo(toMarketBalance) > 0 ) {
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


}



