package backupmanager.gui.forms;

import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.event.ActionEvent;
import java.lang.reflect.InvocationTargetException;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import com.formdev.flatlaf.util.ScaledEmptyBorder;

import backupmanager.Enums.Translations;
import backupmanager.Enums.Translations.TKey;
import backupmanager.Managers.LanguageManager;
import backupmanager.gui.component.AccentColorIcon;
import backupmanager.gui.system.FormManager;
import backupmanager.gui.themes.PanelThemes;
import backupmanager.utils.AppPreferences;
import backupmanager.utils.SystemForm;
import net.miginfocom.swing.MigLayout;
import raven.color.ColorPicker;
import raven.modal.Drawer;
import raven.modal.ModalDialog;
import raven.modal.component.SimpleModalBorder;
import raven.modal.drawer.DrawerBuilder;
import raven.modal.drawer.renderer.AbstractDrawerLineStyleRenderer;
import raven.modal.drawer.renderer.DrawerCurvedLineStyle;
import raven.modal.drawer.renderer.DrawerStraightDotLineStyle;
import raven.modal.drawer.simple.SimpleDrawerBuilder;
import raven.modal.option.LayoutOption;
import raven.modal.option.Location;
import raven.modal.option.Option;

@SystemForm(name = "Setting", description = "application setting and configuration", tags = {"themes", "options"})
public class FormSetting extends CustomForm {

    private static final Logger logger = LoggerFactory.getLogger(FormSetting.class);

    public FormSetting() {
        build();
    }

    @Override
    protected void init() {
        setLayout(new MigLayout("fill", "[fill][fill,grow 0,250:250]", "[fill]"));
        tabbedPane = new JTabbedPane();
        tabbedPane.putClientProperty(FlatClientProperties.STYLE, "" +
                "tabType:card");

        tabbedPane.addTab(Translations.get(TKey.SETTINGS_LAYOUT_TAB), createLayoutOption());
        tabbedPane.addTab(Translations.get(TKey.SETTINGS_LAYOUT_TAB), createStyleOption());
        add(tabbedPane, "gapy 1 0");
        add(createThemes());
    }

    @Override
    protected void loadData() {}

    private JPanel createLayoutOption() {
        JPanel panel = new JPanel(new MigLayout("wrap,fillx", "[fill]"));
        panel.add(createWindowsLayout());
        panel.add(createDrawerLayout());
        panel.add(createModalDefaultOption());
        panel.add(createLanguageOption());
        return panel;
    }

    private Component createWindowsLayout() {
        JPanel panel = new JPanel(new MigLayout());
        windowsLayout = new TitledBorder("Windows Layout");
        panel.setBorder(windowsLayout);
        chRightToLeft = new JCheckBox("Right to Left", !getComponentOrientation().isLeftToRight());
        chFullWindow = new JCheckBox("Full Window Content", FlatClientProperties.clientPropertyBoolean(FormManager.getFrame().getRootPane(), FlatClientProperties.FULL_WINDOW_CONTENT, false));
        chRightToLeft.addActionListener(e -> {
            if (chRightToLeft.isSelected()) {
                FormManager.getFrame().applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            } else {
                FormManager.getFrame().applyComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
            }
            FormManager.getFrame().revalidate();
        });
        chFullWindow.addActionListener(e -> {
            FormManager.getFrame().getRootPane().putClientProperty(FlatClientProperties.FULL_WINDOW_CONTENT, chFullWindow.isSelected());
        });
        panel.add(chRightToLeft);
        panel.add(chFullWindow);
        return panel;
    }

