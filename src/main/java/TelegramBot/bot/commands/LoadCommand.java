package TelegramBot.bot.commands;

import service.DatabaseService;
import model.Country;
import java.util.List;

public class LoadCommand extends BotCommand {
    private final DatabaseService dbService;
    
    public LoadCommand() {
        this.dbService = new DatabaseService();
    }
    
    public String execute(String[] args) {
        try {
            List<Country> countries = dbService.loadAllCountries();
            
            if (countries.isEmpty()) {
                return "No countries found in database.";
            }
            
            StringBuilder response = new StringBuilder();
            response.append("? *Loaded ").append(countries.size()).append(" countries:*\n\n");
            
            // Show top 5 countries
            int limit = Math.min(countries.size(), 5);
            for (int i = 0; i < limit; i++) {
                Country country = countries.get(i);
                response.append(i + 1).append(". ").append(country.getName())
                       .append(": ").append(String.format("%.2f%%", country.getPercentage()))
                       .append(" (").append(country.getRegion()).append(")\n");
            }
            
            if (countries.size() > 5) {
                response.append("\n... and ").append(countries.size() - 5).append(" more countries.");
            }
            
            return response.toString();
            
        } catch (Exception e) {
            return "? Error loading data: " + e.getMessage();
        }
    }
}