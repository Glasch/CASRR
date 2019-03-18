package exchanges;

import model.Order;
import model.OrderType;
import model.Pair;
import org.apache.commons.codec.DecoderException;
import org.json.JSONObject;
import services.ConnectionManager;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Copyright (c) Anton on 17.10.2018.
 */
public class Yobit extends Exchange implements Runnable {
    private BigDecimal takerTax = BigDecimal.valueOf(0.002);
    private HashMap<String, Pair> market = new HashMap<>();
    private static final String apiPrivateUrl = "https://yobit.net/tapi/";
    private static final String key = "F52E287BF91CE5BE8C45475EAB0E1A6C";
    private static final String secretKey = "8400d2f9bf7fc1e77b266e110fde65bd";
    private ArrayList<String> pairs = new ArrayList<String>() {{
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

    @Override
    protected String casting(String pair) {
        String res;
        String[] split = pair.split("/");
        res = split[0] + "_" + split[1];
        return res.toLowerCase();
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

   public  static BigDecimal getBalance(String currency) throws DecoderException {
       JSONObject jsonObject = ConnectionManager.readJSONFromSignedPostRequest(apiPrivateUrl,
               "method=getInfo&nonce=" + String.valueOf(System.currentTimeMillis()).substring(6), key, secretKey);
       return jsonObject.getJSONObject("return").getJSONObject("funds").getBigDecimal(currency.toLowerCase());
   }
}
