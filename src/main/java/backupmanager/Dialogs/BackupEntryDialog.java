package backupmanager.Dialogs;

import static backupmanager.GUI.BackupManagerGUI.backupTable;

import java.time.LocalDateTime;
import java.util.List;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.formdev.flatlaf.FlatClientProperties;

import backupmanager.BackupOperations;
import backupmanager.Entities.Backup;
import backupmanager.Entities.RunningBackups;
import backupmanager.Entities.TimeInterval;
import backupmanager.Entities.ZippingContext;
import backupmanager.Enums.ConfigKey;
import backupmanager.Enums.TranslationLoaderEnum.TranslationCategory;
import backupmanager.Enums.TranslationLoaderEnum.TranslationKey;
import backupmanager.GUI.BackupManagerGUI;
import backupmanager.GUI.BackupProgressGUI;
import backupmanager.Json.JSONConfigReader;
import backupmanager.Managers.BackupManager;
import backupmanager.Services.ZippingThread;
import backupmanager.Table.BackupTable;

public class BackupEntryDialog extends javax.swing.JDialog {

    private static final Logger logger = LoggerFactory.getLogger(BackupEntryDialog.class);

    private static final JSONConfigReader configReader = new JSONConfigReader(ConfigKey.CONFIG_FILE_STRING.getValue(), ConfigKey.CONFIG_DIRECTORY_STRING.getValue());
    private String backupOnText;
    private String backupOffText;
    private Backup currentBackup;
    private boolean closeOk;
    private boolean create;
    private TimeInterval timeInterval;

