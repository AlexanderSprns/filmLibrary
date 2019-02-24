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
import java.util.HashMap;
import java.util.Map;

import static java.sql.DriverManager.getConnection;

public class Controller {
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
    void initialize(){

        btnConnect.setOnAction(event->{
            if (DBConnection()) {
                status.setText("Connection successful");
                status.setTextFill(Color.web("#00FF00"));
                btnAdd.setDisable(false);
                btnDisconnect.setDisable(false);
                btnRefresh.setDisable(false);
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
                    Film film = table1.getSelectionModel().getSelectedItem();
                    Integer number = film.getRowNumber();

                    btnDelete.setOnAction(event -> {
                        if (deleteData(hashMap.get(number))) {
                            status.setText("Record successfully deleted");
                            status.setTextFill(Color.web("#00FF00"));
                        } else {
                            status.setText("Error while deleting record");
                            status.setTextFill(Color.web("#ff0000"));
                        }
                    });
                }
            }
        });
    }

    private Connection connection = null;
    private Map<Integer, Integer> hashMap = new HashMap<>();

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
            ResultSet resultSet = statement.executeQuery("use test_db;");

            PreparedStatement statement1 = connection.prepareStatement("insert into filmlibrary (Title, Director, Year, Rating) values (?,?,?,?);");
            statement1.setString(1, addTitle.getText().trim());
            statement1.setString(2, addDirector.getText().trim());
            statement1.setString(3, addYear.getText().trim());
            statement1.setString(4, Double.toString(addRating.getValue()));
            System.out.println("Parametrized query" + statement1.toString());

            int resultStatement = statement1.executeUpdate();
            System.out.println("Inserted " + resultStatement + " records");
            statement.close();
            resultSet.close();
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
            int number = 1;

            data = FXCollections.observableArrayList();

            idNumber.setCellValueFactory(new PropertyValueFactory<>("rowNumber"));
            title.setCellValueFactory(new PropertyValueFactory<>("title"));
            director.setCellValueFactory(new PropertyValueFactory<>("director"));
            year.setCellValueFactory(new PropertyValueFactory<>("year"));
            rating.setCellValueFactory(new PropertyValueFactory<>("rating"));
            while (resultSet.next()) {
                System.out.println(resultSet.getString(1) + "\t" + resultSet.getString(2) + "\t"
                        + resultSet.getString(3) + "\t" + resultSet.getString(4) + "\t" + resultSet.getString(5));
                data.add(new Film(resultSet.getInt(1), number,resultSet.getString(2), resultSet.getString(3),
                        resultSet.getDate(4), resultSet.getByte(5)));
                setPair(String.valueOf(number), String.valueOf(resultSet.getInt(1)));
                number++;
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

    private void setPair(String key, String value) {
        if (tryParse(key) != null && tryParse(value) != null) {
            hashMap.put(Integer.parseInt(key), Integer.parseInt(value));
            System.out.println("Hash map:" + hashMap);
        } else
            System.out.println("Error while adding to hashmap");

    }
}