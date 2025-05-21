package laptopstore.screen.tablemodel;

import laptopstore.model.Product;
// Để hiển thị tên Category, bạn cần một cách để lấy nó.
// Cách tốt nhất là Product model đã chứa Category Name (lấy từ JOIN trong ProductDataStore)
// Hoặc bạn có thể truyền CategoryDataStore vào đây (không khuyến khích lắm vì TableModel không nên truy cập DB)
// import laptopstore.model.Category;
// import laptopstore.data.CategoryDataStore;

import javax.swing.table.AbstractTableModel;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
// import java.util.HashMap; // Nếu cache tên category
// import java.util.Map;     // Nếu cache tên category

public class ProductTableModel extends AbstractTableModel {
    private List<Product> products;
    // Đã bao gồm "Name" (specificProductName), "Type", "Category ID"
    private final String[] columnNames = {"ID", "Name", "Model", "Brand", "Type", "Category ID", "Price", "Stock", "Published"};
    private final DateTimeFormatter yearFormatter = DateTimeFormatter.ofPattern("yyyy"); // Hoặc dd/MM/yyyy nếu yearPublish là LocalDateTime đầy đủ

    // Ví dụ nếu bạn muốn lấy tên Category từ DataStore (cân nhắc hiệu năng)
    // private CategoryDataStore categoryDs;
    // private Map<Integer, String> categoryNameCache;

    public ProductTableModel(List<Product> products /*, CategoryDataStore categoryDs (nếu cần) */) {
        this.products = new ArrayList<>(products != null ? products : new ArrayList<>());
        // if (categoryDs != null) this.categoryDs = categoryDs;
        // this.categoryNameCache = new HashMap<>();
    }

    public ProductTableModel() {
        this.products = new ArrayList<>();
        // this.categoryDs = new CategoryDataStore(); // Hoặc khởi tạo ở đây
        // this.categoryNameCache = new HashMap<>();
    }


    @Override
    public int getRowCount() {
        return products.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Product product = products.get(rowIndex);
        switch (columnIndex) {
            case 0: return product.getProductId();
            case 1: return product.getSpecificProductName(); // Tên cụ thể của sản phẩm
            case 2: return product.getModel();
            case 3: return product.getBrand();
            case 4: return product.getProductType(); // Loại sản phẩm (Laptop, Gear, etc.)
            case 5: // Hiển thị Category ID.
                // Để hiển thị tên Category:
                // 1. (Tốt nhất) Sửa ProductDataStore để JOIN và lấy category_name,
                //    sau đó thêm trường categoryName vào model Product.java.
                //    Rồi ở đây chỉ cần: return product.getCategoryName();
                // 2. (Cách khác, có thể chậm nếu nhiều dòng) Dùng categoryDs.getCategoryById(...)
                //    (xem ví dụ đã comment ở dưới).
                Integer categoryId = product.getCategoryId();
                return categoryId != null ? categoryId : "N/A"; // Tạm thời hiển thị ID
            case 6:
                BigDecimal price = product.getPrice();
                return price != null ? price.setScale(2, RoundingMode.HALF_UP) : null;
            case 7: return product.getStockQuantity();
            case 8:
                if (product.getYearPublish() != null) {
                    // Nếu yearPublish chỉ lưu năm, bạn có thể cần logic khác để format
                    // Hiện tại giả định nó là LocalDateTime
                    return product.getYearPublish().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")); // Hoặc chỉ năm: .getYear()
                }
                return "";
            default: return null;
        }
    }
    /*
    // Ví dụ cách lấy tên category (có thể gây N+1 query nếu không cache và gọi từ đây)
    private String getCategoryNameFromDs(int categoryId) {
        if (categoryDs == null) return String.valueOf(categoryId); // Không có DataStore
        if (categoryNameCache.containsKey(categoryId)) {
            return categoryNameCache.get(categoryId);
        }
        try {
            Category category = categoryDs.getCategoryById(categoryId);
            if (category != null) {
                String name = category.getCategoryName();
                categoryNameCache.put(categoryId, name);
                return name;
            }
        } catch (java.sql.SQLException e) {
            e.printStackTrace(); // Xử lý lỗi
        }
        return String.valueOf(categoryId); // Trả về ID nếu không tìm thấy tên
    }
    */


    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0: // ID
            case 7: // Stock
                return Integer.class;
            case 5: // Category ID
                return Integer.class; // Nếu hiển thị ID
            // return String.class; // Nếu hiển thị tên Category
            case 6: // Price
                return BigDecimal.class;
            default:
                return String.class;
        }
    }

    public void setProducts(List<Product> newProducts) {
        this.products.clear();
        // if (categoryNameCache != null) this.categoryNameCache.clear();
        if (newProducts != null) {
            this.products.addAll(newProducts);
        }
        fireTableDataChanged();
    }

    public Product getProductAt(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < products.size()) {
            return products.get(rowIndex);
        }
        return null;
    }

    public void addProductRow(Product product) {
        if (product != null) {
            products.add(product);
            fireTableRowsInserted(products.size() - 1, products.size() - 1);
        }
    }

    public void removeProductRow(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < products.size()) {
            products.remove(rowIndex);
            fireTableRowsDeleted(rowIndex, rowIndex);
        }
    }

    public void removeProductRow(Product product) {
        int rowIndex = products.indexOf(product);
        if (rowIndex != -1) {
            removeProductRow(rowIndex);
        }
    }

    public void updateProductRow(int rowIndex, Product product) {
        if (rowIndex >= 0 && rowIndex < products.size() && product != null) {
            products.set(rowIndex, product);
            fireTableRowsUpdated(rowIndex, rowIndex);
        }
    }
}
