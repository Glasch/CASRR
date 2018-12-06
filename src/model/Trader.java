package model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Copyright (c) Anton on 06.12.2018.
 */
public class Trader {
    private Router router;
    private ArrayList<Route> tradeRoutes;
    private BigDecimal valueInDollars;


    public Trader(Router router) {
        this.router = router;
    }

    public void calcValueInDollars() {
        ArrayList<Route> routerRoutes = router.getRoutes();
        HashMap<String, Route> masterRoutes = new HashMap<>();
        for (Route route : routerRoutes) {
            String pairName = route.getPairName();
            Route master = masterRoutes.get(pairName);
            if (master == null) {
                master = new Route(pairName);
                masterRoutes.put(pairName,master);
            }
            master.addDeals(route.getSortedEVDeals());
        }
        for (Route route : masterRoutes.values()) {
            route.filterDeals();
            route.calcRouteValueInDollars();
        }
        for (Route route : routerRoutes) {
            route.filterZeroAmountDeals();
            if (route.getSortedEVDeals().isEmpty()) continue;

            System.out.println("-------------------------------------------------");
            System.out.println(route);
            for (Deal deal : route.getSortedEVDeals() ) {
                System.out.println(deal);
            }
            System.out.println("----------------------------------------------");
        }
        System.out.println();
    }
}



