package STO.DAO;

import STO.Commission;
import STO.DataBase;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CommissionDAO {

    public static int insertCommission(Commission commission) {
        String sql = "INSERT INTO commission (commissionstart, status, idcar) VALUES (?, ?, ?)";
        try (Connection conn = DataBase.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setDate(1, commission.getCommissionStart());
            ps.setString(2, commission.getStatus());
            ps.setInt(3, commission.getIdCar());

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Створення комісії не вдалося, жоден рядок не доданий.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Створення комісії не вдалося, ID не отримано.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static Commission getCommissionById(int id) {
        String sql = "SELECT * FROM commission WHERE idcommission = ?";
        try (Connection conn = DataBase.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Commission(
                            rs.getInt("idcommission"),
                            rs.getDate("commissionstart"),
                            rs.getString("status"),
                            rs.getInt("idcar")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<Commission> getAllCommissions() {
        List<Commission> commissions = new ArrayList<>();
        String sql = "SELECT idcommission, commissionstart, status, idcar FROM commission";

        try (Connection conn = DataBase.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Commission commission = new Commission(
                        rs.getInt("idcommission"),
                        rs.getDate("commissionstart"),
                        rs.getString("status"),
                        rs.getInt("idcar")
                );
                commissions.add(commission);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return commissions;
    }

    public static boolean updateStatus(int id, String newStatus) {
        String sql = "UPDATE commission SET status = ? WHERE idcommission = ?";
        try (Connection conn = DataBase.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, id);
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteCommission(int id) {
        String sql = "DELETE FROM commission WHERE idcommission = ?";
        try (Connection conn = DataBase.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static Date calculateAndSaveCommissionEndDate(int commissionId) throws SQLException {
        String getStartDateSQL = "SELECT commissionstart FROM commission WHERE idcommission = ?";
        String getTotalRepairTimeSQL = """
                SELECT SUM(tr.repairtime) AS totaldays
                FROM CommissionTypeofRepair ctr
                JOIN typeofrepair tr ON ctr.idrepair = tr.idrepair
                WHERE ctr.Idcommission = ?
                """;
        String updateEndDateSQL = "UPDATE commission SET commissionfinish = ? WHERE idcommission = ?";

        LocalDate startDate;
        int totalDays = 0;

        try (Connection conn = DataBase.getConnection()) {
            // Отримуємо дату початку
            try (PreparedStatement startStmt = conn.prepareStatement(getStartDateSQL)) {
                startStmt.setInt(1, commissionId);
                try (ResultSet rsStart = startStmt.executeQuery()) {
                    if (rsStart.next()) {
                        startDate = rsStart.getDate("commissionstart").toLocalDate();
                    } else {
                        throw new SQLException("Комісію з ID " + commissionId + " не знайдено.");
                    }
                }
            }

            // Рахуємо загальну тривалість ремонту
            try (PreparedStatement sumStmt = conn.prepareStatement(getTotalRepairTimeSQL)) {
                sumStmt.setInt(1, commissionId);
                try (ResultSet rsSum = sumStmt.executeQuery()) {
                    if (rsSum.next()) {
                        totalDays = rsSum.getInt("totaldays");
                    }
                }
            }

            // Обчислюємо дату завершення
            LocalDate endDate = startDate.plusDays(totalDays);
            Date sqlEndDate = Date.valueOf(endDate);

            // Оновлюємо дату завершення
            try (PreparedStatement updateStmt = conn.prepareStatement(updateEndDateSQL)) {
                updateStmt.setDate(1, sqlEndDate);
                updateStmt.setInt(2, commissionId);
                updateStmt.executeUpdate();
            }

            return sqlEndDate;
        }
    }

    public static List<Commission> getAvailableCommissions() throws SQLException {
        String sql = "SELECT c.* FROM commission c " +
                "WHERE c.idcommission NOT IN (SELECT rp.idcommission FROM RepairParticipation rp) " +
                "AND c.status = 'в обробці'";

        List<Commission> commissions = new ArrayList<>();

        try (Connection conn = DataBase.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Commission commission = new Commission(
                        rs.getInt("idcommission"),
                        rs.getDate("commissionstart"),
                        rs.getString("status"),
                        rs.getInt("idcar")
                );
                commissions.add(commission);
            }
        }
        return commissions;
    }

    public static int getRepairIdByCommissionId(int commissionId) throws SQLException {
        String sql = "SELECT idrepair FROM CommissionTypeofRepair WHERE Idcommission = ?";
        try (Connection conn = DataBase.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, commissionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("idrepair");
                } else {
                    throw new SQLException("Ремонт не знайдено для замовлення з id: " + commissionId);
                }
            }
        }
    }

    public static boolean completeCommissionAndUpdateMaterials(int commissionId) throws SQLException {
        Connection conn = null;
        PreparedStatement psGetRepairs = null;
        PreparedStatement psGetDetails = null;
        PreparedStatement psUpdateDetailCount = null;
        PreparedStatement psUpdateCommission = null;
        ResultSet rsRepairs = null;
        ResultSet rsDetails = null;

        try {
            conn = DataBase.getConnection();
            conn.setAutoCommit(false);

            // 1. Отримати всі ремонти, пов'язані з комісією
            String sqlGetRepairs = """
            SELECT idrepair FROM CommissionTypeofRepair
            WHERE idcommission = ?
        """;
            psGetRepairs = conn.prepareStatement(sqlGetRepairs);
            psGetRepairs.setInt(1, commissionId);
            rsRepairs = psGetRepairs.executeQuery();

            // 2. Підготувати запит для отримання потрібних деталей по ремонту з назвою
            String sqlGetDetails = """
            SELECT d.iddetail, d.detailname, r.required_count, d.detailcount
            FROM repair_detail_requirements r
            JOIN details d ON r.iddetail = d.iddetail
            WHERE r.idrepair = ?
        """;
            psGetDetails = conn.prepareStatement(sqlGetDetails);

            // 3. Підготувати запит на оновлення деталей
            String sqlUpdateDetail = """
            UPDATE details SET detailcount = detailcount - ?
            WHERE iddetail = ?
        """;
            psUpdateDetailCount = conn.prepareStatement(sqlUpdateDetail);

            // 4. Для кожного ремонту оновити деталі
            while (rsRepairs.next()) {
                int repairId = rsRepairs.getInt("idrepair");

                psGetDetails.setInt(1, repairId);
                rsDetails = psGetDetails.executeQuery();

                while (rsDetails.next()) {
                    int detailId = rsDetails.getInt("iddetail");
                    String detailName = rsDetails.getString("detailname");
                    int requiredCount = rsDetails.getInt("required_count");
                    int availableCount = rsDetails.getInt("detailcount");

                    if (availableCount < requiredCount) {
                        conn.rollback();
                        throw new SQLException("Недостатньо деталей на складі: " + detailName +
                                " (потрібно: " + requiredCount +
                                ", доступно: " + availableCount + ")");
                    }

                    psUpdateDetailCount.setInt(1, requiredCount);
                    psUpdateDetailCount.setInt(2, detailId);
                    psUpdateDetailCount.executeUpdate();
                }

                rsDetails.close();
            }

            // 5. Оновити статус комісії
            String sqlUpdateCommission = "UPDATE commission SET status = 'видано' WHERE idcommission = ?";
            psUpdateCommission = conn.prepareStatement(sqlUpdateCommission);
            psUpdateCommission.setInt(1, commissionId);
            psUpdateCommission.executeUpdate();

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (rsRepairs != null) rsRepairs.close();
            if (rsDetails != null) rsDetails.close();
            if (psGetRepairs != null) psGetRepairs.close();
            if (psGetDetails != null) psGetDetails.close();
            if (psUpdateDetailCount != null) psUpdateDetailCount.close();
            if (psUpdateCommission != null) psUpdateCommission.close();
            if (conn != null) conn.setAutoCommit(true);
        }
    }
    public static void updateFinishedCommissionsIfDue() throws SQLException {
        String sql = """
        UPDATE commission
        SET status = 'завершено'
        WHERE status NOT IN ('завершено', 'видано', 'в обробці')
          AND commissionfinish <= CURRENT_DATE
    """;

        try (Connection conn = DataBase.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
        }
    }
}
