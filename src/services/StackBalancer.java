package services;

import exchanges.Exchange;
import model.Route;
import model.Stack;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;

/*
 * Author: glaschenko
 * Created: 27.12.2018
 */
public class StackBalancer {
    private double maxLossPercent = 0.0;
    private BigDecimal totalStack;
    private HashMap<Class, Stack> stacks = new HashMap<>();

    public StackBalancer(Collection<Exchange> exchanges) {
        exchanges.forEach(e -> stacks.put(e.getClass(), new Stack(e)));
    }

    public HashMap<Class, Stack> getStacks() {
        return stacks;
    }

    public Stack getStack(Class clazz) {
        return stacks.get(clazz);
    }

    void updateTotalStack() {
        totalStack = BigDecimal.ZERO;
        stacks.values().forEach(s -> totalStack = totalStack.add(s.getActualStack()));
    }

    void runCycle() {
        for (Stack from : stacks.values()) {
            for (Stack to : stacks.values()) {
                if(from != to) {
                    tryBalance(from, to);
                }
            }
        }
    }

    private void tryBalance(Stack from, Stack to) {
        if (from.calcActualShare(totalStack) <= from.getTargetShare()) return;
        if (to.calcActualShare(totalStack) >= to.getTargetShare()) return;

        BigDecimal fromDiff = from.calcDiffToTarget(totalStack);
        BigDecimal toDiff = to.calcDiffToTarget(totalStack);

        boolean useFromExchangeRate = fromDiff.abs().compareTo(toDiff.abs()) < 0;
        BigDecimal maxTransaction = fromDiff.abs().min(toDiff.abs());

        for (String pairName : from.getExchange().getMarket().keySet()) {
            System.out.println("pairName = " + pairName);
            if (isUSDPair(pairName)) {
                if (to.getExchange().getMarket().containsKey(pairName)) {
                    Route route = new Route(pairName);
                    route.setExchangeFrom(from.getExchange());
                    route.setExchangeTo(to.getExchange());
                    route.addDealsForExchangesPair(route.getExchangeTo(), route.getExchangeFrom());
                    route.filterDeals(BigDecimal.valueOf(maxLossPercent));
                    route.calcRouteValueInDollars();
                    route.filterZeroAmountDeals();
                    route.applyUSDThreshold(maxTransaction, useFromExchangeRate);
                    route.refreshRouteValueInDollars();
                    System.out.println(route);
                    System.out.println("VALUE: " + route.getRouteValueInDollars());
                    System.out.println("AMOUNT: " + route.getRouteAmountInDollars());
                }
            }
        }


    }

    private boolean isUSDPair(String pairName) {
        return pairName.contains("USD") && !pairName.contains("USDT");
    }

    public void setMaxLossPercent(double maxLossPercent) {
        this.maxLossPercent = maxLossPercent;
    }
}