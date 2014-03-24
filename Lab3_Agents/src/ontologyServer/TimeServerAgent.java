package ontologyServer;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import jade.content.lang.sl.SLCodec;
import jade.core.Agent;
import jade.core.behaviours.OntologyServer;
import jade.lang.acl.ACLMessage;


public class TimeServerAgent extends Agent {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private long time0;
	private long lastUpdateTime;
	private DateFormat formatter = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
	
	protected void setup() {
		// Initialize time0 with the current time
		updateCurrentTime(new Date());
			
		// Register language and ontology 
		getContentManager().registerLanguage(new SLCodec());
		getContentManager().registerOntology(TimeOntology.getInstance());
		
		// Add the behaviour serving REQUESTs to perform actions in the TimeOntology
		// Serving methods are exposed by the agent itself (4th parameter)
		addBehaviour(new OntologyServer(this, TimeOntology.getInstance(), ACLMessage.REQUEST, this));
	}

	// This is invoked when a REQUEST to perform the GetTime action is received
	public void serveGetTimeRequest(GetTime gt, ACLMessage request) {
		Date currentTime = computeCurrentTime();
		ACLMessage reply = request.createReply();
		reply.setPerformative(ACLMessage.INFORM);
		reply.setContent(formatter.format(currentTime));
		send(reply);
	}
	
	// This is invoked when a REQUEST to perform the SetTime action is received
	public void serveSetTimeRequest(SetTime st, ACLMessage request) {
		updateCurrentTime(st.getTime());
		ACLMessage reply = request.createReply();
		reply.setPerformative(ACLMessage.INFORM);
		send(reply);
	}

	private Date computeCurrentTime() {
		long elapsedTimeSinceLastUpdate = System.currentTimeMillis() - lastUpdateTime;
		return new Date(time0 + elapsedTimeSinceLastUpdate);
	}
	
	private void updateCurrentTime(Date d) {
		time0 = d.getTime();
		lastUpdateTime = System.currentTimeMillis();
		System.out.println("Agent "+getLocalName()+" - Current time set to "+formatter.format(d));
	}
}

