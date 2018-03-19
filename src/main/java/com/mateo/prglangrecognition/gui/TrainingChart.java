package com.mateo.prglangrecognition.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import static java.awt.BorderLayout.CENTER;
import static org.jfree.chart.ChartFactory.createXYLineChart;

public class TrainingChart extends JFrame {
    public TrainingChart(int w, int h, List<Double> data) {
        super("Network training chart");
        this.data = data;
        this.width = w;
        this.height = h;

        panel = (ChartPanel) createChartPanel();
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        add(panel, CENTER);
        setPreferredSize(new Dimension(w, h));
        pack();
    }

    private XYDataset createDataset() {
        XYSeriesCollection dataset = new XYSeriesCollection();
        XYSeries series = new XYSeries("Network");
        for (int i = 1; i <= data.size(); i++)
            series.add((double) i, data.get(i - 1));

        dataset.addSeries(series);
        return dataset;
    }

    private JPanel createChartPanel() {
        String chartTitle = "Net. training chart";
        String yAxisLabel = "Net. error";
        String xAxisLabel = "Epoch";

        XYDataset dataset = createDataset();
        JFreeChart chart = createXYLineChart(chartTitle,
                xAxisLabel, yAxisLabel, dataset);

        return new ChartPanel(chart);
    }

    public ChartPanel getChartPanel() {
        return panel;
    }

    private int width, height;
    private List<Double> data;
    private ChartPanel panel;
}
