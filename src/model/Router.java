package model;


import exchanges.Exchange;
import services.Updater;

import java.math.BigDecimal;
import java.util.*;

/**
 * Copyright (c) Anton on 17.11.2018.
 */
public class Router {
    private ArrayList <Exchange> exchanges;
    private ArrayList <Route> masterRoutes;
    private ArrayList <Route> resultingRoutes;

    public Router(Updater updater) {
        exchanges = updater.getExchanges();

        Set <String> allPairs = new HashSet <>();
        for (Exchange exchange : exchanges) {
            allPairs.addAll(exchange.getPairs());
        }

        masterRoutes = findRoutes(exchanges, allPairs);
        for (Route route : masterRoutes) {
            route.filterDeals(BigDecimal.ZERO);
            route.calcRouteValueInDollars();
            route.filterZeroAmountDeals();
        }

        resultingRoutes = new ArrayList <>();

        for (Route masterRoute : masterRoutes) {
            HashMap <ExchangePair, Route> resRoutes = new HashMap <>();
            for (Deal deal : masterRoute.getSortedEVDeals()) {
                ExchangePair exchangePair = new ExchangePair(deal.getBid().getExchange(), deal.getAsk().getExchange());
                Route route = resRoutes.get(exchangePair);
                if (route == null) {
                    route = new Route(masterRoute.getPairName());
                    route.setExchangeFrom(exchangePair.from);
                    route.setExchangeTo(exchangePair.to);
                    resRoutes.put(exchangePair, route);
                }
                route.addDeal(deal);
            }
            resultingRoutes.addAll(resRoutes.values());
        }


//        for (Route resultingRoute : resultingRoutes) {
//            if (resultingRoute.getPairName().contains("USD") && !resultingRoute.getPairName().contains("USDT")) {
//                for (String pairName : resultingRoute.getExchangeFrom().getMarket().keySet()) {
//                    if (pairName.contains("USD") && !pairName.contains("USDT")) {
//                        Route route = new Route(pairName);
//                        route.setExchangeFrom(resultingRoute.getExchangeTo());
//                        route.setExchangeTo(resultingRoute.getExchangeFrom());
//                        route.addDealsForExchangesPair(route.getExchangeFrom(),route.getExchangeTo());
//                        route.calcRouteValueInDollars();
//                        resultingRoute.calcEffectiveAmount();
//                        resultingRoute.calcRouteValueInDollars();
//                        System.out.println();
//                    }
//                }
//            }
//        }
       resultingRoutes.forEach(Route::refreshRouteValueInDollars);
        sort(masterRoutes);
        sort(resultingRoutes);
    }

    private void sort(ArrayList <Route> routes) {
        routes.sort(((o1, o2) -> o2.getRouteValueInDollars().compareTo(o1.getRouteValueInDollars())));
    }

    private class ExchangePair {
        Exchange from;
        Exchange to;

        public ExchangePair(Exchange from, Exchange to) {
            this.from = from;
            this.to = to;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ExchangePair that = (ExchangePair) o;
            return Objects.equals(from, that.from) &&
                    Objects.equals(to, that.to);
        }

        @Override
        public int hashCode() {
            return Objects.hash(from, to);
        }
    }

    private ArrayList <Route> findRoutes(ArrayList <Exchange> exchanges, Set <String> allPairs) {
        ArrayList <Route> masterRoutes = new ArrayList <>();
        for (String pairName : allPairs) {
            Route masterRoute = new Route(pairName);
            masterRoutes.add(masterRoute);
            masterRoute.fillAllDeals(exchanges);
        }
        return masterRoutes;
    }

    public ArrayList <Exchange> getExchanges() {
        return exchanges;
    }

    public ArrayList <Route> getMasterRoutes() {
        return masterRoutes;
    }

    public ArrayList <Route> getResultingRoutes() {
        return resultingRoutes;
    }
}
