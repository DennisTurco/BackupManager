package backupmanager.gui.component.chart.utils;

import org.jfree.chart.renderer.xy.XYItemRenderer;

import backupmanager.gui.component.ToolBarSelection;
import backupmanager.gui.component.chart.TimeSeriesChart;
import backupmanager.gui.component.chart.renderer.ChartDeviationStepRenderer;
import backupmanager.gui.component.chart.renderer.ChartStackedXYBarRenderer;
import backupmanager.gui.component.chart.renderer.ChartXYBarRenderer;
import backupmanager.gui.component.chart.renderer.ChartXYDifferenceRenderer;

public class ToolBarTimeSeriesChartRenderer extends ToolBarSelection<XYItemRenderer> {

    public ToolBarTimeSeriesChartRenderer(TimeSeriesChart chart) {
        super(getRenderers(), renderer -> {
            chart.setRenderer(renderer);
        });
    }

    private static XYItemRenderer[] getRenderers() {
        XYItemRenderer[] renderers = new XYItemRenderer[]{
                // new ChartXYCurveRenderer(),
                // new ChartXYLineRenderer(),
                new ChartXYBarRenderer(),
                new ChartStackedXYBarRenderer(),
                new ChartDeviationStepRenderer(),
                new ChartXYDifferenceRenderer()
        };
        return renderers;
    }
}
