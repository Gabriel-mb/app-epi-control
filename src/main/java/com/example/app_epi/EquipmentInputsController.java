package com.example.app_epi;

import dao.BorrowedDAO;
import dao.ConnectionDAO;
import dao.EquipmentsDAO;
import dao.HistoryDAO;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXDatePicker;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;

import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.util.converter.LocalDateStringConverter;
import models.Borrowed;
import models.Equipment;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import static java.lang.Integer.parseInt;

public class EquipmentInputsController {
    ObservableList<Borrowed> borrowingsList = FXCollections.observableArrayList();
    private Stage stage;
    private Scene scene;
    private Parent root;
    @FXML
    private Label nameLabel;
    @FXML
    private Label idLabel;
    @FXML
    private MFXDatePicker date;
    @FXML
    private AnchorPane anchorPane;
    @FXML
    private TextField equipmentIdInput;
    @FXML
    private ComboBox<String> equipmentName;
    @FXML
    private TableView<Borrowed> table;
    @FXML
    private TableColumn<Borrowed, String> nameColumn;
    @FXML
    private TableColumn<Borrowed, Integer> idColumn;
    @FXML
    private TableColumn<Borrowed, java.util.Date> dateColumn;
    @FXML
    private TableColumn<Borrowed, String> supplierColumn;
    @FXML
    private MFXButton minimizeButton;
    private Double x;
    private Double y;
    private Boolean confirmation = false;
    private String equipName;
    private String supplierName;


