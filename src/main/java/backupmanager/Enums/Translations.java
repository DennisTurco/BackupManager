package backupmanager.Enums;

import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import backupmanager.Managers.LanguageManager;

public class Translations {

    private static final Logger logger = LoggerFactory.getLogger(Translations.class);

    public enum TCategory {
        GENERAL("General"),
        MENU("Menu"),
        TABBED_FRAMES("TabbedFrames"),
        BACKUP_ENTRY("BackupEntry"),
        BACKUP_LIST("BackupList"),
        TIME_PICKER_DIALOG("TimePickerDialog"),
        USER_DIALOG("UserDialog"),
        PROGRESS_BACKUP_FRAME("ProgressBackupFrame"),
        TRAY_ICON("TrayIcon"),
        DIALOGS("Dialogs"),
        SUBSCRIPTION("Subscription"),
        HISTORY_LOGS("HistoryLogs"),
        ABOUT("About"),
        DASHBOARD("Dashboard"),
        SETTINGS("Settings"),
        SEARCH_BAR("SearchBar"),
        TOAST("Toast"),
        ;

        private final String categoryName;
        private final Map<TKey, String> translations = new HashMap<>();

        TCategory(String categoryName) {
            this.categoryName = categoryName;
        }

        public void addTranslation(TKey key, String value) {
            translations.put(key, value);
        }

        // Updated getTranslation method
        public String getTranslation(TKey key) {
            return translations.getOrDefault(key, key.getDefaultValue());
        }

        public String getCategoryName() {
            return categoryName;
        }

        public void clearTranslations() {
            translations.clear();
        }
    }

    public enum TKey {
        // General
        APP_NAME(TCategory.GENERAL, "AppName", "Backup Manager"),
        VERSION(TCategory.GENERAL, "Version", "Version"),
        BACKUP(TCategory.GENERAL, "Backup", "Backup"),
        FROM(TCategory.GENERAL, "From", "From"),
        TO(TCategory.GENERAL, "To", "To"),
        CLOSE_BUTTON(TCategory.GENERAL, "CloseButton", "Close"),
        OK_BUTTON(TCategory.GENERAL, "OkButton", "Ok"),
        CANCEL_BUTTON(TCategory.GENERAL, "CancelButton", "Cancel"),
        APPLY_BUTTON(TCategory.GENERAL, "ApplyButton", "Apply"),
        SAVE_BUTTON(TCategory.GENERAL, "SaveButton", "Save"),
        CREATE_BUTTON(TCategory.GENERAL, "CreateButton", "Create"),
        EDIT_BUTTON(TCategory.GENERAL, "EditButton", "Edit"),
        DELETE_BUTTON(TCategory.GENERAL, "DeleteButton", "Delete"),
        CONTACT_US(TCategory.GENERAL, "ContactUs", "Contact us"),
        QUICK_SEARCH(TCategory.GENERAL, "QuickSearch", "Quick Search..."),

        // Menu
        SUBMENU_MAIN(TCategory.MENU, "SubmenuMain", "MAIN"),
        SUBMENU_OTHER(TCategory.MENU, "SubmenuOther", "OTHER"),
        FILE(TCategory.MENU, "File", "File"),
        OPTIONS(TCategory.MENU, "Options", "Options"),
        ABOUT(TCategory.MENU, "About", "About"),
        HELP(TCategory.MENU, "Help", "Help"),
        BUG_REPORT(TCategory.MENU, "BugReport", "Report a bug"),
        CLEAR(TCategory.MENU, "Clear", "Clear"),
        DONATE(TCategory.MENU, "Donate", "Support the project"),
        HISTORY(TCategory.MENU, "History", "History"),
        INFO_PAGE(TCategory.MENU, "InfoPage", "Info"),
        NEW(TCategory.MENU, "New", "New"),
        QUIT(TCategory.MENU, "Quit", "Quit"),
        SAVE(TCategory.MENU, "Save", "Save"),
        PREFERENCES(TCategory.MENU, "Preferences", "Preferences"),
        IMPORT(TCategory.MENU, "Import", "Import"),
        EXPORT(TCategory.MENU, "Export", "Export"),
        SAVE_WITH_NAME(TCategory.MENU, "SaveWithName", "Save with name"),
        SHARE(TCategory.MENU, "Share", "Share"),
        SUPPORT(TCategory.MENU, "Support", "Support"),
        WEBSITE(TCategory.MENU, "Website", "Website"),
        BACKUP_TABLE(TCategory.MENU, "Backups", "Backup List"),
        CREATE_BACKUP(TCategory.MENU, "CreateBackup", "Create new backup"),
        IMPORT_BACKUP(TCategory.MENU, "ImportBackup", "Import backups from Csv"),
        EXPORT_BACKUP(TCategory.MENU, "ExportBackup", "Export backups to Csv"),
        DASHBOARD(TCategory.MENU, "Dashboard", "Dashboard"),
        GITHUB_PAGE(TCategory.MENU, "Github", "Github page"),
        PAYPAL(TCategory.MENU, "Paypal", "Paypal"),
        BUYMEACOFFE(TCategory.MENU, "BuyMeACoffe", "Buy me a coffe"),
        SUBSCRIPTION(TCategory.MENU, "Subscription", "Subscription"),

        // TabbedFrames
        BACKUP_ENTRY(TCategory.TABBED_FRAMES, "BackupEntry", "Backup Entry"),
        BACKUP_LIST(TCategory.TABBED_FRAMES, "BackupList", "Backup List"),

