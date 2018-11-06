package drools;

public class Bank {
    public String nationality, id;
    public boolean isBlacklisted;
    public int averageAmout;
    public Bank(String Id, String nationality) {
        this.id = id;       
        this.nationality = nationality;
        this.averageAmout = 0;
    }
}