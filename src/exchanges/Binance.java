package exchanges;

import model.Order;
import model.OrderType;
import model.Pair;
import model.Tradable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Copyright (c) Anton on 17.10.2018.
 */
public class Binance extends Exchange implements Runnable {
    private BigDecimal takerTax = BigDecimal.valueOf(0.001);
    private Map <String, Pair> market = new HashMap <>();
    private List <String> pairs = new ArrayList <String>() {{
        add("XRP/BTC");
        add("ETH/BTC");
        add("LTC/BTC");
        add("BCC/BTC");
        add("ADA/BTC");
        add("BAT/BTC");
        add("TRX/BTC");
        add("EOS/BTC");
        add("XLM/BTC");
        add("NEO/BTC");
        add("DASH/BTC");
        add("XMR/BTC");
        add("ETC/BTC");
        add("ZEC/BTC");
        add("BAT/ETH");
        add("XRP/ETH");
        add("ADA/ETH");
        add("ETC/ETH");
        add("BCC/ETH");
        add("XLM/ETH");
        add("TRX/ETH");
        add("LTC/ETH");
        add("NEO/ETH");
        add("DASH/ETH");
    }};

      @Override
    protected String buildAPIRequest(String pair) {
        return "https://api.binance.com/api/v1/depth?symbol=" + casting(pair);
    }

    protected List<Order> findOrders(OrderType type, JSONObject jsonObject, int limit) {
        List <Order> orders = new ArrayList <>();
        try {
            JSONArray jsonObject1 = jsonObject.getJSONArray(getJSONKey(type));
            if (jsonObject1.length() < limit) limit = jsonObject1.length();
            for (int i = 0; i < limit; i++) {
                orders.add(new Order(this, new BigDecimal(jsonObject1.getJSONArray(i).getString(0)),
                        new BigDecimal(jsonObject1.getJSONArray(i).getString(1))));
            }
            return orders;

        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected String casting(String pair) {
        String res;
        String[] split = pair.split("/");
        res = split[0] + split[1];
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
