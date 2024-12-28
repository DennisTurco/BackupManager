package backupmanager;

import backupmanager.Entities.Backup;
import backupmanager.Entities.Preferences;
import backupmanager.Enums.TranslationLoaderEnum.TranslationCategory;
import backupmanager.Enums.TranslationLoaderEnum.TranslationKey;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;

import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;

import javax.swing.JOptionPane;

public class Exporter {
    public static void exportAsPDF(ArrayList<Backup> backups, String headers) {
        Logger.logMessage("Exporting backups to PDF", Logger.LogLevel.INFO);

        String path = BackupOperations.pathSearchWithFileChooser(false);

        if (path == null) {
            Logger.logMessage("Exporting backups to PDF cancelled", Logger.LogLevel.INFO);
            return;
        }

        String filename = JOptionPane.showInputDialog(null, TranslationCategory.DIALOGS.getTranslation(TranslationKey.PDF_NAME_MESSAGE_INPUT));
        if (filename == null || filename.isEmpty()) {
            Logger.logMessage("Exporting backups to PDF cancelled", Logger.LogLevel.INFO);
            return;
        }

        // Validate filename
        if (!filename.matches("[a-zA-Z0-9-_ ]+")) {
            JOptionPane.showMessageDialog(null, TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_MESSAGE_INVALID_FILENAME), TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_GENERIC_TITLE), JOptionPane.ERROR_MESSAGE);
            Logger.logMessage("Exporting backups to PDF cancelled due to invalid file name", Logger.LogLevel.INFO);
            return;
        }

        // Build full path
        String fullPath = Paths.get(path, filename + ".pdf").toString();

        // Check if the file exists
        File file = new File(fullPath);
        if (file.exists()) {
            int overwrite = JOptionPane.showConfirmDialog(null, TranslationCategory.DIALOGS.getTranslation(TranslationKey.DUPLICATED_FILE_NAME_MESSAGE), TranslationCategory.DIALOGS.getTranslation(TranslationKey.CONFIRMATION_REQUIRED_TITLE), JOptionPane.YES_NO_OPTION);
            if (overwrite != JOptionPane.YES_OPTION) {
                Logger.logMessage("Exporting backups to PDF cancelled by user (file exists)", Logger.LogLevel.INFO);
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
            Logger.logMessage("Error exporting backups to PDF: " + ex.getMessage(), Logger.LogLevel.ERROR, ex);
            JOptionPane.showMessageDialog(null, TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_MESSAGE_FOR_EXPORTING_TO_PDF) + ex.getMessage(), TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_GENERIC_TITLE), JOptionPane.ERROR_MESSAGE);
        } finally {
            Logger.logMessage("Exporting backups to PDF finished", Logger.LogLevel.INFO);
        }
    }

    public static void exportAsCSV(ArrayList<Backup> backups, String header) {
        Logger.logMessage("Exporting backups to CSV", Logger.LogLevel.INFO);

        String path = BackupOperations.pathSearchWithFileChooser(false);

        if (path == null) {
            Logger.logMessage("Exporting backups to CSV cancelled", Logger.LogLevel.INFO);
            return;
        }

        String filename = JOptionPane.showInputDialog(null, TranslationCategory.DIALOGS.getTranslation(TranslationKey.CSV_NAME_MESSAGE_INPUT));
        if (filename == null || filename.isEmpty()) {
            Logger.logMessage("Exporting backups to CSV cancelled", Logger.LogLevel.INFO);
            return;
        }

        // Validate filename
        if (!filename.matches("[a-zA-Z0-9-_ ]+")) {
            JOptionPane.showMessageDialog(null, TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_MESSAGE_INVALID_FILENAME), TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_GENERIC_TITLE), JOptionPane.ERROR_MESSAGE);
            Logger.logMessage("Exporting backups to CSV cancelled due to invalid file name", Logger.LogLevel.INFO);
            return;
        }

        // Build full path
        String fullPath = Paths.get(path, filename + ".csv").toString();

        // Check if the file exists
        File file = new File(fullPath);
        if (file.exists()) {
            int overwrite = JOptionPane.showConfirmDialog(null, TranslationCategory.DIALOGS.getTranslation(TranslationKey.DUPLICATED_FILE_NAME_MESSAGE), TranslationCategory.DIALOGS.getTranslation(TranslationKey.CONFIRMATION_REQUIRED_TITLE), JOptionPane.YES_NO_OPTION);
            if (overwrite != JOptionPane.YES_OPTION) {
                Logger.logMessage("Exporting backups to CSV cancelled by user (file exists)", Logger.LogLevel.INFO);
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
            Logger.logMessage("Error exporting backups to CSV: " + ex.getMessage(), Logger.LogLevel.ERROR, ex);
            JOptionPane.showMessageDialog(null, TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_MESSAGE_FOR_EXPORTING_TO_CSV) + ex.getMessage(), TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_GENERIC_TITLE), JOptionPane.ERROR_MESSAGE);
        } finally {
            Logger.logMessage("Exporting backups to CSV finished", Logger.LogLevel.INFO);
        }
    }

}
