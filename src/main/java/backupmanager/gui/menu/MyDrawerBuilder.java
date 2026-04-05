package backupmanager.gui.menu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.UIManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import backupmanager.Entities.ConfigurationBackup;
import backupmanager.Entities.Configurations;
import backupmanager.Enums.ConfigKey;
import backupmanager.Enums.MenuItems;
import backupmanager.Enums.Translations;
import backupmanager.Enums.Translations.TKey;
import backupmanager.Json.JsonConfig;
import backupmanager.Managers.ExportManager;
import backupmanager.Managers.WebsiteManager;
import backupmanager.database.Repositories.BackupConfigurationRepository;
import backupmanager.gui.forms.FormBackupDashboard;
import backupmanager.gui.forms.FormBackupTable;
import backupmanager.gui.forms.FormHistory;
import backupmanager.gui.forms.FormSetting;
import backupmanager.gui.system.AllForms;
import backupmanager.gui.system.FormManager;
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
    private static final Logger logger = LoggerFactory.getLogger(MyDrawerBuilder.class);

    private static final Map<MenuItems, Runnable> menuActionMap = new HashMap<>();
    private static final Map<String, MenuItems> menuBindingMap = new HashMap<>();
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

        icon.setIcon(new FlatSVGIcon("drawer/logo.svg", 100, 100));
        data.setTitle("Backup Manager");
        data.setDescription(ConfigKey.EMAIL.getValue());
        header.setSimpleHeaderData(data);

        rebuildMenu();
    }

    private MyDrawerBuilder() {
        super(createSimpleMenuOption());
        initMenuActions();
        LightDarkButtonFooter lightDarkButtonFooter = (LightDarkButtonFooter) getFooter();
        lightDarkButtonFooter.addModeChangeListener(isDarkMode -> {
            // event for light dark mode changed
        });
    }

    @Override
    public SimpleHeaderData getSimpleHeaderData() {
        AvatarIcon icon = new AvatarIcon(new FlatSVGIcon("drawer/logo.svg", 100, 100), 50, 50, 3.5f);
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
                .setTitle("Backup Manager")
                .setDescription(ConfigKey.EMAIL.getValue());
    }

    private static void initMenuActions() {
        menuActionMap.put(MenuItems.BackupList, () ->
                FormManager.showForm(AllForms.getForm(FormBackupTable.class)));

        menuActionMap.put(MenuItems.Dashboard, () ->
                FormManager.showForm(AllForms.getForm(FormBackupDashboard.class)));

        menuActionMap.put(MenuItems.Export, () ->
                ExportManager.exportAsCSV(BackupConfigurationRepository.getBackupList(), ConfigurationBackup.getCSVHeader()));

        menuActionMap.put(MenuItems.Settings, () ->
                FormManager.showForm(AllForms.getForm(FormSetting.class)));

        menuActionMap.put(MenuItems.History, () ->
                FormManager.showForm(AllForms.getForm(FormHistory.class)));

        menuActionMap.put(MenuItems.InfoPage, () ->
                WebsiteManager.openWebSite(ConfigKey.INFO_PAGE_LINK.getValue()));

        menuActionMap.put(MenuItems.BugReport, () ->
                WebsiteManager.openWebSite(ConfigKey.ISSUE_PAGE_LINK.getValue()));

        menuActionMap.put(MenuItems.ContactUs, () ->
                WebsiteManager.openWebSite(ConfigKey.EMAIL.getValue()));

        menuActionMap.put(MenuItems.PaypalDonate, () ->
                WebsiteManager.openWebSite(ConfigKey.DONATE_PAYPAL_LINK.getValue()));

        menuActionMap.put(MenuItems.BuymeacoffeeDonate, () ->
                WebsiteManager.openWebSite(ConfigKey.DONATE_BUYMEACOFFE_LINK.getValue()));

        menuActionMap.put(MenuItems.Subscription, () ->
                FormManager.showSubscription());

        menuActionMap.put(MenuItems.About, () ->
                FormManager.showAbout());
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
        MenuOption simpleMenuOption = new MenuOption();
        MenuItem[] items = buildMenuItems().toArray(MenuItem[]::new);

        simpleMenuOption.setMenuStyle(new MenuStyle() {

            @Override
            public void styleMenuItem(JButton menu, int[] index, boolean isMainItem) {
                boolean isTopLevel = index.length == 1;

                if (isTopLevel) {

                    if (menu.getIcon() instanceof FlatSVGIcon svgIcon) {
                        FlatSVGIcon newIcon = new FlatSVGIcon(svgIcon.getName(), 20, 20);
                        menu.setIcon(newIcon);
                    }

                    menu.putClientProperty(FlatClientProperties.STYLE, "margin:-1,0,-1,0;");
                }
            }

            @Override
            public void styleMenu(JComponent component) {
                component.putClientProperty(FlatClientProperties.STYLE,
                        getDrawerBackgroundStyle());
            }
        });

        simpleMenuOption.getMenuStyle().setDrawerLineStyleRenderer(new DrawerStraightDotLineStyle());
        simpleMenuOption.setMenuValidation(new MyMenuValidation());

        simpleMenuOption.addMenuEvent((action, index) -> {
            logger.debug("Drawer menu selected " + Arrays.toString(index));
            MenuItems menuItem = resolveMenuItem(action);

            if (menuItem != null && menuActionMap.containsKey(menuItem)) {
                menuActionMap.get(menuItem).run();
                action.consume();
            }
        });

        simpleMenuOption.setMenus(items).setBaseIconPath("drawer/icon/");

        return simpleMenuOption;
    }

    private static Item createMenuItem(String label, String icon, MenuItems menuEnum, Class<?> formClass) {
        Item item = new Item(label, icon, formClass);
        menuBindingMap.put(label.replace(" ", "").toLowerCase(), menuEnum);
        return item;
    }

    private static MenuItems resolveMenuItem(raven.modal.drawer.menu.MenuAction action) {
        String name = action.getItem().getName();

        if (name == null)
            return null;

        String key = name.replace(" ", "").toLowerCase();
        return menuBindingMap.get(key);
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

    private static List<MenuItem> buildMenuItems() {
        JsonConfig config = JsonConfig.getInstance();
        List<MenuItem> itemList = new ArrayList<>();

        itemList.add(new Item.Label(Translations.get(TKey.SUBMENU_MAIN)));

        // Backup menu
        Item backupItem = createMenuItem(Translations.get(TKey.BACKUP_TABLE), "forms.svg", MenuItems.BackupList, FormBackupTable.class);

        if (config.isMenuItemEnabled(MenuItems.Import.name()))
            backupItem.subMenu(Translations.get(TKey.IMPORT_BACKUP));

        if (config.isMenuItemEnabled(MenuItems.Export.name()))
            backupItem.subMenu(Translations.get(TKey.EXPORT_BACKUP));

        itemList.add(backupItem);

        // Dashboard
        itemList.add(createMenuItem(Translations.get(TKey.DASHBOARD), "dashboard.svg", MenuItems.Dashboard, FormBackupDashboard.class));

        itemList.add(new Item.Label(Translations.get(TKey.SUBMENU_OTHER)));

        // Settings
        itemList.add(createMenuItem(Translations.get(TKey.OPTIONS), "setting.svg", MenuItems.Settings, FormSetting.class));

        // History (configurable)
        if (config.isMenuItemEnabled(MenuItems.History.name())) {
            itemList.add(createMenuItem(Translations.get(TKey.HISTORY), "history.svg", MenuItems.History, FormHistory.class));
        }

        // External link (no form class)
        itemList.add(createMenuItem(Translations.get(TKey.GITHUB_PAGE), "github.svg", MenuItems.InfoPage, null));

        // Donate section
        if (config.isMenuItemEnabled(MenuItems.Donate.name())) {

            Item donateItem = createMenuItem(Translations.get(TKey.DONATE), "donate.svg", MenuItems.Donate, null);

            if (config.isMenuItemEnabled(MenuItems.PaypalDonate.name())) {
                donateItem.subMenu(Translations.get(TKey.PAYPAL));
                bindSubMenu(Translations.get(TKey.PAYPAL), MenuItems.PaypalDonate);
            }

            if (config.isMenuItemEnabled(MenuItems.BuymeacoffeeDonate.name())) {
                donateItem.subMenu(Translations.get(TKey.BUYMEACOFFE));
                bindSubMenu(Translations.get(TKey.BUYMEACOFFE), MenuItems.BuymeacoffeeDonate);
            }

            itemList.add(donateItem);
        }

        // Support section
        if (config.isMenuItemEnabled(MenuItems.Support.name())) {

            Item helpItem = createMenuItem(Translations.get(TKey.HELP), "help.svg", MenuItems.Support, null);

            if (config.isMenuItemEnabled(MenuItems.BugReport.name())) {
                helpItem.subMenu(Translations.get(TKey.BUG_REPORT));
                bindSubMenu(Translations.get(TKey.BUG_REPORT), MenuItems.BugReport);
            }

            if (config.isMenuItemEnabled(MenuItems.ContactUs.name())) {
                helpItem.subMenu(Translations.get(TKey.CONTACT_US));
                bindSubMenu(Translations.get(TKey.CONTACT_US), MenuItems.ContactUs);
            }

            itemList.add(helpItem);
        }

        if (Configurations.isSubscriptionNedded()) {
            itemList.add(createMenuItem(Translations.get(TKey.SUBSCRIPTION), "subscription.svg", MenuItems.Subscription, null));
        }

        // About (LAST ITEM)
        itemList.add(createMenuItem(Translations.get(TKey.ABOUT), "about.svg", MenuItems.About, null));

        return itemList;
    }

    private static void bindSubMenu(String label, MenuItems menuEnum) {
        menuBindingMap.put(label.replace(" ", "").toLowerCase(), menuEnum);
    }
}
