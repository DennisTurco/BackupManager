package backupmanager.gui.Table;

import java.time.format.DateTimeFormatter;

import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellRenderer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backupmanager.Entities.ConfigurationBackup;

public class BackupTableDataService {

    private final Logger logger = LoggerFactory.getLogger(BackupTableDataService.class);
    private final JTable table;
    private final DateTimeFormatter formatter;
    private final ProgressBarRenderer progressRenderer;
    private final TableCellRenderer defaultRenderer;

    private static final int COLUMN_PROGRESS = 3;

    public BackupTableDataService(JTable table, DateTimeFormatter formatter) {
        if (table == null) throw new IllegalArgumentException("Table cannot be null");
        if (formatter == null) throw new IllegalArgumentException("Formatter cannot be null");
        this.table = table;
        this.formatter = formatter;
        this.progressRenderer = new ProgressBarRenderer();
        this.defaultRenderer = table.getColumnModel().getColumn(COLUMN_PROGRESS).getCellRenderer();
    }

    public void removeProgress(ConfigurationBackup backup) {
        if (backup == null) throw new IllegalArgumentException("Backup cannot be null");

        int row = findBackupRowIndex(backup);

        // remove the progress bar renderer
        table.getColumnModel().getColumn(COLUMN_PROGRESS).setCellRenderer(defaultRenderer);

        // Set last backup value in the table
        table.getModel().setValueAt(
                backup.getLastBackupDate() != null ? backup.getLastBackupDate().format(formatter) : "",
                row,
                COLUMN_PROGRESS
        );

        table.repaint();  // Repaints the whole table
    }

    public void updateProgress(ConfigurationBackup backup, int value) {
        if (backup == null) throw new IllegalArgumentException("Backup cannot be null");
        if (value < 0 || value > 100) throw new IllegalArgumentException("Value must be between 0 and 100");

        SwingUtilities.invokeLater(() -> {
            // Locate the row index of the backup in the table
            int rowIndex = findBackupRowIndex(backup);
            if (rowIndex == -1) return;

            table.getColumnModel().getColumn(COLUMN_PROGRESS).setCellRenderer(progressRenderer);

            // Restore the original renderer after completion
            if (value == 100) {
                logger.debug("Restore the original renderer after completion");
                removeProgress(backup);
            } else {
                // Update the value of the progress in the table
                table.getModel().setValueAt(value, rowIndex, COLUMN_PROGRESS);
            }

            table.repaint();
        });
    }

    // public void updateTableWithNewBackupList(List<ConfigurationBackup> updatedBackups) {
        // logger.debug("updating backup list");

        // SwingUtilities.invokeLater(() -> {
        //     BackupManagerGUI.model.setRowCount(0);

        //     for (ConfigurationBackup backup : updatedBackups) {
        //         BackupManagerGUI.model.addRow(new Object[]{
        //             backup.getName(),
        //             backup.getTargetPath(),
        //             backup.getDestinationPath(),
        //             backup.getLastBackupDate() != null ? backup.getLastBackupDate().format(formatter) : "",
        //             backup.isAutomatic(),
        //             backup.getNextBackupDate() != null ? backup.getNextBackupDate().format(formatter) : "",
        //             backup.getTimeIntervalBackup() != null ? backup.getTimeIntervalBackup().toString() : ""
        //         });
        //     }
        // });
    // }

    private int findBackupRowIndex(ConfigurationBackup backup) {
        if (backup == null) throw new IllegalArgumentException("Backup cannot be null");

        for (int i = 0; i < table.getRowCount(); i++) {
            if (table.getValueAt(i, 0).equals(backup.getName())) { // first column holds unique backup names
                return i;
            }
        }
        return -1;
    }
}
