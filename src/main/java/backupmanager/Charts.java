package backupmanager;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

public class Charts {
    public static void createChart(JPanel panelChart) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(5, "Clicks", "2025-07-10");
        dataset.addValue(8, "Clicks", "2025-07-11");
        dataset.addValue(3, "Clicks", "2025-07-12");

        JFreeChart chart = ChartFactory.createBarChart(
                "Clicks per Giorno", "Data", "Clicks", dataset);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(700, 500));

        panelChart.setLayout(new java.awt.BorderLayout());
        panelChart.removeAll();
        panelChart.add(chartPanel, java.awt.BorderLayout.CENTER);
        panelChart.validate();
    }
}
