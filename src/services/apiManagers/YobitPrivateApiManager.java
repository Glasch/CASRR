package services.apiManagers;

import exchanges.Yobit;
import org.apache.commons.codec.DecoderException;
import org.json.JSONException;
import org.json.JSONObject;
import services.ConnectionManager;

import java.math.BigDecimal;
import java.time.Instant;

public class YobitPrivateApiManager {

    public static BigDecimal getCurrencyBalance(String currency) throws DecoderException {
        JSONObject jsonObject = ConnectionManager.readJSONFromSignedPostRequest(Yobit.getApiPrivateUrl(),
                "method=getInfo&nonce=" + Instant.now().getEpochSecond(), Yobit.getKey(), Yobit.getSecretKey());
        BigDecimal balance = null;
        try {
            balance = jsonObject.getJSONObject("return").getJSONObject("funds").getBigDecimal(currency.toLowerCase());
        } catch (JSONException e) {
            return BigDecimal.ZERO;
        }
        return balance;
    }

    public static JSONObject createYobitOrder(String pair, DealType dealType, BigDecimal rate, BigDecimal amount) throws DecoderException, InterruptedException {
        long millis = Instant.now().getEpochSecond();
        JSONObject json = ConnectionManager.readJSONFromSignedPostRequest(
                Yobit.getApiPrivateUrl(),
                "method=Trade&pair=" + pair + "&type="+ dealType.toString() + "&rate=" + rate + "amount=" + amount + "&nonce=" + millis, // &rate=3500
                Yobit.getKey(),
                Yobit.getSecretKey());
        return json;
    }

    public static Object checkNullParam(Object object){
        if (object==null){
            return  "";
        }
        else return object;
    }

    public enum DealType{
        BUY, SELL;

        @Override
        public String toString() {
            return this == BUY ? "buy" : "sell";
        }
    }

}
