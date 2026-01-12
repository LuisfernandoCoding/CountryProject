package controller;

import model.Country;
import model.StatisticsModel;
import service.DatabaseService;
import service.ChartService;
import service.CSVService;
import view.MainView;
import org.jfree.chart.ChartPanel;

import javax.swing.*;
import java.io.File;
import java.util.List;

public class MainController {
    private StatisticsModel model;
    private MainView view;
    private DatabaseService dbService;
    private ChartService chartService;
    private CSVService csvService;
    
    public MainController(StatisticsModel model, MainView view) {
        this.model = model;
        this.view = view;
        
        // Initialize services
        this.dbService = new DatabaseService();
        this.chartService = new ChartService();
        this.csvService = new CSVService();
        
        // Setup database
        setupDatabase();
        
        // Setup listeners
        setupListeners();
        
        // Load initial data
        loadInitialData();
    }
    
    private void setupDatabase() {
        try {
            dbService.initializeDatabase();
            dbService.insertSampleData();
            view.setStatus("Database initialized successfully");
        } catch (Exception e) {
            view.showError("Error initializing database: " + e.getMessage());
        }
    }
    
    private void setupListeners() {
        // Load data from database
        view.getLoadDBButton().addActionListener(e -> loadFromDatabase());
        
        // Load data from CSV
        view.getLoadCSVButton().addActionListener(e -> loadFromCSV());
        
        // Filter data by percentage
        view.getFilterButton().addActionListener(e -> filterByPercentage());
        
        // Generate chart
        view.getChartButton().addActionListener(e -> generateChart());
        
        // Show statistics
        view.getStatsButton().addActionListener(e -> showStatistics());
    }
    
