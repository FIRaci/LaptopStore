package laptopstore.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Product {
    private int productId;
    private String model;
    private String brand;
    private String description;
    private BigDecimal price; // Đã là BigDecimal
    private int stockQuantity;
    private LocalDateTime yearPublish;

    private String specificProductName; // Tên cụ thể của sản phẩm
    private Integer categoryId;       // FK
    private String categoryName;      // Tên category (lấy từ JOIN)
    private String productType; // Loại sản phẩm (ví dụ: Laptop, Phụ kiện, ...)

    // Constructor chính, bao gồm các trường mới
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
        this.productType = "Laptop"; // Giá trị mặc định vì đây là cửa hàng laptop
        // categoryName sẽ được set sau khi lấy từ DB (qua JOIN)
    }

    public Product() {}

    // Constructor cũ hơn (nếu vẫn dùng đâu đó, nhưng nên chuyển sang dùng constructor chính)
    public Product(int productId, String model, String brand, String description, BigDecimal price, int stockQuantity, LocalDateTime yearPublish) {
        this(productId, model, model, brand, description, price, stockQuantity, yearPublish, null); // specificProductName = model, categoryId null
    }
    public Product(int productId, String model, String brand, String description, double price, int stockQuantity, LocalDateTime yearPublish) {
        this(productId, model, model, brand, description, BigDecimal.valueOf(price), stockQuantity, yearPublish, null);
    }


    @Override
    public String toString() {
        // Ưu tiên specificProductName nếu có cho JComboBox
        String nameToDisplay = (specificProductName != null && !specificProductName.isEmpty()) ? specificProductName : model;
        return nameToDisplay + (brand != null && !brand.isEmpty() ? " (" + brand + ")" : "");
    }

    // Getters
    public int getProductId() { return productId; }
    public String getModel() { return model; }
    public String getBrand() { return brand; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public int getStockQuantity() { return stockQuantity; }
    public LocalDateTime getYearPublish() { return yearPublish; }
    public String getSpecificProductName() { return specificProductName; }
    public Integer getCategoryId() { return categoryId; }
    public String getCategoryName() { return categoryName; }
    public String getProductType() { return productType; }

    // Setters
    public void setProductId(int productId) { this.productId = productId; }
    public void setModel(String model) { this.model = model; }
    public void setBrand(String brand) { this.brand = brand; }
    public void setDescription(String description) { this.description = description; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }
    public void setYearPublish(LocalDateTime yearPublish) { this.yearPublish = yearPublish; }
    public void setSpecificProductName(String specificProductName) { this.specificProductName = specificProductName; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public void setProductType(String productType) { this.productType = productType; }
}
