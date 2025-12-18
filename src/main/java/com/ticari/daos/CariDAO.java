package com.ticari.daos;

import com.ticari.utils.DbHelper;
import com.ticari.models.Cari;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CariDAO {

    // CARİLERİ GETİR
    public List<Cari> tumunuGetir() {
        List<Cari> liste = new ArrayList<>();
        String sql = "SELECT * FROM Cariler ORDER BY Unvan";

        try (Connection conn = DbHelper.baglan();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                liste.add(new Cari(
                        rs.getInt("CariID"),
                        rs.getString("CariKod"),
                        rs.getString("Unvan"),
                        rs.getString("Tur"),
                        rs.getString("VergiNo"),
                        rs.getDouble("GuncelBakiye")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return liste;
    }

    // YENİ CARİ EKLE
    public boolean cariEkle(Cari cari) {
        String sql = "INSERT INTO Cariler (CariKod, Unvan, Tur, VergiNo, GuncelBakiye) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DbHelper.baglan();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cari.getCariKod());
            stmt.setString(2, cari.getUnvan());
            stmt.setString(3, cari.getTur());
            stmt.setString(4, cari.getVergiNo());
            stmt.setDouble(5, 0.0);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Cari Ekleme Hatası: " + e.getMessage());
            return false;
        }
    }
}