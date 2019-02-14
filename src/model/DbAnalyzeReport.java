package model;

import exchanges.Exchange;
import services.Updater;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (c) Anton on 12.02.2019.
 */
public class DbAnalyzeReport {
    private List <Route> possibleRoutes = new ArrayList <>();

    public void updatePossibleRoutesData(Router router) {
        for (Route route : router.getResultingRoutes()) {
            for (Route possibleRoute : possibleRoutes) {
                if (checkCondition(route, possibleRoute)) {
                    possibleRoute.setAmount(possibleRoute.getAmount().add(route.getAmount()));
                    possibleRoute.setTaxFrom(possibleRoute.getTaxFrom().add(route.getTaxFrom()));
                    possibleRoute.setTaxTo(possibleRoute.getTaxTo().add(route.getTaxTo()));
                    possibleRoute.getDeals().addAll(route.getSortedEVDeals());
                    possibleRoute.calcRouteSpread(possibleRoute.getDeals());
                }
            }
        }
    }

    public void initPossibleRoutes(Router router) {
        Updater updater = Updater.getInstance();
        for (Exchange from : updater.getExchanges()) {
            for (Exchange to : updater.getExchanges()) {
                for (Route route : router.getMasterRoutes()) {
                    possibleRoutes.add(new Route(route.getPairName(), from, to));
                }
            }
        }
    }

    private boolean checkCondition(Route route, Route possibleRoute) {
        return route.getPairName() == possibleRoute.getPairName()
                && route.getExchangeFrom() == possibleRoute.getExchangeFrom()
                && route.getExchangeTo() == possibleRoute.getExchangeTo();
    }

    public List <Route> getPossibleRoutes() {
        return possibleRoutes;
    }
}
