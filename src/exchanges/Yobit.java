package exchanges;

import com.fasterxml.jackson.annotation.JsonAlias;
import model.Order;
import model.OrderType;
import model.Pair;
import model.Tradable;
import org.apache.commons.codec.DecoderException;
import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import services.ConnectionManager;

import javax.swing.*;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Copyright (c) Anton on 17.10.2018.
 */
public class Yobit extends Exchange implements Runnable, Tradable {
    private BigDecimal takerTax = BigDecimal.valueOf(0.002);
    private HashMap<String, Pair> market = new HashMap<>();
    private static final String API_URL = "https://yobit.net/tapi/";
    private static final String KEY = "61491A609906E498E8A2AC414C405B3B";
    private static final String SECRET_KEY = "5fdd666e83bb81577181a074a8062510";

    private ArrayList<String> pairs = new ArrayList<String>() {{
        add("ETH/USD");
        add("ETH/BTC");
        add("DASH/BTC");
        add("ZEC/BTC");
        add("LSK/BTC");
        add("LTC/BTC");
        add("WAVES/BTC");
        add("DOGE/BTC");
        add("XRP/BTC");
//        add("BTC/USD");
    }};

    @Override
    protected String buildAPIRequest(String pair) {
        return "https://yobit.io/api/3/depth/" + casting(pair) + "?limit=10";
    }

    private String genNonce(){
        return String.valueOf(System.currentTimeMillis()).substring(2,11); //Generate new key in Sept 2020
    }
    protected List <Order> findOrders(OrderType type, JSONObject jsonObject, int limit) {
        List<Order> orders = new ArrayList<>();
        try {
            Iterator<String> keys = jsonObject.keys();
            String pairKey=keys.next();
            JSONObject jsonObject1 = jsonObject.getJSONObject(pairKey);

            //bids.length == asks.length
            if (jsonObject1.getJSONArray("bids").length() < limit)
                limit = jsonObject1.getJSONArray("bids").length();

            for (int i = 0; i < limit; i++) {
                if (Objects.equals(this.getJSONKey(type), "bids")) {
                    orders.add(new Order(this, jsonObject1.getJSONArray("bids").getJSONArray(i).getBigDecimal(0), jsonObject1.getJSONArray("bids").getJSONArray(i).getBigDecimal(1)));
                } else {
                    orders.add(new Order(this, jsonObject1.getJSONArray("asks").getJSONArray(i).getBigDecimal(0), jsonObject1.getJSONArray("asks").getJSONArray(i).getBigDecimal(1)));
                }
            }

        } catch (Exception e) {
            return null;
        }

        return orders;
    }

    private HashMap<String, String> prepareAuthHeaders(String data){
        String signedData = ConnectionManager.calculateHMAC(data, SECRET_KEY);
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Key", KEY);
        headers.put("Sign", signedData);
        return headers;
    }

    public List <Order> loadOrders(String pair, OrderType type, int limit){
        String url = "https://yobit.net/api/3/depth/" + casting(pair) + "?limit=" + limit;
        JSONObject json = ConnectionManager.getRequest(url, null);
        return findOrders(type, json, limit);
    }

    @Override
    protected String casting(String pair) {
        String res;
        String[] split = pair.split("/");
        res = split[0] + "_" + split[1];
        return res.toLowerCase();
    }

    @Override
    public BigDecimal getBalance(String currency) {
        BigDecimal balance = null;
        String data = "method=getInfo&nonce=" + genNonce();


        JSONObject httpResponse = ConnectionManager.sendPostRequest(API_URL, data, prepareAuthHeaders(data));

        try {
            balance = httpResponse.getJSONObject("return").getJSONObject("funds").getBigDecimal(currency.toLowerCase());
        } catch (JSONException e){
            System.out.println("Parsing getInfo failed. Response:");
            System.out.println(httpResponse.toString());
            System.out.println("Parsing getInfo failed. Error Message:");
            System.out.println(e.getMessage());
            return null;
        }

        return balance;
    }

