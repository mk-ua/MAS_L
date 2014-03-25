/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A. 

GNU Lesser General Public License

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation, 
version 2.1 of the License. 

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the
Free Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA  02111-1307, USA.
*****************************************************************/

package ua.agentlab3;

import jade.content.abs.AbsContentElementList;
import jade.content.abs.AbsObject;
import jade.content.abs.AbsPredicate;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.lang.sl.SLVocabulary;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.proto.SimpleAchieveREInitiator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import ua.agentlab3.employment.Address;
import ua.agentlab3.employment.BookedTicketFrom;
import ua.agentlab3.employment.Booking;
import ua.agentlab3.employment.Company;
import ua.agentlab3.employment.BookingOntology;
import ua.agentlab3.employment.Person;

public class BuyerAgent extends Agent {
	private static final long serialVersionUID = 11313L;


	private Person readPerson() {
		try {
			BufferedReader buff = new BufferedReader(new InputStreamReader(System.in));
			Person p = new Person();
			Address a = new Address();
			System.out.print("  Person name --> ");			
			p.setName(buff.readLine());
			System.out.print("  Person age ---> ");			
			p.setAge(new Long(buff.readLine()));
			System.out.println("  Person address");
			System.out.print("    Street -----> ");
			a.setStreet(buff.readLine());
			System.out.print("    Number -----> ");
			a.setNumber(new Long(buff.readLine()));
			System.out.print("    City   -----> ");
			a.setCity(buff.readLine());
			p.setAddress(a);
			return p;
		} catch (Exception e) {}
		return null;
	}
	
	class HandleBookingBehaviour extends SequentialBehaviour {

		private static final long serialVersionUID = -8730637833291670469L;
		// Local variables
		Behaviour queryBehaviour = null;
		Behaviour requestBehaviour = null;
		
		// Constructor
		public HandleBookingBehaviour(Agent myAgent){
			super(myAgent);
		}
		
		// This is executed at the beginning of the behaviour
		public void onStart(){
			// Get detail of person to be engaged
			try{
				System.out.println("ENTER details of person which is booking ticket");
				Person p = readPerson();
				
				// Create an object representing the fact that person p works for company c
				BookedTicketFrom bookedTicket = new BookedTicketFrom();
				bookedTicket.setBookedTicketOwner(p);
				bookedTicket.setSellingCompany(((BuyerAgent) myAgent).company);
				
				//Ontology o = myAgent.getContentManager().lookupOntology(EmploymentOntology.NAME);		
				// Create an ACL message to query the engager agent if the above fact is true or false
				ACLMessage queryMsg = new ACLMessage(ACLMessage.QUERY_IF);
				queryMsg.addReceiver(((BuyerAgent) myAgent).seller);
				queryMsg.setLanguage(FIPANames.ContentLanguage.FIPA_SL0);
				queryMsg.setOntology(BookingOntology.NAME);
    			// Write the works for predicate in the :content slot of the message
		    
		    	try {
		    		myAgent.getContentManager().fillContent(queryMsg, bookedTicket);
		    	} catch (Exception e) {
		    		e.printStackTrace();
		    	}
				
		    // Create and add a behaviour to query the engager agent whether
				// person p already works for company c following a FIPAQeury protocol
				queryBehaviour = new CheckAlreadyWorkingBehaviour(myAgent, queryMsg);
				addSubBehaviour(queryBehaviour);
			}
			catch (Exception ioe) { 
				System.err.println("I/O error: " + ioe.getMessage()); 
			}
			
		}
		
		// This is executed at the end of the behaviour
		public int onEnd(){
			// Check whether the user wants to continue
			try{
				BufferedReader buff = new BufferedReader(new InputStreamReader(System.in));
				System.out.println("Would you like to continue?[y/n] ");
				String stop = buff.readLine();
				if (stop.equalsIgnoreCase("y"))
					{
					    reset(); // This makes this behaviour be cyclically executed
					    myAgent.addBehaviour(this);
					}
				else
				    myAgent.doDelete(); // Exit
			}
			catch (IOException ioe) { 
				System.err.println("I/O error: " + ioe.getMessage()); 
			}
			return 0;
		}
		
		// Extends the reset method in order to remove the sub-behaviours that
		// are dynamically added 
		public void reset(){
			if (queryBehaviour != null){
				removeSubBehaviour(queryBehaviour);
				queryBehaviour = null;
			}
			if (requestBehaviour != null){
				removeSubBehaviour(requestBehaviour);
				requestBehaviour = null;
			}
			super.reset();
		}
	}
	
	
	class CheckAlreadyWorkingBehaviour extends SimpleAchieveREInitiator {

		private static final long serialVersionUID = -4519995890326151728L;

		// Constructor
		public CheckAlreadyWorkingBehaviour(Agent myAgent, ACLMessage queryMsg){
			super(myAgent, queryMsg);
			queryMsg.setProtocol(FIPANames.InteractionProtocol.FIPA_QUERY);
		}
		
