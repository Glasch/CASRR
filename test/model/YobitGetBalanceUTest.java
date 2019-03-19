package model;

import junit.framework.TestCase;
import org.apache.commons.codec.DecoderException;
import services.apiManagers.YobitPrivateApiManager;

public class YobitGetBalanceUTest extends TestCase {


    public void testGetYobitBalance() throws DecoderException {

        System.out.println(YobitPrivateApiManager.getCurrencyBalance("USD")); // It works, but provider blocks
    }
}
