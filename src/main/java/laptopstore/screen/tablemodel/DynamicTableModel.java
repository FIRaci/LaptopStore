package laptopstore.screen.tablemodel;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A TableModel that can display data from a List of Maps,
 * where each Map represents a row and keys are column names.
 * Column names and types are determined dynamically from the first row of data.
 */
public class DynamicTableModel extends AbstractTableModel {
    private List<Map<String, Object>> data;
    private String[] columnNames;
    private Class<?>[] columnTypes;

    public DynamicTableModel() {
        this.data = new ArrayList<>();
        this.columnNames = new String[0];
        this.columnTypes = new Class<?>[0];
    }

    public void setData(List<Map<String, Object>> newData) {
        this.data.clear();
        if (newData != null && !newData.isEmpty()) {
            this.data.addAll(newData);
            // Determine column names and types from the first row
            Map<String, Object> firstRow = newData.get(0);
            this.columnNames = firstRow.keySet().toArray(new String[0]);
            this.columnTypes = new Class<?>[this.columnNames.length];
            for (int i = 0; i < this.columnNames.length; i++) {
                Object value = firstRow.get(this.columnNames[i]);
                if (value != null) {
                    this.columnTypes[i] = value.getClass();
                } else {
                    this.columnTypes[i] = Object.class; // Default if value is null
                }
            }
        } else {
            this.columnNames = new String[0];
            this.columnTypes = new Class<?>[0];
        }
        fireTableStructureChanged(); // Important: structure might change
        fireTableDataChanged();
    }

    public void clearData() {
        this.data.clear();
        this.columnNames = new String[0];
        this.columnTypes = new Class<?>[0];
        fireTableStructureChanged();
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int columnIndex) {
        if (columnIndex < 0 || columnIndex >= columnNames.length) {
            return "";
        }
        return columnNames[columnIndex];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex < 0 || columnIndex >= columnTypes.length) {
            return Object.class;
        }
        return columnTypes[columnIndex];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex < 0 || rowIndex >= data.size() || columnIndex < 0 || columnIndex >= columnNames.length) {
            return null;
        }
        Map<String, Object> row = data.get(rowIndex);
        return row.get(columnNames[columnIndex]);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false; // Typically, query results are not editable
    }
}
