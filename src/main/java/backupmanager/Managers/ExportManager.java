package backupmanager.Managers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backupmanager.BackupOperations;
import backupmanager.Entities.ConfigurationBackup;
import backupmanager.Enums.Translations;
import backupmanager.Enums.Translations.TKey;
import backupmanager.Utils.ToastUtils;

public class ExportManager {

    private static final Logger logger = LoggerFactory.getLogger(ExportManager.class);

    public static void exportAsCSV(JFrame component, List<ConfigurationBackup> backups, String header) {
        logger.info("Exporting backups to CSV");

        String path = BackupOperations.pathSearchWithFileChooser(false);

        if (path == null) {
            logger.info("Exporting backups to CSV cancelled");
            return;
        }

        String filename = JOptionPane.showInputDialog(null, Translations.get(TKey.CSV_NAME_MESSAGE_INPUT));
        if (filename == null || filename.isEmpty()) {
            logger.info("Exporting backups to CSV cancelled");
            return;
        }

        // Validate filename
        if (!filename.matches("[a-zA-Z0-9-_ ]+")) {
            ToastUtils.showError(component, Translations.get(TKey.TOAST_CSV_EXPORT_INVALID_FILENAME));
            logger.info("Exporting backups to CSV cancelled due to invalid file name");
            return;
        }

        // Build full path
        String fullPath = Paths.get(path, filename + ".csv").toString();

        // Check if the file exists
        File file = new File(fullPath);
        if (file.exists()) {
            int overwrite = JOptionPane.showConfirmDialog(null, Translations.get(TKey.DUPLICATED_FILE_NAME_MESSAGE), Translations.get(TKey.CONFIRMATION_REQUIRED_TITLE), JOptionPane.YES_NO_OPTION);
            if (overwrite != JOptionPane.YES_OPTION) {
                logger.info("Exporting backups to CSV cancelled by user (file exists)");
                return;
            }
        }

        try (FileWriter writer = new FileWriter(fullPath, StandardCharsets.UTF_8)) {
            // Prepare header row
            if (header != null && !header.isEmpty()) {
                writer.append(header).append("\n");
            }

            // Prepare data rows
            if (backups != null && !backups.isEmpty()) {
                for (ConfigurationBackup backup : backups) {
                    writer.append(backup.toCsvString()).append("\n");
                }
            }

            ToastUtils.showSuccess(component, Translations.get(TKey.TOAST_CSV_EXPORT));
        } catch (IOException ex) {
            logger.error("Error exporting backups to CSV: " + ex.getMessage(), ex);
            ToastUtils.showError(component, Translations.get(TKey.TOAST_CSV_EXPORT_ERROR) + ": " + ex.getMessage());
        } finally {
            logger.info("Exporting backups to CSV finished");
        }
    }
}
