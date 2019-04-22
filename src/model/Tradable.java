package model;

import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.HashMap;

public interface Tradable {
    BigDecimal getBalance(String currency);

    String createOrder(String side, String type, String pair, BigDecimal quantity, BigDecimal price);

    void cancelAllOrders(String pair);

    void cancelAllOrders();

    boolean cancelOrder(String id);

    HashMap<Integer, JSONObject> getActiveOrders(String pair);

    HashMap<Integer, JSONObject> getActiveOrders();

    // TODO: 12.04.2019 Add trading history


}
