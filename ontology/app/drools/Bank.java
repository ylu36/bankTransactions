package drools;

public class Bank {
    public String type, id;
    public boolean isBlacklisted;
    public int averageAmount, frequency, trustedInstance, transactionRejected;
    public Bank(String id, String type) {
        this.id = id;       
        this.isBlacklisted = false;
        this.type = type;
        this.averageAmount = 0;
        this.frequency = 0;
        this.trustedInstance = 0;
        this.transactionRejected = 0;
    }
    public void setAverage(int amount, boolean t1, boolean t2) {
        int total = this.frequency * this.averageAmount + amount;
        this.frequency ++;
        this.averageAmount = total / this.frequency;
        if(t1 || t2)
            this.trustedInstance ++;
    }

    public void incrementFailure() {
        this.transactionRejected ++;
    }

    public void clearFailure() {
        this.transactionRejected = 0;
    }

    public void setBlacklist() {
        this.isBlacklisted = true;
    }
}