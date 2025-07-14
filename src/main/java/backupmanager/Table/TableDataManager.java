package backupmanager.Table;

import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.table.TableColumnModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backupmanager.Entities.Backup;
import backupmanager.GUI.BackupManagerGUI;

public class TableDataManager {

    private static final Logger logger = LoggerFactory.getLogger(TableDataManager.class);

    public static void removeProgressInTheTableAndRestoreAsDefault(Backup backup, DateTimeFormatter formatter) {
        if (backup == null) throw new IllegalArgumentException("Backup cannot be null");
        if (formatter == null) throw new IllegalArgumentException("Formatter cannot be null");

        if (BackupManagerGUI.backupTable == null) {
            return;
        }

        // remove the progress bar renderer
        BackupManagerGUI.backupTable.getColumnModel().getColumn(3).setCellRenderer(new StripedRowRenderer());

        // Set last backup value in the table
        BackupManagerGUI.backupTable.getModel().setValueAt(
                backup.getLastBackupDate() != null ? backup.getLastBackupDate().format(formatter) : "",
                TableDataManager.findBackupRowIndex(backup, BackupManagerGUI.backupTable), 3);

        BackupManagerGUI.backupTable.repaint();  // Repaints the whole table
        BackupManagerGUI.backupTable.revalidate(); // Revalidates the table layout
    }

    public static void updateProgressBarPercentage(Backup backup, int value, DateTimeFormatter formatter) {
        if (backup == null) throw new IllegalArgumentException("Backup cannot be null");
        if (value < 0 || value > 100) throw new IllegalArgumentException("Value must be between 0 and 100");
        if (formatter == null) throw new IllegalArgumentException("Formatter cannot be null");

        if (BackupManagerGUI.backupTable == null) {
            return;
        }

        SwingUtilities.invokeLater(() -> {
            // Locate the row index of the backup in the table
            int rowIndex = TableDataManager.findBackupRowIndex(backup, BackupManagerGUI.backupTable);
            if (rowIndex != -1) {
                TableColumnModel columnModel = BackupManagerGUI.backupTable.getColumnModel();
                int targetColumnIndex = 3;

                columnModel.getColumn(targetColumnIndex).setCellRenderer(new ProgressBarRenderer());

                // Restore the original renderer after completion
                if (value == 100) {
                    logger.debug("Restore the original renderer after completion");
                    BackupManagerGUI.backupTable.getModel().setValueAt(
                        backup.getLastBackupDate() != null ? backup.getLastBackupDate().format(formatter) : "",
                        rowIndex,
                        targetColumnIndex
                    );
                } else {
                    // Update the value of the progress in the table
                    BackupManagerGUI.backupTable.getModel().setValueAt(value, rowIndex, targetColumnIndex);
                }

                BackupManagerGUI.backupTable.repaint();
            }
        });
    }

    public static void updateTableWithNewBackupList(List<Backup> updatedBackups, DateTimeFormatter formatter) { 
        logger.debug("updating backup list");

        SwingUtilities.invokeLater(() -> {
            BackupManagerGUI.model.setRowCount(0);

            for (Backup backup : updatedBackups) {
                BackupManagerGUI.model.addRow(new Object[]{
                    backup.getName(),
                    backup.getTargetPath(),
                    backup.getDestinationPath(),
                    backup.getLastBackupDate() != null ? backup.getLastBackupDate().format(formatter) : "",
                    backup.isAutomatic(),
                    backup.getNextBackupDate() != null ? backup.getNextBackupDate().format(formatter) : "",
                    backup.getTimeIntervalBackup() != null ? backup.getTimeIntervalBackup().toString() : ""
                });
            }
        });
    }

    private static int findBackupRowIndex(Backup backup, BackupTable table) {
        if (backup == null) throw new IllegalArgumentException("Backup cannot be null");
        if (table == null) throw new IllegalArgumentException("Table cannot be null");

        for (int i = 0; i < table.getRowCount(); i++) {
            if (table.getValueAt(i, 0).equals(backup.getName())) { // first column holds unique backup names
                return i;
            }
        }
        return -1;
    }
}
