package backupmanager.forms;

import java.awt.Component;
import java.io.IOException;
import java.text.DecimalFormat;

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
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import backupmanager.sample.csv.CSVDataReader;
import backupmanager.sample.csv.ResponseCSV;
import backupmanager.simple.SimpleInputForms;
import backupmanager.svg.SVGButton;
import backupmanager.system.Form;
import backupmanager.utils.SystemForm;
import backupmanager.utils.table.TableHeaderAlignment;
import net.miginfocom.swing.MigLayout;
import raven.modal.ModalDialog;
import raven.modal.component.SimpleModalBorder;
import raven.modal.option.Location;
import raven.modal.option.Option;
import raven.swingpack.JPagination;

@SystemForm(name = "Table", description = "table is a user interface component", tags = {"list"})
public class FormTable extends Form {

    public FormTable() {
        init();
    }

    private void init() {
        setLayout(new MigLayout(
        "fill,wrap",
        "[fill]",
        "[][grow 100,fill][grow 0]"
        ));
        add(createInfo("Backup List", "A table is a user interface component that displays a collection of records in a structured, tabular format. It allows users to view, sort, and manage data or other resources.", 1));
        add(createBorder(createBasicTable()), "gapx 7 7, grow");
        add(createBorder(createDetails()), "gapx 7 7, hmin 150");
    }