    private Component createDrawerLayout() {
        JPanel panel = new JPanel(new MigLayout());
        drawerLayout = new TitledBorder("Drawer layout");
        panel.setBorder(drawerLayout);

        jrLeft = new JRadioButton("Left");
        jrLeading = new JRadioButton("Leading");
        jrTrailing = new JRadioButton("Trailing");
        jrRight = new JRadioButton("Right");
        jrTop = new JRadioButton("Top");
        jrBottom = new JRadioButton("Bottom");

        ButtonGroup group = new ButtonGroup();
        group.add(jrLeft);
        group.add(jrLeading);
        group.add(jrTrailing);
        group.add(jrRight);
        group.add(jrTop);
        group.add(jrBottom);

        jrLeading.setSelected(true);

        jrLeft.addActionListener(e -> {
            DrawerBuilder drawerBuilder = Drawer.getDrawerBuilder();
            LayoutOption layoutOption = Drawer.getDrawerOption().getLayoutOption();
            layoutOption.setSize(drawerBuilder.getDrawerWidth(), 1f)
                    .setLocation(Location.LEFT, Location.TOP)
                    .setAnimateDistance(-0.7f, 0f);
            getRootPane().revalidate();
        });
        jrLeading.addActionListener(e -> {
            DrawerBuilder drawerBuilder = Drawer.getDrawerBuilder();
            LayoutOption layoutOption = Drawer.getDrawerOption().getLayoutOption();
            layoutOption.setSize(drawerBuilder.getDrawerWidth(), 1f)
                    .setLocation(Location.LEADING, Location.TOP)
                    .setAnimateDistance(-0.7f, 0f);
            getRootPane().revalidate();
        });
        jrTrailing.addActionListener(e -> {
            DrawerBuilder drawerBuilder = Drawer.getDrawerBuilder();
            LayoutOption layoutOption = Drawer.getDrawerOption().getLayoutOption();
            layoutOption.setSize(drawerBuilder.getDrawerWidth(), 1f)
                    .setLocation(Location.TRAILING, Location.TOP)
                    .setAnimateDistance(0.7f, 0f);
            getRootPane().revalidate();
        });
        jrRight.addActionListener(e -> {
            DrawerBuilder drawerBuilder = Drawer.getDrawerBuilder();
            LayoutOption layoutOption = Drawer.getDrawerOption().getLayoutOption();
            layoutOption.setSize(drawerBuilder.getDrawerWidth(), 1f)
                    .setLocation(Location.RIGHT, Location.TOP)
                    .setAnimateDistance(0.7f, 0f);
            getRootPane().revalidate();
        });
        jrTop.addActionListener(e -> {
            DrawerBuilder drawerBuilder = Drawer.getDrawerBuilder();
            LayoutOption layoutOption = Drawer.getDrawerOption().getLayoutOption();
            layoutOption.setSize(1f, drawerBuilder.getDrawerWidth())
                    .setLocation(Location.LEADING, Location.TOP)
                    .setAnimateDistance(0f, -0.7f);
            getRootPane().revalidate();
        });
        jrBottom.addActionListener(e -> {
            DrawerBuilder drawerBuilder = Drawer.getDrawerBuilder();
            LayoutOption layoutOption = Drawer.getDrawerOption().getLayoutOption();
            layoutOption.setSize(1f, drawerBuilder.getDrawerWidth())
                    .setLocation(Location.LEADING, Location.BOTTOM)
                    .setAnimateDistance(0f, 0.7f);
            getRootPane().revalidate();
        });

        panel.add(jrLeft);
        panel.add(jrLeading);
        panel.add(jrTrailing);
        panel.add(jrRight);
        panel.add(jrTop);
        panel.add(jrBottom);
        return panel;
    }

    private Component createModalDefaultOption() {
        JPanel panel = new JPanel(new MigLayout());
        modalOption = new TitledBorder("Default modal option");
        panel.setBorder(new TitledBorder(modalOption));
        chAnimation = new JCheckBox("Animation enable");
        chCloseOnPressedEscape = new JCheckBox("Close on pressed escape");
        chAnimation.setSelected(ModalDialog.getDefaultOption().isAnimationEnabled());
        chCloseOnPressedEscape.setSelected(ModalDialog.getDefaultOption().isCloseOnPressedEscape());

        chAnimation.addActionListener(e -> ModalDialog.getDefaultOption().setAnimationEnabled(chAnimation.isSelected()));
        chCloseOnPressedEscape.addActionListener(e -> ModalDialog.getDefaultOption().setCloseOnPressedEscape(chCloseOnPressedEscape.isSelected()));

        panel.add(chAnimation);
        panel.add(chCloseOnPressedEscape);

        return panel;
    }

