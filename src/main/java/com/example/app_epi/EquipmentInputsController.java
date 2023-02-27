package com.example.app_epi;

import dao.BorrowedDAO;
import dao.ConnectionDAO;
import dao.EquipmentsDAO;
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
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import models.Borrowed;
import models.Equipment;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.Objects;

import static java.lang.Integer.parseInt;

public class EquipmentInputsController {
    private Stage stage;
    private Scene scene;
    private Parent root;
    @FXML
    private Label nameLabel;
    @FXML
    private Label idLabel;
    @FXML
    private DatePicker date;
    @FXML
    private AnchorPane anchorPane;
    @FXML
    private TextField equipmentIdInput;
    @FXML
    private Label equipmentName;
    ObservableList<Borrowed> borrowingsList = FXCollections.observableArrayList();
    @FXML
    private TableView<Borrowed> table;
    @FXML
    private TableColumn<Borrowed, String> nameColumn;
    @FXML
    private TableColumn<Borrowed, Integer> idColumn;
    @FXML
    private TableColumn<Borrowed, java.util.Date> dateColumn;


    public void onSaveButtonClick(ActionEvent event) throws IOException {
        try {
            Connection connection = new ConnectionDAO().connect();
            BorrowedDAO borrowedDAO = new BorrowedDAO(connection);
            //em um while(list.hasnext) usa borrowedDAO.create(item)
            for (Borrowed item : borrowingsList) {
                borrowedDAO.create(new Borrowed(parseInt(idLabel.getText()), item.getIdEquipment(), item.getDate()));
            }

            root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("search-view.fxml")));
            stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText("Ocorreu um erro");
            alert.setContentText(String.valueOf(e));
            alert.showAndWait();
        }
    }

    public void onSearchButtonClick(ActionEvent event) throws IOException, SQLException {
        Connection connection = new ConnectionDAO().connect();
        EquipmentsDAO equipmentsDAO = new EquipmentsDAO(connection);
        Equipment equipment = equipmentsDAO.readId(parseInt(equipmentIdInput.getText()));

        if (equipment == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText("Ocorreu um erro");
            alert.setContentText("Nenhum equipamento encontrado!");
            alert.showAndWait();
            return;
        } else equipmentName.setText(equipment.getName());
    }

    public void onIncludeButtonClick(ActionEvent event) throws IOException{
        if(equipmentName.getText() == null){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText("Ocorreu um erro");
            alert.setContentText("Por favor insira uma matrícula válida");
            alert.showAndWait();
            return;
        } else if (date.getValue() == null){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText("Ocorreu um erro");
            alert.setContentText("Por favor insira uma data válida");
            alert.showAndWait();
            return;
        } else {
            borrowingsList.add(new Borrowed(equipmentName.getText(), parseInt(equipmentIdInput.getText()), Date.valueOf(date.getValue())));
            nameColumn.setCellValueFactory(new PropertyValueFactory<Borrowed, String>("equipmentName"));
            idColumn.setCellValueFactory(new PropertyValueFactory<Borrowed, Integer>("idEquipment"));
            dateColumn.setCellValueFactory(new PropertyValueFactory<Borrowed, java.util.Date>("date"));
            table.setItems(borrowingsList);

            equipmentName.setText("Nome");
            equipmentIdInput.setText("");
            date.setValue(null);
        }
    }

    public void onRemoveButtonClick(ActionEvent event) throws IOException {
        SelectionModel<Borrowed> selectionModel = table.getSelectionModel();
        int selectedIndex = selectionModel.getSelectedIndex();
        ObservableList<Borrowed> data = table.getItems();
        data.remove(selectedIndex);
        table.refresh();
    }

    public void setEmployee(String id, String name) {
        idLabel.setText(id);
        nameLabel.setText(name);
    }
    @FXML
    private void initialize() {
        // percorre todos os nós da cena e define o foco como não transversável para os TextFields
        for (Node node : anchorPane.getChildrenUnmodifiable()) {
            if (node instanceof TextField) {
                ((TextField) node).setFocusTraversable(false);
            }
        }
    }
}