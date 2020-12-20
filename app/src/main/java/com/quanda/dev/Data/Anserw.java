package com.quanda.dev.Data;

public class Anserw {
    private String username;
    private String content;

    public Anserw(String username, String content){
        this.username = username;
        this.content = content;
    }

    public String getUsername() {
        return username;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return "Anserw{" +
                "username='" + username + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
