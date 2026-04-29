package backupmanager.gui.Controllers;

import java.awt.Image;

import javax.swing.ImageIcon;

import backupmanager.Enums.ConfigKey;

public class GuiController {
    public static Image getIcon(Class<?> obj) {
        return new ImageIcon(obj.getResource(ConfigKey.LOGO_IMG.getValue())).getImage();
    }
}
