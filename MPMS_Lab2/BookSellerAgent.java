package lab2.agpackage;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

import java.util.*;

public class BookSellerAgent extends Agent {
	
	/*
	 * 	Shtogryn Oleg modifications
	 *  The catalog of books for sale is a list of types "Book"
	 */
	private List<Book> catalog;
	// The GUI by means of which the user can add books in the catalog
	private BookSellerGui myGui;

	// Put agent initializations here
	protected void setup() {
		catalog = new ArrayList<Book>();
		// Create and show the GUI 
		myGui = new BookSellerGui(this);
		myGui.showGui();
		// Register the book-selling service in the yellow pages
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("book-selling");
		sd.setName("JADE-book-trading");
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}

		// Add the behavior serving queries from buyer agents
		addBehaviour(new OfferRequestsServer());

		// Add the behaviour serving purchase orders from buyer agents
		addBehaviour(new PurchaseOrdersServer());
	}

	// Put agent clean-up operations here
	protected void takeDown() {
		// Deregister from the yellow pages
		try {
			DFService.deregister(this);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		// Close the GUI
		myGui.dispose();
		// Printout a dismissal message
		System.out.println("Seller-agent "+getAID().getName()+" terminating.");
	}

	/*
	 * 	Shtogryn Oleg modifications
	 *  I created a method to search a book in catalog with title of book
	 */
	public Book SearchBookFromTitle(String title) {
		for(Book bookInCatalog : catalog ) {
			if (bookInCatalog.GetBookName().equals(title)) {
				return bookInCatalog;
			}
		}
		return null;
	}

	
	/*
	 * 	Shtogryn Oleg modifications
	 *  I modified a method that update catalog, so seller can add books with amount
	 */
	public void UpdateCatalog(final String title, final int price, final int amount) {
		try {
		addBehaviour(new OneShotBehaviour() {
			public void action() {
				if (SearchBookFromTitle(title)==null) {
					catalog.add(new Book(title,price,amount));
					System.out.println(title+" inserted into catalogue. Price = "+price+". Amount = "+amount);
				} 
				else {
					System.out.println("This book is already in seller's store, so i will change only amount and price!");
					SearchBookFromTitle(title).SetBooksAmount(amount);
					SearchBookFromTitle(title).SetBookPrice(amount);
				}
			}
		} );
		} catch  (Exception ex) {
			ex.printStackTrace(); 
		}
	}

	/**
	   Inner class OfferRequestsServer.
	   This is the behaviour used by Book-seller agents to serve incoming requests 
	   for offer from buyer agents.
	   If the requested book is in the local catalogue the seller agent replies 
	   with a PROPOSE message specifying the price. Otherwise a REFUSE message is
	   sent back.
	 */
	private class OfferRequestsServer extends CyclicBehaviour {
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
			ACLMessage msg = myAgent.receive(mt);

			if (msg != null) {
				// CFP Message received. Process it
				String title = msg.getContent();
				ACLMessage reply = msg.createReply();
				
				Integer price = SearchBookFromTitle(title).GetBookPrice();
				Integer amount = SearchBookFromTitle(title).GetBooksAmount();

				if ( (price != 0) && (amount!=0) ) {
					// The requested book is available for sale. Reply with the price
					reply.setPerformative(ACLMessage.PROPOSE);
					reply.setContent(String.valueOf(price.intValue()));
				}
				else {
					// The requested book is NOT available for sale.
					reply.setPerformative(ACLMessage.REFUSE);
					reply.setContent("not-available");
				}
				myAgent.send(reply);
			}
			else {
				block();
			}
		}
	}  // End of inner class OfferRequestsServer

	/**
	   Inner class PurchaseOrdersServer.
	   This is the behaviour used by Book-seller agents to serve incoming 
	   offer acceptances (i.e. purchase orders) from buyer agents.
	   The seller agent removes the purchased book from its catalogue 
	   and replies with an INFORM message to notify the buyer that the
	   purchase has been sucesfully completed.
	 */
	private class PurchaseOrdersServer extends CyclicBehaviour {
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				// ACCEPT_PROPOSAL Message received. Process it
				String title = msg.getContent();
				ACLMessage reply = msg.createReply();
				
				Integer amount = null;
				for(Book bookInCatalog : catalog ) {
					if (bookInCatalog.GetBookName().equals(title))  amount = (Integer) bookInCatalog.GetBooksAmount();
				}
				
				//Search book in catalog, and if seller have that book (more than 0) - sold it
				if (amount != 0) {
					reply.setPerformative(ACLMessage.INFORM);
					System.out.println(title+" sold to agent "+msg.getSender().getName());
					SearchBookFromTitle(title).DecreaseBookAmount();
					System.out.println(myAgent.getLocalName()+" have "+SearchBookFromTitle(title).GetBooksAmount()+
							" "+SearchBookFromTitle(title).GetBookName() );
					
				}
				else {
					// The requested book has been sold to another buyer in the meanwhile .
					reply.setPerformative(ACLMessage.FAILURE);
					reply.setContent("not-available");
				}
				myAgent.send(reply);
			}
			else {
				block();
			}
		}
	}  // End of inner class OfferRequestsServer
}

/*
 * 	Shtogryn Oleg modifications
 *  I created a class "Book". It is a container of object Book, that has name, price and amount
 */
class Book {
	private String bookName;
	private int bookPrice;
	private int booksAmount;
	private static final int maxBookPrice = 100000;
	private static final int maxBooksAmount = 500000;
	
	Book(String name, int price, int amount) {
		bookName = name;
		bookPrice = price;
		booksAmount = amount;
	}
	
	public String GetBookName() { return bookName; }
	public int GetBookPrice() { return bookPrice; }
	public int GetBooksAmount() { return booksAmount; }
	
	public void SetBookPrice(int price) {
		if ( (price>0) && (price<maxBookPrice))
			bookPrice=price; 
	}
	
	public void SetBooksAmount(int amount) { 
		if ( (amount>0) && (amount<maxBooksAmount))
			booksAmount+=amount; 
	}

	
	public void DecreaseBookAmount() { 
		booksAmount--;
	}
}