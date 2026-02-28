package backupmanager.simple;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import net.miginfocom.swing.MigLayout;
import raven.modal.component.ModalBorderAction;
import raven.modal.component.SimpleModalBorder;

public class SimpleInputForms extends JPanel {

    public static int NEW_COUNTRY = 30;

    public SimpleInputForms() {
        init();
    }

    private void init() {
        setLayout(new MigLayout("fillx,wrap,insets 5 30 5 30,width 400", "[fill]", ""));
        txtBackupName = new JTextField();
        txtTargetPath = new JTextField();
        txtDestinationPath = new JTextField();
        executeBackupBtn = new JButton("Execute Backup");
        automaticBackupBtn = new JButton("Automatic Backup (OFF)");
        targetPathBtn = new JButton(new FlatSVGIcon("icons/folder.svg", 25, 25));
        destinationPathBtn = new JButton(new FlatSVGIcon("icons/folder.svg", 25, 25));
        TimeIntervalBtn = new JButton(new FlatSVGIcon("icons/timer.svg", 25, 25));
        maxToKeeSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        maxToKeeLabel = new JLabel("Max to keep");
        lastBackupLabel = new JLabel("Last backup: never");

        JTextArea txtNotes = new JTextArea();
        txtNotes.setWrapStyleWord(true);
        txtNotes.setLineWrap(true);
        JScrollPane scroll = new JScrollPane(txtNotes);

        // style
        txtBackupName.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Backup name (unique)");
        txtTargetPath.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "target path e.g. C:\\Users\\Admin\\Documents");
        txtDestinationPath.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "destination folder e.g. D:\\Backups");

        // add to panel
        createTitle("Backup information");

        add(new JLabel("Backup Name"), "gapy 5 0");
        add(txtBackupName);
        add(new JLabel("Paths"), "gapy 5 0");
        add(txtTargetPath, "split 2");
        add(targetPathBtn, "w 30!, h 30!");
        add(txtDestinationPath, "split 2");
        add(destinationPathBtn, "w 30!, h 30!");

        add(new JLabel("Notes"), "gapy 5 0");
        add(scroll, "height 120,grow,pushy");

        createTitle("Advanced Information");

        add(lastBackupLabel);
        add(executeBackupBtn);
        add(automaticBackupBtn, "split 2");
        add(TimeIntervalBtn, "w 30!, h 30!");

        add(maxToKeeLabel, "gapy 5 0");
        add(maxToKeeSpinner, "width 100");


        txtNotes.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.isControlDown() && e.getKeyChar() == 10) {
                    ModalBorderAction modalBorderAction = ModalBorderAction.getModalBorderAction(SimpleInputForms.this);
                    if (modalBorderAction != null) {
                        modalBorderAction.doAction(SimpleModalBorder.YES_OPTION);
                    }
                }
            }
        });
    }

    private void createTitle(String title) {
        JLabel lb = new JLabel(title);
        lb.putClientProperty(FlatClientProperties.STYLE, "" +
                "font:+2");
        add(lb, "gapy 5 0");
        add(new JSeparator(), "height 2!,gapy 0 0");
    }

    public void formOpen() {
        txtBackupName.grabFocus();
    }

    private JTextField txtBackupName;
    private JTextField txtTargetPath;
    private JTextField txtDestinationPath;
    private JLabel lastBackupLabel;
    private JButton executeBackupBtn;
    private JButton automaticBackupBtn;
    private JButton targetPathBtn;
    private JButton destinationPathBtn;
    private JButton TimeIntervalBtn;
    private JSpinner maxToKeeSpinner;
    private JLabel maxToKeeLabel;
}
