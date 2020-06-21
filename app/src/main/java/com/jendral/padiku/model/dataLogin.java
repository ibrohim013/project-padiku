package com.jendral.padiku.model;

public class dataLogin {
    private String key;
    private String username;
    private String nama;
    private String password;
    private String level;

    public dataLogin() {
    }

    public dataLogin(String username, String nama, String password, String level) {
        this.username = username;
        this.nama = nama;
        this.password = password;
        this.level = level;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNama() { return nama; }

    public void setNama(String nama) {
        this.nama = nama;
    }
}
