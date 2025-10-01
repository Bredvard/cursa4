package STO.DAO;

import STO.DataBase;
import STO.RepairParticipation;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public class RepairParticipationDAO {

    // Призначити майстра на замовлення (додати участь у ремонті) та оновити статус замовлення
    public static boolean assignMasterToCommission(int commissionId, int masterId, int repairId)
            throws SQLException {

        PreparedStatement ps = null;
        ResultSet rs = null;
        Connection conn = null;

        try {
            conn = DataBase.getConnection();
            conn.setAutoCommit(false);

            // Перевірка чи майстер вже призначений
            String checkSql = "SELECT 1 FROM RepairParticipation WHERE idcommission = ? AND idworker = ?";
            ps = conn.prepareStatement(checkSql);
            ps.setInt(1, commissionId);
            ps.setInt(2, masterId);
            rs = ps.executeQuery();

            if (rs.next()) {
                JOptionPane.showMessageDialog(null,
                        "Цей майстер вже призначений на дане замовлення",
                        "Попередження", JOptionPane.WARNING_MESSAGE);
                return false;
            }
            rs.close();
            ps.close();

            // Отримання ціни ремонту
            double repairPrice = 0;
            String priceSql = "SELECT price FROM typeofrepair WHERE idrepair = ?";
            ps = conn.prepareStatement(priceSql);
            ps.setInt(1, repairId);
            rs = ps.executeQuery();
            if (rs.next()) {
                repairPrice = rs.getDouble("price");
            }
            rs.close();
            ps.close();

            // Додавання участі майстра з ціною
            String insertSql = "INSERT INTO RepairParticipation (idrepair, idworker, idcommission, price) VALUES (?, ?, ?, ?)";
            ps = conn.prepareStatement(insertSql);
            ps.setInt(1, repairId);
            ps.setInt(2, masterId);
            ps.setInt(3, commissionId);
            ps.setDouble(4, repairPrice);
            ps.executeUpdate();
            ps.close();

            // Оновлення статусу замовлення
            String updateSql = "UPDATE commission SET status = 'у роботі' WHERE idcommission = ?";
            ps = conn.prepareStatement(updateSql);
            ps.setInt(1, commissionId);
            ps.executeUpdate();

            conn.commit();
            return true;

        } catch (SQLException ex) {
            if (conn != null) conn.rollback();
            throw ex;
        } finally {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
            if (conn != null) conn.setAutoCommit(true);
        }
    }

    // Отримати список замовлень, де працює конкретний майстер (із інформацією про майстрів)
    public static List<RepairParticipation> getCommissionsByMasterId(int masterId) throws SQLException {

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<RepairParticipation> list = new ArrayList<>();
        try {
            conn = DataBase.getConnection(); // Отримуємо нове підключення
            String sql = "SELECT c.idcommission, c.commissionstart, c.commissionfinish, c.status, rp.idrepair, rp.price " +
                    "FROM commission c " +
                    "JOIN RepairParticipation rp ON c.idcommission = rp.idcommission " +
                    "WHERE rp.idworker = ? AND c.status = 'у роботі' " +
                    "GROUP BY c.idcommission, c.commissionstart, c.status, rp.idrepair, c.commissionfinish, rp.price";

            ps = conn.prepareStatement(sql);
            ps.setInt(1, masterId);
            rs = ps.executeQuery();
                while (rs.next()) {
                    RepairParticipation c = new RepairParticipation(
                            rs.getInt("idcommission"),
                            rs.getDate("commissionstart"),
                            rs.getDate("commissionfinish"),
                            rs.getInt("idrepair"),
                            rs.getString("status"),
                            rs.getDouble("price")
                    );
                    list.add(c);
                }
            } finally {
                DataBase.close(rs);
                DataBase.close(ps);
                DataBase.close(conn);
            }
            return list;


    }
    public static List<RepairParticipation> getAllCommissions() throws SQLException {
        String sql = "SELECT rp.*, c.commissionstart, c.status, rp.idrepair " +
                "FROM RepairParticipation rp " +
                "JOIN commission c ON rp.idcommission = c.idcommission";

        List<RepairParticipation> list = new ArrayList<>();

        try (PreparedStatement ps = DataBase.connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                RepairParticipation rp = new RepairParticipation(
                        rs.getInt("idcommission"),
                        rs.getDate("commissionstart"),
                        rs.getDate("commissionfinish"),
                        rs.getInt("idrepair"),
                        rs.getString("status"),
                        rs.getDouble("price")
                );
                list.add(rp);
            }
        }
        return list;
    }

    public static boolean deleteParticipation(int masterId, int commissionId) throws SQLException {
        Connection conn = null;
        PreparedStatement deleteStmt = null;
        PreparedStatement updateStatusStmt = null;

        try {
            conn = DataBase.getConnection();
            conn.setAutoCommit(false); // Транзакція

            // Видалення участі
            String deleteSQL = "DELETE FROM RepairParticipation WHERE idworker = ? AND idcommission = ?";
            deleteStmt = conn.prepareStatement(deleteSQL);
            deleteStmt.setInt(1, masterId);
            deleteStmt.setInt(2, commissionId);

            int deletedRows = deleteStmt.executeUpdate();

            // Зміна статусу комісії
            String updateSQL = "UPDATE Commission SET status = 'в обробці' WHERE idcommission = ?";
            updateStatusStmt = conn.prepareStatement(updateSQL);
            updateStatusStmt.setInt(1, commissionId);
            updateStatusStmt.executeUpdate();

            conn.commit();
            return deletedRows > 0;
        } catch (SQLException ex) {
            if (conn != null) conn.rollback(); // Відкат, якщо помилка
            throw ex;
        } finally {
            if (deleteStmt != null) deleteStmt.close();
            if (updateStatusStmt != null) updateStatusStmt.close();
            if (conn != null) conn.setAutoCommit(true);
        }
    }





}
