package backupmanager.GUI;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.formdev.flatlaf.FlatClientProperties;

import backupmanager.Managers.ExportManager;
import backupmanager.Charts;
import backupmanager.Dialogs.EntryUserDialog;
import backupmanager.Email.EmailSender;
import backupmanager.Entities.Backup;
import backupmanager.Entities.Preferences;
import backupmanager.Entities.RunningBackups;
import backupmanager.Entities.User;
import backupmanager.Enums.ConfigKey;
import backupmanager.Enums.LanguagesEnum;
import backupmanager.Enums.MenuItems;
import backupmanager.Enums.TranslationLoaderEnum;
import backupmanager.Enums.TranslationLoaderEnum.TranslationCategory;
import backupmanager.Enums.TranslationLoaderEnum.TranslationKey;
import backupmanager.Json.JSONConfigReader;
import backupmanager.Managers.BackupManager;
import backupmanager.Managers.ExceptionManager;
import backupmanager.Managers.ThemeManager;
import backupmanager.Repositories.BackupConfigurationRepository;
import backupmanager.Repositories.UserRepository;
import backupmanager.Services.BackupObserver;
import backupmanager.Table.BackupTable;
import backupmanager.Table.BackupTableModel;
import backupmanager.Table.CheckboxCellRenderer;
import backupmanager.Table.StripedRowRenderer;
import backupmanager.Table.TableDataManager;
import backupmanager.Widgets.SideMenuPanel;
import java.awt.CardLayout;

/**
 * @author Dennis Turco
 */
public final class BackupManagerGUI extends javax.swing.JFrame {
    private static final Logger logger = LoggerFactory.getLogger(BackupManagerGUI.class);
    public static final DateTimeFormatter dateForfolderNameFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH.mm.ss");
    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    private final BackupManager backupManager;
    private final BackupObserver observer;
    public static List<Backup> backups;
    public static DefaultTableModel model;
    public static BackupTable backupTable;
    public static BackupTableModel tableModel;
    public static BackupProgressGUI progressBar;
    private Integer selectedRow;
    private final String currentVersion;
    private final SideMenuPanel sp;

    public BackupManagerGUI() {
        ThemeManager.updateThemeFrame(this);

        initComponents();
        currentVersion = ConfigKey.VERSION.getValue();

        sp = new SideMenuPanel(this);

        // logo application
        Image icon = new ImageIcon(this.getClass().getResource(ConfigKey.LOGO_IMG.getValue())).getImage();
        this.setIconImage(icon);

        initializeMenuItems();
        setScreenSize();
        researchField.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, new com.formdev.flatlaf.extras.FlatSVGIcon("res/img/search.svg", 16, 16));
        setTranslations();

        // first initialize the table, then start observer thread
        initializeTable();
        observer = new BackupObserver(dateForfolderNameFormatter, 1000);
        observer.start();

        interruptBackupPopupItem.setEnabled(false);

        setSvgImages();
        checkForFirstAccess();
        backupManager = new BackupManager(this);
        jSeparator5.setVisible(false);

        // TODO: remove this
        interruptBackupPopupItem.setVisible(false);

