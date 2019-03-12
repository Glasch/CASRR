package model;

import junit.framework.TestCase;
import org.apache.http.auth.AuthenticationException;
import org.json.JSONArray;
import org.knowm.xchange.huobi.Huobi;
import services.ConnectionManager;

public class HoubiGetBalanceUTest extends TestCase {


    public void testGetHuobiBalance() throws AuthenticationException {
        JSONArray jsonArray = ConnectionManager.getHitBtcBalanceJsonArray("",
                "3f6ffb7a7095445a498c52f66d5192b2",
                "110e4b1bd2426cf05039c6db53c265ab" );
        System.out.println(jsonArray);

    }
}
