package com.ticari.controller;

import com.ticari.entity.KasaBanka;
import com.ticari.service.KasaBankaService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;

import java.net.URL;
import java.util.ResourceBundle;

@Controller
@RequiredArgsConstructor
public class KasaController implements Initializable {

    private final KasaBankaService kasaBankaService;

    @FXML private TableView<KasaBanka> tblKasa;
    @FXML private TableColumn<KasaBanka, Integer> colHesapId;
    @FXML private TableColumn<KasaBanka, String> colHesapAdi;
    @FXML private TableColumn<KasaBanka, String> colTur;
    @FXML private TableColumn<KasaBanka, Double> colBakiye;
    @FXML private TableColumn<KasaBanka, String> colParaBirimi;
    
    @FXML private TextField txtArama;
    @FXML private Button btnYeniHesap;
    @FXML private Button btnTransfer;
    @FXML private Button btnHareketler;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTableColumns();
        loadKasaBanka();
        setupSearchListener();
    }

    private void setupTableColumns() {
        colHesapId.setCellValueFactory(new PropertyValueFactory<>("hesapId"));
        colHesapAdi.setCellValueFactory(new PropertyValueFactory<>("hesapAdi"));
        colTur.setCellValueFactory(new PropertyValueFactory<>("tur"));
        colBakiye.setCellValueFactory(new PropertyValueFactory<>("bakiye"));
        
        // Para birimi için sabit TRY göster (şimdilik)
        colParaBirimi.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty("TRY"));
        
        // Format balance column with currency
        colBakiye.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("%.2f ₺", item));
                    // Color code based on balance
                    if (item < 0) {
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    } else if (item > 10000) {
                        setStyle("-fx-text-fill: #2FA84F; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #2d3447;");
                    }
                }
            }
        });
        
        // Format Tur column
        colTur.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item) {
                        case "KASA":
                            setStyle("-fx-background-color: #e8f5e9; -fx-text-fill: #2e7d32;");
                            break;
                        case "BANKA":
                            setStyle("-fx-background-color: #e3f2fd; -fx-text-fill: #1565c0;");
                            break;
                        case "POS":
                            setStyle("-fx-background-color: #fff3e0; -fx-text-fill: #ef6c00;");
                            break;
                    }
                }
            }
        });
    }

    private void loadKasaBanka() {
        var hesaplar = kasaBankaService.findAll();
        tblKasa.setItems(FXCollections.observableArrayList(hesaplar));
    }

    private void setupSearchListener() {
        if (txtArama != null) {
            txtArama.textProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue == null || newValue.isEmpty()) {
                    loadKasaBanka();
                } else {
                    var filtered = kasaBankaService.findAll().stream()
                        .filter(k -> k.getHesapAdi().toLowerCase().contains(newValue.toLowerCase()))
                        .toList();
                    tblKasa.setItems(FXCollections.observableArrayList(filtered));
                }
            });
        }
    }

    @FXML
    private void handleYeniHesap() {
        showAlert("Bilgi", "Yeni hesap tanımlama formu açılacak", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleTransfer() {
        KasaBanka selected = tblKasa.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Uyarı", "Lütfen transfer için bir hesap seçin", Alert.AlertType.WARNING);
            return;
        }
        showAlert("Bilgi", "Transfer ekranı açılacak", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleHareketler() {
        KasaBanka selected = tblKasa.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Uyarı", "Lütfen hareketlerini görmek için bir hesap seçin", Alert.AlertType.WARNING);
            return;
        }
        
        showAlert("Bilgi", 
                 "Hesap: " + selected.getHesapAdi() + "\n" +
                 "Bakiye: " + String.format("%.2f ₺", selected.getBakiye().doubleValue()) + "\n\n" +
                 "Hareketler listesi gösterilecek", 
                 Alert.AlertType.INFORMATION);
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
