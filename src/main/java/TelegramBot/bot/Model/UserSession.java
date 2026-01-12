package TelegramBot.bot.model;

import model.Country;
import java.util.List;
import java.util.ArrayList;

public class UserSession {
    private final Long chatId;
    private final String userName;
    private List<Country> loadedCountries;
    private String lastFilter;
    
    public UserSession(Long chatId, String userName) {
        this.chatId = chatId;
        this.userName = userName;
        this.loadedCountries = new ArrayList<>();
    }
    
    public Long getChatId() {
        return chatId;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public List<Country> getLoadedCountries() {
        return loadedCountries;
    }
    
    public void setLoadedCountries(List<Country> countries) {
        this.loadedCountries = countries;
    }
    
    public String getLastFilter() {
        return lastFilter;
    }
    
    public void setLastFilter(String filter) {
        this.lastFilter = filter;
    }
    
    public void addCountry(Country country) {
        loadedCountries.add(country);
    }
    
    public void clearCountries() {
        loadedCountries.clear();
    }
}