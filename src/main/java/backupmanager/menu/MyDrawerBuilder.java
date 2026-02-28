package backupmanager.menu;

import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.UIManager;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import backupmanager.Enums.ConfigKey;
import backupmanager.forms.FormDashboard;
import backupmanager.forms.FormSetting;
import backupmanager.forms.FormTable;
import backupmanager.system.AllForms;
import backupmanager.system.Form;
import backupmanager.system.FormManager;
import raven.extras.AvatarIcon;
import raven.modal.drawer.DrawerPanel;
import raven.modal.drawer.item.Item;
import raven.modal.drawer.item.MenuItem;
import raven.modal.drawer.menu.MenuOption;
import raven.modal.drawer.menu.MenuStyle;
import raven.modal.drawer.renderer.DrawerStraightDotLineStyle;
import raven.modal.drawer.simple.SimpleDrawerBuilder;
import raven.modal.drawer.simple.footer.LightDarkButtonFooter;
import raven.modal.drawer.simple.footer.SimpleFooterData;
import raven.modal.drawer.simple.header.SimpleHeader;
import raven.modal.drawer.simple.header.SimpleHeaderData;
import raven.modal.option.Option;
import raven.modal.utils.FlatLafStyleUtils;

public class MyDrawerBuilder extends SimpleDrawerBuilder {

    private static MyDrawerBuilder instance;

    public static MyDrawerBuilder getInstance() {
        if (instance == null) {
            instance = new MyDrawerBuilder();
        }
        return instance;
    }

    public void initHeader() {
        // setup drawer header
        SimpleHeader header = (SimpleHeader) getHeader();
        SimpleHeaderData data = header.getSimpleHeaderData();
        AvatarIcon icon = (AvatarIcon) data.getIcon();

        icon.setIcon(new FlatSVGIcon("raven/modal/demo/drawer/logo.png", 100, 100));
        data.setTitle("Backup Manager");
        data.setDescription("assistenza@shardpc.it");
        header.setSimpleHeaderData(data);

        rebuildMenu();
    }

    private MyDrawerBuilder() {
        super(createSimpleMenuOption());
        LightDarkButtonFooter lightDarkButtonFooter = (LightDarkButtonFooter) getFooter();
        lightDarkButtonFooter.addModeChangeListener(isDarkMode -> {
            // event for light dark mode changed
        });
    }

    @Override
    public SimpleHeaderData getSimpleHeaderData() {
        AvatarIcon icon = new AvatarIcon(new FlatSVGIcon("raven/modal/demo/drawer/logo.png", 100, 100), 50, 50, 3.5f);
        icon.setType(AvatarIcon.Type.MASK_SQUIRCLE);
        icon.setBorder(2, 2);

        changeAvatarIconBorderColor(icon);

        UIManager.addPropertyChangeListener(evt -> {
            if (evt.getPropertyName().equals("lookAndFeel")) {
                changeAvatarIconBorderColor(icon);
            }
        });

        return new SimpleHeaderData()
                .setIcon(icon)
                .setTitle("Ra Ven")
                .setDescription("raven@gmail.com");
    }

    private void changeAvatarIconBorderColor(AvatarIcon icon) {
        icon.setBorderColor(new AvatarIcon.BorderColor(UIManager.getColor("Component.accentColor"), 0.7f));
    }

    @Override
    public SimpleFooterData getSimpleFooterData() {
        return new SimpleFooterData()
                .setTitle("Swing Modal Dialog")
                .setDescription("Version " + ConfigKey.VERSION.getValue());
    }

    @Override
    public Option createOption() {
        Option option = super.createOption();
        option.setOpacity(0.3f);
        return option;
    }

    public static MenuOption createSimpleMenuOption() {
        // create simple menu option
        MenuOption simpleMenuOption = new MenuOption();

        MenuItem[] items = new MenuItem[]{
                new Item.Label("MAIN"),
                new Item("Backup List", "forms.svg", FormTable.class)
                    .subMenu("Create new backup")
                    .subMenu("Import backups from Csv")
                    .subMenu("Export backups to Csv"),
                new Item("Dashboard", "dashboard.svg", FormDashboard.class),
                new Item.Label("OTHER"),
                new Item("Setting", "setting.svg", FormSetting.class),
                new Item("History", "history.svg"),
                new Item("Information", "info.svg"),
                new Item("Support the Project", "donate.svg")
                    .subMenu("Paypal")
                    .subMenu("Buy me a coffee"),
                new Item("Help", "help.svg")
                    .subMenu("Report a bug")
                    .subMenu("Support"),
                new Item("About", "about.svg"),
        };

        simpleMenuOption.setMenuStyle(new MenuStyle() {

            @Override
            public void styleMenuItem(JButton menu, int[] index, boolean isMainItem) {
                boolean isTopLevel = index.length == 1;
                if (isTopLevel) {
                    // adjust item menu at the top level because it's contain icon
                    menu.putClientProperty(FlatClientProperties.STYLE, "" +
                            "margin:-1,0,-1,0;");
                }
            }

            @Override
            public void styleMenu(JComponent component) {
                component.putClientProperty(FlatClientProperties.STYLE, getDrawerBackgroundStyle());
            }
        });

        simpleMenuOption.getMenuStyle().setDrawerLineStyleRenderer(new DrawerStraightDotLineStyle());
        simpleMenuOption.setMenuValidation(new MyMenuValidation());

        simpleMenuOption.addMenuEvent((action, index) -> {
            System.out.println("Drawer menu selected " + Arrays.toString(index));
            Class<?> itemClass = action.getItem().getItemClass();
            int i = index[0];
            if (i == 7) {
                action.consume();
                FormManager.showAbout();
                return;
            }
            if (itemClass == null || !Form.class.isAssignableFrom(itemClass)) {
                action.consume();
                return;
            }
            Class<? extends Form> formClass = (Class<? extends Form>) itemClass;
            FormManager.showForm(AllForms.getForm(formClass));
        });

        simpleMenuOption.setMenus(items)
                .setBaseIconPath("drawer/icon/")
                .setIconScale(0.45f);

        return simpleMenuOption;
    }

    @Override
    public int getOpenDrawerAt() {
        return 1000;
    }

    @Override
    public boolean openDrawerAtScale() {
        return false;
    }

    @Override
    public void build(DrawerPanel drawerPanel) {
        drawerPanel.putClientProperty(FlatClientProperties.STYLE, getDrawerBackgroundStyle());
        FlatLafStyleUtils.appendStyle(drawerPanel, "border:0,0,0,1,$Separator.foreground;");
    }

    private static String getDrawerBackgroundStyle() {
        return "background:$Menu.background;";
    }
}
