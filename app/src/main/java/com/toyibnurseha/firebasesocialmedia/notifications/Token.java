package com.toyibnurseha.firebasesocialmedia.notifications;

public class Token {
    /* an FCM Token, or much commonly known as a registrationToken.
    an Id issued by the GCM Connection servers to the client app that allows it to receive messages
     */

    String token;

    public Token(String token) {
        this.token = token;
    }

    public Token() {
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
