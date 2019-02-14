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

public class LiveCoin extends Exchange implements Runnable {
    private BigDecimal takerTax = BigDecimal.valueOf(0.0018);
    private HashMap<String, Pair> market = new HashMap <>();
    private ArrayList<String> pairs = new ArrayList <String>() {{
//        add("BTC/USD");
//        add("ETH/USD");
//        add("BCH/USD");
//        add("LTC/USD");
        add("ETH/BTC");
        add("LTC/BTC");
        add("BCH/BTC");
        add("DASH/BTC");
    }};

    @Override
    protected String buildAPIRequest(String pair) {
        return  "https://api.livecoin.net/exchange/order_book?currencyPair=" + pair + "&depth=10";
    }

    protected List <Order> findOrders(OrderType type, JSONObject jsonObject, int limit) {
        List<Order> orders = new ArrayList <>();
        try {
            JSONArray jsonObject1 = jsonObject.getJSONArray(getJSONKey(type));
            if (jsonObject1.length() < limit) limit = jsonObject1.length();
            for (int i = 0; i < limit; i++) {
                orders.add(new Order(this, new BigDecimal(jsonObject1.getJSONArray(i).getString(0)), new BigDecimal(jsonObject1.getJSONArray(i).getString(1))));
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
