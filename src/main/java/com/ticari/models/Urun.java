package com.ticari.models;

public class Urun {
    // Veritabanındaki kolonların birebir aynısı
    private int urunID;
    private String urunAd;
    private int kategoriID;
    private String kategoriAd; // Tabloda ID yerine isim göstermek için (JOIN ile gelecek)
    private double mevcutSatisFiyati;
    private int minimumStokSeviyesi;
    private int mevcutStokMiktari;
    private int kdvOrani;

    // --- Constructor (Yapıcı Metotlar) ---

    // Boş yapıcı (Lazım olabilir)
    public Urun() {}

    // Yeni ürün eklerken kullanacağımız yapıcı (ID yok, çünkü otomatik artıyor)
    public Urun(String urunAd, int kategoriID, double mevcutSatisFiyati, int minimumStokSeviyesi, int kdvOrani) {
        this.urunAd = urunAd;
        this.kategoriID = kategoriID;
        this.mevcutSatisFiyati = mevcutSatisFiyati;
        this.minimumStokSeviyesi = minimumStokSeviyesi;
        this.kdvOrani = kdvOrani;
    }

    // Veritabanından okurken kullanacağımız yapıcı (Her şey dahil)
    public Urun(int urunID, String urunAd, String kategoriAd, double fiyat, int stok, int minStok) {
        this.urunID = urunID;
        this.urunAd = urunAd;
        this.kategoriAd = kategoriAd;
        this.mevcutSatisFiyati = fiyat;
        this.mevcutStokMiktari = stok;
        this.minimumStokSeviyesi = minStok;
    }

    // --- Getter ve Setter Metotları (Sağ Tık -> Generate -> Getter and Setter diyerek de yapabilirsin) ---

    public int getUrunID() { return urunID; }
    public void setUrunID(int urunID) { this.urunID = urunID; }

    public String getUrunAd() { return urunAd; }
    public void setUrunAd(String urunAd) { this.urunAd = urunAd; }

    public int getKategoriID() { return kategoriID; }
    public void setKategoriID(int kategoriID) { this.kategoriID = kategoriID; }

    public String getKategoriAd() { return kategoriAd; }
    public void setKategoriAd(String kategoriAd) { this.kategoriAd = kategoriAd; }

    public double getMevcutSatisFiyati() { return mevcutSatisFiyati; }
    public void setMevcutSatisFiyati(double mevcutSatisFiyati) { this.mevcutSatisFiyati = mevcutSatisFiyati; }

    public int getMinimumStokSeviyesi() { return minimumStokSeviyesi; }
    public void setMinimumStokSeviyesi(int minimumStokSeviyesi) { this.minimumStokSeviyesi = minimumStokSeviyesi; }

    public int getMevcutStokMiktari() { return mevcutStokMiktari; }
    public void setMevcutStokMiktari(int mevcutStokMiktari) { this.mevcutStokMiktari = mevcutStokMiktari; }

    public int getKdvOrani() { return kdvOrani; }
    public void setKdvOrani(int kdvOrani) { this.kdvOrani = kdvOrani; }
}