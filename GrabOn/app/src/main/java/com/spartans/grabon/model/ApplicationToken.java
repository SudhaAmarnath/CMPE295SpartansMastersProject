package com.spartans.grabon.model;

public class ApplicationToken {
    private String access_token;
    private String expires_in;
    private String refresh_token;
    private String token_type;

    public ApplicationToken() {
    }

    public String getAccess_token() {
        return access_token;
    }

    public String getExpires_in() {
        return expires_in;
    }

    public String getRefresh_token() {
        return refresh_token;
    }

    public String getToken_type() {
        return token_type;
    }
}
