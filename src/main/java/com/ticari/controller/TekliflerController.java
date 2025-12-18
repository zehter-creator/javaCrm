package com.ticari.controller;

import com.ticari.entity.Teklif;
import com.ticari.enums.TeklifDurumu;
import com.ticari.service.TeklifService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

@Controller
@RequiredArgsConstructor
public class TekliflerController implements Initializable {

    private final TeklifService teklifService;

    @FXML private TableView<Teklif> tblTeklifler;
    @FXML private TableColumn<Teklif, Integer> colTeklifNo;
    @FXML private TableColumn<Teklif, String> colCari;
    @FXML private TableColumn<Teklif, LocalDateTime> colTarih;
    @FXML private TableColumn<Teklif, LocalDate> colGecerlilik;
    @FXML private TableColumn<Teklif, Double> colToplam;
    @FXML private TableColumn<Teklif, String> colDurum;
    
    @FXML private TextField txtArama;
    @FXML private DatePicker dpTarihFiltre;
    @FXML private Button btnYeniTeklif;
    @FXML private Button btnPdf;
    @FXML private Button btnDuzenle;
    @FXML private Button btnSipariseDonustur;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTableColumns();
        loadTeklifler();
        setupSearchListener();
    }

    private void setupTableColumns() {
        colTeklifNo.setCellValueFactory(new PropertyValueFactory<>("teklifId"));
        colTarih.setCellValueFactory(new PropertyValueFactory<>("teklifTarihi"));
        colGecerlilik.setCellValueFactory(new PropertyValueFactory<>("gecerlilikTarihi"));
        colToplam.setCellValueFactory(new PropertyValueFactory<>("toplamTutar"));
        colDurum.setCellValueFactory(new PropertyValueFactory<>("durum"));
        
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
        
        // Format status column with color
        colDurum.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.equals(TeklifDurumu.ONAYLANDI.name())) {
                        setStyle("-fx-text-fill: #2FA84F; -fx-font-weight: bold;");
                    } else if (item.equals(TeklifDurumu.REDDEDILDI.name())) {
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                    }
                }
            }
        });
        
        // Format currency column
        colToplam.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f ₺", item));
                }
            }
        });
    }

    private void loadTeklifler() {
        var teklifler = teklifService.findAll();
        tblTeklifler.setItems(FXCollections.observableArrayList(teklifler));
    }

    private void setupSearchListener() {
        if (txtArama != null) {
            txtArama.textProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue == null || newValue.isEmpty()) {
                    loadTeklifler();
                } else {
                    var filtered = teklifService.findAll().stream()
                        .filter(t -> String.valueOf(t.getTeklifId()).contains(newValue) ||
                                   (t.getCari() != null && t.getCari().getUnvan().toLowerCase().contains(newValue.toLowerCase())))
                        .toList();
                    tblTeklifler.setItems(FXCollections.observableArrayList(filtered));
                }
            });
        }
    }

    @FXML
    private void handleYeniTeklif() {
        showAlert("Bilgi", "Yeni Teklif oluşturma ekranı açılacak", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handlePdf() {
        Teklif selected = tblTeklifler.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Uyarı", "Lütfen PDF için bir teklif seçin", Alert.AlertType.WARNING);
            return;
        }
        showAlert("Bilgi", "PDF oluşturma özelliği eklenecek", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleDuzenle() {
        Teklif selected = tblTeklifler.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Uyarı", "Lütfen düzenlemek için bir teklif seçin", Alert.AlertType.WARNING);
            return;
        }
        showAlert("Bilgi", "Teklif düzenleme ekranı açılacak", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleSipariseDonustur() {
        Teklif selected = tblTeklifler.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Uyarı", "Lütfen siparişe dönüştürmek için bir teklif seçin", Alert.AlertType.WARNING);
            return;
        }
        
        if (selected.getDurum() != TeklifDurumu.ONAYLANDI) {
            showAlert("Uyarı", "Sadece onaylanmış teklifler siparişe dönüştürülebilir", Alert.AlertType.WARNING);
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Onay");
        confirm.setHeaderText("Siparişe Dönüştür");
        confirm.setContentText("Bu teklifi siparişe dönüştürmek istediğinizden emin misiniz?");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // TODO: Implement order creation logic
                showAlert("Başarılı", "Teklif siparişe dönüştürüldü", Alert.AlertType.INFORMATION);
                loadTeklifler();
            }
        });
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
