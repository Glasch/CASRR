package model;


import junit.framework.TestCase;
import org.apache.http.auth.AuthenticationException;
import org.json.JSONArray;
import services.ConnectionManager;

import java.io.IOException;


/**
 * Copyright (c) Anton on 10.03.2019.
 */
public class ExchangeAccountUTest extends TestCase {

    public void testGetCurrencyBalance() throws IOException, AuthenticationException {
        JSONArray jsonArray = ConnectionManager.getHitBtcBalanceJsonArray("https://api.hitbtc.com/api/2/trading/balance",
                "3f6ffb7a7095445a498c52f66d5192b2",
                "110e4b1bd2426cf05039c6db53c265ab" );
        System.out.println(jsonArray);
    }
}
