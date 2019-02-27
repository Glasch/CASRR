package model;

import services.UsdConverter;

import java.math.BigDecimal;

/**
 * Copyright (c) Anton on 17.11.2018.
 */
public class Deal {
    private Route route;
    private Order bid; //from
    private Order ask; //to
    private BigDecimal effectiveAmount;
    private BigDecimal spread;
    private BigDecimal valueInDollars;
    private BigDecimal taxFrom;
    private BigDecimal taxTo;

    Deal(Route route, Order bid, Order ask) {
        this.route = route;
        this.bid = bid;
        this.ask = ask;
       refreshEffectiveAmount();
    }

    void refreshEffectiveAmount() {
        BigDecimal askAmount = ask.getRemainingAmount();
        BigDecimal bidAmount = bid.getRemainingAmount();
        this.effectiveAmount = (askAmount.compareTo(bidAmount) > 0) ? bidAmount : askAmount;
        if(effectiveAmount.compareTo(BigDecimal.ZERO) <= 0) effectiveAmount = BigDecimal.ZERO;
        this.spread = calcSpread();
        this.valueInDollars = calcValueInDollars();
    }

    void subtractEffectiveAmount(){
        ask.subtractRemainingAmount(effectiveAmount);
        bid.subtractRemainingAmount(effectiveAmount);
    }

    private BigDecimal calcValueInDollars() {
        UsdConverter converter =  UsdConverter.getInstance();
        BigDecimal valueInDollars = converter.convert(route.getPairName(), effectiveAmount
                .multiply(ask.getPrice())).multiply(spread.divide(BigDecimal.valueOf(100)));
        return valueInDollars;
    }

    private BigDecimal calcSpread() {
        BigDecimal value = ((bid.getPrice().subtract(ask.getPrice())).divide(ask.getPrice(),
                4,
                BigDecimal.ROUND_FLOOR)); //.multiply(BigDecimal.valueOf(100)));
        return value;
    }
   public BigDecimal getEffectiveAmount() {
        return effectiveAmount;
    }

    public Route getRoute() {
        return route;
    }

  public   BigDecimal getSpread() {
        return spread;
    }

    public Order getBid() {
        return bid;
    }

    public Order getAsk() {
        return ask;
    }

    @Override
    public String toString() {
        return "Bid: " + bid + " Ask: " + ask + ". Effective Amount: " + effectiveAmount
                + " Spread: " + spread;
    }

    void setEffectiveAmount(BigDecimal effectiveAmount) {
        this.effectiveAmount = effectiveAmount;
    }

    BigDecimal getTaxFrom() {
        return taxFrom;
    }

    void setTaxFrom(BigDecimal taxFrom) {
        this.taxFrom = taxFrom;
    }

    BigDecimal getTaxTo() {
        return taxTo;
    }

    void setTaxTo(BigDecimal taxTo) {
        this.taxTo = taxTo;
    }

    BigDecimal getValueInDollars() {
        return valueInDollars;
    }


}
