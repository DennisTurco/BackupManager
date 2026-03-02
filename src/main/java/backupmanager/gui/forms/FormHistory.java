package backupmanager.gui.forms;

import java.io.IOException;
import java.io.InputStream;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import com.formdev.flatlaf.FlatClientProperties;

import backupmanager.Enums.ConfigKey;
import backupmanager.gui.system.Form;
import backupmanager.utils.SystemForm;
import net.miginfocom.swing.MigLayout;


@SystemForm(name = "History", description = "application history log")
public class FormHistory extends Form {
    public FormHistory() {
        init();
    }

    private void init() {
        setLayout(new MigLayout("fill,wrap", "[fill]", "[][grow,fill]"));
        createTitle();
        createInfo("Here you can find the application logs, useful for troubleshooting and understanding the application's behavior over time.");
        createLogPanel();
        loadLogs();
    }

    @Override
    public void formInit() {
        loadLogs();
    }

    @Override
    public void formRefresh() {
        loadLogs();
    }

    private void loadLogs() {
        try {
            try (InputStream is = getClass().getClassLoader().getResourceAsStream("res/logs/" + ConfigKey.LOG_FILE_STRING.getValue())) {
                if (is == null) {
                    logsPane.setText("Log file not found in resources");
                    return;
                }

                String content = new String(is.readAllBytes());

                logsPane.setText(content);
                logsPane.setCaretPosition(0);
            }

        } catch (IOException ex) {
            logsPane.setText("Failed to load logs:\n" + ex.getMessage());
        }
    }

    private void createTitle() {
        JPanel panel = new JPanel(new MigLayout("fillx", "[]push[][]"));
        JLabel title = new JLabel("History");

        title.putClientProperty(FlatClientProperties.STYLE, "" +
                "font:bold +3");

        panel.add(title);
        add(panel);
    }

    private void createInfo(String description) {
        JPanel panel = new JPanel(new MigLayout("fillx,wrap", "[fill]"));
        JLabel lbTitle = new JLabel("Information");
        JTextPane text = new JTextPane();
        text.setText(description);
        text.setEditable(false);
        text.setBorder(BorderFactory.createEmptyBorder());
        lbTitle.putClientProperty(FlatClientProperties.STYLE, "" +
                "font:bold");
        panel.add(lbTitle);
        panel.add(text, "width 500");
        add(panel);
    }

    private void createLogPanel() {
        logsPane = new JTextPane();
        logsPane.setEditable(false);
        logsPane.setContentType("text/plain");

        JScrollPane scroll = new JScrollPane(logsPane);

        scroll.putClientProperty(FlatClientProperties.STYLE,
                "arc:10;" +
                "border:1,1,1,1,$Component.borderColor");

        add(scroll, "grow");
    }

    private JTextPane logsPane;
}
