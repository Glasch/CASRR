import exchanges.Exchange;
import model.Route;
import model.Router;
import model.Trader;
import org.apache.http.ParseException;
import services.Updater;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Copyright (c) Anton on 17.11.2018.
 */
public class Main {
    private static String url = "jdbc:postgresql://localhost:5432/cas";
    private static String login = "postgres";
    private static String password = "tMXVuD8JrJ8egE";
    private static int count = 0;

    public static void main(String[] args) throws Exception {

        Updater updater = Updater.getInstance();
        while (true) {
            updater.update();
            Router router = new Router(updater);
            for (Route resultingRoute : router.getResultingRoutes()) {
                resultingRoute.calcRouteSpread(resultingRoute.getDeals());
            }
            Trader trader = new Trader();
            for (Route route : router.getResultingRoutes()) {
//                System.out.println(route.getDeals().size());
                route.filterDeals(route.getExchangeFrom().getTakerTax().add(route.getExchangeTo().getTakerTax()));
//                System.out.println(route.getDeals().size());

                trader.makeRealMoneyDeal(route);


            }
            for (Exchange exchange : updater.getExchanges()) {
                exchange.getMarket().clear();
            }

            Thread.sleep(5000);
        }
    }

    public static Map reverseMap(Map map) {
        Map resMap = new HashMap<>();
        for (Object newValue : map.keySet()) {
            Object newKey = map.get(newValue);
            resMap.put(newKey, newValue);
        }
        return resMap;
    }

    public static boolean isTimestampInRange(Timestamp current, String dateToCompareWith, boolean isFrom) throws
            ParseException, java.text.ParseException {
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        Date date = dateFormat.parse(dateToCompareWith);
        long time = date.getTime();
        Timestamp timestampToCompareWith = new Timestamp(time);
        //if (isFrom)

        return current.after(timestampToCompareWith);
        // else return current.before(timestampToCompareWith);
    }

    public static boolean isTimestampInRange(Timestamp current, String from, String to) throws ParseException, java.text.ParseException {
        return (isTimestampInRange(current, from, true) && (isTimestampInRange(current, to,
                false)));
    }
}


