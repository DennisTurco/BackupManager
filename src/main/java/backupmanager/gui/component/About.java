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
import backupmanager.Enums.MenuItems;
import backupmanager.Enums.Translations;
import backupmanager.Enums.Translations.TKey;
import backupmanager.Json.JsonConfig;
import backupmanager.Managers.WebsiteManager;
import backupmanager.gui.menu.DrawerManager;
import net.miginfocom.swing.MigLayout;

public class About extends JPanel {

    public About() {
        init();
    }

    private void init() {

        setLayout(new MigLayout("fillx,wrap,insets 20,width 520"));

        JTextPane title = createText(Translations.get(TKey.APP_NAME));
        title.putClientProperty(FlatClientProperties.STYLE, "font:bold +6");

        JTextPane description = createText("");
        description.setContentType("text/html");
        description.setText(getDescriptionText());

        description.addHyperlinkListener(e -> {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
                WebsiteManager.openWebSite(DrawerManager.getInstance().getParent(), e.getURL().toString());
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
        String message = Translations.get(TKey.ABOUT_MESSAGE_BODY);
        message = message.replace("[PROJECT_WEBSITE]", ConfigKey.INFO_PAGE_LINK.getValue());

        // removing all the info inside the <p> tag
        JsonConfig config = JsonConfig.getInstance();
        if (!config.isMenuItemEnabled(MenuItems.Website.name())) {
            message = message.replaceAll("<p>.*?</p>", "");
        }

        return message;
    }

    private JComponent createSystemInformation() {

        JPanel panel = new JPanel(new MigLayout("wrap,insets 10"));
        panel.setBorder(new TitledBorder(Translations.get(TKey.ABOUT_SYSTEM_INFORMATION)));

        JTextPane text = createText("");
        text.setContentType("text/html");

        String info = """
        <html>
        %s: %s<br>
        Java: %s<br>
        OS: %s<br>
        </html>
        """.formatted(
                Translations.get(TKey.VERSION),
                ConfigKey.VERSION.getValue(),
                System.getProperty("java.vendor") + " - v" + System.getProperty("java.version"),
                System.getProperty("os.name") + " " + System.getProperty("os.arch")
        );

        text.setText(info);
        panel.add(text);

        return panel;
    }
}
