package model;

import exchanges.Yobit;
import junit.framework.TestCase;
import org.apache.commons.codec.DecoderException;
import org.apache.http.auth.AuthenticationException;
import org.json.JSONObject;
import services.ConnectionManager;

import java.math.BigDecimal;

public class YobitGetBalanceUTest extends TestCase {


    public void testGetHuobiBalance() throws DecoderException {
        System.out.println(Yobit.getBalance("USD"));
    }
}
