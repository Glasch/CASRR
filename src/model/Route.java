package model;

import exchanges.Exchange;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Copyright (c) Anton on 17.11.2018.
 */
public class Route {
    private String pairName;
    private Exchange exchangeFrom;
    private Exchange exchangeTo;
    private ArrayList<Deal> deals;
    private BigDecimal routeValueInDollars;
    private BigDecimal routeAmountInDollars;

    public Route(String pairName) {
        this.pairName = pairName;
        deals = new ArrayList<>();
    }

//    public void calcEffectiveAmount() {
//        UsdConverter converter = UsdConverter.getInstance();
//        effectiveAmountInDollars = BigDecimal.ZERO;
//        for (Deal deal : deals) {
//            effectiveAmountInDollars = effectiveAmountInDollars.add(deal.getEffectiveAmount());
//
//        }
//     converter.loadData();
//     effectiveAmountInDollars = converter.convert(pairName,effectiveAmountInDollars);
//    }

    public void applyUSDThreshold(BigDecimal threshold, boolean useFromExchangeRate) {
        BigDecimal remainingThreshold = threshold;

        for (Iterator<Deal> iterator = deals.iterator(); iterator.hasNext(); ) {
            Deal next = iterator.next();
            if (remainingThreshold.equals(BigDecimal.ZERO)) {
                iterator.remove();
            } else {
                BigDecimal rate = useFromExchangeRate ? next.getBid().getPrice() : next.getAsk().getPrice();
                BigDecimal remainingThresholdInCrypto = remainingThreshold.divide(rate, 20, BigDecimal.ROUND_HALF_UP);

                if (next.getEffectiveAmount().compareTo(remainingThresholdInCrypto) <= 0) {
                    remainingThreshold = remainingThreshold.subtract(next.getEffectiveAmount().multiply(rate));
                } else {
                    next.setEffectiveAmount(remainingThresholdInCrypto);
                    remainingThreshold = BigDecimal.ZERO;
                }
            }
        }
    }

    public void addDeal(Deal deal) {
        this.deals.add(deal);
    }

    public void calcRouteValueInDollars() {
        resetRemainingAmounts();
        applyDeals();
        refreshRouteValueInDollars();
    }

    public void refreshRouteValueInDollars() {
        routeValueInDollars = BigDecimal.ZERO;
        routeAmountInDollars = BigDecimal.ZERO;
        for (Deal each : deals) {
            routeValueInDollars = routeValueInDollars.add(each.getValueInDollars());
            routeAmountInDollars = routeAmountInDollars.add(
                    each.getEffectiveAmount().multiply(each.getBid().getPrice()));
        }
    }

    public void filterZeroAmountDeals() {
        deals.removeIf(next -> next.getEffectiveAmount().compareTo(BigDecimal.ZERO) <= 0);
    }

    private void resetRemainingAmounts() {
        for (Deal deal : deals) {
            deal.getAsk().resetRemainingAmount();
            deal.getBid().resetRemainingAmount();
        }
    }

    private void applyDeals() {
        for (Deal deal : deals) {
            deal.refreshEffectiveAmount();
            deal.subtractEffectiveAmount();
        }
    }

    public void filterDeals(BigDecimal border) {
        ArrayList<Deal> sorted = new ArrayList<>();
        for (Deal deal : deals) {
            if (deal.getSpread().compareTo(border) > 0) {
                sorted.add(deal);
            }
        }
        sorted.sort((o1, o2) -> o2.getSpread().compareTo(o1.getSpread()));
        deals = sorted;
    }

    public void fillAllDeals(ArrayList<Exchange> exchanges) {
        for (Exchange exchangeFrom : exchanges) {
            for (Exchange exchangeTo : exchanges) {
                if (exchangeFrom.getMarket().containsKey(pairName) && exchangeTo.getMarket().containsKey(pairName)) {
                    addDealsForExchangesPair(exchangeFrom, exchangeTo);
                }
            }
        }
    }

    public void addDealsForExchangesPair(Exchange from, Exchange to) {
        for (Order orderFrom : from.getMarket().get(getPairName()).getOrders(OrderType.BID)) {
            for (Order orderTo : to.getMarket().get(getPairName()).getOrders(OrderType.ASK)) {
                deals.add(new Deal(this, orderFrom, orderTo));
            }
        }
    }

    public ArrayList<Deal> getSortedEVDeals() {
        return deals;
    }

    public String getPairName() {
        return pairName;
    }

    public void setExchangeFrom(Exchange exchangeFrom) {
        this.exchangeFrom = exchangeFrom;
    }

    public void setExchangeTo(Exchange exchangeTo) {
        this.exchangeTo = exchangeTo;
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

    public BigDecimal getRouteAmountInDollars() {
        return routeAmountInDollars;
    }
}
