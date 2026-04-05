package backupmanager.gui.system;

import javax.swing.JFrame;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.util.ColorFunctions;

import backupmanager.Enums.Translations;
import backupmanager.Enums.Translations.TKey;
import backupmanager.gui.component.About;
import backupmanager.gui.component.Subscription;
import backupmanager.gui.forms.FormBackupTable;
import backupmanager.gui.forms.FormLogin;
import backupmanager.utils.UndoRedo;
import raven.modal.Drawer;
import raven.modal.ModalDialog;
import raven.modal.component.SimpleModalBorder;

public class FormManager {

    protected static final UndoRedo<Form> FORMS = new UndoRedo<>();
    private static JFrame frame;
    private static MainForm mainForm;
    private static FormLogin login;

    public static void install(JFrame f) {
        frame = f;
        install();
        logout();
    }

    private static void install() {
        FormSearch.getInstance().installKeyMap(getMainForm());
        FlatSVGIcon.ColorFilter.getInstance().setMapperEx((component, color) -> {
            if (color.getRGB() == -6908266) {
                return FlatLaf.isLafDark() ? ColorFunctions.shade(component.getForeground(), 0.2f)
                        : ColorFunctions.tint(component.getForeground(), 0.4f);
            }
            return color;
        });
    }

    public static void showForm(Form form) {
        if (form != FORMS.getCurrent()) {
            FORMS.add(form);
            form.formCheck();
            form.formOpen();
            mainForm.setForm(form);
            mainForm.refresh();
        }
    }

    public static void undo() {
        if (FORMS.isUndoAble()) {
            Form form = FORMS.undo();
            form.formCheck();
            form.formOpen();
            mainForm.setForm(form);
            Drawer.setSelectedItemClass(form.getClass());
        }
    }

    public static void redo() {
        if (FORMS.isRedoAble()) {
            Form form = FORMS.redo();
            form.formCheck();
            form.formOpen();
            mainForm.setForm(form);
            Drawer.setSelectedItemClass(form.getClass());
        }
    }

    public static void refresh() {
        if (FORMS.getCurrent() != null) {
            FORMS.getCurrent().formRefresh();
            mainForm.refresh();
        }
    }

    public static void login() {
        Drawer.setVisible(true);
        frame.getContentPane().removeAll();
        frame.getContentPane().add(getMainForm());

        Drawer.setSelectedItemClass(FormBackupTable.class);
        frame.repaint();
        frame.revalidate();
    }

    public static void logout() {
        Drawer.setVisible(false);
        frame.getContentPane().removeAll();
        Form login = getLogin();
        login.formCheck();
        frame.getContentPane().add(login);
        FORMS.clear();
        frame.repaint();
        frame.revalidate();
    }

    public static JFrame getFrame() {
        return frame;
    }

    private static MainForm getMainForm() {
        if (mainForm == null) {
            mainForm = new MainForm();
        }
        return mainForm;
    }

    private static FormLogin getLogin() {
        if (login == null) {
            login = new FormLogin();
        }
        return login;
    }

    public static void showAbout() {
        ModalDialog.showModal(frame, new SimpleModalBorder(new About(), Translations.get(TKey.ABOUT)),
                ModalDialog.createOption().setAnimationEnabled(false)
        );
    }

    public static void showSubscription() {
        ModalDialog.showModal(frame, new SimpleModalBorder(new Subscription(), Translations.get(TKey.SUBSCRIPTION)),
                ModalDialog.createOption().setAnimationEnabled(false)
        );
    }
}
