package backupmanager.Utils;

import java.awt.Component;
import raven.modal.Toast;
import raven.modal.option.Location;
import raven.modal.toast.option.ToastBorderStyle;
import raven.modal.toast.option.ToastLocation;
import raven.modal.toast.option.ToastOption;
import raven.modal.toast.option.ToastStyle;

public class ToastUtils {
    public static void showDefault(Component owner, String text) {
        showToast(owner, Toast.Type.DEFAULT, text);
    }

    public static void showInfo(Component owner, String text) {
        showToast(owner, Toast.Type.INFO, text);
    }

    public static void showSuccess(Component owner, String text) {
        showToast(owner, Toast.Type.SUCCESS, text);
    }

    public static void showWarning(Component owner, String text) {
        showToast(owner, Toast.Type.WARNING, text);
    }

    public static void showError(Component owner, String text) {
        showToast(owner, Toast.Type.ERROR, text);
    }

    private static void showToast(Component owner, Toast.Type type, String text) {
        Toast.show(owner, type, text, getSelectedOption());
    }

    private static ToastOption getSelectedOption() {
        ToastOption option = Toast.createOption();
        Location h = Location.CENTER;
        Location v = Location.TOP;
        ToastStyle.BackgroundType backgroundType = ToastStyle.BackgroundType.DEFAULT;
        ToastBorderStyle.BorderType borderType = ToastBorderStyle.BorderType.LEADING_LINE;
        option.setAnimationEnabled(true)
                .setPauseDelayOnHover(true)
                .setAutoClose(true)
                .setCloseOnClick(true)
                .setHeavyWeight(false);

        option.getLayoutOption()
                .setLocation(ToastLocation.from(h, v))
                .setRelativeToOwner(false);
        option.getStyle().setBackgroundType(backgroundType)
                .setShowIcon(true)
                .setShowLabel(false)
                .setIconSeparateLine(false)
                .setShowCloseButton(true)
                .setPaintTextColor(false)
                .setPromiseLabel("Saving...")
                .getBorderStyle()
                .setBorderType(borderType)
        ;
        return option;
    }
}
