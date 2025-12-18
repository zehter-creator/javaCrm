package com.ticari.controller;

import com.ticari.entity.*;
import com.ticari.enums.FaturaTuru;
import com.ticari.enums.StokIslemTuru;
import com.ticari.service.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

@Controller
@RequiredArgsConstructor
public class YeniFaturaController implements Initializable {

    private final FaturaService faturaService;
    private final CariService cariService;
    private final UrunService urunService;
    private final StokService stokService;
    private final ParaBirimiService paraBirimiService;

    // Form fields
    @FXML private TextField txtFaturaNo;
    @FXML private DatePicker dpFaturaTarihi;
    @FXML private DatePicker dpVadeTarihi;
    @FXML private ComboBox<Cari> cmbCari;
    @FXML private ComboBox<FaturaTuru> cmbTur;
    @FXML private ComboBox<ParaBirimi> cmbParaBirimi;
    @FXML private TextField txtKur;
    
    // Product selection
    @FXML private ComboBox<Urun> cmbUrunSec;
    @FXML private TextField txtMiktar;
    @FXML private TextField txtBirimFiyat;
    @FXML private TextField txtSatirToplami;
    @FXML private Button btnSatirEkle;
    
    // Detail table
    @FXML private TableView<FaturaDetayRow> tblFaturaDetay;
    @FXML private TableColumn<FaturaDetayRow, Integer> colSira;
    @FXML private TableColumn<FaturaDetayRow, String> colUrunAd;
    @FXML private TableColumn<FaturaDetayRow, Integer> colMiktar;
    @FXML private TableColumn<FaturaDetayRow, String> colBirim;
    @FXML private TableColumn<FaturaDetayRow, Double> colBirimFiyat;
    @FXML private TableColumn<FaturaDetayRow, Integer> colKdv;
    @FXML private TableColumn<FaturaDetayRow, Double> colSatirTutari;
    
    // Summary labels
    @FXML private Label lblAraToplam;
    @FXML private Label lblKdvToplam;
    @FXML private Label lblGenelToplam;
    @FXML private TextArea txtAciklama;
    @FXML private Button btnFaturayiKaydet;

