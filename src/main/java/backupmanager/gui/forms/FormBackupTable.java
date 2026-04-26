package backupmanager.gui.forms;

import java.awt.Component;
import java.time.LocalDateTime;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import backupmanager.Entities.ConfigurationBackup;
import backupmanager.Entities.TimeInterval;
import backupmanager.Enums.Translations;
import backupmanager.Enums.Translations.TKey;
import backupmanager.Exceptions.BackupDeletionException;
import backupmanager.Helpers.BackupHelper;
import static backupmanager.Helpers.BackupHelper.formatter;
import backupmanager.Services.BackupObserver;
import backupmanager.Services.BackupService;
import backupmanager.Utils.SystemForm;
import backupmanager.Utils.ToastUtils;
import backupmanager.Utils.table.TableHeaderAlignment;
import backupmanager.gui.Table.BackupTable;
import backupmanager.gui.Table.BackupTableDataService;
import backupmanager.gui.frames.Controllers.BackupManagerController;
import backupmanager.gui.frames.Controllers.BackupPopupController;
import backupmanager.gui.svg.SVGButton;
import net.miginfocom.swing.MigLayout;

@SystemForm(name = "Table", description = "table is a user interface component", tags = {"list"})
public class FormBackupTable extends CustomForm {

    private static final Logger logger = LoggerFactory.getLogger(FormBackupTable.class);

    private BackupManagerController managerController;
    private BackupTableDataService tableService;
    private BackupObserver backupObserver;
    private final int COL_LAST_RUN = 3;
    private final int COL_AUTOMATIC = 4;
    private final int COL_NEXT_RUN = 5;

    private List<ConfigurationBackup> backups;

    public FormBackupTable() {
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

        tableService = new BackupTableDataService(backupTable, formatter);
        managerController = new BackupManagerController(new BackupService(), tableService);

        backupObserver = new BackupObserver(tableService, 2000);
        backupObserver.start();

        formRefresh();
    }

    @Override
    public void formRefresh() {
        backups = managerController.getAllBackups();
        loadData();
    }

    @Override
    protected void loadData() {
        if (backups == null)
            return;

        DefaultTableModel model = (DefaultTableModel) backupTable.getModel();
        model.setRowCount(0);

        for (ConfigurationBackup backup : backups) {
            model.addRow(backup.toTableRow());
        }
    }

    private Component createBorder(Component component) {
        JPanel panel = new JPanel(new MigLayout("fill,insets 7 0 7 0", "[fill]", "[fill]"));
        panel.add(component);
        return panel;
    }

    private Component createBasicTable() {
        JPanel panelTable = new JPanel(new MigLayout(
            "fill,wrap,insets 10 10 10 10",
            "[fill]",
            "[][grow,fill]"
        ));

        // create table model
        BackupTable table = new BackupTable(
            new DefaultTableModel() {

                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    if (columnIndex == COL_AUTOMATIC)
                        return Boolean.class;
                    if (columnIndex == COL_LAST_RUN || columnIndex == COL_NEXT_RUN)
                        return LocalDateTime.class;
                    if (columnIndex == 6)
                        return TimeInterval.class;
                    if (columnIndex == 7)
                        return Integer.class;
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
                    String details = managerController.buildDetails(backups.get(row));
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
            case "EDIT" -> showEditModal(backup);
            case "DELETE" -> {
                try {
                    boolean deleted = BackupHelper.deleteBackupWithConfirmition(backup);
                    if (deleted)
                        ToastUtils.showSuccess(this, Translations.get(TKey.TOAST_BACKUP_DELETED));
                } catch (BackupDeletionException ex) {
                    ToastUtils.showError(this, Translations.get(TKey.TOAST_BACKUP_DELETED_ERROR));
                }
            }
            case "DUPLICATE" -> BackupPopupController.popupItemDuplicateBackup(backup);
            case "RENAME" -> BackupPopupController.popupItemRenameBackup(backups, backup);
            case "OPEN_TARGET" -> BackupPopupController.popupItemOpenInitialPath(backup);
            case "OPEN_DEST" -> BackupPopupController.popupItemOpenDestinationPath(backup);
            case "RUN_SINGLE" -> BackupPopupController.popupItemRunBackup(backup, tableService, interruptBackupPopupItem, RunBackupPopupItem);
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
                backups = managerController.getAllBackups();
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
    }

    private void showEditModal(ConfigurationBackup backup) {
        managerController.showEditModal(this, backup);
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
        try {
            boolean deleted = BackupHelper.deleteBackupWithConfirmition(backup);
            if (deleted)
                ToastUtils.showSuccess(this, Translations.get(TKey.TOAST_BACKUP_DELETED));
        } catch (BackupDeletionException e) {
            ToastUtils.showError(this, Translations.get(TKey.TOAST_BACKUP_DELETED_ERROR));
        }
        formRefresh();
    }

    @Override
    public void setTranslations() {
        editTitle(Translations.get(TKey.BACKUP_LIST_TITLE));
        editDescription(Translations.get(TKey.BACKUP_LIST_DESCRIPTION));

        txtSearch.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, Translations.get(TKey.RESEARCH_BAR_PLACEHOLDER));
        txtSearch.setToolTipText(Translations.get(TKey.RESEARCH_BAR_TOOLTIP));

        cmdCreate.setText(Translations.get(TKey.CREATE_BUTTON));
        cmdEdit.setText(Translations.get(TKey.EDIT_BUTTON));
        cmdDelete.setText(Translations.get(TKey.DELETE_BUTTON));

        itemEdit.setText(Translations.get(TKey.EDIT_POPUP));
        itemDelete.setText(Translations.get(TKey.DELETE_POPUP));
        itemDuplicate.setText(Translations.get(TKey.DUPLICATE_POPUP));
        itemRename.setText(Translations.get(TKey.RENAME_BACKUP_POPUP));
        itemOpenTargetPath.setText(Translations.get(TKey.OPEN_INITIAL_FOLDER_POPUP));
        itemOpenDestinationPath.setText(Translations.get(TKey.OPEN_DESTINATION_FOLDER_POPUP));
        itemBackup.setText(Translations.get(TKey.BACKUP_POPUP));
        itemRunSingleBackup.setText(Translations.get(TKey.SINGLE_BACKUP_POPUP));
        itemAutoBackup.setText(Translations.get(TKey.AUTO_BACKUP_POPUP));
        itemInterruptBackup.setText(Translations.get(TKey.INTERRUPT_POPUP));
        itemCopyText.setText(Translations.get(TKey.COPY_TEXT_POPUP));
        itemCopyBackupName.setText(Translations.get(TKey.COPY_BACKUP_NAME_POPUP));
        itemCopyTargetPath.setText(Translations.get(TKey.COPY_INITIAL_PATH_POPUP));
        itemCopyDestinationPath.setText(Translations.get(TKey.COPY_DESTINATION_PATH_BACKUP));
    }

    private BackupTable backupTable;
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