        // BackupEntry
        PAGE_TITLE(TCategory.BACKUP_ENTRY, "PageTitle", "Backup Entry"),
        PAGE_SUBTITLE_CREATE(TCategory.BACKUP_ENTRY, "PageSubtitleCreate", "Create Backup"),
        PAGE_SUBTITLE_EDIT(TCategory.BACKUP_ENTRY, "PageSubtitleEdit", "Edit Backup"),
        PAGE_SUBTITLE_INFO(TCategory.BACKUP_ENTRY, "PageSubtitleInfo", "Backup Information"),
        PAGE_SUBTITLE_SETTINGS(TCategory.BACKUP_ENTRY, "PageSubtitleSettings", "Backups Settings"),
        CURRENT_FILE(TCategory.BACKUP_ENTRY, "CurrentFile", "Current file"),
        NOTES(TCategory.BACKUP_ENTRY, "Notes", "Notes"),
        PATHS(TCategory.BACKUP_ENTRY, "Paths", "Paths"),
        LAST_BACKUP(TCategory.BACKUP_ENTRY, "LastBackup", "Last backup"),
        SINGLE_BACKUP_BUTTON(TCategory.BACKUP_ENTRY, "SingleBackupButton", "Single Backup"),
        AUTO_BACKUP_BUTTON(TCategory.BACKUP_ENTRY, "AutoBackupButton", "Auto Backup"),
        AUTO_BACKUP_BUTTON_ON(TCategory.BACKUP_ENTRY, "AutoBackupButtonON", "Auto Backup (ON)"),
        AUTO_BACKUP_BUTTON_OFF(TCategory.BACKUP_ENTRY, "AutoBackupButtonOFF", "Auto Backup (OFF)"),
        BACKUP_NAME_PLACEHOLDER(TCategory.BACKUP_ENTRY, "BackupNamePlaceholder", "Backup name (unique)"),
        INITIAL_PATH_PLACEHOLDER(TCategory.BACKUP_ENTRY, "InitialPathPlaceholder", "Target path e.g. C:\\Users\\Admin\\Documents"),
        DESTINATION_PATH_PLACEHOLDER(TCategory.BACKUP_ENTRY, "DestinationPathPlaceholder", "Destination folder e.g. D:\\Backups"),
        BACKUP_NAME(TCategory.BACKUP_ENTRY, "BackupName", "Backup name"),
        BACKUP_NAME_TOOLTIP(TCategory.BACKUP_ENTRY, "BackupNameTooltip", "(Required) Backup name"),
        INITIAL_PATH_TOOLTIP(TCategory.BACKUP_ENTRY, "InitialPathTooltip", "(Required) Initial path"),
        DESTINATION_PATH_TOOLTIP(TCategory.BACKUP_ENTRY, "DestinationPathTooltip", "(Required) Destination path"),
        INITIAL_FILE_CHOOSER_TOOLTIP(TCategory.BACKUP_ENTRY, "InitialFileChooserTooltip", "Open file explorer"),
        DESTINATION_FILE_CHOOSER_TOOLTIP(TCategory.BACKUP_ENTRY, "DestinationFileChooserTooltip", "Open file explorer"),
        NOTES_TOOLTIP(TCategory.BACKUP_ENTRY, "NotesTooltip", "(Optional) Backup description"),
        SINGLE_BACKUP_TOOLTIP(TCategory.BACKUP_ENTRY, "SingleBackupTooltip", "Perform the backup"),
        AUTO_BACKUP_TOOLTIP(TCategory.BACKUP_ENTRY, "AutoBackupTooltip", "Enable/Disable automatic backup"),
        TIME_PICKER_TOOLTIP(TCategory.BACKUP_ENTRY, "TimePickerTooltip", "Time picker"),
        MAX_BACKUPS_TO_KEEP(TCategory.BACKUP_ENTRY, "MaxBackupsToKeep", "Max backups to keep"),
        MAX_BACKUPS_TO_KEEP_TOOLTIP(TCategory.BACKUP_ENTRY, "MaxBackupsToKeepTooltip", "Maximum number of backups before removing the oldest."),

