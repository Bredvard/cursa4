package STO;

import java.sql.*;
import javax.swing.*;
import STO.GUI.MainMenu;

public class Main {
    public static void main(String[] args) {
        try {
            DataBase.connection = DriverManager.getConnection(DataBase.URL, DataBase.USER, DataBase.PASSWORD);
            DataBase.statement = DataBase.connection.createStatement();
            SwingUtilities.invokeLater(() -> {
                JFrame frame = new JFrame("СТО - Меню");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setContentPane(new MainMenu()); // Тут головне вікно
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            });

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Помилка підключення до бази даних: " + e.getMessage(),
                    "Помилка", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }
}
