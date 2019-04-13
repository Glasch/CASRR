import exchanges.Binance;
import exchanges.Exchange;
import junit.framework.TestCase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.junit.Test;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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

    public void testCompareTimestamps() throws ParseException {

        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        Date firstDate = dateFormat.parse("23/09/2007");
        long firstTime = firstDate.getTime();
        Timestamp first = new Timestamp(firstTime);
        System.out.println(first);

        Date secondDate = dateFormat.parse("24/09/2007");
        long secondTime = secondDate.getTime();
        Timestamp second = new Timestamp(secondTime);
        System.out.println(second);

        Date thirdDate = dateFormat.parse("25/09/2007");
        long thirdTime = thirdDate.getTime();
        Timestamp third = new Timestamp(thirdTime);
        System.out.println(third);

        assertEquals(true,  Main.isTimestampInRange(second, "23/09/2007",true));
        assertEquals(false,  Main.isTimestampInRange(second, "25/09/2007",true));
        assertEquals(false,  Main.isTimestampInRange(second, "23/09/2007",false));
        assertEquals(true,  Main.isTimestampInRange(second, "25/09/2007",false));
        assertEquals(true, Main.isTimestampInRange(second,"23/09/2007", "25/09/2007") );


    }

    public  void testMyTest() {
        HttpRequestBase http = null;
        http = new HttpPost();
        System.out.println(http.getClass());

    }
}
