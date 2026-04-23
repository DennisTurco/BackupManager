package backupmanager.gui.simple;

import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;

import com.formdev.flatlaf.FlatClientProperties;

import backupmanager.Entities.TimeInterval;
import backupmanager.Enums.Translations;
import backupmanager.Enums.Translations.TKey;
import backupmanager.gui.Controllers.TimePickerController;
import net.miginfocom.swing.MigLayout;

public class TimePickerDialog extends CustomDialog<TimeInterval> {

    private final TimePickerController timePickerController;

    public TimePickerDialog(TimeInterval timeInterval) {
        build();

        timePickerController = new TimePickerController();

        if (timeInterval != null) {
            daysSpinner.setValue(timeInterval.days());
            hoursSpinner.setValue(timeInterval.hours());
            minutesSpinner.setValue(timeInterval.minutes());
        }
    }

    @Override
    protected void init() {

        setLayout(new MigLayout(
                "fillx,wrap 2,insets 20 35 20 35,width 420",
                "[right][grow,fill]",
                ""
        ));

        description = new JTextArea();
        description.setEditable(false);
        description.setOpaque(false);
        description.setWrapStyleWord(true);
        description.setLineWrap(true);
        description.setFocusable(false);
        description.putClientProperty(FlatClientProperties.STYLE, "foreground:$Label.disabledForeground");

        add(description, "span,growx,gapy 0 15");

        daysLabel = new JLabel();
        hoursLabel = new JLabel();
        minutesLabel = new JLabel();

        daysSpinner = new JSpinner(new SpinnerNumberModel(30, 0, 365, 1));
        hoursSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 23, 1));
        minutesSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 59, 1));

        styleSpinner(daysSpinner);
        styleSpinner(hoursSpinner);
        styleSpinner(minutesSpinner);

        configureSpinner(daysSpinner, 0, 365);
        configureSpinner(hoursSpinner, 0, 23);
        configureSpinner(minutesSpinner, 0, 59);

        add(daysLabel, "gapy 5 0");
        add(daysSpinner, "width 90!");

        add(hoursLabel, "gapy 5 0");
        add(hoursSpinner, "width 90!");

        add(minutesLabel, "gapy 5 0");
        add(minutesSpinner, "width 90!");
    }

    @Override
    public TimeInterval getResult() {
        int days = (Integer) daysSpinner.getValue();
        int hours = (Integer) hoursSpinner.getValue();
        int minutes = (Integer) minutesSpinner.getValue();

        return timePickerController.getTimeIntervalIfPossible(this, days, hours, minutes);
    }

    @Override
    public void setTranslations() {
        description.setText(Translations.get(TKey.DESCRIPTION));
        daysSpinner.setToolTipText(Translations.get(TKey.SPINNER_TOOLTIP));
        hoursSpinner.setToolTipText(Translations.get(TKey.SPINNER_TOOLTIP));
        minutesSpinner.setToolTipText(Translations.get(TKey.SPINNER_TOOLTIP));
        daysLabel.setText(Translations.get(TKey.DAYS));
        hoursLabel.setText(Translations.get(TKey.HOURS));
        minutesLabel.setText(Translations.get(TKey.MINUTES));
    }

    private JTextArea description;
    private JLabel daysLabel;
    private JLabel hoursLabel;
    private JLabel minutesLabel;
    private JSpinner daysSpinner;
    private JSpinner hoursSpinner;
    private JSpinner minutesSpinner;
}
