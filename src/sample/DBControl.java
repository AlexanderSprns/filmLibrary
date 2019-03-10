package sample;

import javafx.scene.control.Slider;
import javafx.scene.control.TextField;

import java.sql.*;

import static java.sql.DriverManager.getConnection;

class DBControl {

    private static Connection connection = null;

    boolean dbConnection() {
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

    Object loadData() {
        ResultSet resultSet = null;
        try {
            Statement statement = DBControl.connection.createStatement();
            resultSet = statement.executeQuery("select filmlibrary.id, filmlibrary.title, directors.directorName, filmlibrary.year, filmlibrary.rating" +
                    " from filmlibrary inner join directors on filmlibrary.iddirector = directors.iddirectors;");
//            resultSet.close();
//            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultSet;
    }

    Object edit(Film film) {
        ResultSet resultSet = null;
        try {
            PreparedStatement statement = connection.prepareStatement("select filmlibrary.id, filmlibrary.title, directors.directorName, filmlibrary.year, filmlibrary.rating" +
                    " from filmlibrary inner join directors on filmlibrary.iddirector = directors.iddirectors where id=?;");
            statement.setInt(1, film.getID());
            resultSet = statement.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultSet;
    }

    boolean update(int IDnumber, TextField addDirector, TextField addTitle, TextField addYear, Slider addRating) {
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    boolean deleteData(Integer deleteNumber) {
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

    boolean addNewData(TextField addDirector, TextField addTitle, TextField addYear, Slider addRating) {
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }
}