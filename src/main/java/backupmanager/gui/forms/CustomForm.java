package backupmanager.gui.forms;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;

import com.formdev.flatlaf.FlatClientProperties;

import backupmanager.gui.system.Form;
import net.miginfocom.swing.MigLayout;

public abstract class CustomForm extends Form {

    private JLabel lbTitle;
    private JTextPane text;

    protected CustomForm() {}

    protected void build() {
        init();
        setTranslations();
    }

    protected abstract void init();
    protected abstract void setTranslations();

    @Override
    public void formInit() {
        loadData();
    }

    @Override
    public void formRefresh() {
        loadData();
    }

    protected abstract void loadData();

    protected JPanel createInfo(String title, String description, int level) {
        JPanel panel = new JPanel(new MigLayout("fillx,wrap", "[fill]"));
        lbTitle = new JLabel(title);
        text = new JTextPane();
        text.setText(description);
        text.setEditable(false);
        text.setBorder(BorderFactory.createEmptyBorder());
        lbTitle.putClientProperty(FlatClientProperties.STYLE, "" +
                "font:bold +" + (4 - level));
        panel.add(lbTitle);
        panel.add(text, "width 500");
        return panel;
    }

    protected void editTitle(String title) {
        lbTitle.setText(title);
    }

    protected void editDescription(String description) {
        text.setText(description);
    }
}
