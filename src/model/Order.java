package model;

import java.math.BigDecimal;

public class Order {
    private BigDecimal price;
    private BigDecimal amount;

    private BigDecimal remainingAmount;

    public Order(BigDecimal price, BigDecimal amount) {
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

    public BigDecimal getRemainingAmount() {
        return remainingAmount;
    }

    public void subtractRemainingAmount(BigDecimal amount) {
        remainingAmount = remainingAmount.subtract(amount);
    }
}