package backupmanager.utils.table;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class ProgressBarRenderer extends DefaultTableCellRenderer {
    private final JProgressBar progressBar = new JProgressBar(0, 100);

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        // If the value is an Integer (assuming progress data), show the progress bar
        if (value instanceof Integer integer) {
            progressBar.setValue(integer);
            progressBar.setString(integer + "%");

            return progressBar;
        }

        // Return the default (striped) component for non-progress values
        return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    }
}
