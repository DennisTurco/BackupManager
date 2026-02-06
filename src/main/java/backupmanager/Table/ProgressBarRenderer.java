package backupmanager.Table;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class ProgressBarRenderer extends DefaultTableCellRenderer {
    private final StripedRowRenderer stripedRowRenderer = new StripedRowRenderer();
    private final JProgressBar progressBar = new JProgressBar(0, 100);

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        // Delegate the striped row coloring logic to the StripedRowRenderer
        Component c = stripedRowRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        // If the value is an Integer (assuming progress data), show the progress bar
        if (value instanceof Integer integer) {
            progressBar.setValue(integer);
            progressBar.setString(integer + "%");
            progressBar.setStringPainted(true);

            // Set the progress bar background color based on the row (even/odd striped rows)
            if (row % 2 == 0) {
                progressBar.setBackground(new Color(223, 222, 243)); // Even row color for progress bar
            } else {
                progressBar.setBackground(Color.WHITE); // Odd row color for progress bar
            }

            // Return the progress bar component instead of the default cell component
            return progressBar;
        }

        // Return the default (striped) component for non-progress values
        return c;
    }
}