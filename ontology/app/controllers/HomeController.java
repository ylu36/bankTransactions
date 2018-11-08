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

import drools.*;
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

        // public Result index() {
        //     System.out.println("init system...");
        //     final Bank bank = new Bank();
        //     bank.isBlacklisted = false;
        //     bank.type = "local";
        //     drools.kieSession.insert(bank);
        //     final Request request = new Request();
        //     request.category = "Weapons";
        //     request.amount = 100;
        //     request.bank = bank;
        //     drools.kieSession.insert(request);
        //     drools.kieSession.fireAllRules();
    
        //     return ok("rules are running... check the console.");
        // }
        
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
                drools.kieSession.insert(bank);
                if(bank.isBlacklisted) {
                    System.out.println("bank blacklisted");
                    break;
                }
                
                request.senderTrusted = isParticipantTrusted(senderID);
                request.receiverTrusted = isParticipantTrusted(receiverID);
                if(category.equals("Medical")) {
                    request.approved = true;
                    bank.setAverage(amount, request.senderTrusted, request.receiverTrusted);
                }
                drools.kieSession.insert(request);
                drools.kieSession.fireAllRules();
                if(!request.approved) {
                    System.out.println("Request is rejected!");
                    bank.transactionRejected ++;
                    if(bank.transactionRejected == 3) {
                        bank.isBlacklisted = true;
                        System.out.println("bank blacklisted");
                    }
                }
                else {
                    System.out.println("bank average is " + bank.averageAmount);
                    System.out.println("Request is live!");
                    bank.transactionRejected = 0;
                    addTransactionToOntology(senderID, receiverID, transactionRequestID);
                }
                break;
            }
        }
        result.put("status", "success");
        return ok(result);
    }

    public void addTransactionToOntology(String senderID, String receiverID, String transactionID) {
        Individual tx = ontReasoned.getIndividual(NS + senderID);
        Individual rx = ontReasoned.getIndividual(NS + receiverID);
        Individual transaction = ontReasoned.createIndividual(NS + transactionID, Transaction);
        transaction.addProperty(hasSender, tx);
        transaction.addProperty(hasReceiver, rx);
    }

    public Result isCommercial(String id) {
        ObjectNode result = Json.newObject();
        Individual transaction = ontReasoned.getIndividual(NS + id);
        String flag = (transaction.hasOntClass(CommercialTransaction)) ? "true" : "false";
        result.put("result", flag);
        return ok(result);
    }

    public Result isPersonal(String id) {
        ObjectNode result = Json.newObject();
        Individual transaction = ontReasoned.getIndividual(NS + id);
        String flag = (transaction.hasOntClass(PersonalTransaction)) ? "true" : "false";
        result.put("result", flag);
        return ok(result);
    }

    public Result isPurchase(String id) {
        ObjectNode result = Json.newObject();
        Individual transaction = ontReasoned.getIndividual(NS + id);
        String flag = (transaction.hasOntClass(PurchaseTransaction)) ? "true" : "false";
        result.put("result", flag);
        return ok(result);
    }

    public Result isRefund(String id) {
        ObjectNode result = Json.newObject();
        Individual transaction = ontReasoned.getIndividual(NS + id);
        String flag = (transaction.hasOntClass(RefundTransaction)) ? "true" : "false";
        result.put("result", flag);
        return ok(result);
    }

    public Result isTrusted(String id) {
        ObjectNode result = Json.newObject();
        Individual merchant = ontReasoned.getIndividual(NS + id);
        if(merchant.hasOntClass(Trusted)) {
            String flag = (merchant.hasOntClass(Trusted))? "true" : "false";
            result.put("result", flag);
        }
        else {
            result.put("result", "not a merchant");
        }
        return ok(result);
    }
    
    public boolean isParticipantTrusted(String id) {
        boolean flag = false;
        Individual merchant = ontReasoned.getIndividual(NS + id);
        if(merchant.hasOntClass(Trusted) && (merchant.hasOntClass(Trusted) == true))
                flag = true;
        
        return flag;
    }

    public Result reset() {
        ObjectNode result = Json.newObject();
        this.ontReasoned = init();
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
}