    private Component createLanguageOption() {
        JPanel panel = new JPanel(new MigLayout());
        languageTitleBorder = new TitledBorder("Language");
        panel.setBorder(languageTitleBorder);
        languageCombo = new JComboBox<>();
        initComboItem(languageCombo);

        languageCombo.addActionListener(e -> {
            Object selected = languageCombo.getSelectedItem();
            String languageName = selected.toString();
            LanguageManager.setLanguage(languageName);
        });

        panel.add(languageCombo);

        return panel;
    }

    private void initComboItem(JComboBox<Object> combo) {
        combo.addItem("English");
        combo.addItem("Italiano");
        combo.addItem("Español");
        combo.addItem("Deutsch");
        combo.addItem("Français");
    }

    private JPanel createStyleOption() {
        JPanel panel = new JPanel(new MigLayout("wrap,fillx", "[fill]"));
        panel.add(createAccentColor());
        panel.add(createDrawerStyle());
        return panel;
    }

    private static final String[] accentColorKeys = {
            "App.accent.default", "App.accent.blue", "App.accent.purple", "App.accent.red",
            "App.accent.orange", "App.accent.yellow", "App.accent.green",
    };
    private static final String[] accentColorNames = {
            "Default", "Blue", "Purple", "Red", "Orange", "Yellow", "Green",
    };
    private final JToggleButton[] accentColorButtons = new JToggleButton[accentColorKeys.length];
    private JToggleButton accentColorCustomButton;
    private JToggleButton oldSelected;

    private Component createAccentColor() {
        JPanel panel = new JPanel(new MigLayout());
        accentLayout = new TitledBorder("Accent color");
        panel.setBorder(accentLayout);
        ButtonGroup group = new ButtonGroup();
        JToolBar toolBar = new JToolBar();
        toolBar.putClientProperty(FlatClientProperties.STYLE, "" +
                "hoverButtonGroupBackground:null;");

        boolean selected = false;
        for (int i = 0; i < accentColorButtons.length; i++) {
            accentColorButtons[i] = new JToggleButton(new AccentColorIcon(accentColorKeys[i]));
            accentColorButtons[i].setToolTipText(accentColorNames[i]);
            accentColorButtons[i].addActionListener(this::accentColorChanged);
            toolBar.add(accentColorButtons[i]);
            group.add(accentColorButtons[i]);
            if (!selected) {
                if (AppPreferences.accentColor == null) {
                    if (i == 0) {
                        accentColorButtons[i].setSelected(true);
                        oldSelected = accentColorButtons[i];
                        selected = true;
                    }
                } else {
                    Color color = UIManager.getColor(accentColorKeys[i]);
                    if (AppPreferences.accentColor.equals(color)) {
                        accentColorButtons[i].setSelected(true);
                        oldSelected = accentColorButtons[i];
                        selected = true;
                    }
                }
            }
        }
        accentColorCustomButton = createCustomAccentColor();
        group.add(accentColorCustomButton);
        toolBar.add(accentColorCustomButton);
        if (!selected) {
            accentColorCustomButton.setSelected(true);
        }

        FlatLaf.setSystemColorGetter(name -> name.equals("accent") ? AppPreferences.accentColor : null);
        UIManager.addPropertyChangeListener(e -> {
            if ("lookAndFeel".equals(e.getPropertyName())) {
                updateAccentColorButtons();
            }
        });
        updateAccentColorButtons();
        panel.add(toolBar);
        return panel;
    }

