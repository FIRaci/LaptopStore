package laptopstore.data;

import laptopstore.model.Payment;
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

    // Helper method to clean currency string and convert to BigDecimal
    private BigDecimal getBigDecimalFromMoneyString(ResultSet rs, String columnName) throws SQLException {
        String moneyString = rs.getString(columnName);
        if (moneyString == null) {
            return null;
        }
        String cleanedString = moneyString.replaceAll("[^\\d.-]", "").replace(",", "");
        if (cleanedString.isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(cleanedString);
        } catch (NumberFormatException e) {
            System.err.println("Error parsing BigDecimal from string: '" + moneyString + "' (cleaned: '" + cleanedString + "') for column: " + columnName);
            throw new SQLException("Bad value for type BigDecimal after cleaning: " + moneyString, e);
        }
    }

    public Payment addPayment(Payment payment) throws SQLException {
        if (payment == null) throw new IllegalArgumentException("Payment object cannot be null.");
        if (payment.getPaymentMethod() == null || payment.getPaymentMethod().trim().isEmpty()) throw new IllegalArgumentException("Payment method is required.");
        if (payment.getTotalAmount() == null || payment.getTotalAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Total amount must be a non-negative value.");
        }
        if (payment.getStatus() == null || payment.getStatus().trim().isEmpty()) throw new IllegalArgumentException("Status is required.");

        String sql = "INSERT INTO PAYMENTS (employee_id, payment_date, payment_method, total_amount, status, notes) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
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
            System.err.println("SQL Error when adding payment: " + e.getMessage());
            throw e;
        }
        return null;
    }

    public boolean updatePayment(Payment payment) throws SQLException {
        if (payment == null) throw new IllegalArgumentException("Payment object cannot be null.");
        if (payment.getPaymentId() <= 0) throw new IllegalArgumentException("Invalid Payment ID for update.");

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
                pstmt.setNull(2, Types.TIMESTAMP);
            }
            pstmt.setString(3, payment.getPaymentMethod().trim());
            pstmt.setBigDecimal(4, payment.getTotalAmount());
            pstmt.setString(5, payment.getStatus().trim());
            pstmt.setString(6, payment.getNotes());
            pstmt.setInt(7, payment.getPaymentId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("SQL Error when updating payment: " + e.getMessage());
            throw e;
        }
    }

    public boolean deletePayment(int paymentId) throws SQLException {
        if (paymentId <= 0) throw new IllegalArgumentException("Invalid Payment ID for deletion.");
        String sql = "DELETE FROM PAYMENTS WHERE payment_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, paymentId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            if ("23503".equals(e.getSQLState())) {
                throw new SQLException("Cannot delete payment ID " + paymentId + " as it is referenced by an order.", e.getSQLState(), e);
            }
            System.err.println("SQL Error when deleting payment ID " + paymentId + ": " + e.getMessage());
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
                    if (rs.getString("emp_first_name") != null) {
                        payment.setEmployeeName(rs.getString("emp_first_name") + " " + rs.getString("emp_last_name"));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL Error when fetching payment by ID " + paymentId + ": " + e.getMessage());
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
            System.err.println("SQL Error when fetching all payments: " + e.getMessage());
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

        // SỬA Ở ĐÂY: Đọc MONEY từ CSDL
        BigDecimal totalAmountBd = getBigDecimalFromMoneyString(rs, "total_amount");

        String status = rs.getString("status");
        String notes = rs.getString("notes");

        Payment payment = new Payment(id, employeeId, paymentDate, totalAmountBd, paymentMethod, status, notes);
        return payment;
    }
}
