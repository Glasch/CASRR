package  exchanges;

import model.Order;
import model.OrderType;
import model.Pair;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.*;

/**
 * Copyright (c) Anton on 17.10.2018.
 */
public class Kucoin extends Exchange implements Runnable {
    private BigDecimal takerTax = BigDecimal.valueOf(0.001);
    private Map<String, Pair> market = new HashMap<>();
    private ArrayList<String> pairs = new ArrayList<String>() {{
        add("BTC/USDT");
        add("ETH/USDT");
        add("EOS/USDT");
        add("GO/USDT");
        add("LTC/USDT");
        add("XLM/USDT");
        add("NEO/USDT");
        add("ETC/USDT");
        add("TRX/USDT");
        add("ETH/BTC");
        add("AOA/BTC");
        add("GO/BTC");
        add("XLM/BTC");
        add("NEO/BTC");
        add("LTC/BTC");
        add("EOS/BTC");
        add("TRX/BTC");
        add("AOA/ETH");
        add("HAV/ETH");
        add("GO/ETH");
        add("NEO/ETH");
        add("XLM/ETH");
        add("TRX/ETH");
        add("EOS/ETH");
    }};

    @Override
    protected String buildAPIRequest(String pair) {
        return "https://api.kucoin.com/v1/open/orders?symbol=" + casting(pair) + "&limit=10";
    }

    protected List <Order> findOrders(OrderType type, JSONObject jsonObject, int limit) {
        ArrayList<Order> orders = new ArrayList<>();
        try {
            Iterator<String> keys = jsonObject.keys();
            String pairKey=keys.next();
            JSONObject jsonObject1 = jsonObject.getJSONObject("data");

            //bids.length == asks.length
            if (jsonObject1.getJSONArray("SELL").length() < limit)
                limit = jsonObject1.getJSONArray("SELL").length();

            for (int i = 0; i < limit; i++) {
                if (Objects.equals(this.getJSONKey(type), "bids")) {
                    orders.add(new Order(this, jsonObject1.getJSONArray("BUY").getJSONArray(i).getBigDecimal(0), jsonObject1.getJSONArray("BUY").getJSONArray(i).getBigDecimal(1)));
                } else {
                    orders.add(new Order(this, jsonObject1.getJSONArray("SELL").getJSONArray(i).getBigDecimal(0), jsonObject1.getJSONArray("SELL").getJSONArray(i).getBigDecimal(1)));
                }
            }


        } catch (Exception e) {
            System.out.println("Kucoin" + e.toString());
            return null;
        }

        return orders;
    }

    @Override
    protected String casting(String pair) {
        String res;
        String[] split = pair.split("/");
        res = split[0] + "-" + split[1];
        return res;
    }

    @Override
    public Map <String, Pair> getMarket() {
        return market;
    }

    @Override
    public List <String> getPairs() {
        return pairs;
    }

    public BigDecimal getTakerTax() {
        return takerTax;
    }

}
