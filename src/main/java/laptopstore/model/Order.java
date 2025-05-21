package laptopstore.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Order {
    private int orderId;
    private int customerId;
    private int paymentId;
    private LocalDate orderDate;
    private String status;

    private BigDecimal netAmount;   // Đã là BigDecimal
    private BigDecimal tax;         // Đã là BigDecimal
    private BigDecimal totalAmount; // Đã là BigDecimal

    private List<OrderItem> orderItems;

    private String customerName; // Để hiển thị (lấy từ JOIN)
    private String shippingAddress; // Thêm từ schema CSDL
    private String notes;           // Thêm từ schema CSDL


    public Order(int orderId, int customerId, int paymentId, LocalDate orderDate, String status,
                 BigDecimal netAmount, BigDecimal tax, BigDecimal totalAmount,
                 String shippingAddress, String notes) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.paymentId = paymentId;
        this.orderDate = orderDate;
        this.status = status;
        this.netAmount = (netAmount != null) ? netAmount : BigDecimal.ZERO;
        this.tax = (tax != null) ? tax : BigDecimal.ZERO;
        this.totalAmount = (totalAmount != null) ? totalAmount : BigDecimal.ZERO;
        this.shippingAddress = shippingAddress;
        this.notes = notes;
        this.orderItems = new ArrayList<>();
    }

    // Constructor đơn giản hơn, các giá trị tiền tệ sẽ được tính sau
    public Order(int orderId, int customerId, int paymentId, LocalDate orderDate, String status) {
        this(orderId, customerId, paymentId, orderDate, status, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, null, null);
    }

    public Order() {
        this.orderItems = new ArrayList<>();
        this.netAmount = BigDecimal.ZERO;
        this.tax = BigDecimal.ZERO;
        this.totalAmount = BigDecimal.ZERO;
    }

    public void addOrderItem(OrderItem item) {
        if (item != null) {
            this.orderItems.add(item);
        }
    }

    public void calculateAndSetTotals() {
        BigDecimal currentNetAmount = BigDecimal.ZERO;
        if (this.orderItems != null) {
            for (OrderItem item : this.orderItems) {
                if (item.getUnitPrice() != null && item.getQuantity() > 0) {
                    BigDecimal itemTotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                    currentNetAmount = currentNetAmount.add(itemTotal);
                }
            }
        }
        this.netAmount = currentNetAmount;
        this.tax = this.netAmount.multiply(new BigDecimal("0.10")).setScale(2, RoundingMode.HALF_UP); // Ví dụ thuế 10%
        this.totalAmount = this.netAmount.add(this.tax);
    }

    @Override
    public String toString() {
        return "Order ID: " + orderId +
                (customerName != null ? " - Customer: " + customerName : " - Cust.ID: " + customerId) +
                " - Total: $" + (totalAmount != null ? totalAmount.toPlainString() : "N/A") +
                " - Status: " + (status != null ? status : "N/A");
    }

    // Getters
    public int getOrderId() { return orderId; }
    public int getCustomerId() { return customerId; }
    public int getPaymentId() { return paymentId; }
    public LocalDate getOrderDate() { return orderDate; }
    public String getStatus() { return status; }
    public BigDecimal getNetAmount() { return netAmount; }
    public BigDecimal getTax() { return tax; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public List<OrderItem> getOrderItems() { return orderItems; }
    public String getCustomerName() { return customerName; }
    public String getShippingAddress() { return shippingAddress; }
    public String getNotes() { return notes; }

    // Setters
    public void setOrderId(int orderId) { this.orderId = orderId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }
    public void setPaymentId(int paymentId) { this.paymentId = paymentId; }
    public void setOrderDate(LocalDate orderDate) { this.orderDate = orderDate; }
    public void setStatus(String status) { this.status = status; }
    public void setNetAmount(BigDecimal netAmount) { this.netAmount = netAmount; }
    public void setTax(BigDecimal tax) { this.tax = tax; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = (orderItems != null) ? new ArrayList<>(orderItems) : new ArrayList<>();
    }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
    public void setNotes(String notes) { this.notes = notes; }
}
