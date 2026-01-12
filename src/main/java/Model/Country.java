package model;

import java.time.LocalDateTime;

public class Country {
    private Long id;
    private String name;
    private String code;
    private long totalPopulation;
    private long internetUsers;
    private String region;
    private double percentage;
    private LocalDateTime lastUpdated;
    private String dataSource;

    // Construtor simplificado (sem GDP)
    public Country(Long id, String name, String code, long totalPopulation, 
                   long internetUsers, String region, String dataSource) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.totalPopulation = totalPopulation;
        this.internetUsers = internetUsers;
        this.region = region;
        this.dataSource = dataSource;
        calculatePercentage();
        this.lastUpdated = LocalDateTime.now();
    }

    // Construtor b?sico
    public Country(String name, long totalPopulation, long internetUsers, String region) {
        this.name = name;
        this.totalPopulation = totalPopulation;
        this.internetUsers = internetUsers;
        this.region = region;
        calculatePercentage();
    }

    private void calculatePercentage() {
        this.percentage = (totalPopulation > 0) ? 
            (double) internetUsers / totalPopulation * 100 : 0;
    }

    // Getters e Setters (removi os getters/setters do GDP)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public long getTotalPopulation() { return totalPopulation; }
    public void setTotalPopulation(long totalPopulation) { 
        this.totalPopulation = totalPopulation; 
        calculatePercentage();
    }

    public long getInternetUsers() { return internetUsers; }
    public void setInternetUsers(long internetUsers) { 
        this.internetUsers = internetUsers; 
        calculatePercentage();
    }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public double getPercentage() { return percentage; }

    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { 
        this.lastUpdated = lastUpdated; 
    }

    public String getDataSource() { return dataSource; }
    public void setDataSource(String dataSource) { 
        this.dataSource = dataSource; 
    }

    @Override
    public String toString() {
        return String.format("%s: %,d usu?rios (%.2f%%) - %s", 
            name, internetUsers, percentage, region);
    }
}