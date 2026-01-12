package connect;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;

public class ConnectionFactory {
    
    // Improved method that tries to load automatically
    public Connection getConnection() {
        Connection conn = null;
        
        try {
            // JDBC 4.0+ loads driver automatically
            // No need for Class.forName() if the JAR is in the classpath
            
            String url = "jdbc:mysql://127.0.0.1:3306/internet_stats?" +
                        "useSSL=false&" +
                        "serverTimezone=UTC&" +
                        "allowPublicKeyRetrieval=true";
            
            String user = "root";
            String password = "Ëóè123"; // Use your password here
            
            conn = DriverManager.getConnection(url, user, password);
            
            if (conn != null) {
                System.out.println(" MySQL connection established!");
                return conn;
            }
            
        } catch (SQLException e) {
            handleConnectionError(e);
        }
        
        return null;
    }
    
    private void handleConnectionError(SQLException e) {
        // Checks if it's a driver not found error
        String errorMsg = e.getMessage();
        
        if (errorMsg.contains("No suitable driver") || 
            errorMsg.contains("Driver not found")) {
            
            JOptionPane.showMessageDialog(null,
                "MySQL driver not found!\n\n" +
                "Solution:\n" +
                "1. Download: https://dev.mysql.com/downloads/connector/j/\n" +
                "2. Extract mysql-connector-java-8.0.33.jar\n" +
                "3. Add to project classpath",
                "Driver Error",
                JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(null,
                "MySQL connection error:\n" + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
        
        System.err.println("SQL Error: " + e.getMessage());
    }
    
    // Method to check if the driver is available
    public static boolean isDriverAvailable() {
        try {
            // Try to load the driver class
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println(" MySQL driver available");
            return true;
        } catch (ClassNotFoundException e) {
            System.err.println(" MySQL driver not found in classpath");
            return false;
        }
    }
}