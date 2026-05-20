package api.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginResponse {

    @JsonProperty("responseCode")
    private int responseCode;

    @JsonProperty("message")
    private String message;

    @JsonProperty("token")
    private String token;

    public int    getResponseCode()              { return responseCode; }
    public void   setResponseCode(int rc)        { this.responseCode = rc; }
    public String getMessage()                   { return message; }
    public void   setMessage(String message)     { this.message = message; }
    public String getToken()                     { return token; }
    public void   setToken(String token)         { this.token = token; }

    public boolean isSuccess() { return responseCode == 200; }
}