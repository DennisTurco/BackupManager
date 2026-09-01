package backupmanager.gui.component.chart.utils;

import java.awt.Color;
import java.awt.Font;

import javax.swing.UIManager;

import org.jfree.chart.plot.Crosshair;

import backupmanager.gui.component.chart.themes.ChartDrawingSupplier;

public class CustomCrosshairToolTip extends Crosshair {

    public CustomCrosshairToolTip() {
        init();
    }

    private void init() {
        installStyle();
    }

    public void installStyle() {
        Color background = UIManager.getColor("Panel.background");
        Color foreground = UIManager.getColor("Label.foreground");
        Color border = UIManager.getColor("Component.borderColor");
        Font font = UIManager.getFont("Label.font");
        setLabelBackgroundPaint(ChartDrawingSupplier.alpha(background, 0.7f));
        setLabelPaint(foreground);
        setLabelOutlinePaint(border);
        setLabelFont(font);
        setPaint(ChartDrawingSupplier.alpha(foreground, 0.5f));
        setStroke(ChartDrawingSupplier.getDefaultGridlineStroke());
        // setLabelPadding(ChartDrawingSupplier.scaleRectangleInsets(2, 5, 2, 5));
    }
}
