package model;

import exchanges.Exchange;

import java.math.BigDecimal;

/*
 * Author: glaschenko
 * Created: 27.12.2018
 */
public class Stack {
    private Exchange exchange;
    private double targetShare = 0;
    private BigDecimal actualStack = BigDecimal.ZERO;

    public Stack(Exchange exchange, double targetShare) {
        this.exchange = exchange;
        this.targetShare = targetShare;
    }

    public Stack(Exchange exchange) {
        this.exchange = exchange;
    }

    public Exchange getExchange(){
            return exchange;
    }

    public void setExchange(Exchange exchange) {
        this.exchange = exchange;
    }

    public double getTargetShare() {
        return targetShare;
    }

    public void setTargetShare(double targetShare) {
        this.targetShare = targetShare;
    }

    public BigDecimal getActualStack() {
        return actualStack;
    }

    public void setActualStack(BigDecimal actualStack) {
        this.actualStack = actualStack;
    }

    public double calcActualShare(BigDecimal totalStack){
        BigDecimal res = actualStack.divide(totalStack,4,BigDecimal.ROUND_HALF_UP);
        return res.doubleValue();
    }

    public BigDecimal calcDiffToTarget(BigDecimal totalStack){
        return totalStack.multiply(BigDecimal.valueOf(targetShare)).subtract(actualStack);
    }

    @Override
    public String toString() {
        return exchange.toString();
    }
}
