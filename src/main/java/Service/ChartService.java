package service;

import model.Country;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ChartService {
    
    public ChartPanel createBarChart(List<Country> countries, String title) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        // Ordenar por porcentagem (decrescente) e pegar top 10
        List<Country> sorted = countries.stream()
            .sorted((c1, c2) -> Double.compare(c2.getPercentage(), c1.getPercentage()))
            .limit(10)
            .toList();
        
        for (Country country : sorted) {
            dataset.addValue(country.getPercentage(), "Internet %", country.getName());
        }
        
        JFreeChart chart = ChartFactory.createBarChart(
            title,
            "Country",
            "Internet Users (%)",
            dataset,
            PlotOrientation.VERTICAL,
            true,
            true,
            false
        );
        
        chart.setBackgroundPaint(Color.WHITE);
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(600, 400));
        
        return chartPanel;
    }
    
    public ChartPanel createPieChart(List<Country> countries, String title) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        
        // Ordenar por n?mero de usu?rios (decrescente) e pegar top 6
        List<Country> sorted = countries.stream()
            .sorted((c1, c2) -> Long.compare(c2.getInternetUsers(), c1.getInternetUsers()))
            .limit(6)
            .toList();
        
        for (Country country : sorted) {
            dataset.setValue(country.getName(), country.getInternetUsers());
        }
        
        JFreeChart chart = ChartFactory.createPieChart(
            title,
            dataset,
            true,
            true,
            false
        );
        
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(500, 400));
        
        return chartPanel;
    }
    
    // REMOVIDOS m?todos que n?o existiam:
    // - createRegionalComparisonChart()
    // - createTopCountriesChart()
    // - displayChartInFrame()
}