        initSidebar();
    }

    private void checkForFirstAccess() {
        logger.debug("Checking for first access");
        User user = UserRepository.getLastUser();

        if (user == null) { // first access
            setLanguageBasedOnPcLanguage();
            createNewUser();
        } else if (user.equals(User.getDefaultUser())) { // user unregistered
            updateUnregisteredUser(user.getId());
        } else {
            logger.info("Current user: " + user.toString());
        }
    }

    private void createNewUser() {
        User newUser = openUserDialogAndObtainTheResult();

        if (newUser == null) {
            UserRepository.insertUser(User.getDefaultUser());
            return;
        }

        UserRepository.insertUser(newUser);

        sendRegistrationEmail(newUser);
    }

    private void updateUnregisteredUser(int userId) {
        User newUser = openUserDialogAndObtainTheResult();

        if (newUser == null || newUser.equals(User.getDefaultUser())) {
            return;
        }

        newUser.setId(userId);
        UserRepository.updateUser(newUser);

        sendRegistrationEmail(newUser);
    }

    private User openUserDialogAndObtainTheResult() {
        EntryUserDialog userDialog = new EntryUserDialog(this, true);
        userDialog.setVisible(true);
        return userDialog.getUser();
    }

    private void sendRegistrationEmail(User user) {
        EmailSender.sendUserCreationEmail(user);
        EmailSender.sendConfirmEmailToUser(user);
    }

    private void initSidebar() {
        sp.setMain(null);
        sp.setSide(sidebar);
        sp.setMinWidth(55);
        sp.setMaxWidth(150);
        sp.setMainAnimationEnabled(true);
        sp.setSpeed(4);
        sp.setResponsiveMinWidth(1300);
    }

    private void setLanguageBasedOnPcLanguage() {
        Locale defaultLocale = Locale.getDefault();
        String language = defaultLocale.getLanguage();

        logger.info("Setting default language to: " + language);

        switch (language) {
            case "en":
                Preferences.setLanguage(LanguagesEnum.ENG);
                break;
            case "it":
                Preferences.setLanguage(LanguagesEnum.ITA);
                break;
            case "es":
                Preferences.setLanguage(LanguagesEnum.ESP);
                break;
            case "de":
                Preferences.setLanguage(LanguagesEnum.DEU);
                break;
            case "fr":
                Preferences.setLanguage(LanguagesEnum.FRA);
                break;
            default:
                Preferences.setLanguage(LanguagesEnum.ENG);
        }

        reloadPreferences();
    }

    public void showWindow() {
        setVisible(true);
        toFront();
        requestFocus();
    }

    private void setScreenSize() {
        Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
        int width = Math.min((int) size.getWidth(), Integer.parseInt(ConfigKey.GUI_WIDTH.getValue()));
        int height = Math.min((int) size.getHeight(), Integer.parseInt(ConfigKey.GUI_HEIGHT.getValue()));

        this.setSize(width,height);
    }

    public void reloadPreferences() {
        logger.info("Reloading preferences");

        Preferences.updateAllPreferences();

        // load language
        try {
            TranslationLoaderEnum.loadTranslations(ConfigKey.LANGUAGES_DIRECTORY_STRING.getValue() + Preferences.getLanguage().getFileName());
            setTranslations();
        } catch (IOException ex) {
            logger.error("An error occurred during reloading preferences operation: " + ex.getMessage(), ex);
            ExceptionManager.openExceptionMessage(ex.getMessage(), Arrays.toString(ex.getStackTrace()));
        }

        // load theme
        ThemeManager.updateThemeFrame(this);
        ThemeManager.refreshPopup(TablePopup);
        setSvgImages();
    }

    private void displayBackupList() {
        BackupTableModel model = new BackupTableModel(getColumnTranslations(), 0);

        // Populate the model with backup data
        for (Backup backup : backups) {
            model.addRow(new Object[]{
                backup.getName(),
                backup.getTargetPath(),
                backup.getDestinationPath(),
                backup.getLastBackupDate() != null ? backup.getLastBackupDate().format(formatter) : "",
                backup.isAutomatic(),
                backup.getNextBackupDate() != null ? backup.getNextBackupDate().format(formatter) : "",
                backup.getTimeIntervalBackup() != null ? backup.getTimeIntervalBackup().toString() : ""
            });
        }

        backupTable = new BackupTable(model);

        // Add key bindings using InputMap and ActionMap
        InputMap inputMap = backupTable.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap actionMap = backupTable.getActionMap();

        // Handle Enter key
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "enterKey");
        actionMap.put("enterKey", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = backupTable.getSelectedRow();
                if (selectedRow == -1) return;

                logger.debug("Enter key pressed on row: " + selectedRow);
                backupManager.openBackupByName((String) backupTable.getValueAt(selectedRow, 0));

                backupManager.openBackupEntryDialog();
            }
        });

        // Handle Delete key
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "deleteKey");
        actionMap.put("deleteKey", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int[] selectedRows = backupTable.getSelectedRows();
                if (selectedRows.length == 0) return;

                logger.debug("Delete key pressed on rows: " + Arrays.toString(selectedRows));

                int response = JOptionPane.showConfirmDialog(null, TranslationCategory.DIALOGS.getTranslation(TranslationKey.CONFIRMATION_DELETION_MESSAGE), TranslationCategory.DIALOGS.getTranslation(TranslationKey.CONFIRMATION_DELETION_TITLE), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (response != JOptionPane.YES_OPTION) {
                    return;
                }

                for (int row : selectedRows) {
                    backupManager.deleteBackup(row, backupTable, false);
                }
            }
        });

        // Apply renderers for each column
        TableColumnModel columnModel = backupTable.getColumnModel();

        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            if (i == 4) {
                columnModel.getColumn(i).setCellRenderer(new CheckboxCellRenderer());
                columnModel.getColumn(i).setCellEditor(backupTable.getDefaultEditor(Boolean.class));
            } else {
                columnModel.getColumn(i).setCellRenderer(new StripedRowRenderer());
            }
        }

        // Add the existing mouse listener to the new table
        backupTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tableMouseClicked(evt); // Reuse the existing method
            }
        });

        // Update the global model reference
        BackupManagerGUI.model = model;

        // Replace the existing table in the GUI
        JScrollPane scrollPane = (JScrollPane) table.getParent().getParent();
        table = backupTable; // Update the reference to the new table
        scrollPane.setViewportView(table); // Replace the table in the scroll pane
    }

    private void researchInTable() {
        List<Backup> tempBackups = new ArrayList<>();

        String research = researchField.getText();

        for (Backup backup : backups) {
            if (backup.getName().contains(research) ||
                    backup.getTargetPath().contains(research) ||
                    backup.getDestinationPath().contains(research) ||
                    (backup.getLastBackupDate() != null && backup.getLastBackupDate().toString().contains(research)) ||
                    (backup.getNextBackupDate() != null && backup.getNextBackupDate().toString().contains(research)) ||
                    (backup.getTimeIntervalBackup() != null && backup.getTimeIntervalBackup().toString().contains(research))) {
                tempBackups.add(backup);
            }
        }

        TableDataManager.updateTableWithNewBackupList(tempBackups, formatter);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     *
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        TablePopup = new javax.swing.JPopupMenu();
        EditPoputItem = new javax.swing.JMenuItem();
        DeletePopupItem = new javax.swing.JMenuItem();
        interruptBackupPopupItem = new javax.swing.JMenuItem();
        DuplicatePopupItem = new javax.swing.JMenuItem();
        renamePopupItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        OpenInitialFolderItem = new javax.swing.JMenuItem();
        OpenInitialDestinationItem = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        Backup = new javax.swing.JMenu();
        RunBackupPopupItem = new javax.swing.JMenuItem();
        AutoBackupMenuItem = new javax.swing.JCheckBoxMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        jMenu4 = new javax.swing.JMenu();
        CopyBackupNamePopupItem = new javax.swing.JMenuItem();
        CopyInitialPathPopupItem = new javax.swing.JMenuItem();
        CopyDestinationPathPopupItem = new javax.swing.JMenuItem();
        sidebar = new javax.swing.JPanel();
        pageBackupList = new backupmanager.svg.SVGButton();
        pageDashboard = new backupmanager.svg.SVGButton();
        pageSettings = new backupmanager.svg.SVGButton();
        men = new backupmanager.svg.SVGButton();
        copyrightLabelSide = new javax.swing.JLabel();
        panelVersion = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        layeredCardPanel = new javax.swing.JLayeredPane();
        panelBackupList = new javax.swing.JPanel();
        detailsLabel = new javax.swing.JLabel();
        researchField = new javax.swing.JTextField();
        exportAsPdfBtn = new backupmanager.svg.SVGButton();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();
        addBackupEntryButton = new backupmanager.svg.SVGButton();
        exportAsCsvBtn = new backupmanager.svg.SVGButton();
        ExportLabel = new javax.swing.JLabel();
        panelDashboard = new javax.swing.JPanel();
        chart2 = new javax.swing.JPanel();
        chart1 = new javax.swing.JPanel();
        chart1TitleLabel = new javax.swing.JLabel();
        chart2TitleLabel = new javax.swing.JLabel();
        totalBackupsPanel = new javax.swing.JPanel();
        totalBackupsTitleLabel = new javax.swing.JLabel();
        totalBackupsNumber = new javax.swing.JLabel();
        totalBackupConfigurationsPanel = new javax.swing.JPanel();
        totalBackupConfigurationsTitleLabel = new javax.swing.JLabel();
        totalBackupConfigurationsNumber = new javax.swing.JLabel();
        totalSpaceUsedPanel = new javax.swing.JPanel();
        totalSpaceTitleLabel = new javax.swing.JLabel();
        totalSpaceNumber = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        MenuNew = new backupmanager.svg.SVGMenuItem();
        MenuSave = new backupmanager.svg.SVGMenuItem();
        MenuSaveWithName = new backupmanager.svg.SVGMenuItem();
        jSeparator4 = new javax.swing.JPopupMenu.Separator();
        MenuImport = new backupmanager.svg.SVGMenuItem();
        MenuExport = new backupmanager.svg.SVGMenuItem();
        jSeparator5 = new javax.swing.JPopupMenu.Separator();
        MenuClear = new backupmanager.svg.SVGMenuItem();
        MenuHistory = new backupmanager.svg.SVGMenuItem();
        jMenu2 = new javax.swing.JMenu();
        MenuPreferences = new backupmanager.svg.SVGMenuItem();
        MenuQuit = new backupmanager.svg.SVGMenuItem();
        jMenu3 = new javax.swing.JMenu();
        MenuWebsite = new backupmanager.svg.SVGMenuItem();
        MenuInfoPage = new backupmanager.svg.SVGMenuItem();
        MenuShare = new backupmanager.svg.SVGMenuItem();
        MenuDonate = new backupmanager.svg.SVGMenu();
        MenuPaypalDonate = new backupmanager.svg.SVGMenuItem();
        MenuBuyMeACoffeDonate = new backupmanager.svg.SVGMenuItem();
        jMenu5 = new javax.swing.JMenu();
        MenuBugReport = new backupmanager.svg.SVGMenuItem();
        MenuSupport = new backupmanager.svg.SVGMenuItem();

        EditPoputItem.setText("Edit");
        EditPoputItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                EditPoputItemActionPerformed(evt);
            }
        });
        TablePopup.add(EditPoputItem);

        DeletePopupItem.setText("Delete");
        DeletePopupItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DeletePopupItemActionPerformed(evt);
            }
        });
        TablePopup.add(DeletePopupItem);

        interruptBackupPopupItem.setText("Interrupt");
        interruptBackupPopupItem.setToolTipText("");
        interruptBackupPopupItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                interruptBackupPopupItemActionPerformed(evt);
            }
        });
        TablePopup.add(interruptBackupPopupItem);

        DuplicatePopupItem.setText("Duplicate");
        DuplicatePopupItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DuplicatePopupItemActionPerformed(evt);
            }
        });
        TablePopup.add(DuplicatePopupItem);

        renamePopupItem.setText("Rename backup");
        renamePopupItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                renamePopupItemActionPerformed(evt);
            }
        });
        TablePopup.add(renamePopupItem);
        TablePopup.add(jSeparator1);

        OpenInitialFolderItem.setText("Open initial folder");
        OpenInitialFolderItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                OpenInitialFolderItemActionPerformed(evt);
            }
        });
        TablePopup.add(OpenInitialFolderItem);

        OpenInitialDestinationItem.setText("Open destination folder");
        OpenInitialDestinationItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                OpenInitialDestinationItemActionPerformed(evt);
            }
        });
        TablePopup.add(OpenInitialDestinationItem);
        TablePopup.add(jSeparator3);

        Backup.setText("Backup");

        RunBackupPopupItem.setText("Run single backup");
        RunBackupPopupItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RunBackupPopupItemActionPerformed(evt);
            }
        });
        Backup.add(RunBackupPopupItem);

        AutoBackupMenuItem.setSelected(true);
        AutoBackupMenuItem.setText("Auto Backup");
        AutoBackupMenuItem.setToolTipText("");
        AutoBackupMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AutoBackupMenuItemActionPerformed(evt);
            }
        });
        Backup.add(AutoBackupMenuItem);

        TablePopup.add(Backup);
        TablePopup.add(jSeparator2);

        jMenu4.setText("Copy text");

        CopyBackupNamePopupItem.setText("Copy backup name");
        CopyBackupNamePopupItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CopyBackupNamePopupItemActionPerformed(evt);
            }
        });
        jMenu4.add(CopyBackupNamePopupItem);

        CopyInitialPathPopupItem.setText("Copy initial path");
        CopyInitialPathPopupItem.setToolTipText("");
        CopyInitialPathPopupItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CopyInitialPathPopupItemActionPerformed(evt);
            }
        });
        jMenu4.add(CopyInitialPathPopupItem);

        CopyDestinationPathPopupItem.setText("Copy destination path");
        CopyDestinationPathPopupItem.setToolTipText("");
        CopyDestinationPathPopupItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CopyDestinationPathPopupItemActionPerformed(evt);
            }
        });
        jMenu4.add(CopyDestinationPathPopupItem);

        TablePopup.add(jMenu4);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Backup Manager");
        setMinimumSize(new java.awt.Dimension(750, 450));

        sidebar.setBackground(new java.awt.Color(16, 84, 129));
        sidebar.setPreferredSize(new java.awt.Dimension(55, 32));

        pageBackupList.setForeground(new java.awt.Color(195, 217, 233));
        pageBackupList.setText("Backup List");
        pageBackupList.setBorderPainted(false);
        pageBackupList.setContentAreaFilled(false);
        pageBackupList.setFont(new java.awt.Font("Microsoft PhagsPa", 0, 14)); // NOI18N
        pageBackupList.setHideActionText(true);
        pageBackupList.setHorizontalAlignment(javax.swing.SwingConstants.LEADING);
        pageBackupList.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        pageBackupList.setIconTextGap(20);
        pageBackupList.setMargin(new java.awt.Insets(2, 0, 2, 14));
        pageBackupList.setMinimumSize(new java.awt.Dimension(0, 0));
        pageBackupList.setPreferredSize(new java.awt.Dimension(50, 574));
        pageBackupList.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pageBackupListActionPerformed(evt);
            }
        });

        pageDashboard.setForeground(new java.awt.Color(195, 217, 233));
        pageDashboard.setText("Dashboard");
        pageDashboard.setBorderPainted(false);
        pageDashboard.setContentAreaFilled(false);
        pageDashboard.setFont(new java.awt.Font("Microsoft PhagsPa", 0, 14)); // NOI18N
        pageDashboard.setHideActionText(true);
        pageDashboard.setHorizontalAlignment(javax.swing.SwingConstants.LEADING);
        pageDashboard.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        pageDashboard.setIconTextGap(20);
        pageDashboard.setMargin(new java.awt.Insets(2, 0, 2, 14));
        pageDashboard.setMinimumSize(new java.awt.Dimension(0, 0));
        pageDashboard.setPreferredSize(new java.awt.Dimension(50, 574));
        pageDashboard.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pageDashboardActionPerformed(evt);
            }
        });

        pageSettings.setForeground(new java.awt.Color(195, 217, 233));
        pageSettings.setText("Settings");
        pageSettings.setBorderPainted(false);
        pageSettings.setContentAreaFilled(false);
        pageSettings.setFocusPainted(false);
        pageSettings.setFont(new java.awt.Font("Microsoft PhagsPa", 0, 14)); // NOI18N
        pageSettings.setHideActionText(true);
        pageSettings.setHorizontalAlignment(javax.swing.SwingConstants.LEADING);
        pageSettings.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        pageSettings.setIconTextGap(20);
        pageSettings.setMargin(new java.awt.Insets(2, 0, 2, 14));
        pageSettings.setMinimumSize(new java.awt.Dimension(0, 35));
        pageSettings.setPreferredSize(new java.awt.Dimension(50, 574));
        pageSettings.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pageSettingsActionPerformed(evt);
            }
        });

        men.setBackground(new java.awt.Color(34, 40, 47));
        men.setBorderPainted(false);
        men.setContentAreaFilled(false);
        men.setFocusPainted(false);
        men.setFont(new java.awt.Font("Microsoft PhagsPa", 0, 14)); // NOI18N
        men.setHideActionText(true);
        men.setHorizontalAlignment(javax.swing.SwingConstants.LEADING);
        men.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        men.setIconTextGap(20);
        men.setMargin(new java.awt.Insets(2, 3, 2, 14));
        men.setMinimumSize(new java.awt.Dimension(0, 35));
        men.setPreferredSize(new java.awt.Dimension(50, 574));
        men.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menActionPerformed(evt);
            }
        });

        copyrightLabelSide.setFont(new java.awt.Font("Microsoft PhagsPa", 0, 10)); // NOI18N
        copyrightLabelSide.setForeground(new java.awt.Color(126, 170, 214));
        copyrightLabelSide.setText("Shard");
        copyrightLabelSide.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);

        javax.swing.GroupLayout sidebarLayout = new javax.swing.GroupLayout(sidebar);
        sidebar.setLayout(sidebarLayout);
        sidebarLayout.setHorizontalGroup(
            sidebarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sidebarLayout.createSequentialGroup()
                .addGroup(sidebarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(sidebarLayout.createSequentialGroup()
                        .addGroup(sidebarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(sidebarLayout.createSequentialGroup()
                                .addGap(55, 55, 55)
                                .addComponent(copyrightLabelSide, javax.swing.GroupLayout.PREFERRED_SIZE, 0, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(sidebarLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(pageSettings, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(sidebarLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(sidebarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(pageBackupList, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(sidebarLayout.createSequentialGroup()
                                .addComponent(men, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(pageDashboard, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        sidebarLayout.setVerticalGroup(
            sidebarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sidebarLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(men, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(pageBackupList, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(pageDashboard, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(pageSettings, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 204, Short.MAX_VALUE)
                .addComponent(copyrightLabelSide, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18))
        );

        getContentPane().add(sidebar, java.awt.BorderLayout.WEST);

        panelVersion.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panelVersion.setPreferredSize(new java.awt.Dimension(100, 30));

        jLabel3.setText("Version 2.0.2");
        jLabel3.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);

        javax.swing.GroupLayout panelVersionLayout = new javax.swing.GroupLayout(panelVersion);
        panelVersion.setLayout(panelVersionLayout);
        panelVersionLayout.setHorizontalGroup(
            panelVersionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelVersionLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 997, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelVersionLayout.setVerticalGroup(
            panelVersionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelVersionLayout.createSequentialGroup()
                .addComponent(jLabel3)
                .addGap(0, 4, Short.MAX_VALUE))
        );

        getContentPane().add(panelVersion, java.awt.BorderLayout.PAGE_END);

        layeredCardPanel.setLayout(new java.awt.CardLayout());

        panelBackupList.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panelBackupList.setVerifyInputWhenFocusTarget(false);

        detailsLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        researchField.setToolTipText("Research bar");
        researchField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                researchFieldKeyTyped(evt);
            }
        });

        exportAsPdfBtn.setToolTipText("Export as .pdf");
        exportAsPdfBtn.setMaximumSize(new java.awt.Dimension(32, 32));
        exportAsPdfBtn.setMinimumSize(new java.awt.Dimension(32, 32));
        exportAsPdfBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportAsPdfBtnActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Segoe UI", 0, 20)); // NOI18N
        jLabel1.setText("|");
        jLabel1.setAlignmentY(0.0F);

        table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Backup name", "Initial path", "Destination path", "Last backup", "Auto backup", "Next date backup", "Days interval backup"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Boolean.class, java.lang.String.class, java.lang.Integer.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        table.setCursor(new java.awt.Cursor(java.awt.Cursor.CROSSHAIR_CURSOR));
        table.setRowHeight(50);
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tableMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(table);

        addBackupEntryButton.setToolTipText("Add new backup");
        addBackupEntryButton.setMaximumSize(new java.awt.Dimension(32, 32));
        addBackupEntryButton.setMinimumSize(new java.awt.Dimension(32, 32));
        addBackupEntryButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addBackupEntryButtonActionPerformed(evt);
            }
        });

        exportAsCsvBtn.setToolTipText("Export as .csv");
        exportAsCsvBtn.setMaximumSize(new java.awt.Dimension(32, 32));
        exportAsCsvBtn.setMinimumSize(new java.awt.Dimension(32, 32));
        exportAsCsvBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportAsCsvBtnActionPerformed(evt);
            }
        });

        ExportLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        ExportLabel.setText("Export As:");

        javax.swing.GroupLayout panelBackupListLayout = new javax.swing.GroupLayout(panelBackupList);
        panelBackupList.setLayout(panelBackupListLayout);
        panelBackupListLayout.setHorizontalGroup(
            panelBackupListLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(detailsLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jScrollPane1)
            .addGroup(panelBackupListLayout.createSequentialGroup()
                .addComponent(addBackupEntryButton, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 9, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(researchField, javax.swing.GroupLayout.PREFERRED_SIZE, 321, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 256, Short.MAX_VALUE)
                .addComponent(ExportLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 238, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(exportAsCsvBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(exportAsPdfBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        panelBackupListLayout.setVerticalGroup(
            panelBackupListLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBackupListLayout.createSequentialGroup()
                .addGroup(panelBackupListLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(addBackupEntryButton, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(panelBackupListLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel1)
                        .addComponent(researchField, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelBackupListLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(ExportLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(exportAsCsvBtn, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(exportAsPdfBtn, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 297, Short.MAX_VALUE)
                .addGap(12, 12, 12)
                .addComponent(detailsLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
        );

        researchField.getAccessibleContext().setAccessibleName("");

        layeredCardPanel.add(panelBackupList, "BackupList");

        chart2.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        chart2.setPreferredSize(new java.awt.Dimension(230, 180));

        javax.swing.GroupLayout chart2Layout = new javax.swing.GroupLayout(chart2);
        chart2.setLayout(chart2Layout);
        chart2Layout.setHorizontalGroup(
            chart2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        chart2Layout.setVerticalGroup(
            chart2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 174, Short.MAX_VALUE)
        );

        chart1.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        chart1.setPreferredSize(new java.awt.Dimension(230, 180));

        javax.swing.GroupLayout chart1Layout = new javax.swing.GroupLayout(chart1);
        chart1.setLayout(chart1Layout);
        chart1Layout.setHorizontalGroup(
            chart1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        chart1Layout.setVerticalGroup(
            chart1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 174, Short.MAX_VALUE)
        );

        chart1TitleLabel.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        chart1TitleLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        chart1TitleLabel.setText("Title Chart 1");

        chart2TitleLabel.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        chart2TitleLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        chart2TitleLabel.setText("Title Chart 2");

        totalBackupsPanel.setBorder(new javax.swing.border.MatteBorder(null));

        totalBackupsTitleLabel.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        totalBackupsTitleLabel.setText("Total Backups");

        totalBackupsNumber.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        totalBackupsNumber.setText("10");

        javax.swing.GroupLayout totalBackupsPanelLayout = new javax.swing.GroupLayout(totalBackupsPanel);
        totalBackupsPanel.setLayout(totalBackupsPanelLayout);
        totalBackupsPanelLayout.setHorizontalGroup(
            totalBackupsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(totalBackupsPanelLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(totalBackupsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(totalBackupsNumber, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(totalBackupsTitleLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 194, Short.MAX_VALUE))
                .addContainerGap())
        );
        totalBackupsPanelLayout.setVerticalGroup(
            totalBackupsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(totalBackupsPanelLayout.createSequentialGroup()
                .addComponent(totalBackupsTitleLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(totalBackupsNumber, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 31, Short.MAX_VALUE))
        );

        totalBackupConfigurationsPanel.setBorder(new javax.swing.border.MatteBorder(null));
        totalBackupConfigurationsPanel.setPreferredSize(new java.awt.Dimension(214, 104));

        totalBackupConfigurationsTitleLabel.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        totalBackupConfigurationsTitleLabel.setText("Total Backups");

        totalBackupConfigurationsNumber.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        totalBackupConfigurationsNumber.setText("10");

        javax.swing.GroupLayout totalBackupConfigurationsPanelLayout = new javax.swing.GroupLayout(totalBackupConfigurationsPanel);
        totalBackupConfigurationsPanel.setLayout(totalBackupConfigurationsPanelLayout);
        totalBackupConfigurationsPanelLayout.setHorizontalGroup(
            totalBackupConfigurationsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(totalBackupConfigurationsPanelLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(totalBackupConfigurationsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(totalBackupConfigurationsNumber, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(totalBackupConfigurationsTitleLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 194, Short.MAX_VALUE))
                .addContainerGap())
        );
        totalBackupConfigurationsPanelLayout.setVerticalGroup(
            totalBackupConfigurationsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(totalBackupConfigurationsPanelLayout.createSequentialGroup()
                .addComponent(totalBackupConfigurationsTitleLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(totalBackupConfigurationsNumber, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 31, Short.MAX_VALUE))
        );

        totalSpaceUsedPanel.setBorder(new javax.swing.border.MatteBorder(null));
        totalSpaceUsedPanel.setPreferredSize(new java.awt.Dimension(214, 104));

        totalSpaceTitleLabel.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        totalSpaceTitleLabel.setText("Total Space Used");

        totalSpaceNumber.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        totalSpaceNumber.setText("10");

        javax.swing.GroupLayout totalSpaceUsedPanelLayout = new javax.swing.GroupLayout(totalSpaceUsedPanel);
        totalSpaceUsedPanel.setLayout(totalSpaceUsedPanelLayout);
        totalSpaceUsedPanelLayout.setHorizontalGroup(
            totalSpaceUsedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(totalSpaceUsedPanelLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(totalSpaceUsedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(totalSpaceNumber, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(totalSpaceTitleLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 194, Short.MAX_VALUE))
                .addContainerGap())
        );
        totalSpaceUsedPanelLayout.setVerticalGroup(
            totalSpaceUsedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(totalSpaceUsedPanelLayout.createSequentialGroup()
                .addComponent(totalSpaceTitleLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(totalSpaceNumber, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 31, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout panelDashboardLayout = new javax.swing.GroupLayout(panelDashboard);
        panelDashboard.setLayout(panelDashboardLayout);
        panelDashboardLayout.setHorizontalGroup(
            panelDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelDashboardLayout.createSequentialGroup()
                .addGap(52, 52, 52)
                .addGroup(panelDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelDashboardLayout.createSequentialGroup()
                        .addComponent(totalBackupsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(totalBackupConfigurationsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(totalSpaceUsedPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 150, Short.MAX_VALUE)
                .addGroup(panelDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(chart2, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
                    .addComponent(chart1, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
                    .addComponent(chart2TitleLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
                    .addComponent(chart1TitleLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(16, 16, 16))
        );
        panelDashboardLayout.setVerticalGroup(
            panelDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelDashboardLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(panelDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelDashboardLayout.createSequentialGroup()
                        .addGroup(panelDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(panelDashboardLayout.createSequentialGroup()
                                .addComponent(chart1TitleLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(chart1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(totalBackupConfigurationsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(chart2TitleLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(chart2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelDashboardLayout.createSequentialGroup()
                        .addComponent(totalBackupsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(20, 20, 20)
                        .addComponent(totalSpaceUsedPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(34, Short.MAX_VALUE))
        );

        layeredCardPanel.add(panelDashboard, "Dashboard");

        getContentPane().add(layeredCardPanel, java.awt.BorderLayout.CENTER);

        jMenu1.setText("File");

        MenuNew.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        MenuNew.setText("New");
        MenuNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MenuNewActionPerformed(evt);
            }
        });
        jMenu1.add(MenuNew);

        MenuSave.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        MenuSave.setText("Save");
        MenuSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MenuSaveActionPerformed(evt);
            }
        });
        jMenu1.add(MenuSave);

        MenuSaveWithName.setText("Save with name");
        MenuSaveWithName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MenuSaveWithNameActionPerformed(evt);
            }
        });
        jMenu1.add(MenuSaveWithName);
        jMenu1.add(jSeparator4);

        MenuImport.setText("Import backup list");
        jMenu1.add(MenuImport);

        MenuExport.setText("Export backup list");
        jMenu1.add(MenuExport);
        jMenu1.add(jSeparator5);

        MenuClear.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        MenuClear.setText("Clear");
        MenuClear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MenuClearActionPerformed(evt);
            }
        });
        jMenu1.add(MenuClear);

        MenuHistory.setText("History");
        MenuHistory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MenuHistoryActionPerformed(evt);
            }
        });
        jMenu1.add(MenuHistory);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Options");

        MenuPreferences.setText("Preferences");
        MenuPreferences.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MenuPreferencesActionPerformed(evt);
            }
        });
        jMenu2.add(MenuPreferences);

        MenuQuit.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F4, java.awt.event.InputEvent.ALT_DOWN_MASK));
        MenuQuit.setText("Quit");
        MenuQuit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MenuQuitActionPerformed(evt);
            }
        });
        jMenu2.add(MenuQuit);

        jMenuBar1.add(jMenu2);

        jMenu3.setText("About");

        MenuWebsite.setText("Website");
        MenuWebsite.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MenuWebsiteActionPerformed(evt);
            }
        });
        jMenu3.add(MenuWebsite);

        MenuInfoPage.setText("Info");
        MenuInfoPage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MenuInfoPageActionPerformed(evt);
            }
        });
        jMenu3.add(MenuInfoPage);

        MenuShare.setText("Share");
        MenuShare.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MenuShareActionPerformed(evt);
            }
        });
        jMenu3.add(MenuShare);

        MenuDonate.setText("Donate");

        MenuPaypalDonate.setText("Paypal");
        MenuPaypalDonate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MenuPaypalDonateActionPerformed(evt);
            }
        });
        MenuDonate.add(MenuPaypalDonate);

        MenuBuyMeACoffeDonate.setText("Buy me a coffe");
        MenuBuyMeACoffeDonate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MenuBuyMeACoffeDonateActionPerformed(evt);
            }
        });
        MenuDonate.add(MenuBuyMeACoffeDonate);

        jMenu3.add(MenuDonate);

        jMenuBar1.add(jMenu3);

        jMenu5.setText("Help");

        MenuBugReport.setText("Report a bug");
        MenuBugReport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MenuBugReportActionPerformed(evt);
            }
        });
        jMenu5.add(MenuBugReport);

        MenuSupport.setText("Support");
        MenuSupport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MenuSupportActionPerformed(evt);
            }
        });
        jMenu5.add(MenuSupport);

        jMenuBar1.add(jMenu5);

        setJMenuBar(jMenuBar1);

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void MenuQuitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MenuQuitActionPerformed
        backupManager.menuItemQuit(observer);
    }//GEN-LAST:event_MenuQuitActionPerformed

    private void MenuHistoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MenuHistoryActionPerformed
        backupManager.menuItemHistory();
    }//GEN-LAST:event_MenuHistoryActionPerformed

    private void MenuClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MenuClearActionPerformed
    }//GEN-LAST:event_MenuClearActionPerformed

    private void MenuSaveWithNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MenuSaveWithNameActionPerformed
    }//GEN-LAST:event_MenuSaveWithNameActionPerformed

    private void MenuSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MenuSaveActionPerformed
    }//GEN-LAST:event_MenuSaveActionPerformed

    private void MenuNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MenuNewActionPerformed
        backupManager.menuItemNew(progressBar);
    }//GEN-LAST:event_MenuNewActionPerformed

    private void EditPoputItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_EditPoputItemActionPerformed
        backupManager.popupItemEditBackupName(selectedRow, backupTable, backups);
    }//GEN-LAST:event_EditPoputItemActionPerformed

    private void DeletePopupItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DeletePopupItemActionPerformed
        backupManager.popupItemDelete(selectedRow, backupTable);
    }//GEN-LAST:event_DeletePopupItemActionPerformed

    private void researchFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_researchFieldKeyTyped
        researchInTable();
    }//GEN-LAST:event_researchFieldKeyTyped

    private void tableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableMouseClicked
        selectedRow = table.rowAtPoint(evt.getPoint()); // get index of the row

        if (selectedRow == -1) { // if clicked outside valid rows
            table.clearSelection(); // deselect any selected row
            detailsLabel.setText(""); // clear the label
        } else {
            // get correct backup
            String backupName = (String) backupTable.getValueAt(selectedRow, 0);
            backupmanager.Entities.Backup backup = backupmanager.Entities.Backup.getBackupByName(backups, backupName);

            logger.debug("Selected backup: " + backupName);

            // Handling right mouse button click
            if (SwingUtilities.isRightMouseButton(evt)) {
                logger.info("Right click on row: " + selectedRow);
                AutoBackupMenuItem.setSelected(backup.isAutomatic());
                table.setRowSelectionInterval(selectedRow, selectedRow); // select clicked row
                TablePopup.show(evt.getComponent(), evt.getX(), evt.getY()); // show popup

                // check if the backup is running
                if (RunningBackups.readBackupFromJSON(backupName) == null) {
                    DeletePopupItem.setEnabled(true);
                    interruptBackupPopupItem.setEnabled(false);
                } else {
                    DeletePopupItem.setEnabled(false);
                    interruptBackupPopupItem.setEnabled(true);
                }
            }

            // Handling left mouse button double-click
            else if (SwingUtilities.isLeftMouseButton(evt) && evt.getClickCount() == 2) {
                logger.info("Double-click on row: " + selectedRow);
                backupManager.openBackupByName(backupName);
            }

            // Handling single left mouse button click
            else if (SwingUtilities.isLeftMouseButton(evt)) {
                String backupNameStr = TranslationCategory.BACKUP_LIST.getTranslation(TranslationKey.BACKUP_NAME_DETAIL);
                String initialPathStr = TranslationCategory.BACKUP_LIST.getTranslation(TranslationKey.INITIAL_PATH_DETAIL);
                String destinationPathStr = TranslationCategory.BACKUP_LIST.getTranslation(TranslationKey.DESTINATION_PATH_DETAIL);
                String lastBackupStr = TranslationCategory.BACKUP_LIST.getTranslation(TranslationKey.LAST_BACKUP_DETAIL);
                String nextBackupStr = TranslationCategory.BACKUP_LIST.getTranslation(TranslationKey.NEXT_BACKUP_DATE_DETAIL);
                String timeIntervalBackupStr = TranslationCategory.BACKUP_LIST.getTranslation(TranslationKey.TIME_INTERVAL_DETAIL);
                String creationDateStr = TranslationCategory.BACKUP_LIST.getTranslation(TranslationKey.CREATION_DATE_DETAIL);
                String lastUpdateDateStr = TranslationCategory.BACKUP_LIST.getTranslation(TranslationKey.LAST_UPDATE_DATE_DETAIL);
                String backupCountStr = TranslationCategory.BACKUP_LIST.getTranslation(TranslationKey.BACKUP_COUNT_DETAIL);
                String notesStr = TranslationCategory.BACKUP_LIST.getTranslation(TranslationKey.NOTES_DETAIL);
                String maxBackupsToKeepStr = TranslationCategory.BACKUP_LIST.getTranslation(TranslationKey.MAX_BACKUPS_TO_KEEP_DETAIL);

                detailsLabel.setText(
                    "<html><b>" + backupNameStr + ":</b> " + backup.getName() + ", " +
                    "<b>" + initialPathStr + ":</b> " + backup.getTargetPath() + ", " +
                    "<b>" + destinationPathStr + ":</b> " + backup.getDestinationPath() + ", " +
                    "<b>" + lastBackupStr + ":</b> " + (backup.getLastBackupDate() != null ? backup.getLastBackupDate().format(formatter) : "") + ", " +
                    "<b>" + nextBackupStr + ":</b> " + (backup.getNextBackupDate() != null ? backup.getNextBackupDate().format(formatter) : "_") + ", " +
                    "<b>" + timeIntervalBackupStr + ":</b> " + (backup.getTimeIntervalBackup() != null ? backup.getTimeIntervalBackup().toString() : "_") + ", " +
                    "<b>" + creationDateStr + ":</b> " + (backup.getCreationDate() != null ? backup.getCreationDate().format(formatter) : "_") + ", " +
                    "<b>" + lastUpdateDateStr + ":</b> " + (backup.getLastUpdateDate() != null ? backup.getLastUpdateDate().format(formatter) : "_") + ", " +
                    "<b>" + backupCountStr + ":</b> " + (backup.getCount()) + ", " +
                    "<b>" + maxBackupsToKeepStr + ":</b> " + (backup.getMaxToKeep()) + ", " +
                    "<b>" + notesStr + ":</b> " + (backup.getNotes()) +
                    "</html>"
                );
            }
        }
    }//GEN-LAST:event_tableMouseClicked

    private void DuplicatePopupItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DuplicatePopupItemActionPerformed
        backupManager.popupItemDuplicateBackup(selectedRow, backupTable);
    }//GEN-LAST:event_DuplicatePopupItemActionPerformed

    private void RunBackupPopupItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RunBackupPopupItemActionPerformed
        backupManager.popupItemRunBackup(selectedRow, backupTable, backups, interruptBackupPopupItem, RunBackupPopupItem);
    }//GEN-LAST:event_RunBackupPopupItemActionPerformed

    private void CopyBackupNamePopupItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CopyBackupNamePopupItemActionPerformed
        backupManager.popupItemCopyBackupName(selectedRow, backupTable, backups);
    }//GEN-LAST:event_CopyBackupNamePopupItemActionPerformed

    private void CopyInitialPathPopupItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CopyInitialPathPopupItemActionPerformed
        backupManager.popupItemCopyInitialPath(selectedRow, backupTable, backups);
    }//GEN-LAST:event_CopyInitialPathPopupItemActionPerformed

    private void CopyDestinationPathPopupItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CopyDestinationPathPopupItemActionPerformed
        backupManager.popupItemCopyDestinationPath(selectedRow, backupTable, backups);
    }//GEN-LAST:event_CopyDestinationPathPopupItemActionPerformed

    private void AutoBackupMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AutoBackupMenuItemActionPerformed
        backupManager.popupItemAutoBackup(selectedRow, backupTable, backups, AutoBackupMenuItem);
    }//GEN-LAST:event_AutoBackupMenuItemActionPerformed

    private void OpenInitialFolderItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_OpenInitialFolderItemActionPerformed
        backupManager.popupItemOpenInitialPath(selectedRow, backupTable, backups);
    }//GEN-LAST:event_OpenInitialFolderItemActionPerformed

    private void OpenInitialDestinationItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_OpenInitialDestinationItemActionPerformed
        backupManager.popupItemOpenDestinationPath(selectedRow, backupTable, backups);
    }//GEN-LAST:event_OpenInitialDestinationItemActionPerformed

    private void renamePopupItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_renamePopupItemActionPerformed
        backupManager.popupItemRenameBackup(selectedRow, backupTable, backups);
    }//GEN-LAST:event_renamePopupItemActionPerformed

    private void MenuPaypalDonateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MenuPaypalDonateActionPerformed
        backupManager.menuItemDonateViaPaypal();
    }//GEN-LAST:event_MenuPaypalDonateActionPerformed

    private void MenuBugReportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MenuBugReportActionPerformed
        backupManager.menuItemBugReport();
    }//GEN-LAST:event_MenuBugReportActionPerformed

    private void MenuShareActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MenuShareActionPerformed
        backupManager.menuItemShare();
    }//GEN-LAST:event_MenuShareActionPerformed

    private void MenuWebsiteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MenuWebsiteActionPerformed
        backupManager.menuItemWebsite();
    }//GEN-LAST:event_MenuWebsiteActionPerformed

    private void MenuSupportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MenuSupportActionPerformed
        backupManager.menuItemSupport();
    }//GEN-LAST:event_MenuSupportActionPerformed

    private void MenuInfoPageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MenuInfoPageActionPerformed
        backupManager.menuItemInfoPage();
    }//GEN-LAST:event_MenuInfoPageActionPerformed

    private void MenuPreferencesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MenuPreferencesActionPerformed
        backupManager.menuItemOpenPreferences();
    }//GEN-LAST:event_MenuPreferencesActionPerformed

    private void interruptBackupPopupItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_interruptBackupPopupItemActionPerformed
        backupManager.popupItemInterrupt(selectedRow, backupTable, backups, interruptBackupPopupItem, RunBackupPopupItem);
    }//GEN-LAST:event_interruptBackupPopupItemActionPerformed

    private void exportAsCsvBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportAsCsvBtnActionPerformed
        ExportManager.exportAsCSV(new ArrayList<>(backups), backupmanager.Entities.Backup.getCSVHeader());
    }//GEN-LAST:event_exportAsCsvBtnActionPerformed

    private void exportAsPdfBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportAsPdfBtnActionPerformed
        ExportManager.exportAsPDF(new ArrayList<>(backups), backupmanager.Entities.Backup.getCSVHeader());
    }//GEN-LAST:event_exportAsPdfBtnActionPerformed

    private void addBackupEntryButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addBackupEntryButtonActionPerformed
        backupManager.newBackup(progressBar);
    }//GEN-LAST:event_addBackupEntryButtonActionPerformed

    private void MenuBuyMeACoffeDonateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MenuBuyMeACoffeDonateActionPerformed
        backupManager.menuItemDonateViaBuymeacoffe();
    }//GEN-LAST:event_MenuBuyMeACoffeDonateActionPerformed

    private void menActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menActionPerformed
        sp.toggleMenu();
    }//GEN-LAST:event_menActionPerformed

    private void pageSettingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pageSettingsActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_pageSettingsActionPerformed

    private void pageDashboardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pageDashboardActionPerformed
        CardLayout cl = (CardLayout) layeredCardPanel.getLayout();
        cl.show(layeredCardPanel, "Dashboard");

        Charts.createChart(chart1);
        Charts.createChart(chart2);
    }//GEN-LAST:event_pageDashboardActionPerformed

    private void pageBackupListActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pageBackupListActionPerformed
        CardLayout cl = (CardLayout) layeredCardPanel.getLayout();
        cl.show(layeredCardPanel, "BackupList");
    }//GEN-LAST:event_pageBackupListActionPerformed

    private void setTranslations() {
        // update table translations
        if (backups != null)
            displayBackupList();

        // general
        jLabel3.setText(TranslationCategory.GENERAL.getTranslation(TranslationKey.VERSION) + " " + currentVersion);

        // menu
        jMenu1.setText(TranslationCategory.MENU.getTranslation(TranslationKey.FILE));
        jMenu2.setText(TranslationCategory.MENU.getTranslation(TranslationKey.OPTIONS));
        jMenu3.setText(TranslationCategory.MENU.getTranslation(TranslationKey.ABOUT));
        jMenu5.setText(TranslationCategory.MENU.getTranslation(TranslationKey.HELP));

        // menu items
        MenuBugReport.setText(TranslationCategory.MENU.getTranslation(TranslationKey.BUG_REPORT));
        MenuClear.setText(TranslationCategory.MENU.getTranslation(TranslationKey.CLEAR));
        MenuDonate.setText(TranslationCategory.MENU.getTranslation(TranslationKey.DONATE));
        MenuHistory.setText(TranslationCategory.MENU.getTranslation(TranslationKey.HISTORY));
        MenuInfoPage.setText(TranslationCategory.MENU.getTranslation(TranslationKey.INFO_PAGE));
        MenuNew.setText(TranslationCategory.MENU.getTranslation(TranslationKey.NEW));
        MenuQuit.setText(TranslationCategory.MENU.getTranslation(TranslationKey.QUIT));
        MenuSave.setText(TranslationCategory.MENU.getTranslation(TranslationKey.SAVE));
        MenuSaveWithName.setText(TranslationCategory.MENU.getTranslation(TranslationKey.SAVE_WITH_NAME));
        MenuPreferences.setText(TranslationCategory.MENU.getTranslation(TranslationKey.PREFERENCES));
        MenuImport.setText(TranslationCategory.MENU.getTranslation(TranslationKey.IMPORT));
        MenuExport.setText(TranslationCategory.MENU.getTranslation(TranslationKey.EXPORT));
        MenuShare.setText(TranslationCategory.MENU.getTranslation(TranslationKey.SHARE));
        MenuSupport.setText(TranslationCategory.MENU.getTranslation(TranslationKey.SUPPORT));
        MenuWebsite.setText(TranslationCategory.MENU.getTranslation(TranslationKey.WEBSITE));

        // backup list
        ExportLabel.setText(TranslationCategory.BACKUP_LIST.getTranslation(TranslationKey.EXPORT_AS));
        addBackupEntryButton.setToolTipText(TranslationCategory.BACKUP_LIST.getTranslation(TranslationKey.ADD_BACKUP_TOOLTIP));
        exportAsPdfBtn.setToolTipText(TranslationCategory.BACKUP_LIST.getTranslation(TranslationKey.EXPORT_AS_PDF_TOOLTIP));
        exportAsCsvBtn.setToolTipText(TranslationCategory.BACKUP_LIST.getTranslation(TranslationKey.EXPORT_AS_CSV_TOOLTIP));
        researchField.setToolTipText(TranslationCategory.BACKUP_LIST.getTranslation(TranslationKey.RESEARCH_BAR_TOOLTIP));
        researchField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, TranslationCategory.BACKUP_LIST.getTranslation(TranslationKey.RESEARCH_BAR_PLACEHOLDER));

        // popup
        CopyBackupNamePopupItem.setText(TranslationCategory.BACKUP_LIST.getTranslation(TranslationKey.COPY_BACKUP_NAME_POPUP));
        CopyDestinationPathPopupItem.setText(TranslationCategory.BACKUP_LIST.getTranslation(TranslationKey.COPY_DESTINATION_PATH_BACKUP));
        RunBackupPopupItem.setText(TranslationCategory.BACKUP_LIST.getTranslation(TranslationKey.SINGLE_BACKUP_POPUP));
        CopyInitialPathPopupItem.setText(TranslationCategory.BACKUP_LIST.getTranslation(TranslationKey.COPY_INITIAL_PATH_POPUP));
        DeletePopupItem.setText(TranslationCategory.BACKUP_LIST.getTranslation(TranslationKey.DELETE_POPUP));
        interruptBackupPopupItem.setText(TranslationCategory.BACKUP_LIST.getTranslation(TranslationKey.INTERRUPT_POPUP));
        DuplicatePopupItem.setText(TranslationCategory.BACKUP_LIST.getTranslation(TranslationKey.DUPLICATE_POPUP));
        EditPoputItem.setText(TranslationCategory.BACKUP_LIST.getTranslation(TranslationKey.EDIT_POPUP));
        OpenInitialDestinationItem.setText(TranslationCategory.BACKUP_LIST.getTranslation(TranslationKey.OPEN_DESTINATION_FOLDER_POPUP));
        OpenInitialFolderItem.setText(TranslationCategory.BACKUP_LIST.getTranslation(TranslationKey.OPEN_INITIAL_FOLDER_POPUP));
        renamePopupItem.setText(TranslationCategory.BACKUP_LIST.getTranslation(TranslationKey.RENAME_BACKUP_POPUP));
        jMenu4.setText(TranslationCategory.BACKUP_LIST.getTranslation(TranslationKey.COPY_TEXT_POPUP));
        AutoBackupMenuItem.setText(TranslationCategory.BACKUP_LIST.getTranslation(TranslationKey.AUTO_BACKUP_POPUP));
        Backup.setText(TranslationCategory.BACKUP_LIST.getTranslation(TranslationKey.BACKUP_POPUP));
    }

    private String[] getColumnTranslations() {
        String[] columnNames = {
            TranslationCategory.BACKUP_LIST.getTranslation(TranslationKey.BACKUP_NAME_COLUMN),
            TranslationCategory.BACKUP_LIST.getTranslation(TranslationKey.INITIAL_PATH_COLUMN),
            TranslationCategory.BACKUP_LIST.getTranslation(TranslationKey.DESTINATION_PATH_COLUMN),
            TranslationCategory.BACKUP_LIST.getTranslation(TranslationKey.LAST_BACKUP_COLUMN),
            TranslationCategory.BACKUP_LIST.getTranslation(TranslationKey.AUTOMATIC_BACKUP_COLUMN),
            TranslationCategory.BACKUP_LIST.getTranslation(TranslationKey.NEXT_BACKUP_DATE_COLUMN),
            TranslationCategory.BACKUP_LIST.getTranslation(TranslationKey.TIME_INTERVAL_COLUMN)
        };
        return columnNames;
    }

    private void initializeTable() {
        backups = BackupConfigurationRepository.getBackupList();
        displayBackupList();
    }

    private void setSvgImages() {
        men.setSvgImage("res/img/hamburger.svg", 30, 30);
        pageBackupList.setSvgImage("res/img/list.svg", 30, 30);
        pageDashboard.setSvgImage("res/img/dashboard.svg", 30, 30);
        pageSettings.setSvgImage("res/img/settings2.svg", 30, 30);
        exportAsCsvBtn.setSvgImage("res/img/csv.svg", 30, 30);
        exportAsPdfBtn.setSvgImage("res/img/pdf.svg", 30, 30);
        addBackupEntryButton.setSvgImage("res/img/add.svg", 30, 30);
        MenuSave.setSvgImage("res/img/save.svg", 16, 16);
        MenuSaveWithName.setSvgImage("res/img/save_as.svg", 16, 16);
        MenuImport.setSvgImage("res/img/import.svg", 16, 16);
        MenuExport.setSvgImage("res/img/export.svg", 16, 16);
        MenuNew.setSvgImage("res/img/new_file.svg", 16, 16);
        MenuBugReport.setSvgImage("res/img/bug.svg", 16, 16);
        MenuClear.setSvgImage("res/img/clear.svg", 16, 16);
        MenuHistory.setSvgImage("res/img/history.svg", 16, 16);
        MenuDonate.setSvgImage("res/img/donate.svg", 16, 16);
        MenuPaypalDonate.setSvgImage("res/img/paypal.svg", 16, 16);
        MenuBuyMeACoffeDonate.setSvgImage("res/img/buymeacoffee.svg", 16, 16);
        MenuPreferences.setSvgImage("res/img/settings.svg", 16, 16);
        MenuShare.setSvgImage("res/img/share.svg", 16, 16);
        MenuSupport.setSvgImage("res/img/support.svg", 16, 16);
        MenuWebsite.setSvgImage("res/img/website.svg", 16, 16);
        MenuQuit.setSvgImage("res/img/quit.svg", 16, 16);
        MenuInfoPage.setSvgImage("res/img/info.svg", 16, 16);
    }

    private void initializeMenuItems() {
        JSONConfigReader config = new JSONConfigReader(ConfigKey.CONFIG_FILE_STRING.getValue(), ConfigKey.CONFIG_DIRECTORY_STRING.getValue());
        MenuBugReport.setVisible(config.isMenuItemEnabled(MenuItems.BugReport.name()));
        MenuPreferences.setVisible(config.isMenuItemEnabled(MenuItems.Preferences.name()));
        MenuClear.setVisible(config.isMenuItemEnabled(MenuItems.Clear.name()));
        MenuDonate.setVisible(config.isMenuItemEnabled(MenuItems.Donate.name()));
        MenuPaypalDonate.setVisible(config.isMenuItemEnabled(MenuItems.PaypalDonate.name()));
        MenuBuyMeACoffeDonate.setVisible(config.isMenuItemEnabled(MenuItems.BuymeacoffeeDonate.name()));
        MenuHistory.setVisible(config.isMenuItemEnabled(MenuItems.History.name()));
        MenuInfoPage.setVisible(config.isMenuItemEnabled(MenuItems.InfoPage.name()));
        MenuNew.setVisible(config.isMenuItemEnabled(MenuItems.New.name()));
        MenuQuit.setVisible(config.isMenuItemEnabled(MenuItems.Quit.name()));
        MenuSave.setVisible(config.isMenuItemEnabled(MenuItems.Save.name()));
        MenuSaveWithName.setVisible(config.isMenuItemEnabled(MenuItems.SaveWithName.name()));
        MenuShare.setVisible(config.isMenuItemEnabled(MenuItems.Share.name()));
        MenuSupport.setVisible(config.isMenuItemEnabled(MenuItems.Support.name()));
        MenuWebsite.setVisible(config.isMenuItemEnabled(MenuItems.Website.name()));
        MenuImport.setVisible(config.isMenuItemEnabled(MenuItems.Import.name()));
        MenuExport.setVisible(config.isMenuItemEnabled(MenuItems.Export.name()));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBoxMenuItem AutoBackupMenuItem;
    private javax.swing.JMenu Backup;
    private javax.swing.JMenuItem CopyBackupNamePopupItem;
    private javax.swing.JMenuItem CopyDestinationPathPopupItem;
    private javax.swing.JMenuItem CopyInitialPathPopupItem;
    private javax.swing.JMenuItem DeletePopupItem;
    private javax.swing.JMenuItem DuplicatePopupItem;
    private javax.swing.JMenuItem EditPoputItem;
    private javax.swing.JLabel ExportLabel;
    private backupmanager.svg.SVGMenuItem MenuBugReport;
    private backupmanager.svg.SVGMenuItem MenuBuyMeACoffeDonate;
    private backupmanager.svg.SVGMenuItem MenuClear;
    private backupmanager.svg.SVGMenu MenuDonate;
    private backupmanager.svg.SVGMenuItem MenuExport;
    private backupmanager.svg.SVGMenuItem MenuHistory;
    private backupmanager.svg.SVGMenuItem MenuImport;
    private backupmanager.svg.SVGMenuItem MenuInfoPage;
    private backupmanager.svg.SVGMenuItem MenuNew;
    private backupmanager.svg.SVGMenuItem MenuPaypalDonate;
    private backupmanager.svg.SVGMenuItem MenuPreferences;
    private backupmanager.svg.SVGMenuItem MenuQuit;
    private backupmanager.svg.SVGMenuItem MenuSave;
    private backupmanager.svg.SVGMenuItem MenuSaveWithName;
    private backupmanager.svg.SVGMenuItem MenuShare;
    private backupmanager.svg.SVGMenuItem MenuSupport;
    private backupmanager.svg.SVGMenuItem MenuWebsite;
    private javax.swing.JMenuItem OpenInitialDestinationItem;
    private javax.swing.JMenuItem OpenInitialFolderItem;
    private javax.swing.JMenuItem RunBackupPopupItem;
    private javax.swing.JPopupMenu TablePopup;
    private backupmanager.svg.SVGButton addBackupEntryButton;
    private javax.swing.JPanel chart1;
    private javax.swing.JLabel chart1TitleLabel;
    private javax.swing.JPanel chart2;
    private javax.swing.JLabel chart2TitleLabel;
    private javax.swing.JLabel copyrightLabelSide;
    private javax.swing.JLabel detailsLabel;
    private backupmanager.svg.SVGButton exportAsCsvBtn;
    private backupmanager.svg.SVGButton exportAsPdfBtn;
    private javax.swing.JMenuItem interruptBackupPopupItem;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenu jMenu4;
    private javax.swing.JMenu jMenu5;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JPopupMenu.Separator jSeparator4;
    private javax.swing.JPopupMenu.Separator jSeparator5;
    private javax.swing.JLayeredPane layeredCardPanel;
    private backupmanager.svg.SVGButton men;
    private backupmanager.svg.SVGButton pageBackupList;
    private backupmanager.svg.SVGButton pageDashboard;
    private backupmanager.svg.SVGButton pageSettings;
    private javax.swing.JPanel panelBackupList;
    private javax.swing.JPanel panelDashboard;
    private javax.swing.JPanel panelVersion;
    private javax.swing.JMenuItem renamePopupItem;
    private javax.swing.JTextField researchField;
    private javax.swing.JPanel sidebar;
    private javax.swing.JTable table;
    private javax.swing.JLabel totalBackupConfigurationsNumber;
    private javax.swing.JPanel totalBackupConfigurationsPanel;
    private javax.swing.JLabel totalBackupConfigurationsTitleLabel;
    private javax.swing.JLabel totalBackupsNumber;
    private javax.swing.JPanel totalBackupsPanel;
    private javax.swing.JLabel totalBackupsTitleLabel;
    private javax.swing.JLabel totalSpaceNumber;
    private javax.swing.JLabel totalSpaceTitleLabel;
    private javax.swing.JPanel totalSpaceUsedPanel;
    // End of variables declaration//GEN-END:variables
}