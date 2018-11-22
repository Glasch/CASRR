package exchanges;

import exchanges.Exchange;
import model.Order;
import model.OrderType;
import model.Pair;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;

public class Bitfinex extends Exchange implements Runnable {
    private ArrayList <String> pairs = new ArrayList <String>() {{
        add("BTC/USD");
        add("ETH/USD");
        // add("BCH/USD");
        add("XRP/USD");
        add("EOS/USD");
        add("LTC/USD");
        add("DSH/USD");
        add("NEO/USD");
        add("ETC/USD");
        add("ZEC/USD");
        add("XRP/BTC");
        add("ETH/BTC");
        //  add("BCH/BTC");
        add("XMR/BTC");
        add("LTC/BTC");
        add("DSH/BTC");
        add("EOS/BTC");
        add("ETC/BTC");
        add("NEO/BTC");
        add("ZEC/BTC");
        add("TRX/BTC");
        add("XLM/BTC");
        add("BAT/BTC");
        //  add("BCH/ETH");
        add("EOS/ETH");
        add("NEO/ETH");
        add("TRX/ETH");
        add("XLM/ETH");
    }};
    private HashMap <String, Pair> market = new HashMap <>();


    @Override
    protected String buildAPIRequest(String pair) {
        return "https://api.bitfinex.com/v1/book/" + casting(pair);
    }

    protected ArrayList <Order> findOrders(OrderType type, JSONObject jsonObject, int limit) {
        ArrayList <Order> orders = new ArrayList <>();
        try {
            for (int i = 0; i < limit; i++) {
                if ("bids".equals(getJSONKey(type))) {
                    if (jsonObject.getJSONArray("bids").length() < limit)
                        limit = jsonObject.getJSONArray("bids").length();
                    orders.add(new Order(new BigDecimal(jsonObject.getJSONArray("bids").getJSONObject(i).getString("price")),
                            new BigDecimal(jsonObject.getJSONArray("bids").getJSONObject(i).getString("amount"))));
                } else {
                    if (jsonObject.getJSONArray("asks").length() < limit)
                        limit = jsonObject.getJSONArray("asks").length();
                    orders.add(new Order(new BigDecimal(jsonObject.getJSONArray("asks").getJSONObject(i).getString("price")),
                            new BigDecimal(jsonObject.getJSONArray("asks").getJSONObject(i).getString("amount"))));
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
        res = split[0] + split[1];
        return res;
    }



    @Override
    public HashMap <String, Pair> getMarket() {
        return market;
    }

    @Override
    public ArrayList <String> getPairs() {
        return pairs;
    }

}
