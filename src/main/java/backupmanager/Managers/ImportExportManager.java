package backupmanager.Managers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;

import backupmanager.BackupOperations;
import backupmanager.Entities.Backup;
import backupmanager.Entities.BackupList;
import backupmanager.Entities.Preferences;
import backupmanager.Enums.ConfigKey;
import backupmanager.Enums.TranslationLoaderEnum.TranslationCategory;
import backupmanager.Enums.TranslationLoaderEnum.TranslationKey;
import backupmanager.GUI.BackupManagerGUI;
import backupmanager.Json.JSONBackup;
import backupmanager.Table.TableDataManager;

public class ImportExportManager {

    private static final Logger logger = LoggerFactory.getLogger(ImportExportManager.class);

    // return the Backup list. Null if the operations fail or cancelled by the user
    public static List<Backup> importListFromJson(BackupManagerGUI main, DateTimeFormatter formatter) {
        JFileChooser jfc = new JFileChooser(ConfigKey.RES_DIRECTORY_STRING.getValue());
        jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);

        FileNameExtensionFilter jsonFilter = new FileNameExtensionFilter("JSON Files (*.json)", "json");
        jfc.setFileFilter(jsonFilter);
        int returnValue = jfc.showSaveDialog(null);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = jfc.getSelectedFile();
            if (selectedFile.isFile() && selectedFile.getName().toLowerCase().endsWith(".json")) {
                logger.info("File imported: " + selectedFile);

                Preferences.setBackupList(new BackupList(selectedFile.getParent()+File.separator, selectedFile.getName()));
                Preferences.updatePreferencesToJSON();

                try {
                    List<Backup> backups = JSONBackup.readBackupListFromJSON(Preferences.getBackupList().getDirectory(), Preferences.getBackupList().getFile());
                    TableDataManager.updateTableWithNewBackupList(backups, formatter);
                    JOptionPane.showMessageDialog(main, TranslationCategory.DIALOGS.getTranslation(TranslationKey.BACKUP_LIST_CORRECTLY_IMPORTED_MESSAGE), TranslationCategory.DIALOGS.getTranslation(TranslationKey.BACKUP_LIST_CORRECTLY_IMPORTED_TITLE), JOptionPane.INFORMATION_MESSAGE);
                    return backups;
                } catch (IOException ex) {
                    logger.error("An error occurred: " + ex.getMessage(), ex);
                }
            } else {
                JOptionPane.showMessageDialog(main, TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_MESSAGE_FOR_WRONG_FILE_EXTENSION_MESSAGE), TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_MESSAGE_FOR_WRONG_FILE_EXTENSION_TITLE), JOptionPane.ERROR_MESSAGE);
            }
        }

        return null;
    }

    public static void exportListToJson() {
        Path desktopPath = Paths.get(System.getProperty("user.home"), "Desktop", Preferences.getBackupList().getFile());
        Path sourcePath = Paths.get(Preferences.getBackupList().getDirectory() + Preferences.getBackupList().getFile());

        try {
            Files.copy(sourcePath, desktopPath, StandardCopyOption.REPLACE_EXISTING);
            JOptionPane.showMessageDialog(null, TranslationCategory.DIALOGS.getTranslation(TranslationKey.BACKUP_LIST_CORRECTLY_EXPORTED_MESSAGE), TranslationCategory.DIALOGS.getTranslation(TranslationKey.BACKUP_LIST_CORRECTLY_EXPORTED_TITLE), JOptionPane.INFORMATION_MESSAGE);
        } catch (java.nio.file.NoSuchFileException ex) {
            logger.error("Source file not found: " + ex.getMessage());
            JOptionPane.showMessageDialog(null, "Error: The source file was not found.\nPlease check the file path.", "Export Error", JOptionPane.ERROR_MESSAGE);
        } catch (java.nio.file.AccessDeniedException ex) {
            logger.error("Access denied to desktop: " + ex.getMessage());
            JOptionPane.showMessageDialog(null, "Error: Access to the Desktop is denied.\nPlease check folder permissions and try again.","Export Error", JOptionPane.ERROR_MESSAGE);
        } catch (IOException ex) {
            logger.error("Unexpected error: " + ex.getMessage());
            ExceptionManager.openExceptionMessage(ex.getMessage(), Arrays.toString(ex.getStackTrace()));
        }
    }

    public static void exportAsPDF(ArrayList<Backup> backups, String headers) {
        logger.info("Exporting backups to PDF");

        String path = BackupOperations.pathSearchWithFileChooser(false);

        if (path == null) {
            logger.info("Exporting backups to PDF cancelled");
            return;
        }

        String filename = JOptionPane.showInputDialog(null, TranslationCategory.DIALOGS.getTranslation(TranslationKey.PDF_NAME_MESSAGE_INPUT));
        if (filename == null || filename.isEmpty()) {
            logger.info("Exporting backups to PDF cancelled");
            return;
        }

        // Validate filename
        if (!filename.matches("[a-zA-Z0-9-_ ]+")) {
            JOptionPane.showMessageDialog(null, TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_MESSAGE_INVALID_FILENAME), TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_GENERIC_TITLE), JOptionPane.ERROR_MESSAGE);
            logger.info("Exporting backups to PDF cancelled due to invalid file name");
            return;
        }

        // Build full path
        String fullPath = Paths.get(path, filename + ".pdf").toString();

        // Check if the file exists
        File file = new File(fullPath);
        if (file.exists()) {
            int overwrite = JOptionPane.showConfirmDialog(null, TranslationCategory.DIALOGS.getTranslation(TranslationKey.DUPLICATED_FILE_NAME_MESSAGE), TranslationCategory.DIALOGS.getTranslation(TranslationKey.CONFIRMATION_REQUIRED_TITLE), JOptionPane.YES_NO_OPTION);
            if (overwrite != JOptionPane.YES_OPTION) {
                logger.info("Exporting backups to PDF cancelled by user (file exists)");
                return;
            }
        }

        try {
            // Initialize PDF writer
            PdfWriter writer = new PdfWriter(fullPath);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);
            
            // insert pdf title
            document.add(new Paragraph(Preferences.getBackupList().getFile()).setFontSize(12f).setBold());

            // Create table
            String[] headerArray = headers.split(","); // Assuming headers are comma-separated
            Table table = new Table(headerArray.length);
            
            // Add header cells
            for (String header : headerArray) {
                table.addCell(new Cell().add(new Paragraph(header.trim())).setFontSize(8f)); // Wrap the header in a Paragraph
            }

            // Add backup data
            if (backups != null && !backups.isEmpty()) {
                for (Backup backup : backups) {
                    String[] data = backup.toCsvString().split(","); // Assuming backup data is comma-separated
                    for (String value : data) {
                        // new line every 25 characters
                        for (int i = 0; i < value.length(); i++) {
                            if (i % 25 == 0) {
                                value = value.substring(0, i) + "\n" + value.substring(i); 
                            }
                        }
                        table.addCell(new Cell().add(new Paragraph(value.trim())).setFontSize(5f)); // Wrap the value in a Paragraph
                    }
                }
            }
            
            // Add table to document
            document.add(table);
            
            // Close document
            document.close();
            
            // Notify success
            JOptionPane.showMessageDialog(null, TranslationCategory.DIALOGS.getTranslation(TranslationKey.SUCCESSFULLY_EXPORTED_TO_PDF_MESSAGE), TranslationCategory.DIALOGS.getTranslation(TranslationKey.SUCCESS_GENERIC_TITLE), JOptionPane.INFORMATION_MESSAGE);
            
        } catch (IOException ex) {
            logger.error("Error exporting backups to PDF: " + ex.getMessage(), ex);
            JOptionPane.showMessageDialog(null, TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_MESSAGE_FOR_EXPORTING_TO_PDF) + ex.getMessage(), TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_GENERIC_TITLE), JOptionPane.ERROR_MESSAGE);
        } finally {
            logger.info("Exporting backups to PDF finished");
        }
    }

    public static void exportAsCSV(ArrayList<Backup> backups, String header) {
        logger.info("Exporting backups to CSV");

        String path = BackupOperations.pathSearchWithFileChooser(false);

        if (path == null) {
            logger.info("Exporting backups to CSV cancelled");
            return;
        }

        String filename = JOptionPane.showInputDialog(null, TranslationCategory.DIALOGS.getTranslation(TranslationKey.CSV_NAME_MESSAGE_INPUT));
        if (filename == null || filename.isEmpty()) {
            logger.info("Exporting backups to CSV cancelled");
            return;
        }

        // Validate filename
        if (!filename.matches("[a-zA-Z0-9-_ ]+")) {
            JOptionPane.showMessageDialog(null, TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_MESSAGE_INVALID_FILENAME), TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_GENERIC_TITLE), JOptionPane.ERROR_MESSAGE);
            logger.info("Exporting backups to CSV cancelled due to invalid file name");
            return;
        }
        
        // Build full path
        String fullPath = Paths.get(path, filename + ".csv").toString();

        // Check if the file exists
        File file = new File(fullPath);
        if (file.exists()) {
            int overwrite = JOptionPane.showConfirmDialog(null, TranslationCategory.DIALOGS.getTranslation(TranslationKey.DUPLICATED_FILE_NAME_MESSAGE), TranslationCategory.DIALOGS.getTranslation(TranslationKey.CONFIRMATION_REQUIRED_TITLE), JOptionPane.YES_NO_OPTION);
            if (overwrite != JOptionPane.YES_OPTION) {
                logger.info("Exporting backups to CSV cancelled by user (file exists)");
                return;
            }
        }

        try (FileWriter writer = new FileWriter(fullPath)) {
            // Prepare header row
            if (header != null && !header.isEmpty()) {
                writer.append(header).append("\n");
            }

            // Prepare data rows
            if (backups != null && !backups.isEmpty()) {
                for (Backup backup : backups) {
                    writer.append(backup.toCsvString()).append("\n");
                }
            }

            JOptionPane.showMessageDialog(null, TranslationCategory.DIALOGS.getTranslation(TranslationKey.SUCCESSFULLY_EXPORTED_TO_CSV_MESSAGE), TranslationCategory.DIALOGS.getTranslation(TranslationKey.SUCCESS_GENERIC_TITLE), JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            logger.error("Error exporting backups to CSV: " + ex.getMessage(), ex);
            JOptionPane.showMessageDialog(null, TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_MESSAGE_FOR_EXPORTING_TO_CSV) + ex.getMessage(), TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_GENERIC_TITLE), JOptionPane.ERROR_MESSAGE);
        } finally {
            logger.info("Exporting backups to CSV finished");
        }
    }
}