package controllers;

import play.mvc.*;
import play.libs.Json;
import openllet.jena.PelletReasonerFactory;

import org.apache.jena.ontology.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileManager;
import org.apache.jena.reasoner.*;
import org.apache.jena.shared.JenaException;
import javax.inject.Inject;
import javax.inject.Singleton;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.*;
import java.io.*;
 
public class HomeController extends Controller {
    String source_file = "../p3.owl"; 
    String source_url = "http://semanticweb.org/james/ontologies/2018/9/csc750.owl"; 
//     String source_file = "../pizza.owl"; // This is your file on the disk
// String source_url = "http://www.co-ode.org/ontologies/pizza/pizza.owl"; // Remember that IRI from before?
    String NS = source_url + "#";
    OntModel ontReasoned;
    OntClass Merchant, Consumer, CommercialTransaction;

    @Inject
        public HomeController() {
            System.out.println("init system...");
            this.ontReasoned = init();
            this.Merchant = ontReasoned.getOntClass(NS + "Merchant");
            
            this.Consumer = ontReasoned.getOntClass(NS + "Consumer");
            System.out.println(Merchant);
            System.out.println(Consumer);
            // this.CommercialTransaction = ontReasoned.getOntClass(NS + "CommercialTransaction");
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
        Individual dpb = ontReasoned.getIndividual("http://semanticweb.org/james/ontologies/2018/9/csc750.owl#Merchant3");
        System.out.println(dpb);
        return ok(views.html.index.render());
    }
    public Result addMerchant(int id) {
        ObjectNode result = Json.newObject();
        Individual merchant = ontReasoned.createIndividual(NS + "Merchant" + id, Merchant);
        System.out.println("uri is " + merchant);
        String uri = merchant.getURI();
        Individual dpb = ontReasoned.getIndividual(NS + "Merchant" + id);
        System.out.println(dpb);
        result.put("status", "success");
        return ok(result);
    }

    public Result addConsumer(int id) {
        ObjectNode result = Json.newObject();
        Individual consumer = ontReasoned.createIndividual(NS + "Consumer" + id, Consumer);
        // System.out.println("uri is " + consumer.getURI());
        result.put("status", "success");
        return ok(result);
    }

    public Result addTransaction(int senderID, int receiverID, int transactionID) {
        ObjectNode result = Json.newObject();
        
        result.put("status", "success");
        return ok(result);
    }

    public Result isCommercial(int id) {
        ObjectNode result = Json.newObject();
        Individual transaction = ontReasoned.getIndividual(NS + "Transaction" + id);
        String flag = (transaction.hasOntClass(CommercialTransaction)) ? "true" : "false";
        result.put("status", flag);
        return ok(result);
    }
}
