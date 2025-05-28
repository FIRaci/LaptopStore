package laptopstore.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Payment {
    private int paymentId;
    private int employeeId;
    private LocalDateTime paymentDate;
    private BigDecimal totalAmount; // Đã là BigDecimal
    private String paymentMethod;
    private String status;
    private String notes;

    private String employeeName; // Để hiển thị (lấy từ JOIN)

    public Payment(int paymentId, int employeeId, LocalDateTime paymentDate, BigDecimal totalAmount, String paymentMethod, String status, String notes) {
        this.paymentId = paymentId;
        this.employeeId = employeeId; // employeeId có thể là 0 nếu không có nhân viên liên quan (CSDL cho phép NULL)
        this.paymentDate = paymentDate;
        this.totalAmount = totalAmount;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.notes = notes;
    }

    public Payment() {}

    public Payment(int paymentId, int employeeId, LocalDateTime paymentDate, double totalAmount, String paymentMethod, String status, String notes) {
        this(paymentId, employeeId, paymentDate, BigDecimal.valueOf(totalAmount), paymentMethod, status, notes);
    }

    @Override
    public String toString() {
        return "Payment ID: " + paymentId +
                (employeeName != null ? " - Emp: " + employeeName : (employeeId != 0 ? " - Emp.ID: " + employeeId : "")) +
                " - Amount: $" + (totalAmount != null ? totalAmount.toPlainString() : "N/A") +
                " - Method: " + (paymentMethod != null ? paymentMethod : "N/A") +
                " - Status: " + (status != null ? status : "N/A");
    }

    // Getters
    public int getPaymentId() { return paymentId; }
    public int getEmployeeId() { return employeeId; }
    public LocalDateTime getPaymentDate() { return paymentDate; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getStatus() { return status; }
    public String getNotes() { return notes; }
    public String getEmployeeName() { return employeeName; }

    // Setters
    public void setPaymentId(int paymentId) { this.paymentId = paymentId; }
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }
    public void setPaymentDate(LocalDateTime paymentDate) { this.paymentDate = paymentDate; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public void setStatus(String status) { this.status = status; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
}
