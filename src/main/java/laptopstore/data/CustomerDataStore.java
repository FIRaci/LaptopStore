package laptopstore.data;

import laptopstore.model.Customer;
import laptopstore.util.DatabaseConnection;

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
import java.util.List;

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
            customer.setCreatedAt(createdAt); // Cập nhật lại vào model nếu nó được tạo là now()

            char gender = customer.getGender();
            if (gender == 'M' || gender == 'F' || gender == 'O') {
                pstmt.setString(6, String.valueOf(gender));
            } else {
                pstmt.setNull(6, Types.CHAR); // Hoặc giá trị mặc định nếu CSDL không cho phép NULL và không có default
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
            if ("23505".equals(e.getSQLState())) { // UNIQUE constraint violation
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
        // Thêm validate tương tự như addCustomer

        String sql = "UPDATE CUSTOMERS SET username = ?, email = ?, first_name = ?, last_name = ?, " +
                "gender = ?, address = ?, date_of_birth = ?, phone = ? " +
                // Không nên cho phép cập nhật created_at từ UI, trừ khi có lý do đặc biệt
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
            if ("23503".equals(e.getSQLState())) { // FK violation
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
