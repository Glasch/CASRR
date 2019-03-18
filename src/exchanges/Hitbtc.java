package exchanges;

import model.Order;
import model.OrderType;
import model.Pair;
import org.apache.http.auth.AuthenticationException;
import org.json.JSONArray;
import org.json.JSONObject;
import services.ConnectionManager;

import java.math.BigDecimal;
import java.util.*;

/**
 * Copyright (c) Anton on 17.10.2018.
 */
public class Hitbtc extends Exchange implements Runnable {
    private BigDecimal takerTax = BigDecimal.valueOf(0.001);
    private Map<String, Pair> market = new HashMap<>();
    private List<String> pairs = new ArrayList<String>() {{
//        add("BTC/USD");
//        add("ETH/USD");
//        add("XRP/USDT");
//        add("EOS/USD");
//        add("LTC/USD");
//        add("ZEC/USD");
//        add("NEO/USD");
//        add("DASH/USD");
//        add("TRX/USD");
//        add("XLM/USD");
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
        add("XRP/ETH");
        add("XMR/ETH");
        add("TRX/ETH");
        add("LTC/ETH");
    }};

    @Override
    protected String buildAPIRequest(String pair) {
        return "https://api.hitbtc.com/api/2/public/orderbook/" + casting(pair) + "?limit=10";
    }

    protected List <Order> findOrders(OrderType type, JSONObject jsonObject, int limit) {
        List<Order> orders = new ArrayList<>();
        try {
            if (jsonObject.getJSONArray("ask").length() < limit)
                limit = jsonObject.getJSONArray("ask").length();

            for (int i = 0; i < limit; i++) {
                if (Objects.equals(this.getJSONKey(type), "bids")) {
                    orders.add(new Order(this, jsonObject.getJSONArray("bid")
                            .getJSONObject(i).getBigDecimal("price"), jsonObject.getJSONArray("bid")
                            .getJSONObject(i).getBigDecimal("size")));
                } else {
                    orders.add(new Order(this, jsonObject.getJSONArray("ask")
                            .getJSONObject(i).getBigDecimal("price"), jsonObject.getJSONArray("ask")
                            .getJSONObject(i).getBigDecimal("size")));
                }
            }


        } catch (Exception e) {
            System.out.println("Hitbtc" + e.toString());
            return null;
        }

        return orders;
    }

    public static BigDecimal getCurrencyBalance(String currency) throws AuthenticationException {
        JSONArray jsonArray = ConnectionManager.getHitBtcBalanceJsonArray("https://api.hitbtc.com/api/2/trading/balance",
                "3f6ffb7a7095445a498c52f66d5192b2",
                "110e4b1bd2426cf05039c6db53c265ab");
        for (Object currencyInfo : jsonArray) {
            if (currencyInfo instanceof JSONObject){
                if (((JSONObject) currencyInfo).get("currency").equals(currency)){
                    return  new BigDecimal (((JSONObject)currencyInfo).getString("available"));
                }
            }
        }
        throw new IllegalStateException("unexpected error getting HitBTC data");
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
