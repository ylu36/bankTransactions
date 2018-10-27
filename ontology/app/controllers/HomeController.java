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
 
public class HomeController extends Controller {
    String source_file = "../csc750.owl"; 
    String source_url = "http://www.semanticweb.org/james/ontologies/2018/9/csc750.owl";
     
//     String source_file = "../pizza.owl"; // This is your file on the disk
// String source_url = "http://www.co-ode.org/ontologies/pizza/pizza.owl"; // Remember that IRI from before?
    String NS = source_url + "#";
    OntModel ontReasoned;
    OntClass Merchant, Consumer, Transaction, CommercialTransaction, RefundTransaction, PersonalTransaction, PurchaseTransaction;
    OntProperty hasReceiver, hasSender;
    @Inject
        public HomeController() {
            System.out.println("init system...");
            this.ontReasoned = init();
            this.Merchant = ontReasoned.getOntClass(NS + "Merchant");
            this.Consumer = ontReasoned.getOntClass(NS + "Consumer");
            this.Transaction = ontReasoned.getOntClass(NS + "Transaction");
            this.PurchaseTransaction = ontReasoned.getOntClass(NS + "Purchase_transaction");
            this.PersonalTransaction = ontReasoned.getOntClass(NS + "Personal_transaction");
            this.RefundTransaction = ontReasoned.getOntClass(NS + "Refund_transaction");
            this.CommercialTransaction = ontReasoned.getOntClass(NS + "Commercial_transaction");
            this.hasSender = ontReasoned.getObjectProperty(NS + "hasSender");
            this.hasReceiver = ontReasoned.getObjectProperty(NS + "hasReceiver");
        }

        public OntModel init() {            
            // Read the ontology. No reasoner yet.
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
        
            // baseOntology.setNsPrefix( "csc750", NS ); // Just for compact printing; doesn't really matter
        
            // This will create an ontology that has a reasoner attached.
            // This means that it will automatically infer classes an individual belongs to, according to restrictions, etc.
            OntModel ontReasoned = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC, baseOntology);
            return ontReasoned;
        }

    // public Result index() {
    //     // Get the classes we need
    //     OntModel ontReasoned = init();
    //     OntClass pizza1 = ontReasoned.getOntClass(NS + "Pizza");
    //     OntClass deepPanBase = ontReasoned.getOntClass(NS + "DeepPanqwreBase");
    //     OntClass cheese1 = ontReasoned.getOntClass(NS + "CheeseTopping");
    //     OntClass cheesyPizza = ontReasoned.getOntClass(NS + "CheeseyPizza");
    // //    System.out.println("hahahahahah "+pizza1);
    //     // Get the properties we need
    //     OntProperty hasTopping = ontReasoned.getObjectProperty(NS + "hasTopping");
    //     OntProperty hasBase = ontReasoned.getObjectProperty(NS + "hasBase");

    //     // Create the individuals we need. We need a pizza and a topping.
    //     Individual pizza = ontReasoned.createIndividual(NS + "mre", pizza1);
    //     // System.out.println(pizza);
    //     Individual cheese = ontReasoned.createIndividual(NS + "cheese1", cheese1);

    //     // Also need a base, but it already exists in our ontology
    //     Individual dpb = ontReasoned.getIndividual(NS + "DeepPanBase");

    //     // Now add the properties
    //     pizza.addProperty(hasBase, dpb);
    //     pizza.addProperty(hasTopping, cheese);

    //     // Now, we expect the reasoner to have figured out that our pizza also belongs to the class "CheesyPizza"
    //     // because it has a cheese topping. (The CheesyPizza class was defined in terms of a restriction which needs some cheese topping).
    //     System.out.println("pizza is cheesy:" + pizza.hasOntClass(cheesyPizza));  // should print true
    //     return ok(views.html.index.render());
    // }
    
    public Result index() {
        String id = "Merchant3";
        Individual merchant = ontReasoned.createIndividual(NS + id, Merchant);
        System.out.println("uri is " + merchant);
        String uri = merchant.getURI();
        Individual dpb = ontReasoned.getIndividual(NS + id);
        System.out.println(dpb);
        return ok(views.html.index.render());
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

    public Result addTransaction(String senderID, String receiverID, String transactionID) {
        ObjectNode result = Json.newObject();
        Individual tx = ontReasoned.getIndividual(NS + senderID);
        Individual rx = ontReasoned.getIndividual(NS + receiverID);
        Individual transaction = ontReasoned.createIndividual(NS + transactionID, Transaction);
        transaction.addProperty(hasSender, tx);
        transaction.addProperty(hasReceiver, rx);
        String RDF = ontReasoned.getNsPrefixURI("rdf");
        Property type = ontReasoned.getProperty(RDF, "type");
        System.out.println("tx type = " + tx.getPropertyResourceValue(type).toString());
        System.out.println("rx type = " + rx.getPropertyResourceValue(type).toString());
        result.put("status", "success");
        return ok(result);
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
        // if() {

        // }
        // else {
        //     result.put("result", "not a merchant");
        // }
        return ok(result);
    }
    
    public Result reset() {
        ObjectNode result = Json.newObject();
        
        result.put("status", "success");
        return ok(result);
    }
}
