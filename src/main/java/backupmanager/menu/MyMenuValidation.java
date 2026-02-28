package backupmanager.menu;

import raven.modal.Drawer;
import backupmanager.system.Form;
import raven.modal.drawer.menu.MenuValidation;

public class MyMenuValidation extends MenuValidation {
    public static boolean validation(Class<? extends Form> itemClass) {
        int[] index = Drawer.getMenuIndexClass(itemClass);
        return index != null;
    }
}
