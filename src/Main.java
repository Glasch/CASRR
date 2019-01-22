import exchanges.Exchange;
import model.ExchangeAccount;
import model.Route;
import model.Router;
import services.DBManager;
import model.Trader;
import services.Updater;
import services.UsdConverter;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

/**
 * Copyright (c) Anton on 17.11.2018.
 */
public class Main {

    public static void main(String[] args) throws Exception {
        Updater updater = Updater.getInstance();
        UsdConverter usdConverter = UsdConverter.getInstance();
        for (Exchange exchange : updater.getExchanges()) {
            exchange.setExchangeAccount(new ExchangeAccount(exchange));
        }
        DBManager dbManager = new DBManager(updater);
      //  dbManager.saveStaticData();
        while (true) {
            BigDecimal btcTot = BigDecimal.ZERO;
            BigDecimal ethTot = BigDecimal.ZERO;
            BigDecimal ltcTot = BigDecimal.ZERO;
            BigDecimal xrpTot = BigDecimal.ZERO;
            BigDecimal zecTot = BigDecimal.ZERO;
            BigDecimal totInS = BigDecimal.ZERO;
            updater.update();
//            dbManager.saveOrders();
            Router router = new Router(updater);
            Trader trader = new Trader(router);
            for (Exchange exchange : updater.getExchanges()) {
                System.out.println("BEFORE");

                btcTot = btcTot.add(exchange.getExchangeAccount().getBalances().get("BTC"));
                ethTot = ethTot.add(exchange.getExchangeAccount().getBalances().get("ETH"));
                ltcTot = ltcTot.add(exchange.getExchangeAccount().getBalances().get("LTC"));
                xrpTot = xrpTot.add(exchange.getExchangeAccount().getBalances().get("XRP"));
                zecTot = zecTot.add(exchange.getExchangeAccount().getBalances().get("ZEC"));

            }

            BigDecimal totInSBofore = BigDecimal.ZERO;

                    totInSBofore = totInSBofore
                    .add(btcTot.multiply(BigDecimal.valueOf(3555)))
                    .add(ethTot.multiply(BigDecimal.valueOf(30)))
                    .add(ltcTot.multiply(BigDecimal.valueOf(115)))
                    .add(xrpTot.multiply(BigDecimal.valueOf(0.3)))
                    .add(zecTot.multiply(BigDecimal.valueOf(51)));



            for (Route route : router.getResultingRoutes()) {
                trader.makeDeal(route);
                System.out.println("Accepted: " + route );
            }

             btcTot = BigDecimal.ZERO;
             ethTot = BigDecimal.ZERO;
             ltcTot = BigDecimal.ZERO;
             xrpTot = BigDecimal.ZERO;
             zecTot = BigDecimal.ZERO;


            for (Exchange exchange : updater.getExchanges()) {

                System.out.println("AFTER");

                btcTot = btcTot.add(exchange.getExchangeAccount().getBalances().get("BTC"));
                ethTot = ethTot.add(exchange.getExchangeAccount().getBalances().get("ETH"));
                ltcTot = ltcTot.add(exchange.getExchangeAccount().getBalances().get("LTC"));
                xrpTot = xrpTot.add(exchange.getExchangeAccount().getBalances().get("XRP"));
                zecTot = zecTot.add(exchange.getExchangeAccount().getBalances().get("ZEC"));

                System.out.println(exchange);
                System.out.println("BTC: " + exchange.getExchangeAccount().getBalances().get("BTC"));
                System.out.println("ETH: " + exchange.getExchangeAccount().getBalances().get("ETH"));
                System.out.println("LTC: " + exchange.getExchangeAccount().getBalances().get("LTC"));
                System.out.println("XRP: " + exchange.getExchangeAccount().getBalances().get("XRP"));
                System.out.println("ZEC: " + exchange.getExchangeAccount().getBalances().get("ZEC"));

                System.out.println("-------------------------------------------------------------------");
            }

            totInS = totInS
                    .add(btcTot.multiply(BigDecimal.valueOf(3555)))
                    .add(ethTot.multiply(BigDecimal.valueOf(30)))
                    .add(ltcTot.multiply(BigDecimal.valueOf(115)))
                    .add(xrpTot.multiply(BigDecimal.valueOf(0.3)))
                    .add(zecTot.multiply(BigDecimal.valueOf(51)));

            System.out.println("BTC TOT:" + btcTot + " " + btcTot.multiply(BigDecimal.valueOf(3555)) + " S");
            System.out.println("ETH TOT:" + ethTot + " " + ethTot.multiply(BigDecimal.valueOf(30)) + " S");
            System.out.println("LTC TOT:" + ltcTot + " " + ltcTot.multiply(BigDecimal.valueOf(115)) + " S");
            System.out.println("XRP TOT:" + xrpTot + " " + xrpTot.multiply(BigDecimal.valueOf(0.3)) + " S");
            System.out.println("ZEC TOT:" + zecTot + " " + zecTot.multiply(BigDecimal.valueOf(51)) + " S");
            System.out.println( "Before: " + totInSBofore + " After: " + totInS);
            TimeUnit.MINUTES.sleep(5);
        }

    }

}

