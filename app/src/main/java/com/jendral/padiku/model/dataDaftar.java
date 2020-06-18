package com.jendral.padiku.model;

public class dataDaftar {
    private String key;
    private String username;
    private String nama;
    private String email;
    private String kota;
    private String telepon;
    private String gambar;

    public dataDaftar() {
    }

    public dataDaftar(String username, String nama, String email, String kota, String telepon, String gambar) {
        this.username = username;
        this.nama = nama;
        this.email = email;
        this.kota = kota;
        this.telepon = telepon;
        this.gambar = gambar;
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

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getKota() {
        return kota;
    }

    public void setKota(String kota) {
        this.kota = kota;
    }

    public String getTelepon() {
        return telepon;
    }

    public void setTelepon(String telepon) {
        this.telepon = telepon;
    }

    public String getGambar() {
        return gambar;
    }

    public void setGambar(String gambar) {
        this.gambar = gambar;
    }
}
