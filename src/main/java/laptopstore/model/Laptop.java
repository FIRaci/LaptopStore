package laptopstore.model;

public class Laptop {
    private int laptopId;
    private int productId; // FK tham chiếu đến Product
    private String laptopName;

    public Laptop(int laptopId, int productId, String laptopName) {
        this.laptopId = laptopId;
        this.productId = productId;
        this.laptopName = laptopName;
    }

    @Override
    public String toString() {
        return "Laptop: " + laptopName + " (Product ID: " + productId + ")";
    }

    // Getters and setters
    public int getLaptopId() { return laptopId; }
    public int getProductId() { return productId; }
    public String getLaptopName() { return laptopName; }
    public void setLaptopId(int laptopId) { this.laptopId = laptopId; }
    public void setProductId(int productId) { this.productId = productId; }
    public void setLaptopName(String laptopName) { this.laptopName = laptopName; }
}