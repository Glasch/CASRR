import exchanges.Binance;
import exchanges.Exchange;
import junit.framework.TestCase;

import java.util.HashMap;
import java.util.Map;

/**
 * Copyright (c) Anton on 24.02.2019.
 */
public class MainTest extends TestCase {

    public void testMapReverse(){
        Map<Exchange,Integer> from = new HashMap <>();
        from.put(new Binance(), 1);
        Map<Integer,Exchange> to = Main.reverseMap(from);
        System.out.println(from);
        System.out.println(to);
    }
}
