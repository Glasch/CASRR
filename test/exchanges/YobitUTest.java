package exchanges;

import com.fasterxml.jackson.databind.util.JSONPObject;
import junit.framework.TestCase;
import model.Order;
import model.OrderType;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class YobitUTest extends TestCase  {

    public void testGetBalance() {
        Yobit yobit = new Yobit();
        BigDecimal res = yobit.getBalance("USD");
        System.out.println(res);
    }

    public void testNonce(){
        Yobit yobit = new Yobit();
        for (int i = 0; i < 20; i++){
            System.out.println(i);
            long startTime = System.currentTimeMillis();
            BigDecimal res = yobit.getBalance("USD");

            long totalTime = System.currentTimeMillis() - startTime;

            System.out.println(totalTime);

        }
    }

    public void testGetActiveOrders(){
        Yobit yobit = new Yobit();
        HashMap<Integer, JSONObject> res = yobit.getActiveOrders("ETH/USD");
        System.out.println();
    }

    public void testGetAllActiveOrders(){
        Yobit yobit = new Yobit();
        HashMap<Integer, JSONObject> res = yobit.getActiveOrders();
        System.out.println();
    }

    public void testCancelAllOrders(){
        Yobit yobit = new Yobit();
        yobit.cancelAllOrders();
        System.out.println();
    }

    public void testLoadOrders(){
        Yobit yobit = new Yobit();
        List<Order> orders = yobit.loadOrders("ETH/USD", OrderType.BID, 5);
        System.out.println();
    }

    public void testMarketOrder(){
        Yobit yobit = new Yobit();
        String res = yobit.createOrder("buy", "market", "BTC/USD", BigDecimal.valueOf(0.001), null);
        System.out.println();
    }

}
