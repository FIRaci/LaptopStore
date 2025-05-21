package laptopstore.screen.tablemodel;

import laptopstore.model.Payment;
// Để hiển thị tên Employee:
// import laptopstore.model.Employee;
// import laptopstore.data.EmployeeDataStore;

import javax.swing.table.AbstractTableModel;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
// import java.util.HashMap;
// import java.util.Map;

public class PaymentTableModel extends AbstractTableModel {
    private List<Payment> payments;
    private final String[] columnNames = {"Payment ID", "Employee ID", "Payment Date", "Total Amount", "Method", "Status"};
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    // Ví dụ nếu muốn lấy tên Employee
    // private EmployeeDataStore employeeDs;
    // private Map<Integer, String> employeeNameCache;

    public PaymentTableModel(List<Payment> payments) {
        this.payments = new ArrayList<>(payments != null ? payments : new ArrayList<>());
        // this.employeeDs = new EmployeeDataStore();
        // this.employeeNameCache = new HashMap<>();
    }

    public PaymentTableModel() {
        this.payments = new ArrayList<>();
        // this.employeeDs = new EmployeeDataStore();
        // this.employeeNameCache = new HashMap<>();
    }

    @Override
    public int getRowCount() {
        return payments.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Payment payment = payments.get(rowIndex);
        switch (columnIndex) {
            case 0: return payment.getPaymentId();
            case 1: // Hiển thị Employee ID.
                // Để hiển thị tên Employee, tương tự như Category trong ProductTableModel,
                // bạn nên sửa PaymentDataStore để JOIN và lấy tên Employee,
                // sau đó thêm trường employeeName (hoặc Employee object) vào model Payment.java.
                // Rồi ở đây chỉ cần: return payment.getEmployeeName();
                return payment.getEmployeeId(); // Tạm thời hiển thị ID
            case 2: return payment.getPaymentDate() != null ? payment.getPaymentDate().format(dateTimeFormatter) : "";
            case 3:
                BigDecimal totalAmount = payment.getTotalAmount();
                return totalAmount != null ? totalAmount.setScale(2, RoundingMode.HALF_UP) : null;
            case 4: return payment.getPaymentMethod();
            case 5: return payment.getStatus();
            default: return null;
        }
    }
    /*
    private String getEmployeeNameFromDs(int employeeId) {
        if (employeeDs == null) return String.valueOf(employeeId);
        if (employeeNameCache.containsKey(employeeId)) {
            return employeeNameCache.get(employeeId);
        }
        try {
            Employee employee = employeeDs.getEmployeeById(employeeId);
            if (employee != null) {
                String name = employee.getFirstName() + " " + employee.getLastName();
                employeeNameCache.put(employeeId, name);
                return name;
            }
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
        return String.valueOf(employeeId);
    }
    */

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0: // Payment ID
            case 1: // Employee ID
                return Integer.class;
            case 3: // Total Amount
                return BigDecimal.class;
            default:
                return String.class;
        }
    }

    public void setPayments(List<Payment> newPayments) {
        this.payments.clear();
        // if (employeeNameCache != null) this.employeeNameCache.clear();
        if (newPayments != null) {
            this.payments.addAll(newPayments);
        }
        fireTableDataChanged();
    }

    public Payment getPaymentAt(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < payments.size()) {
            return payments.get(rowIndex);
        }
        return null;
    }

    public void addPaymentRow(Payment payment) {
        if (payment != null) {
            payments.add(payment);
            fireTableRowsInserted(payments.size() - 1, payments.size() - 1);
        }
    }

    public void removePaymentRow(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < payments.size()) {
            payments.remove(rowIndex);
            fireTableRowsDeleted(rowIndex, rowIndex);
        }
    }

    public void removePaymentRow(Payment payment) {
        int rowIndex = payments.indexOf(payment);
        if (rowIndex != -1) {
            removePaymentRow(rowIndex);
        }
    }

    public void updatePaymentRow(int rowIndex, Payment payment) {
        if (rowIndex >= 0 && rowIndex < payments.size() && payment != null) {
            payments.set(rowIndex, payment);
            fireTableRowsUpdated(rowIndex, rowIndex);
        }
    }
}
