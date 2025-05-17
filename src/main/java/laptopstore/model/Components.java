package laptopstore.model;

public class Components {
    private int componentId;
    private int productId; // FK tham chiếu đến Product
    private String componentName;

    public Components(int componentId, int productId, String componentName) {
        this.componentId = componentId;
        this.productId = productId;
        this.componentName = componentName;
    }

    @Override
    public String toString() {
        return "Component: " + componentName + " (Product ID: " + productId + ")";
    }

    // Getters and setters
    public int getComponentId() { return componentId; }
    public int getProductId() { return productId; }
    public String getComponentName() { return componentName; }
    public void setComponentId(int componentId) { this.componentId = componentId; }
    public void setProductId(int productId) { this.productId = productId; }
    public void setComponentName(String componentName) { this.componentName = componentName; }
}