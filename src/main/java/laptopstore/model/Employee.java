package laptopstore.model;

import java.time.LocalDate;

public class Employee {
    private int employeeId;
    private String firstName;
    private String lastName;
    private String phone;
    private String address;
    private char gender;
    private String bankNumber;
    private String role;
    private double salary;
    private String workDay;
    private LocalDate hireDay;

    public Employee(int employeeId, String firstName, String lastName, String phone, String address, char gender, String bankNumber, String role, double salary, String workDay, LocalDate hireDay) {
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
    }

    @Override
    public String toString() {
        return firstName + " " + lastName + " (" + role + ")";
    }

    // Getters and setters
    public int getEmployeeId() { return employeeId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
    public char getGender() { return gender; }
    public String getBankNumber() { return bankNumber; }
    public String getRole() { return role; }
    public double getSalary() { return salary; }
    public String getWorkDay() { return workDay; }
    public LocalDate getHireDay() { return hireDay; }
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setAddress(String address) { this.address = address; }
    public void setGender(char gender) { this.gender = gender; }
    public void setBankNumber(String bankNumber) { this.bankNumber = bankNumber; }
    public void setRole(String role) { this.role = role; }
    public void setSalary(double salary) { this.salary = salary; }
    public void setWorkDay(String workDay) { this.workDay = workDay; }
    public void setHireDay(LocalDate hireDay) { this.hireDay = hireDay; }

    public LocalDate getHireDate() {
        return hireDay;
    }
}