package model;

import exchanges.Exchange;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Copyright (c) Anton on 06.12.2018.
 */
public class Trader {
    private Router router;
    private BigDecimal totalValueInDollars;



    public Trader(Router router) {
        this.router = router;
    }

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
//        System.out.println("-------------------------------------------------");
        System.out.println(route);
        for (Deal deal : route.getSortedEVDeals() ) {
            System.out.println(deal);
        }
        System.out.println("----------------------------------------------");
    }

   public void makeDeal(Route route){
        BigDecimal effectiveAmount = BigDecimal.ZERO;
       for (Deal deal : route.getSortedEVDeals()) {
           effectiveAmount = effectiveAmount.add(deal.getEffectiveAmount());
       }
       System.out.println(route + " " + effectiveAmount );
       for (Deal deal : route.getSortedEVDeals()) {
           trade(deal, route);
       }
       System.out.println(route);
       System.out.println(route.getExchangeFrom().getExchangeAccount().balances.get(getDealCurrency(route)));
       System.out.println(route.getExchangeFrom().getExchangeAccount().balances.get(getMarketCurrency(route)));
       System.out.println(route.getExchangeTo().getExchangeAccount().balances.get(getDealCurrency(route)));
       System.out.println(route.getExchangeTo().getExchangeAccount().balances.get(getMarketCurrency(route)));
       System.out.println(effectiveAmount);
       System.out.println();
    }

    private void trade(Deal deal, Route route) {
//        System.out.println(route);
//        System.out.println(deal.getBid().getExchange() + " " + deal.getBid().getPrice());
//        System.out.println(deal.getAsk().getExchange() + " " + deal.getAsk().getPrice());
//        System.out.println(deal.getSpread());
//        System.out.println(deal.getEffectiveAmount());
//        System.out.println(deal.getValueInDollars());
//        System.out.println();

        BigDecimal exchangeFromDealAccount = route.getExchangeFrom().getExchangeAccount().balances.get(getDealCurrency(route));
        BigDecimal exchangeFromMarketAccount = route.getExchangeFrom().getExchangeAccount().balances.get(getMarketCurrency(route));

        exchangeFromDealAccount = exchangeFromDealAccount.subtract(deal.getEffectiveAmount());
        route.getExchangeFrom().getExchangeAccount().balances.replace(getDealCurrency(route),exchangeFromDealAccount);

        exchangeFromMarketAccount = exchangeFromMarketAccount.add(deal.getEffectiveAmount().multiply(deal.getBid().getPrice()));
        route.getExchangeFrom().getExchangeAccount().balances.replace(getMarketCurrency(route), exchangeFromMarketAccount);

        BigDecimal exchangeToDealAccount = route.getExchangeTo().getExchangeAccount().balances.get(getDealCurrency(route));
        BigDecimal exchangeToMarketAccount = route.getExchangeTo().getExchangeAccount().balances.get(getMarketCurrency(route));

        exchangeToDealAccount = exchangeToDealAccount.add(deal.getEffectiveAmount());
        route.getExchangeTo().getExchangeAccount().balances.replace(getDealCurrency(route),exchangeToDealAccount);

        exchangeToMarketAccount = exchangeToMarketAccount.subtract(deal.getEffectiveAmount().multiply(deal.getAsk().getPrice()));
        route.getExchangeTo().getExchangeAccount().balances.replace(getMarketCurrency(route),exchangeToMarketAccount);



    }


    private String getDealCurrency(Route route) {
        return route.getPairName().split("/")[0];
    }

    private String getMarketCurrency(Route route){
        return route.getPairName().split("/")[1];
    }


}



