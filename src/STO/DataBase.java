package STO;
import java.sql.*;

public class DataBase {
    public static final String URL = "jdbc:mysql://localhost:3306/kursa4";
    public static final String USER = "root";
    public static final String PASSWORD = "bredvard";
    public static Statement statement;
    public static Connection connection;

    // Блок ініціалізації
    // Метод для отримання нового підключення
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // Метод для закриття ресурсів
    public static void close(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void close(Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void close(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}