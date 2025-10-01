package STO.GUI;

import STO.*;
import STO.DAO.CarDAO;
import STO.DAO.ClientDao;
import STO.DAO.CommissionDAO;
import STO.DAO.ProblemDAO;
import java.util.List;
import java.util.ArrayList;
import java.sql.*;
import javax.swing.*;
import java.awt.*;

public class CommissionMenu extends JPanel {

    private JLabel markLabel;
    private JTextField nameclientfield;
    private JTextField phonenumberclientfield;
    private JButton createCommissionButton;
    private JTextField surnameclientfield;
    private JTextField autonumberfield;
    private JTextField clientmailfield;
    private JPanel contentPane;
    private JComboBox<String> problemComboBox;
    private JButton addProblemButton;
    private JList<String> list1;
    private JButton DeleteProblemButton;
    private JLabel price;
    private JComboBox<String> carcombobox;
    private DefaultListModel<String> problemsListModel;
    private ScheduleMenu scheduleMenu;

    public CommissionMenu(ScheduleMenu scheduleMenu) {
        this.scheduleMenu = scheduleMenu;
        initializeUI();
        setupEventListeners();
        loadInitialData();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        add(contentPane, BorderLayout.CENTER);
        problemsListModel = new DefaultListModel<>();
        list1.setModel(problemsListModel);
    }

    private void setupEventListeners() {
        addProblemButton.addActionListener(e -> addSelectedProblem());
        DeleteProblemButton.addActionListener(e -> removeSelectedProblem());
        createCommissionButton.addActionListener(e -> addCommissionToDatabase());
    }

    private void loadInitialData() {
        loadSupportedCarMarks();
        loadRepairTypes();
    }

    private void addSelectedProblem() {
        String selectedProblem = (String) problemComboBox.getSelectedItem();
        if (selectedProblem != null && !problemsListModel.contains(selectedProblem)) {
            problemsListModel.addElement(selectedProblem);
            updateTotalPrice();
        }
    }

    private void removeSelectedProblem() {
        int selectedIndex = list1.getSelectedIndex();
        if (selectedIndex != -1) {
            problemsListModel.remove(selectedIndex);
            updateTotalPrice();
        }
    }

