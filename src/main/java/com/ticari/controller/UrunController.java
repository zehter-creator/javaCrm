package com.ticari.controller;

import com.ticari.entity.Urun;
import com.ticari.service.UrunService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;

@Controller
public class UrunController {
    
    private final UrunService urunService;
    
    @FXML
    private TextField txtArama;
    
    @FXML
    private Button btnYeniUrun;
    
    @FXML
    private TableView<Urun> tblUrunler;
    
    @FXML
    private TableColumn<Urun, Integer> colId;
    
    @FXML
    private TableColumn<Urun, String> colUrunAd;
    
    @FXML
    private TableColumn<Urun, String> colKategori;
    
    @FXML
    private TableColumn<Urun, Integer> colStokMiktar;
    
    @FXML
    private TableColumn<Urun, Integer> colMinStok;
    
    @FXML
    private TableColumn<Urun, BigDecimal> colSatisFiyat;
    
    @FXML
    private TableColumn<Urun, String> colDurum;
    
    @FXML
    private Button btnSil;
    
    @FXML
    private Button btnDuzenle;
    
    private ObservableList<Urun> urunListesi;
    
    public UrunController(UrunService urunService) {
        this.urunService = urunService;
    }
    
    @FXML
    public void initialize() {
        setupTableColumns();
        loadUrunler();
        setupSearchListener();
    }
    
    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("urunId"));
        colUrunAd.setCellValueFactory(new PropertyValueFactory<>("urunAd"));
        colKategori.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getKategori() != null ? 
                cellData.getValue().getKategori().getKategoriAd() : ""
            )
        );
        colStokMiktar.setCellValueFactory(new PropertyValueFactory<>("mevcutStokMiktari"));
        colMinStok.setCellValueFactory(new PropertyValueFactory<>("minimumStokSeviyesi"));
        colSatisFiyat.setCellValueFactory(new PropertyValueFactory<>("mevcutSatisFiyati"));
        
        colDurum.setCellFactory(col -> new TableCell<Urun, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                    setStyle("");
                } else {
                    Urun urun = getTableRow().getItem();
                    if (urun.getMevcutStokMiktari() == 0) {
                        setText("TÜKENDİ");
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    } else if (urun.getMevcutStokMiktari() <= urun.getMinimumStokSeviyesi()) {
                        setText("DÜŞÜK");
                        setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                    } else {
                        setText("NORMAL");
                        setStyle("-fx-text-fill: #27ae60;");
                    }
                }
            }
        });
    }
    
    private void loadUrunler() {
        urunListesi = FXCollections.observableArrayList(urunService.tumunuGetir());
        tblUrunler.setItems(urunListesi);
    }
    
    private void setupSearchListener() {
        txtArama.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                loadUrunler();
            } else {
                var filteredList = urunService.urunAdaGoreAra(newValue);
                urunListesi = FXCollections.observableArrayList(filteredList);
                tblUrunler.setItems(urunListesi);
            }
        });
    }
    
    @FXML
    public void handleYeniUrun() {
        showAlert("Bilgi", "Yeni Ürün ekleme formu açılacak", Alert.AlertType.INFORMATION);
    }
    
    @FXML
    public void handleSil() {
        Urun selectedUrun = tblUrunler.getSelectionModel().getSelectedItem();
        if (selectedUrun == null) {
            showAlert("Uyarı", "Lütfen bir ürün seçiniz", Alert.AlertType.WARNING);
            return;
        }
        
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Onay");
        confirmation.setHeaderText("Silme İşlemi");
        confirmation.setContentText(selectedUrun.getUrunAd() + " silinecek. Emin misiniz?");
        
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                urunService.sil(selectedUrun.getUrunId());
                loadUrunler();
                showAlert("Başarılı", "Ürün silindi", Alert.AlertType.INFORMATION);
            }
        });
    }
    
    @FXML
    public void handleDuzenle() {
        Urun selectedUrun = tblUrunler.getSelectionModel().getSelectedItem();
        if (selectedUrun == null) {
            showAlert("Uyarı", "Lütfen bir ürün seçiniz", Alert.AlertType.WARNING);
            return;
        }
        showAlert("Bilgi", "Düzenleme formu açılacak: " + selectedUrun.getUrunAd(), Alert.AlertType.INFORMATION);
    }
    
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
