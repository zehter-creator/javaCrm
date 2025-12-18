package com.ticari.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbHelper {

    // 👇 DİKKAT: Kullanıcı adı ve şifreyi buraya, URL'nin içine gömdük.
    // Bu sayede Türkçe karakter/kodlama hatası olma ihtimalini sıfıra indiriyoruz.
    private static final String URL = "jdbc:sqlserver://localhost:1433;"
            + "databaseName=envanter;"
            + "user=patron;"
            + "password=12345678;"
            + "encrypt=true;"
            + "trustServerCertificate=true;"
            + "loginTimeout=30;"; // 30 saniye bekleme süresi

    public static Connection baglan() throws SQLException {
        try {
            // Sürücüyü yükle
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

            // URL içinde şifre olduğu için tekrar kullanıcı adı/şifre vermiyoruz
            return DriverManager.getConnection(URL);

        } catch (ClassNotFoundException e) {
            System.out.println("❌ Sürücü Hatası: Kütüphane bulunamadı!");
            return null;
        } catch (SQLException e) {
            System.out.println("❌ Bağlantı Hatası!");
            System.out.println("Hata Mesajı: " + e.getMessage());
            throw e;
        }
    }

    public static void main(String[] args) {
        System.out.println("🔌 Bağlantı testi başlatılıyor...");
        try (Connection conn = baglan()) {
            System.out.println("✅ BAŞARILI! Veritabanına giriş yapıldı.");
        } catch (SQLException e) {
            // Hata zaten yukarıda yazıldı
        }
    }
}