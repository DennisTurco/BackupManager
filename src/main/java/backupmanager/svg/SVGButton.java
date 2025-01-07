
package backupmanager.svg;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import java.awt.Color;
import java.awt.Cursor;
import javax.swing.JButton;

public class SVGButton extends JButton {

    private FlatSVGIcon svgIcon;
    private String svgImagePath;
    private int svgWidth, svgHeight;

    public SVGButton() {
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public void setSvgImage(String image, int width, int height) {
        this.svgImagePath = image;
        this.svgWidth = width;
        this.svgHeight = height;

        applySvgColor();
    }
    
    // to update dinamically the svg color
    public void applySvgColor() {
        if (svgImagePath == null) return;

        // Ottieni il colore contrastante in base al tema corrente
        Color contrastColor = getContrastingColor(getBackground());

        // Crea un nuovo FlatSVGIcon
        svgIcon = new FlatSVGIcon(svgImagePath, svgWidth, svgHeight);

        // Applica il filtro colore
        svgIcon.setColorFilter(new FlatSVGIcon.ColorFilter() {
            @Override
            public Color filter(Color color) {
                return contrastColor; // Applica il colore contrastante
            }
        });

        // Imposta l'icona sul bottone
        setIcon(svgIcon);
    }

    private Color getContrastingColor(Color bgColor) {
        int brightness = (int) Math.sqrt(
            bgColor.getRed() * bgColor.getRed() * 0.241 +
            bgColor.getGreen() * bgColor.getGreen() * 0.691 +
            bgColor.getBlue() * bgColor.getBlue() * 0.068
        );

        return (brightness > 130) ? Color.BLACK : Color.WHITE;
    }
}
