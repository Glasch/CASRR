package exchanges;

import model.Order;
import model.OrderType;
import model.Pair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

/**
 * Copyright (c) Anton on 18.10.2018.
 */
public class Bittrex extends Exchange implements Runnable {
    private boolean isMarketValid = true;
    private ArrayList <String> pairs = new ArrayList <String>() {{
        add("BTC/USD");
        add("ETH/USD");
        // add("BCH/USD");
        add("XRP/USD");
        add("ZEC/USD");
        add("ADA/USD");
        add("LTC/USD");
        add("TRX/USD");
        add("ETC/USD");
//        add("USD/SC");
        add("XRP/BTC");
        add("ETH/BTC");
        //  add("BCH/BTC");
        add("XMR/BTC");
        add("LTC/BTC");
        add("ADA/BTC");
        add("XVG/BTC");
        add("BAT/BTC");
        add("TRX/BTC");
        add("XLM/BTC");
//        add("BTC/SC");
        //  add("BCH/ETH");
        add("ADA/ETH");
        add("XRP/ETH");
        add("TRX/ETH");
        add("SC/ETH");
        add("DASH/ETH");
        add("ETC/ETH");
        add("XMR/ETH");
        add("XLM/ETH");
        add("ZEC/ETH");
        add("ETC/ETH");
        add("NEO/ETH");
    }};
    private HashMap <String, Pair> market = new HashMap <>();



    @Override
    protected String buildAPIRequest(String pair) {
        return "https://bittrex.com/api/v1.1/public/getorderbook?market=" + casting(pair) + "&type=both";
    }

    protected ArrayList <Order> findOrders(OrderType type, JSONObject jsonObject, int limit) {
        ArrayList <Order> orders = new ArrayList <>();
        try {
            JSONObject jsonObject1 = jsonObject.getJSONObject("result");
            for (int i = 0; i < limit; i++) {
                if (jsonObject1.getJSONArray(getJSONKey(type)).length() < limit)
                    limit = jsonObject1.getJSONArray(getJSONKey(type)).length();
                orders.add(new Order(jsonObject1.getJSONArray(getJSONKey(type)).getJSONObject(i)
                        .getBigDecimal("Rate"), jsonObject1.getJSONArray(getJSONKey(type))
                        .getJSONObject(i).getBigDecimal("Quantity")));
            }
        } catch (JSONException e) {
            return null;
        }
        return orders;
    }

    @Override
    protected String casting(String pair) {
        String res;
        String[] split = pair.split("/");
        res = split[1] + "-" + split[0];
        return res;
    }


    @Override
    String getJSONKey(OrderType orderType) {
        return orderType == OrderType.BID ? "buy" : "sell";
    }

    @Override
    public HashMap <String, Pair> getMarket() {
        return market;
    }

    @Override
    public ArrayList <String> getPairs() {
        return pairs;
    }

    @Override
    public boolean isMarketValid() {
        return isMarketValid;
    }

    @Override
    public String getLastError() {
        return super.getLastError();
    }

}
