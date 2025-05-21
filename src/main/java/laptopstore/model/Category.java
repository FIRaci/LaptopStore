package laptopstore.model;

public class Category {
    private int categoryId;
    private String categoryName;

    // Constructors
    public Category() {
    }

    public Category(int categoryId, String categoryName) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
    }

    // Getters and Setters
    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    @Override
    public String toString() {
        // Quan trọng cho JComboBox hiển thị tên thay vì object reference
        return categoryName != null ? categoryName : "ID: " + categoryId;
    }
}
