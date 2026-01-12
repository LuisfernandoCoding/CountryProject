package model;

import java.util.ArrayList;
import java.util.List;

public class StatisticsModel {
    private List<Country> countries;
    
    public StatisticsModel() {
        this.countries = new ArrayList<>();
    }
    
    public void setCountries(List<Country> countries) {
        this.countries = countries;
    }
    
    public List<Country> getCountries() {
        return new ArrayList<>(countries);
    }
    
    public List<Country> filterByPercentage(double min, double max) {
        List<Country> filtered = new ArrayList<>();
        for (Country country : countries) {
            if (country.getPercentage() >= min && country.getPercentage() <= max) {
                filtered.add(country);
            }
        }
        return filtered;
    }
    
    public int getTotalCountries() {
        return countries.size();
    }
    
    public long getTotalPopulation() {
        long total = 0;
        for (Country country : countries) {
            total += country.getTotalPopulation();
        }
        return total;
    }
    
    public long getTotalInternetUsers() {
        long total = 0;
        for (Country country : countries) {
            total += country.getInternetUsers();
        }
        return total;
    }
}