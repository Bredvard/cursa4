package STO.GUI;

import javax.swing.*;
import java.awt.Component;

public class UIHelper {

    public static void showError(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Помилка", JOptionPane.ERROR_MESSAGE);
    }

    public static void showWarning(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Увага", JOptionPane.WARNING_MESSAGE);
    }

    public static void showInfo(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Інформація", JOptionPane.INFORMATION_MESSAGE);
    }
}
