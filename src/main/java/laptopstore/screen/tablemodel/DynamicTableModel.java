package laptopstore.screen.tablemodel;

import javax.swing.table.AbstractTableModel;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class DynamicTableModel extends AbstractTableModel {
    private List<Object[]> data;
    private String[] columnNames;

    public DynamicTableModel() {
        this.data = new ArrayList<>();
        this.columnNames = new String[0];
    }

    public DynamicTableModel(List<Object[]> data, String[] columnNames) {
        this.data = new ArrayList<>(data);
        this.columnNames = Arrays.copyOf(columnNames, columnNames.length);
    }

    public void setData(List<Object[]> newData, String[] newColumnNames) {
        if (newData == null) {
            this.data = new ArrayList<>();
        } else {
            this.data = new ArrayList<>(newData);
        }

        if (newColumnNames == null) {
            this.columnNames = new String[0];
        } else {
            this.columnNames = Arrays.copyOf(newColumnNames, newColumnNames.length);
        }
        fireTableStructureChanged();
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
        if (columnIndex >= 0 && columnIndex < columnNames.length) {
            return columnNames[columnIndex];
        }
        return super.getColumnName(columnIndex);
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex >= 0 && rowIndex < data.size()) {
            Object[] row = data.get(rowIndex);
            if (columnIndex >= 0 && columnIndex < row.length) {
                return row[columnIndex];
            }
        }
        return null;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (data.isEmpty() || getColumnCount() == 0 || columnIndex >= getColumnCount()) {
            return Object.class;
        }
        Object value = getValueAt(0, columnIndex);
        if (value != null) {
            return value.getClass();
        }
        return Object.class;
    }

    public void clearData() {
        this.data.clear();
        this.columnNames = new String[0];
        fireTableStructureChanged();
    }
}
