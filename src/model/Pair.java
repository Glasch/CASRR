package model;

import java.util.List;

/**
 * Copyright (c) Anton on 17.10.2018.
 */
public class Pair {
    private String pairName;
    private List<Order> ordersAsks;
    private List<Order> ordersBids;

    public String getPairName() {
        return pairName;
    }

    public void setPairName(String pairName) {
        this.pairName = pairName;
    }

    public List<Order> getOrders(OrderType type){
        switch (type){
            case BID:
                return ordersBids;
            default:
                return ordersAsks;
        }
    }

    public void setOrders(OrderType type, List <Order> orders){
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
