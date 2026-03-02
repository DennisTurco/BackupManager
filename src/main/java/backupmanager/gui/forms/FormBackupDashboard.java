package backupmanager.gui.forms;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.util.UIScale;

import backupmanager.Entities.BackupAnalyticsSnapshot;
import backupmanager.Entities.BackupRequest;
import backupmanager.Entities.ConfigurationBackup;
import backupmanager.Services.BackupAnalyticsService;
import backupmanager.database.Repositories.BackupConfigurationRepository;
import backupmanager.database.Repositories.BackupRequestRepository;
import backupmanager.gui.component.ToolBarSelection;
import backupmanager.gui.component.chart.BarChart;
import backupmanager.gui.component.chart.PieChart;
import backupmanager.gui.component.chart.SpiderChart;
import backupmanager.gui.component.chart.TimeSeriesChart;
import backupmanager.gui.component.chart.themes.ColorThemes;
import backupmanager.gui.component.chart.themes.DefaultChartTheme;
import backupmanager.gui.component.dashboard.CardBox;
import backupmanager.gui.system.Form;
import backupmanager.utils.SystemForm;
import net.miginfocom.swing.MigLayout;

@SystemForm(name = "Backup Dashboard", description = "Backup analytics dashboard")
public class FormBackupDashboard extends Form {

    public FormBackupDashboard() {
        init();
    }

    private void init() {
        setLayout(new MigLayout("wrap,fill", "[fill]", "[grow 0][fill]"));
        createTitle();
        createPanelLayout();
        createCard();
        createDiskUsageChart();
        createExecutionsByMonthChart();
        createAvgDurationChart();
    }

    @Override
    public void formInit() {
        loadData();
    }

    @Override
    public void formRefresh() {
        loadData();
    }

    private void loadData() {
        List<ConfigurationBackup> configurations = BackupConfigurationRepository.getBackupList();
        List<BackupRequest> requests = BackupRequestRepository.getRequestBackups();
        BackupAnalyticsSnapshot snapshot = BackupAnalyticsService.buildSnapshot(requests);

        cardBox.setValueAt(0,
                String.valueOf(configurations.size()),
                "Total Backup Configurations",
                "",
                true);

        cardBox.setValueAt(1,
                String.valueOf(snapshot.totalRequests()),
                "Total Backup Executions",
                snapshot.successRate() + "%",
                true);

        cardBox.setValueAt(2,
                BackupAnalyticsService.formatBytes(snapshot.totalDiskUsageBytes()),
                "Disk Usage",
                "",
                true);

        cardBox.setValueAt(3,
                String.format("%.2f min", BackupAnalyticsService.convertAvgDurationinMinutes(snapshot)),
                "Avg Backup Duration",
                "",
                true);

        cardBox.setValueAt(4,
                String.format("%.1f%%", snapshot.avgCompressionRate() * 100),
                "Compression Rate",
                "",
                true);

        timeSeriesChart.setDataset(BackupAnalyticsService.buildDurationTrendDataset(snapshot.durationTrend()));
    }

    private void createTitle() {

        JPanel panel = new JPanel(new MigLayout("fillx", "[]push[][]"));

        JLabel title = new JLabel("Backup Analytics Dashboard");
        title.putClientProperty(FlatClientProperties.STYLE,
                "font:bold +3");

        ToolBarSelection<ColorThemes> toolBarSelection =
                new ToolBarSelection<>(ColorThemes.values(), colorThemes -> {

                    if (DefaultChartTheme.setChartColors(colorThemes)) {

                        DefaultChartTheme.applyTheme(timeSeriesChart.getFreeChart());
                        DefaultChartTheme.applyTheme(barChart.getFreeChart());
                        DefaultChartTheme.applyTheme(pieChart.getFreeChart());
                        DefaultChartTheme.applyTheme(spiderChart.getFreeChart());

                        cardBox.setCardIconColor(0, DefaultChartTheme.getColor(0));
                        cardBox.setCardIconColor(1, DefaultChartTheme.getColor(1));
                        cardBox.setCardIconColor(2, DefaultChartTheme.getColor(2));
                        cardBox.setCardIconColor(3, DefaultChartTheme.getColor(3));
                        cardBox.setCardIconColor(4, DefaultChartTheme.getColor(4));
                    }
                });

        panel.add(title);
        panel.add(toolBarSelection);
        add(panel);
    }

