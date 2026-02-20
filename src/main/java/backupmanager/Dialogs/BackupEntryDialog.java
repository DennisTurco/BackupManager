package backupmanager.Dialogs;

import static backupmanager.GUI.BackupManagerGUI.backupTable;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.formdev.flatlaf.FlatClientProperties;

import backupmanager.BackupOperations;
import backupmanager.Controllers.BackupEntryController;
import backupmanager.Entities.ConfigurationBackup;
import backupmanager.Entities.TimeInterval;
import backupmanager.Enums.ConfigKey;
import backupmanager.Enums.TranslationLoaderEnum.TranslationCategory;
import backupmanager.Enums.TranslationLoaderEnum.TranslationKey;
import backupmanager.Exceptions.BackupAlreadyRunningException;
import backupmanager.Exceptions.InvalidTimeInterval;
import backupmanager.Helpers.BackupHelper;
import backupmanager.Json.JSONConfigReader;

public class BackupEntryDialog extends javax.swing.JDialog {

    private static final Logger logger = LoggerFactory.getLogger(BackupEntryDialog.class);
    private static final JSONConfigReader configReader = new JSONConfigReader(ConfigKey.CONFIG_FILE_STRING.getValue(), ConfigKey.CONFIG_DIRECTORY_STRING.getValue());

    private final boolean create;
    private String backupOnText;
    private String backupOffText;

    private final BackupEntryController entryController;

