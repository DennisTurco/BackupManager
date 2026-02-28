package backupmanager.component.chart.utils;

import org.jfree.chart.renderer.xy.XYItemRenderer;
import backupmanager.component.ToolBarSelection;
import backupmanager.component.chart.TimeSeriesChart;
import backupmanager.component.chart.renderer.*;

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
