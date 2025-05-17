package laptopstore.model;

import java.time.LocalDateTime;

public class Payment {
    private int paymentId;
    private int employeeId;
    private LocalDateTime paymentDate;
    private double totalAmount;
    private String paymentMethod;
    private String status;

    public Payment(int paymentId, int employeeId, LocalDateTime paymentDate, double totalAmount, String paymentMethod, String status) {
        this.paymentId = paymentId;
        this.employeeId = employeeId;
        this.paymentDate = paymentDate;
        this.totalAmount = totalAmount;
        this.paymentMethod = paymentMethod;
        this.status = status;
    }

    @Override
    public String toString() {
        return "Payment ID: " + paymentId + " - Amount: $" + totalAmount + " - Method: " + paymentMethod + " - Status: " + status;
    }

    // Getters and setters
    public int getPaymentId() { return paymentId; }
    public int getEmployeeId() { return employeeId; }
    public LocalDateTime getPaymentDate() { return paymentDate; }
    public double getTotalAmount() { return totalAmount; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getStatus() { return status; }
    public void setPaymentId(int paymentId) { this.paymentId = paymentId; }
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }
    public void setPaymentDate(LocalDateTime paymentDate) { this.paymentDate = paymentDate; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public void setStatus(String status) { this.status = status; }
}