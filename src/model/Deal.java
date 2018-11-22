package model;

import services.UsdConverter;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * Copyright (c) Anton on 17.11.2018.
 */
public class Deal {
    private Route route;
    private BigDecimal bidFrom;
    private BigDecimal askTo;
    private BigDecimal amountFrom;
    private BigDecimal amountTo;
    private BigDecimal effectiveAmount;
    private BigDecimal spread;
    private BigDecimal valueInDollars;

    public Deal(Route route, BigDecimal bidFrom, BigDecimal askTo, BigDecimal amountFrom, BigDecimal amountTo) throws IOException {
        this.route = route;
        this.bidFrom = bidFrom;
        this.askTo = askTo;
        this.amountFrom = amountFrom;
        this.amountTo = amountTo;
        this.effectiveAmount = (amountTo.compareTo(amountFrom) == 1) ? amountFrom : amountTo;
        this.spread = calcSpread(this);
        this.valueInDollars = calcValueInDollars(this);
    }

    private BigDecimal calcValueInDollars(Deal deal) throws IOException {
        BigDecimal valueInDollars;
        UsdConverter converter = new UsdConverter();
        valueInDollars = converter.convert(deal.getRoute().getPairName(), deal.getEffectiveAmount()
                .multiply(deal.getAskTo())).multiply(deal.getSpread().divide(BigDecimal.valueOf(100)));
        return valueInDollars;
    }

    private BigDecimal calcSpread(Deal deal) {
        BigDecimal value = null;
        value = ((deal.getBidFrom().subtract(deal.getAskTo())).divide(deal.getBidFrom(),
                4,
                BigDecimal.ROUND_HALF_UP).multiply(BigDecimal.valueOf(100)));
        return value;
    }

    public BigDecimal getBidFrom() {
        return bidFrom;
    }

    public BigDecimal getAskTo() {
        return askTo;
    }

    public BigDecimal getEffectiveAmount() {
        return effectiveAmount;
    }

    public Route getRoute() {
        return route;
    }

    public BigDecimal getSpread() {
        return spread;
    }

    @Override
    public String toString() {
        return "BidFrom/amount: " + this.bidFrom + " " + this.amountFrom + " AskTo/amount: " + this.askTo
                + " " + this.amountTo + " Value: " + this.spread + " Effective Amount: " + this.effectiveAmount
                + " Value in $: " + this.valueInDollars;
    }
}
