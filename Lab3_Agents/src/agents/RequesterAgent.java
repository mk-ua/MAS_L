package agents;
import jade.lang.acl.ACLMessage;
import jade.core.*;
import jade.core.behaviours.*;
import jade.domain.FIPANames;
import jade.proto.SimpleAchieveREInitiator;
import jade.content.lang.Codec;
import jade.content.lang.sl.*;
import jade.content.abs.*;
import jade.content.onto.*;
import jade.content.onto.basic.*;
import employment.*;
import java.io.*;

public class RequesterAgent extends Agent {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	class HandleEngagementBehaviour extends SequentialBehaviour {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		Behaviour queryBehaviour = null;
		Behaviour requestBehaviour = null;
		public HandleEngagementBehaviour(Agent myAgent){
			super(myAgent);
		}
		public void onStart(){
			try{
				BufferedReader buff = new BufferedReader(new InputStreamReader(System.in));
				Person p = new Person();
				Address a = new Address();
				System.out.println("ENTER details of person to engage");
				System.out.print("  	Person name      --> ");			
				p.setName(buff.readLine());
				System.out.print("  	Person age       --> ");			
				p.setAge(new Long(buff.readLine()));
				System.out.print("  	Person experience--> ");	
				p.setExperience(new Long(buff.readLine()));
				System.out.println("  	Person address");
				System.out.print("    		Street ---> ");
				a.setStreet(buff.readLine());
				System.out.print("    		Number ---> ");
				a.setNumber(new Long(buff.readLine()));
				System.out.print("   		City   ---> ");
				a.setCity(buff.readLine());
				p.setAddress(a);
				// Create an object representing the fact that person p works for company c
				WorksFor wf = new WorksFor();
				wf.setPerson(p);
				wf.setCompany(((RequesterAgent) myAgent).c);
				@SuppressWarnings("unused")
				Ontology o = myAgent.getContentManager().lookupOntology(EmploymentOntology.NAME);		
				// Create an ACL message to query the engager agent if the above fact is true or false
				ACLMessage queryMsg = new ACLMessage(ACLMessage.QUERY_IF);
				queryMsg.addReceiver(((RequesterAgent) myAgent).engager);
				queryMsg.setLanguage(FIPANames.ContentLanguage.FIPA_SL0);
				queryMsg.setOntology(EmploymentOntology.NAME);
    			// Write the works for predicate in the :content slot of the message
		    	try {
		    		myAgent.getContentManager().fillContent(queryMsg, wf);
		    	} catch (Exception e) {
		    		e.printStackTrace();
		    	}
		    // Create and add a behaviour to query the engager agent whether
				// person p already works for company c following a FIPAQeury protocol
				queryBehaviour = new CheckAlreadyWorkingBehaviour(myAgent, queryMsg);
				addSubBehaviour(queryBehaviour);
			}
			catch (IOException ioe) { 
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
	/**
		This behaviour embeds the check that the indicated person is not
		already working for the indicated company.
		This is done following a FIPA-Query interaction protocol
	*/
	class CheckAlreadyWorkingBehaviour extends SimpleAchieveREInitiator {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		// Constructor
		public CheckAlreadyWorkingBehaviour(Agent myAgent, ACLMessage queryMsg){
			super(myAgent, queryMsg);
			queryMsg.setProtocol(FIPANames.InteractionProtocol.FIPA_QUERY);
		}
		
		protected void handleInform(ACLMessage msg) {
			try{
				AbsPredicate cs = (AbsPredicate)myAgent.getContentManager().extractAbsContent(msg);
				Ontology o = myAgent.getContentManager().lookupOntology(EmploymentOntology.NAME);
				if (cs.getTypeName().equals(EmploymentOntology.WORKS_FOR)) {
					// The indicated person is already working for company c. 
					// Inform the user
					WorksFor wf = (WorksFor)o.toObject((AbsObject)cs);
					Person p = (Person) wf.getPerson();
					Company c = (Company) wf.getCompany();
					System.out.println("Person " + p.getName() + " is already working for " + c.getName());
				}
				else if (cs.getTypeName().equals(SLVocabulary.NOT)){
					// The indicated person is NOT already working for company c.
					// Get person and company details and create an object representing the engagement action
					WorksFor wf = (WorksFor)o.toObject(cs.getAbsObject(SLVocabulary.NOT_WHAT));
					Person p = (Person) wf.getPerson();
					Company c = (Company) wf.getCompany();
					Engage e = new Engage();
					e.setPerson(p);
					e.setCompany(c);
					Action a = new Action();
					a.setActor(((RequesterAgent) myAgent).engager);
					a.setAction(e);
			
					// Create an ACL message to request the above action
					ACLMessage requestMsg = new ACLMessage(ACLMessage.REQUEST);
					requestMsg.addReceiver(((RequesterAgent) myAgent).engager);
					requestMsg.setLanguage(FIPANames.ContentLanguage.FIPA_SL0);
					requestMsg.setOntology(EmploymentOntology.NAME);
    			// Write the action in the :content slot of the message
		    		
		    		try {
		    			myAgent.getContentManager().fillContent(requestMsg, a);
					} catch (Exception pe) {
					}
					// Create and add a behaviour to request the engager agent to engage
					// person p in company c following a FIPARequest protocol
					((HandleEngagementBehaviour) parent).requestBehaviour = new RequestEngagementBehaviour(myAgent, requestMsg);
					((SequentialBehaviour) parent).addSubBehaviour(((HandleEngagementBehaviour) parent).requestBehaviour);
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
	/**
		This behaviour embeds the request to engage the indicated person 
		in the indicated company.
		This is done following a FIPA-Request interaction protocol
	*/
	class RequestEngagementBehaviour extends SimpleAchieveREInitiator {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		// Constructor
		public RequestEngagementBehaviour(Agent myAgent, ACLMessage requestMsg){
			super(myAgent, requestMsg);
			requestMsg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
		}
		protected void handleAgree(ACLMessage msg) {
			System.out.println("Engagement agreed. Waiting for completion notification...");
		}
		protected void handleInform(ACLMessage msg) {
			System.out.println("Engagement successfully completed");	
		}
		protected void handleNotUnderstood(ACLMessage msg) {
			System.out.println("Engagement request not understood by engager agent");			
		}
		protected void handleFailure(ACLMessage msg) {
			System.out.println("Engagement failed");
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
			System.out.println("Engagement refused");
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
	AID engager;
	Company c;   				// The  company where people will be engaged
	protected void setup() {
		// Register the codec for the SL0 language
		getContentManager().registerLanguage(new SLCodec(), FIPANames.ContentLanguage.FIPA_SL0);	
		getContentManager().registerOntology(EmploymentOntology.getInstance());
		try {
			BufferedReader buff = new BufferedReader(new InputStreamReader(System.in));
			System.out.print("ENTER the local name of the Engager agent --> ");
			String name = buff.readLine();
			engager = new AID(name, AID.ISLOCALNAME);
			c  = new Company();
			Address a = new Address();
			System.out.println("ENTER details of the company where people will be engaged");
			System.out.print("  Company name --> ");			
			c.setName(buff.readLine());
			System.out.println("  Company address");
			System.out.print("    Street ------> ");
			a.setStreet(buff.readLine());
			System.out.print("    Number ------> ");
			a.setNumber(new Long(buff.readLine()));
			System.out.print("    City   ------> ");
			a.setCity(buff.readLine());
			c.setAddress(a);
		}
		catch (IOException ioe) { 
			System.err.println("I/O error: " + ioe.getMessage()); 
		}
		// Create and add the main behaviour of this agent
  	addBehaviour(new HandleEngagementBehaviour(this));
	}
}

