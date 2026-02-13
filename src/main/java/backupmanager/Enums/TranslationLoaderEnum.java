package backupmanager.Enums;

import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class TranslationLoaderEnum {

    private static final Logger logger = LoggerFactory.getLogger(TranslationLoaderEnum.class);

    public enum TranslationCategory {
        GENERAL("General"),
        MENU("Menu"),
        TABBED_FRAMES("TabbedFrames"),
        BACKUP_ENTRY("BackupEntry"),
        BACKUP_LIST("BackupList"),
        TIME_PICKER_DIALOG("TimePickerDialog"),
        PREFERENCES_DIALOG("PreferencesDialog"),
        USER_DIALOG("UserDialog"),
        PROGRESS_BACKUP_FRAME("ProgressBackupFrame"),
        TRAY_ICON("TrayIcon"),
        DIALOGS("Dialogs"),
        SUBSCRIPTION("Subscription");

        private final String categoryName;
        private final Map<TranslationKey, String> translations = new HashMap<>();

        TranslationCategory(String categoryName) {
            this.categoryName = categoryName;
        }

        public void addTranslation(TranslationKey key, String value) {
            translations.put(key, value);
        }

        // Updated getTranslation method
        public String getTranslation(TranslationKey key) {
            return translations.getOrDefault(key, key.getDefaultValue());
        }

        public String getCategoryName() {
            return categoryName;
        }
    }

    public enum TranslationKey {
        // General
        APP_NAME("AppName", "Backup Manager"),
        VERSION("Version", "Version"),
        BACKUP("Backup", "Backup"),
        FROM("From", "From"),
        TO("To", "To"),
        CLOSE_BUTTON("CloseButton", "Close"),
        OK_BUTTON("OkButton", "Ok"),
        CANCEL_BUTTON("CancelButton", "Cancel"),
        APPLY_BUTTON("ApplyButton", "Apply"),
        SAVE_BUTTON("SaveButton", "Save"),
        CREATE_BUTTON("CreateButton", "Create"),

        // Menu
        FILE("File", "File"),
        OPTIONS("Options", "Options"),
        ABOUT("About", "About"),
        HELP("Help", "Help"),
        BUG_REPORT("BugReport", "Report a bug"),
        CLEAR("Clear", "Clear"),
        DONATE("Donate", "Donate"),
        HISTORY("History", "History"),
        INFO_PAGE("InfoPage", "Info"),
        NEW("New", "New"),
        QUIT("Quit", "Quit"),
        SAVE("Save", "Save"),
        PREFERENCES("Preferences", "Preferences"),
        IMPORT("Import", "Import"),
        EXPORT("Export", "Export"),
        SAVE_WITH_NAME("SaveWithName", "Save with name"),
        SHARE("Share", "Share"),
        SUPPORT("Support", "Support"),
        WEBSITE("Website", "Website"),

        // TabbedFrames
        BACKUP_ENTRY("BackupEntry", "Backup Entry"),
        BACKUP_LIST("BackupList", "Backup List"),

        // BackupEntry
        PAGE_TITLE("PageTitle", "Backup Entry"),
        CURRENT_FILE("CurrentFile", "Current file"),
        NOTES("Notes", "Notes"),
        LAST_BACKUP("LastBackup", "Last backup"),
        SINGLE_BACKUP_BUTTON("SingleBackupButton", "Single Backup"),
        AUTO_BACKUP_BUTTON("AutoBackupButton", "Auto Backup"),
        AUTO_BACKUP_BUTTON_ON("AutoBackupButtonON", "Auto Backup (ON)"),
        AUTO_BACKUP_BUTTON_OFF("AutoBackupButtonOFF", "Auto Backup (OFF)"),
        INITIAL_PATH_PLACEHOLDER("InitialPathPlaceholder", "Initial path"),
        DESTINATION_PATH_PLACEHOLDER("DestinationPathPlaceholder", "Destination path"),
        BACKUP_NAME("BackupName", "Backup name"),
        BACKUP_NAME_TOOLTIP("BackupNameTooltip", "(Required) Backup name"),
        INITIAL_PATH_TOOLTIP("InitialPathTooltip", "(Required) Initial path"),
        DESTINATION_PATH_TOOLTIP("DestinationPathTooltip", "(Required) Destination path"),
        INITIAL_FILE_CHOOSER_TOOLTIP("InitialFileChooserTooltip", "Open file explorer"),
        DESTINATION_FILE_CHOOSER_TOOLTIP("DestinationFileChooserTooltip", "Open file explorer"),
        NOTES_TOOLTIP("NotesTooltip", "(Optional) Backup description"),
        SINGLE_BACKUP_TOOLTIP("SingleBackupTooltip", "Perform the backup"),
        AUTO_BACKUP_TOOLTIP("AutoBackupTooltip", "Enable/Disable automatic backup"),
        TIME_PICKER_TOOLTIP("TimePickerTooltip", "Time picker"),
        MAX_BACKUPS_TO_KEEP("MaxBackupsToKeep", "Max backups to keep"),
        MAX_BACKUPS_TO_KEEP_TOOLTIP("MaxBackupsToKeepTooltip", "Maximum number of backups before removing the oldest."),

        // BackupList
        BACKUP_NAME_COLUMN("BackupNameColumn", "Backup Name"),
        INITIAL_PATH_COLUMN("InitialPathColumn", "Initial Path"),
        DESTINATION_PATH_COLUMN("DestinationPathColumn", "Destination Path"),
        LAST_BACKUP_COLUMN("LastBackupColumn", "Last Backup"),
        AUTOMATIC_BACKUP_COLUMN("AutomaticBackupColumn", "Automatic Backup"),
        NEXT_BACKUP_DATE_COLUMN("NextBackupDateColumn", "Next Backup Date"),
        TIME_INTERVAL_COLUMN("TimeIntervalColumn", "Time Interval"),
        BACKUP_NAME_DETAIL("BackupNameDetail", "BackupName"),
        INITIAL_PATH_DETAIL("InitialPathDetail", "InitialPath"),
        DESTINATION_PATH_DETAIL("DestinationPathDetail", "DestinationPath"),
        LAST_BACKUP_DETAIL("LastBackupDetail", "LastBackup"),
        NEXT_BACKUP_DATE_DETAIL("NextBackupDateDetail", "NextBackup"),
        TIME_INTERVAL_DETAIL("TimeIntervalDetail", "TimeInterval"),
        CREATION_DATE_DETAIL("CreationDateDetail", "CreationDate"),
        LAST_UPDATE_DATE_DETAIL("LastUpdateDateDetail", "LastUpdateDate"),
        BACKUP_COUNT_DETAIL("BackupCountDetail", "BackupCount"),
        NOTES_DETAIL("NotesDetail", "Notes"),
        MAX_BACKUPS_TO_KEEP_DETAIL("MaxBackupsToKeepDetail", "MaxBackupsToKeep"),
        ADD_BACKUP_TOOLTIP("AddBackupTooltip", "Add new backup"),
        EXPORT_AS("ExportAs", "Export as: "),
        EXPORT_AS_PDF_TOOLTIP("ExportAsPdfTooltip", "Export as PDF"),
        EXPORT_AS_CSV_TOOLTIP("ExportAsCsvTooltip", "Export as CSV"),
        RESEARCH_BAR_TOOLTIP("ResearchBarTooltip", "Research bar"),
        RESEARCH_BAR_PLACEHOLDER("ResearchBarPlaceholder", "Search..."),
        EDIT_POPUP("EditPopup", "Edit"),
        DELETE_POPUP("DeletePopup", "Delete"),
        INTERRUPT_POPUP("InterruptPopup", "Interrupt"),
        DUPLICATE_POPUP("DuplicatePopup", "Duplicate"),
        RENAME_BACKUP_POPUP("RenameBackupPopup", "Rename backup"),
        OPEN_INITIAL_FOLDER_POPUP("OpenInitialFolderPopup", "Open initial path"),
        OPEN_DESTINATION_FOLDER_POPUP("OpenDestinationFolderPopup", "Open destination path"),
        BACKUP_POPUP("BackupPopup", "Backup"),
        SINGLE_BACKUP_POPUP("SingleBackupPopup", "Run single backup"),
        AUTO_BACKUP_POPUP("AutoBackupPopup", "Auto backup"),
        COPY_TEXT_POPUP("CopyTextPopup", "Copy text"),
        COPY_BACKUP_NAME_POPUP("CopyBackupNamePopup", "Copy backup name"),
        COPY_INITIAL_PATH_POPUP("CopyInitialPathPopup", "Copy initial path"),
        COPY_DESTINATION_PATH_BACKUP("CopyDestinationPathPopup", "Copy destination path"),

        // TimePickerDialog
        TIME_INTERVAL_TITLE("TimeIntervalTitle", "Time interval for auto backup"),
        DESCRIPTION("Description", "Select how often to perform the automatic backup by choosing the frequency in days, hours, and minutes."),
        DAYS("Days", "Days"),
        HOURS("Hours", "Hours"),
        MINUTES("Minutes", "Minutes"),
        SPINNER_TOOLTIP("SpinnerTooltip", "Mouse wheel to adjust the value"),

        // PreferencesDialog
        PREFERENCES_TITLE("PreferencesTitle", "Preferences"),
        LANGUAGE("Language", "Language"),
        THEME("Theme", "Theme"),

        // User dialog
        USER_TITLE("UserTitle", "Insert your data"),
        USER_NAME("Name", "Name"),
        USER_SURNAME("Surname", "Surname"),
        USER_EMAIL("Email", "Email"),
        ERROR_MESSAGE_FOR_MISSING_DATA("ErrorMessageForMissingData", "Please fill in all the required fields."),
        ERROR_MESSAGE_FOR_WRONG_EMAIL("ErrorMessageForWrongEmail", "The provided email address is invalid. Please provide a correct one."),
        EMAIL_CONFIRMATION_SUBJECT("EmailConfirmationSubject", "Thank you for choosing Backup Manager!"),
        EMAIL_CONFIRMATION_BODY("EmailConfirmationBody", "Hi [UserName],\n\nThank you for downloading and registering **Backup Manager**, your new tool for secure and efficient backup management!\n\nThis is an automated email sent to confirm your registration. We will contact you by email only to inform you about new releases or important updates of the application.\n\nIn the meantime, if you have any questions, need assistance, or have suggestions, we are always here for you. You can reach us at **[SupportEmail]**.\n\nThank you again for choosing Backup Manager, and enjoy managing your backups!\n\nBest regards,\nThe Backup Manager Team"),

        // ProgressBackupFrame
        PROGRESS_BACKUP_TITLE("ProgressBackupTitle", "Backup in progress"),
        STATUS_COMPLETED("StatusCompleted", "Backup completed!"),
        STATUS_LOADING("StatusLoading", "Loading..."),

        // TrayIcon
        TRAY_TOOLTIP("TrayTooltip", "Backup Service"),
        OPEN_ACTION("OpenAction", "Quick Access"),
        EXIT_ACTION("ExitAction", "Exit"),
        SUCCESS_MESSAGE("SuccessMessage", "\nThe backup was successfully completed:"),
        ERROR_MESSAGE_INPUT_MISSING("ErrorMessageInputMissing", "\nError during automatic backup.\nInput Missing!"),
        ERROR_MESSAGE_FILES_NOT_EXISTING("ErrorMessageFilesNotExisting", "\nError during automatic backup.\nOne or both paths do not exist!"),
        ERROR_MESSAGE_SAME_PATHS("ErrorMessageSamePaths", "\nError during automatic backup.\nThe initial path and destination path cannot be the same. Please choose different paths!"),

        // Dialogs
        ERROR_GENERIC_TITLE("ErrorGenericTitle", "Error"),
        WARNING_GENERIC_TITLE("WarningGenericTitle", "Warning"),
        WARNING_BACKUP_ALREADY_IN_PROGRESS_MESSAGE("WarningBackupAlreadyInProgressMessage", "There is already a backup in progress. It is not possible to perform parallel backups"),
        WARNING_SHORT_TIME_INTERVAL_MESSAGE("WarningShortTimeIntervalMessage", "The selected time interval is very short. For optimal performance, we recommend setting it to at least one hour. Do you still want to proceed?"),

        ERROR_MESSAGE_FOR_FOLDER_NOT_EXISTING("ErrorMessageForFolderNotExisting", "The folder does not exist or is invalid"),
        ERROR_MESSAGE_FOR_SAVING_FILE_WITH_PATHS_EMPTY("ErrorMessageForSavingFileWithPathsEmpty", "Unable to save the file. Both the initial and destination paths must be specified and cannot be empty"),
        BACKUP_SAVED_CORRECTLY_TITLE("BackupSavedCorrectlyTitle", "Backup saved"),
        BACKUP_SAVED_CORRECTLY_MESSAGE("BackupSavedCorrectlyMessage", "saved successfully!"),
        ERROR_SAVING_BACKUP_MESSAGE("ErrorSavingBackupMessage", "Error saving backup"),
        BACKUP_NAME_INPUT("BackupNameInput", "Name of the backup"),
        CONFIRMATION_REQUIRED_TITLE("ConfirmationRequiredTitle", "Confirmation required"),
        DUPLICATED_BACKUP_NAME_MESSAGE("DuplicatedBackupNameMessage", "A backup with the same name already exists, do you want to overwrite it?"),
        BACKUP_LIST_CORRECTLY_EXPORTED_TITLE("BackupListCorrectlyExportedTitle", "Menu Export"),
        BACKUP_LIST_CORRECTLY_EXPORTED_MESSAGE("BackupListCorrectlyExportedMessage", "Backup list successfully exported to the Desktop!"),
        BACKUP_LIST_CORRECTLY_IMPORTED_TITLE("BackupListCorrectlyImportedTitle", "Menu Import"),
        BACKUP_LIST_CORRECTLY_IMPORTED_MESSAGE("BackupListCorrectlyImportedMessage", "Backup list successfully imported!"),
        BACKUP_NAME_ALREADY_USED_MESSAGE("BackupNameAlreadyUsedMessage", "Backup name already used!"),
        ERROR_MESSAGE_FOR_INCORRECT_INITIAL_PATH("ErrorMessageForIncorrectInitialPath", "Error during the backup operation: the initial path is incorrect!"),
        EXCEPTION_MESSAGE_TITLE("ExceptionMessageTitle", "Error..."),
        EXCEPTION_MESSAGE_CLIPBOARD_MESSAGE("ExceptionMessageClipboardMessage", "Error text has been copied to the clipboard."),
        EXCEPTION_MESSAGE_CLIPBOARD_BUTTON("ExceptionMessageClipboardButton", "Copy to clipboard"),
        EXCEPTION_MESSAGE_REPORT_BUTTON("ExceptionMessageReportButton", "Report the Problem"),
        EXCEPTION_MESSAGE_REPORT_MESSAGE("ExceptionMessageReportMessage", "Please report this error, either with an image of the screen or by copying the following error text (it is appreciable to provide a description of the operations performed before the error):"),
        ERROR_MESSAGE_OPENING_WEBSITE("ErrorMessageOpeningWebsite", "Failed to open the web page. Please try again."),
        CONFIRMATION_MESSAGE_FOR_CLEAR("ConfirmationMessageForClear", "Are you sure you want to clean the fields?"),
        CONFIRMATION_MESSAGE_FOR_UNSAVED_CHANGES("ConfirmationMessageForUnsavedChanges", "There are unsaved changes, do you want to save them before moving to another backup?"),
        ERROR_MESSAGE_OPEN_HISTORY_FILE("ErrorMessageOpenHistoryFile", "Error opening history file."),
        CONFIRMATION_MESSAGE_BEFORE_DELETE_BACKUP("ConfirmationMessageBeforeDeleteBackup", "Are you sure you want to delete this item? Please note, this action cannot be undone"),
        SHARE_LINK_COPIED_MESSAGE("ShareLinkCopiedMessage", "Share link copied to clipboard!"),
        CONFIRMATION_MESSAGE_CANCEL_AUTO_BACKUP("ConfirmationMessageCancelAutoBackup", "Are you sure you want to cancel automatic backups for this entry?"),
        CONFIRMATION_MESSAGE_CANCEL_SINGLE_BACKUP("ConfirmationMessageCancelSingleBackup", "Are you sure you want to cancel this backup?"),
        CONFIRMATION_MESSAGE_BEFORE_EXIT("ConfirmationMessageBeforeExit", "Are you sure you want to exit?"),
        ERROR_MESSAGE_UNEXPECTED("ErrorMessageUnexpected", "An unexpected error has occurred!"),
        ERROR_MESSAGE_PATHS_CANNOT_BE_SAME("ErrorMessagePathsCannotBeSame", "The initial path and destination path cannot be the same!"),
        ERROR_MESSAGE_PATHS_ARE_EMPTY("ErrorMessagePathsAreEmpty", "The initial path and destination path cannot be empty!"),
        ERROR_MESSAGE_INVALID_PATH("ErrorMessageInvalidPath", "The selected path is invalid!"),
        ERROR_MESSAGE_NOT_SUPPORTED_EMAIL("ErrorMessageNotSupportedEmail", "Your system does not support sending emails directly from this application."),
        ERROR_MESSAGE_NOT_SUPPORTED_EMAIL_GENERIC("ErrorMessageNotSupportedEmailGeneric", "Your system does not support sending emails."),
        ERROR_WRONG_TIME_INTERVAL("ErrorWrongTimeInterval", "The time interval is not correct"),
        AUTO_BACKUP_ACTIVATED_MESSAGE("AutoBackupActivatedMessage", "Auto Backup has been activated"),
        SETTED_EVERY_MESSAGE("SettedEveryMessage", "\nIs setted every"),
        DAYS_MESSAGE("DaysMessage", " days"),
        ERROR_MESSAGE_UNABLE_TO_SEND_EMAIL("ErrorMessageUnableToSendEmail", "Unable to send email. Please try again later."),
        INTERRUPT_BACKUP_PROCESS_MESSAGE("InterruptBackupProcessMessage", "Are you sure you want to stop this backup?"),
        ERROR_MESSAGE_INPUT_MISSING_GENERIC("ErrorMessageInputMissingGeneric", "Input Missing!"),
        ERROR_MESSAGE_SAVING_FILE("ErrorMessageForSavingFile", "Error saving file"),
        ERROR_MESSAGE_PATH_NOT_EXISTING("ErrorMessageForPathNotExisting", "One or both paths do not exist!"),
        ERROR_MESSAGE_SAME_PATHS_GENERIC("ErrorMessageForSamePaths", "The initial path and destination path cannot be the same. Please choose different paths!"),
        ERROR_MESSAGE_FOR_WRONG_FILE_EXTENSION_TITLE("ErrorMessageForWrongFileExtensionTitle", "Invalid File"),
        ERROR_MESSAGE_FOR_WRONG_FILE_EXTENSION_MESSAGE("ErrorMessageForWrongFileExtensionMessage", "Error: Please select a valid JSON file."),
        ERROR_MESSAGE_COUNTING_FILES("ErrorMessageCountingFiles", "Error occurred while calculating files to back up."),
        ERROR_MESSAGE_ZIPPING_GENERIC("ErrorMessageZippingGeneric", "Error occurred while zipping files."),
        ERROR_MESSAGE_ZIPPING_IO("ErrorMessageZippingIO", "Error occurred while zipping files: I/O error."),
        ERROR_MESSAGE_ZIPPING_SECURITY("ErrorMessageZippingSecurity", "Error occurred while zipping files: Security error."),
        SUCCESS_GENERIC_TITLE( "SuccessGenericTitle", "Success"),
        SUCCESSFULLY_EXPORTED_TO_CSV_MESSAGE("SuccessfullyExportedToCsvMessage", "Backups exported to CSV successfully!"),
        SUCCESSFULLY_EXPORTED_TO_PDF_MESSAGE("SuccessfullyExportedToPdfMessage", "Backups exported to PDF successfully!"),
        ERROR_MESSAGE_FOR_EXPORTING_TO_CSV("ErrorMessageForExportingToCsv", "Error exporting backups to CSV: "),
        ERROR_MESSAGE_FOR_EXPORTING_TO_PDF("ErrorMessageForExportingToPdf", "Error exporting backups to PDF: "),
        CSV_NAME_MESSAGE_INPUT("CsvNameMessageInput", "Enter the name of the CSV file."),
        PDF_NAME_MESSAGE_INPUT("PdfNameMessageInput", "Enter the name of the PDF file."),
        DUPLICATED_FILE_NAME_MESSAGE("DuplicatedFileNameMessage", "File already exists. Overwrite?"),
        ERROR_MESSAGE_INVALID_FILENAME("ErrorMessageInvalidFilename", "Invalid file name. Use only alphanumeric characters, dashes, and underscores."),
        CONFIRMATION_DELETION_TITLE("ConfirmationDeletionTitle", "Confirm Deletion"),
        CONFIRMATION_DELETION_MESSAGE("ConfirmationDeletionMessage", "Are you sure you want to delete the selected rows?"),

        // InfoPage
        INFO_PAGE_DESCRIPTION("InfoPageDescription", "Backup automatic system for files with the option to schedule and make backups regularly."),
        INFO_PAGE_FEATURES_TITLE("InfoPageFeaturesTitle", "Features"),
        INFO_PAGE_FEATURES_DESCRIPTION("InfoPageFeaturesDescription", "Backup files quickly and easily with automatic scheduling and periodic backups"),
        INFO_PAGE_BUTTON_TITLE("InfoPageButtonTitle", "OK"),
        INFO_PAGE_VERSION("InfoPageVersion", "Version: "),
        INFO_PAGE_DEVELOPER("InfoPageDeveloper", "Developer: "),
        INFO_PAGE_CREDITS("InfoPageCredits", "Credits"),
        INFO_PAGE_LICENSE("InfoPageLicense", "License"),

        // Subscription
        SUBSCRIPTION_EXPIRING_TITLE("ExpiringTitle", "Backup Manager subscription expiring soon"),
        SUBSCRIPTION_EXPIRING_MESSAGE("ExpiringMessage", "Your Backup Manager subscription is about to expire.\nAutomatic backups will continue to run until the expiration date.\nPlease contact support to renew it."),
        SUBSCRIPTION_EXPIRED_TITLE("ExpiredTitle", "Backup Manager subscription expired"),
        SUBSCRIPTION_EXPIRED_MESSAGE("ExpiredMessage", "Your Backup Manager subscription has expired.\nAutomatic backups will no longer run.\nPlease contact support to reactivate it.");

        private final String keyName;
        private final String defaultValue;

        private static final Map<String, TranslationKey> lookup = new HashMap<>();

        static {
            for (TranslationKey key : TranslationKey.values()) {
                lookup.put(key.keyName, key);
            }
        }

        // Constructor to assign both key and default value
        private TranslationKey(String keyName, String defaultValue) {
            this.keyName = keyName;
            this.defaultValue = defaultValue;
        }

        // Lookup by keyName (JSON key)
        public static TranslationKey fromKeyName(String keyName) {
            return lookup.get(keyName);
        }

        public String getKeyName() {
            return keyName;
        }

        public String getDefaultValue() {
            return defaultValue;
        }
    }

    public static void loadTranslations(String filePath) throws IOException {
        Gson gson = new Gson();

        try (FileReader reader = new FileReader(filePath, StandardCharsets.UTF_8)) {
            JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);

            for (TranslationCategory category : TranslationCategory.values()) {
                JsonObject categoryTranslations = jsonObject.getAsJsonObject(category.getCategoryName());

                if (categoryTranslations != null) {
                    for (Map.Entry<String, JsonElement> entry : categoryTranslations.entrySet()) {
                        String key = entry.getKey();
                        String value = entry.getValue().getAsString();

                        // Use fromKeyName to get the TranslationKey from the JSON key
                        TranslationKey translationKey = TranslationKey.fromKeyName(key);
                        if (translationKey != null) {
                            // If value is null or empty, fall back to the default value from the enum
                            String translationValue = (value != null && !value.isEmpty()) ? value : translationKey.getDefaultValue();
                            category.addTranslation(translationKey, translationValue);
                        } else {
                            logger.warn("Unrecognized key in JSON: " + key + ", using default value");
                        }
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("An error occurred: " + ex.getMessage(), ex);
        }
    }
}
