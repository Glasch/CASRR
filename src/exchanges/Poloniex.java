package exchanges;

import model.Order;
import model.OrderType;
import model.Pair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Copyright (c) Anton on 19.10.2018.
 */
public class Poloniex extends Exchange implements Runnable {
    private BigDecimal takerTax = BigDecimal.valueOf(0.001);
    private HashMap<String, Pair> market = new HashMap<>();
    private ArrayList<String> pairs = new ArrayList<String>() {{
        add("XRP/BTC");
        add("ETH/BTC");
        add("LTC/BTC");
        add("BCH/BTC");
        add("XMR/BTC");
        add("DASH/BTC");
        add("ETC/BTC");
        add("BAT/BTC");
        add("ZEC/BTC");
        add("XEM/BTC");
        add("BTC/SC");
        add("EOS/BTC");
        add("BCH/ETH");
        add("ETC/ETH");
        add("ZEC/ETH");
        add("EOS/ETH");
        add("BAT/ETH");
    }};

    @Override
    protected String buildAPIRequest(String pair) {
        return  "https://poloniex.com/public?command=returnOrderBook&currencyPair=" + casting(pair) + "&depth=10";
    }

    protected List <Order> findOrders(OrderType type, JSONObject jsonObject, int limit) {
        List<Order> orders = new ArrayList<>();
        try {
            JSONArray jsonObject1 = jsonObject.getJSONArray(getJSONKey(type));
            if (jsonObject1.length() < limit) limit = jsonObject1.length();
            for (int i = 0; i < limit; i++) {
                JSONArray orderLine = jsonObject1.getJSONArray(i);
                orders.add(new Order(this,
                        orderLine.getBigDecimal(0),
                        orderLine.getBigDecimal(1)));
            }
            return orders;

        } catch (JSONException e) {
            return null;
        }
    }

    @Override
    protected String casting(String pair) {
        String res;
        String[] split = pair.split("/");
        res = split[1] + "_" + split[0];
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
