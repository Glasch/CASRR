package exchanges;

import junit.framework.TestCase;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class HitbtcUTest extends TestCase {

    public void testGetBalance() {
        Hitbtc hitbtc = new Hitbtc();
        BigDecimal res = hitbtc.getBalance("USD");
        System.out.println(res);
    }

    public void testCreateOrder() {
        Hitbtc hitbtc = new Hitbtc();

        String res = hitbtc.createOrder(
                "buy",
                "limit", "ethusd",
                BigDecimal.valueOf(0.01),
                BigDecimal.valueOf(100));

        System.out.println(res);
    }

    public void testCancelOrder() throws InterruptedException {
        Hitbtc hitbtc = new Hitbtc();

        hitbtc.cancelAllOrders("ethusd");

        TimeUnit.SECONDS.sleep(10);

        String orderId = hitbtc.createOrder(
                "buy",
                "limit", "ethusd",
                BigDecimal.valueOf(0.01),
                BigDecimal.valueOf(100));

        TimeUnit.SECONDS.sleep(60);

        hitbtc.cancelOrder(orderId);

        System.out.println("cancelled successfully");
    }


}
