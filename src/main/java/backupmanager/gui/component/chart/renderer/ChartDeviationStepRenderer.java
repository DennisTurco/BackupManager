package backupmanager.gui.component.chart.renderer;

import java.awt.BasicStroke;

import org.jfree.chart.renderer.xy.DeviationStepRenderer;

import com.formdev.flatlaf.util.UIScale;

public class ChartDeviationStepRenderer extends DeviationStepRenderer {

    public ChartDeviationStepRenderer() {
        initStyle();
    }

    private void initStyle() {
        setAutoPopulateSeriesOutlinePaint(true);
        setDefaultOutlineStroke(new BasicStroke(UIScale.scale(6f)));
        setUseOutlinePaint(true);
    }

    @Override
    public String toString() {
        return "Deviation Step";
    }
}
