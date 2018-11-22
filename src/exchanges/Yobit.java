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
public class Yobit extends Exchange implements Runnable {
    private ArrayList<String> pairs = new ArrayList<String>() {{
        add("ETH/BTC");
        add("DASH/BTC");
        add("ZEC/BTC");
        add("LSK/BTC");
        add("LTC/BTC");
        add("WAVES/BTC");
        add("DOGE/BTC");
        add("XRP/BTC");
        add("BTC/USD");
    }};
    private HashMap<String, Pair> market = new HashMap<>();

    @Override
    protected String buildAPIRequest(String pair) {
        return "https://yobit.net/api/3/depth/" + casting(pair) + "?limit=10";
    }

    protected ArrayList<Order> findOrders(OrderType type, JSONObject jsonObject, int limit) {
        ArrayList<Order> orders = new ArrayList<>();
        try {
            Iterator<String> keys = jsonObject.keys();
            String pairKey=keys.next();
            JSONObject jsonObject1 = jsonObject.getJSONObject(pairKey);

            //bids.length == asks.length
            if (jsonObject1.getJSONArray("bids").length() < limit)
                limit = jsonObject1.getJSONArray("bids").length();

            for (int i = 0; i < limit; i++) {
                if (Objects.equals(this.getJSONKey(type), "bids")) {
                    orders.add(new Order(jsonObject1.getJSONArray("bids").getJSONArray(i).getBigDecimal(0), jsonObject1.getJSONArray("bids").getJSONArray(i).getBigDecimal(1)));
                } else {
                    orders.add(new Order(jsonObject1.getJSONArray("asks").getJSONArray(i).getBigDecimal(0), jsonObject1.getJSONArray("asks").getJSONArray(i).getBigDecimal(1)));
                }
            }


        } catch (Exception e) {
            return null;
        }

        return orders;
    }

    @Override
    protected String casting(String pair) {
        String res;
        String[] split = pair.split("/");
        res = split[0] + "_" + split[1];
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
