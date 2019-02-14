import exchanges.Exchange;
import model.*;
import services.ConnectionManager;
import services.DBManager;
import services.Reporter;
import services.Updater;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.Map;
import java.util.Set;

/**
 * Copyright (c) Anton on 17.11.2018.
 */
public class Main {
    private static String url = "jdbc:postgresql://185.246.153.215:5432/cas";
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
        Map <Integer, String> idToPair = dbManager.getIdToPair(updater, connection);
        System.out.println(timestamps.size());
        Map<String,BigDecimal> before = reporter.calcGlobalAccount(updater);
        DbAnalyzeReport dbAnalyzeReport = new DbAnalyzeReport();
        dbAnalyzeReport.initPossibleRoutes(new Router(updater));
        for (Timestamp timestamp : timestamps) {
            System.out.println("-----------------------------NEW TIMESTAMP---------------------------------");
//         dbManager.saveStaticData();
//          updater.update();
//          dbManager.saveOrders();
            dbManager.getMarketsFromDB(timestamp, idToExchange, idToPair, connection);
            Router router = new Router(updater);
            Trader trader = new Trader();
            reporter.showAcceptedRoutes(trader,true);
            for (Route route : router.getResultingRoutes()) {
                trader.makeDeal(route);
            }
//            reporter.showAcceptedRoutes(trader,true);
            Map<String,BigDecimal> after = reporter.calcGlobalAccount(updater);
            for (String currency : after.keySet()) {
                after.merge(currency,before.get(currency),BigDecimal::subtract);
            }
            reporter.showExchangeAccounts(true);
            dbAnalyzeReport.updatePossibleRoutesData(router);
            reporter.showDbAnalyzeReport(dbAnalyzeReport,false);
            System.out.println("--------------GLOBAL---------------");
            reporter.printGlobalAccount(after);
            System.out.println(++count);
            for (Exchange exchange : updater.getExchanges()) {
                exchange.getMarket().clear();
            }
        }
        connection.close();
    }
}

