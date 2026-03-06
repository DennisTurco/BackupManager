package backupmanager.gui.component;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.border.TitledBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.DefaultCaret;

import com.formdev.flatlaf.FlatClientProperties;

import backupmanager.Enums.ConfigKey;
import backupmanager.Managers.WebsiteManager;
import net.miginfocom.swing.MigLayout;

public class About extends JPanel {

    public About() {
        init();
    }

    private void init() {

        setLayout(new MigLayout("fillx,wrap,insets 20,width 520"));

        JTextPane title = createText("Backup Manager");
        title.putClientProperty(FlatClientProperties.STYLE, "font:bold +6");

        JTextPane description = createText("");
        description.setContentType("text/html");
        description.setText(getDescriptionText());

        description.addHyperlinkListener(e -> {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
                WebsiteManager.openWebSite(e.getURL().toString());
        });

        add(title);
        add(description, "gapy 10");
        add(createSystemInformation(), "gapy 15");
    }

    private JTextPane createText(String text) {
        JTextPane pane = new JTextPane();
        pane.setBorder(BorderFactory.createEmptyBorder());
        pane.setText(text);
        pane.setEditable(false);
        pane.setOpaque(false);

        pane.setCaret(new DefaultCaret() {
            @Override
            public void paint(java.awt.Graphics g) {}
        });

        return pane;
    }

    private String getDescriptionText() {
        return """
        <html>
        <b>Backup Manager</b> is a simple and powerful application designed to automate
        folder and subfolder backups.

        <br><br>
        Users can schedule automatic backups or execute manual backups anytime.

        <br><br>
        Backup history is stored securely, allowing full control over saved data.

        <br><br>
        Visit <a href="%s">project website</a> for more information.
        </html>
        """.formatted(ConfigKey.INFO_PAGE_LINK.getValue());
    }

    private JComponent createSystemInformation() {

        JPanel panel = new JPanel(new MigLayout("wrap,insets 10"));
        panel.setBorder(new TitledBorder("System Information"));

        JTextPane text = createText("");
        text.setContentType("text/html");

        String info = """
        <html>
        Version: %s<br>
        Java: %s<br>
        OS: %s<br>
        </html>
        """.formatted(
                ConfigKey.VERSION.getValue(),
                System.getProperty("java.vendor") + " - v" + System.getProperty("java.version"),
                System.getProperty("os.name") + " " + System.getProperty("os.arch")
        );

        text.setText(info);
        panel.add(text);

        return panel;
    }
}