        // BackupList
        BACKUP_LIST_TITLE(TCategory.BACKUP_LIST, "BackupListTitle", "Backup List"),
        BACKUP_LIST_DESCRIPTION(TCategory.BACKUP_LIST, "BackupListDescription", "Manage and monitor backup configurations, including creation, editing, scheduling, and execution."),
        BACKUP_NAME_COLUMN(TCategory.BACKUP_LIST, "BackupNameColumn", "Backup Name"),
        INITIAL_PATH_COLUMN(TCategory.BACKUP_LIST, "InitialPathColumn", "Initial Path"),
        DESTINATION_PATH_COLUMN(TCategory.BACKUP_LIST, "DestinationPathColumn", "Destination Path"),
        LAST_BACKUP_COLUMN(TCategory.BACKUP_LIST, "LastBackupColumn", "Last Backup"),
        AUTOMATIC_BACKUP_COLUMN(TCategory.BACKUP_LIST, "AutomaticBackupColumn", "Automatic Backup"),
        NEXT_BACKUP_DATE_COLUMN(TCategory.BACKUP_LIST, "NextBackupDateColumn", "Next Backup Date"),
        TIME_INTERVAL_COLUMN(TCategory.BACKUP_LIST, "TimeIntervalColumn", "Interval (gg.HH:mm)"),
        MAX_BACKUPS_COLUMN(TCategory.BACKUP_LIST, "MaxBackupsColumn", "Max Backups To Keep"),
        BACKUP_NAME_DETAIL(TCategory.BACKUP_LIST, "BackupNameDetail", "BackupName"),
        INITIAL_PATH_DETAIL(TCategory.BACKUP_LIST, "InitialPathDetail", "InitialPath"),
        DESTINATION_PATH_DETAIL(TCategory.BACKUP_LIST, "DestinationPathDetail", "DestinationPath"),
        LAST_BACKUP_DETAIL(TCategory.BACKUP_LIST, "LastBackupDetail", "LastBackup"),
        NEXT_BACKUP_DATE_DETAIL(TCategory.BACKUP_LIST, "NextBackupDateDetail", "NextBackup"),
        TIME_INTERVAL_DETAIL(TCategory.BACKUP_LIST, "TimeIntervalDetail", "TimeInterval"),
        CREATION_DATE_DETAIL(TCategory.BACKUP_LIST, "CreationDateDetail", "CreationDate"),
        LAST_UPDATE_DATE_DETAIL(TCategory.BACKUP_LIST, "LastUpdateDateDetail", "LastUpdateDate"),
        BACKUP_COUNT_DETAIL(TCategory.BACKUP_LIST, "BackupCountDetail", "BackupCount"),
        NOTES_DETAIL(TCategory.BACKUP_LIST, "NotesDetail", "Notes"),
        MAX_BACKUPS_TO_KEEP_DETAIL(TCategory.BACKUP_LIST, "MaxBackupsToKeepDetail", "MaxBackupsToKeep"),
        ADD_BACKUP_TOOLTIP(TCategory.BACKUP_LIST, "AddBackupTooltip", "Add new backup"),
        EXPORT_AS(TCategory.BACKUP_LIST, "ExportAs", "Export as: "),
        EXPORT_AS_PDF_TOOLTIP(TCategory.BACKUP_LIST, "ExportAsPdfTooltip", "Export as PDF"),
        EXPORT_AS_CSV_TOOLTIP(TCategory.BACKUP_LIST, "ExportAsCsvTooltip", "Export as CSV"),
        RESEARCH_BAR_TOOLTIP(TCategory.BACKUP_LIST, "ResearchBarTooltip", "Research bar"),
        RESEARCH_BAR_PLACEHOLDER(TCategory.BACKUP_LIST, "ResearchBarPlaceholder", "Search..."),
        EDIT_POPUP(TCategory.BACKUP_LIST, "EditPopup", "Edit"),
        DELETE_POPUP(TCategory.BACKUP_LIST, "DeletePopup", "Delete"),
        INTERRUPT_POPUP(TCategory.BACKUP_LIST, "InterruptPopup", "Interrupt"),
        DUPLICATE_POPUP(TCategory.BACKUP_LIST, "DuplicatePopup", "Duplicate"),
        RENAME_BACKUP_POPUP(TCategory.BACKUP_LIST, "RenameBackupPopup", "Rename backup"),
        OPEN_INITIAL_FOLDER_POPUP(TCategory.BACKUP_LIST, "OpenInitialFolderPopup", "Open initial path"),
        OPEN_DESTINATION_FOLDER_POPUP(TCategory.BACKUP_LIST, "OpenDestinationFolderPopup", "Open destination path"),
        BACKUP_POPUP(TCategory.BACKUP_LIST, "BackupPopup", "Backup"),
        SINGLE_BACKUP_POPUP(TCategory.BACKUP_LIST, "SingleBackupPopup", "Run single backup"),
        AUTO_BACKUP_POPUP(TCategory.BACKUP_LIST, "AutoBackupPopup", "Auto backup"),
        COPY_TEXT_POPUP(TCategory.BACKUP_LIST, "CopyTextPopup", "Copy text"),
        COPY_BACKUP_NAME_POPUP(TCategory.BACKUP_LIST, "CopyBackupNamePopup", "Copy backup name"),
        COPY_INITIAL_PATH_POPUP(TCategory.BACKUP_LIST, "CopyInitialPathPopup", "Copy initial path"),
        COPY_DESTINATION_PATH_BACKUP(TCategory.BACKUP_LIST, "CopyDestinationPathPopup", "Copy destination path"),

        // TimePickerDialog
        TIME_INTERVAL_TITLE(TCategory.TIME_PICKER_DIALOG, "TimeIntervalTitle", "Time interval for auto backup"),
        DESCRIPTION(TCategory.TIME_PICKER_DIALOG, "Description", "Select how often to perform the automatic backup by choosing the frequency in days, hours, and minutes."),
        DAYS(TCategory.TIME_PICKER_DIALOG, "Days", "Days"),
        HOURS(TCategory.TIME_PICKER_DIALOG, "Hours", "Hours"),
        MINUTES(TCategory.TIME_PICKER_DIALOG, "Minutes", "Minutes"),
        SPINNER_TOOLTIP(TCategory.TIME_PICKER_DIALOG, "SpinnerTooltip", "Mouse wheel to adjust the value"),

