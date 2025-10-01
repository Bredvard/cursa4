package STO.DAO;
import STO.Client;
import STO.DataBase;

import java.sql.*;

public class ClientDao {

    public static int insertClient(Client client) throws SQLException {
        String sql = "INSERT INTO client (ClientName, ClientSurname, ClientPhone, ClientEmail) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = DataBase.connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, client.getName());
            stmt.setString(2, client.getSurname());
            stmt.setString(3, client.getPhone());
            stmt.setString(4, client.getEmail());
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1); // Повертаємо згенерований ID
            }
        }
        return -1;
    }
}