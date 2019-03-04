package sample;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;

import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Optional;

import static java.sql.DriverManager.getConnection;

public class Controller {

    private static Connection connection = null;

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
    void initialize(){

        if (dbСonnection()) {
            status.setText("Connection successful");
            status.setTextFill(Color.web("#00FF00"));
            loadData();
        } else {
            status.setText("Connection failed");
            status.setTextFill(Color.web("#ff0000"));
        }

        btnAdd.setOnAction(event -> {
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
                loadData();
            }
        });

        table1.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Film>() {
            @Override
            public void changed(ObservableValue<? extends Film> observable, Film oldValue, Film newValue) {
                if(table1.getSelectionModel().getSelectedItem() != null) {
                    btnDelete.setDisable(false);
                    btnEdit.setDisable(false);
//                    btnUpdate.setDisable(false);
                    Film film = table1.getSelectionModel().getSelectedItem();

                    btnDelete.setOnAction(event -> {
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle("Delete confirmation");
                        alert.setHeaderText("You sure you want to delete this record?\n This action cannot be undone.");
                        alert.setContentText(null);
                        Optional<ButtonType> ConfResult = alert.showAndWait();
                        if (ConfResult.get() == ButtonType.OK) {
                            if (deleteData(film.getID())) {
                                status.setText("Record successfully deleted");
                                status.setTextFill(Color.web("#00FF00"));
                                loadData();
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
                            loadData();
                        } else {
                            status.setText("Error while updating record");
                            status.setTextFill(Color.web("#ff0000"));
                        }
                    });
                } else {
                    btnEdit.setDisable(true);
                    btnDelete.setDisable(true);
//                    btnUpdate.setDisable(true);
                }
            }
        });

