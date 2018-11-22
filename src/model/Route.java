package model;

import exchanges.Exchange;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * Copyright (c) Anton on 17.11.2018.
 */
public class Route {
    private String pairName;
    private Exchange exchangeFrom;
    private Exchange exchangeTo;
    private ArrayList <Deal> deals;
    private ArrayList <Deal> sortedEVDeals;
    private BigDecimal routeValue;


    public Route(String pairName, Exchange exchangeFrom, Exchange exchangeTo) throws IOException {
        this.pairName = pairName;
        this.exchangeFrom = exchangeFrom;
        this.exchangeTo = exchangeTo;
        this.deals = findDeals(this);
        this.sortedEVDeals = filterDeals(deals);
        this.routeValue = findRouteValue(sortedEVDeals);
    }

    private BigDecimal findRouteValue(ArrayList <Deal> sortedEVDeals) {
        BigDecimal routeValue = null;

        return routeValue;
    }

    private ArrayList <Deal> filterDeals(ArrayList <Deal> deals) {
        ArrayList <Deal> sortedEVDeals = new ArrayList <>();
        for (Deal deal : deals) {
            if (deal.getSpread().compareTo(BigDecimal.ZERO) == 1) {
                sortedEVDeals.add(deal);
            }
        }
        sortedEVDeals.sort((o1, o2) -> {
            BigDecimal bigDecimal1 = o1.getSpread();
            BigDecimal bigDecimal2 = o2.getSpread();
            return bigDecimal2.compareTo(bigDecimal1);
        });
        return sortedEVDeals;
    }

    private ArrayList <Deal> findDeals(Route route) throws IOException {
        ArrayList <Deal> deals = new ArrayList <>();
        for (Order orderFrom : route.getExchangeFrom().getMarket().get(route.getPairName()).getOrders(OrderType.BID)) {
            for (Order orderTo : route.getExchangeTo().getMarket().get(route.getPairName()).getOrders(OrderType.ASK)) {
                deals.add(new Deal(route, orderFrom.getPrice(), orderTo.getPrice(), orderFrom.getAmount(), orderTo.getAmount()));
            }
        }
        return deals;
    }

    public ArrayList <Deal> getSortedEVDeals() {
        return sortedEVDeals;
    }

    public String getPairName() {
        return pairName;
    }

    public Exchange getExchangeFrom() {
        return exchangeFrom;
    }

    public Exchange getExchangeTo() {
        return exchangeTo;
    }
}
