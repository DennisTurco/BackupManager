package backupmanager.gui.component.chart.renderer;

import org.jfree.chart.renderer.xy.XYSplineRenderer;

import com.formdev.flatlaf.util.UIScale;

public class ChartXYCurveRenderer extends XYSplineRenderer {

    private static final int precision = 10;

    public ChartXYCurveRenderer() {
        this(UIScale.scale(precision));
    }

    public ChartXYCurveRenderer(int precision) {
        super(precision);
        initStyle();
    }

    private void initStyle() {
        setAutoPopulateSeriesOutlinePaint(true);
        setAutoPopulateSeriesOutlineStroke(true);
        setUseOutlinePaint(true);
    }

    @Override
    public String toString() {
        return "Curve";
    }
}
