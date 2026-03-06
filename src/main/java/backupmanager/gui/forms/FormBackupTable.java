package backupmanager.gui.forms;

import java.awt.Component;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import backupmanager.Entities.ConfigurationBackup;
import backupmanager.Entities.TimeInterval;
import backupmanager.Enums.Translations.TCategory;
import backupmanager.Enums.Translations.TKey;
import backupmanager.Helpers.BackupHelper;
import static backupmanager.Helpers.BackupHelper.formatter;
import backupmanager.Services.BackupService;
import backupmanager.gui.frames.Controllers.BackupManagerController;
import backupmanager.gui.frames.Controllers.BackupPopupController;
import backupmanager.gui.sample.csv.ConfigurationBackupDataTable;
import backupmanager.gui.svg.SVGButton;
import backupmanager.utils.SystemForm;
import backupmanager.utils.table.TableHeaderAlignment;
import net.miginfocom.swing.MigLayout;
import raven.swingpack.JPagination;

@SystemForm(name = "Table", description = "table is a user interface component", tags = {"list"})
public class FormBackupTable extends CustomForm {

    private static final Logger logger = LoggerFactory.getLogger(FormBackupTable.class);

    private final BackupManagerController managerController;
    private final int COL_LAST_RUN = 3;
    private final int COL_AUTOMATIC = 4;
    private final int COL_NEXT_RUN = 5;

    private final BackupService backupService;
    private List<ConfigurationBackup> backups;

    public FormBackupTable() {
        backupService = new BackupService();
        managerController = new BackupManagerController(backupService);

        build();
    }

    @Override
    protected void init() {
        setLayout(new MigLayout(
        "fill,wrap",
        "[fill]",
        "[][grow 100,fill][grow 0]"
        ));
        add(createInfo("Title", "Description", 1));
        add(createBorder(createBasicTable()), "gapx 7 7, grow");
        add(createBorder(createDetails()), "gapx 7 7, hmin 150");
    }

    @Override
    public void formInit() {
        DefaultTableModel model = (DefaultTableModel) backupTable.getModel();
        model.setColumnIdentifiers(ConfigurationBackup.getCSVHeaderArray());

        backupTable.createDefaultColumnsFromModel();

        formRefresh();
    }

    @Override
    public void formRefresh() {
        backups = backupService.getAllBackups();
        loadData();
    }

    @Override
    protected void loadData() {
        loadTable(pagination.getSelectedPage());
    }

    private void loadTable(int page) {
        if (backups != null) {
            ConfigurationBackupDataTable res = ConfigurationBackupDataTable.create(backups, page, limit);
            lbTotalPage.setText(DecimalFormat.getInstance().format(res.getTotal()));
            pagination.getModel().setPageRange(res.getPage(), res.getPageSize());

            DefaultTableModel model = (DefaultTableModel) backupTable.getModel();
            model.setRowCount(0);
            for (ConfigurationBackup backup : res.getData()) {
                model.addRow(backup.toTableRow());
            }
        }
    }

    private Component createBorder(Component component) {
        JPanel panel = new JPanel(new MigLayout("fill,insets 7 0 7 0", "[fill]", "[fill]"));
        panel.add(component);
        return panel;
    }

