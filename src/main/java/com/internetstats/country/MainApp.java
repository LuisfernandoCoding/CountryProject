package com.internetstats.country;

import model.StatisticsModel;
import view.MainView;
import controller.MainController;
import service.DatabaseService;
import service.CSVService;
import connect.ConnectionFactory;
import TelegramBot.bot.InternetStatsBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Scanner;

public class MainApp {
    private static InternetStatsBot telegramBot;
    
    public static void main(String[] args) {
        System.out.println("=== INTERNET USERS STATISTICS APPLICATION ===");
        System.out.println("Choose mode:");
        System.out.println("1. GUI Application (Desktop)");
        System.out.println("2. Telegram Bot");
        System.out.println("3. Both (GUI + Telegram Bot)");
        System.out.print("Enter choice (1, 2, or 3): ");
        
        Scanner scanner = new Scanner(System.in);
        int choice = scanner.nextInt();
        
        switch (choice) {
            case 1:
                startGUIApplication();
                break;
            case 2:
                startTelegramBot();
                break;
            case 3:
                startBoth();
                break;
            default:
                System.out.println("Invalid choice. Starting GUI Application...");
                startGUIApplication();
        }
    }
    
    private static void startGUIApplication() {
        System.out.println("\n=== STARTING GUI APPLICATION ===");
        
        if (!testDatabaseConnection()) {
            System.err.println("ERROR: Could not connect to database!");
            System.err.println("Please check:");
            System.err.println("1. MySQL is running");
            System.err.println("2. Database 'internet_stats' exists");
            System.err.println("3. Username/password are correct in ConnectionFactory.java");
            return;
        }
        
        System.out.println("SUCCESS: MySQL connection established!");
        createSampleCSV();
        
        SwingUtilities.invokeLater(() -> {
            try {
                System.out.println("Initializing graphical interface...");
                
                StatisticsModel model = new StatisticsModel();
                MainView view = new MainView();
                addExportMenu(view, model);
                MainController controller = new MainController(model, view);
                
                view.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                view.setLocationRelativeTo(null);
                view.setVisible(true);
                
                System.out.println("SUCCESS: GUI Application started!");
                printGUIIInstructions();
                
            } catch (Exception e) {
                System.err.println("ERROR starting application: " + e.getMessage());
                e.printStackTrace();
                
                JOptionPane.showMessageDialog(null,
                    "Error starting application:\n" + e.getMessage(),
                    "Initialization Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }
    
    private static void startTelegramBot() {
        System.out.println("\n=== STARTING TELEGRAM BOT ===");
        
        if (!testDatabaseConnection()) {
            System.err.println("ERROR: Could not connect to database!");
            return;
        }
        
        createSampleCSV();
        
        try {
            String botToken = getBotToken();
            if (botToken == null || botToken.isEmpty()) {
                System.err.println("ERROR: Bot token not provided!");
                return;
            }
            
            System.out.println("Starting Telegram Bot...");
            telegramBot = new InternetStatsBot(botToken);
            
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(telegramBot);
            
            System.out.println("SUCCESS: Telegram Bot started!");
            System.out.println("Bot username: " + telegramBot.getBotUsername());
            System.out.println("Bot is listening for messages...");
            
            // Adiciona shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\nShutting down Telegram Bot...");
                if (telegramBot != null) {
                    telegramBot.shutdown();
                }
            }));
            
            printTelegramInstructions();
            
            // Mant?m o programa rodando
            System.out.println("\nPress Ctrl+C to stop the bot.");
            
            // Mant?m a thread principal viva
            Thread.currentThread().join();
            
        } catch (Exception e) {
            System.err.println("Failed to start Telegram Bot: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void startBoth() {
        System.out.println("\n=== STARTING BOTH GUI AND TELEGRAM BOT ===");
        
        // Primeiro testa a conex?o com o banco
        if (!testDatabaseConnection()) {
            System.err.println("ERROR: Could not connect to database!");
            return;
        }
        
        createSampleCSV();
        
        // Inicia o bot do Telegram em uma thread separada
        Thread telegramThread = new Thread(() -> {
            try {
                String botToken = getBotToken();
                if (botToken == null || botToken.isEmpty()) {
                    System.err.println("WARNING: Bot token not provided. Starting only GUI.");
                    return;
                }
                
                System.out.println("Starting Telegram Bot in background...");
                telegramBot = new InternetStatsBot(botToken);
                TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
                botsApi.registerBot(telegramBot);
                
                System.out.println("SUCCESS: Telegram Bot started in background!");
                
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    if (telegramBot != null) {
                        telegramBot.shutdown();
                    }
                }));
                
            } catch (Exception e) {
                System.err.println("Failed to start Telegram Bot: " + e.getMessage());
            }
        });
        
        telegramThread.start();
        
        // Inicia a GUI na thread principal
        startGUIApplication();
    }
    
    private static String getBotToken() {
        // Tenta obter o token de diferentes fontes
        String token = null;
        
        // 1. Verifica argumentos da linha de comando
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("\n=== TELEGRAM BOT CONFIGURATION ===");
        System.out.println("Please enter your Telegram Bot Token:");
        System.out.println("(Get it from @BotFather on Telegram)");
        System.out.print("Token: ");
        
        token = scanner.nextLine().trim();
        
        if (token.isEmpty()) {
            System.out.println("\nNo token provided. Checking for token file...");
            
            // 2. Verifica arquivo de configura??o
            File tokenFile = new File("bot_token.txt");
            if (tokenFile.exists()) {
                try {
                    Scanner fileScanner = new Scanner(tokenFile);
                    token = fileScanner.nextLine().trim();
                    fileScanner.close();
                    System.out.println("Token loaded from file.");
                } catch (Exception e) {
                    System.err.println("Error reading token file: " + e.getMessage());
                }
            }
        }
        
        if (token != null && !token.isEmpty()) {
            // Salva o token em arquivo para uso futuro
            try {
                java.io.FileWriter writer = new java.io.FileWriter("bot_token.txt");
                writer.write(token);
                writer.close();
                System.out.println("Token saved to file for future use.");
            } catch (Exception e) {
                System.err.println("Warning: Could not save token to file.");
            }
        }
        
        return token;
    }
    
    private static void addExportMenu(MainView view, StatisticsModel model) {
        JMenuBar menuBar = new JMenuBar();
        
        JMenu fileMenu = new JMenu("File");
        JMenuItem exportItem = new JMenuItem("Export to CSV");
        exportItem.addActionListener(e -> exportToCSV(model));
        
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> {
            if (telegramBot != null) {
                telegramBot.shutdown();
            }
            System.exit(0);
        });
        
        fileMenu.add(exportItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        
        // Adiciona menu para controle do bot do Telegram (se estiver rodando)
        if (telegramBot != null) {
            JMenu telegramMenu = new JMenu("Telegram Bot");
            JMenuItem botStatus = new JMenuItem("Bot Status: Running");
            botStatus.setEnabled(false);
            
            JMenuItem stopBot = new JMenuItem("Stop Telegram Bot");
            stopBot.addActionListener(e -> {
                telegramBot.shutdown();
                telegramBot = null;
                botStatus.setText("Bot Status: Stopped");
                stopBot.setEnabled(false);
            });
            
            telegramMenu.add(botStatus);
            telegramMenu.add(stopBot);
            menuBar.add(telegramMenu);
        }
        
        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> JOptionPane.showMessageDialog(view,
            "Internet Users Statistics Application\n" +
            "Version 2.0 (GUI + Telegram Bot)\n" +
            "\nGUI Functionalities:\n" +
            "- Load data from MySQL database\n" +
            "- Import data from CSV files\n" +
            "- Filter data by percentage range\n" +
            "- Generate charts and statistics\n" +
            "- Export data to CSV\n" +
            "\nTelegram Bot Commands:\n" +
            "/start - Welcome message\n" +
            "/load - Load dataset\n" +
            "/filter min max - Filter data\n" +
            "/stats - Show statistics\n" +
            "/lowest - Show lowest values\n" +
            "/help - Show help",
            "About",
            JOptionPane.INFORMATION_MESSAGE));
        
        helpMenu.add(aboutItem);
        
        menuBar.add(fileMenu);
        menuBar.add(helpMenu);
        
        view.setJMenuBar(menuBar);
    }
    
