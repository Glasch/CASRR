package services.apiManagers;

import exchanges.Yobit;
import junit.framework.TestCase;
import org.apache.commons.codec.DecoderException;
import org.json.JSONObject;
import services.ConnectionManager;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Copyright (c) Anton on 20.03.2019.
 */
public class YobitPrivateApiManagerUTest extends TestCase {

    public void testGetCurrencyBalance() throws DecoderException {
        System.out.println(YobitPrivateApiManager.getCurrencyBalance("USD"));
        assertEquals(BigDecimal.ZERO,YobitPrivateApiManager.getCurrencyBalance("eth"));
    }

    public void testCreateYobitOrder() throws DecoderException, InterruptedException {
        System.out.println(YobitPrivateApiManager.createYobitOrder("btc_usd", YobitPrivateApiManager.DealType.BUY, BigDecimal.valueOf(3500), BigDecimal.valueOf(0.001) )); // TODO: 20.03.2019 {"success":0,"error":"Total transaction amount is less than minimal total: 0.10000000"} what is that? Yesterday it works.
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

    public void testCheckNullParam() {
        assertEquals("", YobitPrivateApiManager.checkNullParam(null));
        assertEquals(100, YobitPrivateApiManager.checkNullParam(100));
    }
}