    @Override
    public String createOrder(String side, String type, String pair, BigDecimal quantity, BigDecimal price) {
        JSONObject httpResponse = null;
        Integer status;

        if ( quantity.compareTo(BigDecimal.ZERO) == 0 ) return null;

        try {

            if (type == "limit") {
                String data = "method=Trade&nonce=" + genNonce() + "&pair=" + casting(pair) + "&type=" + side +
                        "&rate=" + price + "&amount=" + quantity;
               httpResponse = ConnectionManager.sendPostRequest(API_URL, data, prepareAuthHeaders(data));
            } else if (type == "market"){


                if (side == "buy") {
                    List<Order> orders = loadOrders(pair, OrderType.ASK, 1);
                    BigDecimal topOrderAmount = orders.get(0).getAmount();
                    BigDecimal amountToBuy = quantity;

                    if (amountToBuy.compareTo(topOrderAmount) == 1){
                        amountToBuy = topOrderAmount;
                    }

                    String data = "method=Trade&nonce=" + genNonce() + "&pair=" + casting(pair) + "&type=" + side +
                            "&rate=" + orders.get(0).getPrice() + "&amount=" + amountToBuy;


                    httpResponse = ConnectionManager.sendPostRequest(API_URL, data, prepareAuthHeaders(data));

                    status = httpResponse.getInt("success");

                    if ( status != 1){
                        throw new Exception("status: error");
                    }

                    BigDecimal amountBought =  httpResponse.getJSONObject("return").getBigDecimal("received");

                    if ( amountBought.compareTo(amountToBuy) != 0 ){
                        cancelOrder(httpResponse.getJSONObject("return").getBigDecimal("order_id").toString());


                    }
                    BigDecimal amountLeft = quantity.subtract(amountBought);

                    System.out.println("Planned to Buy: " + amountToBuy + " Sold " + amountBought + " with price "
                            + orders.get(0).getPrice() + " amountLeft: " + amountLeft);
                    System.out.println("Amount Left " + quantity.subtract(amountToBuy));

                    createOrder(side, type, pair, amountLeft, null);
                } else if (side == "sell"){
                    List<Order> orders = loadOrders(pair, OrderType.BID, 1);
                    BigDecimal topOrderAmount = orders.get(0).getAmount();
                    BigDecimal amountToSell = quantity;

                    if (amountToSell.compareTo(topOrderAmount) == 1){
                        amountToSell = topOrderAmount;
                    }

                    String data = "method=Trade&nonce=" + genNonce() + "&pair=" + casting(pair) + "&type=" + side +
                            "&rate=" + orders.get(0).getPrice() + "&amount=" + amountToSell;

                    System.out.println("Sold " + amountToSell + " with price " + orders.get(0).getPrice());
                    System.out.println("Amount Left " + quantity.subtract(amountToSell));
                    httpResponse = ConnectionManager.sendPostRequest(API_URL, data, prepareAuthHeaders(data));

                    status = httpResponse.getInt("success");

                    if ( status != 1){
                        throw new Exception("status: error");
                    }

                    BigDecimal amountLeft = httpResponse.getJSONObject("return").getBigDecimal("remains");

                    createOrder(side, type, pair, amountLeft, null);

                } else {
                    throw new Exception("Unexpected side");
                }
            }



            status = httpResponse.getInt("success");

            if ( status != 1){
                throw new Exception("status: error");
            }

            return httpResponse.getJSONObject("return").getString("order_id");
        } catch (Exception e){
            System.out.println("Parsing Trade response failed. Response:");
            System.out.println(httpResponse.toString());
            System.out.println("Error Message:");
            System.out.println(e.getMessage());
            return null;
        }

    }

    @Override
    public void cancelAllOrders(String pair) {
        HashMap<Integer, JSONObject> orders = getActiveOrders(pair);

        for (Map.Entry<Integer, JSONObject> entry : orders.entrySet()) {
            if (entry.getValue() instanceof JSONObject) {
                String order_id = entry.getValue().getString("order_id");

                if (!cancelOrder(order_id)) {
                    System.out.println("Cancelling order failed. Order ID:" + order_id);
                }

            } else {
                System.out.println("Unexpected object in cancelAllOrders. Object:" + entry.getValue());
            }

        }
    }

    @Override
    public void cancelAllOrders() {
        HashMap<Integer, JSONObject> orders = getActiveOrders();

        for (Map.Entry<Integer, JSONObject> entry : orders.entrySet()) {
            if (entry.getValue() instanceof JSONObject) {
                String order_id = entry.getValue().getString("order_id");

                if (!cancelOrder(order_id)) {
                    System.out.println("Cancelling order failed. Order ID:" + order_id);
                }

            } else {
                System.out.println("Unexpected object in cancelAllOrders. Object:" + entry.getValue());
            }

        }
    }

    @Override
    public boolean cancelOrder(String id) {
        String data = "method=CancelOrder&nonce=" + genNonce() + "&order_id=" + id;
        JSONObject httpResponse = ConnectionManager.sendPostRequest(API_URL, data, prepareAuthHeaders(data));
        Integer status;
        try {
            status = httpResponse.getInt("success");

            if ( status != 1){
                throw new Exception("status: error");
            }

            return true;

        } catch (Exception e){
            System.out.println("Parsing Trade response failed. Response:");
            System.out.println(httpResponse.toString());
            System.out.println("Error Message:");
            System.out.println(e.getMessage());
            return false;
        }
    }

    @Override
    public HashMap<Integer, JSONObject> getActiveOrders(String pair) {
        String data = "method=ActiveOrders&nonce=" + genNonce() + "&pair=" + casting(pair);

        JSONObject httpResponse = ConnectionManager.sendPostRequest(API_URL, data, prepareAuthHeaders(data));
        Integer status;
        HashMap<Integer, JSONObject> orders = new HashMap<>();

        try {
            status = httpResponse.getInt("success");

            if ( status != 1){
                throw new Exception("status: error");
            }

            if (httpResponse.has("return")) {
                Iterator<String> keys = httpResponse.getJSONObject("return").keys();
                int i = 0;
                while (keys.hasNext()) {
                    String key = keys.next();

                    JSONObject order = (JSONObject) httpResponse.getJSONObject("return").get(key);
                    order.put("order_id", key);
                    orders.put(i, order);
                }
            }
           return orders;

        } catch (Exception e){
            System.out.println("Parsing ActiveOrders response failed. Response:");
            System.out.println(httpResponse.toString());
            System.out.println("Error Message:");
            System.out.println(e.getMessage());
            return null;
        }
    }

    @Override
    public HashMap<Integer, JSONObject> getActiveOrders() {
        HashMap<Integer, JSONObject> orders = new HashMap<>();

        Iterator i = pairs.iterator();

        while (i.hasNext()) {
            orders.putAll( getActiveOrders(i.next().toString()));
        }

        return orders;
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


    public static String getApiPrivateUrl() {
        return API_URL;
    }

    public static String getKey() {
        return KEY;
    }

    public static String getSecretKey() {
        return SECRET_KEY;
    }


}
