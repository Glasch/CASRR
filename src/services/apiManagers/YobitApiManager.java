package services.apiManagers;

import exchanges.Yobit;
import org.apache.commons.codec.DecoderException;
import org.json.JSONException;
import org.json.JSONObject;
import services.ConnectionManager;

import java.math.BigDecimal;
import java.time.Instant;

public class YobitApiManager {
    //public


    //private
    public static BigDecimal GetCurrencyBalance(String currency) throws DecoderException {
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
}
