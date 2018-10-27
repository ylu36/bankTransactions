# Transaction Ontology

## Background
This project uses [Protege](https://protege.stanford.edu/products.php#desktop-protege), an Ontology editor, for GUI-based Ontology developement. Back-end RESTful APIs are developed with `sbt` using Play framework, a lightweight web framework built with Java and Scala. 
## How to Run

### Play Framework
* Install latest version of sbt: [SBT](http://www.scala-sbt.org/download.html)
* Clone this repository: `git clone https://github.com/ylu36/transactionOntology.git`
* Go to `transactionOntology` folder: `cd transactionOntology`
* Type `sbt run` to run the application.
* For stopping the server, Press the `Enter` key on keyboard.
* For opening the sbt console, type `sbt` from the command prompt.
* There are following APIs present in the web application:
# RESTful APIs 
POST    /addconsumer/:id  
POST    /addmerchant/:id       
POST    /addtransaction/:senderID/:receiverID/:transactionID    
GET     /iscommercial/:transactionID  
GET     /ispersonal/:transactionID                
GET     /ispurchase/:transactionID                            
GET     /isrefund/:transactionID                 
GET     /istrusted/:merchantID   
POST    /reset   

**Note: This project is created in Linux environment; Endpoints tested with [Postman](https://www.getpostman.com/).
