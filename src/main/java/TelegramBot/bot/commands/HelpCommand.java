package TelegramBot.bot.commands;

public class HelpCommand extends BotCommand {
    
    public String execute(String[] args) {
        StringBuilder help = new StringBuilder();
        help.append("? *Internet Stats Bot - Help Guide*\n\n");
        help.append("*Basic Commands:*\n");
        help.append("/start - Start the bot and see welcome message\n");
        help.append("/load - Load internet speed dataset\n");
        help.append("/filter <min> <max> - Filter data by percentage range\n");
        help.append("/stats - Show detailed statistics\n");
        help.append("/lowest - Show top 10 lowest percentages\n");
        help.append("/help - Show this help message\n\n");
        help.append("*Quick Filters:*\n");
        help.append("You can directly type percentage ranges:\n");
        help.append("`75 85` - Filters data between 75% and 85%\n\n");
        help.append("*Examples:*\n");
        help.append("1. `/load` - Load the dataset\n");
        help.append("2. `/stats` - See overall statistics\n");
        help.append("3. `/filter 80 90` - Filter 80% to 90% range\n");
        help.append("4. `60 70` - Quick filter 60% to 70% range\n");
        help.append("5. `/lowest` - See bottom 10 values\n\n");
        help.append("*Data Format:*\n");
        help.append("The bot expects a CSV file with columns:\n");
        help.append("Label,Percentage\n\n");
        help.append("*Need Help?*\n");
        help.append("Make sure to load data first with /load command.");
        
        return help.toString();
    }
}