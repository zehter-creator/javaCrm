package com.ticari.controller;

import com.ticari.entity.Urun;
import com.ticari.service.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;

import java.net.URL;
import java.util.ResourceBundle;

@Controller
@RequiredArgsConstructor
public class DashboardController implements Initializable {

    private final KasaBankaService kasaBankaService;
    private final CariService cariService;
    private final UrunService urunService;
    private final FaturaService faturaService;

    // Summary cards
    @FXML private Label lblToplamKasa;
    @FXML private Label lblAlacaklar;
    @FXML private Label lblStokDegeri;
    @FXML private Label lblKritikStokSayisi;
    
    // Charts
    @FXML private BarChart<String, Number> chartSatislar;
    @FXML private PieChart chartKategoriler;
    
    // Critical stock table
    @FXML private TableView<Urun> tblKritikStok;
    @FXML private TableColumn<Urun, Integer> colUrunId;
    @FXML private TableColumn<Urun, String> colUrunAd;
    @FXML private TableColumn<Urun, Integer> colKalan;
    @FXML private TableColumn<Urun, Integer> colMinSinir;
    
    // Recent invoices table
    @FXML private TableView<?> tblSonIslemler;
    
    // Search
    @FXML private TextField txtArama;
    @FXML private Button btnSorgula;
    
    // Navigation buttons
    @FXML private Button btnGenelBakis;
    @FXML private Button btnUrunStok;
    @FXML private Button btnCariHesaplar;
    @FXML private Button btnFaturalar;
    @FXML private Button btnKasaBanka;
    @FXML private Button btnTeklifler;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadSummaryData();
        setupCharts();
        setupCriticalStockTable();
    }

    private void loadSummaryData() {
        // Load total cash
        double toplamKasa = kasaBankaService.getToplamBakiye().doubleValue();
        lblToplamKasa.setText(String.format("₺ %.2f", toplamKasa));
        
        // Load receivables (customers with positive balance)
        double alacaklar = cariService.findAll().stream()
            .filter(c -> c.getGuncelBakiye() != null && c.getGuncelBakiye().doubleValue() > 0)
            .mapToDouble(c -> c.getGuncelBakiye().doubleValue())
            .sum();
        lblAlacaklar.setText(String.format("₺ %.2f", alacaklar));
        
        // Calculate total stock value
        double stokDegeri = urunService.findAll().stream()
            .mapToDouble(u -> u.getMevcutStokMiktari() * u.getMevcutSatisFiyati().doubleValue())
            .sum();
        lblStokDegeri.setText(String.format("₺ %.2f", stokDegeri));
        
        // Count critical stock items
        long kritikStokSayisi = urunService.findLowStockProducts().size();
        lblKritikStokSayisi.setText(kritikStokSayisi + " Ürün");
    }

    private void setupCharts() {
        // Setup sales bar chart (sample data)
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Satışlar");
        series.getData().add(new XYChart.Data<>("Pzt", 12500));
        series.getData().add(new XYChart.Data<>("Sal", 15200));
        series.getData().add(new XYChart.Data<>("Çar", 18900));
        series.getData().add(new XYChart.Data<>("Per", 14300));
        series.getData().add(new XYChart.Data<>("Cum", 21000));
        series.getData().add(new XYChart.Data<>("Cmt", 8500));
        series.getData().add(new XYChart.Data<>("Paz", 6200));
        
        if (chartSatislar != null) {
            chartSatislar.getData().add(series);
        }
        
        // Setup category pie chart (sample data)
        if (chartKategoriler != null) {
            PieChart.Data slice1 = new PieChart.Data("Elektronik", 35);
            PieChart.Data slice2 = new PieChart.Data("Gıda", 25);
            PieChart.Data slice3 = new PieChart.Data("Giyim", 20);
            PieChart.Data slice4 = new PieChart.Data("Mobilya", 15);
            PieChart.Data slice5 = new PieChart.Data("Diğer", 5);
            
            chartKategoriler.setData(FXCollections.observableArrayList(
                slice1, slice2, slice3, slice4, slice5
            ));
        }
    }

    private void setupCriticalStockTable() {
        if (tblKritikStok != null) {
            colUrunId.setCellValueFactory(new PropertyValueFactory<>("urunId"));
            colUrunAd.setCellValueFactory(new PropertyValueFactory<>("urunAd"));
            colKalan.setCellValueFactory(new PropertyValueFactory<>("mevcutStokMiktari"));
            colMinSinir.setCellValueFactory(new PropertyValueFactory<>("minimumStokSeviyesi"));
            
            // Load critical stock items
            var kritikUrunler = urunService.findLowStockProducts();
            tblKritikStok.setItems(FXCollections.observableArrayList(kritikUrunler));
            
            // Color code the rows
            colKalan.setCellFactory(column -> new TableCell<>() {
                @Override
                protected void updateItem(Integer item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(String.valueOf(item));
                        if (item == 0) {
                            setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                        } else {
                            setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                        }
                    }
                }
            });
        }
    }

    @FXML
    private void handleSorgula() {
        String searchText = txtArama.getText();
        if (searchText == null || searchText.trim().isEmpty()) {
            showAlert("Uyarı", "Lütfen arama terimi girin", Alert.AlertType.WARNING);
            return;
        }
        showAlert("Bilgi", "Arama özelliği: '" + searchText + "' için sonuçlar gösterilecek", 
                 Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleGenelBakis() {
        loadSummaryData();
        showAlert("Bilgi", "Genel Bakış sayfası yenilendi", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleUrunStok() {
        showAlert("Bilgi", "Ürün ve Stok sayfasına yönlendirilecek", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleCariHesaplar() {
        showAlert("Bilgi", "Cari Hesaplar sayfasına yönlendirilecek", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleFaturalar() {
        showAlert("Bilgi", "Faturalar sayfasına yönlendirilecek", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleKasaBanka() {
        showAlert("Bilgi", "Kasa ve Banka sayfasına yönlendirilecek", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleTeklifler() {
        showAlert("Bilgi", "Teklifler sayfasına yönlendirilecek", Alert.AlertType.INFORMATION);
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
