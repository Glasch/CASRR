import model.Route;
import model.Router;
import services.Updater;
import services.UsdConverter;

import java.io.IOException;

/**
 * Copyright (c) Anton on 17.11.2018.
 */
public class Main {

    public static void main(String[] args) throws InterruptedException, IOException {
        Updater updater = Updater.getInstance();
        updater.update();
        Router router = new Router(updater);
        for (Route route : router.getRoutes()) {
            if (!route.getSortedEVDeals().isEmpty()) {
                System.out.println(route.getPairName() + " " + route.getExchangeFrom() + " " + route.getExchangeTo() +
                        " " + route.getSortedEVDeals());
            }
        }
    }
}

