package  model;

/*
 * Author: glaschenko
 * Created: 08.11.2018
 */
public enum OrderType {
    BID, ASK;


 public String getJSONKey(OrderType orderType) {
        return orderType == OrderType.BID ? "bids" : "asks";
    }

}
