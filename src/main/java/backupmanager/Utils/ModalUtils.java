package backupmanager.Utils;

import java.awt.Component;

import raven.modal.ModalDialog;
import raven.modal.option.BorderOption;
import raven.modal.option.Location;
import raven.modal.option.Option;

public class ModalUtils {

    public static void showDefault(Component owner, String title, String message, int options) {
        showCustomModal(owner, SimpleMessageModal.Type.DEFAULT, title, message, options);
    }

    public static void showInfo(Component owner, String title, String message, int options) {
        showCustomModal(owner, SimpleMessageModal.Type.INFO, title, message, options);
    }

    public static void showSuccess(Component owner, String title, String message, int options) {
        showCustomModal(owner, SimpleMessageModal.Type.SUCCESS, title, message, options);
    }

    public static void showWarning(Component owner, String title, String message, int options) {
        showCustomModal(owner, SimpleMessageModal.Type.WARNING, title, message, options);
    }

    public static void showError(Component owner, String title, String message, int options) {
        showCustomModal(owner, SimpleMessageModal.Type.ERROR, title, message, options);
    }

    private static void showCustomModal(Component owner, SimpleMessageModal.Type type, String title, String message, int options) {
        SimpleMessageModal modal = new SimpleMessageModal(type, message, title, options, null);
        ModalDialog.showModal(owner, modal, getSelectedOption());
    }

    private static Option getSelectedOption() {
        Option option = ModalDialog.createOption();
        float scale = 0.1f;
        Location h = Location.CENTER;
        Location v = Location.CENTER;
        Option.BackgroundClickType backgroundClickType = Option.BackgroundClickType.BLOCK;
        option.setAnimationEnabled(true)
                .setCloseOnPressedEscape(true)
                .setBackgroundClickType(backgroundClickType)
                .setOpacity(0.5f)
                .setHeavyWeight(false);
        option.getBorderOption()
                .setBorderWidth(0)
                .setShadow(BorderOption.Shadow.NONE);
        option.getLayoutOption().setLocation(h, v)
                .setRelativeToOwner(false)
                .setMovable(false);
        if (scale != 0) {
            option.getLayoutOption().setAnimateDistance(0, 0)
                    .setAnimateScale(scale);
        }
        return option;
    }
}
