package model;

import exchanges.Yobit;
import junit.framework.TestCase;
import org.apache.commons.codec.DecoderException;
import org.json.JSONObject;
import services.ConnectionManager;

import java.time.Instant;

public class CreateOrderYobitUTest extends TestCase {

    public void testCreateYobitOrder() throws DecoderException, InterruptedException {
        long millis = Instant.now().getEpochSecond();

        JSONObject json = ConnectionManager.readJSONFromSignedPostRequest(
                Yobit.getApiPrivateUrl(),
                "method=Trade&pair=btc_usd&type=buy&rate=3500amount=0.001&nonce=" + millis, // &rate=3500
                Yobit.getKey(),
                Yobit.getSecretKey());
        System.out.println(json);

    }

    public void testGetActiveOrders() throws DecoderException {
        long millis = Instant.now().getEpochSecond();
        JSONObject json = ConnectionManager.readJSONFromSignedPostRequest(
                Yobit.getApiPrivateUrl(),
                "method=ActiveOrders&pair=btc_usd&nonce=" + millis, // &pair=btc_usd
                Yobit.getKey(),
                Yobit.getSecretKey());
        System.out.println(json);
    }

    public void testGetInfoYobitOrder() throws DecoderException {
        long millis = Instant.now().getEpochSecond();
        JSONObject json = ConnectionManager.readJSONFromSignedPostRequest(
                Yobit.getApiPrivateUrl(),
                "method=OrderInfo&order_id=1500011733874622&nonce=" + millis,
                Yobit.getKey(),
                Yobit.getSecretKey());
        System.out.println(json);
    }

    public void testCancelYobitOrder() throws DecoderException {
        long millis = Instant.now().getEpochSecond();
        JSONObject json = ConnectionManager.readJSONFromSignedPostRequest(
                Yobit.getApiPrivateUrl(),
                "method=CancelOrder&order_id=1500011733874622&nonce=" + millis,
                Yobit.getKey(),
                Yobit.getSecretKey());
        System.out.println(json);
    }

    public void testYobitTradeHistory() throws DecoderException {
        long millis = Instant.now().getEpochSecond();
        JSONObject json = ConnectionManager.readJSONFromSignedPostRequest(
                Yobit.getApiPrivateUrl(),
                "method=TradeHistory&pair=btc_usd&nonce=" + millis,
                Yobit.getKey(),
                Yobit.getSecretKey());
        System.out.println(json);
    }


}

