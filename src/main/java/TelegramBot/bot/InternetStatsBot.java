package TelegramBot.bot;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import TelegramBot.bot.model.UserSession;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

public class InternetStatsBot extends TelegramLongPollingBot {
    private final String botToken;
    private final Map<Long, UserSession> userSessions;
    private final ExecutorService threadPool;
    
    public InternetStatsBot(String botToken) {
        super(botToken);
        this.botToken = botToken;
        this.userSessions = new ConcurrentHashMap<>();
        this.threadPool = Executors.newFixedThreadPool(10);
        
        System.out.println("Telegram Bot initialized with 10 threads for concurrent users");
    }
    
    @Override
    public String getBotUsername() {
        return "InternetStatsBot";
    }
    
    @Override
    public String getBotToken() {
        return botToken;
    }
    
    @Override
    public void onUpdateReceived(Update update) {
        threadPool.submit(new Runnable() {
            @Override
            public void run() {
                processUpdate(update);
            }
        });
    }
    
    private void processUpdate(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            long chatId = update.getMessage().getChatId();
            String messageText = update.getMessage().getText();
            String userName = update.getMessage().getFrom().getUserName();
            
            if (userName == null) {
                userName = update.getMessage().getFrom().getFirstName();
            }
            
            System.out.println("[" + Thread.currentThread().getName() + "] From: @" + userName + " | Message: " + messageText);
            
            UserSession session = userSessions.get(chatId);
            if (session == null) {
                session = new UserSession(chatId, userName);
                userSessions.put(chatId, session);
            }
            
            String response = processMessage(messageText, session);
            
            sendMessage(chatId, response);
        }
    }
    
    private String processMessage(String message, UserSession session) {
        try {
            // Verifica se ? um intervalo de porcentagem (ex: "75 85")
            if (message.matches("\\d+(\\.\\d+)?\\s+\\d+(\\.\\d+)?")) {
                String[] args = message.split("\\s+");
                return executeFilterCommand(args);
            }
            
            // Verifica se ? um comando
            if (message.startsWith("/")) {
                String[] parts = message.split("\\s+", 2);
                String command = parts[0].toLowerCase();
                String[] args = parts.length > 1 ? parts[1].split("\\s+") : new String[0];
                
                return executeCommand(command, args);
            }
            
            return "Please send a command (e.g., /load, /filter 75 85) or a percentage range (e.g., 75 85)";
            
        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }
    
    private String executeCommand(String command, String[] args) {
        switch (command) {
            case "/start":
                return executeStartCommand(args);
            case "/load":
                return executeLoadCommand(args);
            case "/filter":
                return executeFilterCommand(args);
            case "/stats":
                return executeStatsCommand(args);
            case "/lowest":
                return executeLowestCommand(args);
            case "/help":
                return executeHelpCommand(args);
            default:
                return "Unknown command. Type /help for available commands.";
        }
    }
    
    private String executeStartCommand(String[] args) {
        return "Welcome to Internet Stats Bot!\n\n" +
               "I can help you analyze internet speed statistics.\n\n" +
               "Available Commands:\n" +
               "/load - Load the dataset\n" +
               "/filter <min> <max> - Filter by percentage range\n" +
               "/stats - Show statistics\n" +
               "/lowest - Show lowest values\n" +
               "/help - Show this help message\n\n" +
               "Example usage:\n" +
               "Type '75 85' to filter data between 75% and 85%\n" +
               "Or use '/filter 75 85'\n\n" +
               "Start by loading the data with /load";
    }
    
    private String executeLoadCommand(String[] args) {
        // Aqui voc? implementa a l?gica para carregar dados
        // Por enquanto, retorna uma mensagem de exemplo
        return "Data loaded successfully. Ready to analyze.";
    }
    
    private String executeFilterCommand(String[] args) {
        if (args.length != 2) {
            return "Usage: /filter <min> <max> or simply type 'min max'";
        }
        
        try {
            double min = Double.parseDouble(args[0]);
            double max = Double.parseDouble(args[1]);
            
            // Aqui voc? implementa a l?gica de filtragem
            // Por enquanto, retorna uma mensagem de exemplo
            return String.format("Filtering data between %.2f%% and %.2f%%...\n" +
                               "This would show results where percentage is between %.2f and %.2f", 
                               min, max, min, max);
        } catch (NumberFormatException e) {
            return "Error: Please provide valid numbers for min and max";
        }
    }
    
    private String executeStatsCommand(String[] args) {
        // Aqui voc? implementa a l?gica para estat?sticas
        return "Statistics:\n" +
               "- Total entries: 100\n" +
               "- Average: 78.5%\n" +
               "- Highest: 95.2%\n" +
               "- Lowest: 45.8%";
    }
    
    private String executeLowestCommand(String[] args) {
        // Aqui voc? implementa a l?gica para valores mais baixos
        return "Top 10 lowest values:\n" +
               "1. Country A: 45.8%\n" +
               "2. Country B: 48.3%\n" +
               "3. Country C: 52.1%\n" +
               "4. Country D: 55.6%\n" +
               "5. Country E: 58.9%\n" +
               "6. Country F: 61.2%\n" +
               "7. Country G: 63.5%\n" +
               "8. Country H: 65.8%\n" +
               "9. Country I: 68.1%\n" +
               "10. Country J: 70.4%";
    }
    
    private String executeHelpCommand(String[] args) {
        return "Internet Stats Bot - Help Guide\n\n" +
               "Basic Commands:\n" +
               "/start - Start the bot and see welcome message\n" +
               "/load - Load internet speed dataset\n" +
               "/filter <min> <max> - Filter data by percentage range\n" +
               "/stats - Show detailed statistics\n" +
               "/lowest - Show top 10 lowest percentages\n" +
               "/help - Show this help message\n\n" +
               "Quick Filters:\n" +
               "You can directly type percentage ranges:\n" +
               "'75 85' - Filters data between 75% and 85%\n\n" +
               "Examples:\n" +
               "1. '/load' - Load the dataset\n" +
               "2. '/stats' - See overall statistics\n" +
               "3. '/filter 80 90' - Filter 80% to 90% range\n" +
               "4. '60 70' - Quick filter 60% to 70% range\n" +
               "5. '/lowest' - See bottom 10 values\n\n" +
               "Data Format:\n" +
               "The bot expects a CSV file with columns:\n" +
               "Label,Percentage\n\n" +
               "Need Help?\n" +
               "Make sure to load data first with /load command.";
    }
    
    private void sendMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        message.setParseMode("Markdown");
        
        try {
            execute(message);
            System.out.println("Sent response to chatId: " + chatId);
        } catch (TelegramApiException e) {
            System.err.println("Error sending Telegram message: " + e.getMessage());
        }
    }
    
    public void shutdown() {
        System.out.println("Shutting down Telegram Bot...");
        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(30, TimeUnit.SECONDS)) {
                threadPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
        System.out.println("Telegram Bot shutdown complete.");
    }
}