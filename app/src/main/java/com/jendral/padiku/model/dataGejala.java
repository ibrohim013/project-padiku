package com.jendral.padiku.model;

public class dataGejala {
    private  String key;
    private  String judul;
    private  String gejala;
    private  String solusi;
    private  String gambar;

    public dataGejala(String judul, String gejala, String solusi, String gambar) {
        this.judul = judul;
        this.gejala = gejala;
        this.solusi = solusi;
        this.gambar = gambar;
    }

    public dataGejala() {
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public String getJudul() {
        return judul;
    }

    public String getGejala() {
        return gejala;
    }

    public String getGambar() {
        return gambar;
    }

    public String getSolusi() {
        return solusi;
    }
}
