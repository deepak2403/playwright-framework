package api.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LoginRequest {

    @JsonProperty("email")
    private String email;

    @JsonProperty("password")
    private String password;

    public static LoginRequest of(String email, String password) {
        LoginRequest r = new LoginRequest();
        r.email    = email;
        r.password = password;
        return r;
    }

    public String getEmail()               { return email; }
    public void   setEmail(String email)   { this.email = email; }
    public String getPassword()                { return password; }
    public void   setPassword(String password) { this.password = password; }
}