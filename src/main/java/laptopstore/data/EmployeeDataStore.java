package laptopstore.data;

import laptopstore.model.Employee;
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

public class EmployeeDataStore {

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

    public Employee addEmployee(Employee employee) throws SQLException {
        if (employee == null) throw new IllegalArgumentException("Employee object cannot be null.");
        if (employee.getFirstName() == null || employee.getFirstName().trim().isEmpty()) throw new IllegalArgumentException("First name is required.");
        if (employee.getLastName() == null || employee.getLastName().trim().isEmpty()) throw new IllegalArgumentException("Last name is required.");
        if (employee.getRole() == null || employee.getRole().trim().isEmpty()) throw new IllegalArgumentException("Role is required.");
        if (employee.getSalary() == null || employee.getSalary().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Salary must be a non-negative value.");
        }
        if (employee.getHireDay() == null) throw new IllegalArgumentException("Hire day is required.");
        if (employee.getEmail() == null || employee.getEmail().trim().isEmpty()) throw new IllegalArgumentException("Email is required.");

        String sql = "INSERT INTO EMPLOYEES (first_name, last_name, phone, email, address, gender, bank_number, role, salary, work_day, hire_day) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, employee.getFirstName().trim());
            pstmt.setString(2, employee.getLastName().trim());
            pstmt.setString(3, employee.getPhone());
            pstmt.setString(4, employee.getEmail().trim());
            pstmt.setString(5, employee.getAddress());
            char gender = employee.getGender();
            if (gender == 'M' || gender == 'F' || gender == 'O') {
                pstmt.setString(6, String.valueOf(gender));
            } else {
                pstmt.setNull(6, Types.CHAR);
            }
            pstmt.setString(7, employee.getBankNumber());
            pstmt.setString(8, employee.getRole().trim());
            pstmt.setBigDecimal(9, employee.getSalary());
            pstmt.setString(10, employee.getWorkDay());
            pstmt.setDate(11, java.sql.Date.valueOf(employee.getHireDay()));

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
                    throw new SQLException("Phone number '" + employee.getPhone() + "' already exists.", e.getSQLState(), e);
                } else if (e.getMessage().toLowerCase().contains("email")) {
                    throw new SQLException("Email '" + employee.getEmail().trim() + "' already exists.", e.getSQLState(), e);
                }
            }
            System.err.println("SQL Error when adding employee: " + e.getMessage());
            throw e;
        }
        return null;
    }

    public boolean updateEmployee(Employee employee) throws SQLException {
        if (employee == null) throw new IllegalArgumentException("Employee object cannot be null.");
        if (employee.getEmployeeId() <= 0) throw new IllegalArgumentException("Invalid Employee ID for update.");

        String sql = "UPDATE EMPLOYEES SET first_name=?, last_name=?, phone=?, email=?, address=?, gender=?, " +
                "bank_number=?, role=?, salary=?, work_day=?, hire_day=? WHERE employee_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, employee.getFirstName().trim());
            pstmt.setString(2, employee.getLastName().trim());
            pstmt.setString(3, employee.getPhone());
            pstmt.setString(4, employee.getEmail().trim());
            pstmt.setString(5, employee.getAddress());
            char gender = employee.getGender();
            if (gender == 'M' || gender == 'F' || gender == 'O') {
                pstmt.setString(6, String.valueOf(gender));
            } else {
                pstmt.setNull(6, Types.CHAR);
            }
            pstmt.setString(7, employee.getBankNumber());
            pstmt.setString(8, employee.getRole().trim());
            pstmt.setBigDecimal(9, employee.getSalary());
            pstmt.setString(10, employee.getWorkDay());
            if (employee.getHireDay() != null) {
                pstmt.setDate(11, java.sql.Date.valueOf(employee.getHireDay()));
            } else {
                pstmt.setNull(11, Types.DATE);
            }
            pstmt.setInt(12, employee.getEmployeeId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                if (e.getMessage().toLowerCase().contains("phone")) {
                    throw new SQLException("Phone number '" + employee.getPhone() + "' is already used by another employee.", e.getSQLState(), e);
                } else if (e.getMessage().toLowerCase().contains("email")) {
                    throw new SQLException("Email '" + employee.getEmail().trim() + "' is already used by another employee.", e.getSQLState(), e);
                }
            }
            System.err.println("SQL Error when updating employee: " + e.getMessage());
            throw e;
        }
    }

    public boolean deleteEmployee(int employeeId) throws SQLException {
        if (employeeId <= 0) throw new IllegalArgumentException("Invalid Employee ID for deletion.");
        String sql = "DELETE FROM EMPLOYEES WHERE employee_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, employeeId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            if ("23503".equals(e.getSQLState())) {
                throw new SQLException("Cannot delete employee ID " + employeeId + " due to existing payment references.", e.getSQLState(), e);
            }
            System.err.println("SQL Error when deleting employee ID " + employeeId + ": " + e.getMessage());
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
            System.err.println("SQL Error when fetching employee by ID " + employeeId + ": " + e.getMessage());
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
            System.err.println("SQL Error when fetching all employees: " + e.getMessage());
            throw e;
        }
        return employees;
    }

    private Employee mapRowToEmployee(ResultSet rs) throws SQLException {
        int id = rs.getInt("employee_id");
        String firstName = rs.getString("first_name");
        String lastName = rs.getString("last_name");
        String phone = rs.getString("phone");
        String email = rs.getString("email");
        String address = rs.getString("address");
        String genderStr = rs.getString("gender");
        char gender = (genderStr != null && !genderStr.isEmpty()) ? genderStr.charAt(0) : '\0';
        String bankNumber = rs.getString("bank_number");
        String role = rs.getString("role");

        // SỬA Ở ĐÂY: Đọc MONEY từ CSDL
        BigDecimal salaryBd = getBigDecimalFromMoneyString(rs, "salary");

        String workDay = rs.getString("work_day");
        java.sql.Date hireDaySql = rs.getDate("hire_day");
        LocalDate hireDay = (hireDaySql != null) ? hireDaySql.toLocalDate() : null;

        return new Employee(id, firstName, lastName, phone, address, gender, bankNumber, role, salaryBd, workDay, hireDay, email);
    }
}
