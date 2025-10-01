package STO;

public class Master {
    private int id;
    private String name;
    private String surname;
    private String fathername;
    private int workshopId;

    public Master(int id, String name, String surname, String fathername, int workshopId) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.fathername = fathername;
        this.workshopId = workshopId;
    }

    public Master(String name, String surname, String fathername, int workshopId) {
        this.name = name;
        this.surname = surname;
        this.fathername = fathername;
        this.workshopId = workshopId;
    }

    // Геттери
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public String getFathername() {
        return fathername;
    }

    public int getWorkshopId() {
        return workshopId;
    }

    public String getFullName() {
        return surname + " " + name + " " + fathername;
    }

    // Сеттери
    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public void setFathername(String fathername) {
        this.fathername = fathername;
    }

    public void setWorkshopId(int workshopId) {
        this.workshopId = workshopId;
    }

    @Override
    public String toString() {
        return surname + " " + name + " " + fathername + " (цех №" + workshopId + ")";
    }

}