		protected void handleInform(ACLMessage msg) {
			try{
				AbsPredicate cs = (AbsPredicate)myAgent.getContentManager().extractAbsContent(msg);
				Ontology o = myAgent.getContentManager().lookupOntology(BookingOntology.NAME);
				if (cs.getTypeName().equals(BookingOntology.BOOKED_TICKET_FROM)) {
					// The indicated person is already working for company c. 
					// Inform the user
					BookedTicketFrom wf = (BookedTicketFrom)o.toObject((AbsObject)cs);
					Person p = (Person) wf.getBookedTicketOwner();
					Company c = (Company) wf.getSellingCompany();
					System.out.println("Person " + p.getName() + " is already working for " + c.getName());
				}
				else if (cs.getTypeName().equals(SLVocabulary.NOT)){
					// The indicated person is NOT already working for company c.
					// Get person and company details and create an object representing the Booking action
					BookedTicketFrom wf = (BookedTicketFrom)o.toObject(cs.getAbsObject(SLVocabulary.NOT_WHAT));
					Person p = (Person) wf.getBookedTicketOwner();
					Company c = (Company) wf.getSellingCompany();
					Booking booking = new Booking();
					booking.setPerson(p);
					booking.setCompany(c);
					Action action = new Action();
					action.setActor(((BuyerAgent) myAgent).seller);
					action.setAction(booking);
			
					// Create an ACL message to request the above action
					ACLMessage requestMsg = new ACLMessage(ACLMessage.REQUEST);
					requestMsg.addReceiver(((BuyerAgent) myAgent).seller);
					requestMsg.setLanguage(FIPANames.ContentLanguage.FIPA_SL0);
					requestMsg.setOntology(BookingOntology.NAME);
    			// Write the action in the :content slot of the message
		    		
		    		try {
		    			myAgent.getContentManager().fillContent(requestMsg, action);
					} catch (Exception pe) {
					}
					// Create and add a behaviour to request the engager agent to engage
					// person p in company c following a FIPARequest protocol
					((HandleBookingBehaviour) parent).requestBehaviour = new RequestBookingBehaviour(myAgent, requestMsg);
					((SequentialBehaviour) parent).addSubBehaviour(((HandleBookingBehaviour) parent).requestBehaviour);
				}
				else{
					// Unexpected response received from the engager agent.
					// Inform the user
					System.out.println("Unexpected response from engager agent");
				}
				
			} // End of try
			catch (Codec.CodecException fe) {
				System.err.println("FIPAException in fill/extract Msgcontent:" + fe.getMessage());
			}
			catch (OntologyException fe) {
				System.err.println("OntologyException in getRoleName:" + fe.getMessage());
			}
		}
		
	}
			
			
	class RequestBookingBehaviour extends SimpleAchieveREInitiator {

		private static final long serialVersionUID = -5671225281456674249L;
		// Constructor
		public RequestBookingBehaviour(Agent myAgent, ACLMessage requestMsg){
			super(myAgent, requestMsg);
			requestMsg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
		}

		protected void handleAgree(ACLMessage msg) {
			System.out.println("Booking agreed. Waiting for completion notification...");
		}
		protected void handleInform(ACLMessage msg) {
			System.out.println("Booking successfully completed");	
		}
		protected void handleNotUnderstood(ACLMessage msg) {
			System.out.println("Booking request not understood by engager agent");			
		}
		protected void handleFailure(ACLMessage msg) {
			System.out.println("Booking failed");
			// Get the failure reason and communicate it to the user
			try{
				AbsPredicate absPred =(AbsPredicate)myAgent.getContentManager().extractContent(msg);
				
				System.out.println("The reason is: " + absPred.getTypeName());
			}
			catch (Codec.CodecException fe){
				System.err.println("FIPAException reading failure reason: " + fe.getMessage());
			}
			catch (OntologyException oe){
				System.err.println("OntologyException reading failure reason: " + oe.getMessage());
			}
		}
		protected void handleRefuse(ACLMessage msg) {
			System.out.println("Booking refused");
			// Get the refusal reason and communicate it to the user
			try{
				AbsContentElementList list =(AbsContentElementList)myAgent.getContentManager().extractAbsContent(msg);
				AbsPredicate absPred = (AbsPredicate)list.get(1);
				System.out.println("The reason is: " + absPred.getTypeName());
			}
			catch (Codec.CodecException fe){
				System.err.println("FIPAException reading refusal reason: " + fe.getMessage());
			}
			catch (OntologyException oe){
				System.err.println("OntologyException reading refusal reason: " + oe.getMessage());
			}
		}
	}		
		

	// AGENT LOCAL VARIABLES
	AID seller; // AID of the agent the Booking requests will have to be sent to
	Company company;   // The  company where people will be engaged
	
	
	// AGENT SETUP
	protected void setup() {
		
		// Register the codec for the SL0 language
		getContentManager().registerLanguage(new SLCodec(), FIPANames.ContentLanguage.FIPA_SL0);	
		
		// Register the ontology used by this application
		getContentManager().registerOntology(BookingOntology.getInstance());
	
		// Get from the user the name of the agent the Booking requests
		// will have to be sent to
		try {
			BufferedReader buff = new BufferedReader(new InputStreamReader(System.in));
			
			System.out.print("ENTER the local name of the Booking Company agent --> ");
			String name = buff.readLine();
			seller = new AID(name, AID.ISLOCALNAME);
		
			// Get from the user the details of the company where people will 
			// be engaged
			company  = new Company();
			Address address = new Address();
			System.out.println("ENTER details of the company where people will book ticket");
			System.out.print("  Company name --> ");			
			company.setName(buff.readLine());
			System.out.println("  Company address");
			System.out.print("    Street ------> ");
			address.setStreet(buff.readLine());
			System.out.print("    Number ------> ");
			address.setNumber(new Long(buff.readLine()));
			System.out.print("    City   ------> ");
			address.setCity(buff.readLine());
			System.out.println("    Concert Name ------>");
			company.setConcertName(buff.readLine());
			company.setAddress(address);
		}
		catch (IOException ioe) { 
			System.err.println("I/O error: " + ioe.getMessage()); 
		}
		
		// Create and add the main behaviour of this agent
  	addBehaviour(new HandleBookingBehaviour(this));
	}
}
