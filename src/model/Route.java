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
        this.deals = findDeals();
        this.sortedEVDeals = filterDeals();
        this.routeValue = findRouteValue();
    }

    private BigDecimal findRouteValue() {
//        applyDeals();
        BigDecimal routeValue = null;

        return routeValue;
    }

//    private void applyDeals() {
//        for (int i = 0; i < sortedEVDeals.size(); i++) {
//            Deal deal = sortedEVDeals.get(i);
//            for (int j = i+1; j < sortedEVDeals.size(); j++) {
//                Deal nextDeal = sortedEVDeals.get(j);
//                nextDeal.setBidFrom(nextDeal.getBidFrom().subtract(deal.getEffectiveAmount()));
//                nextDeal.setAskTo(nextDeal.getAskTo().subtract(deal.getEffectiveAmount()));
//                nextDeal.
//            }
//        }
//        for (Deal deal : sortedEVDeals) {
//        }
//
//    }

    private ArrayList <Deal> filterDeals() {
        ArrayList <Deal> sortedEVDeals = new ArrayList <>();
        for (Deal deal : deals) {
            if (deal.getSpread().compareTo(BigDecimal.ZERO) > 0) {
                sortedEVDeals.add(deal);
            }
        }
        sortedEVDeals.sort((o1, o2) -> o2.getSpread().compareTo(o1.getSpread()));
        return sortedEVDeals;
    }

    private ArrayList <Deal> findDeals() throws IOException {
        ArrayList <Deal> deals = new ArrayList <>();
        for (Order orderFrom : getExchangeFrom().getMarket().get(getPairName()).getOrders(OrderType.BID)) {
            for (Order orderTo : getExchangeTo().getMarket().get(getPairName()).getOrders(OrderType.ASK)) {
                deals.add(new Deal(this, orderFrom.getPrice(), orderTo.getPrice(), orderFrom.getAmount(), orderTo.getAmount()));
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
