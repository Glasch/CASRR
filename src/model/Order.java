package model;

import exchanges.Exchange;

import java.math.BigDecimal;

public class Order {
    private static int lastId = 1;

    private int id = lastId++;
    private Exchange exchange;
    private BigDecimal price;
    private BigDecimal amount;
    private BigDecimal remainingAmount;

    public Order(Exchange exchange, BigDecimal price, BigDecimal amount) {
        this.exchange = exchange;
        this.price = price;
        this.amount = amount;
        resetRemainingAmount();
    }

    void resetRemainingAmount() {
        remainingAmount = amount;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    BigDecimal getRemainingAmount() {
        return remainingAmount;
    }

    void subtractRemainingAmount(BigDecimal amount) {
        remainingAmount = remainingAmount.subtract(amount);
    }

    public Exchange getExchange() {
        return exchange;
    }

    @Override
    public String toString() {
        BigDecimal div = amount.subtract(remainingAmount).divide(amount, BigDecimal.ROUND_HALF_UP);
        Long percentUsed = Math.round(div.doubleValue()*100);
        return "O-"+id + " P: " + price + " A: " + amount + " U: " + percentUsed + "%";
    }

    public void setExchange(Exchange exchange) {
        this.exchange = exchange;
    }
}