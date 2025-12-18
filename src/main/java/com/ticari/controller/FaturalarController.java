package com.ticari.controller;

import com.ticari.entity.Fatura;
import com.ticari.service.FaturaService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

@Controller
@RequiredArgsConstructor
public class FaturalarController implements Initializable {

    private final FaturaService faturaService;

    @FXML
    private TableView<Fatura> tblFaturalar;
    
    @FXML
    private TableColumn<Fatura, LocalDateTime> colTarih;
    
    @FXML
    private TableColumn<Fatura, String> colFaturaNo;
    
    @FXML
    private TableColumn<Fatura, String> colCari;
    
    @FXML
    private TableColumn<Fatura, String> colTur;
    
    @FXML
    private TableColumn<Fatura, Double> colGenelToplam;
    
    @FXML
    private TableColumn<Fatura, String> colParaBirimi;
    
    @FXML
    private TextField txtArama;
    
    @FXML
    private DatePicker dpTarihFiltre;
    
    @FXML
    private Button btnYeniFatura;
    
    @FXML
    private Button btnYazdir;
    
    @FXML
    private Button btnDetay;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTableColumns();
        loadFaturalar();
        setupSearchListener();
    }

    private void setupTableColumns() {
        colTarih.setCellValueFactory(new PropertyValueFactory<>("faturaTarihi"));
        colFaturaNo.setCellValueFactory(new PropertyValueFactory<>("faturaNo"));
        colTur.setCellValueFactory(new PropertyValueFactory<>("tur"));
        colGenelToplam.setCellValueFactory(new PropertyValueFactory<>("genelToplam"));
        colParaBirimi.setCellValueFactory(new PropertyValueFactory<>("paraBirimi"));
        
        // Custom cell factory for Cari display
        colCari.setCellValueFactory(cellData -> {
            if (cellData.getValue().getCari() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                    cellData.getValue().getCari().getUnvan()
                );
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });
        
        // Format date column
        colTarih.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toLocalDate().toString());
                }
            }
        });
        
        // Format currency column
        colGenelToplam.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f", item));
                }
            }
        });
    }

    private void loadFaturalar() {
        var faturalar = faturaService.findAll();
        tblFaturalar.setItems(FXCollections.observableArrayList(faturalar));
    }

    private void setupSearchListener() {
        if (txtArama != null) {
            txtArama.textProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue == null || newValue.isEmpty()) {
                    loadFaturalar();
                } else {
                    var filtered = faturaService.findAll().stream()
                        .filter(f -> f.getFaturaNo().toLowerCase().contains(newValue.toLowerCase()) ||
                                   (f.getCari() != null && f.getCari().getUnvan().toLowerCase().contains(newValue.toLowerCase())))
                        .toList();
                    tblFaturalar.setItems(FXCollections.observableArrayList(filtered));
                }
            });
        }
    }

    @FXML
    private void handleYeniFatura() {
        showAlert("Bilgi", "Yeni Fatura ekranı açılacak", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleYazdir() {
        Fatura selected = tblFaturalar.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Uyarı", "Lütfen yazdırmak için bir fatura seçin", Alert.AlertType.WARNING);
            return;
        }
        showAlert("Bilgi", "PDF yazdırma özelliği eklenecek", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleDetay() {
        Fatura selected = tblFaturalar.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Uyarı", "Lütfen detay görmek için bir fatura seçin", Alert.AlertType.WARNING);
            return;
        }
        showAlert("Bilgi", "Fatura ID: " + selected.getFaturaId() + "\nFatura No: " + selected.getFaturaNo(), 
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
