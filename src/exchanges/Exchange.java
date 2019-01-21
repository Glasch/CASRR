package exchanges;

import model.ExchangeAccount;
import model.Order;
import model.OrderType;
import model.Pair;
import org.json.JSONObject;
import services.ConnectionManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Copyright (c) Anton on 23.10.2018.
 */
public abstract class Exchange implements Runnable {
    ExchangeAccount exchangeAccount;
    private boolean isMarketValid = true;


    @Override
    public void run() {
        getMarket().clear();
        try {
            updateMarketData();
        } catch (Exception e) {
            validateMarket(getMarket());
        }
    }


    protected Pair createPair(String pairName, JSONObject jsonObject, String request) {
        Pair pair = new Pair();
        pair.setPairName(pairName);
        pair.setOrders(OrderType.ASK, findOrders(OrderType.ASK, jsonObject, 10));
        pair.setOrders(OrderType.BID, findOrders(OrderType.BID, jsonObject, 10));
        try {
            pair.setTopBid(pair.getOrders(OrderType.BID).get(0).getPrice());
            pair.setTopAsk(pair.getOrders(OrderType.ASK).get(0).getPrice());
        } catch (Exception ignored) {
        }
        return pair;
    }

    synchronized void validateMarket(HashMap <String, Pair> market) {
        for (String pairName : market.keySet()) {
            if (market.get(pairName).getOrders(OrderType.BID) == null) {
                this.getPairs().remove(pairName);
            }
        }
        market.keySet().removeIf(o -> market.get(o).getOrders(OrderType.BID) == null);
        if (market.isEmpty()) {
            isMarketValid = false;
        }
    }


    protected void updateMarketData() {
        for (String pairName : getPairs()) {
            String request = buildAPIRequest(pairName);
            JSONObject json = ConnectionManager.readJSONFromRequest(request);
            if(json != null) {
                Pair pair = createPair(pairName, json, request);
                getMarket().put(pair.getPairName(), pair);
            }
            else{
                System.out.println(getClass().getName() + ": unable to get data for pair: " + pairName);
            }
        }
        validateMarket(getMarket());
    }

    protected abstract String buildAPIRequest(String pairName);

    protected abstract ArrayList <Order> findOrders(OrderType type, JSONObject jsonObject, int limit);

    protected abstract String casting(String pair);


    public abstract HashMap <String, Pair> getMarket();

    public abstract ArrayList <String> getPairs();

    public String getLastError() {
        return this + " API Error";
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }

    String getJSONKey(OrderType orderType) {
        return orderType == OrderType.BID ? "bids" : "asks";
    }

    public boolean isMarketValid() {
        return isMarketValid;
    }

    public ExchangeAccount getExchangeAccount() {
        return exchangeAccount;
    }

    public void setExchangeAccount(ExchangeAccount exchangeAccount) {
        this.exchangeAccount = exchangeAccount;
    }
}
