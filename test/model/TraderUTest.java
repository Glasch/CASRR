package model;

import exchanges.Binance;
import exchanges.Exchange;
import exchanges.Poloniex;
import junit.framework.TestCase;
import org.junit.Assert;
import services.Updater;

import java.math.BigDecimal;

/**
 * Copyright (c) Anton on 28.01.2019.
 */
public class TraderUTest extends TestCase {

    public void testTrader() {
        Order bid = new Order(null,BigDecimal.valueOf(0.03071972),BigDecimal.valueOf(100));
        Order ask = new Order(null,BigDecimal.valueOf(0.03065828),BigDecimal.valueOf(100));

        Order bid1 = new Order(null,BigDecimal.valueOf(0.03071972),BigDecimal.valueOf(100));
        Order ask1 = new Order(null,BigDecimal.valueOf(0.03065828),BigDecimal.valueOf(100));

        Route route = new Route("ETH/BTC");
        Updater updater = Updater.getInstance();
        for (Exchange exchange : updater.getExchanges()) {
            exchange.setExchangeAccount(new ExchangeAccount(exchange));
            if (exchange instanceof Binance){
                bid.setExchange(exchange);
                bid1.setExchange(exchange);
                route.setExchangeFrom(exchange);
            }
            if (exchange instanceof Poloniex){
                ask.setExchange(exchange);
                ask1.setExchange(exchange);
                route.setExchangeTo(exchange);
            }
        }


        Deal deal = new Deal(route,bid,ask);
        Deal deal1 = new Deal(route,bid1,ask1);

        route.addDeal(deal);
        route.addDeal(deal1);

        Trader trader = new Trader();

        BigDecimal btcTot = BigDecimal.ZERO;
        BigDecimal ethTot = BigDecimal.ZERO;

        for (Exchange exchange : updater.getExchanges()) {
            btcTot = btcTot.add(exchange.getExchangeAccount().getBalances().get("BTC"));
            ethTot = ethTot.add(exchange.getExchangeAccount().getBalances().get("ETH"));
        }

        BigDecimal totInSBefore = BigDecimal.ZERO;

        totInSBefore = totInSBefore
                .add(btcTot.multiply(BigDecimal.valueOf(3418)))
                .add(ethTot.multiply(BigDecimal.valueOf(105)));

        btcTot = BigDecimal.ZERO;
        ethTot = BigDecimal.ZERO;

        trader.makeDeal(route);
        System.out.println(route);
        System.out.println(route.getSortedEVDeals());
        for (Deal currentDeal : route.getSortedEVDeals()) {
            System.out.println("Tax From: " + currentDeal.getTaxFrom());
            System.out.println("Tax To: " + currentDeal.getTaxTo());
        }


        for (Exchange exchange : updater.getExchanges()) {

            System.out.println("AFTER");

            btcTot = btcTot.add(exchange.getExchangeAccount().getBalances().get("BTC"));
            ethTot = ethTot.add(exchange.getExchangeAccount().getBalances().get("ETH"));

            System.out.println(exchange);
            System.out.println("BTC: " + exchange.getExchangeAccount().getBalances().get("BTC"));
            System.out.println("ETH: " + exchange.getExchangeAccount().getBalances().get("ETH"));

            System.out.println("-------------------------------------------------------------------");
        }

        BigDecimal totInS = BigDecimal.ZERO;
        totInS = totInS
                .add(btcTot.multiply(BigDecimal.valueOf(3418)))
                .add(ethTot.multiply(BigDecimal.valueOf(105)));

        System.out.println("BTC TOT:" + btcTot + " " + btcTot.multiply(BigDecimal.valueOf(3418)) + " S");
        System.out.println("ETH TOT:" + ethTot + " " + ethTot.multiply(BigDecimal.valueOf(105)) + " S");
        System.out.println( "Before: " + totInSBefore + " After: " + totInS);

        System.out.println(totInS.subtract(totInSBefore).abs().divide(totInSBefore,10,BigDecimal.ROUND_HALF_UP));
        Assert.assertTrue( totInS.subtract(totInSBefore).abs().divide(totInSBefore,10,BigDecimal.ROUND_HALF_UP)
                .compareTo(BigDecimal.valueOf(0.001)) < 1);

    }
}
