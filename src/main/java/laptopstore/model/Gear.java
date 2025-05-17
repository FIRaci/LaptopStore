package laptopstore.model;

public class Gear {
    private int gearId;
    private int productId; // FK tham chiếu đến Product
    private String gearName;

    public Gear(int gearId, int productId, String gearName) {
        this.gearId = gearId;
        this.productId = productId;
        this.gearName = gearName;
    }

    @Override
    public String toString() {
        return "Gear: " + gearName + " (Product ID: " + productId + ")";
    }

    // Getters and setters
    public int getGearId() { return gearId; }
    public int getProductId() { return productId; }
    public String getGearName() { return gearName; }
    public void setGearId(int gearId) { this.gearId = gearId; }
    public void setProductId(int productId) { this.productId = productId; }
    public void setGearName(String gearName) { this.gearName = gearName; }
}