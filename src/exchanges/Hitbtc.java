package exchanges;

import model.Order;
import model.OrderType;
import model.Pair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Objects;

/**
 * Copyright (c) Anton on 17.10.2018.
 */
public class Hitbtc extends Exchange implements Runnable {
    private ArrayList<String> pairs = new ArrayList<String>() {{
        add("BTC/USD");
        add("ETH/USD");
        add("XRP/USDT");
        add("EOS/USD");
        add("LTC/USD");
        add("ZEC/USD");
        add("NEO/USD");
        add("DASH/USD");
        add("TRX/USD");
        add("XLM/USD");
        add("XRP/BTC");
        add("ETH/BTC");
        add("DASH/BTC");
        add("ZEC/BTC");
        add("XMR/BTC");
        add("EOS/BTC");
        add("XLM/BTC");
        add("XEM/BTC");
        add("LSK/BTC");
        add("LTC/BTC");
        add("NEO/BTC");
        add("DOGE/BTC");
        add("ETC/BTC");
        add("TRX/BTC");
        add("GNT/BTC");
        add("ETC/ETH");
        add("DASH/ETH");
        add("ZEC/ETH");
        add("EOS/ETH");
        add("XLM/ETH");
        add("ZEC/ETH");
        add("XRP/ETH");
        add("XMR/ETH");
        add("TRX/ETH");
        add("LTC/ETH");
    }};
    private HashMap<String, Pair> market = new HashMap<>();

    @Override
    protected String buildAPIRequest(String pair) {
        return "https://api.hitbtc.com/api/2/public/orderbook/" + casting(pair) + "?limit=10";
    }

    protected ArrayList<Order> findOrders(OrderType type, JSONObject jsonObject, int limit) {
        ArrayList<Order> orders = new ArrayList<>();
        try {
            //bids.length == asks.length
            if (jsonObject.getJSONArray("ask").length() < limit)
                limit = jsonObject.getJSONArray("ask").length();

            for (int i = 0; i < limit; i++) {
                if (Objects.equals(this.getJSONKey(type), "bids")) {
                    orders.add(new Order(jsonObject.getJSONArray("bid").getJSONObject(i).getBigDecimal("price"), jsonObject.getJSONArray("bid").getJSONObject(i).getBigDecimal("size")));
                } else {
                    orders.add(new Order(jsonObject.getJSONArray("ask").getJSONObject(i).getBigDecimal("price"), jsonObject.getJSONArray("ask").getJSONObject(i).getBigDecimal("size")));
                }
            }


        } catch (Exception e) {
            System.out.println("Hitbtc" + e.toString());
            return null;
        }

        return orders;
    }

    @Override
    protected String casting(String pair) {
        String res;
        String[] split = pair.split("/");
        res = split[0] + split[1];
        return res;
    }

    @Override
    public HashMap<String, Pair> getMarket() {
        return market;
    }

    @Override
    public ArrayList<String> getPairs() {
        return pairs;
    }

}
