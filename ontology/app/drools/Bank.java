package drools;

public class Bank {
    public String type, id;
    public boolean isBlacklisted;
    public int averageAmount, frequency;
    public Bank(String id, String type) {
        this.id = id;       
        this.isBlacklisted = false;
        this.type = type;
        this.averageAmount = 0;
        this.frequency = 0;
    }
    public void setAverage(int amount) {
        int total = this.frequency * this.averageAmount + amount;
        this.frequency ++;
        this.averageAmount = total / this.frequency;
    }
}