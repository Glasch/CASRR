package model;

import exchanges.Binance;
import exchanges.Bittrex;
import exchanges.Exchange;
import exchanges.Exmo;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;
import services.Updater;
import services.UsdConverter;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * Copyright (c) Anton on 28.01.2019.
 */
public class TraderUTest extends TestCase {

    public void testTrader() {
        Order bid = new Order(null,BigDecimal.valueOf(0.02),BigDecimal.valueOf(100));
        Order ask = new Order(null,BigDecimal.valueOf(0.01996),BigDecimal.valueOf(100));

        Route route = new Route("ETH/BTC");
        Updater updater = Updater.getInstance();
        for (Exchange exchange : updater.getExchanges()) {
            exchange.setExchangeAccount(new ExchangeAccount(exchange));
            if (exchange instanceof Binance){
                bid.setExchange(exchange);
                route.setExchangeFrom(exchange);
            }
            if (exchange instanceof Bittrex){
                ask.setExchange(exchange);
                route.setExchangeTo(exchange);
            }
        }

        Deal deal = new Deal(route,bid,ask);
        route.addDeal(deal);
        Trader trader = new Trader();

        BigDecimal btcTot = BigDecimal.ZERO;
        BigDecimal ethTot = BigDecimal.ZERO;

        for (Exchange exchange : updater.getExchanges()) {
            btcTot = btcTot.add(exchange.getExchangeAccount().getBalances().get("BTC"));
            ethTot = ethTot.add(exchange.getExchangeAccount().getBalances().get("ETH"));
        }

        BigDecimal totInSBofore = BigDecimal.ZERO;

        totInSBofore = totInSBofore
                .add(btcTot.multiply(BigDecimal.valueOf(3555)))
                .add(ethTot.multiply(BigDecimal.valueOf(30)));

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
                .add(btcTot.multiply(BigDecimal.valueOf(3555)))
                .add(ethTot.multiply(BigDecimal.valueOf(30)));

        System.out.println("BTC TOT:" + btcTot + " " + btcTot.multiply(BigDecimal.valueOf(3555)) + " S");
        System.out.println("ETH TOT:" + ethTot + " " + ethTot.multiply(BigDecimal.valueOf(30)) + " S");
        System.out.println( "Before: " + totInSBofore + " After: " + totInS);

        Assert.assertTrue(totInSBofore.compareTo(totInS) == 0);

    }
}
