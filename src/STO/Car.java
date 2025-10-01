package STO;

public class Car {
    private int id;
    private String number;     // CarNomer
    private String model;      // CarModel
    private String mark;       // CarMark
    private int clientId;      // IdClient

    // Конструктор без id (для вставки нового авто)
    public Car(String number, String model, String mark, int clientId) {
        this.number = number;
        this.model = model;
        this.mark = mark;
        this.clientId = clientId;
    }

    // Конструктор з id (для отриманого з БД авто)
    public Car(int id, String number, String model, String mark, int clientId) {
        this.id = id;
        this.number = number;
        this.model = model;
        this.mark = mark;
        this.clientId = clientId;
    }

    // Геттери
    public int getId() { return id; }
    public String getNumber() { return number; }
    public String getModel() { return model; }
    public String getMark() { return mark; }
    public int getClientId() { return clientId; }

    // Сеттери
    public void setId(int id) { this.id = id; }
    public void setNumber(String number) { this.number = number; }
    public void setModel(String model) { this.model = model; }
    public void setMark(String mark) { this.mark = mark; }
    public void setClientId(int clientId) { this.clientId = clientId; }
}
