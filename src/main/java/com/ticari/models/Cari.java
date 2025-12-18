package com.ticari.models;

public class Cari {
    private int cariID;
    private String cariKod;
    private String unvan;
    private String tur;       // ALICI, SATICI
    private String vergiNo;
    private double guncelBakiye;

    public Cari() {}

    // Yeni Ekleme Constructor'ı
    public Cari(String cariKod, String unvan, String tur, String vergiNo) {
        this.cariKod = cariKod;
        this.unvan = unvan;
        this.tur = tur;
        this.vergiNo = vergiNo;
        this.guncelBakiye = 0.0;
    }

    // Okuma Constructor'ı
    public Cari(int cariID, String cariKod, String unvan, String tur, String vergiNo, double guncelBakiye) {
        this.cariID = cariID;
        this.cariKod = cariKod;
        this.unvan = unvan;
        this.tur = tur;
        this.vergiNo = vergiNo;
        this.guncelBakiye = guncelBakiye;
    }

    // Getters & Setters
    public int getCariID() { return cariID; }
    public void setCariID(int cariID) { this.cariID = cariID; }

    public String getCariKod() { return cariKod; }
    public void setCariKod(String cariKod) { this.cariKod = cariKod; }

    public String getUnvan() { return unvan; }
    public void setUnvan(String unvan) { this.unvan = unvan; }

    public String getTur() { return tur; }
    public void setTur(String tur) { this.tur = tur; }

    public String getVergiNo() { return vergiNo; }
    public void setVergiNo(String vergiNo) { this.vergiNo = vergiNo; }

    public double getGuncelBakiye() { return guncelBakiye; }
    public void setGuncelBakiye(double guncelBakiye) { this.guncelBakiye = guncelBakiye; }

    @Override
    public String toString() {
        return unvan; // ComboBox'ta sadece ismin görünmesi için
    }
}