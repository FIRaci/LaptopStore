package laptopstore.screen.tablemodel;

import laptopstore.model.Employee;
import javax.swing.table.AbstractTableModel;
import java.math.BigDecimal;
import java.math.RoundingMode; // Để làm tròn BigDecimal
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;

public class EmployeeTableModel extends AbstractTableModel {
    private List<Employee> employees;
    private final String[] columnNames = {"ID", "First Name", "Last Name", "Phone", "Address", "Gender", "Bank Number", "Role", "Salary", "Work Days", "Hire Date"};
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public EmployeeTableModel(List<Employee> employees) {
        this.employees = new ArrayList<>(employees != null ? employees : new ArrayList<>());
    }

    public EmployeeTableModel() {
        this.employees = new ArrayList<>();
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
            case 8: // Salary là BigDecimal
                BigDecimal salary = employee.getSalary();
                return salary != null ? salary.setScale(2, RoundingMode.HALF_UP) : null; // Format 2 chữ số thập phân
            case 9: return employee.getWorkDay();
            case 10: return employee.getHireDay() != null ? employee.getHireDay().format(dateFormatter) : "";
            default: return null;
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0: // ID
                return Integer.class;
            case 8: // Salary
                return BigDecimal.class; // JTable sẽ cố gắng render BigDecimal
            default:
                return String.class;
        }
    }

    public void setEmployees(List<Employee> newEmployees) {
        this.employees.clear();
        if (newEmployees != null) {
            this.employees.addAll(newEmployees);
        }
        fireTableDataChanged();
    }

    public Employee getEmployeeAt(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < employees.size()) {
            return employees.get(rowIndex);
        }
        return null;
    }

    public void addEmployeeRow(Employee employee) {
        if (employee != null) {
            employees.add(employee);
            fireTableRowsInserted(employees.size() - 1, employees.size() - 1);
        }
    }

    public void removeEmployeeRow(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < employees.size()) {
            employees.remove(rowIndex);
            fireTableRowsDeleted(rowIndex, rowIndex);
        }
    }

    public void removeEmployeeRow(Employee employee) {
        int rowIndex = employees.indexOf(employee);
        if (rowIndex != -1) {
            removeEmployeeRow(rowIndex);
        }
    }

    public void updateEmployeeRow(int rowIndex, Employee employee) {
        if (rowIndex >= 0 && rowIndex < employees.size() && employee != null) {
            employees.set(rowIndex, employee);
            fireTableRowsUpdated(rowIndex, rowIndex);
        }
    }
}