        table1.setRowFactory(tableView -> {
            TableRow<Film> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Film rowData = row.getItem();
                    System.out.println("Double click on: " + rowData.getID());
                    editRecord(rowData);
                    btnUpdate.setDisable(false);
                    btnUpdate.setOnAction(event1 -> {
                        updateRecord(rowData.getID());
                        loadData();
                        btnUpdate.setDisable(true);
                    });
                }
            });
            return row;
        });
    }

    private void editRecord(Film film) {
        try {
            PreparedStatement statement = connection.prepareStatement("select filmlibrary.id, filmlibrary.title, directors.directorName, filmlibrary.year, filmlibrary.rating" +
                    " from filmlibrary inner join directors on filmlibrary.iddirector = directors.iddirectors where id=?;");
            statement.setInt(1, film.getID());
            ResultSet resultSet = statement.executeQuery();
            DateFormat dateFormat = new SimpleDateFormat("yyyy");
            while (resultSet.next()) {
                addTitle.setText(resultSet.getString(2));
                addDirector.setText(resultSet.getString(3));
                addYear.setText(dateFormat.format(resultSet.getDate(4)));
                addRating.setValue(resultSet.getByte(5));
            }
            loadData();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean dbСonnection() {
        boolean result = false;
        try {
            String user = "root";
            String pass = "root";
            String url = "jdbc:mysql://localhost:3306/test_db?serverTimezone=UTC&useSSL=false";
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = getConnection(url, user, pass);
            System.out.println("Connection ID: " + connection.toString());
            result = true;
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return result;
    }

    static void disconnect() {
        if (connection != null) {
            try {
                System.out.println("Disconnected ID: " + connection.toString());
                connection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private Integer tryParse(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private boolean updateRecord(int IDnumber) {
        boolean result = false;
        try {
            PreparedStatement statement1 = connection.prepareStatement("INSERT INTO directors (directorName)\n" +
                    "SELECT * FROM (SELECT ?) AS tmp\n" +
                    "WHERE NOT EXISTS (\n" +
                    "    SELECT directorName FROM directors WHERE directorName = ?\n" +
                    ") LIMIT 1;");
            statement1.setString(1, addDirector.getText().trim());
            statement1.setString(2, addDirector.getText().trim());
            statement1.executeUpdate();
            statement1.clearParameters();
            statement1 = connection.prepareStatement("select iddirectors from directors where directorName = ?;");
            statement1.setString(1, addDirector.getText().trim());
            ResultSet resultSet = statement1.executeQuery();
            statement1.clearParameters();
            statement1 = connection.prepareStatement("update filmlibrary set title=?, iddirector=?, year=?, rating=? where id=?;");
            statement1.setString(1, addTitle.getText().trim());
            while (resultSet.next()) {
                statement1.setInt(2, resultSet.getInt(1));
            }
            statement1.setString(3, addYear.getText().trim());
            statement1.setString(4, Double.toString(addRating.getValue()));
            statement1.setInt(5, IDnumber);
            statement1.executeUpdate();
            System.out.println("Update :" + statement1.toString());
            statement1.close();
            result = true;
            addTitle.setText(null);
            addDirector.setText(null);
            addYear.setText(null);
            addRating.setValue(0);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    private boolean deleteData(Integer deleteNumber) {
        boolean result = false;
        try {
            PreparedStatement statement = connection.prepareStatement("delete from filmlibrary where id=?;");
            statement.setInt(1, deleteNumber);
            statement.executeUpdate();
            statement.close();
            result = true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }


    private boolean addData() {
        boolean result = false;
        try {
            PreparedStatement statement1 = connection.prepareStatement("INSERT INTO directors (directorName)\n" +
                    "SELECT * FROM (SELECT ?) AS tmp\n" +
                    "WHERE NOT EXISTS (\n" +
                    "    SELECT directorName FROM directors WHERE directorName = ?\n" +
                    ") LIMIT 1;");
            statement1.setString(1, addDirector.getText().trim());
            statement1.setString(2, addDirector.getText().trim());
            statement1.executeUpdate();
            statement1.clearParameters();
            statement1 = connection.prepareStatement("select iddirectors from directors where directorName = ?;");
            statement1.setString(1, addDirector.getText().trim());
            ResultSet resultSet = statement1.executeQuery();
            statement1.clearParameters();
            statement1 = connection.prepareStatement("insert into filmlibrary (title, iddirector, year, rating) values (?, ?, ?, ?);");
            statement1.setString(1, addTitle.getText().trim());
            while (resultSet.next()) {
                statement1.setInt(2, resultSet.getInt(1));
            }
            statement1.setString(3, addYear.getText().trim());
            statement1.setString(4, Double.toString(addRating.getValue()));
            statement1.executeUpdate();
            System.out.println("Parametrized query :" + statement1.toString());
            statement1.close();
            result = true;
            addTitle.setText(null);
            addDirector.setText(null);
            addYear.setText(null);
            addRating.setValue(0);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    private boolean loadData () {
        boolean result = false;
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet;
            resultSet = statement.executeQuery("select filmlibrary.id, filmlibrary.title, directors.directorName, filmlibrary.year, filmlibrary.rating" +
                    " from filmlibrary inner join directors on filmlibrary.iddirector = directors.iddirectors;");
            System.out.println("executing query to DB: " + resultSet.toString());
            int count = 1;
            data = FXCollections.observableArrayList();
            idNumber.setCellValueFactory(new PropertyValueFactory<>("indexNumber"));
            title.setCellValueFactory(new PropertyValueFactory<>("title"));
            director.setCellValueFactory(new PropertyValueFactory<>("director"));
            year.setCellValueFactory(new PropertyValueFactory<>("year"));
            rating.setCellValueFactory(new PropertyValueFactory<>("rating"));
            while (resultSet.next()) {
                System.out.println(count + "\t" + resultSet.getInt(1) + "\t" + resultSet.getString(2) + "\t"
                        + resultSet.getString(3) + "\t" + resultSet.getDate(4) + "\t" + resultSet.getByte(5));
                data.add(new Film(resultSet.getInt(1), count, resultSet.getString(2), resultSet.getString(3),
                        resultSet.getDate(4), resultSet.getByte(5)));
                count++;
            }
            table1.setItems(data);
            resultSet.close();
            statement.close();
            result = true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }
}