    public BackupEntryDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);

        initializeDialog();
        setAutoBackupOff();
        this.create = true;
        okButton.setText(TranslationCategory.GENERAL.getTranslation(TranslationKey.CREATE_BUTTON));
    }

    public BackupEntryDialog(java.awt.Frame parent, boolean modal, Backup currentBackup) {
        super(parent, modal);
        
        this.currentBackup = currentBackup;

        initializeDialog();
        updateCurrentFiedsByBackup(currentBackup);
        backupName.setText(currentBackup.getBackupName());
        backupName.setEditable(false);
        backupName.setFocusable(false);
        this.create = false;
        this.timeInterval = currentBackup.getTimeIntervalBackup();
        okButton.setText(TranslationCategory.GENERAL.getTranslation(TranslationKey.SAVE_BUTTON));
    }

    private void initializeDialog() {
        initComponents();

        setCurrentBackupMaxBackupsToKeep(configReader.getMaxCountForSameBackup());
        this.closeOk = false;

        setSvgImages();
        setTranslations();
    }

    private void SetLastBackupLabel(LocalDateTime date) {
        if (date != null) {
            String dateStr = date.format(BackupManager.formatter);
            dateStr = TranslationCategory.BACKUP_ENTRY.getTranslation(TranslationKey.LAST_BACKUP) + ": " + dateStr;
            lastBackupLabel.setText(dateStr);
        }
        else lastBackupLabel.setText("");
    }

    private void updateCurrentFiedsByBackup(Backup backup) {
        SetStartPathField(backup.getInitialPath());
        SetDestinationPathField(backup.getDestinationPath());
        SetLastBackupLabel(backup.getLastUpdateDate());
        setAutoBackupPreference(backup.isAutoBackup());
        setCurrentBackupNotes(backup.getNotes());
        setCurrentBackupMaxBackupsToKeep(backup.getMaxBackupsToKeep());
        
        if (backup.getTimeIntervalBackup() != null) {
            setAutoBackupOn(backup);
        } else {
            setAutoBackupOff();
        }  
    }

    private void showMessageActivationAutoBackup(String activated, String from, String to, String setted, TimeInterval timeInterval, String days, String startPath, String destinationPath) {
        JOptionPane.showMessageDialog(null,
                activated + "\n\t" + from + ": " + startPath + "\n\t" + to + ": "
                + destinationPath + setted + " " + timeInterval.toString() + days,
                "AutoBackup", 1);
    }

    private void openBackupActivationMessage(TimeInterval timeInterval) {
        if (timeInterval == null) 
            throw new IllegalArgumentException("Time interval cannot be null");

        String from = TranslationCategory.GENERAL.getTranslation(TranslationKey.FROM);
        String to = TranslationCategory.GENERAL.getTranslation(TranslationKey.TO);
        String activated = TranslationCategory.DIALOGS.getTranslation(TranslationKey.AUTO_BACKUP_ACTIVATED_MESSAGE);
        String setted = TranslationCategory.DIALOGS.getTranslation(TranslationKey.SETTED_EVERY_MESSAGE);
        String days = TranslationCategory.DIALOGS.getTranslation(TranslationKey.DAYS_MESSAGE);

        String startPath = GetStartPathField();
        String destinationPath = GetDestinationPathField();
        showMessageActivationAutoBackup(activated, from, to, setted, timeInterval, days, startPath, destinationPath);
    }

    public void setAutoBackupPreference(boolean option) {         
        currentBackup.setAutoBackup(option);
        
        if (option) {
            setAutoBackupOn(currentBackup);
        } else {
            disableAutoBackup(currentBackup);
        }
    }
    
    public void setAutoBackupPreference(Backup backup, boolean option) {        
        backup.setAutoBackup(option);
        if (backup.getBackupName().equals(currentBackup.getBackupName())) {
            toggleAutoBackup.setSelected(option);
            if (option) {
                enableTimePickerButton(backup);
            } else {
                disableTimePickerButton();
            }
        }
        if (!option) {
            disableAutoBackup(backup);
        }

        toggleAutoBackup.setText(toggleAutoBackup.isSelected() ? backupOnText : backupOffText);
    }

    public Backup getBackup() {
        if (!closeOk){
            return null;
        }
         
        String initialPath = startPathField.getText();
        String destinationPath = destinationPathField.getText();
        String notes = backupNoteTextArea.getText();
        boolean autoBackup = toggleAutoBackup.isSelected();
        int maxBackupsToKeep = (int) maxBackupCountSpinner.getValue();

        LocalDateTime nextDateBackup = null;
        if (timeInterval != null){
            nextDateBackup = BackupManager.getNexDateBackup(timeInterval);
        }

        if (currentBackup == null) {
            String name = backupName.getText();
            LocalDateTime lastBackup = null;
            LocalDateTime creationDate = LocalDateTime.now();
            LocalDateTime lastUpdateDate = creationDate;
            int backupCount = 0;
            return new Backup(name, initialPath, destinationPath, lastBackup, autoBackup, nextDateBackup, timeInterval, notes, creationDate, lastUpdateDate, backupCount, maxBackupsToKeep);
        } else {
            String name = currentBackup.getBackupName();
            LocalDateTime lastBackup = currentBackup.getLastBackup();
            LocalDateTime creationDate = currentBackup.getCreationDate();
            LocalDateTime lastUpdateDate = LocalDateTime.now();
            int backupCount = currentBackup.getBackupCount();
            return new Backup(name, initialPath, destinationPath, lastBackup, autoBackup, nextDateBackup, timeInterval, notes, creationDate, lastUpdateDate, backupCount, maxBackupsToKeep);
        }
    }

    private TimeInterval openTimePicker(TimeInterval time) {
        TimePicker picker = new TimePicker(this, time, true);
        picker.setVisible(true);
        return picker.getTimeInterval();
    }

    public String GetStartPathField() {
        return startPathField.getText();
    }
    public String GetDestinationPathField() {
        return destinationPathField.getText();
    }
    public String GetNotesTextArea() {
        return backupNoteTextArea.getText();
    }
    public boolean GetAutomaticBackupPreference() {
        return toggleAutoBackup.isSelected();
    }
    public int GetMaxBackupsToKeep() {
        return (int) maxBackupCountSpinner.getValue();
    }
    public void SetStartPathField(String text) {
        startPathField.setText(text);
    }
    public void SetDestinationPathField(String text) {
        destinationPathField.setText(text);
    }

    private void setCurrentBackupNotes(String notes) {
        backupNoteTextArea.setText(notes);
    }

    public void setCurrentBackupMaxBackupsToKeep(int maxBackupsCount) {
        maxBackupCountSpinner.setValue(maxBackupsCount);
    }
    
    public void SingleBackup(String path1, String path2, BackupTable backupTable) {
        logger.info("Event --> single backup");
        
        currentBackup.setInitialPath(path2);
		
        String temp = "\\";

        //------------------------------INPUT CONTROL ERRORS------------------------------
        if (!BackupOperations.CheckInputCorrect(currentBackup.getBackupName(), path1, path2, null)) return;

        //------------------------------TO GET THE CURRENT DATE------------------------------
        LocalDateTime dateNow = LocalDateTime.now();

        //------------------------------SET ALL THE VARIABLES------------------------------
        String name1; // folder name/initial file
        String date = dateNow.format(BackupManager.dateForfolderNameFormatter);

        //------------------------------SET ALL THE STRINGS------------------------------
        name1 = path1.substring(path1.length()-1, path1.length()-1);

        for(int i=path1.length()-1; i>=0; i--) {
            if(path1.charAt(i) != temp.charAt(0)) name1 = path1.charAt(i) + name1;
            else break;
        }

        name1 = BackupOperations.removeExtension(name1);
        path2 = path2 + "\\" + name1 + " (Backup " + date + ")";

        //------------------------------COPY THE FILE OR DIRECTORY------------------------------
        logger.info("date backup: " + date);
    	
        BackupManagerGUI.progressBar = new BackupProgressGUI(path1, path2);
        BackupManagerGUI.progressBar.setVisible(true);

        ZippingContext context = new ZippingContext(currentBackup, null, backupTable, BackupManagerGUI.progressBar, null, null);
        ZippingThread.zipDirectory(path1, path2 + ".zip", context);

        //if current_file_opened is null it means they are not in a backup but it is a backup with no associated json file
        if (currentBackup.getBackupName() != null && !currentBackup.getBackupName().isEmpty()) { 
            currentBackup.setInitialPath(GetStartPathField());
            currentBackup.setDestinationPath(GetDestinationPathField());
            currentBackup.setLastBackup(LocalDateTime.now());
        }
    }

    private boolean automaticBackup() {
        logger.info("Event --> automatic backup");
        
        if(!BackupOperations.CheckInputCorrect(currentBackup.getBackupName(),startPathField.getText(), destinationPathField.getText(), null)) return false;

        // if the file has not been saved you need to save it before setting the auto backup
        if(currentBackup.isAutoBackup() == false || currentBackup.getNextDateBackup() == null || currentBackup.getTimeIntervalBackup() == null) {
            if (currentBackup.getBackupName() == null || currentBackup.getBackupName().isEmpty()) return false;

            // message
            TimeInterval timeInterval = openTimePicker(null);
            if (timeInterval == null) return false;

            //set date for next backup
            LocalDateTime nextDateBackup = BackupManager.getNexDateBackup(timeInterval);

            currentBackup.setTimeIntervalBackup(timeInterval);
            currentBackup.setNextDateBackup(nextDateBackup);
            btnTimePicker.setToolTipText(timeInterval.toString());
            btnTimePicker.setEnabled(true);

            logger.info("Event --> Next date backup setted to " + nextDateBackup);

            openBackupActivationMessage(timeInterval);
        }

        currentBackup.setInitialPath(GetStartPathField());
        currentBackup.setDestinationPath(GetDestinationPathField());
        return true;
    }

    private void setAutoBackupOn(Backup backup) {
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

    private void enableTimePickerButton(Backup backup) {
        if (backup.getTimeIntervalBackup() != null) {
            btnTimePicker.setToolTipText(backup.getTimeIntervalBackup().toString());
            btnTimePicker.setEnabled(true);
        } else {
            btnTimePicker.setEnabled(true);
        }  
    }

    private void disableTimePickerButton() {
        btnTimePicker.setToolTipText(TranslationCategory.BACKUP_ENTRY.getTranslation(TranslationKey.TIME_PICKER_TOOLTIP));
        btnTimePicker.setEnabled(false);
    }

    private void disableAutoBackup(Backup backup) {
        logger.info("Event --> auto backup disabled");
                
        backup.setTimeIntervalBackup(null);
        backup.setNextDateBackup(null);
        backup.setAutoBackup(false);
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
        SingleBackup.setText(TranslationCategory.BACKUP_ENTRY.getTranslation(TranslationKey.SINGLE_BACKUP_BUTTON));
        SingleBackup.setToolTipText(TranslationCategory.BACKUP_ENTRY.getTranslation(TranslationKey.SINGLE_BACKUP_TOOLTIP));
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
        jLabel1.setText(TranslationCategory.BACKUP_ENTRY.getTranslation(TranslationKey.BACKUP_NAME));
        backupName.setToolTipText(TranslationCategory.BACKUP_ENTRY.getTranslation(TranslationKey.BACKUP_NAME_TOOLTIP));
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
        SingleBackup = new javax.swing.JButton();
        toggleAutoBackup = new javax.swing.JToggleButton();
        btnTimePicker = new backupmanager.svg.SVGButton();
        maxBackupCountSpinner = new javax.swing.JSpinner();
        jLabel4 = new javax.swing.JLabel();
        closeButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        backupName = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();

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

        SingleBackup.setBackground(new java.awt.Color(51, 153, 255));
        SingleBackup.setForeground(new java.awt.Color(255, 255, 255));
        SingleBackup.setText("Single Backup");
        SingleBackup.setToolTipText("Perform the backup");
        SingleBackup.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        SingleBackup.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SingleBackupActionPerformed(evt);
            }
        });

        toggleAutoBackup.setText("Auto Backup");
        toggleAutoBackup.setToolTipText("Enable/Disable automatic backup");
        toggleAutoBackup.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
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

        backupName.setToolTipText("(Required) Backup name");
        backupName.setActionCommand("null");
        backupName.setAlignmentX(0.0F);
        backupName.setAlignmentY(0.0F);
        backupName.setAutoscrolls(false);
        backupName.setMaximumSize(new java.awt.Dimension(465, 26));
        backupName.setMinimumSize(new java.awt.Dimension(465, 26));
        backupName.setPreferredSize(new java.awt.Dimension(465, 26));

        jLabel1.setText("Backup name:");

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
                                            .addComponent(SingleBackup, javax.swing.GroupLayout.PREFERRED_SIZE, 188, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(toggleAutoBackup, javax.swing.GroupLayout.PREFERRED_SIZE, 188, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(btnTimePicker, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 244, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(maxBackupCountSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(startPathField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(backupName, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnPathSearch1, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(15, 15, 15))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(12, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(backupName, javax.swing.GroupLayout.DEFAULT_SIZE, 32, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
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
                        .addComponent(SingleBackup, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
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
        List<RunningBackups> backups = RunningBackups.readBackupListFromJSON();
        if (backups.size() > 0) {
            JOptionPane.showMessageDialog(null, TranslationCategory.DIALOGS.getTranslation(TranslationKey.WARNING_BACKUP_ALREADY_IN_PROGRESS_MESSAGE), TranslationCategory.DIALOGS.getTranslation(TranslationKey.WARNING_GENERIC_TITLE), JOptionPane.WARNING_MESSAGE);
            return;
        }

        // update currentBackup
        if (currentBackup == null) {
            currentBackup = getBackup();
        } 

        SingleBackup(startPathField.getText(), destinationPathField.getText(), backupTable);
    }//GEN-LAST:event_SingleBackupActionPerformed

    private void toggleAutoBackupActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toggleAutoBackupActionPerformed
        logger.info("Event --> Changing auto backup preference");

        // checks
        if (!BackupOperations.CheckInputCorrect(currentBackup.getBackupName(),startPathField.getText(), destinationPathField.getText(), null)) {
            setAutoBackupOff();
            return;
        }
        if (currentBackup.isAutoBackup()) {
            int response = JOptionPane.showConfirmDialog(null, TranslationCategory.DIALOGS.getTranslation(TranslationKey.CONFIRMATION_MESSAGE_CANCEL_AUTO_BACKUP), TranslationCategory.DIALOGS.getTranslation(TranslationKey.CONFIRMATION_REQUIRED_TITLE), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (response != JOptionPane.YES_OPTION) {
                setAutoBackupOff();
                return;
            }
        }

        boolean enabled = toggleAutoBackup.isSelected();
        if (enabled && automaticBackup()) {
            logger.info("Event --> Auto Backup setted to Enabled");
            setAutoBackupOn(currentBackup);
        }
        else {
            logger.info("Event --> Auto Backup setted to Disabled");
            disableAutoBackup(currentBackup);
            setAutoBackupOff();
            return;
        }

        currentBackup.setAutoBackup(enabled);
    }//GEN-LAST:event_toggleAutoBackupActionPerformed

    private void btnTimePickerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTimePickerActionPerformed
        TimeInterval timeInterval = openTimePicker(currentBackup.getTimeIntervalBackup());
        if (timeInterval == null) return;

        btnTimePicker.setToolTipText(timeInterval.toString());
        LocalDateTime nextDateBackup = BackupManager.getNexDateBackup(timeInterval);

        currentBackup.setTimeIntervalBackup(timeInterval);
        currentBackup.setNextDateBackup(nextDateBackup);
        currentBackup.setInitialPath(GetStartPathField());
        currentBackup.setDestinationPath(GetDestinationPathField());

        openBackupActivationMessage(timeInterval);
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
        closeOk = true;

        currentBackup = getBackup();

        if (create) {
            if (Backup.getBackupByName(currentBackup.getBackupName()) != null) {
                int response = JOptionPane.showConfirmDialog(null, TranslationCategory.DIALOGS.getTranslation(TranslationKey.DUPLICATED_BACKUP_NAME_MESSAGE), TranslationCategory.DIALOGS.getTranslation(TranslationKey.CONFIRMATION_REQUIRED_TITLE), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (response == JOptionPane.YES_OPTION) {
                    BackupManager.RemoveBackup(currentBackup.getBackupName());
                } else {
                    return;
                }
            }
            BackupManager.newBackup(currentBackup);
        } else {
            BackupManager.updateBackup(currentBackup);
        }

        this.dispose();
    }//GEN-LAST:event_okButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton SingleBackup;
    private javax.swing.JTextField backupName;
    private javax.swing.JTextArea backupNoteTextArea;
    private backupmanager.svg.SVGButton btnPathSearch1;
    private backupmanager.svg.SVGButton btnPathSearch2;
    private backupmanager.svg.SVGButton btnTimePicker;
    private javax.swing.JButton closeButton;
    private javax.swing.JTextField destinationPathField;
    private javax.swing.JLabel jLabel1;
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
