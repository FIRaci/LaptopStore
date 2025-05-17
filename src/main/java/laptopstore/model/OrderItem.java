package laptopstore.model;

public class OrderItem {
    private int odId;
    private int orderId;
    private int productId;
    private int quantity;
    private double unitPrice;

    public OrderItem(int odId, int orderId, int productId, int quantity, double unitPrice) {
        this.odId = odId;
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    @Override
    public String toString() {
        return "OrderItem [odId=" + odId + ", orderId=" + orderId + ", productId=" + productId + ", quantity=" + quantity + ", unitPrice=$" + unitPrice + "]";
    }

    // Getters and setters
    public int getOdId() { return odId; }
    public int getOrderId() { return orderId; }
    public int getProductId() { return productId; }
    public int getQuantity() { return quantity; }
    public double getUnitPrice() { return unitPrice; }
    public void setOdId(int odId) { this.odId = odId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    public void setProductId(int productId) { this.productId = productId; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }
}