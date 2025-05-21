package laptopstore.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Customer {
    private int customerId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private LocalDateTime createdAt;
    private char gender; // 'M', 'F', hoặc 'O' (Other) - CSDL nên cho phép NULL hoặc có giá trị mặc định
    private String address;
    private LocalDate dateOfBirth;
    private String phone;

    public Customer(int customerId, String username, String email, String firstName, String lastName, LocalDateTime createdAt, char gender, String address, LocalDate dateOfBirth, String phone) {
        this.customerId = customerId;
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.createdAt = createdAt;
        this.gender = gender;
        this.address = address;
        this.dateOfBirth = dateOfBirth;
        this.phone = phone;
    }

    public Customer() {
        // Constructor rỗng có thể hữu ích
    }

    @Override
    public String toString() {
        return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "") +
                " (" + (username != null && !username.isEmpty() ? username : "ID: " + customerId) + ")";
    }

    // Getters
    public int getCustomerId() { return customerId; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public char getGender() { return gender; }
    public String getAddress() { return address; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public String getPhone() { return phone; }

    // Setters
    public void setCustomerId(int customerId) { this.customerId = customerId; }
    public void setUsername(String username) { this.username = username; }
    public void setEmail(String email) { this.email = email; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setGender(char gender) { this.gender = gender; }
    public void setAddress(String address) { this.address = address; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public void setPhone(String phone) { this.phone = phone; }
}
