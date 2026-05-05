package backupmanager.gui.forms;

import java.awt.Component;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import com.formdev.flatlaf.FlatClientProperties;

import backupmanager.Enums.ConfigKey;
import backupmanager.Enums.Translations;
import backupmanager.Enums.Translations.TKey;
import backupmanager.Utils.SystemForm;
import net.miginfocom.swing.MigLayout;

@SystemForm(name = "History", description = "application history log")
public class FormHistory extends CustomForm {
    public FormHistory() {
        build();
    }

    @Override
    protected void init() {
        setLayout(new MigLayout("fill,wrap", "[fill]", "[][grow,fill]"));
        add(createInfo("Information", "Here you can find the application logs, useful for troubleshooting and understanding the application's behavior over time.", 1));
        add(createLogPanel());
    }

    @Override
    protected void loadData() {
        try {
            Path logFile = Paths.get(
                System.getProperty("user.home"),
                ".backupmanager",
                "logs",
                ConfigKey.LOG_FILE_STRING.getValue()
            );

            if (!Files.exists(logFile)) {
                logsPane.setText("Log file not found:\n" + logFile);
                return;
            }

            String content = Files.readString(logFile);

            logsPane.setText(content);
            logsPane.setCaretPosition(0);

        } catch (IOException ex) {
            logsPane.setText("Failed to load logs:\n" + ex.getMessage());
        }
    }

    private Component createLogPanel() {
        JPanel panel = new JPanel(
            new MigLayout("fill,insets 5 0 5 0", "[fill]", "[grow]")
        );

        logsPane = new JTextPane();
        logsPane.setEditable(false);
        logsPane.setContentType("text/plain");

        JScrollPane detailScroll = new JScrollPane(logsPane);
        detailScroll.putClientProperty(FlatClientProperties.STYLE,
                "arc:10; border:1,1,1,1,$Component.borderColor");

        panel.add(detailScroll, "grow");
        return panel;
    }

    @Override
    public void setTranslations() {
        editTitle(Translations.get(TKey.HISTORY_LOGS_TITLE));
        editDescription(Translations.get(TKey.HISTORY_LOGS_DESCRIPTION));
    }

    private JTextPane logsPane;
}
