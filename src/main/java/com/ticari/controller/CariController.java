package com.ticari.controller;

import com.ticari.entity.Cari;
import com.ticari.service.CariService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;

@Controller
public class CariController {
    
    private final CariService cariService;
    
    @FXML
    private TextField txtArama;
    
    @FXML
    private Button btnYeniCari;
    
    @FXML
    private TableView<Cari> tblCariler;
    
    @FXML
    private TableColumn<Cari, String> colCariKod;
    
    @FXML
    private TableColumn<Cari, String> colUnvan;
    
    @FXML
    private TableColumn<Cari, String> colTur;
    
    @FXML
    private TableColumn<Cari, String> colVergiNo;
    
    @FXML
    private TableColumn<Cari, BigDecimal> colBakiye;
    
    @FXML
    private TableColumn<Cari, String> colDurum;
    
    @FXML
    private Button btnEkstre;
    
    @FXML
    private Button btnSil;
    
    @FXML
    private Button btnDuzenle;
    
    private ObservableList<Cari> cariListesi;
    
    public CariController(CariService cariService) {
        this.cariService = cariService;
    }
    
    @FXML
    public void initialize() {
        setupTableColumns();
        loadCariler();
        setupSearchListener();
    }
    
    private void setupTableColumns() {
        colCariKod.setCellValueFactory(new PropertyValueFactory<>("cariKod"));
        colUnvan.setCellValueFactory(new PropertyValueFactory<>("unvan"));
        colTur.setCellValueFactory(new PropertyValueFactory<>("tur"));
        colVergiNo.setCellValueFactory(new PropertyValueFactory<>("vergiNo"));
        colBakiye.setCellValueFactory(new PropertyValueFactory<>("guncelBakiye"));
        
        colDurum.setCellFactory(col -> new TableCell<Cari, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                    setStyle("");
                } else {
                    Cari cari = getTableRow().getItem();
                    if (cari.getGuncelBakiye().compareTo(BigDecimal.ZERO) > 0) {
                        setText("BORÇLU");
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    } else if (cari.getGuncelBakiye().compareTo(BigDecimal.ZERO) < 0) {
                        setText("ALACAKLI");
                        setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    } else {
                        setText("DENGELİ");
                        setStyle("-fx-text-fill: #95a5a6;");
                    }
                }
            }
        });
    }
    
    private void loadCariler() {
        cariListesi = FXCollections.observableArrayList(cariService.tumunuGetir());
        tblCariler.setItems(cariListesi);
    }
    
    private void setupSearchListener() {
        txtArama.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                loadCariler();
            } else {
                var filteredList = cariService.unvanaGoreAra(newValue);
                cariListesi = FXCollections.observableArrayList(filteredList);
                tblCariler.setItems(cariListesi);
            }
        });
    }
    
    @FXML
    public void handleYeniCari() {
        showAlert("Bilgi", "Yeni Cari ekleme formu açılacak", Alert.AlertType.INFORMATION);
    }
    
    @FXML
    public void handleEkstre() {
        Cari selectedCari = tblCariler.getSelectionModel().getSelectedItem();
        if (selectedCari == null) {
            showAlert("Uyarı", "Lütfen bir cari seçiniz", Alert.AlertType.WARNING);
            return;
        }
        showAlert("Bilgi", "Ekstre formu açılacak: " + selectedCari.getUnvan(), Alert.AlertType.INFORMATION);
    }
    
    @FXML
    public void handleSil() {
        Cari selectedCari = tblCariler.getSelectionModel().getSelectedItem();
        if (selectedCari == null) {
            showAlert("Uyarı", "Lütfen bir cari seçiniz", Alert.AlertType.WARNING);
            return;
        }
        
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Onay");
        confirmation.setHeaderText("Silme İşlemi");
        confirmation.setContentText(selectedCari.getUnvan() + " silinecek. Emin misiniz?");
        
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                cariService.sil(selectedCari.getCariId());
                loadCariler();
                showAlert("Başarılı", "Cari silindi", Alert.AlertType.INFORMATION);
            }
        });
    }
    
    @FXML
    public void handleDuzenle() {
        Cari selectedCari = tblCariler.getSelectionModel().getSelectedItem();
        if (selectedCari == null) {
            showAlert("Uyarı", "Lütfen bir cari seçiniz", Alert.AlertType.WARNING);
            return;
        }
        showAlert("Bilgi", "Düzenleme formu açılacak: " + selectedCari.getUnvan(), Alert.AlertType.INFORMATION);
    }
    
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