    private JToggleButton createCustomAccentColor() {
        JToggleButton button = new JToggleButton(new FlatSVGIcon("icons/color.svg", 16, 16));
        button.addActionListener(e -> {
            ColorPicker colorPicker = new ColorPicker(AppPreferences.accentColor);
            colorPicker.setBorder(new ScaledEmptyBorder(0, 20, 0, 20));
            Option option = ModalDialog.createOption();
            option.setAnimationEnabled(false);
            colorPickerLayout = new SimpleModalBorder(colorPicker, Translations.get(TKey.SETTINGS_COLOR_PICKER_LAYOUT), SimpleModalBorder.YES_NO_OPTION, (controller, action) -> {
                if (action == SimpleModalBorder.YES_OPTION) {
                    AppPreferences.accentColor = colorPicker.getSelectedColor();
                    oldSelected = null;
                    applyAccentColor();
                } else if (action == SimpleModalBorder.CLOSE_OPTION) {
                    if (oldSelected != null) {
                        oldSelected.setSelected(true);
                    }
                }
            });
            ModalDialog.showModal(this, colorPickerLayout, option);
        });
        return button;
    }

    private Component createDrawerStyle() {
        JPanel panel = new JPanel(new MigLayout("insets 0,filly", "[][][grow,fill]", "[fill]"));
        JPanel lineStyle = new JPanel(new MigLayout("wrap", "[200]"));
        JPanel lineStyleOption = new JPanel(new MigLayout("wrap", "[200]"));
        JPanel lineColorOption = new JPanel(new MigLayout("wrap", "[200]"));

        drawerLineLayout = new TitledBorder("Drawer line style");
        lineStyleOptionLayout = new TitledBorder("Line style option");
        colorOptionLayout = new TitledBorder("Color option");

        lineStyle.setBorder(drawerLineLayout);
        lineStyleOption.setBorder(lineStyleOptionLayout);
        lineColorOption.setBorder(colorOptionLayout);

        ButtonGroup groupStyle = new ButtonGroup();
        jrCurvedStyle = new JRadioButton("Curved line style");
        jrStraightDotStyle = new JRadioButton("Straight dot line style", true);
        groupStyle.add(jrCurvedStyle);
        groupStyle.add(jrStraightDotStyle);

        ButtonGroup groupStyleOption = new ButtonGroup();
        jrStyleOption1 = new JRadioButton("Rectangle");
        jrStyleOption2 = new JRadioButton("Ellipse", true);
        groupStyleOption.add(jrStyleOption1);
        groupStyleOption.add(jrStyleOption2);

        chPaintLineColor = new JCheckBox("Paint selected line color");

        jrCurvedStyle.addActionListener(e -> {
            if (jrCurvedStyle.isSelected()) {
                jrStyleOption1.setText(Translations.get(TKey.SETTINGS_LINE_STYLE_LINE));
                jrStyleOption2.setText(Translations.get(TKey.SETTINGS_LINE_STYLE_CURVED));
                boolean round = jrStyleOption2.isSelected();
                boolean paintSelectedLine = chPaintLineColor.isSelected();
                setDrawerLineStyle(true, round, paintSelectedLine);
            }
        });
        jrStraightDotStyle.addActionListener(e -> {
            if (jrStraightDotStyle.isSelected()) {
                jrStyleOption1.setText(Translations.get(TKey.SETTINGS_LINE_STYLE_RETTANGLE));
                jrStyleOption2.setText(Translations.get(TKey.SETTINGS_LINE_STYLE_ELLIPSE));
                boolean round = jrStyleOption2.isSelected();
                boolean paintSelectedLine = chPaintLineColor.isSelected();
                setDrawerLineStyle(false, round, paintSelectedLine);
            }
        });

        jrStyleOption1.addActionListener(e -> {
            if (jrStyleOption1.isSelected()) {
                boolean curved = jrCurvedStyle.isSelected();
                boolean paintSelectedLine = chPaintLineColor.isSelected();
                setDrawerLineStyle(curved, false, paintSelectedLine);
            }
        });

        jrStyleOption2.addActionListener(e -> {
            if (jrStyleOption2.isSelected()) {
                boolean curved = jrCurvedStyle.isSelected();
                boolean paintSelectedLine = chPaintLineColor.isSelected();
                setDrawerLineStyle(curved, true, paintSelectedLine);
            }
        });

        chPaintLineColor.addActionListener(e -> {
            boolean curved = jrCurvedStyle.isSelected();
            boolean round = jrStyleOption2.isSelected();
            boolean paintSelectedLine = chPaintLineColor.isSelected();
            setDrawerLineStyle(curved, round, paintSelectedLine);
        });

        lineStyle.add(jrCurvedStyle);
        lineStyle.add(jrStraightDotStyle);

        lineStyleOption.add(jrStyleOption1);
        lineStyleOption.add(jrStyleOption2);

        lineColorOption.add(chPaintLineColor);

        panel.add(lineStyle);
        panel.add(lineStyleOption);
        panel.add(lineColorOption);
        return panel;
    }

