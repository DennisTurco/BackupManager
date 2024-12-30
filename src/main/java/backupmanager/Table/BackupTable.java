package backupmanager.Table;

import java.awt.Point;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableModel;

public class BackupTable extends JTable {
    public BackupTable(TableModel model) {
        super(model);
        setRowHeight(35);
        setAutoCreateRowSorter(true); // Enable column sorting

        // Add the TableModelListener to handle updates
        getModel().addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE) {
                int row = e.getFirstRow();
                int column = e.getColumn();
                int targetColumnIndex = 3; // Specify the target column index

                // Check if the value in the target column is 0, then set the StripedProgressBarRenderer
                if (column == targetColumnIndex) {
                    Object value = getValueAt(row, column);
                    if (value instanceof Integer && (Integer) value == 0) {
                        getColumnModel().getColumn(targetColumnIndex).setCellRenderer(new ProgressBarRenderer());
                        repaint(); // Repaint the table to reflect the changes
                    }
                }
            }
        });
    }

    @Override
    public String getToolTipText(java.awt.event.MouseEvent e) {
        Point point = e.getPoint();
        int row = rowAtPoint(point);
        int col = columnAtPoint(point);

        if (col == 6) {
            Object value = getValueAt(row, col);
            return value != null ? "dd.HH:mm" : null;
        }
        return null;
    }
}
