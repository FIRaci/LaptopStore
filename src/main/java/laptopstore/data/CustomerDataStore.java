package laptopstore.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import laptopstore.model.Customer;
import laptopstore.util.DatabaseConnection;

public class CustomerDataStore {

    public Customer addCustomer(Customer customer) throws SQLException {
        if (customer == null) throw new IllegalArgumentException("Customer object cannot be null.");
        if (customer.getUsername() == null || customer.getUsername().trim().isEmpty()) throw new IllegalArgumentException("Username is required.");
        if (customer.getEmail() == null || customer.getEmail().trim().isEmpty()) throw new IllegalArgumentException("Email is required.");
        if (customer.getFirstName() == null || customer.getFirstName().trim().isEmpty()) throw new IllegalArgumentException("First name is required.");
        if (customer.getLastName() == null || customer.getLastName().trim().isEmpty()) throw new IllegalArgumentException("Last name is required.");

        String sql = "INSERT INTO CUSTOMERS (username, email, first_name, last_name, created_at, gender, address, date_of_birth, phone) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, customer.getUsername().trim());
            pstmt.setString(2, customer.getEmail().trim());
            pstmt.setString(3, customer.getFirstName().trim());
            pstmt.setString(4, customer.getLastName().trim());
            LocalDateTime createdAt = customer.getCreatedAt() != null ? customer.getCreatedAt() : LocalDateTime.now();
            pstmt.setTimestamp(5, Timestamp.valueOf(createdAt));
            customer.setCreatedAt(createdAt);
            char gender = customer.getGender();
            if (gender == 'M' || gender == 'F' || gender == 'O') {
                pstmt.setString(6, String.valueOf(gender));
            } else {
                pstmt.setNull(6, Types.CHAR);
            }
            pstmt.setString(7, customer.getAddress());
            if (customer.getDateOfBirth() != null) {
                pstmt.setDate(8, java.sql.Date.valueOf(customer.getDateOfBirth()));
            } else {
                pstmt.setNull(8, Types.DATE);
            }
            pstmt.setString(9, customer.getPhone());
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        customer.setCustomerId(generatedKeys.getInt(1));
                        return customer;
                    }
                }
            }
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                if (e.getMessage().toLowerCase().contains("username")) {
                    throw new SQLException("Username '" + customer.getUsername().trim() + "' đã tồn tại.", e.getSQLState(), e);
                } else if (e.getMessage().toLowerCase().contains("email")) {
                    throw new SQLException("Email '" + customer.getEmail().trim() + "' đã tồn tại.", e.getSQLState(), e);
                }
            }
            System.err.println("Lỗi SQL khi thêm khách hàng: " + e.getMessage());
            throw e;
        }
        return null;
    }

    public boolean updateCustomer(Customer customer) throws SQLException {
        if (customer == null) throw new IllegalArgumentException("Customer object cannot be null.");
        if (customer.getCustomerId() <= 0) throw new IllegalArgumentException("Customer ID không hợp lệ để cập nhật.");
        String sql = "UPDATE CUSTOMERS SET username = ?, email = ?, first_name = ?, last_name = ?, " +
                "gender = ?, address = ?, date_of_birth = ?, phone = ? " +
                "WHERE customer_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, customer.getUsername().trim());
            pstmt.setString(2, customer.getEmail().trim());
            pstmt.setString(3, customer.getFirstName().trim());
            pstmt.setString(4, customer.getLastName().trim());
            char gender = customer.getGender();
            if (gender == 'M' || gender == 'F' || gender == 'O') {
                pstmt.setString(5, String.valueOf(gender));
            } else {
                pstmt.setNull(5, Types.CHAR);
            }
            pstmt.setString(6, customer.getAddress());
            if (customer.getDateOfBirth() != null) {
                pstmt.setDate(7, java.sql.Date.valueOf(customer.getDateOfBirth()));
            } else {
                pstmt.setNull(7, Types.DATE);
            }
            pstmt.setString(8, customer.getPhone());
            pstmt.setInt(9, customer.getCustomerId());
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                if (e.getMessage().toLowerCase().contains("username")) {
                    throw new SQLException("Username '" + customer.getUsername().trim() + "' đã được sử dụng bởi khách hàng khác.", e.getSQLState(), e);
                } else if (e.getMessage().toLowerCase().contains("email")) {
                    throw new SQLException("Email '" + customer.getEmail().trim() + "' đã được sử dụng bởi khách hàng khác.", e.getSQLState(), e);
                }
            }
            System.err.println("Lỗi SQL khi cập nhật khách hàng: " + e.getMessage());
            throw e;
        }
    }

    public boolean deleteCustomer(int customerId) throws SQLException {
        if (customerId <= 0) throw new IllegalArgumentException("Customer ID không hợp lệ để xóa.");
        String sql = "DELETE FROM CUSTOMERS WHERE customer_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, customerId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            if ("23503".equals(e.getSQLState())) {
                throw new SQLException("Không thể xóa khách hàng ID " + customerId + " vì có đơn hàng hoặc dữ liệu liên quan khác.", e.getSQLState(), e);
            }
            System.err.println("Lỗi SQL khi xóa khách hàng ID " + customerId + ": " + e.getMessage());
            throw e;
        }
    }

    public Customer getCustomerById(int customerId) throws SQLException {
        if (customerId <= 0) return null;
        String sql = "SELECT customer_id, username, email, first_name, last_name, created_at, gender, address, date_of_birth, phone FROM CUSTOMERS WHERE customer_id = ?";
        Customer customer = null;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, customerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    customer = mapRowToCustomer(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi lấy khách hàng theo ID " + customerId + ": " + e.getMessage());
            throw e;
        }
        return customer;
    }

    public List<Customer> getAllCustomers() throws SQLException {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT customer_id, username, email, first_name, last_name, created_at, gender, address, date_of_birth, phone FROM CUSTOMERS ORDER BY last_name, first_name, customer_id";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                customers.add(mapRowToCustomer(rs));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi lấy tất cả khách hàng: " + e.getMessage());
            throw e;
        }
        return customers;
    }

    public List<Customer> getLatestCustomers(int limit) throws SQLException {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT customer_id, username, email, first_name, last_name, created_at, " +
                "gender, address, date_of_birth, phone " +
                "FROM CUSTOMERS ORDER BY created_at DESC, customer_id DESC LIMIT ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    customers.add(mapRowToCustomer(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching latest " + limit + " customers: " + e.getMessage());
            throw e;
        }
        return customers;
    }

    public List<Customer> getMaleCustomers() throws SQLException {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT * FROM CUSTOMERS WHERE gender = 'M' ORDER BY last_name, first_name";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    customers.add(mapRowToCustomer(rs));
                }
            }
        }
        return customers;
    }

    public List<Customer> getCustomersNameStartsWith(String prefix) throws SQLException {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT * FROM CUSTOMERS WHERE first_name LIKE ? ORDER BY last_name, first_name";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, prefix + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    customers.add(mapRowToCustomer(rs));
                }
            }
        }
        return customers;
    }

    public List<Customer> getTopCustomersByOrders() throws SQLException {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT c.*, COUNT(o.order_id) as order_count_for_ranking " +
                "FROM customers c " +
                "LEFT JOIN orders o ON c.customer_id = o.customer_id " +
                "GROUP BY c.customer_id " +
                "ORDER BY order_count_for_ranking DESC " +
                "LIMIT 5";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                customers.add(mapRowToCustomer(rs));
            }
        }
        return customers;
    }

    public List<Customer> getCustomersWithNoOrders() throws SQLException {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT c.* FROM customers c " +
                "LEFT JOIN orders o ON c.customer_id = o.customer_id " +
                "WHERE o.order_id IS NULL";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                customers.add(mapRowToCustomer(rs));
            }
        }
        return customers;
    }

    public List<Customer> getTopGamingLaptopCustomers() throws SQLException {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT c.*, COUNT(DISTINCT o.order_id) as gaming_orders_for_ranking " +
                "FROM customers c " +
                "JOIN orders o ON c.customer_id = o.customer_id " +
                "JOIN order_details od ON o.order_id = od.order_id " +
                "JOIN products p ON od.product_id = p.product_id " +
                "WHERE p.category_id = 1 " +
                "GROUP BY c.customer_id " +
                "ORDER BY gaming_orders_for_ranking DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                customers.add(mapRowToCustomer(rs));
            }
        }
        return customers;
    }

    public List<Customer> getHighSpendingCustomers() throws SQLException {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT DISTINCT c.* FROM customers c " +
                "JOIN orders o ON c.customer_id = o.customer_id " +
                "WHERE o.total_amount > 5000000";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                customers.add(mapRowToCustomer(rs));
            }
        }
        return customers;
    }

    public List<Customer> getCustomersWithPendingOrders() throws SQLException {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT DISTINCT c.* FROM customers c " +
                "JOIN orders o ON c.customer_id = o.customer_id " +
                "WHERE o.status IN ('Pending', 'Processing')";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                customers.add(mapRowToCustomer(rs));
            }
        }
        return customers;
    }

    public List<Map<String, Object>> getMostLoyalCustomers(int minOrders, int limit) throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();
        String sql = "SELECT " +
                "    cus.customer_id, " +
                "    cus.first_name, " +
                "    cus.last_name, " +
                "    cus.email, " +
                "    COUNT(o.order_id) AS total_orders, " +
                "    SUM(o.total_amount) AS total_spent, " +
                "    AVG(o.total_amount) AS average_order_value " +
                "FROM CUSTOMERS cus " +
                "JOIN ORDERS o ON cus.customer_id = o.customer_id " +
                "GROUP BY cus.customer_id, cus.first_name, cus.last_name, cus.email " +
                "HAVING COUNT(o.order_id) >= ? " +
                "ORDER BY total_spent DESC, total_orders DESC " +
                "LIMIT ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, minOrders);
            pstmt.setInt(2, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("customer_id", rs.getInt("customer_id"));
                    row.put("first_name", rs.getString("first_name"));
                    row.put("last_name", rs.getString("last_name"));
                    row.put("email", rs.getString("email"));
                    row.put("total_orders", rs.getInt("total_orders"));
                    row.put("total_spent", rs.getBigDecimal("total_spent"));
                    row.put("average_order_value", rs.getBigDecimal("average_order_value"));
                    results.add(row);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi lấy khách hàng trung thành: " + e.getMessage());
            throw e;
        }
        return results;
    }

    public List<Map<String, Object>> getInactiveCustomers(int monthsInactive) throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();
        String sql = "SELECT " +
                "    c.customer_id, " +
                "    c.first_name, " +
                "    c.last_name, " +
                "    c.email, " +
                "    MAX(o.order_date) AS last_order_date " +
                "FROM CUSTOMERS c " +
                "LEFT JOIN ORDERS o ON c.customer_id = o.customer_id " +
                "GROUP BY c.customer_id, c.first_name, c.last_name, c.email " +
                "HAVING MAX(o.order_date) IS NULL OR MAX(o.order_date) < (CURRENT_DATE - CAST(? || ' months' AS INTERVAL)) " +
                "ORDER BY last_order_date ASC NULLS FIRST, c.last_name";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, monthsInactive);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("customer_id", rs.getInt("customer_id"));
                    row.put("first_name", rs.getString("first_name"));
                    row.put("last_name", rs.getString("last_name"));
                    row.put("email", rs.getString("email"));
                    row.put("last_order_date", rs.getDate("last_order_date"));
                    results.add(row);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi lấy khách hàng không hoạt động: " + e.getMessage());
            throw e;
        }
        return results;
    }

    public List<Map<String, Object>> findCustomersByAgeAndOrderCriteria(
            int minAge, int maxAge, int minOrders, LocalDate ordersStartDate, LocalDate ordersEndDate) throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();
        String sql = "SELECT " +
                "    c.first_name || ' ' || c.last_name as customer_name, " +
                "    EXTRACT(YEAR FROM AGE(CURRENT_DATE, c.date_of_birth)) as age, " +
                "    COUNT(DISTINCT o.order_id) as total_orders, " +
                "    SUM(o.total_amount) as total_spent, " +
                "    MAX(o.order_date) as last_order_date " +
                "FROM CUSTOMERS c " +
                "JOIN ORDERS o ON c.customer_id = o.customer_id " +
                "WHERE EXTRACT(YEAR FROM AGE(CURRENT_DATE, c.date_of_birth)) BETWEEN ? AND ? " +
                "AND o.order_date BETWEEN ? AND ? " +
                "GROUP BY c.customer_id, c.first_name, c.last_name, c.date_of_birth " +
                "HAVING COUNT(DISTINCT o.order_id) >= ? " ;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, minAge);
            pstmt.setInt(2, maxAge);
            pstmt.setDate(3, java.sql.Date.valueOf(ordersStartDate));
            pstmt.setDate(4, java.sql.Date.valueOf(ordersEndDate));
            pstmt.setInt(5, minOrders);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("customer_name", rs.getString("customer_name"));
                    row.put("age", rs.getInt("age"));
                    row.put("total_orders", rs.getInt("total_orders"));
                    row.put("total_spent", rs.getBigDecimal("total_spent"));
                    row.put("last_order_date", rs.getDate("last_order_date"));
                    results.add(row);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi tìm khách hàng theo tuổi và tiêu chí đơn hàng: " + e.getMessage());
            throw e;
        }
        return results;
    }

    public List<Map<String, Object>> getTopSpendersInTopCategories(int categoryLimit, int customerLimitPerCategory) throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();
        String sql = "WITH CustomerCategorySpending AS (" +
                "    SELECT " +
                "        c.customer_id, " +
                "        c.first_name || ' ' || c.last_name as customer_name, " +
                "        cat.category_name, " +
                "        SUM(od.quantity * p.price) as total_spent, " +
                "        ROW_NUMBER() OVER (PARTITION BY cat.category_name ORDER BY SUM(od.quantity * p.price) DESC) as rn " +
                "    FROM CUSTOMERS c " +
                "    JOIN ORDERS o ON c.customer_id = o.customer_id " +
                "    JOIN ORDER_DETAILS od ON o.order_id = od.order_id " +
                "    JOIN PRODUCTS p ON od.product_id = p.product_id " +
                "    JOIN CATEGORIES cat ON p.category_id = cat.category_id " +
                "    WHERE o.order_date BETWEEN '2024-01-01' AND '2024-12-31' " +
                "    GROUP BY c.customer_id, c.first_name, c.last_name, cat.category_name " +
                ") " +
                "SELECT " +
                "    category_name, " +
                "    customer_name, " +
                "    total_spent " +
                "FROM CustomerCategorySpending " +
                "WHERE rn = 1 " +
                "ORDER BY total_spent DESC " +
                "LIMIT 15";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("category_name", rs.getString("category_name"));
                row.put("customer_name", rs.getString("customer_name")); 
                row.put("amount_spent_in_category", rs.getBigDecimal("total_spent"));
                results.add(row);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi lấy top khách hàng chi tiêu/top danh mục: " + e.getMessage());
            throw e;
        }
        return results;
    }

    // NQ12: Số lượng khách hàng mới đăng ký theo N năm trở lại đây (theo tháng-năm).
    public List<Map<String, Object>> getNewCustomersByMonthForLastNYears(int years) throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();
        String sql = "SELECT TO_CHAR(created_at, 'YYYY-MM') AS registration_month_year, COUNT(customer_id) AS new_customer_count " +
                "FROM CUSTOMERS " +
                "WHERE created_at >= (CURRENT_DATE - CAST(? || ' years' AS INTERVAL)) " +
                "GROUP BY TO_CHAR(created_at, 'YYYY-MM') " +
                "ORDER BY registration_month_year";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, years);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("registration_month_year", rs.getString("registration_month_year"));
                    row.put("new_customer_count", rs.getInt("new_customer_count"));
                    results.add(row);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi lấy khách hàng đăng ký mới trong " + years + " năm theo tháng: " + e.getMessage());
            throw e;
        }
        return results;
    }

    // NQ18: Khách hàng có địa chỉ chứa một chuỗi cụ thể (ví dụ 'Green Valley')
    public List<Customer> getCustomersByAddressContaining(String addressPattern) throws SQLException {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT * FROM CUSTOMERS WHERE address ILIKE ? ORDER BY last_name, first_name";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + addressPattern + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    customers.add(mapRowToCustomer(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi lấy khách hàng theo địa chỉ chứa chuỗi '" + addressPattern + "': " + e.getMessage());
            throw e;
        }
        return customers;
    }

    // NQ21: Khách hàng có sinh nhật trong tháng hiện tại
    public List<Customer> getCustomersWithBirthdayThisMonth() throws SQLException {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT customer_id, username, email, first_name, last_name, created_at, gender, address, date_of_birth, phone " +
                "FROM CUSTOMERS " +
                "WHERE EXTRACT(MONTH FROM date_of_birth) = EXTRACT(MONTH FROM CURRENT_DATE) " +
                "ORDER BY date_of_birth ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                customers.add(mapRowToCustomer(rs));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi lấy khách hàng có sinh nhật trong tháng hiện tại: " + e.getMessage());
            throw e;
        }
        return customers;
    }

    // NQ25: Khách hàng mua sản phẩm từ ít nhất 3 thương hiệu khác nhau
    public List<Map<String, Object>> getCustomersWithMinBrandsPurchased(int minBrands, int limit) throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();
        String sql = "SELECT c.customer_id, c.first_name, c.last_name, COUNT(DISTINCT p.brand) AS brand_count " +
                "FROM CUSTOMERS c " +
                "JOIN ORDERS o ON c.customer_id = o.customer_id " +
                "JOIN ORDER_DETAILS od ON o.order_id = od.order_id " +
                "JOIN PRODUCTS p ON od.product_id = p.product_id " +
                "GROUP BY c.customer_id, c.first_name, c.last_name " +
                "HAVING COUNT(DISTINCT p.brand) >= ? " +
                "ORDER BY brand_count DESC " +
                "LIMIT ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, minBrands);
            pstmt.setInt(2, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("customer_id", rs.getInt("customer_id"));
                    row.put("first_name", rs.getString("first_name"));
                    row.put("last_name", rs.getString("last_name"));
                    row.put("brand_count", rs.getInt("brand_count"));
                    results.add(row);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi lấy khách hàng mua từ ít nhất " + minBrands + " thương hiệu: " + e.getMessage());
            throw e;
        }
        return results;
    }

    // NQ28: Top 3 khách hàng có tổng số tiền chi cao nhất
    public List<Map<String, Object>> getTopCustomersByTotalSpent(int topN) throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();
        String sql = "SELECT c.customer_id, c.first_name, c.last_name, SUM(o.total_amount) AS total_spent " +
                "FROM CUSTOMERS c " +
                "JOIN ORDERS o ON c.customer_id = o.customer_id " +
                "GROUP BY c.customer_id, c.first_name, c.last_name " +
                "ORDER BY total_spent DESC " +
                "LIMIT ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, topN);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("customer_id", rs.getInt("customer_id"));
                    row.put("first_name", rs.getString("first_name"));
                    row.put("last_name", rs.getString("last_name"));
                    row.put("total_spent", rs.getBigDecimal("total_spent"));
                    results.add(row);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi lấy top " + topN + " khách hàng chi tiêu cao nhất: " + e.getMessage());
            throw e;
        }
        return results;
    }

    // NQ30: Khách hàng chưa mua sản phẩm thuộc danh mục 'Office'
    public List<Customer> getCustomersNotPurchasingCategory(String categoryName) throws SQLException {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT c.customer_id, c.username, c.email, c.first_name, c.last_name, c.created_at, c.gender, c.address, c.date_of_birth, c.phone " +
                "FROM CUSTOMERS c " +
                "WHERE NOT EXISTS (" +
                "    SELECT 1 " +
                "    FROM ORDERS o " +
                "    JOIN ORDER_DETAILS od ON o.order_id = od.order_id " +
                "    JOIN PRODUCTS p ON od.product_id = p.product_id " +
                "    JOIN CATEGORIES cat ON p.category_id = cat.category_id " +
                "    WHERE o.customer_id = c.customer_id " +
                "    AND cat.category_name = ?" +
                ") " +
                "ORDER BY c.customer_id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, categoryName);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    customers.add(mapRowToCustomer(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi lấy khách hàng chưa mua sản phẩm từ danh mục '" + categoryName + "': " + e.getMessage());
            throw e;
        }
        return customers;
    }


    private Customer mapRowToCustomer(ResultSet rs) throws SQLException {
        int id = rs.getInt("customer_id");
        String username = rs.getString("username");
        String email = rs.getString("email");
        String firstName = rs.getString("first_name");
        String lastName = rs.getString("last_name");
        Timestamp createdAtTs = rs.getTimestamp("created_at");
        LocalDateTime createdAt = (createdAtTs != null) ? createdAtTs.toLocalDateTime() : null;
        String genderStr = rs.getString("gender");
        char gender = (genderStr != null && !genderStr.isEmpty()) ? genderStr.charAt(0) : '\0';
        String address = rs.getString("address");
        java.sql.Date dobSql = rs.getDate("date_of_birth");
        LocalDate dateOfBirth = (dobSql != null) ? dobSql.toLocalDate() : null;
        String phone = rs.getString("phone");
        return new Customer(id, username, email, firstName, lastName, createdAt, gender, address, dateOfBirth, phone);
    }
}
