package backupmanager.gui.simple;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSpinner;

import com.formdev.flatlaf.FlatClientProperties;

public abstract class CustomDialog<T> extends JPanel {

    public CustomDialog() { }

    protected final void build() {
        init();
        setTranslations();
    }

    protected abstract void init();
    protected abstract void setTranslations();
    protected abstract T getResult();

    protected void createTitle(String title) {
        JLabel lb = new JLabel(title);
        lb.putClientProperty(FlatClientProperties.STYLE, "font:+2");
        add(lb, "gapy 5 0");
        add(new JSeparator(), "height 2!,gapy 0 0");
    }

    protected void styleSpinner(JSpinner spinner) {
        spinner.putClientProperty(FlatClientProperties.STYLE, "" +
                "arc:10;" +
                "minimumWidth:90");
    }

    protected void configureSpinner(JSpinner spinner, int min, int max) {
        if (spinner.getEditor() instanceof JSpinner.NumberEditor editor) {
            editor.getTextField().setEditable(false);
        }

        spinner.addMouseWheelListener(evt -> {
            int rotation = evt.getWheelRotation();
            int current = (Integer) spinner.getValue();
            spinner.setValue(current + (rotation < 0 ? 1 : -1));
            validateSpinner(spinner, min, max);
        });
    }

    private void validateSpinner(JSpinner spinner, int min, int max) {
        Integer value = (Integer) spinner.getValue();

        if (value == null || value < min) {
            spinner.setValue(min);
        } else if (value > max) {
            spinner.setValue(max);
        }
    }
}