        // User dialog
        USER_TITLE(TCategory.USER_DIALOG, "UserTitle", "Insert your data"),
        USER_DESCRIPTION(TCategory.USER_DIALOG, "UserDescription", "Please enter your data to access the system"),
        USER_NAME(TCategory.USER_DIALOG, "Name", "Name"),
        USER_SURNAME(TCategory.USER_DIALOG, "Surname", "Surname"),
        USER_EMAIL(TCategory.USER_DIALOG, "Email", "Email"),
        USER_NAME_PLACEHOLDER(TCategory.USER_DIALOG, "UserNamePlaceholder", "Enter your name"),
        USER_SURNAME_PLACEHOLDER(TCategory.USER_DIALOG, "UserSurnamePlaceholder", "Enter your surname"),
        USER_EMAIL_PLACEHOLDER(TCategory.USER_DIALOG, "UserEmailPlaceholder", "Enter your email"),
        EMAIL_CONFIRMATION_SUBJECT(TCategory.USER_DIALOG, "EmailConfirmationSubject", "Thank you for choosing Backup Manager!"),
        EMAIL_CONFIRMATION_BODY(TCategory.USER_DIALOG, "EmailConfirmationBody", "Hi [UserName],\n\nThank you for downloading and registering **Backup Manager**, your new tool for secure and efficient backup management!\n\nThis is an automated email sent to confirm your registration. We will contact you by email only to inform you about new releases or important updates of the application.\n\nIn the meantime, if you have any questions, need assistance, or have suggestions, we are always here for you. You can reach us at **[SupportEmail]**.\n\nThank you again for choosing Backup Manager, and enjoy managing your backups!\n\nBest regards,\nThe Backup Manager Team"),

        // ProgressBackupFrame
        PROGRESS_BACKUP_TITLE(TCategory.PROGRESS_BACKUP_FRAME, "ProgressBackupTitle", "Backup in progress"),
        STATUS_COMPLETED(TCategory.PROGRESS_BACKUP_FRAME, "StatusCompleted", "Backup completed!"),
        STATUS_LOADING(TCategory.PROGRESS_BACKUP_FRAME, "StatusLoading", "Loading..."),

        // TrayIcon
        TRAY_TOOLTIP(TCategory.TRAY_ICON, "TrayTooltip", "Backup Service"),
        OPEN_ACTION(TCategory.TRAY_ICON, "OpenAction", "Quick Access"),
        EXIT_ACTION(TCategory.TRAY_ICON, "ExitAction", "Exit"),
        SUCCESS_MESSAGE(TCategory.TRAY_ICON, "SuccessMessage", "\nThe backup was successfully completed:"),
        ERROR_MESSAGE_INPUT_MISSING(TCategory.TRAY_ICON, "ErrorMessageInputMissing", "\nError during automatic backup.\nInput Missing!"),
        ERROR_MESSAGE_FILES_NOT_EXISTING(TCategory.TRAY_ICON, "ErrorMessageFilesNotExisting", "\nError during automatic backup.\nOne or both paths do not exist!"),
        ERROR_MESSAGE_SAME_PATHS(TCategory.TRAY_ICON, "ErrorMessageSamePaths", "\nError during automatic backup.\nThe initial path and destination path cannot be the same. Please choose different paths!"),

        // Dialogs
        ERROR_GENERIC_TITLE(TCategory.DIALOGS, "ErrorGenericTitle", "Error"),
        WARNING_GENERIC_TITLE(TCategory.DIALOGS, "WarningGenericTitle", "Warning"),
        WARNING_BACKUP_ALREADY_IN_PROGRESS_MESSAGE(TCategory.DIALOGS, "WarningBackupAlreadyInProgressMessage", "There is already a backup in progress. It is not possible to perform parallel backups"),
        WARNING_SHORT_TIME_INTERVAL_MESSAGE(TCategory.DIALOGS, "WarningShortTimeIntervalMessage", "The selected time interval is very short. For optimal performance, we recommend setting it to at least one hour. Do you still want to proceed?"),

