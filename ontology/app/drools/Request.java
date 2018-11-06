package drools;
import java.util.Date;
import java.text.SimpleDateFormat;
public class Request {
    public Bank bank;
    public int amount;
    public String senderID, receiverID, bankID;
    public boolean senderTrusted, receiverTrusted;
    public String category, transactionRequestID;
    public String timestamp; 
    
    public Request(String senderID, String receiverID, String bankID, String category, int amount, String transactionRequestID) {
        this.senderID = senderID;
        this.receiverID = receiverID;
        this.bankID = bankID;
        this.senderTrusted = true;
        this.receiverTrusted = true;
        this.category = category;
        this.amount = amount;
        this.transactionRequestID = transactionRequestID;
        this.timestamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
    }

}