package STO;

import java.sql.Date;

public class Commission {
    private int idCommission;
    private Date commissionStart;
    private String status;
    private int idCar;

    // Конструктор без id (для створення нового запису)
    public Commission(int idCar) {
        this.idCar = idCar;
        this.status = "В обробці"; // статус за замовчуванням
        this.commissionStart = new Date(System.currentTimeMillis()); // поточна дата
    }

    // Конструктор із усіма полями (наприклад, для отримання з БД)
    public Commission(int idCommission, Date commissionStart, String status, int idCar) {
        this.idCommission = idCommission;
        this.commissionStart = commissionStart;
        this.status = status;
        this.idCar = idCar;
    }

    // Геттери і сеттери
    public int getIdCommission() {
        return idCommission;
    }

    public void setIdCommission(int idCommission) {
        this.idCommission = idCommission;
    }

    public Date getCommissionStart() {
        return commissionStart;
    }

    public void setCommissionStart(Date commissionStart) {
        this.commissionStart = commissionStart;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getIdCar() {
        return idCar;
    }

    public void setIdCar(int idCar) {
        this.idCar = idCar;
    }

    @Override
    public String toString() {
        return
                "Номер замовлення: " + idCommission +
                ", Дата початку=" + commissionStart +
                ", статус='" + status + '\'';
    }
}
