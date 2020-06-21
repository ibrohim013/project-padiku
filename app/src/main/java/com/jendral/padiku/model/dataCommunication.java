package com.jendral.padiku.model;

public class dataCommunication {
    private String key;

    private String dari;
    private String pesan;
    private String tanggal;
    private long waktu;
    private String jenis;
    private String image;
    private String push;
    private String level;

    public dataCommunication() {
    }

    public dataCommunication(String key, String dari, String pesan, String tanggal, long waktu, String jenis,String level) {
        this.key = key;
        this.dari = dari;
        this.pesan = pesan;
        this.tanggal = tanggal;
        this.waktu = waktu;
        this.jenis = jenis;
        this.level = level;
    }

    public dataCommunication(String key, String dari, String pesan, String tanggal, long waktu, String jenis, String image,String level) {
        this.key = key;
        this.dari = dari;
        this.pesan = pesan;
        this.tanggal = tanggal;
        this.waktu = waktu;
        this.jenis = jenis;
        this.image = image;
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

    public String getDari() {
        return dari;
    }

    public void setDari(String dari) {
        this.dari = dari;
    }

    public String getPesan() {
        return pesan;
    }

    public void setPesan(String pesan) {
        this.pesan = pesan;
    }

    public long getWaktu() {
        return waktu;
    }

    public void setWaktu(long waktu) {
        this.waktu = waktu;
    }

    public String getJenis() {
        return jenis;
    }

    public void setJenis(String jenis) {
        this.jenis = jenis;
    }

    public String getTanggal() {
        return tanggal;
    }

    public void setTanggal(String tanggal) {
        this.tanggal = tanggal;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getPush() {
        return push;
    }

    public void setPush(String push) {
        this.push = push;
    }
}
