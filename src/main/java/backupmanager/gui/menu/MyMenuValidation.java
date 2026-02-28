package backupmanager.gui.menu;

import raven.modal.Drawer;
import backupmanager.gui.system.Form;
import raven.modal.drawer.menu.MenuValidation;

public class MyMenuValidation extends MenuValidation {
    public static boolean validation(Class<? extends Form> itemClass) {
        int[] index = Drawer.getMenuIndexClass(itemClass);
        return index != null;
    }
}
