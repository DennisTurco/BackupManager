package backupmanager.gui.Table;

import javax.swing.table.DefaultTableModel;

public class BackupTableModel extends DefaultTableModel {
    public BackupTableModel(Object[] columnNames, int rowCount) {
        super(columnNames, rowCount);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return columnIndex == 4 ? Boolean.class : super.getColumnClass(columnIndex);
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }
}
