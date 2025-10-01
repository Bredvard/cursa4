package STO.GUI;

import javax.swing.*;
import java.awt.*;

public class MainMenu extends JPanel {
    private ScheduleMenu scheduleMenu = new ScheduleMenu();
    private CommissionMenu commissionMenu = new CommissionMenu(scheduleMenu);
    private MaterialsMenu materialsMenu = new MaterialsMenu();
    private DeliveryMenu deliveryMenu = new DeliveryMenu(materialsMenu);
    private JPanel cardPanel;
    private JButton scheduleButton;
    private JButton commissionButton;
    private JButton deliverybutton;
    private JButton materialsbutton;

    public MainMenu() {
        setLayout(new BorderLayout());

        // Навігаційна панель
        JPanel navPanel = new JPanel();

        navPanel.add(commissionButton);
        navPanel.add(scheduleButton);
        navPanel.add(deliverybutton);
        navPanel.add(materialsbutton);
        add(navPanel, BorderLayout.NORTH);

        // Панель карток

        cardPanel.add(commissionMenu, "commission");
        cardPanel.add(scheduleMenu, "schedule");
        cardPanel.add(materialsMenu, "materials");
        cardPanel.add(deliveryMenu, "delivery");
        add(cardPanel, BorderLayout.CENTER);

        // Дії кнопок
        commissionButton.addActionListener(e -> switchCard("commission"));
        scheduleButton.addActionListener(e -> switchCard("schedule"));
       deliverybutton.addActionListener(e -> switchCard("delivery"));
        materialsbutton.addActionListener(e -> switchCard("materials"));
    }

    private void switchCard(String name) {
        CardLayout cl = (CardLayout) cardPanel.getLayout();
        cl.show(cardPanel, name);
    }
}