    public BackupEntryDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);

        entryController = new BackupEntryController(null);

        initializeDialog();
        setAutoBackupOff();
        this.create = true;
        okButton.setText(TranslationCategory.GENERAL.getTranslation(TranslationKey.CREATE_BUTTON));
    }

    public BackupEntryDialog(java.awt.Frame parent, boolean modal, ConfigurationBackup currentBackup) {
        super(parent, modal);

        entryController = new BackupEntryController(currentBackup);

        initializeDialog();
        updateCurrentFiedsByBackup(currentBackup);
        backupNameField.setText(currentBackup.getName());
        backupNameField.setEditable(false);
        backupNameField.setFocusable(false);
        this.create = false;
        okButton.setText(TranslationCategory.GENERAL.getTranslation(TranslationKey.SAVE_BUTTON));
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

    private void setStartPathField(String text) {
        startPathField.setText(text);
    }
    private void setDestinationPathField(String text) {
        destinationPathField.setText(text);
    }
    private void setCurrentBackupNotes(String notes) {
        backupNoteTextArea.setText(notes);
    }
    private void setCurrentBackupMaxBackupsToKeep(int maxBackupsCount) {
        maxBackupCountSpinner.setValue(maxBackupsCount);
    }

    private void initializeDialog() {
        initComponents();

        setCurrentBackupMaxBackupsToKeep(configReader.getConfigValue("MaxCountForSameBackup", 1));

        setSvgImages();
        setTranslations();
    }

    private void setLastBackupLabel(LocalDateTime date) {
        if (date != null) {
            String dateStr = date.format(BackupHelper.formatter);
            dateStr = TranslationCategory.BACKUP_ENTRY.getTranslation(TranslationKey.LAST_BACKUP) + ": " + dateStr;
            lastBackupLabel.setText(dateStr);
        }
        else lastBackupLabel.setText("");
    }

    private void openBackupActivationMessage(TimeInterval newtimeInterval) {
        entryController.handleOpenBackupActivationMessage(newtimeInterval, startPathField.getText(), destinationPathField.getText());
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

    private void toggleAutomaticBackup() {
        if (entryController.toggleAutomaticBackup(backupNameField.getText(), startPathField.getText(), destinationPathField.getText(), backupNoteTextArea.getText(), toggleAutoBackup.isSelected(), (int) maxBackupCountSpinner.getValue())) {
            setAutoBackupOn(entryController.getCurrentBackup());
            toggleAutoBackup.setSelected(true);
            btnTimePicker.setToolTipText(entryController.getCurrentBackup().getTimeIntervalBackup().toString());
            btnTimePicker.setEnabled(true);
        } else {
            setAutoBackupOff();
            toggleAutoBackup.setSelected(false);
        }
    }

    private void setAutoBackupOn(ConfigurationBackup backup) {
        toggleAutoBackup.setSelected(true);
        toggleAutoBackup.setText(backupOnText);

        if (backup != null)
            enableTimePickerButton(backup);
        else
            disableTimePickerButton();
    }

    private void setAutoBackupOff() {
        toggleAutoBackup.setSelected(false);
        toggleAutoBackup.setText(backupOffText);
        disableTimePickerButton();
    }

    private void disableTimePickerButton() {
        btnTimePicker.setToolTipText(TranslationCategory.BACKUP_ENTRY.getTranslation(TranslationKey.TIME_PICKER_TOOLTIP));
        btnTimePicker.setEnabled(false);
    }

    private void enableTimePickerButton(ConfigurationBackup backup) {
        if (backup.getTimeIntervalBackup() != null) {
            btnTimePicker.setToolTipText(backup.getTimeIntervalBackup().toString());
            btnTimePicker.setEnabled(true);
        } else {
            btnTimePicker.setEnabled(true);
        }
    }

    private void disableAutoBackup(ConfigurationBackup backup) {
        logger.info("Event --> auto backup disabled");

        backup.setTimeIntervalBackup(null);
        backup.setNextBackupDate(null);
        backup.setAutomatic(false);
        backup.setLastUpdateDate(LocalDateTime.now());
    }

    private void maxBackupCountSpinnerChange() {
        Integer backupCount = (Integer) maxBackupCountSpinner.getValue();

        if (backupCount == null || backupCount < 1) {
            maxBackupCountSpinner.setValue(1);
        }  else if (backupCount > 10) {
            maxBackupCountSpinner.setValue(10);
        }
    }

    private void mouseWeel(java.awt.event.MouseWheelEvent evt) {
        javax.swing.JSpinner spinner = (javax.swing.JSpinner) evt.getSource();
        int rotation = evt.getWheelRotation();

        if (rotation < 0) {
            spinner.setValue((Integer) spinner.getValue() + 1);
        } else {
            spinner.setValue((Integer) spinner.getValue() - 1);
        }
    }

    private void setSvgImages() {
        btnPathSearch1.setSvgImage("res/img/folder.svg", 30, 30);
        btnPathSearch2.setSvgImage("res/img/folder.svg", 30, 30);
        btnTimePicker.setSvgImage("res/img/timer.svg", 30, 30);
    }

    private void setTranslations() {
        backupOnText = TranslationCategory.BACKUP_ENTRY.getTranslation(TranslationKey.AUTO_BACKUP_BUTTON_ON);
        backupOffText = TranslationCategory.BACKUP_ENTRY.getTranslation(TranslationKey.AUTO_BACKUP_BUTTON_OFF);
        btnPathSearch1.setToolTipText(TranslationCategory.BACKUP_ENTRY.getTranslation(TranslationKey.INITIAL_FILE_CHOOSER_TOOLTIP));
        btnPathSearch2.setToolTipText(TranslationCategory.BACKUP_ENTRY.getTranslation(TranslationKey.DESTINATION_FILE_CHOOSER_TOOLTIP));
        startPathField.setToolTipText(TranslationCategory.BACKUP_ENTRY.getTranslation(TranslationKey.INITIAL_PATH_TOOLTIP));
        destinationPathField.setToolTipText(TranslationCategory.BACKUP_ENTRY.getTranslation(TranslationKey.DESTINATION_PATH_TOOLTIP));
        backupNoteTextArea.setToolTipText(TranslationCategory.BACKUP_ENTRY.getTranslation(TranslationKey.NOTES_TOOLTIP));
        singleBackup.setText(TranslationCategory.BACKUP_ENTRY.getTranslation(TranslationKey.SINGLE_BACKUP_BUTTON));
        singleBackup.setToolTipText(TranslationCategory.BACKUP_ENTRY.getTranslation(TranslationKey.SINGLE_BACKUP_TOOLTIP));
        toggleAutoBackup.setText(TranslationCategory.BACKUP_ENTRY.getTranslation(TranslationKey.AUTO_BACKUP_BUTTON_OFF));
        toggleAutoBackup.setToolTipText(TranslationCategory.BACKUP_ENTRY.getTranslation(TranslationKey.AUTO_BACKUP_TOOLTIP));
        jLabel2.setText(TranslationCategory.BACKUP_ENTRY.getTranslation(TranslationKey.NOTES) + ":");
        lastBackupLabel.setText(TranslationCategory.BACKUP_ENTRY.getTranslation(TranslationKey.LAST_BACKUP) + ": ");
        setTitle(TranslationCategory.BACKUP_ENTRY.getTranslation(TranslationKey.PAGE_TITLE));
        startPathField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, TranslationCategory.BACKUP_ENTRY.getTranslation(TranslationKey.INITIAL_PATH_PLACEHOLDER));
        destinationPathField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, TranslationCategory.BACKUP_ENTRY.getTranslation(TranslationKey.DESTINATION_PATH_PLACEHOLDER));
        btnTimePicker.setToolTipText(TranslationCategory.BACKUP_ENTRY.getTranslation(TranslationKey.TIME_PICKER_TOOLTIP));
        maxBackupCountSpinner.setToolTipText(TranslationCategory.BACKUP_ENTRY.getTranslation(TranslationKey.MAX_BACKUPS_TO_KEEP_TOOLTIP) + "\n" + TranslationCategory.TIME_PICKER_DIALOG.getTranslation(TranslationKey.SPINNER_TOOLTIP));
        jLabel4.setText(TranslationCategory.BACKUP_ENTRY.getTranslation(TranslationKey.MAX_BACKUPS_TO_KEEP));
        closeButton.setText(TranslationCategory.GENERAL.getTranslation(TranslationKey.CLOSE_BUTTON));
        backupNameField.setToolTipText(TranslationCategory.BACKUP_ENTRY.getTranslation(TranslationKey.BACKUP_NAME_TOOLTIP));
        backupNameField.setHintText(TranslationCategory.BACKUP_ENTRY.getTranslation(TranslationKey.BACKUP_NAME));
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        startPathField = new javax.swing.JTextField();
        btnPathSearch1 = new backupmanager.svg.SVGButton();
        btnPathSearch2 = new backupmanager.svg.SVGButton();
        destinationPathField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        backupNoteTextArea = new javax.swing.JTextArea();
        lastBackupLabel = new javax.swing.JLabel();
        singleBackup = new javax.swing.JButton();
        toggleAutoBackup = new javax.swing.JToggleButton();
        btnTimePicker = new backupmanager.svg.SVGButton();
        maxBackupCountSpinner = new javax.swing.JSpinner();
        jLabel4 = new javax.swing.JLabel();
        closeButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        backupNameField = new backupmanager.customwidgets.ModernTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);

        startPathField.setToolTipText("(Required) Initial path");
        startPathField.setActionCommand("null");
        startPathField.setAlignmentX(0.0F);
        startPathField.setAlignmentY(0.0F);
        startPathField.setAutoscrolls(false);
        startPathField.setMaximumSize(new java.awt.Dimension(465, 26));
        startPathField.setMinimumSize(new java.awt.Dimension(465, 26));
        startPathField.setPreferredSize(new java.awt.Dimension(465, 26));

        btnPathSearch1.setToolTipText("");
        btnPathSearch1.setMaximumSize(new java.awt.Dimension(35, 35));
        btnPathSearch1.setMinimumSize(new java.awt.Dimension(35, 35));
        btnPathSearch1.setPreferredSize(new java.awt.Dimension(35, 35));
        btnPathSearch1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPathSearch1ActionPerformed(evt);
            }
        });

        btnPathSearch2.setToolTipText("Open file explorer");
        btnPathSearch2.setMaximumSize(new java.awt.Dimension(35, 35));
        btnPathSearch2.setMinimumSize(new java.awt.Dimension(35, 35));
        btnPathSearch2.setPreferredSize(new java.awt.Dimension(35, 35));
        btnPathSearch2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPathSearch2ActionPerformed(evt);
            }
        });

        destinationPathField.setToolTipText("(Required) Destination path");
        destinationPathField.setActionCommand("<Not Set>");
        destinationPathField.setAlignmentX(0.0F);
        destinationPathField.setAlignmentY(0.0F);
        destinationPathField.setMaximumSize(new java.awt.Dimension(465, 26));
        destinationPathField.setMinimumSize(new java.awt.Dimension(465, 26));
        destinationPathField.setPreferredSize(new java.awt.Dimension(465, 26));

        jLabel2.setText("notes:");

        backupNoteTextArea.setColumns(20);
        backupNoteTextArea.setRows(5);
        backupNoteTextArea.setToolTipText("(Optional) Backup description");
        backupNoteTextArea.setMaximumSize(new java.awt.Dimension(232, 84));
        backupNoteTextArea.setMinimumSize(new java.awt.Dimension(232, 84));
        jScrollPane2.setViewportView(backupNoteTextArea);

        lastBackupLabel.setText("last backup: ");

        singleBackup.setBackground(new java.awt.Color(51, 153, 255));
        singleBackup.setForeground(new java.awt.Color(255, 255, 255));
        singleBackup.setText("Single Backup");
        singleBackup.setToolTipText("Perform the backup");
        singleBackup.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        singleBackup.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SingleBackupActionPerformed(evt);
            }
        });

        toggleAutoBackup.setText("Auto Backup");
        toggleAutoBackup.setToolTipText("Enable/Disable automatic backup");
        toggleAutoBackup.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        toggleAutoBackup.setPreferredSize(new java.awt.Dimension(108, 27));
        toggleAutoBackup.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toggleAutoBackupActionPerformed(evt);
            }
        });

        btnTimePicker.setToolTipText("Time picker");
        btnTimePicker.setMaximumSize(new java.awt.Dimension(36, 36));
        btnTimePicker.setMinimumSize(new java.awt.Dimension(36, 36));
        btnTimePicker.setPreferredSize(new java.awt.Dimension(36, 36));
        btnTimePicker.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTimePickerActionPerformed(evt);
            }
        });

        maxBackupCountSpinner.setToolTipText("Maximum number of backups before removing the oldest.");
        maxBackupCountSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                maxBackupCountSpinnerStateChanged(evt);
            }
        });
        maxBackupCountSpinner.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                maxBackupCountSpinnerMouseWheelMoved(evt);
            }
        });

        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel4.setText("Keep only last");

        closeButton.setText("Close");
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });

        okButton.setText("Ok");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        backupNameField.setToolTipText("(Required) Backup name");
        backupNameField.setActionCommand("null");
        backupNameField.setAlignmentX(0.0F);
        backupNameField.setAlignmentY(0.0F);
        backupNameField.setAutoscrolls(false);
        backupNameField.setMaximumSize(new java.awt.Dimension(465, 26));
        backupNameField.setMinimumSize(new java.awt.Dimension(465, 26));
        backupNameField.setPreferredSize(new java.awt.Dimension(465, 26));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(35, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(okButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(closeButton)
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 469, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(destinationPathField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnPathSearch2, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 462, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lastBackupLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 461, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(131, 131, 131)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(singleBackup, javax.swing.GroupLayout.PREFERRED_SIZE, 188, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(toggleAutoBackup, javax.swing.GroupLayout.PREFERRED_SIZE, 188, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(btnTimePicker, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 244, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(maxBackupCountSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(backupNameField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(startPathField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnPathSearch1, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(15, 15, 15))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(backupNameField, javax.swing.GroupLayout.DEFAULT_SIZE, 53, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(startPathField, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnPathSearch1, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(destinationPathField, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnPathSearch2, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lastBackupLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(singleBackup, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(toggleAutoBackup, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(btnTimePicker, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(maxBackupCountSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(closeButton)
                    .addComponent(okButton))
                .addContainerGap())
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void btnPathSearch1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPathSearch1ActionPerformed
        logger.debug("File chooser: " + startPathField.getName() + ", files allowed: " + true);
        String text = BackupOperations.pathSearchWithFileChooser(true);
        if (text != null) {
            startPathField.setText(text);
        }
    }//GEN-LAST:event_btnPathSearch1ActionPerformed

    private void btnPathSearch2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPathSearch2ActionPerformed
        logger.debug("File chooser: " + destinationPathField.getName() + ", files allowed: " + false);
        String text = BackupOperations.pathSearchWithFileChooser(false);
        if (text != null) {
            destinationPathField.setText(text);
        }
    }//GEN-LAST:event_btnPathSearch2ActionPerformed

    private void SingleBackupActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SingleBackupActionPerformed
        try {
            entryController.handleSingleBackupRequest(
                backupTable,
                backupNameField.getText(),
                startPathField.getText(),
                destinationPathField.getText(),
                backupNoteTextArea.getText(),
                toggleAutoBackup.isSelected(),
                (int) maxBackupCountSpinner.getValue()
            );
        } catch (BackupAlreadyRunningException e) {
            // no handle
        }
    }//GEN-LAST:event_SingleBackupActionPerformed

    private void toggleAutoBackupActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toggleAutoBackupActionPerformed
        logger.info("Event --> Changing auto backup preference");
        toggleAutomaticBackup();
    }//GEN-LAST:event_toggleAutoBackupActionPerformed

    private void btnTimePickerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTimePickerActionPerformed
        try {
            TimeInterval time = entryController.handleTimePickerAction(this, startPathField.getText(), destinationPathField.getText());
            btnTimePicker.setToolTipText(time.toString());
            openBackupActivationMessage(time);
        } catch (InvalidTimeInterval e) {
            // no actions
        }
    }//GEN-LAST:event_btnTimePickerActionPerformed

    private void maxBackupCountSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_maxBackupCountSpinnerStateChanged
        maxBackupCountSpinnerChange();
    }//GEN-LAST:event_maxBackupCountSpinnerStateChanged

    private void maxBackupCountSpinnerMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_maxBackupCountSpinnerMouseWheelMoved
        mouseWeel(evt);
    }//GEN-LAST:event_maxBackupCountSpinnerMouseWheelMoved

    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        this.dispose();
    }//GEN-LAST:event_closeButtonActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        if (entryController.canDisposeAfterOk(backupNameField.getText(), startPathField.getText(), destinationPathField.getText(), backupNoteTextArea.getText(), toggleAutoBackup.isSelected(), (int) maxBackupCountSpinner.getValue(), create))
            this.dispose();
    }//GEN-LAST:event_okButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton singleBackup;
    private backupmanager.customwidgets.ModernTextField backupNameField;
    private javax.swing.JTextArea backupNoteTextArea;
    private backupmanager.svg.SVGButton btnPathSearch1;
    private backupmanager.svg.SVGButton btnPathSearch2;
    private backupmanager.svg.SVGButton btnTimePicker;
    private javax.swing.JButton closeButton;
    private javax.swing.JTextField destinationPathField;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lastBackupLabel;
    private javax.swing.JSpinner maxBackupCountSpinner;
    private javax.swing.JButton okButton;
    private javax.swing.JTextField startPathField;
    private javax.swing.JToggleButton toggleAutoBackup;
    // End of variables declaration//GEN-END:variables
}
