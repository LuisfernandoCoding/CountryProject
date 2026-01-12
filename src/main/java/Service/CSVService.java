package service;

import model.Country;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CSVService {
    
    public List<Country> loadFromCSV(String filePath) {
        List<Country> countries = new ArrayList<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isFirstLine = true;
            
            while ((line = br.readLine()) != null) {
                // Skip header line
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                
                // Parse CSV line (format: name,population,internet_users,region)
                String[] values = line.split(",");
                if (values.length >= 4) {
                    try {
                        String name = values[0].trim();
                        long population = parseLong(values[1].trim());
                        long internetUsers = parseLong(values[2].trim());
                        String region = values[3].trim();
                        
                        Country country = new Country(name, population, internetUsers, region);
                        country.setDataSource("CSV Import");
                        
                        // Generate country code from first 2 letters of name
                        if (name.length() >= 2) {
                            country.setCode(name.substring(0, 2).toUpperCase());
                        }
                        
                        countries.add(country);
                        
                    } catch (NumberFormatException e) {
                        System.err.println("Error parsing line: " + line + " - " + e.getMessage());
                    }
                }
            }
            
            System.out.println("Loaded " + countries.size() + " countries from CSV");
            
        } catch (IOException e) {
            System.err.println("Error reading CSV file: " + e.getMessage());
        }
        
        return countries;
    }
    
    public void saveToCSV(List<Country> countries, String filePath) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            // Write header
            writer.println("Country,Country Code,Total Population,Internet Users,Percentage,Region,Data Source");
            
            // Write data
            for (Country country : countries) {
                writer.printf("%s,%s,%d,%d,%.2f,%s,%s%n",
                    country.getName(),
                    country.getCode() != null ? country.getCode() : "",
                    country.getTotalPopulation(),
                    country.getInternetUsers(),
                    country.getPercentage(),
                    country.getRegion(),
                    country.getDataSource() != null ? country.getDataSource() : ""
                );
            }
            
            System.out.println("Saved " + countries.size() + " countries to CSV");
            
        } catch (IOException e) {
            System.err.println("Error saving to CSV: " + e.getMessage());
        }
    }
    
    private long parseLong(String value) {
        // Remove any non-numeric characters except minus sign
        value = value.replaceAll("[^\\d-]", "");
        return value.isEmpty() ? 0 : Long.parseLong(value);
    }
    
    // Create sample CSV file for testing
    public void createSampleCSV(String filePath) {
        List<Country> sampleData = new ArrayList<>();
        sampleData.add(new Country("Germany", 83000000, 72000000, "Europe"));
        sampleData.add(new Country("France", 67000000, 58000000, "Europe"));
        sampleData.add(new Country("Poland", 38000000, 32000000, "Eastern Europe"));
        sampleData.add(new Country("Czech Republic", 10700000, 9200000, "Eastern Europe"));
        sampleData.add(new Country("Hungary", 9700000, 8100000, "Eastern Europe"));
        
        saveToCSV(sampleData, filePath);
    }
}