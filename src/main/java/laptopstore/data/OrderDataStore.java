package laptopstore.data;

import laptopstore.model.Order;
import laptopstore.model.OrderItem;
// import laptopstore.model.Product; // Không cần trực tiếp ở đây nữa nếu OrderItem đã có productName
import laptopstore.util.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class OrderDataStore {

    public Order addOrder(Order order) throws SQLException {
        if (order == null) throw new IllegalArgumentException("Order object cannot be null.");
        if (order.getCustomerId() <= 0) throw new IllegalArgumentException("Customer ID is invalid for the order.");
        if (order.getOrderItems() == null || order.getOrderItems().isEmpty()) {
            throw new IllegalArgumentException("An order must have at least one item.");
        }

        order.calculateAndSetTotals(); // Đảm bảo tổng tiền được tính đúng với BigDecimal

        Connection conn = null;
        String insertOrderSql = "INSERT INTO ORDERS (customer_id, payment_id, order_date, status, net_amount, tax, total_amount, shipping_address, notes) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String insertOrderItemSql = "INSERT INTO ORDER_DETAILS (order_id, product_id, quantity, unit_price) " +
                "VALUES (?, ?, ?, ?)";
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement pstmtOrder = conn.prepareStatement(insertOrderSql, Statement.RETURN_GENERATED_KEYS)) {
                pstmtOrder.setInt(1, order.getCustomerId());

                if (order.getPaymentId() > 0) {
                    pstmtOrder.setInt(2, order.getPaymentId());
                } else {
                    pstmtOrder.setNull(2, Types.INTEGER);
                }

                pstmtOrder.setDate(3, order.getOrderDate() != null ? java.sql.Date.valueOf(order.getOrderDate()) : java.sql.Date.valueOf(LocalDate.now()));
                pstmtOrder.setString(4, order.getStatus() != null ? order.getStatus() : "Pending");

                pstmtOrder.setBigDecimal(5, order.getNetAmount());     // Đã là BigDecimal
                pstmtOrder.setBigDecimal(6, order.getTax());           // Đã là BigDecimal
                pstmtOrder.setBigDecimal(7, order.getTotalAmount());   // Đã là BigDecimal

                pstmtOrder.setString(8, order.getShippingAddress());
                pstmtOrder.setString(9, order.getNotes());

                int affectedRows = pstmtOrder.executeUpdate();
                if (affectedRows == 0) throw new SQLException("Creating order failed, no rows affected in ORDERS.");

                try (ResultSet generatedKeys = pstmtOrder.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        order.setOrderId(generatedKeys.getInt(1));
                    } else {
                        throw new SQLException("Creating order failed, no ID obtained for ORDERS.");
                    }
                }
            }

            if (!order.getOrderItems().isEmpty()) {
                try (PreparedStatement pstmtOrderItem = conn.prepareStatement(insertOrderItemSql, Statement.RETURN_GENERATED_KEYS)) {
                    for (OrderItem item : order.getOrderItems()) {
                        if (item.getProductId() <=0 || item.getQuantity() <=0 || item.getUnitPrice() == null || item.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
                            conn.rollback(); // Rollback nếu có item không hợp lệ
                            throw new SQLException("Invalid OrderItem data: " + item);
                        }
                        item.setOrderId(order.getOrderId());

                        pstmtOrderItem.setInt(1, item.getOrderId());
                        pstmtOrderItem.setInt(2, item.getProductId());
                        pstmtOrderItem.setInt(3, item.getQuantity());
                        pstmtOrderItem.setBigDecimal(4, item.getUnitPrice()); // Đã là BigDecimal

                        int itemAffectedRows = pstmtOrderItem.executeUpdate();
                        if (itemAffectedRows > 0) {
                            try (ResultSet itemGeneratedKeys = pstmtOrderItem.getGeneratedKeys()) {
                                if (itemGeneratedKeys.next()) {
                                    item.setOdId(itemGeneratedKeys.getInt(1));
                                } else {
                                    throw new SQLException("Creating order item failed, no OD_ID obtained for product_id: " + item.getProductId());
                                }
                            }
                        } else {
                            throw new SQLException("Creating order item failed (no rows affected) for product_id: " + item.getProductId());
                        }
                    }
                }
            }
            conn.commit();
            return order;

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException excep) { System.err.println("Error during rollback: " + excep.getMessage());}
            }
            System.err.println("SQL Error during addOrder transaction: " + e.getMessage());
            throw e;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { System.err.println("Error closing connection: " + e.getMessage());}
            }
        }
    }

    public Order getOrderById(int orderId) throws SQLException {
        if (orderId <= 0) return null;
        Order order = null;
        String orderSql = "SELECT o.*, c.first_name as customer_first_name, c.last_name as customer_last_name " +
                "FROM ORDERS o JOIN CUSTOMERS c ON o.customer_id = c.customer_id " +
                "WHERE o.order_id = ?";
        String orderItemsSql = "SELECT od.*, p.product_name as item_product_name " +
                "FROM ORDER_DETAILS od JOIN PRODUCTS p ON od.product_id = p.product_id " +
                "WHERE od.order_id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            try (PreparedStatement pstmtOrder = conn.prepareStatement(orderSql)) {
                pstmtOrder.setInt(1, orderId);
                try (ResultSet rsOrder = pstmtOrder.executeQuery()) {
                    if (rsOrder.next()) {
                        order = mapRowToOrder(rsOrder);
                        order.setCustomerName(rsOrder.getString("customer_first_name") + " " + rsOrder.getString("customer_last_name"));
                    }
                }
            }

            if (order != null) {
                List<OrderItem> items = new ArrayList<>();
                try (PreparedStatement pstmtItems = conn.prepareStatement(orderItemsSql)) {
                    pstmtItems.setInt(1, orderId);
                    try (ResultSet rsItems = pstmtItems.executeQuery()) {
                        while (rsItems.next()) {
                            OrderItem item = mapRowToOrderItem(rsItems);
                            item.setProductName(rsItems.getString("item_product_name"));
                            items.add(item);
                        }
                    }
                }
                order.setOrderItems(items);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi lấy order theo ID " + orderId + ": " + e.getMessage());
            throw e;
        }
        return order;
    }

    public boolean updateOrderStatus(int orderId, String newStatus) throws SQLException {
        if (orderId <= 0 || newStatus == null || newStatus.trim().isEmpty()) {
            throw new IllegalArgumentException("Order ID hoặc Status không hợp lệ.");
        }
        String sql = "UPDATE ORDERS SET status = ? WHERE order_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newStatus.trim());
            pstmt.setInt(2, orderId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi cập nhật trạng thái order ID " + orderId + ": " + e.getMessage());
            throw e;
        }
    }

    public boolean updateOrder(Order order) throws SQLException {
        if (order == null || order.getOrderId() <= 0) {
            throw new IllegalArgumentException("Order hoặc Order ID không hợp lệ để cập nhật.");
        }
        order.calculateAndSetTotals();

        String sql = "UPDATE ORDERS SET customer_id=?, payment_id=?, order_date=?, status=?, " +
                "net_amount=?, tax=?, total_amount=?, shipping_address=?, notes=? " +
                "WHERE order_id=?";
        // Cập nhật OrderItems cần logic phức tạp hơn (xóa cũ, thêm mới) và nên nằm trong transaction.
        // Phương thức này hiện chỉ cập nhật bảng ORDERS.
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, order.getCustomerId());
            if (order.getPaymentId() > 0) pstmt.setInt(2, order.getPaymentId());
            else pstmt.setNull(2, Types.INTEGER);
            pstmt.setDate(3, java.sql.Date.valueOf(order.getOrderDate()));
            pstmt.setString(4, order.getStatus());
            pstmt.setBigDecimal(5, order.getNetAmount());
            pstmt.setBigDecimal(6, order.getTax());
            pstmt.setBigDecimal(7, order.getTotalAmount());
            pstmt.setString(8, order.getShippingAddress());
            pstmt.setString(9, order.getNotes());
            pstmt.setInt(10, order.getOrderId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi cập nhật order ID " + order.getOrderId() + ": " + e.getMessage());
            throw e;
        }
    }

    public boolean deleteOrder(int orderId) throws SQLException {
        if (orderId <= 0) throw new IllegalArgumentException("Order ID không hợp lệ để xóa.");
        // ON DELETE CASCADE trên ORDER_DETAILS sẽ tự xóa items.
        String sql = "DELETE FROM ORDERS WHERE order_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, orderId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi xóa order ID " + orderId + ": " + e.getMessage());
            throw e;
        }
    }

    public List<Order> getAllOrders() throws SQLException {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.*, c.first_name as customer_first_name, c.last_name as customer_last_name " +
                "FROM ORDERS o JOIN CUSTOMERS c ON o.customer_id = c.customer_id " +
                "ORDER BY o.order_date DESC, o.order_id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Order order = mapRowToOrder(rs);
                order.setCustomerName(rs.getString("customer_first_name") + " " + rs.getString("customer_last_name"));
                // Không load OrderItems ở đây để tránh N+1. UI sẽ gọi getOrderById nếu cần chi tiết.
                orders.add(order);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi lấy tất cả orders: " + e.getMessage());
            throw e;
        }
        return orders;
    }

    private Order mapRowToOrder(ResultSet rs) throws SQLException {
        int id = rs.getInt("order_id");
        int customerId = rs.getInt("customer_id");
        Integer paymentIdObj = rs.getObject("payment_id", Integer.class); // Lấy Integer để xử lý NULL
        int paymentId = (paymentIdObj != null) ? paymentIdObj : 0; // Chuyển về int, 0 nếu null

        java.sql.Date orderDateSql = rs.getDate("order_date");
        LocalDate orderDate = (orderDateSql != null) ? orderDateSql.toLocalDate() : null;
        String status = rs.getString("status");
        BigDecimal netAmountBd = rs.getBigDecimal("net_amount");     // Lấy trực tiếp BigDecimal
        BigDecimal taxBd = rs.getBigDecimal("tax");                 // Lấy trực tiếp BigDecimal
        BigDecimal totalAmountBd = rs.getBigDecimal("total_amount"); // Lấy trực tiếp BigDecimal
        String shippingAddress = rs.getString("shipping_address");
        String notes = rs.getString("notes");

        // Sử dụng constructor của Order đã được cập nhật
        Order order = new Order(id, customerId, paymentId, orderDate, status, netAmountBd, taxBd, totalAmountBd, shippingAddress, notes);
        // customerName sẽ được set ở hàm gọi sau khi JOIN
        return order;
    }

    private OrderItem mapRowToOrderItem(ResultSet rs) throws SQLException {
        int odId = rs.getInt("od_id");
        int orderId = rs.getInt("order_id");
        int productId = rs.getInt("product_id");
        int quantity = rs.getInt("quantity");
        BigDecimal unitPriceBd = rs.getBigDecimal("unit_price"); // Lấy trực tiếp BigDecimal

        // Sử dụng constructor của OrderItem đã được cập nhật
        OrderItem item = new OrderItem(odId, orderId, productId, quantity, unitPriceBd);
        // productName sẽ được set ở hàm gọi sau khi JOIN
        return item;
    }
}
