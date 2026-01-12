package TelegramBot.bot.commands;

public class StartCommand extends BotCommand {
    
    public String execute(String[] args) {
        StringBuilder message = new StringBuilder();
        message.append("? *Welcome to Internet Stats Bot!*\n\n");
        message.append("I can help you analyze internet speed statistics.\n\n");
        message.append("*Available Commands:*\n");
        message.append("/load - Load the dataset\n");
        message.append("/filter <min> <max> - Filter by percentage range\n");
        message.append("/stats - Show statistics\n");
        message.append("/lowest - Show lowest values\n");
        message.append("/help - Show this help message\n\n");
        message.append("*Example usage:*\n");
        message.append("Type `75 85` to filter data between 75% and 85%\n");
        message.append("Or use `/filter 75 85`\n\n");
        message.append("Start by loading the data with /load");
        
        return message.toString();
    }
}