        ERROR_MESSAGE_FOR_FOLDER_NOT_EXISTING(TCategory.DIALOGS, "ErrorMessageForFolderNotExisting", "The folder does not exist or is invalid"),
        ERROR_MESSAGE_FOR_SAVING_FILE_WITH_PATHS_EMPTY(TCategory.DIALOGS, "ErrorMessageForSavingFileWithPathsEmpty", "Unable to save the file. Both the initial and destination paths must be specified and cannot be empty"),
        BACKUP_SAVED_CORRECTLY_TITLE(TCategory.DIALOGS, "BackupSavedCorrectlyTitle", "Backup saved"),
        BACKUP_SAVED_CORRECTLY_MESSAGE(TCategory.DIALOGS, "BackupSavedCorrectlyMessage", "saved successfully!"),
        ERROR_SAVING_BACKUP_MESSAGE(TCategory.DIALOGS, "ErrorSavingBackupMessage", "Error saving backup"),
        BACKUP_NAME_INPUT(TCategory.DIALOGS, "BackupNameInput", "Name of the backup"),
        CONFIRMATION_REQUIRED_TITLE(TCategory.DIALOGS, "ConfirmationRequiredTitle", "Confirmation required"),
        DUPLICATED_BACKUP_NAME_MESSAGE(TCategory.DIALOGS, "DuplicatedBackupNameMessage", "A backup with the same name already exists, do you want to overwrite it?"),
        BACKUP_LIST_CORRECTLY_EXPORTED_TITLE(TCategory.DIALOGS, "BackupListCorrectlyExportedTitle", "Menu Export"),
        BACKUP_LIST_CORRECTLY_EXPORTED_MESSAGE(TCategory.DIALOGS, "BackupListCorrectlyExportedMessage", "Backup list successfully exported to the Desktop!"),
        BACKUP_LIST_CORRECTLY_IMPORTED_TITLE(TCategory.DIALOGS, "BackupListCorrectlyImportedTitle", "Menu Import"),
        BACKUP_LIST_CORRECTLY_IMPORTED_MESSAGE(TCategory.DIALOGS, "BackupListCorrectlyImportedMessage", "Backup list successfully imported!"),
        BACKUP_NAME_ALREADY_USED_MESSAGE(TCategory.DIALOGS, "BackupNameAlreadyUsedMessage", "Backup name already used!"),
        ERROR_MESSAGE_FOR_INCORRECT_INITIAL_PATH(TCategory.DIALOGS, "ErrorMessageForIncorrectInitialPath", "Error during the backup operation: the initial path is incorrect!"),
        EXCEPTION_MESSAGE_TITLE(TCategory.DIALOGS, "ExceptionMessageTitle", "Error..."),
        EXCEPTION_MESSAGE_CLIPBOARD_MESSAGE(TCategory.DIALOGS, "ExceptionMessageClipboardMessage", "Error text has been copied to the clipboard."),
        EXCEPTION_MESSAGE_CLIPBOARD_BUTTON(TCategory.DIALOGS, "ExceptionMessageClipboardButton", "Copy to clipboard"),
        EXCEPTION_MESSAGE_REPORT_BUTTON(TCategory.DIALOGS, "ExceptionMessageReportButton", "Report the Problem"),
        EXCEPTION_MESSAGE_REPORT_MESSAGE(TCategory.DIALOGS, "ExceptionMessageReportMessage", "Please report this error, either with an image of the screen or by copying the following error text (it is appreciable to provide a description of the operations performed before the error):"),
        ERROR_MESSAGE_OPENING_WEBSITE(TCategory.DIALOGS, "ErrorMessageOpeningWebsite", "Failed to open the web page. Please try again."),
        CONFIRMATION_MESSAGE_FOR_CLEAR(TCategory.DIALOGS, "ConfirmationMessageForClear", "Are you sure you want to clean the fields?"),
        CONFIRMATION_MESSAGE_FOR_UNSAVED_CHANGES(TCategory.DIALOGS, "ConfirmationMessageForUnsavedChanges", "There are unsaved changes, do you want to save them before moving to another backup?"),
        ERROR_MESSAGE_OPEN_HISTORY_FILE(TCategory.DIALOGS, "ErrorMessageOpenHistoryFile", "Error opening history file."),
        CONFIRMATION_MESSAGE_BEFORE_DELETE_BACKUP(TCategory.DIALOGS, "ConfirmationMessageBeforeDeleteBackup", "Are you sure you want to delete this item? Please note, this action cannot be undone"),
        SHARE_LINK_COPIED_MESSAGE(TCategory.DIALOGS, "ShareLinkCopiedMessage", "Share link copied to clipboard!"),
        CONFIRMATION_MESSAGE_CANCEL_AUTO_BACKUP(TCategory.DIALOGS, "ConfirmationMessageCancelAutoBackup", "Are you sure you want to cancel automatic backups for this entry?"),
        CONFIRMATION_MESSAGE_CANCEL_SINGLE_BACKUP(TCategory.DIALOGS, "ConfirmationMessageCancelSingleBackup", "Are you sure you want to cancel this backup?"),
        CONFIRMATION_MESSAGE_BEFORE_EXIT(TCategory.DIALOGS, "ConfirmationMessageBeforeExit", "Are you sure you want to exit?"),
        ERROR_MESSAGE_UNEXPECTED(TCategory.DIALOGS, "ErrorMessageUnexpected", "An unexpected error has occurred!"),
        ERROR_MESSAGE_PATHS_CANNOT_BE_SAME(TCategory.DIALOGS, "ErrorMessagePathsCannotBeSame", "The initial path and destination path cannot be the same!"),
        ERROR_MESSAGE_PATHS_ARE_EMPTY(TCategory.DIALOGS, "ErrorMessagePathsAreEmpty", "The initial path and destination path cannot be empty!"),
        ERROR_MESSAGE_INVALID_PATH(TCategory.DIALOGS, "ErrorMessageInvalidPath", "The selected path is invalid!"),
        ERROR_MESSAGE_NOT_SUPPORTED_EMAIL(TCategory.DIALOGS, "ErrorMessageNotSupportedEmail", "Your system does not support sending emails directly from this application."),
        ERROR_MESSAGE_NOT_SUPPORTED_EMAIL_GENERIC(TCategory.DIALOGS, "ErrorMessageNotSupportedEmailGeneric", "Your system does not support sending emails."),
        ERROR_WRONG_TIME_INTERVAL(TCategory.DIALOGS, "ErrorWrongTimeInterval", "The time interval is not correct"),
        AUTO_BACKUP_ACTIVATED_MESSAGE(TCategory.DIALOGS, "AutoBackupActivatedMessage", "Auto Backup has been activated"),
        SETTED_EVERY_MESSAGE(TCategory.DIALOGS, "SettedEveryMessage", "\nIs setted every"),
        DAYS_MESSAGE(TCategory.DIALOGS, "DaysMessage", " days"),
        ERROR_MESSAGE_UNABLE_TO_SEND_EMAIL(TCategory.DIALOGS, "ErrorMessageUnableToSendEmail", "Unable to send email. Please try again later."),
        INTERRUPT_BACKUP_PROCESS_MESSAGE(TCategory.DIALOGS, "InterruptBackupProcessMessage", "Are you sure you want to stop this backup?"),
        ERROR_MESSAGE_INPUT_MISSING_GENERIC(TCategory.DIALOGS, "ErrorMessageInputMissingGeneric", "Input Missing!"),
        ERROR_MESSAGE_SAVING_FILE(TCategory.DIALOGS, "ErrorMessageForSavingFile", "Error saving file"),
        ERROR_MESSAGE_PATH_NOT_EXISTING(TCategory.DIALOGS, "ErrorMessageForPathNotExisting", "One or both paths do not exist!"),
        ERROR_MESSAGE_SAME_PATHS_GENERIC(TCategory.DIALOGS, "ErrorMessageForSamePaths", "The initial path and destination path cannot be the same. Please choose different paths!"),
        ERROR_MESSAGE_FOR_WRONG_FILE_EXTENSION_TITLE(TCategory.DIALOGS, "ErrorMessageForWrongFileExtensionTitle", "Invalid File"),
        ERROR_MESSAGE_FOR_WRONG_FILE_EXTENSION_MESSAGE(TCategory.DIALOGS, "ErrorMessageForWrongFileExtensionMessage", "Error: Please select a valid JSON file."),
        ERROR_MESSAGE_COUNTING_FILES(TCategory.DIALOGS, "ErrorMessageCountingFiles", "Error occurred while calculating files to back up."),
        ERROR_MESSAGE_ZIPPING_GENERIC(TCategory.DIALOGS, "ErrorMessageZippingGeneric", "Error occurred while zipping files."),
        ERROR_MESSAGE_ZIPPING_IO(TCategory.DIALOGS, "ErrorMessageZippingIO", "Error occurred while zipping files: I/O error."),
        ERROR_MESSAGE_ZIPPING_SECURITY(TCategory.DIALOGS, "ErrorMessageZippingSecurity", "Error occurred while zipping files: Security error."),
        SUCCESS_GENERIC_TITLE(TCategory.DIALOGS,  "SuccessGenericTitle", "Success"),
        SUCCESSFULLY_EXPORTED_TO_CSV_MESSAGE(TCategory.DIALOGS, "SuccessfullyExportedToCsvMessage", "Backups exported to CSV successfully!"),
        SUCCESSFULLY_EXPORTED_TO_PDF_MESSAGE(TCategory.DIALOGS, "SuccessfullyExportedToPdfMessage", "Backups exported to PDF successfully!"),
        ERROR_MESSAGE_FOR_EXPORTING_TO_CSV(TCategory.DIALOGS, "ErrorMessageForExportingToCsv", "Error exporting backups to CSV: "),
        ERROR_MESSAGE_FOR_EXPORTING_TO_PDF(TCategory.DIALOGS, "ErrorMessageForExportingToPdf", "Error exporting backups to PDF: "),
        CSV_NAME_MESSAGE_INPUT(TCategory.DIALOGS, "CsvNameMessageInput", "Enter the name of the CSV file."),
        PDF_NAME_MESSAGE_INPUT(TCategory.DIALOGS, "PdfNameMessageInput", "Enter the name of the PDF file."),
        DUPLICATED_FILE_NAME_MESSAGE(TCategory.DIALOGS, "DuplicatedFileNameMessage", "File already exists. Overwrite?"),
        ERROR_MESSAGE_INVALID_FILENAME(TCategory.DIALOGS, "ErrorMessageInvalidFilename", "Invalid file name. Use only alphanumeric characters, dashes, and underscores."),
        CONFIRMATION_DELETION_TITLE(TCategory.DIALOGS, "ConfirmationDeletionTitle", "Confirm Deletion"),
        CONFIRMATION_DELETION_MESSAGE(TCategory.DIALOGS, "ConfirmationDeletionMessage", "Are you sure you want to delete the selected rows?"),

