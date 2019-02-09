import exchanges.Exchange;
import model.ExchangeAccount;
import model.Route;
import model.Router;
import model.Trader;
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
        Connection connection = ConnectionManager.getDBconnection(url, login, password);
        Set <Timestamp> timestamps = dbManager.getTimestamps(connection);
        connection.close();
        System.out.println(timestamps.size());
        Map<String,BigDecimal> before = reporter.calcGlobalAccount(updater);
        for (Timestamp timestamp : timestamps) {
//         dbManager.saveStaticData();
//          updater.update();
//          dbManager.saveOrders();
            dbManager.getMarketsFromDB(updater, timestamp);
            Router router = new Router(updater);
            Trader trader = new Trader();
            for (Route route : router.getResultingRoutes()) {
                trader.makeDeal(route);
            }
            for (Exchange exchange : updater.getExchanges()) {
                exchange.getMarket().clear();
            }
            Map<String,BigDecimal> after = reporter.calcGlobalAccount(updater);
            for (String currency : after.keySet()) {
                after.merge(currency,before.get(currency),BigDecimal::subtract);
            }
            reporter.showExchangeAccounts(true);
            reporter.printGlobalAccount(after);
            System.out.println(++count);
        }
    }
}

