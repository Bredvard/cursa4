package STO.DAO;

import STO.Car;
import STO.DataBase;

import java.sql.*;

public class CarDAO {
    public static int insertCar(Car car) throws SQLException {
        String sql = "INSERT INTO car (CarNomer, CarMark, IdClient) VALUES (?, ?, ?)";
        try (Connection conn = DataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, car.getNumber());
            stmt.setString(2, car.getMark());
            stmt.setInt(3, car.getClientId());

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        return -1;
    }

    public static boolean isSupportedMark(String mark) throws SQLException {
        String sql = "SELECT COUNT(*) FROM workshop_car_marks WHERE LOWER(car_mark) = LOWER(?)";
        try (Connection conn = DataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, mark);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public static java.util.List<String> getSupportedMarks() throws SQLException {
        String sql = "SELECT car_mark FROM workshop_car_marks";
        java.util.List<String> marks = new java.util.ArrayList<>();

        try (Connection conn = DataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                marks.add(rs.getString("car_mark"));
            }
        }

        return marks;
    }

}
