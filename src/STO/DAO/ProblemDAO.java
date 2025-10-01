package STO.DAO;

import STO.DataBase;
import STO.Problem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProblemDAO {

    public static List<Problem> getAllProblems() throws SQLException {
        List<Problem> problems = new ArrayList<>();
        String sql = "SELECT * FROM typeofrepair";
        try (Statement stmt = DataBase.connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Problem p = new Problem(
                        rs.getInt("idrepair"),
                        rs.getString("repairname"),
                        rs.getDouble("price"),
                        rs.getInt("repairtime")
                );
                problems.add(p);
            }
        }
        return problems;
    }

    public static Problem getByName(String name) throws SQLException {
        String sql = "SELECT * FROM typeofrepair WHERE repairname = ?";
        try (PreparedStatement stmt = DataBase.connection.prepareStatement(sql)) {
            stmt.setString(1, name);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Problem(
                            rs.getInt("idrepair"),
                            rs.getString("repairname"),
                            rs.getDouble("price"),
                            rs.getInt("repairtime")
                    );
                }
            }
        }
        return null;
    }

    public static Problem getById(int id) throws SQLException {
        String sql = "SELECT * FROM typeofrepair WHERE idrepair = ?";
        try (PreparedStatement stmt = DataBase.connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Problem(
                            rs.getInt("idrepair"),
                            rs.getString("repairname"),
                            rs.getDouble("price"),
                            rs.getInt("repairtime")
                    );
                }
            }
        }
        return null;
    }

    public static boolean insertProblemsForCommission(int commissionId, List<String> problemNames) throws SQLException {
        String selectRepairSQL = "SELECT idrepair FROM typeofrepair WHERE repairname = ?";
        String insertCommissionRepairSQL = "INSERT INTO CommissionTypeofRepair (Idcommission, idrepair) VALUES (?, ?)";

        try (PreparedStatement selectRepairStmt = DataBase.connection.prepareStatement(selectRepairSQL);
             PreparedStatement insertStmt = DataBase.connection.prepareStatement(insertCommissionRepairSQL)) {

            for (String problemName : problemNames) {
                selectRepairStmt.setString(1, problemName);
                try (ResultSet rsRepair = selectRepairStmt.executeQuery()) {
                    if (rsRepair.next()) {
                        int repairId = rsRepair.getInt("idrepair");
                        insertStmt.setInt(1, commissionId);
                        insertStmt.setInt(2, repairId);
                        insertStmt.executeUpdate();
                    } else {
                        // Якщо проблема не знайдена, можна повертати false
                        return false;
                    }
                }
            }
            return true;
        }
    }
    public static String getRepairNameById(int repairId) throws SQLException {
        String sql = "SELECT repairname FROM typeofrepair WHERE idrepair = ?";
        try (PreparedStatement stmt = DataBase.connection.prepareStatement(sql)) {
            stmt.setInt(1, repairId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("repairname");
            }
        }
        return "Невідомий ремонт";
    }


}
