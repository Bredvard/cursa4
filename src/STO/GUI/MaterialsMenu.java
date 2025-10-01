package STO.GUI;

import STO.DAO.MaterialsDAO;
import STO.*;

import javax.swing.*;
import java.awt.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.*;

import java.util.List;
import java.sql.*;

public class MaterialsMenu extends JPanel {
    private JPanel contentPane;
    private JButton orderMaterialsButton;
    private JTable materialsTable;
    private DefaultTableModel materialsTableModel;

    public MaterialsMenu() {
        initUI();
        orderMaterialsButton.addActionListener(e -> updateSelectedMaterialCount());
        initTable();
        loadMaterials();
    }
    private void initUI(){
        setLayout(new BorderLayout());
        add(contentPane, BorderLayout.CENTER);
    }
    private void initTable(){
        String[] columns = {"Номер деталі", "Назва деталі", "Кількість", "Ціна за штуку"};
        materialsTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;  // Всі клітинки не редагуються
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 0: return Integer.class;  // Номер деталі
                    case 1: return String.class;   // Назва деталі
                    case 2: return Integer.class;  // Кількість
                    case 3: return Double.class;   // Ціна за штуку
                    default: return Object.class;
                }
            }
        };

        materialsTable.setModel(materialsTableModel);
        materialsTable.setRowSorter(new TableRowSorter<>(materialsTableModel));
        materialsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    private void updateSelectedMaterialCount() {
        int selectedRow = materialsTable.getSelectedRow();
        if (selectedRow == -1) {
            UIHelper.showWarning(this, "Будь ласка, виберіть деталь для оновлення.");
            return;
        }

        int modelRow = materialsTable.convertRowIndexToModel(selectedRow);
        int materialId = (int) materialsTableModel.getValueAt(modelRow, 0);
        String materialName = (String) materialsTableModel.getValueAt(modelRow, 1);
        int currentCount = (int) materialsTableModel.getValueAt(modelRow, 2);

        String input = JOptionPane.showInputDialog(this,
                "Поточна кількість: " + currentCount + "\nВведіть кількість для додавання до \"" + materialName + "\":",
                0);

        if (input == null) return;

        try {
            int addedCount = Integer.parseInt(input);
            if (addedCount < 0) {
                UIHelper.showWarning(this, "Не можна додавати від'ємне значення.");
                return;
            }
            int newCount = currentCount + addedCount;

          updateMaterialInDatabase(materialId, newCount, addedCount);

        } catch (NumberFormatException e) {
            UIHelper.showError(this, "Некоректне значення. Введіть ціле число.");
        }
    }
    public void updateMaterialInDatabase(int materialId, int newCount, int addedCount){
    try (Connection connection = DataBase.getConnection()) {
        MaterialsDAO dao = new MaterialsDAO(connection);
        boolean success = dao.updateMaterialCount(materialId, newCount);
        handleUpdateResult(success, addedCount);
    }
        catch (Exception e) {
        UIHelper.showError(this, "Помилка при оновленні: " + e.getMessage());
        e.printStackTrace();
    }
    }
    private void handleUpdateResult(boolean success, int addedCount) {
        if (success) {
            UIHelper.showInfo(this, "Кількість успішно оновлена (додано " + addedCount + ").");
            loadMaterials();
        } else {
            UIHelper.showError(this, "Не вдалося оновити кількість.");
        }
    }

    public void loadMaterials() {
        try (Connection connection = DataBase.getConnection()) {
            MaterialsDAO materialsDAO = new MaterialsDAO(connection);
            List<Materials> materialsList = materialsDAO.getAllMaterials();

            materialsTableModel.setRowCount(0); // очищення
            System.out.println("Завантаження матеріалів...");
            System.out.println("DAO: " + materialsDAO);

            System.out.println("Кількість матеріалів: " + materialsList.size());

            for (Materials material : materialsList) {
                System.out.println(material.getId() + " " + material.getName());
            }

            for (Materials material : materialsList) {
                materialsTableModel.addRow(new Object[]{
                        material.getId(),
                        material.getName(),
                        material.getCount(),
                        material.getPrice()
                });
            }

        } catch (Exception e) {
            UIHelper.showError(this, "Помилка при завантаженні матеріалів: " + e.getMessage());
            e.printStackTrace();
        }
    }
}