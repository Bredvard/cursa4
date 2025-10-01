package STO.GUI;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import STO.DAO.CommissionDAO;
import STO.*;
import java.sql.*;
import java.sql.SQLException;
import java.awt.*;

public class DeliveryMenu extends JPanel {
    private JPanel contentPane;
    private JTable finishedCommissionsTable;
    private JButton button1;
    private DefaultTableModel finishedCommissionsTableModel;
    private MaterialsMenu materialsMenu;

    public DeliveryMenu(MaterialsMenu materialsMenu) {
       initUI();
       initTable();
       button1.addActionListener(e -> issueSelectedCommission());
       fillTable();
    }
    private void initUI(){
        this.materialsMenu = materialsMenu;
        setLayout(new BorderLayout());
        add(contentPane, BorderLayout.CENTER);
    }
    private void initTable(){
        String[] columns = {"Номер замовлення", "Ім'я клієнта", "Номер телефону", "Статус", "Дата початку", "Дата завершення"};
        finishedCommissionsTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;  // Всі клітинки не редагуються
            }
        };

        try {
            CommissionDAO.updateFinishedCommissionsIfDue();  // 🔹 Перевірка дати
        } catch (SQLException e) {
            e.printStackTrace();
            UIHelper.showError(this, "Помилка при оновленні статусу замовлень");
        }
    }
    public void fillTable() {
        String query = """
        SELECT c.idcommission, cl.ClientName, cl.ClientPhone,
               c.status, c.commissionstart, c.commissionfinish
        FROM commission c
        JOIN car ca ON c.idcar = ca.idcar
        JOIN client cl ON ca.idclient = cl.idclient
        WHERE c.status = 'завершено'
        """;

        try (Connection conn = DataBase.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Object[] row = {
                        rs.getInt("idcommission"),
                        rs.getString("ClientName"),
                        rs.getString("ClientPhone"),
                        rs.getString("status"),
                        rs.getDate("commissionstart"),
                        rs.getDate("commissionfinish")
                };
                finishedCommissionsTableModel.addRow(row);
            }

            finishedCommissionsTable.setModel(finishedCommissionsTableModel);
        } catch (SQLException e) {
            UIHelper.showError(this, "Помилка під час завантаження даних: " + e.getMessage());
        }
    }

    private void issueSelectedCommission() {
        int selectedRow = finishedCommissionsTable.getSelectedRow();
        if (selectedRow == -1) {
            UIHelper.showWarning(this, "Виберіть замовлення для видачі");
            return;
        }

        int commissionId = (int) finishedCommissionsTableModel.getValueAt(selectedRow, 0);

        try {
            boolean success = CommissionDAO.completeCommissionAndUpdateMaterials(commissionId);
            if (success) {
                UIHelper.showInfo(this, "Замовлення успішно видано та склад оновлено");
                finishedCommissionsTableModel.removeRow(selectedRow);
                materialsMenu.loadMaterials(); // Видалити з таблиці
            }
        } catch (SQLException ex) {
            UIHelper.showError(this, "Помилка під час видачі замовлення: " + ex.getMessage());
        }
    }
}