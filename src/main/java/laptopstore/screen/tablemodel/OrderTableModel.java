package laptopstore.screen.tablemodel;

import laptopstore.model.Order;
// Để hiển thị tên Customer:
// import laptopstore.model.Customer;
// import laptopstore.data.CustomerDataStore;

import javax.swing.table.AbstractTableModel;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
// import java.util.HashMap;
// import java.util.Map;

public class OrderTableModel extends AbstractTableModel {
    private List<Order> orders;
    // Đã bao gồm Net Amount, Tax
    private final String[] columnNames = {"Order ID", "Customer ID", "Payment ID", "Order Date", "Status", "Net Amount", "Tax", "Total Amount"};
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Ví dụ nếu muốn lấy tên Customer
    // private CustomerDataStore customerDs;
    // private Map<Integer, String> customerNameCache;

    public OrderTableModel(List<Order> orders) {
        this.orders = new ArrayList<>(orders != null ? orders : new ArrayList<>());
        // this.customerDs = new CustomerDataStore();
        // this.customerNameCache = new HashMap<>();
    }

    public OrderTableModel() {
        this.orders = new ArrayList<>();
        // this.customerDs = new CustomerDataStore();
        // this.customerNameCache = new HashMap<>();
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
            case 1: // Hiển thị Customer ID.
                // Để hiển thị tên Customer, tương tự như Category trong ProductTableModel,
                // bạn nên sửa OrderDataStore để JOIN và lấy tên Customer,
                // sau đó thêm trường customerName (hoặc Customer object) vào model Order.java.
                // Rồi ở đây chỉ cần: return order.getCustomerName();
                return order.getCustomerId(); // Tạm thời hiển thị ID
            case 2: return order.getPaymentId() == 0 ? "N/A" : order.getPaymentId();
            case 3: return order.getOrderDate() != null ? order.getOrderDate().format(dateFormatter) : "";
            case 4: return order.getStatus();
            case 5:
                BigDecimal netAmount = order.getNetAmount();
                return netAmount != null ? netAmount.setScale(2, RoundingMode.HALF_UP) : null;
            case 6:
                BigDecimal tax = order.getTax();
                return tax != null ? tax.setScale(2, RoundingMode.HALF_UP) : null;
            case 7:
                BigDecimal totalAmount = order.getTotalAmount();
                return totalAmount != null ? totalAmount.setScale(2, RoundingMode.HALF_UP) : null;
            default: return null;
        }
    }
    /*
    private String getCustomerNameFromDs(int customerId) {
        if (customerDs == null) return String.valueOf(customerId);
        if (customerNameCache.containsKey(customerId)) {
            return customerNameCache.get(customerId);
        }
        try {
            Customer customer = customerDs.getCustomerById(customerId);
            if (customer != null) {
                String name = customer.getFirstName() + " " + customer.getLastName();
                customerNameCache.put(customerId, name);
                return name;
            }
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
        return String.valueOf(customerId);
    }
    */

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0: // Order ID
            case 1: // Customer ID
            case 2: // Payment ID
                return Integer.class;
            case 5: // Net Amount
            case 6: // Tax
            case 7: // Total Amount
                return BigDecimal.class;
            default:
                return String.class;
        }
    }

    public void setOrders(List<Order> newOrders) {
        this.orders.clear();
        // if (customerNameCache != null) this.customerNameCache.clear();
        if (newOrders != null) {
            this.orders.addAll(newOrders);
        }
        fireTableDataChanged();
    }

    public Order getOrderAt(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < orders.size()) {
            return orders.get(rowIndex);
        }
        return null;
    }

    public void addOrderRow(Order order) {
        if (order != null) {
            orders.add(order);
            fireTableRowsInserted(orders.size() - 1, orders.size() - 1);
        }
    }

    public void removeOrderRow(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < orders.size()) {
            orders.remove(rowIndex);
            fireTableRowsDeleted(rowIndex, rowIndex);
        }
    }

    public void removeOrderRow(Order order) {
        int rowIndex = orders.indexOf(order);
        if (rowIndex != -1) {
            removeOrderRow(rowIndex);
        }
    }

    public void updateOrderRow(int rowIndex, Order order) {
        if (rowIndex >= 0 && rowIndex < orders.size() && order != null) {
            orders.set(rowIndex, order);
            fireTableRowsUpdated(rowIndex, rowIndex);
        }
    }
}
