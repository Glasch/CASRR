import exchanges.Exchange;
import model.*;
import services.ConnectionManager;
import services.DBManager;
import services.Reporter;
import services.Updater;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
        DBManager dbManager = new DBManager(updater);
        Reporter reporter = new Reporter();
        for (Exchange exchange : updater.getExchanges()) {
            exchange.setExchangeAccount(new ExchangeAccount(exchange));
        }
        System.out.println("Создаем Коннекшен!");
        Connection connection = ConnectionManager.getDBconnection(url, login, password);
        System.out.println("OK!");
        Set <Timestamp> timestamps = dbManager.getTimestamps(connection);
        Map <Integer, Exchange> idToExchange = dbManager.getIdToExchange(updater, connection);
        Map <Exchange, Integer> exchangeToId = reverseMap(idToExchange);

        Map <Integer, String> idToPair = dbManager.getIdToPair(updater, connection);
        Map <String, Integer> pairToId = reverseMap(idToPair);

        System.out.println(timestamps.size());
        Map <String, BigDecimal> before = reporter.calcGlobalAccount(updater);
        DbAnalyzeReport dbAnalyzeReport = new DbAnalyzeReport();
        dbAnalyzeReport.initPossibleRoutes(new Router(updater));
        dbManager.saveStaticData();
        for (Timestamp timestamp : timestamps) {
            System.out.println(timestamp);
            System.out.println("-----------------------------NEW TIMESTAMP---------------------------------");
//          updater.update();
//          dbManager.saveOrders();
            dbManager.getMarketsFromDB(timestamp, idToExchange, idToPair, connection);
            Router router = new Router(updater);
            Trader trader = new Trader();
            reporter.showAcceptedRoutes(trader, true);
            for (Route route : router.getResultingRoutes()) {
                trader.makeDeal(route);
            }
            for (Route acceptedRoute : trader.getAcceptedRoutes()) {
                dbManager.saveRoute(connection, acceptedRoute, exchangeToId, pairToId);
            }
//            reporter.showAcceptedRoutes(trader,true);
            Map <String, BigDecimal> after = reporter.calcGlobalAccount(updater);
            for (String currency : after.keySet()) {
                after.merge(currency, before.get(currency), BigDecimal::subtract);
            }
            reporter.showExchangeAccounts(true);
            dbAnalyzeReport.updatePossibleRoutesData(router);
            reporter.showDbAnalyzeReport(dbAnalyzeReport, false);
            System.out.println("--------------GLOBAL---------------");
            reporter.printGlobalAccount(after);
            System.out.println(++count);
            for (Exchange exchange : updater.getExchanges()) {
                exchange.getMarket().clear();
            }
        }

        connection.close();
    }

  public static Map reverseMap(Map map) {
        Map resMap = new HashMap <>();
        for (Object newValue : map.keySet()) {
            Object newKey = map.get(newValue);
            resMap.put(newKey, newValue);
        }
        return resMap;
    }

   public static boolean isTimestampInRange(Timestamp current, String dateToCompareWith, boolean isFrom) throws ParseException {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date date = dateFormat.parse(dateToCompareWith);
        long time = date.getTime();
        Timestamp timestampToCompareWith = new Timestamp(time);
        if (isFrom) return current.after(timestampToCompareWith);
        else return current.before(timestampToCompareWith);
    }

  public   static boolean isTimestampInRange(Timestamp current, String from, String to) throws ParseException {
        return (isTimestampInRange(current, from, true) && (isTimestampInRange(current, to, false)));
    }


}

