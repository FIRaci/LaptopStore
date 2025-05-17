package laptopstore.screen.tablemodel;

import laptopstore.model.Payment;
import laptopstore.model.Employee; // Cần để lấy tên Employee
import laptopstore.LaptopStoreApplication; // Để truy cập danh sách employees

import javax.swing.table.AbstractTableModel;
import java.util.List;
import java.util.ArrayList;
import java.time.format.DateTimeFormatter;

public class PaymentTableModel extends AbstractTableModel {
    private List<Payment> payments;
    private final String[] columnNames = {"Payment ID", "Employee", "Payment Date", "Total Amount", "Method", "Status"};
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    public PaymentTableModel(List<Payment> payments) {
        this.payments = new ArrayList<>(payments);
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
            case 1: // Lấy tên Employee từ employeeId
                Employee employee = LaptopStoreApplication.employees.stream()
                        .filter(e -> e.getEmployeeId() == payment.getEmployeeId())
                        .findFirst().orElse(null);
                return employee != null ? employee.getFirstName() + " " + employee.getLastName() : "Unknown (ID: " + payment.getEmployeeId() + ")";
            case 2: return payment.getPaymentDate() != null ? payment.getPaymentDate().format(dateTimeFormatter) : "";
            case 3: return payment.getTotalAmount();
            case 4: return payment.getPaymentMethod();
            case 5: return payment.getStatus();
            default: return null;
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0: // Payment ID
                return Integer.class;
            case 3: // Total Amount
                return Double.class;
            default:
                return String.class;
        }
    }

    public void addPayment(Payment payment) {
        payments.add(payment);
        fireTableRowsInserted(payments.size() - 1, payments.size() - 1);
    }

    public void removePayment(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < payments.size()) {
            payments.remove(rowIndex);
            fireTableRowsDeleted(rowIndex, rowIndex);
        }
    }

    public void removePayment(Payment payment) {
        int rowIndex = payments.indexOf(payment);
        if (rowIndex != -1) {
            removePayment(rowIndex);
        }
    }

    public void updatePayment(int rowIndex, Payment payment) {
        if (rowIndex >= 0 && rowIndex < payments.size()) {
            payments.set(rowIndex, payment);
            fireTableRowsUpdated(rowIndex, rowIndex);
        }
    }

    public Payment getPaymentAt(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < payments.size()) {
            return payments.get(rowIndex);
        }
        return null;
    }

    public void setPayments(List<Payment> newPayments) {
        this.payments.clear();
        this.payments.addAll(newPayments);
        fireTableDataChanged();
    }
}