        // Subscription
        SUBSCRIPTION_EXPIRING_TITLE(TCategory.SUBSCRIPTION, "ExpiringTitle", "Backup Manager subscription expiring soon"),
        SUBSCRIPTION_EXPIRING_MESSAGE(TCategory.SUBSCRIPTION, "ExpiringMessage", "Your Backup Manager subscription is about to expire.\nAutomatic backups will continue to run until the expiration date.\nPlease contact support to renew it."),
        SUBSCRIPTION_EXPIRED_TITLE(TCategory.SUBSCRIPTION, "ExpiredTitle", "Backup Manager subscription expired"),
        SUBSCRIPTION_EXPIRED_MESSAGE(TCategory.SUBSCRIPTION, "ExpiredMessage", "Your Backup Manager subscription has expired.\nAutomatic backups will no longer run.\nPlease contact support to reactivate it."),
        SUBSCRIPTION_ACTIVE(TCategory.SUBSCRIPTION, "ActiveLabel", "Active"),
        SUBSCRIPTION_EXPIRING(TCategory.SUBSCRIPTION, "ExpiringLabel", "Expiring"),
        SUBSCRIPTION_EXPIRED(TCategory.SUBSCRIPTION, "ExpiredLabel", "Expired"),
        SUBSCRIPTION_STATUS(TCategory.SUBSCRIPTION, "Status", "Subscription status"),
        SUBSCRIPTION_VALID_FROM(TCategory.SUBSCRIPTION, "ValidFrom", "Valid from"),
        SUBSCRIPTION_VALID_TO(TCategory.SUBSCRIPTION, "ValidTo", "Valid to"),
        SUBSCRIPTION_TO_EXTEND(TCategory.SUBSCRIPTION, "ToExtend", "to extend the subscription period."),

