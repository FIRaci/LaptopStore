package laptopstore.screen.tablemodel;

import laptopstore.model.OrderItem;
import laptopstore.model.Product; // Cần để lấy tên Product
import laptopstore.LaptopStoreApplication; // Để truy cập danh sách products

import javax.swing.table.AbstractTableModel;
import java.util.List;
import java.util.ArrayList;

public class OrderItemTableModel extends AbstractTableModel {
    private List<OrderItem> orderItems;
    // "OD ID", "Product ID", "Product Name", "Quantity", "Unit Price"
    private final String[] columnNames = {"OD ID", "Product ID", "Product Name", "Quantity", "Unit Price"};

    public OrderItemTableModel(List<OrderItem> orderItems) {
        this.orderItems = new ArrayList<>(orderItems); // Khởi tạo với danh sách rỗng hoặc danh sách item của order được chọn
    }

    public OrderItemTableModel() { // Constructor rỗng để có thể khởi tạo mà không cần item ban đầu
        this.orderItems = new ArrayList<>();
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
            case 1: return item.getProductId();
            case 2: // Lấy tên Product từ productId
                Product product = LaptopStoreApplication.products.stream()
                        .filter(p -> p.getProductId() == item.getProductId())
                        .findFirst().orElse(null);
                return product != null ? product.getModel() : "Unknown (ID: " + item.getProductId() + ")";
            case 3: return item.getQuantity();
            case 4: return item.getUnitPrice();
            default: return null;
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0: // OD ID
            case 1: // Product ID
            case 3: // Quantity
                return Integer.class;
            case 4: // Unit Price
                return Double.class;
            default:
                return String.class;
        }
    }

    // Phương thức để thiết lập/cập nhật danh sách các item hiển thị
    public void setItems(List<OrderItem> newItems) {
        if (newItems == null) {
            this.orderItems = new ArrayList<>();
        } else {
            this.orderItems = new ArrayList<>(newItems);
        }
        fireTableDataChanged(); // Thông báo cho JTable rằng toàn bộ dữ liệu đã thay đổi
    }

    public void addItem(OrderItem item) {
        orderItems.add(item);
        fireTableRowsInserted(orderItems.size() - 1, orderItems.size() - 1);
    }

    public void removeItem(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < orderItems.size()) {
            orderItems.remove(rowIndex);
            fireTableRowsDeleted(rowIndex, rowIndex);
        }
    }

    public void clearItems() {
        orderItems.clear();
        fireTableDataChanged();
    }

    public OrderItem getItemAt(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < orderItems.size()) {
            return orderItems.get(rowIndex);
        }
        return null;
    }

    public List<OrderItem> getItems() { // Lấy danh sách các item hiện tại (ví dụ, để lưu khi tạo order mới)
        return new ArrayList<>(orderItems);
    }
}