package backupmanager.gui.menu;

import javax.swing.JFrame;

import backupmanager.Managers.LanguageManager;
import backupmanager.interfaces.ITranslatable;
import raven.modal.Drawer;
import raven.modal.drawer.DrawerPanel;
import raven.modal.drawer.item.MenuItem;

public class DrawerManager implements ITranslatable {

    private DrawerPanel drawerPanel;
    private MenuItem[] menuItems;
    private JFrame parent;

    private static DrawerManager instance;

    public static DrawerManager getInstance() {
        if (instance == null) {
            instance = new DrawerManager();
        }
        return instance;
    }

    public void install(JFrame frame) {
        parent = frame;
        MyDrawerBuilder builder = new MyDrawerBuilder();

        drawerPanel = builder.createDrawer();
        menuItems = builder.getSimpleMenuOption().getMenus();

        Drawer.installDrawer(parent, builder);
    }

    private DrawerManager() {
        LanguageManager.register(this);
        rebuildDrawer();
    }

    private void rebuildDrawer() {
        MyDrawerBuilder builder = new MyDrawerBuilder();
        drawerPanel = builder.createDrawer();
    }

     public DrawerPanel getDrawer() {
        return drawerPanel;
    }

    public MenuItem[] getMenuItems() {
        return menuItems;
    }

    public JFrame getParent() {
        return parent;
    }

    // it's not the best rebuild the menu every time the language is changed but right now there is no a method to update
    // the MenuItem title textin the raven library
    @Override
    public void setTranslations() {
        rebuildDrawer();
    }
}
