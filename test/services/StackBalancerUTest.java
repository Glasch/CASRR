package services;

import exchanges.Bittrex;
import exchanges.Exchange;
import exchanges.Exmo;
import junit.framework.TestCase;
import model.Stack;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/*
 * Author: glaschenko
 * Created: 27.12.2018
 */
public class StackBalancerUTest extends TestCase {

    public void testStackBalancer() throws InterruptedException {
        Updater updater = Updater.getInstance();
        updater.update();

        StackBalancer stackBalancer = new StackBalancer(Updater.getInstance().getExchanges());
        stackBalancer.setMaxLossPercent(-15);

        Stack bittrexStack = stackBalancer.getStack(Bittrex.class);
        bittrexStack.setTargetShare(0.5);
        bittrexStack.setActualStack(BigDecimal.valueOf(200));

        Stack exmoStack = stackBalancer.getStack(Exmo.class);
        exmoStack.setTargetShare(0.5);
        exmoStack.setActualStack(BigDecimal.valueOf(0));

        stackBalancer.updateTotalStack();
        stackBalancer.runCycle();

        System.out.println();
    }

    public void testGetCurrencies() {
        Set <String> currencies = new HashSet <>();
        Updater updater = Updater.getInstance();
        ArrayList <Exchange> exchanges = updater.getExchanges();
        for (Exchange exchange : exchanges) {
            if (exchange.getClass().getSimpleName().equals("Binance")) {
                ArrayList <String> pairs = exchange.getPairs();
                for (String pair : pairs) {
                    String[] split = pair.split("/");
                    currencies.add(split[0]);
                    currencies.add(split[1]);
                }
            }
        }
        System.out.println(currencies);
    }

}
