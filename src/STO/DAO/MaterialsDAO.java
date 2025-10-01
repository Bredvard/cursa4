package STO.DAO;

import STO.Materials;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MaterialsDAO {
    private Connection connection;

    public MaterialsDAO(Connection connection) {
        this.connection = connection;
    }

    // Отримати всі матеріали
    public List<Materials> getAllMaterials() {
        List<Materials> materials = new ArrayList<>();
        String sql = "SELECT iddetail, detailName, detailCount, detailPrice as price FROM details"; // заміни 0.0 на detailPrice якщо є

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("iddetail");
                String name = rs.getString("detailName");
                int count = rs.getInt("detailCount");
                double price = rs.getDouble("price"); // замінити якщо detailPrice доданий

                materials.add(new Materials(id, name, count, price));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return materials;
    }

    // Оновити кількість матеріалу після надходження
    public boolean updateMaterialCount(int id, int newCount) {
        String sql = "UPDATE details SET detailCount = ? WHERE iddetail = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, newCount);
            stmt.setInt(2, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Отримати матеріал за ID
    public Materials getMaterialById(int id) {
        String sql = "SELECT iddetail, detailName, detailCount, 0.0 as price FROM details WHERE iddetail = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String name = rs.getString("detailName");
                    int count = rs.getInt("detailCount");
                    double price = rs.getDouble("price"); // або detailPrice

                    return new Materials(id, name, count, price);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


}
