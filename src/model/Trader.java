package model;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * Copyright (c) Anton on 06.12.2018.
 */
public class Trader {
    private Router router;
    private ArrayList <Route> tradeRoutes;
    private BigDecimal valueInDollars;


    public Trader(Router router) {
        this.router = router;
        calcValueInDollars();
    }

    public void calcValueInDollars() {
        ArrayList <Route> routerRoutes = router.getRoutes();
        routerRoutes.sort((o1, o2) -> o2.getRouteValueInDollars().compareTo(o1.getRouteValueInDollars()));
        for (Route routerRoute : routerRoutes) {
            for (Route route : routerRoutes) {
                route.calcRouteValueInDollars();
                routerRoutes.sort((o1, o2) -> o2.getRouteValueInDollars().compareTo(o1.getRouteValueInDollars()));
            } // Вылечить ConcurrentModificationException (iterator)
            System.out.println("-------------------------------------------");
            System.out.println(routerRoute.getRouteValueInDollars());
            System.out.println(routerRoute.getExchangeFrom() + " --> " + routerRoute.getExchangeTo() + " " + routerRoute.getPairName());
            for (Deal deal : routerRoute.getSortedEVDeals()) {
                System.out.println(deal.getBid().getPrice()+ " " + deal.getAsk().getPrice()+ " " + deal.getEffectiveAmount());
                deal.getBid().setAmount(BigDecimal.ZERO);
                deal.getAsk().setAmount(BigDecimal.ZERO);
            }
        }
        System.out.println();
    }

}



