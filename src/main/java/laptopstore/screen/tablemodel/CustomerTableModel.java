package laptopstore.screen.tablemodel;

import laptopstore.model.Customer;
import javax.swing.table.AbstractTableModel;
import java.util.List;
import java.util.ArrayList;

public class CustomerTableModel extends AbstractTableModel {
    private List<Customer> customers;
    private final String[] columnNames = {"ID", "Username", "First Name", "Last Name", "Email", "Gender", "Address", "Date of Birth", "Phone"};

    public CustomerTableModel(List<Customer> customers) {
        this.customers = new ArrayList<>(customers);
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
            case 7: return customer.getDateOfBirth() != null ? customer.getDateOfBirth().toString() : "";
            case 8: return customer.getPhone();
            default: return null;
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 0) { // ID
            return Integer.class;
        }
        return String.class; // Các cột khác đều là String hoặc được chuyển thành String
    }

    public void addCustomer(Customer customer) {
        customers.add(customer);
        fireTableRowsInserted(customers.size() - 1, customers.size() - 1);
    }

    public void removeCustomer(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < customers.size()) {
            customers.remove(rowIndex);
            fireTableRowsDeleted(rowIndex, rowIndex);
        }
    }

    public void removeCustomer(Customer customer) {
        int rowIndex = customers.indexOf(customer);
        if (rowIndex != -1) {
            removeCustomer(rowIndex);
        }
    }

    public void updateCustomer(int rowIndex, Customer customer) {
        if (rowIndex >= 0 && rowIndex < customers.size()) {
            customers.set(rowIndex, customer);
            fireTableRowsUpdated(rowIndex, rowIndex);
        }
    }

    public Customer getCustomerAt(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < customers.size()) {
            return customers.get(rowIndex);
        }
        return null;
    }

    public void setCustomers(List<Customer> newCustomers) {
        this.customers.clear();
        this.customers.addAll(newCustomers);
        fireTableDataChanged();
    }
}