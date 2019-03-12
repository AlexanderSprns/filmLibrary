package sample;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Optional;

public class Controller {

    private DBControl dbControl = new DBControl();

    @FXML
    public TableView<Film> table1;
    private ObservableList<Film> data;

    @FXML
    private TableColumn<Film, Integer> idNumber;

    @FXML
    private TableColumn<Film, String> title;

    @FXML
    private TableColumn<Film, String> director;

    @FXML
    private TableColumn<Film, Date> year;

    @FXML
    private TableColumn<Film, Byte> rating;

    @FXML
    private TextField addTitle;

    @FXML
    private TextField addDirector;

    @FXML
    private TextField addYear;

    @FXML
    private Slider addRating;

    @FXML
    private Button btnAdd;

    @FXML
    private Label status;

    @FXML
    private Button btnDelete;

    @FXML
    private Button btnEdit;

    @FXML
    private Button btnUpdate;

    @FXML
    private Button btnRestoreDB;

    @FXML
    void initialize(){

        if (dbControl.dbConnection()) {
            status.setText("Connection successful");
            status.setTextFill(Color.web("#00FF00"));
            showData();
        } else {
            status.setText("Connection failed");
            status.setTextFill(Color.web("#ff0000"));
        }

        btnRestoreDB.setOnAction(event -> {
            dbControl.dbRestoring();
            showData();
        });

        btnAdd.setOnAction(event -> addRecordBtn());

        table1.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Film>() {
            @Override
            public void changed(ObservableValue<? extends Film> observable, Film oldValue, Film newValue) {
                if(table1.getSelectionModel().getSelectedItem() != null) {
                    btnDelete.setDisable(false);
                    btnEdit.setDisable(false);
                    Film film = table1.getSelectionModel().getSelectedItem();

                    btnDelete.setOnAction(event -> {
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle("Delete confirmation");
                        alert.setHeaderText("You sure you want to delete this record?\nThis action cannot be undone.");
                        alert.setContentText(null);
                        Optional<ButtonType> ConfResult = alert.showAndWait();
                        if (ConfResult.get() == ButtonType.OK) {
                            if (dbControl.deleteData(film.getID())) {
                                status.setText("Record successfully deleted");
                                status.setTextFill(Color.web("#00FF00"));
                                showData();
                            } else {
                                status.setText("Error while deleting record");
                                status.setTextFill(Color.web("#ff0000"));
                            }
                        }
                    });

                    btnEdit.setOnAction(event -> {
                        btnEdit.setDisable(true);
                        btnUpdate.setDisable(false);
                        editRecord(film);
                    });

                    btnUpdate.setOnAction(event -> {
                        if (updateRecord(film.getID())) {
                            status.setText("Record successfully updated");
                            status.setTextFill(Color.web("#00FF00"));
                            btnUpdate.setDisable(true);
                            showData();
                        } else {
                            status.setText("Error while updating record");
                            status.setTextFill(Color.web("#ff0000"));
                        }
                    });

                    btnAdd.setOnAction(event -> addRecordBtn());

                    btnRestoreDB.setOnAction(event -> {
                        dbControl.dbRestoring();
                        showData();
                    });
                } else {
                    btnEdit.setDisable(true);
                    btnDelete.setDisable(true);
                }
            }
        });

        table1.setRowFactory(tableView -> {
            TableRow<Film> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Film rowData = row.getItem();
                    editRecord(rowData);
                    btnUpdate.setDisable(false);
                    btnUpdate.setOnAction(event1 -> {
                        updateRecord(rowData.getID());
                        showData();
                        btnUpdate.setDisable(true);
                    });
                }
            });
            return row;
        });
    }

    private void addRecordBtn() {
        addTitle.setStyle("-fx-text-box-border: gray");
        addDirector.setStyle("-fx-text-box-border: gray");
        addYear.setStyle("-fx-text-box-border: gray");
        if (addTitle.getText().trim().isEmpty() || addTitle.getText() == null) {
            addTitle.requestFocus();
            addTitle.setStyle("-fx-text-box-border: red; -fx-focus-color: red;");
            status.setText("Error while adding record");
            status.setTextFill(Color.web("#ff0000"));
        } else if (addDirector.getText().trim().isEmpty() || addDirector.getText() == null) {
            addDirector.requestFocus();
            addDirector.setStyle("-fx-text-box-border: red; -fx-focus-color: red;");
            status.setText("Error while adding record");
            status.setTextFill(Color.web("#ff0000"));
        } else if (addYear.getText().trim().isEmpty() || addYear.getText() == null ||
                tryParse(addYear.getText().trim()) == null || addYear.getText().trim().length() != 4) {
            addYear.requestFocus();
            addYear.setStyle("-fx-text-box-border: red; -fx-focus-color: red;");
            status.setText("Error while adding record");
            status.setTextFill(Color.web("#ff0000"));
        } else if (addData()) {
            status.setText("Record successfully added");
            status.setTextFill(Color.web("#00FF00"));
            showData();
        }
    }

    private void showData() {
        try {
            ResultSet resultSet = (ResultSet) dbControl.loadData();
            int count = 1;
            data = FXCollections.observableArrayList();
            idNumber.setCellValueFactory(new PropertyValueFactory<>("indexNumber"));
            title.setCellValueFactory(new PropertyValueFactory<>("title"));
            director.setCellValueFactory(new PropertyValueFactory<>("director"));
            year.setCellValueFactory(new PropertyValueFactory<>("year"));
            rating.setCellValueFactory(new PropertyValueFactory<>("rating"));
            while (resultSet.next()) {
                data.add(new Film(resultSet.getInt(1), count, resultSet.getString(2), resultSet.getString(3),
                        resultSet.getDate(4), resultSet.getByte(5)));
                count++;
            }
            table1.setItems(data);
            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Integer tryParse(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void editRecord(Film film) {
        try {
            ResultSet resultSet = (ResultSet) dbControl.edit(film);
            DateFormat dateFormat = new SimpleDateFormat("yyyy");
            while (resultSet.next()) {
                addTitle.setText(resultSet.getString(2));
                addDirector.setText(resultSet.getString(3));
                addYear.setText(dateFormat.format(resultSet.getDate(4)));
                addRating.setValue(resultSet.getByte(5));
            }
            showData();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean updateRecord(int IDnumber) {
        boolean result = false;
        if (dbControl.update(IDnumber, addDirector, addTitle, addYear, addRating)) {
            addTitle.setText(null);
            addDirector.setText(null);
            addYear.setText(null);
            addRating.setValue(0);
            result = true;
        }
        return result;
    }

    private boolean addData() {
        boolean result = false;
        if (dbControl.addNewData(addDirector, addTitle, addYear, addRating)) {
            addTitle.setText(null);
            addDirector.setText(null);
            addYear.setText(null);
            addRating.setValue(0);
            result = true;
        }
        return result;
    }
}