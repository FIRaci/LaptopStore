package laptopstore.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Employee {
    private int employeeId;
    private String firstName;
    private String lastName;
    private String phone;
    private String address;
    private char gender; // 'M', 'F', 'O'
    private String bankNumber;
    private String role;
    private BigDecimal salary; // Đã là BigDecimal
    private String workDay;
    private LocalDate hireDay;
    private String email; // Đã thêm email

    // Constructor chính sử dụng BigDecimal
    public Employee(int employeeId, String firstName, String lastName, String phone, String address, char gender, String bankNumber, String role, BigDecimal salary, String workDay, LocalDate hireDay, String email) {
        this.employeeId = employeeId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.address = address;
        this.gender = gender;
        this.bankNumber = bankNumber;
        this.role = role;
        this.salary = salary;
        this.workDay = workDay;
        this.hireDay = hireDay;
        this.email = email;
    }

    public Employee() {}

    // Constructor phụ để chuyển từ double (có thể bỏ nếu không dùng nhiều)
    public Employee(int employeeId, String firstName, String lastName, String phone, String address, char gender, String bankNumber, String role, double salary, String workDay, LocalDate hireDay, String email) {
        this(employeeId, firstName, lastName, phone, address, gender, bankNumber, role, BigDecimal.valueOf(salary), workDay, hireDay, email);
    }

    @Override
    public String toString() {
        return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "") +
                " (" + (role != null && !role.isEmpty() ? role : "ID: " + employeeId) + ")";
    }

    // Getters
    public int getEmployeeId() { return employeeId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
    public char getGender() { return gender; }
    public String getBankNumber() { return bankNumber; }
    public String getRole() { return role; }
    public BigDecimal getSalary() { return salary; }
    public String getWorkDay() { return workDay; }
    public LocalDate getHireDay() { return hireDay; }
    public String getEmail() { return email; }

    // Setters
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setAddress(String address) { this.address = address; }
    public void setGender(char gender) { this.gender = gender; }
    public void setBankNumber(String bankNumber) { this.bankNumber = bankNumber; }
    public void setRole(String role) { this.role = role; }
    public void setSalary(BigDecimal salary) { this.salary = salary; }
    public void setWorkDay(String workDay) { this.workDay = workDay; }
    public void setHireDay(LocalDate hireDay) { this.hireDay = hireDay; }
    public void setEmail(String email) { this.email = email; }
}
