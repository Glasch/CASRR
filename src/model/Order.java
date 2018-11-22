package model;

import java.math.BigDecimal;

public class Order {
    private BigDecimal price;
    private BigDecimal amount;

    public Order(BigDecimal price, BigDecimal amount) {
        this.price = price;
        this.amount = amount;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}