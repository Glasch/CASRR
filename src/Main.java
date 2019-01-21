import exchanges.Exchange;
import model.ExchangeAccount;
import model.Route;
import model.Router;
import services.DBManager;
import model.Trader;
import services.Updater;

/**
 * Copyright (c) Anton on 17.11.2018.
 */
public class Main {

    public static void main(String[] args) throws Exception {
        Updater updater = Updater.getInstance();
        for (Exchange exchange : updater.getExchanges()) {
            exchange.setExchangeAccount(new ExchangeAccount(exchange));
        }
        DBManager dbManager = new DBManager(updater);
      //  dbManager.saveStaticData();
        while (true) {
            updater.update();
//            dbManager.saveOrders();
            Router router = new Router(updater);
            Trader trader = new Trader(router);
            for (Route route : router.getResultingRoutes()) {
                trader.makeDeal(route);
                System.out.println();
            }
            trader.calcValueInDollars();
            System.out.println();
        }
    }

}