    private Component createBasicTable() {
        JPanel panelTable = new JPanel(new MigLayout(
            "fill,wrap,insets 10 0 10 0",
            "[fill]",
            "[][grow,fill][]"
        ));

        // create table model
        JTable table = new JTable();
        table.setModel(new DefaultTableModel() {

            @Override
            public Class<?> getColumnClass(int columnIndex) {

                if (columnIndex == COL_AUTOMATIC) {
                    return Boolean.class;
                }

                if (columnIndex == COL_LAST_RUN || columnIndex == COL_NEXT_RUN) {
                    return LocalDateTime.class;
                }

                if (columnIndex == 6) {
                    return TimeInterval.class;
                }

                if (columnIndex == 7) {
                    return Integer.class;
                }

                return String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });

        table.putClientProperty(FlatClientProperties.STYLE, "rowHeight:34; showHorizontalLines:false;");

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = table.getSelectedRow();
                if (row != -1) {
                    String details = backupService.buildDetails(backups.get(row));
                    txtDetails.setText(details);
                }
            }
        });

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() != -1) {
                    int row = table.getSelectedRow();
                    logger.debug("Double clicked row: " + row);
                    showEditModal(backups.get(row));
                }
            }
        });

        table.getInputMap(JComponent.WHEN_FOCUSED).put(
                KeyStroke.getKeyStroke("DELETE"),
                "deleteRow");

        table.getActionMap().put("deleteRow", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                int row = table.getSelectedRow();
                if (row != -1) {
                    logger.debug("Deleted row: " + row);
                    showDeleteConfirmation();
                }
            }
        });

        // table scroll
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        // alignment table header
        table.getTableHeader().setDefaultRenderer(
                new TableHeaderAlignment(table) {

            @Override
            protected int getAlignment(int column) {
                return SwingConstants.LEFT;
            }
        });

        // style
        panelTable.putClientProperty(FlatClientProperties.STYLE, "" +
                "arc:10;" +
                "background:$Table.background;");
        table.getTableHeader().putClientProperty(FlatClientProperties.STYLE, "" +
                "height:30;" +
                "hoverBackground:null;" +
                "pressedBackground:null;" +
                "separatorColor:$TableHeader.background;");
        table.putClientProperty(FlatClientProperties.STYLE, "" +
                "rowHeight:30;" +
                "showHorizontalLines:true;" +
                "intercellSpacing:0,1;" +
                "cellFocusColor:$TableHeader.hoverBackground;" +
                "selectionBackground:$TableHeader.hoverBackground;" +
                "selectionInactiveBackground:$TableHeader.hoverBackground;" +
                "selectionForeground:$Table.foreground;");
        scrollPane.getVerticalScrollBar().putClientProperty(FlatClientProperties.STYLE, "" +
                "trackArc:$ScrollBar.thumbArc;" +
                "trackInsets:3,3,3,3;" +
                "thumbInsets:3,3,3,3;" +
                "background:$Table.background;");

        // create title
        panelTable.add(createHeaderAction());
        panelTable.add(scrollPane, "grow, pushy");

        // create pagination
        pagination = new JPagination(11, 1, 1);
        pagination.setMinimumSize(pagination.getPreferredSize());
        pagination.addChangeListener(e -> loadData());
        JPanel panelPage = new JPanel(new MigLayout("insets 5 15 5 15", "[][]push[pref!]"));
        lbTotalPage = new JLabel("0");
        pagination.putClientProperty(FlatClientProperties.STYLE, "" +
                "background:null;");
        panelPage.putClientProperty(FlatClientProperties.STYLE, "" +
                "background:null;");
        panelPage.add(new JLabel("Total:"));
        panelPage.add(lbTotalPage);
        panelPage.add(pagination);

        panelTable.add(panelPage, "growx");

        backupTable = table;

        setRenderer();

        buildTablePopupMenu();

        return panelTable;
    }

    private void setRenderer() {
        DefaultTableCellRenderer dateRenderer = new DefaultTableCellRenderer() {
            @Override
            protected void setValue(Object value) {
                if (value instanceof LocalDateTime date) {
                    setText(date.format(formatter));
                } else {
                    super.setValue(value);
                }
            }
        };

        backupTable.setDefaultRenderer(LocalDateTime.class, dateRenderer);

        DefaultTableCellRenderer baseRenderer = new DefaultTableCellRenderer() {

            @Override
            public void setHorizontalAlignment(int alignment) {
                super.setHorizontalAlignment(SwingConstants.LEFT);
            }

            @Override
            protected void setValue(Object value) {

                if (value instanceof Boolean bool) {
                    setHorizontalAlignment(SwingConstants.CENTER);
                    setText(bool ? "✓" : "");
                    return;
                }

                if (value instanceof Integer || value instanceof Long) {
                    setHorizontalAlignment(SwingConstants.LEFT);
                    setText(String.valueOf(value));
                    return;
                }

                if (value instanceof LocalDateTime date) {
                    setHorizontalAlignment(SwingConstants.LEFT);
                    setText(date.format(formatter));
                    return;
                }

                super.setValue(value);
            }
        };

        backupTable.setDefaultRenderer(Object.class, baseRenderer);
    }

    private void buildTablePopupMenu() {
        JPopupMenu popupMenu = new JPopupMenu();

        itemEdit = new JMenuItem("Edit");
        itemDelete = new JMenuItem("Delete");
        itemDuplicate = new JMenuItem("Duplicate");
        itemRename = new JMenuItem("Rename");
        itemOpenTargetPath = new JMenuItem("Open target path");
        itemOpenDestinationPath = new JMenuItem("Open destination path");

        itemBackup = new JMenu("Backup");
        itemRunSingleBackup = new JMenuItem("Run single backup");
        itemAutoBackup = new JCheckBoxMenuItem("Auto backup");
        itemInterruptBackup = new JMenuItem("Interrupt backup process");

        itemCopyText = new JMenu("Copy text");
        itemCopyBackupName = new JMenuItem("Copy backup name");
        itemCopyTargetPath = new JMenuItem("Copy target path");
        itemCopyDestinationPath = new JMenuItem("Copy destination path");

        popupMenu.add(itemEdit);
        popupMenu.add(itemDelete);
        popupMenu.add(itemDuplicate);
        popupMenu.add(itemRename);
        popupMenu.addSeparator();
        popupMenu.add(itemOpenTargetPath);
        popupMenu.add(itemOpenDestinationPath);
        popupMenu.addSeparator();
        popupMenu.add(itemBackup);
        itemBackup.add(itemRunSingleBackup);
        itemBackup.add(itemAutoBackup);
        itemBackup.add(itemInterruptBackup);
        popupMenu.addSeparator();
        popupMenu.add(itemCopyText);
        itemCopyText.add(itemCopyBackupName);
        itemCopyText.add(itemCopyTargetPath);
        itemCopyText.add(itemCopyDestinationPath);

        itemEdit.addActionListener(e -> handleAction("EDIT", itemInterruptBackup, itemRunSingleBackup));
        itemDelete.addActionListener(e -> handleAction("DELETE", itemInterruptBackup, itemRunSingleBackup));
        itemDuplicate.addActionListener(e -> handleAction("DUPLICATE", itemInterruptBackup, itemRunSingleBackup));
        itemRename.addActionListener(e -> handleAction("RENAME", itemInterruptBackup, itemRunSingleBackup));
        itemOpenTargetPath.addActionListener(e -> handleAction("OPEN_TARGET", itemInterruptBackup, itemRunSingleBackup));
        itemOpenDestinationPath.addActionListener(e -> handleAction("OPEN_DEST", itemInterruptBackup, itemRunSingleBackup));
        itemRunSingleBackup.addActionListener(e -> handleAction("RUN_SINGLE", itemInterruptBackup, itemRunSingleBackup));
        itemAutoBackup.addActionListener(e -> handleToggle());
        itemInterruptBackup.addActionListener(e -> handleAction("INTERRUPT_BACKUP", itemInterruptBackup, itemRunSingleBackup));
        itemCopyBackupName.addActionListener(e -> handleAction("COPY_NAME", itemInterruptBackup, itemRunSingleBackup));
        itemCopyTargetPath.addActionListener(e -> handleAction("COPY_TARGET", itemInterruptBackup, itemRunSingleBackup));
        itemCopyDestinationPath.addActionListener(e -> handleAction("COPY_DEST", itemInterruptBackup, itemRunSingleBackup));

        itemInterruptBackup.setEnabled(false); // Disable interrupt option by default

        backupTable.addMouseListener(new java.awt.event.MouseAdapter() {
        @Override
        public void mousePressed(java.awt.event.MouseEvent e) {
            if (e.isPopupTrigger()) {
                showPopup(e);
            }
        }

        @Override
        public void mouseReleased(java.awt.event.MouseEvent e) {
            if (e.isPopupTrigger()) {
                showPopup(e);
            }
        }

        private void showPopup(java.awt.event.MouseEvent e) {
            int row = backupTable.rowAtPoint(e.getPoint());
            if (row >= 0) {
                backupTable.setRowSelectionInterval(row, row);
            }
            popupMenu.show(e.getComponent(), e.getX(), e.getY());
            ConfigurationBackup backup = getBackupFromTableRow(row);
            itemAutoBackup.setSelected(backup.isAutomatic());
        }
        });
    }

    private void handleAction(String action, JMenuItem interruptBackupPopupItem, JMenuItem RunBackupPopupItem) {
        int selectedRow = backupTable.getSelectedRow();
        if (selectedRow < 0)
            return;

        ConfigurationBackup backup = getBackupFromTableRow(selectedRow);
        switch (action) {
            case "EDIT" -> BackupPopupController.popupItemEditBackupName(backup);
            case "DELETE" -> BackupHelper.deleteBackup(backup);
            case "DUPLICATE" -> BackupPopupController.popupItemDuplicateBackup(backup);
            case "RENAME" -> BackupPopupController.popupItemRenameBackup(backups, backup);
            case "OPEN_TARGET" -> BackupPopupController.popupItemCopyInitialPath(backup);
            case "OPEN_DEST" -> BackupPopupController.popupItemOpenDestinationPath(backup);
            case "RUN_SINGLE" -> BackupPopupController.popupItemRunBackup(backup, backupTable, interruptBackupPopupItem, RunBackupPopupItem);
            case "COPY_NAME" -> BackupPopupController.popupItemCopyBackupName(backup);
            case "COPY_TARGET" -> BackupPopupController.popupItemCopyInitialPath(backup);
            case "COPY_DEST" -> BackupPopupController.popupItemCopyDestinationPath(backup);
        }

        formRefresh();
    }

    private void handleToggle() {
        int selectedRow = backupTable.getSelectedRow();
        if (selectedRow < 0)
            return;

        ConfigurationBackup backup = getBackupFromTableRow(selectedRow);
        BackupPopupController.popupItemAutoBackup(backup);

        formRefresh();
    }

    private ConfigurationBackup getBackupFromTableRow(int row) {
        row = backupTable.convertRowIndexToModel(row);
        return backups.get(row);
    }

    private Component createDetails() {
        JPanel detailsPanel = new JPanel(
            new MigLayout("fill,insets 5 0 5 0", "[fill]", "[grow]")
        );

        txtDetails = new JTextPane();
        txtDetails.setEditable(false);
        txtDetails.setContentType("text/html");

        JScrollPane detailScroll = new JScrollPane(txtDetails);
        detailScroll.putClientProperty(FlatClientProperties.STYLE,
                "arc:10; border:1,1,1,1,$Component.borderColor");

        detailsPanel.add(detailScroll, "grow");
        return detailsPanel;
    }

    private Component createHeaderAction() {
        JPanel panel = new JPanel(new MigLayout("insets 5 20 5 20", "[fill,300]push[][]"));

        txtSearch = new JTextField();
        txtSearch.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Search...");
        txtSearch.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, new FlatSVGIcon("icons/search.svg", 0.4f));
        cmdCreate = new SVGButton("Create");
        cmdEdit = new SVGButton("Edit");
        cmdDelete = new SVGButton("Delete");

        cmdCreate.setSvgImage("icons/add.svg", 16, 16);
        cmdEdit.setSvgImage("icons/edit.svg", 16, 16);
        cmdDelete.setSvgImage("icons/delete.svg", 16, 16);

        cmdCreate.putClientProperty(FlatClientProperties.STYLE, "background:$Component.accentColor;");
        cmdDelete.putClientProperty(FlatClientProperties.STYLE, "background:$Component.error.background;");

        cmdCreate.addActionListener(e -> showCreateModal());
        cmdEdit.addActionListener(e -> showEditModal());
        cmdDelete.addActionListener(e -> showDeleteConfirmation());
        panel.add(txtSearch);
        panel.add(cmdCreate);
        panel.add(cmdEdit);
        panel.add(cmdDelete);

        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            private void update() {
                if (backups == null)
                    return;

                String research = txtSearch.getText();
                backups = backupService.getAllBackups();
                backups = managerController.researchInTableAndGet(backups, research);
                loadData();
            }

            @Override
            public void insertUpdate(DocumentEvent e) { update(); }
            @Override
            public void removeUpdate(DocumentEvent e) { update(); }
            @Override
            public void changedUpdate(DocumentEvent e) { update(); }
        });

        panel.putClientProperty(FlatClientProperties.STYLE, "" +
                "background:null;");
        return panel;
    }

    public void showCreateModal() {
        managerController.showCreateModal(this);
        loadData();
    }

    private void showEditModal(ConfigurationBackup backup) {
        managerController.showEditModal(this, backup);
        loadData();
    }

    private void showEditModal() {
        int selectedRow = backupTable.getSelectedRow();
        if (selectedRow < 0)
            return;

        ConfigurationBackup backup = getBackupFromTableRow(selectedRow);
        managerController.showEditModal(this, backup);
        loadData();
    }

    private void showDeleteConfirmation() {
        int selectedRow = backupTable.getSelectedRow();
        if (selectedRow < 0)
            return;

        ConfigurationBackup backup = getBackupFromTableRow(selectedRow);
        BackupHelper.deleteBackupWithConfirmition(backup);
        formRefresh();
    }

    @Override
    protected void setTranslations() {
        editTitle(TCategory.BACKUP_LIST.getTranslation(TKey.BACKUP_LIST_TITLE));
        editDescription(TCategory.BACKUP_LIST.getTranslation(TKey.BACKUP_LIST_DESCRIPTION));

        txtSearch.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, TCategory.BACKUP_LIST.getTranslation(TKey.RESEARCH_BAR_PLACEHOLDER));
        txtSearch.setToolTipText(TCategory.BACKUP_LIST.getTranslation(TKey.RESEARCH_BAR_TOOLTIP));

        cmdCreate.setText(TCategory.GENERAL.getTranslation(TKey.CREATE_BUTTON));
        cmdEdit.setText(TCategory.GENERAL.getTranslation(TKey.EDIT_BUTTON));
        cmdDelete.setText(TCategory.GENERAL.getTranslation(TKey.DELETE_BUTTON));

        itemEdit.setText(TCategory.BACKUP_LIST.getTranslation(TKey.EDIT_POPUP));
        itemDelete.setText(TCategory.BACKUP_LIST.getTranslation(TKey.DELETE_POPUP));
        itemDuplicate.setText(TCategory.BACKUP_LIST.getTranslation(TKey.DUPLICATE_POPUP));
        itemRename.setText(TCategory.BACKUP_LIST.getTranslation(TKey.RENAME_BACKUP_POPUP));
        itemOpenTargetPath.setText(TCategory.BACKUP_LIST.getTranslation(TKey.OPEN_INITIAL_FOLDER_POPUP));
        itemOpenDestinationPath.setText(TCategory.BACKUP_LIST.getTranslation(TKey.OPEN_DESTINATION_FOLDER_POPUP));
        itemBackup.setText(TCategory.BACKUP_LIST.getTranslation(TKey.BACKUP_POPUP));
        itemRunSingleBackup.setText(TCategory.BACKUP_LIST.getTranslation(TKey.SINGLE_BACKUP_POPUP));
        itemAutoBackup.setText(TCategory.BACKUP_LIST.getTranslation(TKey.AUTO_BACKUP_POPUP));
        itemInterruptBackup.setText(TCategory.BACKUP_LIST.getTranslation(TKey.INTERRUPT_POPUP));
        itemCopyText.setText(TCategory.BACKUP_LIST.getTranslation(TKey.COPY_TEXT_POPUP));
        itemCopyBackupName.setText(TCategory.BACKUP_LIST.getTranslation(TKey.COPY_BACKUP_NAME_POPUP));
        itemCopyTargetPath.setText(TCategory.BACKUP_LIST.getTranslation(TKey.COPY_INITIAL_PATH_POPUP));
        itemCopyDestinationPath.setText(TCategory.BACKUP_LIST.getTranslation(TKey.COPY_DESTINATION_PATH_BACKUP));
    }

    private final int limit = 50;
    private JPagination pagination;
    private JTable backupTable;
    private JLabel lbTotalPage;
    private JTextPane txtDetails;
    private JTextField txtSearch;
    private SVGButton cmdCreate;
    private SVGButton cmdEdit;
    private SVGButton cmdDelete;

    private JMenuItem itemEdit;
    private JMenuItem itemDelete;
    private JMenuItem itemDuplicate;
    private JMenuItem itemRename;
    private JMenuItem itemOpenTargetPath;
    private JMenuItem itemOpenDestinationPath;
    private JMenu itemBackup;
    private JMenuItem itemRunSingleBackup;
    private JCheckBoxMenuItem itemAutoBackup;
    private JMenuItem itemInterruptBackup;
    private JMenu itemCopyText;
    private JMenuItem itemCopyBackupName;
    private JMenuItem itemCopyTargetPath;
    private JMenuItem itemCopyDestinationPath;
}
