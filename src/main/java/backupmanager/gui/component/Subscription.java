package backupmanager.gui.component;

import java.awt.Graphics;
import java.time.format.DateTimeFormatter;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.DefaultCaret;

import backupmanager.Enums.ConfigKey;
import backupmanager.Enums.SubscriptionStatus;
import backupmanager.Helpers.SubscriptionHelper;
import backupmanager.Managers.WebsiteManager;
import net.miginfocom.swing.MigLayout;

public class Subscription extends JPanel {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public Subscription() {
        init();
    }

    private void init() {
        setLayout(new MigLayout("fillx, wrap, insets 25, width 520"));
        setOpaque(false);

        SubscriptionStatus status = SubscriptionHelper.getSubscriptionStatus();
        String statusString = SubscriptionHelper.getSubscriptionStatusTranslated(status);

        backupmanager.Entities.Subscription subscription = SubscriptionHelper.getLastValidSubscription();
        String from = formatDate(subscription != null ? subscription.startDate() : null);
        String to   = formatDate(subscription != null ? subscription.endDate() : null);

        JTextPane description = createHtmlPane(
                buildHtml(status, statusString, from, to)
        );

        add(description, "growx");
    }

    private JTextPane createHtmlPane(String html) {
        JTextPane pane = new JTextPane();
        pane.setContentType("text/html");
        pane.setText(html);
        pane.setEditable(false);
        pane.setOpaque(false);
        pane.setBorder(BorderFactory.createEmptyBorder());

        pane.setCaret(new DefaultCaret() {
            @Override public void paint(Graphics g) {}
        });

        pane.addHyperlinkListener(e -> {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                WebsiteManager.openWebSite(e.getURL().toString());
            }
        });

        return pane;
    }

    private String formatDate(java.time.temporal.TemporalAccessor date) {
        if (date == null) return "N/A";
        return DATE_FORMAT.format(date);
    }

    private String buildHtml(SubscriptionStatus status, String statusText, String validFrom, String validTo) {
        String statusColor = switch (status) {
            case ACTIVE -> "#2E7D32";
            case EXPIRATION -> "#ED6C02";
            case EXPIRED -> "#D32F2F";
            case NONE -> "#757575";
        };

        String subject = encodeURIComponent("Support - Backup Manager");

        return """
                <html>
                <div>
                    <b>Subscription status:</b>
                    <span style="color:%s;"><b>%s</b></span>
                    <br><br>

                    <b>Valid from:</b> %s<br>
                    <b>Valid to:</b> %s<br>

                    <br>
                    <a href="mailto:%s?subject=%s">Contact us</a>
                    to extend the subscription period.
                </div>
                </html>
                """.formatted(
                statusColor,
                statusText,
                validFrom,
                validTo,
                ConfigKey.EMAIL.getValue(),
                subject
        );
    }

    private String encodeURIComponent(String value) {
        return java.net.URLEncoder
                .encode(value, java.nio.charset.StandardCharsets.UTF_8)
                .replace("+", "%20");
    }
}
