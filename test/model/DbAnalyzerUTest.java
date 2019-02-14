package model;

import exchanges.Exchange;
import junit.framework.TestCase;
import services.Reporter;
import services.Updater;

/**
 * Copyright (c) Anton on 12.02.2019.
 */
public class DbAnalyzerUTest extends TestCase {

    public void testDbAnalyzer() throws InterruptedException {
        Reporter reporter = new Reporter();
        Updater updater = Updater.getInstance();
        for (Exchange exchange : updater.getExchanges()) {
            exchange.setExchangeAccount(new ExchangeAccount(exchange));
        }

        DbAnalyzeReport dbAnalyzeReport = new DbAnalyzeReport();
        dbAnalyzeReport.initPossibleRoutes(new Router(updater));
        Trader trader = new Trader();


//        while (true) {
            updater.update();
            Router router = new Router(updater);
            for (Route route : router.getResultingRoutes()) {
                trader.makeDeal(route);
            }
            dbAnalyzeReport.updatePossibleRoutesData(router);
            reporter.showDbAnalyzeReport(dbAnalyzeReport,true);
            Thread.sleep(1000);
            System.out.println("---------------------------------------------");
 //       }
    }
}
