package model;

import exchanges.Exchange;
import services.UsdConverter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Copyright (c) Anton on 17.11.2018.
 */
public class Route {
    private String pairName;
    private Exchange exchangeFrom;
    private Exchange exchangeTo;
    private ArrayList <Deal> deals;
    private ArrayList <Deal> sortedEVDeals;
    private BigDecimal routeValueInDollars;


    public Route(String pairName, Exchange exchangeFrom, Exchange exchangeTo) {
        this.pairName = pairName;
        this.exchangeFrom = exchangeFrom;
        this.exchangeTo = exchangeTo;
        this.deals = findDeals();
        filterDeals();
        calcRouteValueInDollars();
    }

    public Route(String pairName){
        this.pairName = pairName;
        deals = new ArrayList<>();
    }

    public void addDeals(List<Deal> deals){
        this.deals.addAll(deals);
    }

    public void calcRouteValueInDollars() {
        resetRemainingAmounts();
        applyDeals();

        routeValueInDollars = BigDecimal.ZERO;
        for (Deal each : sortedEVDeals) {
            routeValueInDollars = routeValueInDollars.add(each.getValueInDollars());
        }
    }

    public void filterZeroAmountDeals(){
        sortedEVDeals.removeIf(next -> next.getEffectiveAmount().compareTo(BigDecimal.ZERO) <= 0);
    }

    private void resetRemainingAmounts() {
        for (Deal deal : deals) {
            deal.getAsk().resetRemainingAmount();
            deal.getBid().resetRemainingAmount();
        }
    }

    private void applyDeals() {
        for (Deal deal : sortedEVDeals) {
            deal.refreshEffectiveAmount();
            deal.subtractEffectiveAmount();
        }
    }

    public void filterDeals() {
        ArrayList <Deal> sorted = new ArrayList <>();
        for (Deal deal : deals) {
            if (deal.getSpread().compareTo(BigDecimal.ZERO) > 0) {
                sorted.add(deal);
            }
        }
        sorted.sort((o1, o2) -> o2.getSpread().compareTo(o1.getSpread()));
        sortedEVDeals =  sorted;
    }

    private ArrayList <Deal> findDeals(){
        ArrayList <Deal> deals = new ArrayList <>();
        for (Order orderFrom : getExchangeFrom().getMarket().get(getPairName()).getOrders(OrderType.BID)) {
            for (Order orderTo : getExchangeTo().getMarket().get(getPairName()).getOrders(OrderType.ASK)) {
                deals.add(new Deal(this, orderFrom, orderTo));
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

    public BigDecimal getRouteValueInDollars() {
        return routeValueInDollars;
    }

    @Override
    public String toString() {
        return exchangeFrom + " ---> " + exchangeTo + " " + pairName + " " + getRouteValueInDollars();
    }
}
