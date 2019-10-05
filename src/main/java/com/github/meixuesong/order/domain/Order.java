package com.github.meixuesong.order.domain;

import com.github.meixuesong.aggregatepersistence.Versionable;
import com.github.meixuesong.customer.Customer;
import lombok.Data;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Data
public class Order implements Versionable {
    private String id;
    private Date createTime;
    private Customer customer;
    private List<OrderItem> items;
    private OrderStatus status;
    private BigDecimal totalPrice = BigDecimal.ZERO;
    private BigDecimal totalPayment = BigDecimal.ZERO;
    private int version;

    public void checkout(Payment payment) {
        if (status != OrderStatus.NEW) {
            throw new OrderPaymentException("The order status is not for payment.");
        }

        totalPayment = payment.getAmount();
        validatePayments();
        this.status = OrderStatus.PAID;
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
        totalPrice = items.stream().map(item -> item.getSubTotal()).reduce(BigDecimal.ZERO, (a, b) -> a.add(b));
    }

    public void discard() {
        if (status != OrderStatus.NEW) {
            throw new RuntimeException("Only new order can be discard.");
        }

        this.status = OrderStatus.DISCARD;
    }

    private void validatePayments() {
        if (totalPayment.compareTo(totalPrice) != 0) {
            DecimalFormat decimalFormat = new DecimalFormat("0.00");
            throw new OrderPaymentException(String.format("Payment (%s) is not equals to total price (%s)",
                    decimalFormat.format(totalPayment), decimalFormat.format(totalPrice)));
        }
    }
}
