package STO;
import java.sql.*;

public class RepairParticipation {
    private int idCommission;
    private Date commissionStart;
    private String status;
    private int idRepair;
    private Date commissionEnd;  // рядок з іменами майстрів через кому
    private double price; // Додане поле
    public RepairParticipation(int idCommission, Date commissionStart,Date commissionEnd , int idRepair, String status, double price) {
        this.idCommission = idCommission;
        this.commissionStart = commissionStart;
        this.status = status;
        this.idRepair = idRepair;
        this.commissionEnd = commissionEnd;
        this.price = price;
    }

    // геттери
    public int getIdCommission() { return idCommission; }
    public Date getCommissionStart() { return commissionStart; }
    public String getStatus() { return status; }
    public int getidRepair() { return idRepair; }
    public Date getCommissionEnd() { return commissionEnd; }


    @Override
    public String toString() {
        return "Замовлення #" + idCommission + " (Авто: " + idRepair + ")";
    }
}
