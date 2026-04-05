package backupmanager.Managers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.List;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backupmanager.BackupOperations;
import backupmanager.Entities.ConfigurationBackup;
import backupmanager.Enums.Translations.TCategory;
import backupmanager.Enums.Translations.TKey;

public class ExportManager {

    private static final Logger logger = LoggerFactory.getLogger(ExportManager.class);

    public static void exportAsCSV(List<ConfigurationBackup> backups, String header) {
        logger.info("Exporting backups to CSV");

        String path = BackupOperations.pathSearchWithFileChooser(false);

        if (path == null) {
            logger.info("Exporting backups to CSV cancelled");
            return;
        }

        String filename = JOptionPane.showInputDialog(null, TCategory.DIALOGS.getTranslation(TKey.CSV_NAME_MESSAGE_INPUT));
        if (filename == null || filename.isEmpty()) {
            logger.info("Exporting backups to CSV cancelled");
            return;
        }

        // Validate filename
        if (!filename.matches("[a-zA-Z0-9-_ ]+")) {
            JOptionPane.showMessageDialog(null, TCategory.DIALOGS.getTranslation(TKey.ERROR_MESSAGE_INVALID_FILENAME), TCategory.DIALOGS.getTranslation(TKey.ERROR_GENERIC_TITLE), JOptionPane.ERROR_MESSAGE);
            logger.info("Exporting backups to CSV cancelled due to invalid file name");
            return;
        }

        // Build full path
        String fullPath = Paths.get(path, filename + ".csv").toString();

        // Check if the file exists
        File file = new File(fullPath);
        if (file.exists()) {
            int overwrite = JOptionPane.showConfirmDialog(null, TCategory.DIALOGS.getTranslation(TKey.DUPLICATED_FILE_NAME_MESSAGE), TCategory.DIALOGS.getTranslation(TKey.CONFIRMATION_REQUIRED_TITLE), JOptionPane.YES_NO_OPTION);
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

            JOptionPane.showMessageDialog(null, TCategory.DIALOGS.getTranslation(TKey.SUCCESSFULLY_EXPORTED_TO_CSV_MESSAGE), TCategory.DIALOGS.getTranslation(TKey.SUCCESS_GENERIC_TITLE), JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            logger.error("Error exporting backups to CSV: " + ex.getMessage(), ex);
            JOptionPane.showMessageDialog(null, TCategory.DIALOGS.getTranslation(TKey.ERROR_MESSAGE_FOR_EXPORTING_TO_CSV) + ex.getMessage(), TCategory.DIALOGS.getTranslation(TKey.ERROR_GENERIC_TITLE), JOptionPane.ERROR_MESSAGE);
        } finally {
            logger.info("Exporting backups to CSV finished");
        }
    }
}
