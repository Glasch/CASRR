package model;


import exchanges.Exchange;
import services.Updater;

import java.math.BigDecimal;
import java.util.*;

/**
 * Copyright (c) Anton on 17.11.2018.
 */
public class Router {
    private List <Exchange> exchanges;
    private List <Route> masterRoutes;
    private List <Route> resultingRoutes = new ArrayList <>();

    public Router(Updater updater) {
        exchanges = updater.getExchanges();

        Set <String> allPairs = findAllPairs();

        masterRoutes = findRoutes(exchanges, allPairs);
        for (Route route : masterRoutes) {
            route.filterDeals(BigDecimal.ZERO);
            route.calcRouteValueInDollars();
            route.filterZeroAmountDeals();
        }

        for (Route masterRoute : masterRoutes) {
            Map <ExchangePair, Route> resRoutes = new HashMap <>();
            for (Deal deal : masterRoute.getSortedEVDeals()) {
                ExchangePair exchangePair = new ExchangePair(deal.getBid().getExchange(), deal.getAsk().getExchange());
                Route route = resRoutes.get(exchangePair);
                if (route == null) {
                    route = new Route(masterRoute.getPairName());
                    route.setExchangeFrom(exchangePair.from);
                    route.setExchangeTo(exchangePair.to);
                    resRoutes.put(exchangePair, route);
                }
                if (deal.getSpread().compareTo(route.getExchangeFrom().getTakerTax().add(route.getExchangeTo().getTakerTax())) > 0) {
                    route.addDeal(deal);
                }
            }
            resultingRoutes.addAll(resRoutes.values());
        }

        resultingRoutes.forEach(Route::refreshRouteValueInDollars);
        sort(masterRoutes);
        sort(resultingRoutes);

        for (Route resultingRoute : resultingRoutes) {
            for (Deal deal  : resultingRoute.getSortedEVDeals())
                resultingRoute.setAmount(resultingRoute.getAmount().add(deal.getEffectiveAmount()));
        }
    }

    public Set <String> findAllPairs() {
        Set <String> allPairs = new HashSet<>();
        for (Exchange exchange : exchanges) {
            allPairs.addAll(exchange.getPairs());
        }
        return allPairs;
    }

    private void sort(List <Route> routes) {
        routes.sort(((o1, o2) -> o2.getRouteValueInDollars().compareTo(o1.getRouteValueInDollars())));
    }

    private class ExchangePair {
        Exchange from;
        Exchange to;

        ExchangePair(Exchange from, Exchange to) {
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

    public List <Route> findRoutes(List <Exchange> exchanges, Set <String> allPairs) {
        List <Route> masterRoutes = new ArrayList <>();
        for (String pairName : allPairs) {
            Route masterRoute = new Route(pairName);
            masterRoutes.add(masterRoute);
            masterRoute.fillAllDeals(exchanges);
        }
        return masterRoutes;
    }

    public List <Exchange> getExchanges() {
        return exchanges;
    }

    public List <Route> getResultingRoutes() {
        return resultingRoutes;
    }

    public List <Route> getMasterRoutes() {
        return masterRoutes;
    }
}