    public void onSearchButtonClick() throws SQLException, IOException {
        if (Objects.equals(equipmentIdInput.getText(), "")) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText("Ocorreu um erro");
            alert.setContentText("Por favor insira uma matrícula adequada!");
            alert.showAndWait();
            return;
        }
        Connection connection = new ConnectionDAO().connect();
        EquipmentsDAO equipmentsDAO = new EquipmentsDAO(connection);
        List<Equipment> equipmentList = equipmentsDAO.readId(parseInt(equipmentIdInput.getText()));
        if (equipmentList.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText("Ocorreu um erro");
            alert.setContentText("Nenhum equipamento encontrado!");
            alert.showAndWait();
            return;
        } else {
            equipmentName.getItems().clear();
            for (Equipment i : equipmentList) {
                equipmentName.getItems().addAll(i.getSupplierName() + " - " + i.getName());
            }
        }
        equipmentName.getSelectionModel().selectFirst();
    }

    public void onIncludeButtonClick() throws SQLException, IOException {
        Connection connection = new ConnectionDAO().connect();
        BorrowedDAO borrowedDAO = new BorrowedDAO(connection);
        splitSelection();
        for (Borrowed borrowed : table.getItems()) {
            Integer id = idColumn.getCellData(borrowed);
            String supplier = supplierColumn.getCellData(borrowed);
            if (parseInt(equipmentIdInput.getText()) == id && Objects.equals(supplierName, supplier)) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erro");
                alert.setHeaderText("Ocorreu um erro");
                alert.setContentText("Ferramenta já cadastrada!");
                alert.showAndWait();

                equipmentName.getItems().clear();
                equipmentIdInput.setText("");
                date.setValue(null);

                return;
            }
        }
        if (equipmentName.getValue() == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText("Ocorreu um erro");
            alert.setContentText("Por favor insira uma matrícula válida");
            alert.showAndWait();
        } else if (date.getValue() == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText("Ocorreu um erro");
            alert.setContentText("Por favor insira uma data válida");
            alert.showAndWait();
        } else if (borrowedDAO.searchBorrowed(parseInt(equipmentIdInput.getText()), borrowedDAO.getSupplierId(supplierName))) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText("Ferramenta já Alocada");
            alert.setContentText("Por favor selecione outra ferramenta!");
            alert.showAndWait();
        } else {
            borrowingsList.add(new Borrowed(equipName, parseInt(equipmentIdInput.getText()), Date.valueOf(date.getValue()), supplierName, borrowedDAO.getSupplierId(supplierName)));
            borrowedDAO.create(new Borrowed(parseInt(idLabel.getText()), parseInt(equipmentIdInput.getText()), Date.valueOf(date.getValue()), borrowedDAO.getSupplierId(supplierName)));

            table.setItems(borrowingsList);

            equipmentName.getItems().clear();
            equipmentIdInput.setText("");
            date.setValue(null);
        }
    }

    private void splitSelection() {
        String selection = equipmentName.getValue();
        String[] sections = selection.split(" - ");

        supplierName = sections[0];
        equipName = sections[1];
    }

    public void removeData(Integer idEquip, String supplierName) throws SQLException, IOException {
        Connection connection = new ConnectionDAO().connect();
        BorrowedDAO borrowedDAO = new BorrowedDAO(connection);
        HistoryDAO historyDAO = new HistoryDAO(connection);
        if (borrowedDAO.readId(idEquip) != null) borrowedDAO.delete(idEquip, historyDAO.getSupplierId(supplierName));
    }

    public void setEmployee(String id, String name) {
        idLabel.setText(id);
        nameLabel.setText(name);
    }

    public void setTable(ObservableList<Borrowed> list, Boolean confirm) {
        nameColumn.setCellValueFactory(new PropertyValueFactory<Borrowed, String>("equipmentName"));
        idColumn.setCellValueFactory(new PropertyValueFactory<Borrowed, Integer>("idEquipment"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<Borrowed, java.util.Date>("date"));
        supplierColumn.setCellValueFactory(new PropertyValueFactory<>("supplierName"));

        table.setItems(list);
        confirmation = confirm;

        borrowingsList.addAll(list);
    }

    @FXML
    private void initialize() {
        // percorre todos os nós da cena e define o foco como não transversável para os TextFields
        for (Node node : anchorPane.getChildrenUnmodifiable()) {
            if (node instanceof TextField) {
                node.setFocusTraversable(false);
            }
        }
        //formata a data do datepicker
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        Supplier<StringConverter<LocalDate>> converterSupplier = () -> new LocalDateStringConverter(dateFormatter, null);
        date.setConverterSupplier(converterSupplier);
        minimizeButton.setOnAction(e ->
                ( (Stage) ( (Button) e.getSource() ).getScene().getWindow() ).setIconified(true)
        );
    }

    public void anchorPane_dragged(MouseEvent event) {
        Stage stage = (Stage) anchorPane.getScene().getWindow();
        stage.setY(event.getScreenY() - y);
        stage.setX(event.getScreenX() - x);

    }

    public void anchorPane_pressed(MouseEvent event) {
        x = event.getSceneX();
        y = event.getSceneY();
    }

    public void onCloseButtonClick() {
        System.exit(0);
    }

    public void onMenuButtonClick(MouseEvent event) throws IOException {
        root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("search-view.fxml")));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        scene.setFill(Color.TRANSPARENT);
        stage.show();
    }

    public void onDevolutionClick(ActionEvent event) throws IOException {
        //remover o dado do borrowed e gerar dado no histórico
        FXMLLoader loader = new FXMLLoader(getClass().getResource("devolution-view.fxml"));
        Parent root = loader.load();
        DevolutionController devolutionController = loader.getController();

        SelectionModel<Borrowed> selectionModel = table.getSelectionModel();
        Borrowed itemSelected = selectionModel.getSelectedItem();

        if (itemSelected == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText("Ocorreu um erro");
            alert.setContentText("Por favor selecione um item que quer devolver.");
            alert.showAndWait();
            return;
        }
        devolutionController.setData(nameLabel.getText(), idLabel.getText(), String.valueOf(itemSelected.getIdEquipment()), itemSelected.getEquipmentName(), String.valueOf(itemSelected.getDate()), itemSelected.getSupplierName());

        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        scene.setFill(Color.TRANSPARENT);
        stage.show();
    }

    public void onBackButtonClick(MouseEvent event) throws IOException, SQLException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("patCard-view.fxml"));
        Parent root = loader.load();

        CardController cardController = loader.getController();
        cardController.setTableEmployee(idLabel.getText());

        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        scene.setFill(Color.TRANSPARENT);
        stage.show();
    }
}