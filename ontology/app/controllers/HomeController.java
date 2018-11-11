package controllers;

import play.mvc.*;
import play.libs.Json;
import openllet.jena.PelletReasonerFactory;

import org.apache.jena.ontology.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.util.FileManager;
import org.apache.jena.reasoner.*;
import org.apache.jena.shared.JenaException;
import javax.inject.Inject;
import javax.inject.Singleton;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.*;
import java.io.*;
import plugins.Drools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import drools.*;
import java.io.IOException; 

public class HomeController extends Controller {
    String source_file = "../csc750.owl"; 
    String source_url = "http://www.semanticweb.org/james/ontologies/2018/9/csc750.owl";
    String NS = source_url + "#";
    OntModel ontReasoned;
    OntClass Merchant, Trusted, Consumer, Transaction, CommercialTransaction, RefundTransaction, PersonalTransaction, PurchaseTransaction;
    OntProperty hasReceiver, hasSender;
    Set<Bank> banks; 
    @Inject
    Drools drools;
    final static Logger logger = LoggerFactory.getLogger(HomeController.class);
    @Inject
    public HomeController() {
        System.out.println("init system...");
        this.ontReasoned = init();
        this.Merchant = ontReasoned.getOntClass(NS + "Merchant");
        this.Trusted = ontReasoned.getOntClass(NS + "Trusted");
        this.Consumer = ontReasoned.getOntClass(NS + "Consumer");
        this.Transaction = ontReasoned.getOntClass(NS + "Transaction");
        this.PurchaseTransaction = ontReasoned.getOntClass(NS + "Purchase_transaction");
        this.PersonalTransaction = ontReasoned.getOntClass(NS + "Personal_transaction");
        this.RefundTransaction = ontReasoned.getOntClass(NS + "Refund_transaction");
        this.CommercialTransaction = ontReasoned.getOntClass(NS + "Commercial_transaction");
        this.hasSender = ontReasoned.getObjectProperty(NS + "hasSender");
        this.hasReceiver = ontReasoned.getObjectProperty(NS + "hasReceiver");
        this.banks = new HashSet<>();
    }

