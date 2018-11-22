package model;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * Copyright (c) Anton on 17.10.2018.
 */
public class Pair {
    private String pairName;
    private BigDecimal topBid;
    private BigDecimal topAsk;
    private ArrayList<Order> ordersAsks;
    private ArrayList<Order> ordersBids;

    public String getPairName() {
        return pairName;
    }

    public BigDecimal getTopBid() {
        return topBid;
    }

    public BigDecimal getTopAsk() {
        return topAsk;
    }

    public void setPairName(String pairName) {
        this.pairName = pairName;
    }

    public void setTopBid(BigDecimal topBid) {
        this.topBid = topBid;
    }

    public void setTopAsk(BigDecimal topAsk) {
        this.topAsk = topAsk;
    }

    public ArrayList<Order> getOrders(OrderType type){
        switch (type){
            case BID:
                return ordersBids;
            default:
                return ordersAsks;
        }
    }

    public void setOrders(OrderType type, ArrayList<Order> orders){
        switch (type){
            case ASK:
                this.ordersAsks = orders;
                break;
            case BID:
                this.ordersBids = orders;
                break;
        }
    }
}
