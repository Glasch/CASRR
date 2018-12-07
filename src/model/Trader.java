package model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Copyright (c) Anton on 06.12.2018.
 */
public class Trader {
    private Router router;
    private BigDecimal totalValueInDollars;


    public Trader(Router router) {
        this.router = router;
    }

    public void calcValueInDollars() {
        System.out.println("-------------MASTER ROUTES------------------");
        for (Route route : router.getMasterRoutes()) {
            if (route.getSortedEVDeals().isEmpty()) continue;
            printRoute(route);
        }
        System.out.println("-------------INDIVIDUAL ROUTES------------------");
        totalValueInDollars = BigDecimal.ZERO;
        for (Route route : router.getResultingRoutes()) {
            if (route.getSortedEVDeals().isEmpty()) continue;
            printRoute(route);
            totalValueInDollars = totalValueInDollars.add(route.getRouteValueInDollars());
        }
        System.out.println("WE WILL GET RICHER BY: " + totalValueInDollars);

    }

    private void printRoute(Route route) {
//        System.out.println("-------------------------------------------------");
        System.out.println(route);
        for (Deal deal : route.getSortedEVDeals() ) {
            System.out.println(deal);
        }
        System.out.println("----------------------------------------------");
    }
}



