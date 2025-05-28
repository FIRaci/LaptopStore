package laptopstore.screen.tablemodel;

import laptopstore.model.OrderItem;
// Để hiển thị tên Product:
// import laptopstore.model.Product;
// import laptopstore.data.ProductDataStore;

import javax.swing.table.AbstractTableModel;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.ArrayList;
// import java.util.HashMap;
// import java.util.Map;

public class OrderItemTableModel extends AbstractTableModel {
    private List<OrderItem> orderItems;
    // Đã thêm cột Total
    private final String[] columnNames = {"OD ID", "Product ID", "Quantity", "Unit Price", "Item Total"};

    // Ví dụ nếu muốn lấy tên Product
    // private ProductDataStore productDs;
    // private Map<Integer, String> productNameCache;

    public OrderItemTableModel(List<OrderItem> orderItems) {
        this.orderItems = new ArrayList<>(orderItems != null ? orderItems : new ArrayList<>());
        // this.productDs = new ProductDataStore();
        // this.productNameCache = new HashMap<>();
    }

    public OrderItemTableModel() {
        this.orderItems = new ArrayList<>();
        // this.productDs = new ProductDataStore();
        // this.productNameCache = new HashMap<>();
    }


    @Override
    public int getRowCount() {
        return orderItems.size();
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
        OrderItem item = orderItems.get(rowIndex);
        switch (columnIndex) {
            case 0: return item.getOdId();
            case 1: // Hiển thị Product ID.
                // Để hiển thị tên Product, tương tự như Category trong ProductTableModel,
                // bạn nên sửa OrderDataStore (cụ thể là getOrderItemsByOrderId hoặc mapRowToOrderItem
                // nếu OrderItem model chứa Product object) để JOIN và lấy tên Product.
                // Sau đó thêm trường productName (hoặc Product object) vào model OrderItem.java.
                // Rồi ở đây chỉ cần: return item.getProductName();
                return item.getProductId(); // Tạm thời hiển thị ID
            case 2: return item.getQuantity();
            case 3:
                BigDecimal unitPrice = item.getUnitPrice();
                return unitPrice != null ? unitPrice.setScale(2, RoundingMode.HALF_UP) : null;
            case 4: // Total for this item
                if (item.getUnitPrice() != null && item.getQuantity() > 0) {
                    return item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())).setScale(2, RoundingMode.HALF_UP);
                }
                return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            default: return null;
        }
    }
    /*
    private String getProductNameFromDs(int productId) {
        if (productDs == null) return String.valueOf(productId);
        if (productNameCache.containsKey(productId)) {
            return productNameCache.get(productId);
        }
        try {
            Product product = productDs.getProductById(productId);
            if (product != null) {
                String name = product.getSpecificProductName() != null ? product.getSpecificProductName() : product.getModel();
                productNameCache.put(productId, name);
                return name;
            }
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
        return String.valueOf(productId);
    }
    */

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0: // OD ID
            case 1: // Product ID
            case 2: // Quantity
                return Integer.class;
            case 3: // Unit Price
            case 4: // Item Total
                return BigDecimal.class;
            default:
                return String.class;
        }
    }

    public void setItems(List<OrderItem> newItems) {
        this.orderItems.clear();
        // if (productNameCache != null) this.productNameCache.clear();
        if (newItems != null) {
            this.orderItems.addAll(newItems);
        }
        fireTableDataChanged();
    }

    public void addItem(OrderItem item) {
        if (item != null) {
            orderItems.add(item);
            fireTableRowsInserted(orderItems.size() - 1, orderItems.size() - 1);
        }
    }

    public void removeItem(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < orderItems.size()) {
            orderItems.remove(rowIndex);
            fireTableRowsDeleted(rowIndex, rowIndex);
        }
    }

    public void removeItem(OrderItem item) {
        int index = orderItems.indexOf(item);
        if (index != -1) {
            removeItem(index);
        }
    }

    public void clearItems() {
        orderItems.clear();
        // if (productNameCache != null) this.productNameCache.clear();
        fireTableDataChanged();
    }

    public OrderItem getItemAt(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < orderItems.size()) {
            return orderItems.get(rowIndex);
        }
        return null;
    }

    public List<OrderItem> getItems() {
        // Trả về một bản sao để tránh thay đổi từ bên ngoài ảnh hưởng trực tiếp list nội bộ
        return new ArrayList<>(orderItems);
    }
}
