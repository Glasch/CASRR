package exchanges;

import model.*;
import org.json.JSONObject;
import services.ConnectionManager;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Copyright (c) Anton on 23.10.2018.
 */
public abstract class Exchange implements Runnable {
    BigDecimal takerTax;
    private ExchangeAccount exchangeAccount;
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

    private Pair createPair(String pairName, JSONObject jsonObject, String request) {
        Pair pair = new Pair();
        pair.setPairName(pairName);
        pair.setOrders(OrderType.ASK, findOrders(OrderType.ASK, jsonObject, 10));
        pair.setOrders(OrderType.BID, findOrders(OrderType.BID, jsonObject, 10));
        return pair;
    }

    private synchronized void validateMarket(Map <String, Pair> market) {
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

    private void updateMarketData() {
        for (String pairName : getPairs()) {
            String request = buildAPIRequest(pairName);
            JSONObject json = ConnectionManager.getRequest(request, null);
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

    String getJSONKey(OrderType orderType) {
        return orderType == OrderType.BID ? "bids" : "asks";
    }

    protected abstract String buildAPIRequest(String pairName);

    protected abstract List <Order> findOrders(OrderType type, JSONObject jsonObject, int limit);

    protected abstract String casting(String pair);

    public abstract Map <String, Pair> getMarket();

    public abstract List <String> getPairs();

    public String getLastError() {
        return this + " API Error";
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

    public BigDecimal getTakerTax() { return takerTax; }

       @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }


}
