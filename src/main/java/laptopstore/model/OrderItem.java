package laptopstore.model;

// Đã xóa import java.math.BigDecimal;

public class OrderItem {
    private int odId;
    private int orderId;
    private int productId;
    private int quantity;
    // Đã xóa unitPrice
    private String productName; // Giữ lại để hiển thị nếu cần (sẽ được set từ Product)

    public OrderItem(int odId, int orderId, int productId, int quantity) {
        this.odId = odId;
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        // productName sẽ được set riêng sau khi lấy thông tin Product
    }

    public OrderItem() {
        // Constructor rỗng
    }


    @Override
    public String toString() {
        return "OrderItem [odId=" + odId +
                ", orderId=" + orderId +
                ", productId=" + productId + (productName != null ? " (" + productName + ")" : "") +
                ", quantity=" + quantity +
                "]";
    }

    // Getters
    public int getOdId() {
        return odId;
    }

    public int getOrderId() {
        return orderId;
    }

    public int getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getProductName() {
        return productName;
    }

    // Setters
    public void setOdId(int odId) {
        this.odId = odId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }
}
