package drools;
import java.util.Date;
public class Request {
    public Bank bank;
    public int amount;
    public String sender, receiver;
    public String category;
    public String timeStamp; // = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
    // public boolean isSenderTrusted() {

    // }

    public void setSender(String s) {
        sender = s;
    }

    public void setAmount(int a) {
        amount = a;
    }
}