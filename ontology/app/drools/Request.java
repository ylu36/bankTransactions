package drools;
import java.util.Date;
import java.text.SimpleDateFormat;
public class Request {
    public Bank bank;
    public int amount;
    public String senderID, receiverID, bankID;
    public boolean senderTrusted, receiverTrusted, approved;
    public String category, transactionRequestID;
    public String timestamp; 
    
    public Request(String senderID, String receiverID, String bankID, String category, int amount, String transactionRequestID) {
        this.senderID = senderID;
        this.receiverID = receiverID;
        this.bankID = bankID;
        this.senderTrusted = false;
        this.receiverTrusted = false;
        this.category = category;
        this.amount = amount;
        this.approved = false;
        this.transactionRequestID = transactionRequestID;
        this.timestamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
    }

    public void setApproval(boolean f) {
        this.approved = f;
    }
}