    private void loadRepairTypes() {
        try {
            problemComboBox.removeAllItems();
            String query = "SELECT repairname FROM typeofrepair";

            try (PreparedStatement stmt = DataBase.connection.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    problemComboBox.addItem(rs.getString("repairname"));
                }
            }
        } catch (SQLException e) {
            UIHelper.showError(this, "Помилка при завантаженні типів ремонту: " + e.getMessage());
        }
    }

    private void loadSupportedCarMarks() {
        try {
            carcombobox.removeAllItems();
            for (String mark : CarDAO.getSupportedMarks()) {
                carcombobox.addItem(mark);
            }
        } catch (SQLException e) {
            UIHelper.showError(this, "Помилка при завантаженні марок авто: " + e.getMessage());
        }
    }

    private void addCommissionToDatabase() {
        if (!validateInput()) {
            return;
        }

        try {
            Client client = createClientFromInput();
            int clientId = saveClient(client);
            if (clientId == -1) return;

            Car car = createCarFromInput(clientId);
            int carId = saveCar(car);
            if (carId == -1) return;

            int commissionId = createCommission(carId);
            if (commissionId == -1) return;

            boolean problemsSaved = saveProblems(commissionId);
            if (!problemsSaved) return;

            Date finishDate = CommissionDAO.calculateAndSaveCommissionEndDate(commissionId);
            handleSuccess(finishDate);

        } catch (SQLException ex) {
            handleDatabaseError(ex);
        }
    }

    private boolean validateInput() {
        String clientName = nameclientfield.getText();
        String clientSurname = surnameclientfield.getText();
        String clientPhone = phonenumberclientfield.getText();
        String carNumber = autonumberfield.getText();
        String carMark = (String) carcombobox.getSelectedItem();

        if (clientName.isEmpty() || clientSurname.isEmpty() || clientPhone.isEmpty() ||
                carNumber.isEmpty() || carMark.isEmpty() || problemsListModel.isEmpty()) {

            UIHelper.showError(this, "Будь ласка, заповніть всі обов'язкові поля та додайте принаймні одну проблему!");
            return false;
        }

        try {
            if (!CarDAO.isSupportedMark(carMark)) {
                UIHelper.showError(this, "СТО не обслуговує марку: " + carMark);
                return false;
            }
        } catch (SQLException e) {
            UIHelper.showError(this, "Помилка при перевірці марки авто: " + e.getMessage());
            return false;
        }

        return true;
    }

    private Client createClientFromInput() {
        return new Client(
                nameclientfield.getText(),
                surnameclientfield.getText(),
                clientmailfield.getText(),
                phonenumberclientfield.getText()
        );
    }

    private Car createCarFromInput(int clientId) {
        return new Car(
                autonumberfield.getText(),
                "",
                (String) carcombobox.getSelectedItem(),
                clientId
        );
    }

    private int saveClient(Client client) {
        try {
            int clientId = ClientDao.insertClient(client);
            if (clientId == -1) {
                UIHelper.showError(this, "Помилка при додаванні клієнта");
            }
            return clientId;
        } catch (SQLException e) {
            UIHelper.showError(this, "Помилка при збереженні клієнта: " + e.getMessage());
            return -1;
        }
    }

    private int saveCar(Car car) {
        try {
            int carId = CarDAO.insertCar(car);
            if (carId == -1) {
                UIHelper.showError(this, "Помилка при додаванні автомобіля");
            }
            return carId;
        } catch (SQLException e) {
            UIHelper.showError(this, "Помилка при збереженні автомобіля: " + e.getMessage());
            return -1;
        }
    }

    private int createCommission(int carId) {
            int commissionId = CommissionDAO.insertCommission(new Commission(carId));
            if (commissionId == -1) {
                UIHelper.showError(this, "Помилка при додаванні замовлення");
            }
            return commissionId;

    }

    private boolean saveProblems(int commissionId) {
        try {
            List<String> problems = getSelectedProblems();
            boolean success = ProblemDAO.insertProblemsForCommission(commissionId, problems);

            if (!success) {
                UIHelper.showError(this, "Одна або більше проблем не знайдені у базі даних.");
            }
            return success;
        } catch (SQLException e) {
            UIHelper.showError(this, "Помилка при збереженні проблем: " + e.getMessage());
            return false;
        }
    }

    private List<String> getSelectedProblems() {
        List<String> problems = new ArrayList<>();
        for (int i = 0; i < problemsListModel.size(); i++) {
            problems.add(problemsListModel.get(i));
        }
        return problems;
    }

    private void handleSuccess(Date finishDate) {
        UIHelper.showInfo(this, "Замовлення успішно створено! Дата завершення замовлення: " + finishDate);
        clearFields();
        scheduleMenu.loadCommissionsToComboBox();
    }

    private void handleDatabaseError(SQLException ex) {
        ex.printStackTrace();
        UIHelper.showError(this, "Помилка при додаванні даних: " + ex.getMessage());
    }

    private void clearFields() {
        nameclientfield.setText("");
        surnameclientfield.setText("");
        phonenumberclientfield.setText("");
        clientmailfield.setText("");
        autonumberfield.setText("");
        problemsListModel.clear();
        updateTotalPrice();
    }

    private void updateTotalPrice() {
        try {
            double total = calculateTotalPrice();
            price.setText(String.format("Загальна ціна: %.2f грн", total));
        } catch (SQLException e) {
            UIHelper.showError(this, "Помилка при розрахунку ціни: " + e.getMessage());
            price.setText("Загальна ціна: помилка розрахунку");
        }
    }

    private double calculateTotalPrice() throws SQLException {
        double total = 0.0;
        String query = "SELECT price FROM typeofrepair WHERE repairname = ?";

        for (int i = 0; i < problemsListModel.size(); i++) {
            String problemName = problemsListModel.get(i);

            try (PreparedStatement stmt = DataBase.connection.prepareStatement(query)) {
                stmt.setString(1, problemName);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        total += rs.getDouble("price");
                    }
                }
            }
        }
        return total;
    }
}