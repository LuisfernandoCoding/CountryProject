package TelegramBot.bot.commands;

import service.DatabaseService;
import model.Country;
import java.util.List;

public class StatsCommand extends BotCommand {
    private final DatabaseService dbService;
    
    public StatsCommand() {
        this.dbService = new DatabaseService();
    }
    
  
    public String execute(String[] args) {
        try {
            List<Country> countries = dbService.loadAllCountries();
            
            if (countries.isEmpty()) {
                return "No data available for statistics.";
            }
            
            long totalPopulation = 0;
            long totalInternetUsers = 0;
            
            for (Country country : countries) {
                totalPopulation += country.getTotalPopulation();
                totalInternetUsers += country.getInternetUsers();
            }
            
            double globalAverage = (double) totalInternetUsers / totalPopulation * 100;
            
            return " *Global Statistics:*\n\n" +
                   "Total countries: " + countries.size() + "\n" +
                   "Total population: " + String.format("%,d", totalPopulation) + "\n" +
                   "Total internet users: " + String.format("%,d", totalInternetUsers) + "\n" +
                   "Global average: " + String.format("%.2f%%", globalAverage) + "\n\n" +
                   "Use /filter 75 85 to find specific countries.";
            
        } catch (Exception e) {
            return " Error calculating statistics: " + e.getMessage();
        }
    }
}