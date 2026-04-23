package backupmanager.gui.simple;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.time.LocalDateTime;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import backupmanager.Entities.ConfigurationBackup;
import backupmanager.Entities.TimeInterval;
import backupmanager.Enums.Translations;
import backupmanager.Enums.Translations.TKey;
import backupmanager.Exceptions.BackupAlreadyRunningException;
import backupmanager.Helpers.BackupHelper;
import backupmanager.gui.Controllers.BackupEntryController;
import backupmanager.gui.Table.BackupTableDataService;
import net.miginfocom.swing.MigLayout;
import raven.modal.ModalDialog;
import raven.modal.component.ModalBorderAction;
import raven.modal.component.SimpleModalBorder;
import raven.modal.option.Location;
import raven.modal.option.Option;

public class BackupEntryDialog extends CustomDialog<ConfigurationBackup> {

    private static final Logger logger = LoggerFactory.getLogger(BackupHelper.class);
    private final BackupEntryController entryController;
    private final BackupTableDataService backupTable;

    private final boolean create;
    private String backupOnText;
    private String backupOffText;

    public BackupEntryDialog(BackupTableDataService backupTable) {
        entryController = new BackupEntryController(null);
        this.backupTable = backupTable;
        create = true;

        build();

        setAutoBackupOff();
    }

    public BackupEntryDialog(BackupTableDataService backupTable, ConfigurationBackup currentBackup) {
        entryController = new BackupEntryController(currentBackup);
        this.backupTable = backupTable;
        create = false;

        build();

        updateCurrentFiedsByBackup(currentBackup);
        txtBackupName.setText(currentBackup.getName());
        txtBackupName.setEditable(false);
        txtBackupName.setFocusable(false);
    }