    private static void exportToCSV(StatisticsModel model) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export to CSV");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "CSV files (*.csv)", "csv"));
        
        int result = fileChooser.showSaveDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            String filePath = file.getAbsolutePath();
            
            if (!filePath.toLowerCase().endsWith(".csv")) {
                filePath += ".csv";
            }
            
            CSVService csvService = new CSVService();
            csvService.saveToCSV(model.getCountries(), filePath);
            
            JOptionPane.showMessageDialog(null,
                "Data exported successfully to:\n" + filePath,
                "Export Complete",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private static void createSampleCSV() {
        File sampleFile = new File("sample_countries.csv");
        if (!sampleFile.exists()) {
            CSVService csvService = new CSVService();
            csvService.createSampleCSV("sample_countries.csv");
            System.out.println("Sample CSV file created: sample_countries.csv");
        }
    }
    
    private static boolean testDatabaseConnection() {
        System.out.println("Testing MySQL connection...");
        
        DatabaseService dbService = new DatabaseService();
        
        if (!dbService.testConnection()) {
            System.err.println("ERROR: Database connection failed!");
            return false;
        }
        
        System.out.println("SUCCESS: MySQL connection OK");
        
        try {
            dbService.initializeDatabase();
            System.out.println("SUCCESS: Database initialized");
            
            int count = dbService.loadAllCountries().size();
            if (count == 0) {
                System.out.println("INFO: Database empty. Inserting sample data...");
                dbService.insertSampleData();
                count = dbService.loadAllCountries().size();
                System.out.println("SUCCESS: " + count + " countries inserted");
            } else {
                System.out.println("INFO: Database already contains " + count + " countries");
            }
            
            return true;
            
        } catch (Exception e) {
            System.err.println("ERROR initializing database: " + e.getMessage());
            return false;
        }
    }
    
    private static void printGUIIInstructions() {
        System.out.println("\n======================================");
        System.out.println("GUI Application Instructions:");
        System.out.println("1. Click 'Load from Database' to load countries from MySQL");
        System.out.println("2. Click 'Load from CSV' to import data from CSV file");
        System.out.println("3. Use 'Filter (75%-85%)' to filter countries");
        System.out.println("4. Click 'Generate Chart' to create charts");
        System.out.println("5. Use 'Statistics' to view statistics");
        System.out.println("6. Use 'File ? Export to CSV' to save data");
        System.out.println("======================================");
    }
    
    private static void printTelegramInstructions() {
        System.out.println("\n======================================");
        System.out.println("Telegram Bot Instructions:");
        System.out.println("1. Search for your bot on Telegram");
        System.out.println("2. Send /start to begin");
        System.out.println("3. Available commands:");
        System.out.println("   /start - Welcome message");
        System.out.println("   /load - Load dataset");
        System.out.println("   /stats - Show statistics");
        System.out.println("   /filter min max - Filter data");
        System.out.println("   /lowest - Show lowest values");
        System.out.println("   /help - Show help");
        System.out.println("4. You can also type '75 85' directly");
        System.out.println("======================================");
    }
}