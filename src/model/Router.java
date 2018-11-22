package model;


import exchanges.Exchange;
import services.Updater;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Copyright (c) Anton on 17.11.2018.
 */
public class Router {
    ArrayList <Exchange> exchanges;
    Set <String> allPairs = new HashSet <>();
    ArrayList <Route> routes = new ArrayList <>();

    public Router(Updater updater) throws InterruptedException, IOException {
        exchanges = updater.getExchanges();
        for (Exchange exchange : exchanges) {
            allPairs.addAll(exchange.getPairs());
        }

        this.routes = findRoutes(exchanges, allPairs);
    }

    private ArrayList <Route> findRoutes(ArrayList <Exchange> exchanges, Set <String> allPairs) throws IOException {
        ArrayList <Route> routes = new ArrayList <>();
        for (Exchange exchangeFrom : exchanges) {
            for (Exchange exchangeTo : exchanges) {
                for (String pairName : allPairs) {
                    if (exchangeFrom.getMarket().containsKey(pairName) &&
                            exchangeTo.getMarket().containsKey(pairName)) {
                        routes.add(new Route(pairName, exchangeFrom, exchangeTo));
                    }
                }
            }
        }
        return routes;
    }

    public Set <String> getAllPairs() {
        return allPairs;
    }

    public ArrayList <Exchange> getExchanges() {
        return exchanges;
    }

    public ArrayList <Route> getRoutes() {
        return routes;
    }
}
