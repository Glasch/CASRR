package  exchanges;

import model.Order;
import model.OrderType;
import model.Pair;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.*;

/**
 * Copyright (c) Anton on 17.10.2018.
 */
public class Exmo extends Exchange implements Runnable {
    private BigDecimal takerTax = BigDecimal.valueOf(0.002);
    private Map<String, Pair> market = new HashMap<>();
    private List<String> pairs = new ArrayList<String>() {{
//        add("BTC/USD");
//        add("ETH/USD");
//        add("BCH/USD");
//        add("NEO/USD");
//        add("ADA/USD");
//        add("TRX/USD");
//        add("XLM/USD");
//        add("EOS/USD");
        add("XRP/BTC");
        add("ETH/BTC");
        add("BCH/BTC");
        add("LTC/BTC");
        add("NEO/BTC");
        add("ADA/BTC");
        add("TRX/BTC");
        add("XLM/BTC");
        add("EOS/BTC");
        add("DASH/BTC");
        add("ETC/BTC");
        add("ZEC/BTC");
        add("XMR/BTC");
        add("ADA/ETH");
        add("BCH/ETH");
    }};

    @Override
    protected String buildAPIRequest(String pair) {
        return  "https://api.exmo.me/v1/order_book/?pair=" + casting(pair);
    }

    protected List <Order> findOrders(OrderType type, JSONObject jsonObject, int limit) {
        List<Order> orders = new ArrayList<>();
        try {
            Iterator<String> keys = jsonObject.keys();
            String pairKey=keys.next();
            JSONObject jsonObject1 = jsonObject.getJSONObject(pairKey);
            for (int i = 0; i < limit; i++) {
                if (("bids".equals(getJSONKey(type)))) {
                    if (jsonObject1.getJSONArray("bid").length() < limit)
                        limit = jsonObject1.getJSONArray("bid").length();
                    orders.add( new Order(this, jsonObject1.getJSONArray("bid").getJSONArray(i).getBigDecimal(0), (jsonObject1.getJSONArray("bid").getJSONArray(i).getBigDecimal(1))));
                } else {
                    if (jsonObject1.getJSONArray("ask").length() < limit)
                        limit = jsonObject1.getJSONArray("ask").length();
                    orders.add(new Order(this, jsonObject1.getJSONArray("ask").getJSONArray(i).getBigDecimal(0), (jsonObject1.getJSONArray("ask").getJSONArray(i).getBigDecimal(1))));
                }
            }
        } catch (JSONException e) {
            System.out.println("EXMO" + e.toString());
            return null;
        }
        return orders;
    }

    @Override
    protected String casting(String pair) {
        String res;
        String[] split = pair.split("/");
        res = split[0] + "_" + split[1];
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

