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
import backupmanager.Enums.Translations;
import backupmanager.Enums.Translations.TKey;
import backupmanager.Services.BackupAnalyticsService;
import backupmanager.database.Repositories.BackupConfigurationRepository;
import backupmanager.database.Repositories.BackupRequestRepository;
import backupmanager.gui.component.ToolBarSelection;
import backupmanager.gui.component.chart.BarChart;
import backupmanager.gui.component.chart.TimeSeriesChart;
import backupmanager.gui.component.chart.themes.ColorThemes;
import backupmanager.gui.component.chart.themes.DefaultChartTheme;
import backupmanager.gui.component.dashboard.CardBox;
import backupmanager.utils.SystemForm;
import net.miginfocom.swing.MigLayout;

@SystemForm(name = "Backup Dashboard", description = "Backup analytics dashboard")
public class FormBackupDashboard extends CustomForm {

    private static final int CARD_TOTAL_CONFIG = 0;
    private static final int CARD_SUCCESS_RATE = 1;
    private static final int CARD_DURATION = 2;
    private static final int CARD_COMPRESSION = 3;
    private static final int CARD_DISK_USAGE = 4;

    public FormBackupDashboard() {
        build();
    }

    @Override
    protected void init() {
        setLayout(new MigLayout("wrap,fill", "[fill]", "[grow 0][fill]"));
        createTitle();
        createPanelLayout();
        createCard();
        // createDiskUsageChart();
        createExecutionsByMonthChart();
        createAvgDurationChart();
    }

    @Override
    protected void loadData() {
        List<ConfigurationBackup> configurations = BackupConfigurationRepository.getBackupList();
        List<BackupRequest> requests = BackupRequestRepository.getRequestBackups();
        BackupAnalyticsSnapshot snapshot = BackupAnalyticsService.buildSnapshot(requests);

        cardBox.setValueAt(CARD_TOTAL_CONFIG,
                String.valueOf(configurations.size()),
                "",
                "",
                true);

        cardBox.setValueAt(CARD_SUCCESS_RATE,
                String.valueOf(snapshot.totalRequests()),
                "Success rate",
                String.format("%.2f%%", snapshot.successRate()),
                true);

        cardBox.setValueAt(CARD_DURATION,
                String.format("%.2f min", BackupAnalyticsService.convertAvgDurationinMinutes(snapshot)),
                "",
                "",
                true);

        cardBox.setValueAt(CARD_COMPRESSION,
                String.format("%.1f%%", snapshot.avgCompressionRate() * 100),
                "",
                "",
                true);

        durationChart.setDataset(BackupAnalyticsService.buildDurationTrendDataset(snapshot.durationTrend(), Translations.get(TKey.DASHBOARD_CHART_AVG_DURATION)));
        executionsChart.setDataset(BackupAnalyticsService.buildRequestsPerMonthDataset(requests, Translations.get(TKey.DASHBOARD_CHART_EXECUTIONS)));
    }

    protected void createTitle() {

        JPanel panel = new JPanel(new MigLayout("fillx", "[]push[][]"));

        title = new JLabel("Backup Analytics Dashboard");
        title.putClientProperty(FlatClientProperties.STYLE,
                "font:bold +3");

        ToolBarSelection<ColorThemes> toolBarSelection =
                new ToolBarSelection<>(ColorThemes.values(), colorThemes -> {

                    if (DefaultChartTheme.setChartColors(colorThemes)) {

                        DefaultChartTheme.applyTheme(durationChart.getFreeChart());
                        DefaultChartTheme.applyTheme(executionsChart.getFreeChart());

                        cardBox.setCardIconColor(0, DefaultChartTheme.getColor(0));
                        cardBox.setCardIconColor(1, DefaultChartTheme.getColor(1));
                        cardBox.setCardIconColor(2, DefaultChartTheme.getColor(2));
                        cardBox.setCardIconColor(3, DefaultChartTheme.getColor(3));
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
                createIcon("icons/dashboard/database.svg", DefaultChartTheme.getColor(CARD_TOTAL_CONFIG)),
                "Total Backup Configurations");

        cardBox.addCardItem(
                createIcon("icons/dashboard/run.svg", DefaultChartTheme.getColor(CARD_SUCCESS_RATE)),
                "Total Backup Executions");

        cardBox.addCardItem(
                createIcon("icons/dashboard/duration.svg", DefaultChartTheme.getColor(CARD_DURATION)),
                "Avg Backup Duration");

        cardBox.addCardItem(
                createIcon("icons/dashboard/rate.svg", DefaultChartTheme.getColor(CARD_COMPRESSION)),
                "Compression Rate");

        panel.add(cardBox);
        panelLayout.add(panel);
    }

    private JPanel createChartPanel(int height) {
        return new JPanel(new MigLayout("gap 14,wrap,fillx", "[fill]", "[" + height + "]"));
    }

    private void createAvgDurationChart() {
        JPanel panel = createChartPanel(350);
        durationChart = new TimeSeriesChart();
        panel.add(durationChart);
        panelLayout.add(panel);
    }

    private void createExecutionsByMonthChart() {
        JPanel panel = createChartPanel(350);
        executionsChart = new BarChart();
        panel.add(executionsChart);
        panelLayout.add(panel);
    }


    private Icon createIcon(String icon, Color color) {
        return new FlatSVGIcon(icon, 20, 20).setColorFilter(new FlatSVGIcon.ColorFilter(color1 -> color));
    }

    @Override
    public void setTranslations() {
        title.setText(Translations.get(TKey.DASHBOARD_TITLE));
        cardBox.setTitleTextAt(CARD_TOTAL_CONFIG, Translations.get(TKey.DASHBOARD_CARD_TOTAL_CONFIGURATIONS));
        cardBox.setTitleTextAt(CARD_SUCCESS_RATE, Translations.get(TKey.DASHBOARD_CARD_TOTAL_EXECUTIONS));
        cardBox.setTitleTextAt(CARD_DURATION, Translations.get(TKey.DASHBOARD_CARD_AVG_DURATION));
        cardBox.setTitleTextAt(CARD_COMPRESSION, Translations.get(TKey.DASHBOARD_CARD_COMPRESSION_RATE));
        cardBox.setDescriptionTextAt(CARD_SUCCESS_RATE, Translations.get(TKey.DASHBOARD_CARD_SUCCESS_RATE));
    }

    private JLabel title;

    private JPanel panelLayout;
    private CardBox cardBox;

    private TimeSeriesChart durationChart;
    private BarChart  executionsChart;

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
