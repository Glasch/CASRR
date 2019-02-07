import exchanges.Exchange;
import model.ExchangeAccount;
import model.Route;
import model.Router;
import model.Trader;
import services.Reporter;
import services.Updater;

import java.util.concurrent.TimeUnit;

/**
 * Copyright (c) Anton on 17.11.2018.
 */
public class Main {
    public static void main(String[] args) throws Exception {
        Reporter reporter = new Reporter();
        Updater updater = Updater.getInstance();
        for (Exchange exchange : updater.getExchanges()) {
            exchange.setExchangeAccount(new ExchangeAccount(exchange));
        }
//         DBManager dbManager = new DBManager(updater);
//         dbManager.saveStaticData();
        while (true) {
            updater.update();
//          dbManager.saveOrders();
            Router router = new Router(updater);
            Trader trader = new Trader();
            for (Route route : router.getResultingRoutes()) {
                trader.makeDeal(route);
            }

            TimeUnit.MINUTES.sleep(5);
        }
    }

}

