package TelegramBot.bot.commands;

import service.DatabaseService;
import model.Country;
import java.util.List;

public class FilterCommand extends BotCommand {
    private final DatabaseService dbService;
    
    public FilterCommand() {
        this.dbService = new DatabaseService();
    }
    
    public String execute(String[] args) {
        try {
            double min = 75.0;
            double max = 85.0;
            
            if (args.length >= 2) {
                min = Double.parseDouble(args[0]);
                max = Double.parseDouble(args[1]);
            } else if (args.length == 1) {
                min = Double.parseDouble(args[0]);
                max = min + 10.0;
            }
            
            if (min > max) {
                double temp = min;
                min = max;
                max = temp;
            }
            
            List<Country> filtered = dbService.filterByPercentageRange(min, max);
            
            if (filtered.isEmpty()) {
                return "No countries found with " + min + "% to " + max + "% internet users.";
            }
            
            StringBuilder response = new StringBuilder();
            response.append("*Countries with ").append(min).append("% to ").append(max).append("%:*\n\n");
            
            for (Country country : filtered) {
                response.append("• ").append(country.getName())
                       .append(": ").append(String.format("%.2f%%", country.getPercentage()))
                       .append(" (").append(country.getRegion()).append(")\n");
            }
            
            // Add country with lowest users in Eastern Europe
            Country lowest = dbService.getCountryWithLowestRegisteredUsersInEasternEurope();
            if (lowest != null) {
                response.append("\n *Lowest users in Eastern Europe:*\n");
                response.append(lowest.getName()).append(": ")
                       .append(String.format("%,d", lowest.getInternetUsers()))
                       .append(" users (").append(String.format("%.2f%%", lowest.getPercentage())).append(")");
            }
            
            return response.toString();
            
        } catch (NumberFormatException e) {
            return "Please enter valid numbers. Example: /filter 75 85";
        } catch (Exception e) {
            return " Error: " + e.getMessage();
        }
    }
}