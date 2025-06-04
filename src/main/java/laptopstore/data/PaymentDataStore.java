package laptopstore.data;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import laptopstore.model.Payment;
import laptopstore.util.DatabaseConnection;

public class PaymentDataStore {

    // ... (Các phương thức add, update, delete, getById, getAll, ... và các truy vấn cũ giữ nguyên)
    public Payment addPayment(Payment payment) throws SQLException {
        if (payment == null) throw new IllegalArgumentException("Payment object cannot be null.");
        if (payment.getPaymentMethod() == null || payment.getPaymentMethod().trim().isEmpty()) throw new IllegalArgumentException("Payment method is required.");
        if (payment.getTotalAmount() == null || payment.getTotalAmount().compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("Total amount must be non-negative.");
        String sql = "INSERT INTO PAYMENTS (employee_id, payment_date, payment_method, total_amount, notes) " +
                "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (payment.getEmployeeId() > 0) {
                pstmt.setInt(1, payment.getEmployeeId());
            } else {
                pstmt.setNull(1, Types.INTEGER);
            }
            LocalDateTime paymentDate = payment.getPaymentDate() != null ? payment.getPaymentDate() : LocalDateTime.now();
            pstmt.setTimestamp(2, Timestamp.valueOf(paymentDate));
            payment.setPaymentDate(paymentDate);
            pstmt.setString(3, payment.getPaymentMethod().trim());
            pstmt.setBigDecimal(4, payment.getTotalAmount());
            pstmt.setString(5, payment.getNotes());
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        payment.setPaymentId(generatedKeys.getInt(1));
                        return payment;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi thêm payment: " + e.getMessage());
            throw e;
        }
        return null;
    }

    public boolean updatePayment(Payment payment) throws SQLException {
        if (payment == null) throw new IllegalArgumentException("Payment object cannot be null.");
        if (payment.getPaymentId() <= 0) throw new IllegalArgumentException("Payment ID không hợp lệ để cập nhật.");
        String sql = "UPDATE PAYMENTS SET employee_id=?, payment_date=?, payment_method=?, total_amount=?, notes=? " +
                "WHERE payment_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (payment.getEmployeeId() > 0) {
                pstmt.setInt(1, payment.getEmployeeId());
            } else {
                pstmt.setNull(1, Types.INTEGER);
            }
            if (payment.getPaymentDate() != null) {
                pstmt.setTimestamp(2, Timestamp.valueOf(payment.getPaymentDate()));
            } else {
                pstmt.setNull(2, Types.TIMESTAMP);
            }
            pstmt.setString(3, payment.getPaymentMethod().trim());
            pstmt.setBigDecimal(4, payment.getTotalAmount());
            pstmt.setString(5, payment.getNotes());
            pstmt.setInt(6, payment.getPaymentId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi cập nhật payment: " + e.getMessage());
            throw e;
        }
    }

    public boolean deletePayment(int paymentId) throws SQLException {
        if (paymentId <= 0) throw new IllegalArgumentException("Payment ID không hợp lệ để xóa.");
        String sql = "DELETE FROM PAYMENTS WHERE payment_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, paymentId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi xóa payment ID " + paymentId + ": " + e.getMessage());
            throw e;
        }
    }

    public Payment getPaymentById(int paymentId) throws SQLException {
        if (paymentId <= 0) return null;
        String sql = "SELECT p.payment_id, p.employee_id, p.payment_date, p.payment_method, p.total_amount, p.notes, " +
                "e.first_name as emp_first_name, e.last_name as emp_last_name " +
                "FROM PAYMENTS p LEFT JOIN EMPLOYEES e ON p.employee_id = e.employee_id " +
                "WHERE p.payment_id = ?";
        Payment payment = null;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, paymentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    payment = mapRowToPayment(rs);
                    if (rs.getString("emp_first_name") != null) {
                        payment.setEmployeeName(rs.getString("emp_first_name") + " " + rs.getString("emp_last_name"));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi lấy payment theo ID " + paymentId + ": " + e.getMessage());
            throw e;
        }
        return payment;
    }

    public List<Payment> getAllPayments() throws SQLException {
        List<Payment> payments = new ArrayList<>();
        String sql = "SELECT p.payment_id, p.employee_id, p.payment_date, p.payment_method, p.total_amount, p.notes, " +
                "e.first_name as emp_first_name, e.last_name as emp_last_name " +
                "FROM PAYMENTS p LEFT JOIN EMPLOYEES e ON p.employee_id = e.employee_id " +
                "ORDER BY p.payment_date DESC, p.payment_id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Payment payment = mapRowToPayment(rs);
                if (rs.getString("emp_first_name") != null) {
                    payment.setEmployeeName(rs.getString("emp_first_name") + " " + rs.getString("emp_last_name"));
                }
                payments.add(payment);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi lấy tất cả payments: " + e.getMessage());
            throw e;
        }
        return payments;
    }

    public List<Payment> getLatestPayments(int limit) throws SQLException {
        List<Payment> payments = new ArrayList<>();
        String sql = "SELECT p.*, CONCAT(e.first_name, ' ', e.last_name) as employee_name " +
                "FROM PAYMENTS p " +
                "LEFT JOIN EMPLOYEES e ON p.employee_id = e.employee_id " +
                "ORDER BY p.payment_date DESC, p.payment_id DESC LIMIT ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Payment payment = mapRowToPayment(rs);
                    String empName = rs.getString("employee_name");
                    if (empName != null && !empName.trim().isEmpty()) {
                        payment.setEmployeeName(empName);
                    }
                    payments.add(payment);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching latest " + limit + " payments: " + e.getMessage());
            throw e;
        }
        return payments;
    }

    public List<Map<String, Object>> getTopPaymentsByOrderCount(int limit) throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();
        String sql = "SELECT " +
                "    p.payment_id, " +
                "    p.payment_method, " +
                "    p.total_amount AS payment_total_amount, " +
                "    COUNT(o.order_id) AS number_of_orders_paid " +
                "FROM PAYMENTS p " +
                "JOIN ORDERS o ON p.payment_id = o.payment_id " +
                "GROUP BY p.payment_id, p.payment_method, p.total_amount " +
                "ORDER BY number_of_orders_paid DESC, p.total_amount DESC " +
                "LIMIT ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("payment_id", rs.getInt("payment_id"));
                    row.put("payment_method", rs.getString("payment_method"));
                    row.put("payment_total_amount", rs.getBigDecimal("payment_total_amount"));
                    row.put("number_of_orders_paid", rs.getInt("number_of_orders_paid"));
                    results.add(row);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi lấy top payment theo số lượng order: " + e.getMessage());
            throw e;
        }
        return results;
    }

    public List<Map<String, Object>> getSingleOrderPaymentsByHighestPaidEmployee() throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();
        String sql = "WITH HighestSalary AS ( " +
                "    SELECT MAX(salary) as max_salary FROM EMPLOYEES " +
                "), TopEmployees AS ( " +
                "    SELECT employee_id, first_name, last_name, salary " +
                "    FROM EMPLOYEES, HighestSalary " +
                "    WHERE salary = HighestSalary.max_salary " +
                "), EmployeePayments AS ( " +
                "    SELECT  " +
                "        p.payment_id,  " +
                "        p.total_amount AS payment_amount, " +
                "        p.payment_method, " +
                "        te.first_name || ' ' || te.last_name AS employee_name, " +
                "        te.salary AS employee_salary, " +
                "        COUNT(o.order_id) AS orders_linked_count, " +
                "        MIN(o.order_id) AS single_order_id " +
                "    FROM PAYMENTS p " +
                "    JOIN TopEmployees te ON p.employee_id = te.employee_id " +
                "    LEFT JOIN ORDERS o ON p.payment_id = o.payment_id " +
                "    GROUP BY p.payment_id, p.total_amount, p.payment_method, te.first_name, te.last_name, te.salary " +
                "    HAVING COUNT(o.order_id) = 1 " +
                ") " +
                "SELECT * FROM EmployeePayments ORDER BY employee_name, payment_id";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("employee_name", rs.getString("employee_name"));
                row.put("employee_salary", rs.getBigDecimal("employee_salary"));
                row.put("payment_id", rs.getInt("payment_id"));
                row.put("payment_amount", rs.getBigDecimal("payment_amount"));
                row.put("payment_method", rs.getString("payment_method"));
                row.put("order_id_linked", rs.getInt("single_order_id"));
                results.add(row);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi lấy payment của nhân viên lương cao nhất: " + e.getMessage());
            throw e;
        }
        return results;
    }

    // NQ15: Tổng số tiền thanh toán theo từng phương thức thanh toán
    public List<Map<String, Object>> getPaymentSummaryByMethod() throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();
        String sql = "SELECT payment_method, COUNT(payment_id) AS payment_count, SUM(total_amount) AS total_amount_sum " +
                "FROM PAYMENTS " +
                "GROUP BY payment_method " +
                "ORDER BY payment_count DESC, total_amount_sum DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("payment_method", rs.getString("payment_method"));
                row.put("payment_count", rs.getInt("payment_count"));
                row.put("total_amount_sum", rs.getBigDecimal("total_amount_sum"));
                results.add(row);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi lấy tổng thanh toán theo phương thức: " + e.getMessage());
            throw e;
        }
        return results;
    }

    private Payment mapRowToPayment(ResultSet rs) throws SQLException {
        // ... (code như cũ)
        int id = rs.getInt("payment_id");
        Integer employeeIdObj = rs.getObject("employee_id", Integer.class);
        int employeeId = (employeeIdObj != null) ? employeeIdObj : 0;
        Timestamp paymentDateTs = rs.getTimestamp("payment_date");
        LocalDateTime paymentDate = (paymentDateTs != null) ? paymentDateTs.toLocalDateTime() : null;
        String paymentMethod = rs.getString("payment_method");
        BigDecimal totalAmountBd = rs.getBigDecimal("total_amount");
        String notes = rs.getString("notes");
        Payment payment = new Payment(id, employeeId, paymentDate, totalAmountBd, paymentMethod, notes);
        return payment;
    }
}