    public OntModel init() {   
        OntModel baseOntology = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
        try
        {
            InputStream in = FileManager.get().open(source_file);
            try
            {
                baseOntology.read(in, null);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        catch (JenaException je)
        {
            System.err.println("ERROR" + je.getMessage());
            je.printStackTrace();
            System.exit(0);
        }
        OntModel ontReasoned = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC, baseOntology);
        return ontReasoned;
    }

    
    public Result addMerchant(String id) {
        ObjectNode result = Json.newObject();
        Individual merchant = ontReasoned.createIndividual(NS + id, Merchant);
        System.out.println("merchant uri is " + merchant);
        result.put("status", "success");
        return ok(result);
    }

    public Result addConsumer(String id) {
        ObjectNode result = Json.newObject();
        Individual consumer = ontReasoned.createIndividual(NS + id, Consumer);
        System.out.println("consumer uri is " + consumer);
        result.put("status", "success");
        return ok(result);
    }

    public Result addTransaction(String senderID, String receiverID, String bankID, String category, int amount, String transactionRequestID) {
        ObjectNode result = Json.newObject();
        Request request = new Request(senderID, receiverID, bankID, category, amount, transactionRequestID);
        // check if the bank from list 'banks' with bankID is blacklisted
        for(Bank bank: banks) {
            if(bank.id.equals(bankID)) {
                System.out.println(bank.averageAmount + " " + bank.frequency);

                drools.kieSession.insert(bank);
                if(bank.isBlacklisted) {
                    System.out.println("bank blacklisted");
                    break;
                }
                
                request.senderTrusted = isParticipantTrusted(senderID);
                request.receiverTrusted = isParticipantTrusted(receiverID);
                if(bank.averageAmount > 0 && (amount > 10 * bank.averageAmount )) {
                    BufferedWriter output = null;
                    String message = "REJECT BECAUSE OF RULE 5: " + request.transactionRequestID + '\t' + request.bankID + '\t' + request.senderID + '\t' + request.receiverID
                        + '\t' + request.amount + '\t' + request.category + '\t' + request.timestamp;
                    try {
                        File file = new File("fail.log");
                        output = new BufferedWriter(new FileWriter(file, true));
                        output.write(message + '\n');
                    } catch ( IOException e ) {
                        e.printStackTrace();
                    } finally {
                        if ( output != null ) {
                            try {output.close();} catch (Exception ex) {/*ignore*/}
                        }
                    }
                    result.put("status", "failure");
                    result.put("reason", 5);
                    break;
                }
                if((double)bank.trustedInstance / bank.frequency < 0.25) {
                    BufferedWriter output = null;
                    String message = "REJECT BECAUSE OF RULE 6: " + request.transactionRequestID + '\t' + request.bankID + '\t' + request.senderID + '\t' + request.receiverID
                        + '\t' + request.amount + '\t' + request.category + '\t' + request.timestamp;
                    try {
                        File file = new File("fail.log");
                        output = new BufferedWriter(new FileWriter(file, true));
                        output.write(message + '\n');
                    } catch ( IOException e ) {
                        e.printStackTrace();
                    } finally {
                        if ( output != null ) {
                            try {output.close();} catch (Exception ex) {/*ignore*/}
                        }
                    }
                    result.put("status", "failure");
                    result.put("reason", 6);
                    break;
                }

                drools.kieSession.insert(request);
                drools.kieSession.fireAllRules();
                if(request.statusCode > 0) {
                    System.out.println("Request is rejected! " );
                    bank.transactionRejected ++;
                    if(bank.transactionRejected == 3) {
                        bank.isBlacklisted = true;
                        System.out.println("bank blacklisted");
                    }
                    result.put("status", "failure");
                    result.put("reason", request.statusCode);
                }
                else {
                    bank.setAverage(amount, request.senderTrusted, request.receiverTrusted);
                    if(!category.equals("medical") && !category.equals("weapons")) {
                        BufferedWriter output = null;
                        String message = "ACCEPT: " + request.transactionRequestID + '\t' + request.bankID + '\t' + request.senderID + '\t' + request.receiverID
                            + '\t' + request.amount + '\t' + request.category + '\t' + request.timestamp;
                        try {
                            File file = new File("success.log");
                            output = new BufferedWriter(new FileWriter(file, true));
                            output.write(message + '\n');
                        } catch ( IOException e ) {
                            e.printStackTrace();
                        } finally {
                            if ( output != null ) {
                                try {output.close();} catch (Exception ex) {/*ignore*/}
                            }
                        }
                    }
                    // System.out.println("bank average is " + bank.averageAmount);
                    // System.out.println("Request is live!");
                    // bank.transactionRejected = 0;
                    addTransactionToOntology(senderID, receiverID, transactionRequestID);
                    result.put("status", "success");
                }
                break;
            }
        }
        return ok(result);
    }

    public void addTransactionToOntology(String senderID, String receiverID, String transactionID) {
        Individual tx = ontReasoned.getIndividual(NS + senderID);
        Individual rx = ontReasoned.getIndividual(NS + receiverID);
        Individual transaction = ontReasoned.createIndividual(NS + transactionID, Transaction);
        transaction.addProperty(hasSender, tx);
        transaction.addProperty(hasReceiver, rx);
        System.out.println("Request is live! " + tx.hasOntClass(Trusted) + rx.hasOntClass(Trusted));
    }

    public Result isCommercial(String id) {
        ObjectNode result = Json.newObject();
        Individual transaction = ontReasoned.getIndividual(NS + id);
        if(transaction != null) {
            String flag = (transaction.hasOntClass(CommercialTransaction)) ? "true" : "false";
            result.put("status", "success");
            result.put("result", flag);
        }
        else {
            result.put("status", "failure");
            result.put("reason", "not a transaction");
        }
        return ok(result);
    }

    public Result isPersonal(String id) {
        ObjectNode result = Json.newObject();
        Individual transaction = ontReasoned.getIndividual(NS + id);
        if(transaction != null) {
            String flag = (transaction.hasOntClass(PersonalTransaction)) ? "true" : "false";
            result.put("status", "success");
            result.put("result", flag);
        }
        else {
            result.put("status", "failure");
            result.put("reason", "not a transaction");
        }
        return ok(result);
    }

    public Result isPurchase(String id) {
        ObjectNode result = Json.newObject();
        Individual transaction = ontReasoned.getIndividual(NS + id);
        if(transaction != null) {
            String flag = (transaction.hasOntClass(PurchaseTransaction)) ? "true" : "false";
            result.put("status", "success");
            result.put("result", flag);
        }
        else {
            result.put("status", "failure");
            result.put("reason", "not a transaction");
        }
        return ok(result);
    }

    public Result isRefund(String id) {
        ObjectNode result = Json.newObject();
        Individual transaction = ontReasoned.getIndividual(NS + id);
        if(transaction != null) {
            String flag = (transaction.hasOntClass(RefundTransaction)) ? "true" : "false";
            result.put("status", "success");
            result.put("result", flag);
        }
        else {
            result.put("status", "failure");
            result.put("reason", "not a transaction");
        }
        return ok(result);
    }

    public Result isTrusted(String id) {
        ObjectNode result = Json.newObject();
        Individual merchant = ontReasoned.getIndividual(NS + id);
        if(merchant != null) {
            String flag = (merchant.hasOntClass(Trusted))? "true" : "false";
            result.put("result", flag);
        }
        else {
            result.put("status", "failure");
            result.put("reason", "not a merchant");
        }
        return ok(result);
    }
    
    public boolean isParticipantTrusted(String id) {
        boolean flag = false;
        Individual merchant = ontReasoned.getIndividual(NS + id);
        if(merchant != null && (merchant.hasOntClass(Trusted) == true))
                flag = true;
        
        return flag;
    }

    public Result reset() {
        ObjectNode result = Json.newObject();
        this.ontReasoned = init();
        this.banks = new HashSet<>();
        new File("./success.log").delete();
        new File("./failure.log").delete();
        result.put("status", "success");
        return ok(result);
    }

    public Result addBank(String type, String id) {
        ObjectNode result = Json.newObject();
        banks.add(new Bank(id, type));
        System.out.println("banks have " + banks.size() + "with id=" + id);
        result.put("status", "success");
        return ok(result);
    }

    public Result isBankBlacklisted(String bankID) {
        ObjectNode result = Json.newObject();
        boolean found = false;
        for(Bank bank: banks) {
            if(bank.id.equals(bankID)) {
                boolean flag = bank.isBlacklisted;
                result.put("status", "success");
                result.put("result", flag);
                found = true;
            }
        }
        if(!found) {            
            result.put("status", "failure");
            result.put("reason", "not a bank");
        }
        return ok(result);
    }

    public Result countBankRejections(String bankID) {
        ObjectNode result = Json.newObject();
        boolean found = false;
        int num = 0;
        for(Bank bank: banks) {
            if(bank.id.equals(bankID)) {
                num = bank.transactionRejected;
                result.put("status", "success");
                result.put("rejections", num);
                found = true;
            }
        }
        if(!found) {            
            result.put("status", "failure");
            result.put("reason", "not a bank");
        }
        return ok(result);
    }
}