        // DASHBOARD
        DASHBOARD_TITLE(TCategory.DASHBOARD, "DashboardTitle", "Backup Analytics Dashboard"),
        DASHBOARD_CARD_TOTAL_CONFIGURATIONS(TCategory.DASHBOARD, "DashboardCardTotalConfigurations", "Total Backup Configurations"),
        DASHBOARD_CARD_TOTAL_EXECUTIONS(TCategory.DASHBOARD, "DashboardCardTotalExecutions", "Total Backup Executions"),
        DASHBOARD_CARD_SUCCESS_RATE(TCategory.DASHBOARD, "DashboardCardSuccessRate", "Success rate"),
        DASHBOARD_CARD_AVG_DURATION(TCategory.DASHBOARD, "DashboardCardAvgDuration", "Avg Backup Duration"),
        DASHBOARD_CARD_COMPRESSION_RATE(TCategory.DASHBOARD, "DashboardCardCompressionRate", "Compression Rate"),
        DASHBOARD_CHART_EXECUTIONS(TCategory.DASHBOARD, "DashboardChartExecutions", "Backup Executions"),
        DASHBOARD_CHART_AVG_DURATION(TCategory.DASHBOARD, "DashboardChartAvgDuration", "Average Backup Duration (min)"),

        // HISTORY_LOGS
        HISTORY_LOGS_TITLE(TCategory.HISTORY_LOGS, "HistoryLogsTitle", "History logs"),
        HISTORY_LOGS_DESCRIPTION(TCategory.HISTORY_LOGS, "HistoryLogsDescription", "Here you can find the application logs, useful for troubleshooting and understanding the application's behavior over time."),

        // ABOUT
        ABOUT_SYSTEM_INFORMATION(TCategory.ABOUT, "AboutSystemInformation", "System Information"),
        ABOUT_MESSAGE_BODY(TCategory.ABOUT, "AboutMessageBody", "<html><b>Backup Manager</b> is a simple and powerful application designed to automate folder and subfolder backups.<br><br> Users can schedule automatic backups or execute manual backups anytime.<br><br> Backup history is stored securely, allowing full control over saved data.<br><p>Visit <a href=[PROJECT_WEBSITE]>project website</a> for more information.</p></html>"),

        // SETTINGS
        SETTINGS_LAYOUT_TAB(TCategory.SETTINGS, "SettingsLayoutTab", "Layout"),
        SETTINGS_STYLE_TAB(TCategory.SETTINGS, "SettingsStyleTab", "Style"),
        SETTINGS_WINDOWS_LAYOUT(TCategory.SETTINGS, "SettingsWindowsLayout", "Windows Layout"),
        SETTINGS_WINDOWS_RIGHT(TCategory.SETTINGS, "SettingsWindowsRight", "Right to Left"),
        SETTINGS_WINDOWS_FULL(TCategory.SETTINGS, "SettingsWindowsFull", "Full Window Content"),
        SETTINGS_DRAWER_LAYOUT(TCategory.SETTINGS, "SettingsDrawerLayout", "Drawer layout"),
        SETTINGS_DRAWER_LEFT(TCategory.SETTINGS, "SettingsDrawerLeft", "Left"),
        SETTINGS_DRAWER_LEADING(TCategory.SETTINGS, "SettingsDrawerLeading", "Leading"),
        SETTINGS_DRAWER_TRAILING(TCategory.SETTINGS, "SettingsDrawerTrailing", "Trailing"),
        SETTINGS_DRAWER_RIGHT(TCategory.SETTINGS, "SettingsDrawerRight", "Right"),
        SETTINGS_DRAWER_TOP(TCategory.SETTINGS, "SettingsDrawerTop", "Top"),
        SETTINGS_DRAWER_BOTTOM(TCategory.SETTINGS, "SettingsDrawerBottom", "Bottom"),
        SETTINGS_MODAL_OPTION(TCategory.SETTINGS, "SettingsModalOption", "Default modal option"),
        SETTINGS_MODAL_ANIMATION(TCategory.SETTINGS, "SettingsModalAnimation", "Animation enable"),
        SETTINGS_MODAL_CLOSE(TCategory.SETTINGS, "SettingsModalClose", "Close on pressed escape"),
        SETTINGS_LANGUAGES_LAYOUT(TCategory.SETTINGS, "SettingsLanguagesLayout", "Language"),
        SETTINGS_ACCENT_LAYOUT(TCategory.SETTINGS, "SettingsAccentLayout", "Accent color"),
        SETTINGS_COLOR_PICKER_LAYOUT(TCategory.SETTINGS, "SettingsColorPickerLayout", "Color Picker"),
        SETTINGS_DRAWER_LINE_LAYOUT(TCategory.SETTINGS, "SettingsDrawerLineLayout", "Drawer line style"),
        SETTINGS_DRAWER_LINE_CURVED(TCategory.SETTINGS, "SettingsDrawerLineCurved", "Curved line style"),
        SETTINGS_DRAWER_DOT_LINE(TCategory.SETTINGS, "SettingsDrawerDotLine", "Straight dot line style"),
        SETTINGS_LINE_STYLE_LAYOUT(TCategory.SETTINGS, "SettingsLineStyleLayout", "Line style option"),
        SETTINGS_LINE_STYLE_RETTANGLE(TCategory.SETTINGS, "SettingsLineStyleRettangle", "Rettangle"),
        SETTINGS_LINE_STYLE_ELLIPSE(TCategory.SETTINGS, "SettingsLineStyleEllipse", "Ellipse"),
        SETTINGS_LINE_STYLE_LINE(TCategory.SETTINGS, "SettingsLineStyleLine", "Line"),
        SETTINGS_LINE_STYLE_CURVED(TCategory.SETTINGS, "SettingsLineStyleCurved", "Curved"),
        SETTINGS_COLOR_OPTION_LAYOUT(TCategory.SETTINGS, "SettingsColorOptionLayout", "Color option"),
        SETTINGS_COLOR_OPTION_PAINTED(TCategory.SETTINGS, "SettingsColorOptionPainted", "Paint selected line color"),

