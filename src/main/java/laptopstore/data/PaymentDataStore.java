package laptopstore.data;

import laptopstore.model.Payment;
// import laptopstore.model.Employee; // Không cần trực tiếp ở đây nếu Payment model đã có employeeName
import laptopstore.util.DatabaseConnection;

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
import java.util.List;

public class PaymentDataStore {

    public Payment addPayment(Payment payment) throws SQLException {
        if (payment == null) throw new IllegalArgumentException("Payment object cannot be null.");
        if (payment.getPaymentMethod() == null || payment.getPaymentMethod().trim().isEmpty()) throw new IllegalArgumentException("Payment method is required.");
        if (payment.getTotalAmount() == null || payment.getTotalAmount().compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("Total amount must be non-negative.");
        if (payment.getStatus() == null || payment.getStatus().trim().isEmpty()) throw new IllegalArgumentException("Status is required.");


        String sql = "INSERT INTO PAYMENTS (employee_id, payment_date, payment_method, total_amount, status, notes) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            if (payment.getEmployeeId() > 0) {
                pstmt.setInt(1, payment.getEmployeeId());
            } else {
                // Schema CSDL cho phép employee_id là NULL
                pstmt.setNull(1, Types.INTEGER);
            }

            LocalDateTime paymentDate = payment.getPaymentDate() != null ? payment.getPaymentDate() : LocalDateTime.now();
            pstmt.setTimestamp(2, Timestamp.valueOf(paymentDate));
            payment.setPaymentDate(paymentDate); // Cập nhật lại model nếu là now()

            pstmt.setString(3, payment.getPaymentMethod().trim());
            pstmt.setBigDecimal(4, payment.getTotalAmount()); // payment.getTotalAmount() đã là BigDecimal
            pstmt.setString(5, payment.getStatus().trim());
            pstmt.setString(6, payment.getNotes());


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
        // Thêm validate tương tự addPayment

        String sql = "UPDATE PAYMENTS SET employee_id=?, payment_date=?, payment_method=?, total_amount=?, status=?, notes=? " +
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
                pstmt.setNull(2, Types.TIMESTAMP); // Hoặc không cập nhật nếu không muốn
            }
            pstmt.setString(3, payment.getPaymentMethod().trim());
            pstmt.setBigDecimal(4, payment.getTotalAmount()); // payment.getTotalAmount() là BigDecimal
            pstmt.setString(5, payment.getStatus().trim());
            pstmt.setString(6, payment.getNotes());
            pstmt.setInt(7, payment.getPaymentId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi cập nhật payment: " + e.getMessage());
            throw e;
        }
    }

    public boolean deletePayment(int paymentId) throws SQLException {
        if (paymentId <= 0) throw new IllegalArgumentException("Payment ID không hợp lệ để xóa.");
        // Schema CSDL có ON DELETE SET NULL cho ORDERS.payment_id
        String sql = "DELETE FROM PAYMENTS WHERE payment_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, paymentId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            if ("23503".equals(e.getSQLState())) {
                throw new SQLException("Không thể xóa thanh toán ID " + paymentId + " vì có đơn hàng liên quan (và CSDL không cho phép SET NULL).", e.getSQLState(), e);
            }
            System.err.println("Lỗi SQL khi xóa payment ID " + paymentId + ": " + e.getMessage());
            throw e;
        }
    }

    public Payment getPaymentById(int paymentId) throws SQLException {
        if (paymentId <= 0) return null;
        String sql = "SELECT p.payment_id, p.employee_id, p.payment_date, p.payment_method, p.total_amount, p.status, p.notes, " +
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
                    if (rs.getString("emp_first_name") != null) { // Kiểm tra nếu có join với employee
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
        String sql = "SELECT p.payment_id, p.employee_id, p.payment_date, p.payment_method, p.total_amount, p.status, p.notes, " +
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

    private Payment mapRowToPayment(ResultSet rs) throws SQLException {
        int id = rs.getInt("payment_id");
        Integer employeeIdObj = rs.getObject("employee_id", Integer.class);
        int employeeId = (employeeIdObj != null) ? employeeIdObj : 0;

        Timestamp paymentDateTs = rs.getTimestamp("payment_date");
        LocalDateTime paymentDate = (paymentDateTs != null) ? paymentDateTs.toLocalDateTime() : null;
        String paymentMethod = rs.getString("payment_method");
        BigDecimal totalAmountBd = rs.getBigDecimal("total_amount"); // Lấy trực tiếp BigDecimal
        String status = rs.getString("status");
        String notes = rs.getString("notes");

        // Sử dụng constructor của Payment đã được cập nhật
        Payment payment = new Payment(id, employeeId, paymentDate, totalAmountBd, paymentMethod, status, notes);
        // employeeName sẽ được set ở hàm gọi sau khi JOIN
        return payment;
    }
}
