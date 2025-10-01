package STO;

public class Client {
    private int id;
    private String name;
    private String surname;
    private String email;
    private String phone;

    public Client(String name, String surname, String email, String phone) {
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.phone = phone;
    }

    public Client(int id, String name, String surname, String email, String phone) {
        this(name, surname, email, phone);
        this.id = id;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getSurname() { return surname; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
}