        // SEARCH BAR
        SEARCH_TITLE(TCategory.SEARCH_BAR, "SearcTitle", "Search..."),
        SEARCH_NO_RECENT(TCategory.SEARCH_BAR, "SearchNoRecent", "No recent searches"),
        SEARCH_NO_RESULT(TCategory.SEARCH_BAR, "SearchNoResult", "No result for"),
        SEARCH_FAVORITE(TCategory.SEARCH_BAR, "SearchFavorite", "Favorite"),
        SEARCH_RECENT(TCategory.SEARCH_BAR, "SearchRecent", "Recents"),

        // TOAST MESSAGES
        TOAST_BACKUP_EDITED(TCategory.TOAST, "BackupEditedOk", "Backup updated successfully"),
        TOAST_BACKUP_CREATED(TCategory.TOAST, "BackupCreatedOk", "Backup created successfully"),
        TOAST_BACKUP_DELETED(TCategory.TOAST, "BackupDeletedOk", "Backup deleted successfully"),
        TOAST_BACKUP_REPLACED(TCategory.TOAST, "BackupReplacedOk", "Existing backup replaced successfully"),
        TOAST_BACKUP_DELETED_ERROR(TCategory.TOAST, "BackupDeletedError", "Failed to delete the backup"),
        TOAST_BACKUP_REPLACED_ERROR(TCategory.TOAST, "BackupReplacedError", "Failed to replace the backup"),
        TOAST_INVALID_TIME(TCategory.TOAST, "InvalidTime", "Invalid time interval"),
        TOAST_SUBSCRIPTION_EXPIRING(TCategory.TOAST, "SubscriptionExpiring", "Your Backup Manager subscription will expire soon. Please contact us to renew it"),
        TOAST_SUBSCRIPTION_EXPIRED(TCategory.TOAST, "SubscriptionExpired", "Your Backup Manager subscription has expired. Please contact us to renew it"),
        TOAST_LANGUAGE_CHANGE(TCategory.TOAST, "LanguageChange", "Some changes will take effect after restarting the application"),
        TOAST_MISSING_DATA_LOGIN_ERROR(TCategory.TOAST, "MissingDataLoginError", "Please fill in all the required fields"),
        TOAST_WRONG_EMAIL_LOGIN_ERROR(TCategory.TOAST, "WrongEmailLoginError", "The provided email address is invalid. Please provide a correct one"),
        TOAST_LOGIN(TCategory.TOAST, "LoginOk", "Welcome! You have successfully signed in"),
        ;


        private final TCategory category;
        private final String keyName;
        private final String defaultValue;

        private static final Map<String, TKey> lookup = new HashMap<>();

        static {
            for (TKey key : TKey.values()) {
                lookup.put(key.keyName, key);
            }
        }

        // Constructor to assign both key and default value
        private TKey(TCategory category, String keyName, String defaultValue) {
            this.category = category;
            this.keyName = keyName;
            this.defaultValue = defaultValue;
        }

        // Lookup by keyName (JSON key)
        public static TKey fromKeyName(String keyName) {
            return lookup.get(keyName);
        }

        public TCategory getCategory() { return category; }
        public String getKeyName() { return keyName; }
        public String getDefaultValue() { return defaultValue; }
    }

    public static String get(TKey key) {
        TCategory category = key.getCategory();
        return category.getTranslation(key);
    }

    public static void loadTranslations(String filePath) throws IOException {
        Gson gson = new Gson();

        // Clear previous translations to avoid stale values when switching languages
        for (TCategory c : TCategory.values()) {
            c.clearTranslations();
        }

        try (FileReader reader = new FileReader(filePath, StandardCharsets.UTF_8)) {
            JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);

            for (TCategory category : TCategory.values()) {
                JsonObject categoryTranslations = jsonObject.getAsJsonObject(category.getCategoryName());

                if (categoryTranslations == null) {
                    logger.warn("Missing category in {}: {}", LanguageManager.getLanguage().getFileName(), category.getCategoryName());
                    continue;
                }

                Set<TKey> loadedKeys = new HashSet<>();
                for (Map.Entry<String, JsonElement> entry : categoryTranslations.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue().getAsString();

                    // Use fromKeyName to get the TKey from the JSON key
                    TKey translationKey = TKey.fromKeyName(key);
                    if (translationKey != null) {
                        // If value is null or empty, fall back to the default value from the enum
                        String translationValue = (value != null && !value.isEmpty()) ? value : translationKey.getDefaultValue();
                        category.addTranslation(translationKey, translationValue);
                        loadedKeys.add(translationKey);
                    } else {
                        logger.warn("Unrecognized key in JSON: {}, using default value", key);
                    }
                }

                for (TKey key : TKey.values()) {
                    if (key.getCategory() == category && !loadedKeys.contains(key)) {
                        logger.warn("Missing translation in {} -> category: {}, key: {}", LanguageManager.getLanguage().getFileName(), key.getCategory(), key.getKeyName());
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("An error occurred: {}", ex.getMessage(), ex);
        }
    }
}
