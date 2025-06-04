package laptopstore.data;

import java.math.BigDecimal;
// import java.math.RoundingMode; // Not directly used
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
// import java.time.temporal.ChronoUnit; // Not directly used
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import laptopstore.model.Employee;
import laptopstore.util.DatabaseConnection;

public class EmployeeDataStore {

    // ... (Các phương thức add, update, delete, getById, getAll, ... và các truy vấn cũ giữ nguyên)
    public Employee addEmployee(Employee employee) throws SQLException {
        if (employee == null) throw new IllegalArgumentException("Employee object cannot be null.");
        if (employee.getFirstName() == null || employee.getFirstName().trim().isEmpty()) throw new IllegalArgumentException("First name is required.");
        if (employee.getLastName() == null || employee.getLastName().trim().isEmpty()) throw new IllegalArgumentException("Last name is required.");
        if (employee.getRole() == null || employee.getRole().trim().isEmpty()) throw new IllegalArgumentException("Role is required.");
        if (employee.getSalary() == null || employee.getSalary().compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("Salary must be non-negative.");
        if (employee.getHireDay() == null) throw new IllegalArgumentException("Hire day is required.");
        if (employee.getEmail() == null || employee.getEmail().trim().isEmpty()) throw new IllegalArgumentException("Email is required.");

        String sql = "INSERT INTO EMPLOYEES (first_name, last_name, phone, address, gender, bank_number, role, salary, work_day, hire_day, email) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, employee.getFirstName().trim());
            pstmt.setString(2, employee.getLastName().trim());
            pstmt.setString(3, employee.getPhone());
            pstmt.setString(4, employee.getAddress());
            char gender = employee.getGender();
            if (gender == 'M' || gender == 'F' || gender == 'O') {
                pstmt.setString(5, String.valueOf(gender));
            } else {
                pstmt.setNull(5, Types.CHAR);
            }
            pstmt.setString(6, employee.getBankNumber());
            pstmt.setString(7, employee.getRole().trim());
            pstmt.setBigDecimal(8, employee.getSalary());
            pstmt.setString(9, employee.getWorkDay());
            pstmt.setDate(10, java.sql.Date.valueOf(employee.getHireDay()));
            pstmt.setString(11, employee.getEmail().trim());
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        employee.setEmployeeId(generatedKeys.getInt(1));
                        return employee;
                    }
                }
            }
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                if (e.getMessage().toLowerCase().contains("phone")) {
                    throw new SQLException("Số điện thoại '" + employee.getPhone() + "' đã tồn tại.", e.getSQLState(), e);
                } else if (e.getMessage().toLowerCase().contains("email")) {
                    throw new SQLException("Email '" + employee.getEmail().trim() + "' đã tồn tại.", e.getSQLState(), e);
                }
            }
            System.err.println("Lỗi SQL khi thêm nhân viên: " + e.getMessage());
            throw e;
        }
        return null;
    }

    public boolean updateEmployee(Employee employee) throws SQLException {
        if (employee == null) throw new IllegalArgumentException("Employee object cannot be null.");
        if (employee.getEmployeeId() <= 0) throw new IllegalArgumentException("Employee ID không hợp lệ để cập nhật.");
        String sql = "UPDATE EMPLOYEES SET first_name=?, last_name=?, phone=?, address=?, gender=?, " +
                "bank_number=?, role=?, salary=?, work_day=?, hire_day=?, email=? WHERE employee_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, employee.getFirstName().trim());
            pstmt.setString(2, employee.getLastName().trim());
            pstmt.setString(3, employee.getPhone());
            pstmt.setString(4, employee.getAddress());
            char gender = employee.getGender();
            if (gender == 'M' || gender == 'F' || gender == 'O') {
                pstmt.setString(5, String.valueOf(gender));
            } else {
                pstmt.setNull(5, Types.CHAR);
            }
            pstmt.setString(6, employee.getBankNumber());
            pstmt.setString(7, employee.getRole().trim());
            pstmt.setBigDecimal(8, employee.getSalary());
            pstmt.setString(9, employee.getWorkDay());
            if (employee.getHireDay() != null) {
                pstmt.setDate(10, java.sql.Date.valueOf(employee.getHireDay()));
            } else {
                pstmt.setNull(10, Types.DATE);
            }
            pstmt.setString(11, employee.getEmail().trim());
            pstmt.setInt(12, employee.getEmployeeId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                if (e.getMessage().toLowerCase().contains("phone")) {
                    throw new SQLException("Số điện thoại '" + employee.getPhone() + "' đã được sử dụng.", e.getSQLState(), e);
                } else if (e.getMessage().toLowerCase().contains("email")) {
                    throw new SQLException("Email '" + employee.getEmail().trim() + "' đã được sử dụng.", e.getSQLState(), e);
                }
            }
            System.err.println("Lỗi SQL khi cập nhật nhân viên: " + e.getMessage());
            throw e;
        }
    }

    public boolean deleteEmployee(int employeeId) throws SQLException {
        if (employeeId <= 0) throw new IllegalArgumentException("Employee ID không hợp lệ để xóa.");
        String sql = "DELETE FROM EMPLOYEES WHERE employee_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, employeeId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            if ("23503".equals(e.getSQLState())) {
                throw new SQLException("Không thể xóa nhân viên ID " + employeeId + " vì có dữ liệu thanh toán liên quan (PAYMENTS.employee_id).", e.getSQLState(), e);
            }
            System.err.println("Lỗi SQL khi xóa nhân viên ID " + employeeId + ": " + e.getMessage());
            throw e;
        }
    }

    public Employee getEmployeeById(int employeeId) throws SQLException {
        if (employeeId <= 0) return null;
        String sql = "SELECT * FROM EMPLOYEES WHERE employee_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, employeeId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToEmployee(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi lấy nhân viên theo ID " + employeeId + ": " + e.getMessage());
            throw e;
        }
        return null;
    }

    public List<Employee> getAllEmployees() throws SQLException {
        List<Employee> employees = new ArrayList<>();
        String sql = "SELECT * FROM EMPLOYEES ORDER BY last_name, first_name, employee_id";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                employees.add(mapRowToEmployee(rs));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi lấy tất cả nhân viên: " + e.getMessage());
            throw e;
        }
        return employees;
    }

    public List<Employee> getLatestEmployees(int limit) throws SQLException {
        List<Employee> employees = new ArrayList<>();
        String sql = "SELECT * FROM EMPLOYEES ORDER BY hire_day DESC, employee_id DESC LIMIT ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    employees.add(mapRowToEmployee(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching latest " + limit + " employees: " + e.getMessage());
            throw e;
        }
        return employees;
    }

    public List<Map<String, Object>> getEmployeeSalesPerformance(LocalDate startDate, LocalDate endDate) throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();
        String sql = "SELECT " +
                "    e.employee_id, " +
                "    e.first_name AS employee_first_name, " +
                "    e.last_name AS employee_last_name, " +
                "    e.role, " +
                "    COUNT(DISTINCT o.order_id) AS orders_handled_count, " +
                "    SUM(py.total_amount) AS total_revenue_processed " +
                "FROM EMPLOYEES e " +
                "JOIN PAYMENTS py ON e.employee_id = py.employee_id " +
                "JOIN ORDERS o ON py.payment_id = o.payment_id " +
                "WHERE o.order_date BETWEEN ? AND ? " +
                "GROUP BY e.employee_id, e.first_name, e.last_name, e.role " +
                "ORDER BY total_revenue_processed DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDate(1, java.sql.Date.valueOf(startDate));
            pstmt.setDate(2, java.sql.Date.valueOf(endDate));
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("employee_id", rs.getInt("employee_id"));
                    row.put("employee_first_name", rs.getString("employee_first_name"));
                    row.put("employee_last_name", rs.getString("employee_last_name"));
                    row.put("role", rs.getString("role"));
                    row.put("orders_handled_count", rs.getInt("orders_handled_count"));
                    row.put("total_revenue_processed", rs.getBigDecimal("total_revenue_processed"));
                    results.add(row);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi lấy hiệu suất bán hàng của nhân viên: " + e.getMessage());
            throw e;
        }
        return results;
    }

    public List<Employee> getEmployeesNotSellingProductInMonth(String productName, int month, int year) throws SQLException {
        List<Employee> employees = new ArrayList<>();
        String sql = "SELECT e.* " +
                "FROM EMPLOYEES e " +
                "WHERE NOT EXISTS ( " +
                "    SELECT 1 " +
                "    FROM ORDERS o " +
                "    JOIN ORDER_DETAILS od ON o.order_id = od.order_id " +
                "    JOIN PRODUCTS p ON od.product_id = p.product_id " +
                "    JOIN PAYMENTS py ON o.payment_id = py.payment_id " +
                "    WHERE py.employee_id = e.employee_id " +
                "      AND p.product_name = ? " +
                "      AND EXTRACT(MONTH FROM o.order_date) = ? " +
                "      AND EXTRACT(YEAR FROM o.order_date) = ? " +
                ") ORDER BY e.last_name, e.first_name";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, productName);
            pstmt.setInt(2, month);
            pstmt.setInt(3, year);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    employees.add(mapRowToEmployee(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi tìm nhân viên không bán sản phẩm: " + e.getMessage());
            throw e;
        }
        return employees;
    }

    public List<Map<String, Object>> getTopPerformingEmployeesByPaymentPerDay(int limit, LocalDate referenceDate) throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();
        String sql = "SELECT " +
                "    e.employee_id, " +
                "    e.first_name || ' ' || e.last_name AS employee_name, " +
                "    e.role, " +
                "    e.hire_day, " +
                "    COALESCE(SUM(py.total_amount), 0) AS total_payments_amount, " +
                "    (DATE_PART('day', ?::timestamp - e.hire_day::timestamp) + 1) AS days_worked, " +
                "    CASE " +
                "        WHEN (DATE_PART('day', ?::timestamp - e.hire_day::timestamp) + 1) > 0 " +
                "        THEN COALESCE(SUM(py.total_amount), 0) / (DATE_PART('day', ?::timestamp - e.hire_day::timestamp) + 1) " +
                "        ELSE 0 " +
                "    END AS performance_metric " +
                "FROM EMPLOYEES e " +
                "LEFT JOIN PAYMENTS py ON e.employee_id = py.employee_id " +
                "GROUP BY e.employee_id, e.first_name, e.last_name, e.role, e.hire_day " +
                "ORDER BY performance_metric DESC " +
                "LIMIT ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDate(1, java.sql.Date.valueOf(referenceDate));
            pstmt.setDate(2, java.sql.Date.valueOf(referenceDate));
            pstmt.setDate(3, java.sql.Date.valueOf(referenceDate));
            pstmt.setInt(4, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("employee_id", rs.getInt("employee_id"));
                    row.put("employee_name", rs.getString("employee_name"));
                    row.put("role", rs.getString("role"));
                    row.put("hire_day", rs.getDate("hire_day"));
                    row.put("total_payments_amount", rs.getBigDecimal("total_payments_amount"));
                    row.put("days_worked", rs.getDouble("days_worked"));
                    row.put("performance_metric", rs.getBigDecimal("performance_metric"));
                    results.add(row);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi lấy top nhân viên hiệu suất: " + e.getMessage());
            throw e;
        }
        return results;
    }

    // NQ14: Nhân viên được thuê trong N năm gần đây
    public List<Employee> getEmployeesHiredInLastNYears(int years) throws SQLException {
        List<Employee> employees = new ArrayList<>();
        String sql = "SELECT * FROM EMPLOYEES " +
                "WHERE hire_day >= (CURRENT_DATE - CAST(? || ' years' AS INTERVAL)) " +
                "ORDER BY hire_day DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, years);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    employees.add(mapRowToEmployee(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi lấy nhân viên được thuê trong " + years + " năm gần đây: " + e.getMessage());
            throw e;
        }
        return employees;
    }

    private Employee mapRowToEmployee(ResultSet rs) throws SQLException {
        int id = rs.getInt("employee_id");
        String firstName = rs.getString("first_name");
        String lastName = rs.getString("last_name");
        String phone = rs.getString("phone");
        String address = rs.getString("address");
        String genderStr = rs.getString("gender");
        char gender = (genderStr != null && !genderStr.isEmpty()) ? genderStr.charAt(0) : '\0';
        String bankNumber = rs.getString("bank_number");
        String role = rs.getString("role");
        BigDecimal salaryBd = rs.getBigDecimal("salary");
        String workDay = rs.getString("work_day");
        java.sql.Date hireDaySql = rs.getDate("hire_day");
        LocalDate hireDay = (hireDaySql != null) ? hireDaySql.toLocalDate() : null;
        String email = rs.getString("email");
        return new Employee(id, firstName, lastName, phone, address, gender, bankNumber, role, salaryBd, workDay, hireDay, email);
    }
}
