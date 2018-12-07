package exchanges;

import exchanges.Exchange;
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
public class Huobi extends Exchange implements Runnable {
    private ArrayList<String> pairs = new ArrayList<String>() {{
        add("BTC/USDT");
        add("ETH/USDT");
        add("XRP/USDT");
        add("LTC/USDT");
        add("ETC/USDT");
        add("EOS/USDT");
        add("ADA/USDT");
        add("DASH/USDT");
        add("ZEC/USDT");
        add("TRX/USDT");
        add("OMG/USDT");
        add("ETH/BTC");
        add("XRP/BTC");
        add("BCH/BTC");
        add("LTC/BTC");
        add("ETC/BTC");
        add("EOS/BTC");
        add("ADA/BTC");
        add("ZEC/BTC");
        add("DASH/BTC");
        add("XMR/BTC");
        add("XLM/BTC");
        add("TRX/BTC");
        add("EOS/ETH");
        add("OMG/ETH");
        add("TRX/ETH");
        add("XLM/ETH");
        add("XMR/ETH");
    }};
    private HashMap<String, Pair> market = new HashMap<>();

    @Override
    protected String buildAPIRequest(String pair) {
        return "https://api.huobi.pro/market/depth?symbol=" + casting(pair) + "&type=step1";
    }

    protected ArrayList<Order> findOrders(OrderType type, JSONObject jsonObject, int limit) {
        ArrayList<Order> orders = new ArrayList<>();
        try {

            JSONObject jsonObject1 = jsonObject.getJSONObject("tick");
            //bids.length == asks.length
            if (jsonObject1.getJSONArray("bids").length() < limit)
                limit = jsonObject.getJSONArray("bids").length();

            for (int i = 0; i < limit; i++) {
                if (Objects.equals(this.getJSONKey(type), "bids")) {
                    orders.add(new Order(this, jsonObject1.getJSONArray("bids").getJSONArray(i).getBigDecimal(0), jsonObject1.getJSONArray("bids").getJSONArray(i).getBigDecimal(1)));
                } else {
                    orders.add(new Order(this, jsonObject1.getJSONArray("asks").getJSONArray(i).getBigDecimal(0), jsonObject1.getJSONArray("asks").getJSONArray(i).getBigDecimal(1)));
                }
            }


        } catch (Exception e) {
            System.out.println("Huobi" + e.toString());
            return null;
        }

        return orders;
    }

    @Override
    protected String casting(String pair) {
        String res;
        String[] split = pair.split("/");
        res = split[0] + split[1];
        return res.toLowerCase();
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
