import model.Route;
import model.Router;
import services.ConnectionManager;
import services.DBManager;
import services.Updater;

import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Copyright (c) Anton on 17.11.2018.
 */
public class Main {

    public static void main(String[] args) throws Exception {
        Updater updater = Updater.getInstance();
        DBManager dbManager = new DBManager(updater);
        dbManager.saveStaticData();
        updater.update();
        dbManager.saveOrders();
        while (true) {
            updater.update();
            Router router = new Router(updater);
            System.out.println(Updater.getTimestamp());
        for (Route route : router.getRoutes()) {
            if (!route.getSortedEVDeals().isEmpty()) {
                System.out.println("----------ROUTE-----------------------------");
                System.out.println(route.getPairName() + " " + route.getExchangeFrom() + " " + route.getExchangeTo() +
                        " " + route.getSortedEVDeals());
                System.out.println("----------ROUTE VALUE: " + route.getRouteValueInDollars() + "\n");
            }
        }
        }
    }
}

