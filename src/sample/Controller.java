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

import static java.sql.DriverManager.getConnection;

public class Controller {

    private Connection connection = null;

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
    private Button btnConnect;

    @FXML
    private Label status;

    @FXML
    private Button btnRefresh;

    @FXML
    private Button btnDelete;

    @FXML
    private Button btnDisconnect;

    @FXML
    private Button btnEdit;

    @FXML
    private Button btnUpdate;

    @FXML
    void initialize(){

        btnConnect.setOnAction(event->{
            if (DBConnection()) {
                status.setText("Connection successful");
                status.setTextFill(Color.web("#00FF00"));
                btnAdd.setDisable(false);
                btnDisconnect.setDisable(false);
                btnRefresh.setDisable(false);
                btnConnect.setDisable(true);
            } else {
                status.setText("Connection failed");
                status.setTextFill(Color.web("#ff0000"));
            }
        });

        btnRefresh.setOnAction(event -> {
            if (loadData()) {
                status.setText("Data updated");
                status.setTextFill(Color.web("#00FF00"));
            } else {
                status.setText("Error while loading data");
                status.setTextFill(Color.web("#ff0000"));
            }
        });

        btnAdd.setOnAction(event -> {
            if (!addTitle.getText().trim().isEmpty() && addTitle.getText() != null &&
                    !addDirector.getText().trim().isEmpty() && addDirector.getText() != null &&
                    !addYear.getText().trim().isEmpty() && addYear.getText() != null &&
                    tryParse(addYear.getText()) != null && addData()) {
                status.setText("Record successfully added");
                status.setTextFill(Color.web("#00FF00"));
            } else {
                status.setText("Error while adding record");
                status.setTextFill(Color.web("#ff0000"));
            }
        });

        btnDisconnect.setOnAction(event -> disconnect());

        table1.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Film>() {
            @Override
            public void changed(ObservableValue<? extends Film> observable, Film oldValue, Film newValue) {
                if(table1.getSelectionModel().getSelectedItem() != null)
                {
                    btnDelete.setDisable(false);
                    btnEdit.setDisable(false);
                    Film film = table1.getSelectionModel().getSelectedItem();

                    btnDelete.setOnAction(event -> {
                        if (deleteData(film.getID())) {
                            status.setText("Record successfully deleted");
                            status.setTextFill(Color.web("#00FF00"));
                        } else {
                            status.setText("Error while deleting record");
                            status.setTextFill(Color.web("#ff0000"));
                        }
                    });

                    btnEdit.setOnAction(event -> {
                        try {
                            Statement statement = connection.createStatement();
                            ResultSet resultSet;
                            statement.executeQuery("use test_db;");
                            resultSet = statement.executeQuery("select * from filmlibrary where ID=" + film.getID() + ";");
                            DateFormat dateFormat = new SimpleDateFormat("yyyy");
                            while (resultSet.next()) {
                                addTitle.setText(resultSet.getString(2));
                                addDirector.setText(resultSet.getString(3));
                                addYear.setText(dateFormat.format(resultSet.getDate(4)));
                                addRating.setValue(resultSet.getByte(5));
                            }
                            btnEdit.setDisable(true);
                            btnUpdate.setDisable(false);
                            btnAdd.setDisable(true);
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    });

                    btnUpdate.setOnAction(event -> {
                        if (editData(film.getID())) {
                            status.setText("Record successfully updated");
                            status.setTextFill(Color.web("#00FF00"));
                            btnEdit.setDisable(false);
                        } else {
                            status.setText("Error while updating record");
                            status.setTextFill(Color.web("#ff0000"));
                        }
                    });
                } else {
                    btnEdit.setDisable(true);
                    btnDelete.setDisable(true);
                    btnUpdate.setDisable(true);
                    btnAdd.setDisable(false);
                }
            }
        });
    }

    private boolean DBConnection() {
        boolean result = false;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

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

    private void disconnect() {
        if (connection != null) {
            try {
                System.out.println("Disconnected ID: " + connection.toString());
                connection.close();
                status.setText("Disconnected successfully");
                status.setTextFill(Color.web("#00FF00"));
                btnRefresh.setDisable(true);
                btnDelete.setDisable(true);
                btnAdd.setDisable(true);
                btnDisconnect.setDisable(true);
                btnConnect.setDisable(false);
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

    private boolean editData(int IDnumber) {
        boolean result = false;
        try {
            PreparedStatement statement1 = connection.prepareStatement("update filmlibrary set Title=?, Director=?, Year=?, Rating=? where ID=?");
            statement1.setString(1, addTitle.getText().trim());
            statement1.setString(2, addDirector.getText().trim());
            statement1.setString(3, addYear.getText().trim());
            statement1.setInt(4, (int) addRating.getValue());
            statement1.setInt(5, IDnumber);
            statement1.executeUpdate();
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
            int number = deleteNumber;
            Statement statement = connection.createStatement();
            statement.executeQuery("use test_db;");
            statement.executeUpdate("delete from filmlibrary where ID = " + number + ";");
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
            Statement statement = connection.createStatement();
            statement.executeQuery("use test_db;");

            PreparedStatement statement1 = connection.prepareStatement("insert into filmlibrary (Title, Director, Year, Rating) values (?,?,?,?);");
            statement1.setString(1, addTitle.getText().trim());
            statement1.setString(2, addDirector.getText().trim());
            statement1.setString(3, addYear.getText().trim());
            statement1.setString(4, Double.toString(addRating.getValue()));
            System.out.println("Parametrized query" + statement1.toString());

            int resultStatement = statement1.executeUpdate();
            System.out.println("Inserted " + resultStatement + " records");
            statement.close();
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
            resultSet = statement.executeQuery("select * from filmlibrary;");
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