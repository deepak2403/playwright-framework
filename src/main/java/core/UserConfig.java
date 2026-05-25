package core;

public class UserConfig {

    private final String email;
    private final String password;
    private final String searchTerm;
    private final String brand;
    private final String secondaryKeyword; // e.g. "5050", "M3", "i7"
    private final String ramSize;          // e.g. "16"
    private final String ramType;          // e.g. "DDR5", "LPDDR5"

    public UserConfig(String email, String password, String searchTerm,
                      String brand, String secondaryKeyword,
                      String ramSize, String ramType) {
        this.email             = email;
        this.password          = password;
        this.searchTerm        = searchTerm;
        this.brand             = brand;
        this.secondaryKeyword  = secondaryKeyword;
        this.ramSize           = ramSize;
        this.ramType           = ramType;
    }

    public String getEmail()             { return email; }
    public String getPassword()          { return password; }
    public String getSearchTerm()        { return searchTerm; }
    public String getBrand()             { return brand; }
    public String getSecondaryKeyword()  { return secondaryKeyword; }
    public String getRamSize()           { return ramSize; }
    public String getRamType()           { return ramType; }

    @Override
    public String toString() {
        return email + " | " + searchTerm + " | " + brand;
    }
}