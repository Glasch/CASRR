package model;

import exchanges.Hitbtc;
import junit.framework.TestCase;
import org.apache.http.auth.AuthenticationException;

public class HitBtcGetCurrencyBalanceUTest extends TestCase {

    public void testGetHitBtcBalance() throws AuthenticationException {
        System.out.println(Hitbtc.getCurrencyBalance("USD"));
    }
}