    private void createPanelLayout() {

        panelLayout = new JPanel(new DashboardLayout());

        JScrollPane scrollPane = new JScrollPane(panelLayout);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        scrollPane.setHorizontalScrollBarPolicy(
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        scrollPane.getVerticalScrollBar().setUnitIncrement(10);

        scrollPane.getVerticalScrollBar().putClientProperty(
                FlatClientProperties.STYLE,
                "width:5;" +
                        "trackArc:$ScrollBar.thumbArc;" +
                        "trackInsets:0,0,0,0;" +
                        "thumbInsets:0,0,0,0;");

        add(scrollPane);
    }


    private void createCard() {

        JPanel panel = new JPanel(new MigLayout("fillx", "[fill]"));

        cardBox = new CardBox();

        cardBox.addCardItem(
                createIcon("icons/dashboard/database.svg", DefaultChartTheme.getColor(0)),
                "Total Backup Configurations");

        cardBox.addCardItem(
                createIcon("icons/dashboard/run.svg", DefaultChartTheme.getColor(1)),
                "Total Backup Executions");

        cardBox.addCardItem(
                createIcon("icons/dashboard/usage.svg", DefaultChartTheme.getColor(2)),
                "Disk Usage");

        cardBox.addCardItem(
                createIcon("icons/dashboard/duration.svg", DefaultChartTheme.getColor(3)),
                "Avg Backup Duration");

        cardBox.addCardItem(
                createIcon("icons/dashboard/rate.svg", DefaultChartTheme.getColor(4)),
                "Compression Rate");

        panel.add(cardBox);
        panelLayout.add(panel);
    }

    private void createAvgDurationChart() {

        JPanel panel = new JPanel(
                new MigLayout("gap 14,wrap,fillx", "[fill]", "[350]"));

        timeSeriesChart = new TimeSeriesChart();
        barChart = new BarChart();

        panel.add(timeSeriesChart);
        panel.add(barChart);

        panelLayout.add(panel);
    }

    private void createDiskUsageChart() {

        JPanel panel = new JPanel(
                new MigLayout("gap 14,wrap,fillx", "[fill]", "[350]"));

        spiderChart = new SpiderChart();
        pieChart = new PieChart();

        panel.add(spiderChart);
        panel.add(pieChart);

        panelLayout.add(panel);
    }

    private void createExecutionsByMonthChart() {

        JPanel panel = new JPanel(
                new MigLayout("fillx,gap 14", "[fill,300::]", "[300]"));

        timeSeriesChart = new TimeSeriesChart();
        barChart = new BarChart();

        panel.add(timeSeriesChart);
        panel.add(barChart);

        panelLayout.add(panel);
    }


    private Icon createIcon(String icon, Color color) {
        return new FlatSVGIcon(icon, 20, 20).setColorFilter(new FlatSVGIcon.ColorFilter(color1 -> color));
    }

    private JPanel panelLayout;
    private CardBox cardBox;

    private TimeSeriesChart timeSeriesChart;
    private BarChart barChart;
    private SpiderChart spiderChart;
    private PieChart pieChart;

    private class DashboardLayout implements LayoutManager {

        private final int gap = 0;

        @Override
        public void addLayoutComponent(String name, Component comp) {}

        @Override
        public void removeLayoutComponent(Component comp) {}

        @Override
        public Dimension preferredLayoutSize(Container parent) {

            synchronized (parent.getTreeLock()) {

                Insets insets = parent.getInsets();

                int width = insets.left + insets.right;
                int height = insets.top + insets.bottom;

                int g = UIScale.scale(gap);

                int count = parent.getComponentCount();

                for (int i = 0; i < count; i++) {
                    Component com = parent.getComponent(i);
                    Dimension size = com.getPreferredSize();
                    height += size.height;
                }

                if (count > 1) {
                    height += (count - 1) * g;
                }

                return new Dimension(width, height);
            }
        }

        @Override
        public Dimension minimumLayoutSize(Container parent) {
            return new Dimension(10, 10);
        }

        @Override
        public void layoutContainer(Container parent) {

            synchronized (parent.getTreeLock()) {

                Insets insets = parent.getInsets();

                int x = insets.left;
                int y = insets.top;

                int width = parent.getWidth() -
                        (insets.left + insets.right);

                int g = UIScale.scale(gap);

                int count = parent.getComponentCount();

                for (int i = 0; i < count; i++) {

                    Component com = parent.getComponent(i);
                    Dimension size = com.getPreferredSize();

                    com.setBounds(x, y, width, size.height);

                    y += size.height + g;
                }
            }
        }
    }
}
