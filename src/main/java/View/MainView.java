package view;

import model.Country;
import org.jfree.chart.ChartPanel;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;

public class MainView extends JFrame {
    private JTable dataTable;
    private JButton loadDBButton;
    private JButton loadCSVButton;
    private JButton filterButton;
    private JButton chartButton;
    private JButton statsButton;
    private JTextField minField;
    private JTextField maxField;
    private JComboBox<String> chartTypeCombo;
    private JTextArea resultArea;
    private JLabel statusLabel;
    
    public MainView() {
        setTitle("Internet Users Statistics");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initComponents();
    }
    
    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Control panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        
        loadDBButton = new JButton("Load from Database");
        loadCSVButton = new JButton("Load from CSV");
        filterButton = new JButton("Filter (75%-85%)");
        chartButton = new JButton("Generate Chart");
        statsButton = new JButton("Statistics");
        
        minField = new JTextField("75", 5);
        maxField = new JTextField("85", 5);
        
        String[] chartTypes = {"Bar Chart", "Pie Chart"};
        chartTypeCombo = new JComboBox<>(chartTypes);
        
        controlPanel.add(loadDBButton);
        controlPanel.add(loadCSVButton);
        controlPanel.add(new JLabel("Filter %:"));
        controlPanel.add(minField);
        controlPanel.add(new JLabel("to"));
        controlPanel.add(maxField);
        controlPanel.add(filterButton);
        controlPanel.add(new JLabel("Chart:"));
        controlPanel.add(chartTypeCombo);
        controlPanel.add(chartButton);
        controlPanel.add(statsButton);
        
        // Table
        String[] columns = {"Country", "Code", "Population", "Internet Users", "%", "Region", "Source"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        dataTable = new JTable(model);
        dataTable.setAutoCreateRowSorter(true);
        JScrollPane tableScrollPane = new JScrollPane(dataTable);
        
        // Result area
        resultArea = new JTextArea(6, 50);
        resultArea.setEditable(false);
        resultArea.setBorder(BorderFactory.createTitledBorder("Results"));
        JScrollPane resultScrollPane = new JScrollPane(resultArea);
        
        // Status bar
        statusLabel = new JLabel(" Ready");
        statusLabel.setBorder(BorderFactory.createEtchedBorder());
        
        // Add components
        mainPanel.add(controlPanel, BorderLayout.NORTH);
        mainPanel.add(tableScrollPane, BorderLayout.CENTER);
        mainPanel.add(resultScrollPane, BorderLayout.SOUTH);
        
        add(mainPanel, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);
    }
    
    public void displayCountries(List<Country> countries) {
        DefaultTableModel model = (DefaultTableModel) dataTable.getModel();
        model.setRowCount(0);
        
        for (Country country : countries) {
            Object[] row = {
                country.getName(),
                country.getCode() != null ? country.getCode() : "",
                String.format("%,d", country.getTotalPopulation()),
                String.format("%,d", country.getInternetUsers()),
                String.format("%.2f%%", country.getPercentage()),
                country.getRegion(),
                country.getDataSource() != null ? country.getDataSource() : "Manual"
            };
            model.addRow(row);
        }
        
        setStatus("Displaying " + countries.size() + " countries");
    }
    
    public void displayChart(ChartPanel chartPanel) {
        JFrame chartFrame = new JFrame("Chart");
        chartFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        chartFrame.getContentPane().add(chartPanel);
        chartFrame.pack();
        chartFrame.setLocationRelativeTo(this);
        chartFrame.setVisible(true);
    }
    
    public void displayMessage(String message) {
        resultArea.setText(message);
    }
    
    public void displayStatistics(String statistics) {
        resultArea.setText(statistics);
    }
    
    public void setStatus(String status) {
        statusLabel.setText(" Status: " + status);
    }
    
    public File selectCSVFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select CSV File");
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV files (*.csv)", "csv"));
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        }
        return null;
    }
    
    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    public void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }
    
    public int showConfirmDialog(String message, String title) {
        return JOptionPane.showConfirmDialog(this, message, title, JOptionPane.YES_NO_OPTION);
    }
    
    // Getters atualizados
    public JButton getLoadDBButton() { return loadDBButton; }
    public JButton getLoadCSVButton() { return loadCSVButton; }
    public JButton getFilterButton() { return filterButton; }
    public JButton getChartButton() { return chartButton; }
    public JButton getStatsButton() { return statsButton; }
    public JTextField getMinField() { return minField; }
    public JTextField getMaxField() { return maxField; }
    public JComboBox<String> getChartTypeCombo() { return chartTypeCombo; }
}