    private void setDrawerLineStyle(boolean curved, boolean round, boolean color) {
        AbstractDrawerLineStyleRenderer style;
        if (curved) {
            style = new DrawerCurvedLineStyle(round, color);
        } else {
            style = new DrawerStraightDotLineStyle(round, color);
        }
        ((SimpleDrawerBuilder) Drawer.getDrawerBuilder()).getSimpleMenuOption().getMenuStyle().setDrawerLineStyleRenderer(style);
        ((SimpleDrawerBuilder) Drawer.getDrawerBuilder()).getDrawerMenu().repaint();
    }

    private void accentColorChanged(ActionEvent e) {
        String accentColorKey = null;
        for (int i = 0; i < accentColorButtons.length; i++) {
            if (accentColorButtons[i].isSelected()) {
                accentColorKey = accentColorKeys[i];
                oldSelected = accentColorButtons[i];
                break;
            }
        }
        AppPreferences.accentColor = (accentColorKey != null && !accentColorKey.equals(accentColorKeys[0]))
                ? UIManager.getColor(accentColorKey)
                : null;
        applyAccentColor();
    }

    private void applyAccentColor() {
        Class<? extends LookAndFeel> lafClass = UIManager.getLookAndFeel().getClass();
        AppPreferences.updateAccentColor(AppPreferences.accentColor);
        try {
            FlatLaf.setup(lafClass.getDeclaredConstructor().newInstance());
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            logger.warn("Error while trying to apply the accent color: {}", e);
        }
        FlatLaf.updateUI();
    }

    private void updateAccentColorButtons() {
        Class<? extends LookAndFeel> lafClass = UIManager.getLookAndFeel().getClass();
        boolean isAccentColorSupported =
                lafClass == FlatLightLaf.class ||
                        lafClass == FlatDarkLaf.class ||
                        lafClass == FlatIntelliJLaf.class ||
                        lafClass == FlatDarculaLaf.class ||
                        lafClass == FlatMacLightLaf.class ||
                        lafClass == FlatMacDarkLaf.class;
        for (JToggleButton btn : accentColorButtons) {
            btn.setEnabled(isAccentColorSupported);
        }
        if (accentColorCustomButton != null) {
            accentColorCustomButton.setEnabled(isAccentColorSupported);
        }
    }

    private JPanel createThemes() {
        JPanel panel = new JPanel(new MigLayout("wrap,fill,insets 0", "[fill]", "[grow 0,fill]0[fill]"));
        final PanelThemes panelThemes = new PanelThemes();
        JPanel panelHeader = new JPanel(new MigLayout("fillx,insets 3", "[grow 0]push[]"));
        panelHeader.add(new JLabel("Themes"));
        JComboBox<Object> combo = new JComboBox<>(new Object[]{"All", "Light", "Dark"});
        combo.addActionListener(e -> {
            panelThemes.updateThemesList(combo.getSelectedIndex());
        });
        panelHeader.add(combo);
        panel.add(panelHeader);
        panel.add(panelThemes);
        return panel;
    }

