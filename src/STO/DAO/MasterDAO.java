package STO.DAO;

import STO.DataBase;
import STO.Master;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MasterDAO {

    public static List<Master> getAllWorkers() throws SQLException {
        List<Master> workers = new ArrayList<>();
        String sql = "SELECT idWorker, workertName, workerSurname, workerFathername FROM worker";

        try (PreparedStatement stmt = DataBase.connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("idWorker");
                String name = rs.getString("workertName");
                String surname = rs.getString("workerSurname");
                String fathername = rs.getString("workerFathername");

                Master worker = new Master(id, name, surname, fathername, 1);  // 1 — можливо, роль або статус, як у твоєму коді
                workers.add(worker);
            }
        }
        return workers;
    }
}
