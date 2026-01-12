package TelegramBot.bot.commands;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class LowestCommand extends BotCommand {
    
    
    public String execute(String[] args) {
        try {
            List<InternetData> dataList = loadData();
            if (dataList.isEmpty()) {
                return "? No data available. Please load data first with /load";
            }
            
            // Sort by percentage in ascending order
            Collections.sort(dataList, Comparator.comparingDouble(InternetData::getPercentage));
            
            // Get top 10 lowest
            int limit = Math.min(10, dataList.size());
            StringBuilder result = new StringBuilder();
            result.append("? *Top 10 Lowest Percentages:*\n\n");
            
            for (int i = 0; i < limit; i++) {
                InternetData data = dataList.get(i);
                result.append(String.format("%d. %s: %.2f%%\n", 
                    i + 1, 
                    data.getLabel(), 
                    data.getPercentage()));
            }
            
            double average = dataList.stream()
                .mapToDouble(InternetData::getPercentage)
                .average()
                .orElse(0.0);
            
            result.append(String.format("\n? *Average: %.2f%%*", average));
            
            return result.toString();
            
        } catch (IOException e) {
            return "? Error loading data: " + e.getMessage();
        }
    }
    
    private List<InternetData> loadData() throws IOException {
        List<InternetData> dataList = new ArrayList<>();
        String filePath = "internet_data.csv"; // Adjust this path as needed
        
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean firstLine = true;
            
            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue; // Skip header
                }
                
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    try {
                        String label = parts[0].trim();
                        double percentage = Double.parseDouble(parts[1].trim());
                        dataList.add(new InternetData(label, percentage));
                    } catch (NumberFormatException e) {
                        // Skip invalid lines
                        System.err.println("Skipping invalid line: " + line);
                    }
                }
            }
        }
        
        return dataList;
    }
    
    // Inner class to hold data
    private static class InternetData {
        private final String label;
        private final double percentage;
        
        public InternetData(String label, double percentage) {
            this.label = label;
            this.percentage = percentage;
        }
        
        public String getLabel() {
            return label;
        }
        
        public double getPercentage() {
            return percentage;
        }
    }
}