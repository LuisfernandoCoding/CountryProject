package service;

import connect.ConnectionFactory;
  // CORRIGIDO: era "connect.ConnectionFactory"
import model.Country;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseService {
    private ConnectionFactory connectionFactory;
    
    public DatabaseService() {
        this.connectionFactory = new ConnectionFactory();
    }
    
    public void initializeDatabase() {
        try (Connection conn = connectionFactory.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Criar tabela simplificada (sem coluna gdp se n?o existe)
            String createTableSQL = 
                "CREATE TABLE IF NOT EXISTS countries (" +
                "id INT PRIMARY KEY AUTO_INCREMENT, " +
                "name VARCHAR(100) NOT NULL UNIQUE, " +
                "code VARCHAR(3), " +
                "total_population BIGINT, " +
                "internet_users BIGINT, " +
                "region VARCHAR(50), " +
                "percentage DECIMAL(5,2) AS (CASE " +
                "    WHEN total_population > 0 THEN " +
                "        ROUND((internet_users * 100.0 / total_population), 2) " +
                "    ELSE 0 END) STORED, " +
                "data_source VARCHAR(50), " +
                "last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                ")";
            
            stmt.execute(createTableSQL);
            
            // Criar ?ndices
            try {
                stmt.execute("CREATE INDEX idx_percentage ON countries(percentage)");
            } catch (SQLException e) {
                // ?ndice j? existe, ignorar erro
            }
            
            try {
                stmt.execute("CREATE INDEX idx_region ON countries(region)");
            } catch (SQLException e) {
                // ?ndice j? existe, ignorar erro
            }
            
            System.out.println("? Banco de dados inicializado com sucesso!");
            
        } catch (SQLException e) {
            System.err.println("? Erro ao inicializar banco: " + e.getMessage());
        }
    }
    
    public void saveCountry(Country country) {
        String sql = "INSERT INTO countries (name, code, total_population, internet_users, " +
                    "region, data_source) VALUES (?, ?, ?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE " +
                    "total_population = VALUES(total_population), " +
                    "internet_users = VALUES(internet_users), " +
                    "region = VALUES(region), " +
                    "data_source = VALUES(data_source)";
        
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, country.getName());
            pstmt.setString(2, country.getCode());
            pstmt.setLong(3, country.getTotalPopulation());
            pstmt.setLong(4, country.getInternetUsers());
            pstmt.setString(5, country.getRegion());
            pstmt.setString(6, country.getDataSource());
            
            pstmt.executeUpdate();
            System.out.println("? Pa?s salvo: " + country.getName());
            
        } catch (SQLException e) {
            System.err.println("? Erro ao salvar pa?s: " + e.getMessage());
        }
    }
    
    public List<Country> loadAllCountries() {
        List<Country> countries = new ArrayList<>();
        String sql = "SELECT * FROM countries ORDER BY name";
        
        try (Connection conn = connectionFactory.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Country country = new Country(
                    rs.getLong("id"),
                    rs.getString("name"),
                    rs.getString("code"),
                    rs.getLong("total_population"),
                    rs.getLong("internet_users"),
                    rs.getString("region"),
                    rs.getString("data_source")  // Removido gdp
                );
                countries.add(country);
            }
            
            System.out.println("? Carregados " + countries.size() + " pa?ses do banco");
            
        } catch (SQLException e) {
            System.err.println("? Erro ao carregar pa?ses: " + e.getMessage());
        }
        
        return countries;
    }
    
    public List<Country> filterByPercentageRange(double min, double max) {
        List<Country> countries = new ArrayList<>();
        String sql = "SELECT * FROM countries WHERE percentage BETWEEN ? AND ? ORDER BY percentage DESC";
        
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDouble(1, min);
            pstmt.setDouble(2, max);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Country country = new Country(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("code"),
                        rs.getLong("total_population"),
                        rs.getLong("internet_users"),
                        rs.getString("region"),
                        rs.getString("data_source")  // Removido gdp
                    );
                    countries.add(country);
                }
            }
            
            System.out.println("? Filtrados " + countries.size() + " pa?ses com % entre " + min + " e " + max);
            
        } catch (SQLException e) {
            System.err.println("? Erro ao filtrar pa?ses: " + e.getMessage());
        }
        
        return countries;
    }
    
    public List<Country> getCountriesByRegion(String region) {
        List<Country> countries = new ArrayList<>();
        String sql = "SELECT * FROM countries WHERE region = ? ORDER BY percentage DESC";
        
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, region);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Country country = new Country(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("code"),
                        rs.getLong("total_population"),
                        rs.getLong("internet_users"),
                        rs.getString("region"),
                        rs.getString("data_source")  // Removido gdp
                    );
                    countries.add(country);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("? Erro ao buscar pa?ses por regi?o: " + e.getMessage());
        }
        
        return countries;
    }
    
    public Country findCountryWithLowestInternetUsersInEurope() {
        String sql = "SELECT * FROM countries WHERE region LIKE '%Europe%' ORDER BY internet_users ASC LIMIT 1";
        
        try (Connection conn = connectionFactory.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return new Country(
                    rs.getLong("id"),
                    rs.getString("name"),
                    rs.getString("code"),
                    rs.getLong("total_population"),
                    rs.getLong("internet_users"),
                    rs.getString("region"),
                    rs.getString("data_source")  // Removido gdp
                );
            }
            
        } catch (SQLException e) {
            System.err.println("? Erro ao buscar pa?s com menor n?mero de usu?rios: " + e.getMessage());
        }
        
        return null;
    }
    
    public List<Object[]> getRegionalStatistics() {
        List<Object[]> statistics = new ArrayList<>();
        String sql = 
            "SELECT region, " +
            "COUNT(*) as country_count, " +
            "ROUND(AVG(percentage), 2) as avg_percentage, " +
            "SUM(internet_users) as total_internet_users, " +
            "SUM(total_population) as total_population " +
            "FROM countries " +
            "GROUP BY region " +
            "ORDER BY avg_percentage DESC";
        
        try (Connection conn = connectionFactory.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Object[] stat = {
                    rs.getString("region"),
                    rs.getInt("country_count"),
                    rs.getDouble("avg_percentage"),
                    rs.getLong("total_internet_users"),
                    rs.getLong("total_population")
                };
                statistics.add(stat);
            }
            
        } catch (SQLException e) {
            System.err.println("? Erro ao obter estat?sticas regionais: " + e.getMessage());
        }
        
        return statistics;
    }
    
    public void insertSampleData() {
        // Verificar se j? existem dados
        String checkSQL = "SELECT COUNT(*) FROM countries";
        
        try (Connection conn = connectionFactory.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(checkSQL)) {
            
            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println("??  Banco j? cont?m dados. Pulando inser??o de exemplo.");
                return;
            }
            
        } catch (SQLException e) {
            System.err.println("??  Erro ao verificar dados: " + e.getMessage());
        }
        
        // Inserir dados de exemplo (pa?ses da Europa Oriental com 75-85%)
        String insertSQL = 
            "INSERT INTO countries (name, code, total_population, internet_users, region, data_source) VALUES " +
            "('Poland', 'PL', 38000000, 32000000, 'Eastern Europe', 'sample'), " +           
            "('Czech Republic', 'CZ', 10700000, 9200000, 'Eastern Europe', 'sample'), " +    // 85.98%
            "('Hungary', 'HU', 9700000, 8100000, 'Eastern Europe', 'sample'), " +           // 83.51%
            "('Romania', 'RO', 19000000, 15500000, 'Eastern Europe', 'sample'), " +         // 81.58%
            "('Bulgaria', 'BG', 6900000, 5300000, 'Eastern Europe', 'sample'), " +          // 76.81%
            "('Slovakia', 'SK', 5400000, 4600000, 'Eastern Europe', 'sample'), " +          // 85.19%
            "('Croatia', 'HR', 4100000, 3400000, 'Eastern Europe', 'sample'), " +           // 82.93%
            "('Slovenia', 'SI', 2100000, 1800000, 'Eastern Europe', 'sample'), " +          // 85.71%
            "('Germany', 'DE', 83000000, 72000000, 'Europe', 'sample'), " +                 // 86.75%
            "('France', 'FR', 67000000, 58000000, 'Europe', 'sample')";                     // 86.57%
        
        try (Connection conn = connectionFactory.getConnection();
             Statement stmt = conn.createStatement()) {
            
            int rows = stmt.executeUpdate(insertSQL);
            System.out.println("? " + rows + " pa?ses de exemplo inseridos no banco");
            
        } catch (SQLException e) {
            System.err.println("? Erro ao inserir dados de exemplo: " + e.getMessage());
        }
    }
    
    // M?todo adicional para o requisito espec?fico
    public Country getCountryWithLowestRegisteredUsersInEasternEurope() {
        String sql = "SELECT * FROM countries WHERE region = 'Eastern Europe' ORDER BY internet_users ASC LIMIT 1";
        
        try (Connection conn = connectionFactory.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                Country country = new Country(
                    rs.getLong("id"),
                    rs.getString("name"),
                    rs.getString("code"),
                    rs.getLong("total_population"),
                    rs.getLong("internet_users"),
                    rs.getString("region"),
                    rs.getString("data_source")
                );
                System.out.println("? Pa?s com MENOR usu?rios na Europa Oriental: " + 
                    country.getName() + " (" + country.getInternetUsers() + " usu?rios)");
                return country;
            }
            
        } catch (SQLException e) {
            System.err.println("Erro: " + e.getMessage());
        }
        
        return null;
    }
    
    // M?todo para pa?ses entre 75% e 85%
    public List<Country> getCountries75to85Percent() {
        return filterByPercentageRange(75.0, 85.0);
    }
    
    // Testar conex?o
    public boolean testConnection() {
        try (Connection conn = connectionFactory.getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    public void close() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}  