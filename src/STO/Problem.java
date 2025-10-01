package STO;

public class Problem {
    private int id;
    private String name;
    private double price;
    private int repairTime; // наприклад, у годинах або днях

    public Problem(int id, String name, double price, int repairTime) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.repairTime = repairTime;
    }

    // Конструктор без id (наприклад, для створення нового запису)
    public Problem(String name, double price, int repairTime) {
        this.name = name;
        this.price = price;
        this.repairTime = repairTime;
    }

    // Гетери
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public int getRepairTime() {
        return repairTime;
    }

    // Сетери
    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setRepairTime(int repairTime) {
        this.repairTime = repairTime;
    }

    @Override
    public String toString() {
        return name; // для зручності відображення в ComboBox
    }
}
