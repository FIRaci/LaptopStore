package laptopstore.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Product {
    private int productId;
    private String model;
    private String brand;
    private String description;
    private BigDecimal price;
    private int stockQuantity;
    private LocalDateTime yearPublish;

    private String specificProductName;
    private Integer categoryId;
    private String categoryName;

    public Product(int productId, String specificProductName, String model, String brand, String description,
                   BigDecimal price, int stockQuantity, LocalDateTime yearPublish,
                   Integer categoryId) {
        this.productId = productId;
        this.specificProductName = specificProductName;
        this.model = model;
        this.brand = brand;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.yearPublish = yearPublish;
        this.categoryId = categoryId;

    }

    public Product() {
    }

    @Override
    public String toString() {
        return String.format("ID: %d, Name: %s, Model: %s, Brand: %s, Category: %s, Price: %s, Stock: %d, Published: %s",
                productId,
                specificProductName != null ? specificProductName : "N/A",
                model != null ? model : "N/A",
                brand != null ? brand : "N/A",
                categoryName != null ? categoryName : (categoryId != null ? "CatID: " + categoryId : "N/A"),
                price != null ? price.toPlainString() + " VNĐ" : "N/A",
                stockQuantity,
                yearPublish != null ? yearPublish.toLocalDate().toString() : "N/A"
        );
    }

    // Getters
    public int getProductId() {
        return productId;
    }

    public String getModel() {
        return model;
    }

    public String getBrand() {
        return brand;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public LocalDateTime getYearPublish() {
        return yearPublish;
    }

    public String getSpecificProductName() {
        return specificProductName;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    // Setters
    public void setProductId(int productId) {
        this.productId = productId;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public void setYearPublish(LocalDateTime yearPublish) {
        this.yearPublish = yearPublish;
    }

    public void setSpecificProductName(String specificProductName) {
        this.specificProductName = specificProductName;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
}
