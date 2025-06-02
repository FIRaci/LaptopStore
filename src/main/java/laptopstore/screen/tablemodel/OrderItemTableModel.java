package laptopstore.screen.tablemodel;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import laptopstore.model.OrderItem;

public class OrderItemTableModel extends AbstractTableModel {
    private List<OrderItem> orderItems;
    private final String[] columnNames = {"OD ID", "Product ID", "Quantity"};

    public OrderItemTableModel(List<OrderItem> orderItems) {
        this.orderItems = new ArrayList<>(orderItems != null ? orderItems : new ArrayList<>());
    }

    public OrderItemTableModel() {
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
        return switch (columnIndex) {
            case 0 -> item.getOdId();
            case 1 -> item.getProductId();
            case 2 -> item.getQuantity();
            default -> null;
        };
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return switch (columnIndex) {
            case 0, 1, 2 -> Integer.class;
            default -> String.class;
        };
    }

    public void setItems(List<OrderItem> newItems) {
        this.orderItems.clear();
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
