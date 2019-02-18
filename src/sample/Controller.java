package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;

import java.sql.*;
import java.util.Properties;

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
    private TextField deleteNumber;

    @FXML
    private Button btnDelete;

    @FXML
    void initialize(){
        btnConnect.setOnAction(event->{
            if (DBConnection()) {
                status.setText("Connection successful");
                status.setTextFill(Color.web("#1dff00"));
            } else {
                status.setText("Connection failed");
                status.setTextFill(Color.web("#ff0000"));
            }
        });

        btnRefresh.setOnAction(event -> {
            if (loadData()) {
                status.setText("Data updated");
                status.setTextFill(Color.web("#1dff00"));
            } else {
                status.setText("Error while loading data");
                status.setTextFill(Color.web("#ff0000"));
            }
        });

        btnAdd.setOnAction(event -> {
            if (addData()) {
                status.setText("Record successfully added");
                status.setTextFill(Color.web("#1dff00"));
            } else {
                status.setText("Error while adding record");
                status.setTextFill(Color.web("#ff0000"));
            }
        });

        btnDelete.setOnAction(event -> {

        });

    }
    private boolean deleteData() {
        
    }

    private boolean addData() {
        boolean result = false;
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("use filmlibrary;");

            PreparedStatement statement1 = connection.prepareStatement("insert into filmlibrary(Title, Director, Year, Rating) values (?,?,?,?);");
            statement1.setString(1, addTitle.getText());
            statement1.setString(2, addDirector.getText());
            statement1.setString(3, addYear.getText());
            statement1.setString(4, Double.toString(addRating.getValue()));
            System.out.println("Parametrized query" + statement1.toString());

            int resultStatement = statement1.executeUpdate();
            System.out.println("Inserted " + resultStatement + " records");
            statement.close();
            resultSet.close();
            result = true;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
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

            data = FXCollections.observableArrayList();

            idNumber.setCellValueFactory(new PropertyValueFactory<>("ID"));
            title.setCellValueFactory(new PropertyValueFactory<>("title"));
            director.setCellValueFactory(new PropertyValueFactory<>("director"));
            year.setCellValueFactory(new PropertyValueFactory<>("year"));
            rating.setCellValueFactory(new PropertyValueFactory<>("rating"));
            while (resultSet.next()) {
                System.out.println(resultSet.getString(1) + "\t" + resultSet.getString(2) + "\t"
                        + resultSet.getString(3) + "\t" + resultSet.getString(4) + "\t" + resultSet.getString(5));
                data.add(new Film(resultSet.getByte(1), resultSet.getString(2), resultSet.getString(3),
                        resultSet.getDate(4), resultSet.getByte(5)));
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

    private Connection connection = null;
    private boolean DBConnection() {
        boolean result = false;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        try {
            String url = "jdbc:mysql://localhost:3306/test_db";
            String user = "root";
            String pass = "root";
            Properties properties = new Properties();
            properties.setProperty("user", user);
            properties.setProperty("password", pass);
            properties.setProperty("useSSL", "false");
            properties.setProperty("autoReconnect", "true");
            connection = getConnection(url, properties);
            System.out.println("Connection ID" + connection.toString());
            result = true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    return result;
    }
}