    private void loadInitialData() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                view.setStatus("Loading initial data...");
                List<Country> countries = dbService.loadAllCountries();
                model.setCountries(countries);
                return null;
            }
            
            @Override
            protected void done() {
                try {
                    view.displayCountries(model.getCountries());
                    view.setStatus("Initial data loaded");
                } catch (Exception e) {
                    view.showError("Error loading initial data: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }
    
    private void loadFromDatabase() {
        SwingWorker<List<Country>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Country> doInBackground() {
                view.setStatus("Loading from database...");
                return dbService.loadAllCountries();
            }
            
            @Override
            protected void done() {
                try {
                    List<Country> countries = get();
                    if (!countries.isEmpty()) {
                        model.setCountries(countries);
                        view.displayCountries(countries);
                        view.displayMessage("Loaded " + countries.size() + " countries from database");
                        view.setStatus("Database data loaded");
                    } else {
                        view.displayMessage("No countries found in database");
                    }
                } catch (Exception e) {
                    view.showError("Error loading from database: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }
    
    private void loadFromCSV() {
        File csvFile = view.selectCSVFile();
        if (csvFile == null) {
            return; // User cancelled
        }
        
        SwingWorker<List<Country>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Country> doInBackground() {
                view.setStatus("Loading from CSV...");
                return csvService.loadFromCSV(csvFile.getAbsolutePath());
            }
            
            @Override
            protected void done() {
                try {
                    List<Country> countries = get();
                    if (!countries.isEmpty()) {
                        // Ask user if they want to save to database
                        int response = view.showConfirmDialog(
                            "Do you want to save " + countries.size() + " countries to the database?",
                            "Save to Database"
                        );
                        
                        if (response == JOptionPane.YES_OPTION) {
                            // Save to database
                            for (Country country : countries) {
                                dbService.saveCountry(country);
                            }
                            view.showInfo("Data saved to database successfully");
                        }
                        
                        // Update model and view
                        model.setCountries(countries);
                        view.displayCountries(countries);
                        
                        view.displayMessage("Loaded " + countries.size() + 
                            " countries from CSV: " + csvFile.getName());
                        view.setStatus("CSV data loaded");
                        
                    } else {
                        view.showError("No valid data found in CSV file");
                    }
                } catch (Exception e) {
                    view.showError("Error loading from CSV: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }
    
    private void filterByPercentage() {
        try {
            double min = Double.parseDouble(view.getMinField().getText());
            double max = Double.parseDouble(view.getMaxField().getText());
            
            if (min > max) {
                view.showError("Minimum value must be less than maximum value");
                return;
            }
            
            SwingWorker<List<Country>, Void> worker = new SwingWorker<>() {
                @Override
                protected List<Country> doInBackground() {
                    view.setStatus("Filtering data...");
                    return dbService.filterByPercentageRange(min, max);
                }
                
                @Override
                protected void done() {
                    try {
                        List<Country> filtered = get();
                        
                        if (!filtered.isEmpty()) {
                            view.displayCountries(filtered);
                            
                            // Find country with lowest internet users in Eastern Europe
                            Country lowest = dbService.getCountryWithLowestRegisteredUsersInEasternEurope();
                            
                            StringBuilder message = new StringBuilder();
                            message.append("Found ").append(filtered.size())
                                   .append(" countries with ").append(min).append("% to ").append(max).append("%\n\n");
                            
                            if (lowest != null) {
                                message.append("Country with lowest registered internet users in Eastern Europe:\n");
                                message.append(lowest.getName()).append(": ")
                                       .append(String.format("%,d", lowest.getInternetUsers()))
                                       .append(" users (").append(String.format("%.2f", lowest.getPercentage())).append("%)");
                            }
                            
                            view.displayMessage(message.toString());
                            view.setStatus("Filter applied successfully");
                            
                        } else {
                            view.displayMessage("No countries found in the specified range");
                        }
                    } catch (Exception e) {
                        view.showError("Error filtering data: " + e.getMessage());
                    }
                }
            };
            worker.execute();
            
        } catch (NumberFormatException e) {
            view.showError("Please enter valid numeric values");
        }
    }
    
    private void generateChart() {
        if (model.getCountries().isEmpty()) {
            view.showError("No data available. Please load data first.");
            return;
        }
        
        String chartType = (String) view.getChartTypeCombo().getSelectedItem();
        
        SwingWorker<ChartPanel, Void> worker = new SwingWorker<>() {
            @Override
            protected ChartPanel doInBackground() {
                view.setStatus("Generating chart...");
                
                if ("Bar Chart".equals(chartType)) {
                    return chartService.createBarChart(
                        model.getCountries(), 
                        "Internet Users by Country"
                    );
                } else if ("Pie Chart".equals(chartType)) {
                    return chartService.createPieChart(
                        model.getCountries(),
                        "Internet Users Distribution"
                    );
                }
                return null;
            }
            
            @Override
            protected void done() {
                try {
                    ChartPanel chartPanel = get();
                    if (chartPanel != null) {
                        view.displayChart(chartPanel);
                        view.displayMessage(chartType + " generated successfully!");
                        view.setStatus("Chart generated");
                    }
                } catch (Exception e) {
                    view.showError("Error generating chart: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }
    
    private void showStatistics() {
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() {
                view.setStatus("Calculating statistics...");
                
                List<Country> allCountries = dbService.loadAllCountries();
                
                if (allCountries.isEmpty()) {
                    return "No data available for statistics";
                }
                
                StringBuilder sb = new StringBuilder();
                sb.append("GENERAL STATISTICS\n\n");
                
                // Basic statistics
                long totalPopulation = 0;
                long totalInternetUsers = 0;
                
                for (Country country : allCountries) {
                    totalPopulation += country.getTotalPopulation();
                    totalInternetUsers += country.getInternetUsers();
                }
                
                double globalAverage = (double) totalInternetUsers / totalPopulation * 100;
                
                sb.append("Total countries: ").append(allCountries.size()).append("\n");
                sb.append("Total population: ").append(String.format("%,d", totalPopulation)).append("\n");
                sb.append("Total internet users: ").append(String.format("%,d", totalInternetUsers)).append("\n");
                sb.append("Global average: ").append(String.format("%.2f%%", globalAverage)).append("\n\n");
                
                // Countries between 75% and 85% (specific requirement)
                List<Country> filtered75to85 = dbService.getCountries75to85Percent();
                if (!filtered75to85.isEmpty()) {
                    sb.append("COUNTRIES WITH 75%-85% INTERNET USERS:\n");
                    for (Country country : filtered75to85) {
                        sb.append("- ").append(country.getName())
                          .append(": ").append(String.format("%.2f%%", country.getPercentage()))
                          .append(" (").append(country.getRegion()).append(")\n");
                    }
                    sb.append("\n");
                }
                
                // Country with lowest number of users in Eastern Europe
                Country lowest = dbService.getCountryWithLowestRegisteredUsersInEasternEurope();
                if (lowest != null) {
                    sb.append("COUNTRY WITH LOWEST USERS IN EASTERN EUROPE:\n");
                    sb.append(lowest.getName()).append(": ")
                      .append(String.format("%,d", lowest.getInternetUsers()))
                      .append(" users (").append(String.format("%.2f%%", lowest.getPercentage())).append(")\n");
                }
                
                return sb.toString();
            }
            
            @Override
            protected void done() {
                try {
                    String statistics = get();
                    view.displayStatistics(statistics);
                    view.setStatus("Statistics calculated");
                } catch (Exception e) {
                    view.showError("Error calculating statistics: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }
}