package exchanges;

import model.Order;
import model.OrderType;
import model.Pair;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Bitfinex extends Exchange implements Runnable {
    private BigDecimal takerTax = BigDecimal.valueOf(0.001);
    private Map <String, Pair> market = new HashMap <>();
    private List <String> pairs = new ArrayList <String>() {{
//        add("BTC/USD");
//        add("ETH/USD");
//        add("BCH/USD");
//        add("XRP/USD");
//        add("EOS/USD");
//        add("LTC/USD");
//        add("DSH/USD");
//        add("NEO/USD");
//        add("ETC/USD");
//        add("ZEC/USD");
        add("XRP/BTC");
        add("ETH/BTC");
        add("BCH/BTC");
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
        add("BCH/ETH");
        add("EOS/ETH");
        add("NEO/ETH");
        add("TRX/ETH");
        add("XLM/ETH");
    }};

    @Override
    protected String buildAPIRequest(String pair) {
        return "https://api.bitfinex.me/v1/book/" + casting(pair);
    }

    protected List <Order> findOrders(OrderType type, JSONObject jsonObject, int limit) {
        List <Order> orders = new ArrayList <>();
        try {
            for (int i = 0; i < limit; i++) {
                if ("bids".equals(getJSONKey(type))) {
                    if (jsonObject.getJSONArray("bids").length() < limit)
                        limit = jsonObject.getJSONArray("bids").length();
                    orders.add(new Order(this, new BigDecimal(jsonObject.getJSONArray("bids").getJSONObject(i).getString("price")),
                            new BigDecimal(jsonObject.getJSONArray("bids").getJSONObject(i).getString("amount"))));
                } else {
                    if (jsonObject.getJSONArray("asks").length() < limit)
                        limit = jsonObject.getJSONArray("asks").length();
                    orders.add(new Order(this, new BigDecimal(jsonObject.getJSONArray("asks").getJSONObject(i).getString("price")),
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
