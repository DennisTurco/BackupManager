package backupmanager.gui.component.chart.renderer;

import java.awt.BasicStroke;

import org.jfree.chart.renderer.xy.XYDifferenceRenderer;

import backupmanager.gui.component.chart.themes.DefaultChartTheme;

public class ChartXYDifferenceRenderer extends XYDifferenceRenderer {

    public ChartXYDifferenceRenderer() {
        setPositivePaint(DefaultChartTheme.getColor(0));
        setNegativePaint(DefaultChartTheme.getColor(1));
        setAutoPopulateSeriesStroke(false);
        setDefaultStroke(new BasicStroke(0f));
    }

    @Override
    public String toString() {
        return "Different";
    }
}
