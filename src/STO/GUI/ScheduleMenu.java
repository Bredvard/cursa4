package STO.GUI;
import STO.DAO.MasterDAO;
import STO.DAO.ProblemDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import STO.*;
import STO.DAO.CommissionDAO;
import STO.DAO.RepairParticipationDAO;

import java.util.List;
import java.sql.*;

public class ScheduleMenu extends JPanel {
    private JComboBox<Master> mastercomboBox;
    private JTable commissionTable;
    private JComboBox<Commission> commissioncomboBox;
    private JButton deletefromschedule;
    private JPanel contentPane;
    private JButton assignMasterButton;
    private DefaultTableModel commissionTableModel;
    private int currentMasterId;

    public ScheduleMenu() {

        initUI();
        initListener();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        add(contentPane, BorderLayout.CENTER);

        assignMasterButton.addActionListener(e -> assignMasterToCommission());

        String[] columns = {"Номер замовлення", "Дата початку", "Дата завершення", "Статус", "Ремонт"};
        commissionTableModel = new DefaultTableModel(columns, 0){
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        commissionTable.setModel(commissionTableModel);
    }

    private void  initListener (){
        assignMasterButton.addActionListener(e -> assignMasterToCommission());

        mastercomboBox.addActionListener(e -> {
            Master selectedMaster = (Master) mastercomboBox.getSelectedItem();
            if (selectedMaster != null) {
                currentMasterId = selectedMaster.getId();
                loadCommissionsToTable();
            }
        });
        if (mastercomboBox.getItemCount() > 0) {
            mastercomboBox.setSelectedIndex(0);
        }

        deletefromschedule.addActionListener(e -> deleteSelectedCommission());
        commissionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        loadMasters();
        loadCommissionsToComboBox();
        loadCommissionsToTable();

    }

    private void loadMasters() {
        try {
            List<Master> masters = MasterDAO.getAllWorkers();
            mastercomboBox.removeAllItems();
            for (Master w : masters) {
                mastercomboBox.addItem(w);
            }
        } catch (SQLException e) {
            UIHelper.showError(this, "Не вдалося завантажити майстрів: " + e.getMessage());
        }
    }

    protected void loadCommissionsToComboBox() {
        try {
            List<Commission> availableCommissions = CommissionDAO.getAvailableCommissions();
            System.out.println("Доступних замовлень: " + availableCommissions.size());

            commissioncomboBox.removeAllItems();

            for (Commission commission : availableCommissions) {
                System.out.println("Додаємо до вибору: " + commission);
                commissioncomboBox.addItem(commission);
            }
        } catch (SQLException ex) {
            System.err.println("Помилка завантаження: " + ex.getMessage());
            UIHelper.showError(this, "Не вдалося завантажити доступні замовлення");
        }
    }

    private void deleteSelectedCommission() {
        int selectedRow = commissionTable.getSelectedRow();

        if (selectedRow == -1) {
            UIHelper.showWarning(this, "Оберіть замовлення для видалення");
            return;
        }

        int commissionId = (int) commissionTableModel.getValueAt(selectedRow, 0);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Ви впевнені, що хочете видалити це замовлення з розкладу майстра?",
                "Підтвердження", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            boolean deleted = RepairParticipationDAO.deleteParticipation(currentMasterId, commissionId);

            if (deleted) {
                UIHelper.showInfo(this, "Замовлення успішно видалено");
                refreshData();
            } else {
                UIHelper.showError(this, "Не вдалося видалити замовлення");
            }
        } catch (SQLException ex) {
            UIHelper.showError(this, "Помилка при видаленні: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void assignMasterToCommission() {
        Master selectedMaster = (Master) mastercomboBox.getSelectedItem();
        Commission selectedCommission = (Commission) commissioncomboBox.getSelectedItem();

        if (selectedMaster == null || selectedCommission == null) {
            UIHelper.showWarning(this, "Будь ласка, виберіть майстра та замовлення");
            return;
        }

        try {
            int repairId = CommissionDAO.getRepairIdByCommissionId(selectedCommission.getIdCommission());

            boolean success = RepairParticipationDAO.assignMasterToCommission(
                    selectedCommission.getIdCommission(),
                    selectedMaster.getId(),
                    repairId
            );

            if (success) {
                UIHelper.showInfo(this, "Майстра " + selectedMaster.getFullName() +
                        " успішно призначено на замовлення #" +
                        selectedCommission.getIdCommission());
                refreshData();
            } else {
                UIHelper.showError(this, "Не вдалося призначити майстра");
            }
        } catch (SQLException ex) {
            UIHelper.showError(this, "Помилка бази даних: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void refreshData() {
        loadCommissionsToComboBox();
        loadCommissionsToTable();
        // loadMasters(); // опціонально
    }

    private void loadCommissionsToTable() {
        try {
            List<RepairParticipation> commissions = RepairParticipationDAO.getCommissionsByMasterId(currentMasterId);
            commissionTableModel.setRowCount(0);

            for (RepairParticipation c : commissions) {
                String repairName = "Невідомий ремонт";
                try {
                    repairName = ProblemDAO.getRepairNameById(c.getidRepair());
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                Object[] row = new Object[]{
                        c.getIdCommission(),
                        c.getCommissionStart(),
                        c.getCommissionEnd(),
                        c.getStatus(),
                        repairName
                };
                commissionTableModel.addRow(row);
            }
        } catch (SQLException e) {
            UIHelper.showError(this, "Не вдалося завантажити замовлення для майстра: " + e.getMessage());
        }
    }
}