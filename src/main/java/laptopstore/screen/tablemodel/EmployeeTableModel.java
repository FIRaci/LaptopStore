package laptopstore.screen.tablemodel;

import laptopstore.model.Employee;
import javax.swing.table.AbstractTableModel;
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDate;

public class EmployeeTableModel extends AbstractTableModel {
    private List<Employee> employees;
    // "ID", "First Name", "Last Name", "Role", "Salary", "Phone", "Address", "Gender", "Bank Number", "Work Days", "Hire Date"
    private final String[] columnNames = {"ID", "First Name", "Last Name", "Phone", "Address", "Gender", "Bank Number", "Role", "Salary", "Work Days", "Hire Date"};

    public EmployeeTableModel(List<Employee> employees) {
        this.employees = new ArrayList<>(employees);
    }

    @Override
    public int getRowCount() {
        return employees.size();
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
        Employee employee = employees.get(rowIndex);
        switch (columnIndex) {
            case 0: return employee.getEmployeeId();
            case 1: return employee.getFirstName();
            case 2: return employee.getLastName();
            case 3: return employee.getPhone();
            case 4: return employee.getAddress();
            case 5: Character gender = employee.getGender();
                return (gender != null && gender != ' ') ? String.valueOf(gender) : "";
            case 6: return employee.getBankNumber();
            case 7: return employee.getRole();
            case 8: return employee.getSalary();
            case 9: return employee.getWorkDay();
            case 10: return employee.getHireDate() != null ? employee.getHireDate().toString() : "";
            default: return null;
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0: // ID
                return Integer.class;
            case 8: // Salary
                return Double.class;
            default:
                return String.class;
        }
    }

    public void addEmployee(Employee employee) {
        employees.add(employee);
        fireTableRowsInserted(employees.size() - 1, employees.size() - 1);
    }

    public void removeEmployee(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < employees.size()) {
            employees.remove(rowIndex);
            fireTableRowsDeleted(rowIndex, rowIndex);
        }
    }

    public void removeEmployee(Employee employee) {
        int rowIndex = employees.indexOf(employee);
        if (rowIndex != -1) {
            removeEmployee(rowIndex);
        }
    }

    public void updateEmployee(int rowIndex, Employee employee) {
        if (rowIndex >= 0 && rowIndex < employees.size()) {
            employees.set(rowIndex, employee);
            fireTableRowsUpdated(rowIndex, rowIndex);
        }
    }

    public Employee getEmployeeAt(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < employees.size()) {
            return employees.get(rowIndex);
        }
        return null;
    }

    public void setEmployees(List<Employee> newEmployees) {
        this.employees.clear();
        this.employees.addAll(newEmployees);
        fireTableDataChanged();
    }
}