    @Override
    protected void setTranslations() {
        windowsLayout.setTitle(Translations.get(TKey.SETTINGS_WINDOWS_LAYOUT));
        chRightToLeft.setText(Translations.get(TKey.SETTINGS_WINDOWS_RIGHT));
        chFullWindow.setText(Translations.get(TKey.SETTINGS_WINDOWS_FULL));
        drawerLayout.setTitle(Translations.get(TKey.SETTINGS_DRAWER_LAYOUT));
        jrLeft.setText(Translations.get(TKey.SETTINGS_DRAWER_LEFT));
        jrLeading.setText(Translations.get(TKey.SETTINGS_DRAWER_LEADING));
        jrTrailing.setText(Translations.get(TKey.SETTINGS_DRAWER_TRAILING));
        jrRight.setText(Translations.get(TKey.SETTINGS_DRAWER_RIGHT));
        jrTop.setText(Translations.get(TKey.SETTINGS_DRAWER_TOP));
        jrBottom.setText(Translations.get(TKey.SETTINGS_DRAWER_BOTTOM));
        modalOption.setTitle(Translations.get(TKey.SETTINGS_MODAL_OPTION));
        chAnimation.setText(Translations.get(TKey.SETTINGS_MODAL_ANIMATION));
        chCloseOnPressedEscape.setText(Translations.get(TKey.SETTINGS_MODAL_CLOSE));
        languageTitleBorder.setTitle(Translations.get(TKey.SETTINGS_LANGUAGES_LAYOUT));
        accentLayout.setTitle(Translations.get(TKey.SETTINGS_ACCENT_LAYOUT));
        // colorPickerLayout.setTitle(Translations.get(TKey.SETTINGS_COLOR_PICKER_LAYOUT)); // the method is not actually avaiable
        drawerLineLayout.setTitle(Translations.get(TKey.SETTINGS_DRAWER_LINE_LAYOUT));
        lineStyleOptionLayout.setTitle(Translations.get(TKey.SETTINGS_LINE_STYLE_LAYOUT));
        colorOptionLayout.setTitle(Translations.get(TKey.SETTINGS_COLOR_OPTION_LAYOUT));
        jrCurvedStyle.setText(Translations.get(TKey.SETTINGS_DRAWER_LINE_CURVED));
        jrStraightDotStyle.setText(Translations.get(TKey.SETTINGS_DRAWER_DOT_LINE));
        jrStyleOption1.setText(Translations.get(TKey.SETTINGS_LINE_STYLE_RETTANGLE));
        jrStyleOption2.setText(Translations.get(TKey.SETTINGS_LINE_STYLE_ELLIPSE));
        chPaintLineColor.setText(Translations.get(TKey.SETTINGS_COLOR_OPTION_PAINTED));
    }

    private JTabbedPane tabbedPane;
    private JComboBox<Object> languageCombo;
    private TitledBorder windowsLayout;
    private JCheckBox chRightToLeft;
    private JCheckBox chFullWindow;
    private TitledBorder drawerLayout;
    private JRadioButton jrLeft;
    private JRadioButton jrLeading;
    private JRadioButton jrTrailing;
    private JRadioButton jrRight;
    private JRadioButton jrTop;
    private JRadioButton jrBottom;
    private TitledBorder modalOption;
    private JCheckBox chAnimation;
    private JCheckBox chCloseOnPressedEscape;
    private TitledBorder languageTitleBorder;
    private TitledBorder accentLayout;
    private SimpleModalBorder colorPickerLayout;
    private TitledBorder drawerLineLayout;
    private TitledBorder lineStyleOptionLayout;
    private TitledBorder colorOptionLayout;
    private JRadioButton jrCurvedStyle;
    private JRadioButton jrStraightDotStyle;
    private JRadioButton jrStyleOption1;
    private JRadioButton jrStyleOption2;
    private JCheckBox chPaintLineColor;
}
