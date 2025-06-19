package laptopstore.data;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import laptopstore.model.Order;
import laptopstore.model.OrderItem;
import laptopstore.util.DatabaseConnection;

public class OrderDataStore {

    private final ProductDataStore productDb;

    public OrderDataStore() {
        this.productDb = new ProductDataStore();
    }

    public Order addOrder(Order order) throws SQLException {
        if (order == null) throw new IllegalArgumentException("Order object cannot be null.");
        if (order.getCustomerId() <= 0) throw new IllegalArgumentException("Customer ID is invalid for the order.");
        if (order.getOrderItems() == null || order.getOrderItems().isEmpty()) {
            throw new IllegalArgumentException("An order must have at least one item.");
        }
        order.calculateAndSetTotals(productDb);

        Connection conn = null;
        String insertOrderSql = "INSERT INTO ORDERS (customer_id, payment_id, order_date, status, net_amount, tax, total_amount, shipping_address, notes) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String insertOrderItemSql = "INSERT INTO ORDER_DETAILS (order_id, product_id, quantity) " +
                "VALUES (?, ?, ?)";
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
                pstmtOrder.setBigDecimal(5, order.getNetAmount());
                pstmtOrder.setBigDecimal(6, order.getTax());
                pstmtOrder.setBigDecimal(7, order.getTotalAmount());
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
                        if (item.getProductId() <= 0 || item.getQuantity() <= 0) {
                            conn.rollback();
                            throw new SQLException("Invalid OrderItem data (product_id or quantity): " + item);
                        }
                        item.setOrderId(order.getOrderId());
                        pstmtOrderItem.setInt(1, item.getOrderId());
                        pstmtOrderItem.setInt(2, item.getProductId());
                        pstmtOrderItem.setInt(3, item.getQuantity());
                        int itemAffectedRows = pstmtOrderItem.executeUpdate();
                        if (itemAffectedRows > 0) {
                            try (ResultSet itemGeneratedKeys = pstmtOrderItem.getGeneratedKeys()) {
                                if (itemGeneratedKeys.next()) {
                                    item.setOdId(itemGeneratedKeys.getInt(1));
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
        String orderItemsSql = "SELECT od.od_id, od.order_id, od.product_id, od.quantity, p.product_name as item_product_name " +
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
                order.calculateAndSetTotals(productDb);
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
        order.calculateAndSetTotals(productDb);

        Connection conn = null;
        String updateOrderSql = "UPDATE ORDERS SET customer_id=?, payment_id=?, order_date=?, status=?, " +
                "net_amount=?, tax=?, total_amount=?, shipping_address=?, notes=? " +
                "WHERE order_id=?";
        String deleteOrderItemsSql = "DELETE FROM ORDER_DETAILS WHERE order_id = ?";
        String insertOrderItemSql = "INSERT INTO ORDER_DETAILS (order_id, product_id, quantity) VALUES (?, ?, ?)";

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            try (PreparedStatement pstmtOrder = conn.prepareStatement(updateOrderSql)) {
                pstmtOrder.setInt(1, order.getCustomerId());
                if (order.getPaymentId() > 0) pstmtOrder.setInt(2, order.getPaymentId());
                else pstmtOrder.setNull(2, Types.INTEGER);
                pstmtOrder.setDate(3, java.sql.Date.valueOf(order.getOrderDate()));
                pstmtOrder.setString(4, order.getStatus());
                pstmtOrder.setBigDecimal(5, order.getNetAmount());
                pstmtOrder.setBigDecimal(6, order.getTax());
                pstmtOrder.setBigDecimal(7, order.getTotalAmount());
                pstmtOrder.setString(8, order.getShippingAddress());
                pstmtOrder.setString(9, order.getNotes());
                pstmtOrder.setInt(10, order.getOrderId());
                pstmtOrder.executeUpdate();
            }
            try (PreparedStatement pstmtDeleteItems = conn.prepareStatement(deleteOrderItemsSql)) {
                pstmtDeleteItems.setInt(1, order.getOrderId());
                pstmtDeleteItems.executeUpdate();
            }
            if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
                try (PreparedStatement pstmtInsertItem = conn.prepareStatement(insertOrderItemSql, Statement.RETURN_GENERATED_KEYS)) {
                    for (OrderItem item : order.getOrderItems()) {
                        if (item.getProductId() <= 0 || item.getQuantity() <= 0) {
                            conn.rollback();
                            throw new SQLException("Invalid OrderItem data for update: " + item);
                        }
                        item.setOrderId(order.getOrderId());
                        pstmtInsertItem.setInt(1, item.getOrderId());
                        pstmtInsertItem.setInt(2, item.getProductId());
                        pstmtInsertItem.setInt(3, item.getQuantity());
                        int itemAffectedRows = pstmtInsertItem.executeUpdate();
                        if (itemAffectedRows > 0) {
                            try (ResultSet itemGeneratedKeys = pstmtInsertItem.getGeneratedKeys()) {
                                if (itemGeneratedKeys.next()) {
                                    item.setOdId(itemGeneratedKeys.getInt(1));
                                }
                            }
                        } else {
                            throw new SQLException("Creating order item failed during update (no rows affected) for product_id: " + item.getProductId());
                        }
                    }
                }
            }
            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException excep) { System.err.println("Error during rollback: " + excep.getMessage());}
            }
            System.err.println("SQL Error during updateOrder transaction for order ID " + order.getOrderId() + ": " + e.getMessage());
            throw e;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { System.err.println("Error closing connection: " + e.getMessage());}
            }
        }
    }

    public boolean deleteOrder(int orderId) throws SQLException {
        if (orderId <= 0) throw new IllegalArgumentException("Order ID không hợp lệ để xóa.");
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
                orders.add(order);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi lấy tất cả orders: " + e.getMessage());
            throw e;
        }
        return orders;
    }

    public List<Map<String, Object>> getRevenueByCategory(LocalDate startDate, LocalDate endDate) throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();
        String sql = "SELECT " +
                "    cat.category_name, " +
                "    SUM(od.quantity * p.price) AS total_revenue_category, " +
                "    COUNT(DISTINCT o.order_id) AS total_orders_category " +
                "FROM ORDERS o " +
                "JOIN ORDER_DETAILS od ON o.order_id = od.order_id " +
                "JOIN PRODUCTS p ON od.product_id = p.product_id " +
                "JOIN CATEGORIES cat ON p.category_id = cat.category_id " +
                "WHERE o.order_date BETWEEN ? AND ? " +
                "GROUP BY cat.category_name " +
                "ORDER BY total_revenue_category DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDate(1, java.sql.Date.valueOf(startDate));
            pstmt.setDate(2, java.sql.Date.valueOf(endDate));
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("category_name", rs.getString("category_name"));
                    row.put("total_revenue_category", rs.getBigDecimal("total_revenue_category"));
                    row.put("total_orders_category", rs.getInt("total_orders_category"));
                    results.add(row);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi lấy doanh thu theo danh mục: " + e.getMessage());
            throw e;
        }
        return results;
    }

    public List<Map<String, Object>> getMonthlySalesTrend(LocalDate startDate, LocalDate endDate) throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();
        String sql = "SELECT " +
                "    TO_CHAR(o.order_date, 'YYYY-MM') AS sales_month, " +
                "    COUNT(o.order_id) AS monthly_orders, " +
                "    SUM(o.total_amount) AS monthly_revenue " +
                "FROM ORDERS o " +
                "WHERE o.order_date BETWEEN ? AND ? " +
                "GROUP BY sales_month " +
                "ORDER BY sales_month ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDate(1, java.sql.Date.valueOf(startDate));
            pstmt.setDate(2, java.sql.Date.valueOf(endDate));
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("sales_month", rs.getString("sales_month"));
                    row.put("monthly_orders", rs.getInt("monthly_orders"));
                    row.put("monthly_revenue", rs.getBigDecimal("monthly_revenue"));
                    results.add(row);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi lấy xu hướng bán hàng theo tháng: " + e.getMessage());
            throw e;
        }
        return results;
    }

    public List<Map<String, Object>> getFrequentlyBoughtTogether(int limit) throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();
        String sql = "WITH PairedProducts AS ( " +
                "    SELECT " +
                "        od1.product_id AS product1_id, " +
                "        p1.product_name AS product1_name, " +
                "        od2.product_id AS product2_id, " +
                "        p2.product_name AS product2_name, " +
                "        COUNT(DISTINCT od1.order_id) as times_bought_together " +
                "    FROM ORDER_DETAILS od1 " +
                "    JOIN ORDER_DETAILS od2 ON od1.order_id = od2.order_id AND od1.product_id < od2.product_id " +
                "    JOIN PRODUCTS p1 ON od1.product_id = p1.product_id " +
                "    JOIN PRODUCTS p2 ON od2.product_id = p2.product_id " +
                "    GROUP BY od1.product_id, p1.product_name, od2.product_id, p2.product_name " +
                ") " +
                "SELECT " +
                "    product1_name, " +
                "    product2_name, " +
                "    times_bought_together " +
                "FROM PairedProducts " +
                "ORDER BY times_bought_together DESC " +
                "LIMIT ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("product1_name", rs.getString("product1_name"));
                    row.put("product2_name", rs.getString("product2_name"));
                    row.put("times_bought_together", rs.getInt("times_bought_together"));
                    results.add(row);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi lấy sản phẩm thường mua cùng nhau: " + e.getMessage());
            throw e;
        }
        return results;
    }

    public List<Map<String, Object>> getOrderStatusDistribution() throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();
        String sql = "SELECT " +
                "    status, " +
                "    COUNT(*) AS count_status, " +
                "    ROUND((COUNT(*) * 100.0 / (SELECT COUNT(*) FROM ORDERS)), 2) AS percentage_status " +
                "FROM ORDERS " +
                "GROUP BY status " +
                "ORDER BY count_status DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("status", rs.getString("status"));
                row.put("count_status", rs.getInt("count_status"));
                row.put("percentage_status", rs.getBigDecimal("percentage_status"));
                results.add(row);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi lấy phân phối trạng thái đơn hàng: " + e.getMessage());
            throw e;
        }
        return results;
    }

    public List<Map<String, Object>> getRevenueByPaymentLinkStatus() throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();
        String sql = "SELECT " +
                "    CASE " +
                "        WHEN payment_id IS NOT NULL AND payment_id > 0 THEN 'With Payment Record' " +
                "        ELSE 'Without Payment Record' " +
                "    END AS payment_link_status, " +
                "    COUNT(order_id) AS number_of_orders, " +
                "    AVG(total_amount) AS average_order_value, " +
                "    SUM(total_amount) AS total_revenue " +
                "FROM ORDERS " +
                "GROUP BY payment_link_status";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("payment_link_status", rs.getString("payment_link_status"));
                row.put("number_of_orders", rs.getInt("number_of_orders"));
                row.put("average_order_value", rs.getBigDecimal("average_order_value"));
                row.put("total_revenue", rs.getBigDecimal("total_revenue"));
                results.add(row);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi so sánh doanh thu theo liên kết thanh toán: " + e.getMessage());
            throw e;
        }
        return results;
    }

    public List<Map<String, Object>> getTopProductsPerCategoryByRevenue(int limitPerCategory, LocalDate startDate, LocalDate endDate) throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();
        String sql = "WITH ProductRevenuePerCategory AS ( " +
                "    SELECT " +
                "        c.category_name, " +
                "        p.product_name, " +
                "        SUM(od.quantity * p.price) AS product_revenue_in_category, " +
                "        ROW_NUMBER() OVER (PARTITION BY c.category_id ORDER BY SUM(od.quantity * p.price) DESC) as product_rank_in_category " +
                "    FROM ORDERS o " +
                "    JOIN ORDER_DETAILS od ON o.order_id = od.order_id " +
                "    JOIN PRODUCTS p ON od.product_id = p.product_id " +
                "    JOIN CATEGORIES c ON p.category_id = c.category_id " +
                "    WHERE o.order_date BETWEEN ? AND ? " +
                "    GROUP BY c.category_id, c.category_name, p.product_id, p.product_name " +
                ") " +
                "SELECT category_name, product_rank_in_category, product_name, product_revenue_in_category " +
                "FROM ProductRevenuePerCategory " +
                "WHERE product_rank_in_category <= ? " +
                "ORDER BY category_name, product_rank_in_category";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDate(1, java.sql.Date.valueOf(startDate));
            pstmt.setDate(2, java.sql.Date.valueOf(endDate));
            pstmt.setInt(3, limitPerCategory);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("category_name", rs.getString("category_name"));
                    row.put("product_rank_in_category", rs.getInt("product_rank_in_category"));
                    row.put("product_name", rs.getString("product_name"));
                    row.put("product_revenue_in_category", rs.getBigDecimal("product_revenue_in_category"));
                    results.add(row);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi lấy top sản phẩm theo doanh thu/danh mục: " + e.getMessage());
            throw e;
        }
        return results;
    }

    public List<Map<String, Object>> getTopEmployeesPerCategoryBySales(int limitPerCategory, LocalDate startDate, LocalDate endDate) throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();
        String sql = "WITH EmployeeSalesPerCategory AS ( " +
                "    SELECT " +
                "        c.category_name, " +
                "        e.employee_id, " +
                "        e.first_name || ' ' || e.last_name AS employee_name, " +
                "        SUM(od.quantity * p.price) AS employee_sales_in_category, " +
                "        ROW_NUMBER() OVER (PARTITION BY c.category_id ORDER BY SUM(od.quantity * p.price) DESC) as employee_rank_in_category " +
                "    FROM ORDERS o " +
                "    JOIN PAYMENTS py ON o.payment_id = py.payment_id " +
                "    JOIN EMPLOYEES e ON py.employee_id = e.employee_id " +
                "    JOIN ORDER_DETAILS od ON o.order_id = od.order_id " +
                "    JOIN PRODUCTS p ON od.product_id = p.product_id " +
                "    JOIN CATEGORIES c ON p.category_id = c.category_id " +
                "    WHERE o.order_date BETWEEN ? AND ? " +
                "    GROUP BY c.category_id, c.category_name, e.employee_id, e.first_name, e.last_name " +
                ") " +
                "SELECT category_name, employee_rank_in_category, employee_name, employee_sales_in_category " +
                "FROM EmployeeSalesPerCategory " +
                "WHERE employee_rank_in_category <= ? " +
                "ORDER BY category_name, employee_rank_in_category";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDate(1, java.sql.Date.valueOf(startDate));
            pstmt.setDate(2, java.sql.Date.valueOf(endDate));
            pstmt.setInt(3, limitPerCategory);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("category_name", rs.getString("category_name"));
                    row.put("employee_rank_in_category", rs.getInt("employee_rank_in_category"));
                    row.put("employee_name", rs.getString("employee_name"));
                    row.put("employee_sales_in_category", rs.getBigDecimal("employee_sales_in_category"));
                    results.add(row);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi lấy top nhân viên theo doanh số/danh mục: " + e.getMessage());
            throw e;
        }
        return results;
    }

    public List<Map<String, Object>> getHighestValueOrderPerMonth(int year) throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();
        String sql = "WITH MonthlyMaxOrders AS ( " +
                "    SELECT DISTINCT ON (DATE_TRUNC('month', order_date)) " +
                "        o.order_id, " +
                "        o.order_date, " +
                "        o.total_amount, " +
                "        c.first_name || ' ' || c.last_name as customer_name, " +
                "        e.first_name || ' ' || e.last_name as employee_name, " +
                "        p.payment_method " +
                "    FROM ORDERS o " +
                "    JOIN CUSTOMERS c ON o.customer_id = c.customer_id " +
                "    JOIN PAYMENTS p ON o.payment_id = p.payment_id " +
                "    JOIN EMPLOYEES e ON p.employee_id = e.employee_id " +
                "    WHERE EXTRACT(YEAR FROM o.order_date) = ? " +
                "    ORDER BY DATE_TRUNC('month', order_date), o.total_amount DESC " +
                ") " +
                "SELECT * FROM MonthlyMaxOrders ORDER BY order_date";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, year);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("order_id", rs.getInt("order_id"));
                    row.put("order_date", rs.getDate("order_date"));
                    row.put("total_amount", rs.getBigDecimal("total_amount"));
                    row.put("customer_name", rs.getString("customer_name"));
                    row.put("employee_name", rs.getString("employee_name"));
                    row.put("payment_method", rs.getString("payment_method"));
                    results.add(row);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi lấy đơn hàng giá trị cao nhất mỗi tháng: " + e.getMessage());
            throw e;
        }
        return results;
    }

    public List<Map<String, Object>> getUnpaidOrdersForSpecificMaleCustomers(String namePatternFragment) throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();
        String sql = "SELECT o.* " +
                "FROM ORDERS o " +
                "JOIN CUSTOMERS c ON o.customer_id = c.customer_id " +
                "WHERE o.payment_id IS NULL " + 
                "AND c.gender = 'M' " +
                "AND (c.first_name ILIKE ? OR c.last_name ILIKE ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String likePattern = "%" + namePatternFragment + "%";
            pstmt.setString(1, likePattern);
            pstmt.setString(2, likePattern);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("order_id", rs.getInt("order_id"));
                    row.put("customer_id", rs.getInt("customer_id")); 
                    row.put("payment_id", rs.getObject("payment_id"));
                    row.put("order_date", rs.getDate("order_date"));
                    row.put("status", rs.getString("status"));
                    row.put("net_amount", rs.getBigDecimal("net_amount")); 
                    row.put("tax", rs.getBigDecimal("tax"));
                    row.put("total_amount", rs.getBigDecimal("total_amount"));
                    row.put("shipping_address", rs.getString("shipping_address"));
                    row.put("notes", rs.getString("notes"));
                    results.add(row);
                }
            }
        }
        return results;
    }

    // NQ13: Các đơn hàng có tổng giá trị trên X
    public List<Order> getOrdersWithValueGreaterThan(BigDecimal amountThreshold) throws SQLException {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.*, c.first_name as customer_first_name, c.last_name as customer_last_name " +
                "FROM ORDERS o " +
                "JOIN CUSTOMERS c ON o.customer_id = c.customer_id " +
                "WHERE o.total_amount > ? " +
                "ORDER BY o.total_amount DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBigDecimal(1, amountThreshold);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Order order = mapRowToOrder(rs);
                    order.setCustomerName(rs.getString("customer_first_name") + " " + rs.getString("customer_last_name"));
                    orders.add(order);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi lấy đơn hàng giá trị lớn: " + e.getMessage());
            throw e;
        }
        return orders;
    }

    // NQ17: Số lượng đơn hàng theo trạng thái trong N năm gần nhất (Sửa đổi)
    public List<Map<String, Object>> getOrderCountByStatusForLastNYears(int years) throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();
        String sql = "SELECT status, COUNT(order_id) AS order_count " + // Đổi tên cột count
                "FROM ORDERS " +
                "WHERE order_date >= (CURRENT_DATE - CAST(? || ' years' AS INTERVAL)) " +
                "GROUP BY status " +
                "ORDER BY status";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, years);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("status", rs.getString("status"));
                    row.put("order_count", rs.getInt("order_count"));
                    results.add(row);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi lấy số lượng đơn hàng trong " + years + " năm theo trạng thái: " + e.getMessage());
            throw e;
        }
        return results;
    }


    // NQ20: Tổng doanh thu trong N năm qua (theo từng năm)
    public List<Map<String, Object>> getTotalRevenueForLastNYearsByYear(int years) throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();
        String sql = "SELECT EXTRACT(YEAR FROM order_date) AS sales_year, SUM(total_amount) AS yearly_revenue " +
                "FROM ORDERS " +
                "WHERE order_date >= date_trunc('year', CURRENT_DATE - CAST((? - 1 || ' years') AS INTERVAL)) " +
                "  AND order_date < date_trunc('year', CURRENT_DATE + INTERVAL '1 year') " +
                "GROUP BY EXTRACT(YEAR FROM order_date) " +
                "ORDER BY sales_year DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, years);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("sales_year", rs.getInt("sales_year"));
                    row.put("yearly_revenue", rs.getBigDecimal("yearly_revenue"));
                    results.add(row);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi lấy tổng doanh thu " + years + " năm qua theo năm: " + e.getMessage());
            throw e;
        }
        return results;
    }
    // NQ22: Tháng có doanh thu cao nhất trong năm
    public List<Map<String, Object>> getMonthWithHighestRevenue(int year) throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();
        String sql = "SELECT EXTRACT(MONTH FROM order_date) AS month, SUM(total_amount) AS total_revenue " +
                "FROM ORDERS " +
                "WHERE EXTRACT(YEAR FROM order_date) = ? " +
                "GROUP BY EXTRACT(MONTH FROM order_date) " +
                "ORDER BY total_revenue DESC " +
                "LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, year);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("month", rs.getInt("month"));
                    row.put("total_revenue", rs.getBigDecimal("total_revenue"));
                    results.add(row);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi lấy tháng có doanh thu cao nhất trong năm " + year + ": " + e.getMessage());
            throw e;
        }
        return results;
    }

    // NQ26: Số lượng khách hàng mua theo từng thương hiệu
    public List<Map<String, Object>> getCustomerCountByBrand() throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();
        String sql = "SELECT p.brand, COUNT(DISTINCT o.customer_id) AS customer_count " +
                "FROM ORDERS o " +
                "JOIN ORDER_DETAILS od ON o.order_id = od.order_id " +
                "JOIN PRODUCTS p ON od.product_id = p.product_id " +
                "GROUP BY p.brand " +
                "ORDER BY customer_count DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("brand", rs.getString("brand"));
                row.put("customer_count", rs.getInt("customer_count"));
                results.add(row);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi lấy số lượng khách hàng mua theo thương hiệu: " + e.getMessage());
            throw e;
        }
        return results;
    }


    private Order mapRowToOrder(ResultSet rs) throws SQLException {
        int id = rs.getInt("order_id");
        int customerId = rs.getInt("customer_id");
        Integer paymentIdObj = rs.getObject("payment_id", Integer.class);
        int paymentId = (paymentIdObj != null) ? paymentIdObj : 0;
        java.sql.Date orderDateSql = rs.getDate("order_date");
        LocalDate orderDate = (orderDateSql != null) ? orderDateSql.toLocalDate() : null;
        String status = rs.getString("status");
        BigDecimal netAmountBd = rs.getBigDecimal("net_amount");
        BigDecimal taxBd = rs.getBigDecimal("tax");
        BigDecimal totalAmountBd = rs.getBigDecimal("total_amount");
        String shippingAddress = rs.getString("shipping_address");
        String notes = rs.getString("notes");
        return new Order(id, customerId, paymentId, orderDate, status, netAmountBd, taxBd, totalAmountBd, shippingAddress, notes);
    }

    private OrderItem mapRowToOrderItem(ResultSet rs) throws SQLException {
        int odId = rs.getInt("od_id");
        int orderId = rs.getInt("order_id");
        int productId = rs.getInt("product_id");
        int quantity = rs.getInt("quantity");
        return new OrderItem(odId, orderId, productId, quantity);
    }
}
