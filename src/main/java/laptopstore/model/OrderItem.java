package laptopstore.model;

import java.math.BigDecimal;

public class OrderItem {
    private int odId;
    private int orderId;
    private int productId;
    private int quantity;
    private BigDecimal unitPrice; // Đã là BigDecimal

    private String productName; // Để hiển thị tên sản phẩm (lấy từ JOIN)

    public OrderItem(int odId, int orderId, int productId, int quantity, BigDecimal unitPrice) {
        this.odId = odId;
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public OrderItem() {}

    public OrderItem(int odId, int orderId, int productId, int quantity, double unitPrice) {
        this(odId, orderId, productId, quantity, BigDecimal.valueOf(unitPrice));
    }

    @Override
    public String toString() {
        return "OrderItem [odId=" + odId +
                ", productId=" + productId + (productName != null ? " ("+productName+")" : "") +
                ", quantity=" + quantity +
                ", unitPrice=$" + (unitPrice != null ? unitPrice.toPlainString() : "N/A") + "]";
    }

    // Getters
    public int getOdId() { return odId; }
    public int getOrderId() { return orderId; }
    public int getProductId() { return productId; }
    public int getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public String getProductName() { return productName; }

    // Setters
    public void setOdId(int odId) { this.odId = odId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    public void setProductId(int productId) { this.productId = productId; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public void setProductName(String productName) { this.productName = productName; }
}
