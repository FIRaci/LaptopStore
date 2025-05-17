package laptopstore.model;

import laptopstore.LaptopStoreApplication;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Order {
    private int orderId;
    private int customerId;
    private int paymentId;
    private LocalDate orderDate;
    private String status;
    private double netAmount;
    private double tax;
    private double totalAmount;
    private List<OrderItem> orderItems;

    public Order(int orderId, int customerId, int paymentId, LocalDate orderDate, String status, double netAmount, double tax, double totalAmount) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.paymentId = paymentId;
        this.orderDate = orderDate;
        this.status = status;
        this.netAmount = netAmount;
        this.tax = tax;
        this.totalAmount = totalAmount;
        this.orderItems = new ArrayList<>();
    }

    public void addOrderItem(OrderItem item) {
        orderItems.add(item);
        calculateTotalAmount();
    }

    public void calculateTotalAmount() {
        netAmount = 0;
        for (OrderItem item : orderItems) {
            Product product = LaptopStoreApplication.products.stream()
                    .filter(p -> p.getProductId() == item.getProductId())
                    .findFirst().orElse(null);
            if (product != null) {
                netAmount += product.getPrice() * item.getQuantity();
            }
        }
        tax = netAmount * 0.1; // Thuế 10%
        totalAmount = netAmount + tax;
    }

    @Override
    public String toString() {
        return "Order ID: " + orderId + " - Total: $" + totalAmount + " - Status: " + status;
    }

    // Getters and setters
    public int getOrderId() { return orderId; }
    public int getCustomerId() { return customerId; }
    public int getPaymentId() { return paymentId; }
    public LocalDate getOrderDate() { return orderDate; }
    public String getStatus() { return status; }
    public double getNetAmount() { return netAmount; }
    public double getTax() { return tax; }
    public double getTotalAmount() { return totalAmount; }
    public List<OrderItem> getOrderItems() { return orderItems; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }
    public void setPaymentId(int paymentId) { this.paymentId = paymentId; }
    public void setOrderDate(LocalDate orderDate) { this.orderDate = orderDate; }
    public void setStatus(String status) { this.status = status; }
    public void setNetAmount(double netAmount) { this.netAmount = netAmount; }
    public void setTax(double tax) { this.tax = tax; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    public void setOrderItems(List<OrderItem> orderItems) { this.orderItems = orderItems; }
}