package exchanges;

import model.Order;
import model.OrderType;
import model.Pair;
import model.Tradable;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import services.ConnectionManager;

import java.math.BigDecimal;
import java.util.*;

/**
 * Copyright (c) Anton on 17.10.2018.
 */
public class Hitbtc extends Exchange implements Runnable, Tradable {
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
    private static final String API_URL = "https://api.hitbtc.com/api/2/";
    private static final String LOGIN = "3f6ffb7a7095445a498c52f66d5192b2";
    private static final String PASSWORD = "110e4b1bd2426cf05039c6db53c265ab";

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



    /* Trading API */

    @Override
    public BigDecimal getBalance(String currency) {
        JSONArray jsonArray = ConnectionManager.sendBasicGetRequest(API_URL + "trading/balance",
                LOGIN,
                PASSWORD);
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
    public String createOrder(String side, String type, String pair, BigDecimal quantity, BigDecimal price) {

        ArrayList<NameValuePair> postParameters;
        postParameters = new ArrayList<NameValuePair>();
        postParameters.add(new BasicNameValuePair("side", side));
        postParameters.add(new BasicNameValuePair("symbol", pair));
        postParameters.add(new BasicNameValuePair("type", type));
        postParameters.add(new BasicNameValuePair("quantity", quantity.toString()));
        if (price != null) {
            postParameters.add(new BasicNameValuePair("price", price.toString()));
        }

        JSONObject jsonObject = ConnectionManager.sendBasicPostRequest(API_URL + "order",
                postParameters,
                LOGIN,
                PASSWORD);

        return jsonObject.getString("clientOrderId");

        //throw new IllegalStateException(jsonObject.getJSONObject("error").getString("message"));
        //TODO: repeat createOrder, notify
    }

    @Override
    public void cancelAllOrders(String pair) {
        ConnectionManager.sendBasicDeleteRequest(API_URL + "order?symbol=" + pair,
                LOGIN,
                PASSWORD);
    }

    @Override
    public void cancelAllOrders() {
        ConnectionManager.sendBasicDeleteRequest(API_URL + "order",
                LOGIN,
                PASSWORD);
    }

    @Override
    public boolean cancelOrder(String id) {
        ConnectionManager.sendBasicDeleteRequest(API_URL + "order/" + id,
                LOGIN,
                PASSWORD);

        return true;

    }

    @Override
    public HashMap<Integer, JSONObject> getActiveOrders(String pair) {
        return null;
    }

    @Override
    public HashMap<Integer, JSONObject> getActiveOrders() {
        return null;
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
