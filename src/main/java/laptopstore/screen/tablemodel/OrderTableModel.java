package laptopstore.screen.tablemodel;

import laptopstore.model.Order;
import laptopstore.model.Customer; // Cần để lấy tên Customer
import laptopstore.LaptopStoreApplication; // Để truy cập danh sách customers

import javax.swing.table.AbstractTableModel;
import java.util.List;
import java.util.ArrayList;
import java.time.format.DateTimeFormatter;

public class OrderTableModel extends AbstractTableModel {
    private List<Order> orders;
    // "Order ID", "Customer", "Payment ID", "Order Date", "Status", "Total Amount"
    private final String[] columnNames = {"Order ID", "Customer", "Payment ID", "Order Date", "Status", "Total Amount"};
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");


    public OrderTableModel(List<Order> orders) {
        this.orders = new ArrayList<>(orders);
    }

    @Override
    public int getRowCount() {
        return orders.size();
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
        Order order = orders.get(rowIndex);
        switch (columnIndex) {
            case 0: return order.getOrderId();
            case 1: // Lấy tên Customer từ customerId
                Customer customer = LaptopStoreApplication.customers.stream()
                        .filter(c -> c.getCustomerId() == order.getCustomerId())
                        .findFirst().orElse(null);
                return customer != null ? customer.getFirstName() + " " + customer.getLastName() : "Unknown (ID: " + order.getCustomerId() + ")";
            case 2: return order.getPaymentId();
            case 3: return order.getOrderDate() != null ? order.getOrderDate().format(dateFormatter) : "";
            case 4: return order.getStatus();
            case 5: return order.getTotalAmount(); // getTotalAmount đã được tính toán trong lớp Order
            default: return null;
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0: // Order ID
            case 2: // Payment ID
                return Integer.class;
            case 5: // Total Amount
                return Double.class;
            default:
                return String.class;
        }
    }

    public void addOrder(Order order) {
        orders.add(order);
        fireTableRowsInserted(orders.size() - 1, orders.size() - 1);
    }

    public void removeOrder(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < orders.size()) {
            orders.remove(rowIndex);
            fireTableRowsDeleted(rowIndex, rowIndex);
        }
    }

    public void removeOrder(Order order) {
        int rowIndex = orders.indexOf(order);
        if (rowIndex != -1) {
            removeOrder(rowIndex);
        }
    }

    public void updateOrder(int rowIndex, Order order) {
        if (rowIndex >= 0 && rowIndex < orders.size()) {
            // Đảm bảo rằng order mới có tổng tiền được tính toán lại nếu cần
            // (Lớp Order của bạn đã có phương thức calculateTotalAmount() tự động gọi khi addOrderItem)
            // Nếu có thay đổi item bên ngoài, bạn có thể cần gọi order.calculateTotalAmount() ở đây.
            orders.set(rowIndex, order);
            fireTableRowsUpdated(rowIndex, rowIndex);
        }
    }

    public void updateOrder(Order orderToUpdate) { // Phương thức này hữu ích để cập nhật đối tượng trực tiếp
        int rowIndex = -1;
        for (int i = 0; i < orders.size(); i++) {
            if (orders.get(i).getOrderId() == orderToUpdate.getOrderId()) {
                rowIndex = i;
                break;
            }
        }
        if (rowIndex != -1) {
            updateOrder(rowIndex, orderToUpdate);
        }
    }


    public Order getOrderAt(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < orders.size()) {
            return orders.get(rowIndex);
        }
        return null;
    }

    public void setOrders(List<Order> newOrders) {
        this.orders.clear();
        this.orders.addAll(newOrders);
        fireTableDataChanged();
    }
}