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

import jade.content.ContentElementList;
import jade.content.Predicate;
import jade.content.abs.AbsPredicate;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.lang.sl.SLVocabulary;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.content.onto.basic.Done;
import jade.content.onto.basic.TrueProposition;
import jade.core.Agent;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.SimpleAchieveREResponder;
import jade.util.leap.ArrayList;
import jade.util.leap.Iterator;
import jade.util.leap.List;
import ua.agentlab3.employment.Address;
import ua.agentlab3.employment.BookedTicketFrom;
import ua.agentlab3.employment.Booking;
import ua.agentlab3.employment.BookingError;
import ua.agentlab3.employment.BookingOntology;
import ua.agentlab3.employment.Company;
import ua.agentlab3.employment.Person;
import ua.agentlab3.employment.PersonToYoung;

public class SellerAgent extends Agent {

	private static final long serialVersionUID = -277699612109771523L;

	class HandleEnganementQueriesBehaviour extends SimpleAchieveREResponder {

		private static final long serialVersionUID = -8831029499177766647L;

		public HandleEnganementQueriesBehaviour(Agent myAgent) {
			super(myAgent, MessageTemplate.and(MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_QUERY), MessageTemplate.MatchOntology(BookingOntology.NAME)));
		}

		public ACLMessage prepareResponse(ACLMessage msg) {

			ACLMessage reply = msg.createReply();

			// The QUERY message could be a QUERY-REF. In this case reply
			// with NOT_UNDERSTOOD

			if (msg.getPerformative() != ACLMessage.QUERY_IF) {
				reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
				String content = "(" + msg.toString() + ")";
				reply.setContent(content);
				return (reply);
			}

			try {
				// Get the predicate for which the truth is queried
				Predicate pred = (Predicate) myAgent.getContentManager().extractContent(msg);
				if (!(pred instanceof BookedTicketFrom)) {
					// If the predicate for which the truth is queried is not
					// WORKS_FOR
					// reply with NOT_UNDERSTOOD
					reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
					String content = "(" + msg.toString() + ")";
					reply.setContent(content);
					return (reply);
				}

				// Reply
				reply.setPerformative(ACLMessage.INFORM);
				BookedTicketFrom wf = (BookedTicketFrom) pred;
				Person p = wf.getBookedTicketOwner();
				Company c = wf.getSellingCompany();
				if (((SellerAgent) myAgent).isWorking(p, c))
					reply.setContent(msg.getContent());
				else {
					// Create an object representing the fact that the WORKS_FOR
					// predicate is NOT true.
					Ontology o = getContentManager().lookupOntology(BookingOntology.NAME);
					AbsPredicate not = new AbsPredicate(SLVocabulary.NOT);
					not.set(SLVocabulary.NOT_WHAT, o.fromObject(wf));
					myAgent.getContentManager().fillContent(reply, not);
				}
			} catch (Codec.CodecException fe) {
				System.err.println(myAgent.getLocalName() + " Fill/extract content unsucceeded. Reason:" + fe.getMessage());
			} catch (OntologyException oe) {
				System.err.println(myAgent.getLocalName() + " getRoleName() unsucceeded. Reason:" + oe.getMessage());
			}

			return (reply);

		} // END of handleQueryMessage() method

	} // END of HandleEnganementQueriesBehaviour

	/**
	 * This behaviour handles a single engagement action that has been requested
	 * following the FIPA-Request protocol
	 */
	class HandleEngageBehaviour extends SimpleAchieveREResponder {

		private static final long serialVersionUID = -2304815331552749577L;

		public HandleEngageBehaviour(Agent myAgent) {
			super(myAgent, MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST));
		}


		public ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) {
			// Prepare a dummy ACLMessage used to create the content of all
			// reply messages
			ACLMessage msg = request.createReply();

			try {
				// Get the requested action
				Action a = (Action) myAgent.getContentManager().extractContent(request);
				Booking e = (Booking) a.getAction();
				Person p = e.getPerson();
				Company c = e.getCompany();

				// Check person's age. If < 35 --> AGREE, else REFUSE and exit
				// Perform the engagement action
				int result = ((SellerAgent) myAgent).doBooking(p, c);

				// Reply according to the result
				if (result > 0) {
					// OK --> INFORM action done
					Done d = new Done();
					d.setAction(a);
					myAgent.getContentManager().fillContent(msg, d);
					msg.setPerformative(ACLMessage.INFORM);
				} else {
					// NOT OK --> FAILURE
					ContentElementList l = new ContentElementList();
					l.add(a);
					l.add(new BookingError());
					myAgent.getContentManager().fillContent(msg, l);
					msg.setPerformative(ACLMessage.FAILURE);
				}

			} catch (Exception fe) {
				System.out.println(myAgent.getName() + ": Error handling the booking action.");
				System.out.println(fe.getMessage());
			}

			// System.out.println(msg);
			return msg;
		}

		public ACLMessage prepareResponse(ACLMessage request) {
			// Prepare a dummy ACLMessage used to create the content of all
			// reply messages
			ACLMessage temp = request.createReply();

			try {
				// Get the requested action.
				Action action = (Action) getContentManager().extractContent(request);
				Booking booking = (Booking) action.getAction();
				Person client = booking.getPerson();

				// Check person's age. If > 18 --> AGREE, else REFUSE and exit
				if (client.getAge().intValue() > 18) {
					// AGREE to accomplish the engagement action without any
					// special condition.
					ContentElementList l = new ContentElementList();
					l.add(action);
					l.add(new TrueProposition());
					getContentManager().fillContent(temp, l);
					temp.setPerformative(ACLMessage.AGREE);
				} else {
					ContentElementList list = new ContentElementList();
					list.add(action);
					list.add(new PersonToYoung());
					getContentManager().fillContent(temp, list);
					temp.setPerformative(ACLMessage.REFUSE);
				}

			} catch (Exception fe) {
				fe.printStackTrace();
				System.out.println(getName() + ": Error handling the booking action.");
				System.out.println(fe.getMessage());
			}

			return temp;
		}
	}

	// AGENT LOCAL VARIABLES
	private Company representedCompany; // The company on behalf of which this
										// agent is able to engage people
	private List clients; // The people currently working for the company

	// AGENT CONSTRUCTOR
	public SellerAgent() {
		super();

		representedCompany = new Company();
		representedCompany.setName("My");
		Address a = new Address();
		a.setStreet("Baker");
		a.setNumber(new Long(7));
		a.setCity("London");
		representedCompany.setAddress(a);
		representedCompany.setConcertName("Rolling Stones");

		clients = new ArrayList();
	}

	// AGENT SETUP
	protected void setup() {
		System.out.println("This is the agent representing the company:\n" + representedCompany);

		// Register the codec for the SL0 language
		getContentManager().registerLanguage(new SLCodec(), FIPANames.ContentLanguage.FIPA_SL0);

		// Register the ontology used by this application
		getContentManager().registerOntology(BookingOntology.getInstance());

		// Create and add the behaviour for handling QUERIES using the
		// booking-ontology
		addBehaviour(new HandleEnganementQueriesBehaviour(this));

		// Create and add the behaviour for handling REQUESTS using the
		// booking-ontology
		addBehaviour(new HandleEngageBehaviour(this));
	}

	// AGENT METHODS
	boolean isWorking(Person p, Company c) {
		if (!c.equals(representedCompany)) {
			return false;
		}

		boolean isAnEmployee = false;
		Iterator i = clients.iterator();
		while (i.hasNext()) {
			Person empl = (Person) i.next();
			if (p.equals(empl))
				isAnEmployee = true;
		}

		return isAnEmployee;
	}

	int doBooking(Person p, Company c) {
		if (!c.equals(representedCompany))
			return (-1); //Can't book in another company
		else
			clients.add(p);
		return (1);
	}
}
