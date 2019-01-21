package model;

import java.math.BigDecimal;
import java.util.HashMap;

/**
 * Copyright (c) Anton on 14.01.2019.
 */
public class ExchangeCosts {
    BigDecimal tradingFee;
    HashMap<String,BigDecimal> cashoutFee;

    public ExchangeCosts(BigDecimal tradingFee) {
        this.tradingFee = tradingFee;
    }
}