    @Override
    public void formInit() {
        try {
            data = CSVDataReader.load(getClass().getResourceAsStream("/data/customers-1000.csv"));
            DefaultTableModel model = (DefaultTableModel) basicTable.getModel();
            model.setColumnIdentifiers(data.getColumns());
            basicTable.setModel(model);

            // table column size
            basicTable.getColumnModel().getColumn(0).setMaxWidth(50);

            formRefresh();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public void formRefresh() {
        showData(pagination.getSelectedPage());
    }

    private void showData(int page) {
        if (data != null) {
            ResponseCSV res = data.getData(page, limit);
            lbTotalPage.setText(DecimalFormat.getInstance().format(res.getTotal()));
            pagination.getModel().setPageRange(res.getPage(), res.getPageSize());

            DefaultTableModel model = (DefaultTableModel) basicTable.getModel();
            model.setRowCount(0);
            for (String[] row : res.getData()) {
                model.addRow(row);
            }
        }
    }

    private JPanel createInfo(String title, String description, int level) {
        JPanel panel = new JPanel(new MigLayout("fillx,wrap", "[fill]"));
        JLabel lbTitle = new JLabel(title);
        JTextPane text = new JTextPane();
        text.setText(description);
        text.setEditable(false);
        text.setBorder(BorderFactory.createEmptyBorder());
        lbTitle.putClientProperty(FlatClientProperties.STYLE, "" +
                "font:bold +" + (4 - level));
        panel.add(lbTitle);
        panel.add(text, "width 500");
        return panel;
    }

    private Component createBorder(Component component) {
        JPanel panel = new JPanel(new MigLayout("fill,insets 7 0 7 0", "[fill]", "[fill]"));
        panel.add(component);
        return panel;
    }

    private Component createBasicTable() {
        JPanel panelTable = new JPanel(new MigLayout("fill,wrap,insets 10 0 10 0",
              "[fill]",
              "[][][grow,fill][pref!]"));

        // create table model
        JTable table = new JTable();
        table.setModel(new DefaultTableModel() {
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
                    StringBuilder details = new StringBuilder();
                    for (int i = 0; i < table.getColumnCount(); i++) {
                        details.append(table.getColumnName(i))
                            .append(": ")
                            .append(table.getValueAt(row, i))
                            .append("\n");
                    }
                    txtDetails.setText(details.toString());
                }
            }
        });

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() != -1) {
                    int row = table.getSelectedRow();
                    //showRowDetail(table, row);
                    System.out.println("Double clicked row: " + row);
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
                    ((DefaultTableModel) table.getModel()).removeRow(row);
                    System.out.println("Deleted row: " + row);
                }
            }
        });

        // table scroll
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        // alignment table header
        table.getTableHeader().setDefaultRenderer(new TableHeaderAlignment(table) {
            @Override
            protected int getAlignment(int column) {
                if (column == 0) {
                    return SwingConstants.CENTER;
                }
                return SwingConstants.LEADING;
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
        panelTable.add(scrollPane, "grow, push");

        // create pagination
        pagination = new JPagination(11, 1, 1);
        pagination.setMinimumSize(pagination.getPreferredSize());
        pagination.addChangeListener(e -> {
            showData(pagination.getSelectedPage());
        });
        JPanel panelPage = new JPanel(new MigLayout("insets 5 15 5 15", "[][]push[pref!]"));
        lbTotalPage = new JLabel("0");
        pagination.putClientProperty(FlatClientProperties.STYLE, "" +
                "background:null;");
        panelPage.putClientProperty(FlatClientProperties.STYLE, "" +
                "background:null;");
        panelPage.add(new JLabel("Total:"));
        panelPage.add(lbTotalPage);
        panelPage.add(pagination);

        panelTable.add(panelPage);

        basicTable = table;


        JPopupMenu popupMenu = new JPopupMenu();

        JMenuItem itemEdit = new JMenuItem("Edit");
        JMenuItem itemDelete = new JMenuItem("Delete");
        JMenuItem itemDuplicate = new JMenuItem("Duplicate");
        JMenuItem itemRename = new JMenuItem("Rename");
        JMenuItem itemOpenTargetPath = new JMenuItem("Open target path");
        JMenuItem itemOpenDestinationPath = new JMenuItem("Open destination path");

        JMenu itemBackup = new JMenu("Backup");
        JMenuItem itemRunSingleBackup = new JMenuItem("Run single backup");
        JCheckBoxMenuItem itemAutoBackup = new JCheckBoxMenuItem("Auto backup");

        JMenu itemCopyText = new JMenu("Copy text");
        JMenuItem itemCopyBackupName = new JMenuItem("Copy backup name");
        JMenuItem itemCopyTargetPath = new JMenuItem("Copy target path");
        JMenuItem itemCopyDestinationPath = new JMenuItem("Copy destination path");

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
        popupMenu.addSeparator();
        popupMenu.add(itemCopyText);
        itemCopyText.add(itemCopyBackupName);
        itemCopyText.add(itemCopyTargetPath);
        itemCopyText.add(itemCopyDestinationPath);

        table.addMouseListener(new java.awt.event.MouseAdapter() {
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
            int row = table.rowAtPoint(e.getPoint());
            if (row >= 0) {
                table.setRowSelectionInterval(row, row);
            }
            popupMenu.show(e.getComponent(), e.getX(), e.getY());
        }
        });


        return panelTable;
    }

    private Component createDetails() {
        JPanel detailsPanel = new JPanel(
            new MigLayout("fill,insets 5 0 5 0", "[fill]", "[grow]")
        );

        txtDetails = new JTextArea();
        txtDetails.setEditable(false);
        txtDetails.setLineWrap(true);
        txtDetails.setWrapStyleWord(true);

        JScrollPane detailScroll = new JScrollPane(txtDetails);
        detailScroll.putClientProperty(FlatClientProperties.STYLE,
                "arc:10; border:1,1,1,1,$Component.borderColor");

        detailsPanel.add(detailScroll, "grow");
        return detailsPanel;
    }

    private Component createHeaderAction() {
        JPanel panel = new JPanel(new MigLayout("insets 5 20 5 20", "[fill,300]push[][]"));

        JTextField txtSearch = new JTextField();
        txtSearch.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Search...");
        txtSearch.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, new FlatSVGIcon("icons/search.svg", 0.4f));
        SVGButton cmdCreate = new SVGButton("Create");
        SVGButton cmdEdit = new SVGButton("Edit");
        SVGButton cmdDelete = new SVGButton("Delete");

        cmdCreate.setSvgImage("icons/add.svg", 16, 16);
        cmdEdit.setSvgImage("icons/edit.svg", 16, 16);
        cmdDelete.setSvgImage("icons/delete.svg", 16, 16);

        cmdCreate.putClientProperty(FlatClientProperties.STYLE, "background:$Component.accentColor;");
        cmdDelete.putClientProperty(FlatClientProperties.STYLE, "background:$Component.error.background;");

        cmdCreate.addActionListener(e -> showModal());
        panel.add(txtSearch);
        panel.add(cmdCreate);
        panel.add(cmdEdit);
        panel.add(cmdDelete);

        panel.putClientProperty(FlatClientProperties.STYLE, "" +
                "background:null;");
        return panel;
    }

    private void showModal() {
        Option option = ModalDialog.createOption();
        option.getLayoutOption().setSize(-1, 1f)
                .setLocation(Location.TRAILING, Location.TOP)
                .setAnimateDistance(0.7f, 0);
        ModalDialog.showModal(this, new SimpleModalBorder(
                new SimpleInputForms(), "Create", SimpleModalBorder.YES_NO_OPTION,
                (controller, action) -> {
                }), option);
    }

    private CSVDataReader data;
    private final int limit = 50;
    private JPagination pagination;
    private JTable basicTable;
    private JLabel lbTotalPage;
    private JTextArea txtDetails;
}
