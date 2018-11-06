package drools;

public class Bank {
    public String type, id;
    public boolean isBlacklisted;
    public int averageAmout;
    public Bank(String Id, String type) {
        this.id = id;       
        this.isBlacklisted = false;
        this.type = type;
        this.averageAmout = 0;
    }
}