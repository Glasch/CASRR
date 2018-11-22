package services;

import exchanges.*;
import model.Router;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Copyright (c) Anton on 17.11.2018.
 */

public class Updater {
 private static Updater instance;
 private ArrayList<Exchange> exchanges = new ArrayList <>();
 private ExecutorService executorService = Executors.newCachedThreadPool();
 private List<Future> futures = new ArrayList<>();

  private Updater() {
        exchanges.add(new Binance());
        exchanges.add(new Bitfinex());
        exchanges.add(new Bittrex());
        exchanges.add(new Exmo());
        exchanges.add(new LiveCoin());
        exchanges.add(new Poloniex());
        exchanges.add(new Yobit());
        exchanges.add(new Kucoin());
        exchanges.add(new Hitbtc());
        exchanges.add(new Huobi());
    }

    public static Updater getInstance() {
        if(instance == null){
            instance = new Updater();
        }
        return instance;
    }

    public void update() throws InterruptedException {
        UsdConverter converter = UsdConverter.getInstance();
        converter.loadData();

        for (Exchange exchange : exchanges) {
            futures.add(executorService.submit(exchange));
        }

        waitAllFutures(futures);

        for (Iterator<Exchange> iterator = exchanges.iterator(); iterator.hasNext(); ) {
            Exchange exchange = iterator.next();
            if(!exchange.isMarketValid()) {
                System.out.println("Exchange is not valid and was removed: " + exchange + ", error: " + exchange.getLastError());
                iterator.remove();
            }
            else{
                System.out.println(exchange + " OK");
            }
        }

        futures.clear();
    }

    private void waitAllFutures(List<Future> futures) throws InterruptedException {
        for (Future future : futures) {
            try {
                future.get();
            } catch (ExecutionException e) {
                throw new IllegalStateException("Future problem", e);
            }
        }
    }

    public ArrayList <Exchange> getExchanges() {
        return exchanges;
    }
}
