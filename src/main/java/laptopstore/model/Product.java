package laptopstore.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Product {
    private int productId;
    private String model;
    private String brand;
    private String description;
    private double price; // MONEY sẽ được biểu diễn bằng double trong Java
    private int stockQuantity;
    private LocalDateTime yearPublish;

    public Product(int productId, String model, String brand, String description, BigDecimal price, int stockQuantity, LocalDateTime yearPublish) {
        this.productId = productId;
        this.model = model;
        this.brand = brand;
        this.description = description;
        this.price = price.doubleValue();
        this.stockQuantity = stockQuantity;
        this.yearPublish = yearPublish;
    }

    @Override
    public String toString() {
        return model + " (" + brand + ") - $" + price + " - Stock: " + stockQuantity + " - Year: " + yearPublish.getYear();
    }

    // Getters and setters
    public int getProductId() { return productId; }
    public String getModel() { return model; }
    public String getBrand() { return brand; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public int getStockQuantity() { return stockQuantity; }
    public LocalDateTime getYearPublish() { return yearPublish; }
    public void setProductId(int productId) { this.productId = productId; }
    public void setModel(String model) { this.model = model; }
    public void setBrand(String brand) { this.brand = brand; }
    public void setDescription(String description) { this.description = description; }
    public void setPrice(double price) { this.price = price; }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }
    public void setYearPublish(LocalDateTime yearPublish) { this.yearPublish = yearPublish; }

    public int getStock() {
        return stockQuantity;
    }
}