    @Override
    protected void init() {
        setLayout(new MigLayout("fillx,wrap,insets 5 30 5 30,width 400", "[fill]", ""));
        txtBackupName = new JTextField();
        txtTargetPath = new JTextField();
        txtDestinationPath = new JTextField();
        executeBackupBtn = new JButton("Execute Backup");
        automaticBackupBtn = new JToggleButton("Automatic Backup (OFF)");
        targetPathBtn = new JButton(new FlatSVGIcon("icons/folder.svg", 25, 25));
        destinationPathBtn = new JButton(new FlatSVGIcon("icons/folder.svg", 25, 25));
        timeIntervalBtn = new JButton(new FlatSVGIcon("icons/timer.svg", 25, 25));
        maxToKeeSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        maxToKeeLabel = new JLabel("Max to keep");
        lastBackupLabel = new JLabel("Last backup: never");
        txtNotes = new JLabel("Notes");
        textAreaNotes = new JTextArea();
        textAreaNotes.setWrapStyleWord(true);
        textAreaNotes.setLineWrap(true);
        txtPath = new JLabel("Paths");
        txtBackupNameLabel = new JLabel("Backup Name");
        JScrollPane scroll = new JScrollPane(textAreaNotes);

        createTitle(Translations.get(TKey.PAGE_SUBTITLE_INFO));

        add(txtBackupNameLabel, "gapy 5 0");
        add(txtBackupName);
        add(txtPath, "gapy 5 0");
        add(txtTargetPath, "split 2");
        add(targetPathBtn, "w 30!, h 30!");
        add(txtDestinationPath, "split 2");
        add(destinationPathBtn, "w 30!, h 30!");

        add(txtNotes, "gapy 5 0");
        add(scroll, "height 120,grow,pushy");

        createTitle(Translations.get(TKey.PAGE_SUBTITLE_SETTINGS));

        add(lastBackupLabel);
        add(executeBackupBtn);
        add(automaticBackupBtn, "split 2");
        add(timeIntervalBtn, "w 30!, h 30!");

        add(maxToKeeLabel, "gapy 5 0");
        add(maxToKeeSpinner, "width 100");

        executeBackupBtn.addActionListener(e -> executeBackup());
        automaticBackupBtn.addActionListener(e -> toggleAutomaticBackup());
        targetPathBtn.addActionListener(e -> entryController.openFileChooser(txtTargetPath, true));
        destinationPathBtn.addActionListener(e -> entryController.openFileChooser(txtDestinationPath, false));
        timeIntervalBtn.addActionListener(e -> openTimeInterval());

        executeBackupBtn.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        automaticBackupBtn.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        targetPathBtn.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        destinationPathBtn.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        timeIntervalBtn.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));

        styleSpinner(maxToKeeSpinner);
        configureSpinner(maxToKeeSpinner, 1, 100);

        txtNotes.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.isControlDown() && e.getKeyChar() == 10) {
                    ModalBorderAction modalBorderAction = ModalBorderAction.getModalBorderAction(BackupEntryDialog.this);
                    if (modalBorderAction != null) {
                        modalBorderAction.doAction(SimpleModalBorder.YES_OPTION);
                    }
                }
            }
        });
    }

    @Override
    public ConfigurationBackup getResult() {
        return entryController.getCurrentBackup();
    }

    private void openTimeInterval() {
        // try {
        //     TimeInterval time = entryController.handleTimePickerAction(this, txtTargetPath.getText(), txtDestinationPath.getText());
        //     timeIntervalBtn.setToolTipText(time.toString());
        //     openBackupActivationMessage(time);
        // } catch (InvalidTimeInterval e) {
        //     // no actions
        // }

        TimePickerDialog timePicker = new TimePickerDialog(entryController.getCurrentBackup().getTimeIntervalBackup());

        Option option = ModalDialog.createOption();
        option.getLayoutOption().setSize(-1, 1f)
                .setLocation(Location.TRAILING, Location.TOP)
                .setAnimateDistance(0.7f, 0);
        ModalDialog.showModal(this, new SimpleModalBorder(
                timePicker,
                Translations.get(TKey.TIME_INTERVAL_TITLE),
                SimpleModalBorder.YES_NO_OPTION,
                (controller, action) -> {
                    if (action == SimpleModalBorder.YES_OPTION) {

                            TimeInterval time = timePicker.getResult();

                            timeIntervalBtn.setToolTipText(time.toString());
                            openBackupActivationMessage(time);

                            controller.close();
                        }

                        if (action == SimpleModalBorder.NO_OPTION) {
                            controller.close();
                        }
                }), option);
    }

    private void updateCurrentFiedsByBackup(ConfigurationBackup backup) {
        setStartPathField(backup.getTargetPath());
        setDestinationPathField(backup.getDestinationPath());
        setLastBackupLabel(backup.getLastUpdateDate());
        setAutoBackupPreference(backup.isAutomatic());
        setCurrentBackupNotes(backup.getNotes());
        setCurrentBackupMaxBackupsToKeep(backup.getMaxToKeep());

        if (backup.getTimeIntervalBackup() != null) {
            setAutoBackupOn(backup);
        } else {
            setAutoBackupOff();
        }
    }

    private void executeBackup() {
        try {
            entryController.handleSingleBackupRequest(
                backupTable,
                txtBackupName.getText(),
                txtTargetPath.getText(),
                txtDestinationPath.getText(),
                textAreaNotes.getText(),
                automaticBackupBtn.isSelected(),
                (int) maxToKeeSpinner.getValue()
            );
        } catch (BackupAlreadyRunningException e) {
            // no handle
        }
    }

    private void toggleAutomaticBackup() {
        if (entryController.toggleAutomaticBackup(txtBackupName.getText(), txtTargetPath.getText(), txtDestinationPath.getText(), txtNotes.getText(), automaticBackupBtn.isSelected(), (int) maxToKeeSpinner.getValue())) {
            setAutoBackupOn(entryController.getCurrentBackup());
            automaticBackupBtn.setSelected(true);
            timeIntervalBtn.setToolTipText(entryController.getCurrentBackup().getTimeIntervalBackup().toString());
            timeIntervalBtn.setEnabled(true);
        } else {
            setAutoBackupOff();
            automaticBackupBtn.setSelected(false);
        }
    }

    private void setAutoBackupOn(ConfigurationBackup backup) {
        automaticBackupBtn.setSelected(true);
        automaticBackupBtn.setText(backupOnText);

        if (backup != null)
            enableTimePickerButton(backup);
        else
            disableTimePickerButton();
    }

    private void setAutoBackupOff() {
        automaticBackupBtn.setSelected(false);
        automaticBackupBtn.setText(backupOffText);
        disableTimePickerButton();
    }

    private void disableTimePickerButton() {
        timeIntervalBtn.setToolTipText(Translations.get(TKey.TIME_PICKER_TOOLTIP));
        timeIntervalBtn.setEnabled(false);
    }

    private void enableTimePickerButton(ConfigurationBackup backup) {
        if (backup.getTimeIntervalBackup() != null) {
            timeIntervalBtn.setToolTipText(backup.getTimeIntervalBackup().toString());
            timeIntervalBtn.setEnabled(true);
        } else {
            timeIntervalBtn.setEnabled(true);
        }
    }

    private boolean canDispose() {
        return entryController.canDisposeAfterOk(txtBackupName.getText(), txtTargetPath.getText(), txtDestinationPath.getText(), textAreaNotes.getText(), automaticBackupBtn.isSelected(), (int) maxToKeeSpinner.getValue(), create);
    }

    private void disableAutoBackup(ConfigurationBackup backup) {
        logger.info("Event --> auto backup disabled");

        backup.setTimeIntervalBackup(null);
        backup.setNextBackupDate(null);
        backup.setAutomatic(false);
        backup.setLastUpdateDate(LocalDateTime.now());
    }

    private void openBackupActivationMessage(TimeInterval newtimeInterval) {
        entryController.handleOpenBackupActivationMessage(newtimeInterval, txtTargetPath.getText(), txtDestinationPath.getText());
    }

    private void setAutoBackupPreference(boolean option) {
        ConfigurationBackup currentBackup = entryController.getCurrentBackup();
        currentBackup.setAutomatic(option);

        if (option) {
            setAutoBackupOn(currentBackup);
        } else {
            disableAutoBackup(currentBackup);
        }
    }

    private void setLastBackupLabel(LocalDateTime date) {
        if (date != null) {
            String dateStr = date.format(BackupHelper.formatter);
            dateStr = Translations.get(TKey.LAST_BACKUP) + ": " + dateStr;
            lastBackupLabel.setText(dateStr);
        }
        else lastBackupLabel.setText("");
    }

    private void setStartPathField(String text) {
        txtTargetPath.setText(text);
    }
    private void setDestinationPathField(String text) {
        txtDestinationPath.setText(text);
    }
    private void setCurrentBackupNotes(String notes) {
        textAreaNotes.setText(notes);
    }
    private void setCurrentBackupMaxBackupsToKeep(int maxBackupsCount) {
        maxToKeeSpinner.setValue(maxBackupsCount);
    }

    @Override
    public void setTranslations() {
        backupOnText = Translations.get(TKey.AUTO_BACKUP_BUTTON_ON);
        backupOffText = Translations.get(TKey.AUTO_BACKUP_BUTTON_OFF);
        targetPathBtn.setToolTipText(Translations.get(TKey.INITIAL_FILE_CHOOSER_TOOLTIP));
        destinationPathBtn.setToolTipText(Translations.get(TKey.DESTINATION_FILE_CHOOSER_TOOLTIP));
        txtTargetPath.setToolTipText(Translations.get(TKey.INITIAL_PATH_TOOLTIP));
        txtDestinationPath.setToolTipText(Translations.get(TKey.DESTINATION_PATH_TOOLTIP));
        textAreaNotes.setToolTipText(Translations.get(TKey.NOTES_TOOLTIP));
        executeBackupBtn.setText(Translations.get(TKey.SINGLE_BACKUP_BUTTON));
        executeBackupBtn.setToolTipText(Translations.get(TKey.SINGLE_BACKUP_TOOLTIP));
        automaticBackupBtn.setText(Translations.get(TKey.AUTO_BACKUP_BUTTON_OFF));
        automaticBackupBtn.setToolTipText(Translations.get(TKey.AUTO_BACKUP_TOOLTIP));
        txtNotes.setText(Translations.get(TKey.NOTES) + ":");
        lastBackupLabel.setText(Translations.get(TKey.LAST_BACKUP) + ": ");
        txtTargetPath.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, Translations.get(TKey.INITIAL_PATH_PLACEHOLDER));
        txtDestinationPath.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, Translations.get(TKey.DESTINATION_PATH_PLACEHOLDER));
        timeIntervalBtn.setToolTipText(Translations.get(TKey.TIME_PICKER_TOOLTIP));
        maxToKeeSpinner.setToolTipText(Translations.get(TKey.MAX_BACKUPS_TO_KEEP_TOOLTIP) + "\n" + Translations.get(TKey.SPINNER_TOOLTIP));
        maxToKeeLabel.setText(Translations.get(TKey.MAX_BACKUPS_TO_KEEP));
        txtBackupName.setToolTipText(Translations.get(TKey.BACKUP_NAME_TOOLTIP));
        txtBackupName.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, Translations.get(TKey.BACKUP_NAME_PLACEHOLDER));
        txtPath.setText(Translations.get(TKey.PATHS));
        txtBackupNameLabel.setText(Translations.get(TKey.BACKUP_NAME));
    }

    private JTextField txtBackupName;
    private JTextField txtTargetPath;
    private JTextField txtDestinationPath;
    private JLabel txtBackupNameLabel;
    private JLabel txtPath;
    private JLabel txtNotes;
    private JTextArea textAreaNotes;
    private JLabel lastBackupLabel;
    private JButton executeBackupBtn;
    private JToggleButton automaticBackupBtn;
    private JButton targetPathBtn;
    private JButton destinationPathBtn;
    private JButton timeIntervalBtn;
    private JSpinner maxToKeeSpinner;
    private JLabel maxToKeeLabel;
}
