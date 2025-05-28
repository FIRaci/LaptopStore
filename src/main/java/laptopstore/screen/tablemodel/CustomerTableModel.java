package laptopstore.screen.tablemodel;

import laptopstore.model.Customer;
import javax.swing.table.AbstractTableModel;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;

public class CustomerTableModel extends AbstractTableModel {
    private List<Customer> customers;
    private final String[] columnNames = {"ID", "Username", "First Name", "Last Name", "Email", "Gender", "Address", "Date of Birth", "Phone", "Created At"};
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");


    public CustomerTableModel(List<Customer> customers) {
        this.customers = new ArrayList<>(customers != null ? customers : new ArrayList<>());
    }

    public CustomerTableModel() { // Constructor rỗng để có thể khởi tạo trước khi có dữ liệu
        this.customers = new ArrayList<>();
    }

    @Override
    public int getRowCount() {
        return customers.size();
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
        Customer customer = customers.get(rowIndex);
        switch (columnIndex) {
            case 0: return customer.getCustomerId();
            case 1: return customer.getUsername();
            case 2: return customer.getFirstName();
            case 3: return customer.getLastName();
            case 4: return customer.getEmail();
            case 5: Character gender = customer.getGender();
                return (gender != null && gender != ' ') ? String.valueOf(gender) : "";
            case 6: return customer.getAddress();
            case 7: return customer.getDateOfBirth() != null ? customer.getDateOfBirth().format(dateFormatter) : "";
            case 8: return customer.getPhone();
            case 9: return customer.getCreatedAt() != null ? customer.getCreatedAt().format(dateTimeFormatter) : "";
            default: return null;
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 0) { // ID
            return Integer.class;
        }
        return String.class;
    }

    public void setCustomers(List<Customer> newCustomers) {
        this.customers.clear();
        if (newCustomers != null) {
            this.customers.addAll(newCustomers);
        }
        fireTableDataChanged();
    }

    public Customer getCustomerAt(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < customers.size()) {
            return customers.get(rowIndex);
        }
        return null;
    }

    public void addCustomerRow(Customer customer) {
        if (customer != null) {
            customers.add(customer);
            fireTableRowsInserted(customers.size() - 1, customers.size() - 1);
        }
    }

    public void removeCustomerRow(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < customers.size()) {
            customers.remove(rowIndex);
            fireTableRowsDeleted(rowIndex, rowIndex);
        }
    }

    public void removeCustomerRow(Customer customer) {
        int rowIndex = customers.indexOf(customer);
        if (rowIndex != -1) {
            removeCustomerRow(rowIndex);
        }
    }

    public void updateCustomerRow(int rowIndex, Customer customer) {
        if (rowIndex >= 0 && rowIndex < customers.size() && customer != null) {
            customers.set(rowIndex, customer);
            fireTableRowsUpdated(rowIndex, rowIndex);
        }
    }
}