    private final ObservableList<FaturaDetayRow> detayList = FXCollections.observableArrayList();
    private double araToplam = 0.0;
    private double kdvToplam = 0.0;
    private double genelToplam = 0.0;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupComboBoxes();
        setupTableColumns();
        setupListeners();
        setDefaultValues();
    }

    private void setupComboBoxes() {
        // Load Cari list
        var cariler = cariService.findAll();
        cmbCari.setItems(FXCollections.observableArrayList(cariler));
        cmbCari.setConverter(new StringConverter<>() {
            @Override
            public String toString(Cari cari) {
                return cari != null ? cari.getUnvan() : "";
            }
            @Override
            public Cari fromString(String string) {
                return null;
            }
        });

        // Load Urun list
        var urunler = urunService.findAll();
        cmbUrunSec.setItems(FXCollections.observableArrayList(urunler));
        cmbUrunSec.setConverter(new StringConverter<>() {
            @Override
            public String toString(Urun urun) {
                return urun != null ? urun.getUrunAd() : "";
            }
            @Override
            public Urun fromString(String string) {
                return null;
            }
        });

        // Setup Tur combo
        cmbTur.setItems(FXCollections.observableArrayList(FaturaTuru.values()));

        // Setup Para Birimi
        List<ParaBirimi> paraBirimleri = paraBirimiService.findAll();
        cmbParaBirimi.setItems(FXCollections.observableArrayList(paraBirimleri));
        cmbParaBirimi.setConverter(new StringConverter<ParaBirimi>() {
            @Override
            public String toString(ParaBirimi paraBirimi) {
                return paraBirimi != null ? paraBirimi.getParaKod() : "";
            }
            
            @Override
            public ParaBirimi fromString(String string) {
                return paraBirimiService.findById(string).orElse(null);
            }
        });
    }

    private void setupTableColumns() {
        colSira.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleIntegerProperty(
                detayList.indexOf(cellData.getValue()) + 1
            ).asObject());
        colUrunAd.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getUrunAd()));
        colMiktar.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getMiktar()).asObject());
        colBirim.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty("Adet"));
        colBirimFiyat.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getBirimFiyat()).asObject());
        colKdv.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getKdvOrani()).asObject());
        colSatirTutari.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getSatirTutari()).asObject());

        tblFaturaDetay.setItems(detayList);
    }

    private void setupListeners() {
        // When product is selected, auto-fill price
        cmbUrunSec.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                txtBirimFiyat.setText(String.valueOf(newVal.getMevcutSatisFiyati().doubleValue()));
                calculateRowTotal();
            }
        });

        // Calculate row total when quantity or price changes
        txtMiktar.textProperty().addListener((obs, oldVal, newVal) -> calculateRowTotal());
        txtBirimFiyat.textProperty().addListener((obs, oldVal, newVal) -> calculateRowTotal());
    }

    private void setDefaultValues() {
        dpFaturaTarihi.setValue(LocalDate.now());
        dpVadeTarihi.setValue(LocalDate.now().plusDays(30));
        
        // Set default currency to TRY
        paraBirimiService.findById("TRY").ifPresent(cmbParaBirimi::setValue);
        
        txtKur.setText("1.00");
        txtFaturaNo.setText("FAT-" + System.currentTimeMillis());
    }

    private void calculateRowTotal() {
        try {
            int miktar = Integer.parseInt(txtMiktar.getText().trim());
            double birimFiyat = Double.parseDouble(txtBirimFiyat.getText().trim());
            double total = miktar * birimFiyat;
            txtSatirToplami.setText(String.format("%.2f", total));
        } catch (NumberFormatException e) {
            txtSatirToplami.setText("0.00");
        }
    }

    @FXML
    private void handleSatirEkle() {
        Urun selectedUrun = cmbUrunSec.getValue();
        if (selectedUrun == null) {
            showAlert("Uyarı", "Lütfen bir ürün seçin", Alert.AlertType.WARNING);
            return;
        }

        try {
            int miktar = Integer.parseInt(txtMiktar.getText().trim());
            double birimFiyat = Double.parseDouble(txtBirimFiyat.getText().trim());
            int kdvOrani = 20; // Default KDV
            
            FaturaDetayRow row = new FaturaDetayRow();
            row.setUrunId(selectedUrun.getUrunId());
            row.setUrunAd(selectedUrun.getUrunAd());
            row.setMiktar(miktar);
            row.setBirimFiyat(birimFiyat);
            row.setKdvOrani(kdvOrani);
            
            double satirToplami = miktar * birimFiyat;
            double satirKdv = satirToplami * kdvOrani / 100.0;
            row.setSatirTutari(satirToplami + satirKdv);
            
            detayList.add(row);
            calculateTotals();
            clearProductSelection();
            
        } catch (NumberFormatException e) {
            showAlert("Hata", "Miktar ve birim fiyat sayısal olmalıdır", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleSeciliSatirSil() {
        FaturaDetayRow selected = tblFaturaDetay.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Uyarı", "Lütfen silmek için bir satır seçin", Alert.AlertType.WARNING);
            return;
        }
        detayList.remove(selected);
        calculateTotals();
    }

    @FXML
    private void handleFaturayiKaydet() {
        if (!validateForm()) {
            return;
        }

        try {
            Fatura fatura = new Fatura();
            fatura.setFaturaNo(txtFaturaNo.getText().trim());
            fatura.setFaturaTarihi(dpFaturaTarihi.getValue().atStartOfDay());
            fatura.setCari(cmbCari.getValue());
            fatura.setTur(cmbTur.getValue());
            fatura.setGenelToplam(BigDecimal.valueOf(genelToplam));
            fatura.setParaBirimi(cmbParaBirimi.getValue());
            fatura.setKur(new BigDecimal(txtKur.getText().trim()));
            fatura.setAciklama(txtAciklama.getText());

            // Save fatura
            Fatura savedFatura = faturaService.save(fatura);

            // Create stock movements
            for (FaturaDetayRow row : detayList) {
                StokGirisCikis stok = new StokGirisCikis();
                stok.setFatura(savedFatura);
                stok.setUrun(urunService.findById(row.getUrunId()).orElse(null));
                stok.setIslemTuru(cmbTur.getValue() == FaturaTuru.ALIS ? StokIslemTuru.GIRIS : StokIslemTuru.CIKIS);
                stok.setMiktar(row.getMiktar());
                stok.setBirimFiyat(BigDecimal.valueOf(row.getBirimFiyat()));
                stok.setTarih(LocalDateTime.now());
                stok.setParaBirimi(cmbParaBirimi.getValue());
                stok.setKur(new BigDecimal(txtKur.getText().trim()));
                
                stokService.save(stok);
            }

            showAlert("Başarılı", "Fatura başarıyla kaydedildi!\nFatura No: " + savedFatura.getFaturaNo(), 
                     Alert.AlertType.INFORMATION);
            clearForm();

        } catch (Exception e) {
            showAlert("Hata", "Fatura kaydedilirken hata oluştu: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleIptal() {
        clearForm();
    }

    private boolean validateForm() {
        if (txtFaturaNo.getText().trim().isEmpty()) {
            showAlert("Uyarı", "Fatura numarası boş olamaz", Alert.AlertType.WARNING);
            return false;
        }
        if (cmbCari.getValue() == null) {
            showAlert("Uyarı", "Lütfen bir cari seçin", Alert.AlertType.WARNING);
            return false;
        }
        if (cmbTur.getValue() == null) {
            showAlert("Uyarı", "Lütfen işlem türü seçin", Alert.AlertType.WARNING);
            return false;
        }
        if (detayList.isEmpty()) {
            showAlert("Uyarı", "Faturaya en az bir ürün eklenmelidir", Alert.AlertType.WARNING);
            return false;
        }
        return true;
    }

    private void calculateTotals() {
        araToplam = 0.0;
        kdvToplam = 0.0;
        
        for (FaturaDetayRow row : detayList) {
            double satirToplami = row.getMiktar() * row.getBirimFiyat();
            double satirKdv = satirToplami * row.getKdvOrani() / 100.0;
            araToplam += satirToplami;
            kdvToplam += satirKdv;
        }
        
        genelToplam = araToplam + kdvToplam;
        
        lblAraToplam.setText(String.format("%.2f ₺", araToplam));
        lblKdvToplam.setText(String.format("%.2f ₺", kdvToplam));
        lblGenelToplam.setText(String.format("%.2f ₺", genelToplam));
    }

    private void clearProductSelection() {
        cmbUrunSec.setValue(null);
        txtMiktar.clear();
        txtBirimFiyat.clear();
        txtSatirToplami.clear();
    }

    private void clearForm() {
        txtFaturaNo.setText("FAT-" + System.currentTimeMillis());
        dpFaturaTarihi.setValue(LocalDate.now());
        dpVadeTarihi.setValue(LocalDate.now().plusDays(30));
        cmbCari.setValue(null);
        cmbTur.setValue(null);
        txtAciklama.clear();
        detayList.clear();
        calculateTotals();
        clearProductSelection();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Data
    public static class FaturaDetayRow {
        private Integer urunId;
        private String urunAd;
        private Integer miktar;
        private Double birimFiyat;
        private Integer kdvOrani;
        private Double satirTutari;
    }
}
