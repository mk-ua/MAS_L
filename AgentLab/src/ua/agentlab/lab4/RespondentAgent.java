package ua.agentlab.lab4;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class RespondentAgent extends Agent {
	protected void setup() {
		System.out.println("Started : " + getAID().getName());
		addBehaviour(new CyclicBehaviour(this) {
			private static final long serialVersionUID = -1912882200351395625L;

			public void action() {
				ACLMessage msg = receive(MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
				if (msg != null) {
					String request = msg.getContent();
					System.out.println(getClass().getName() + " – " + myAgent.getLocalName() + " received request: " + request);
					
					String response = "Unknown request";
					if (request.equalsIgnoreCase("first_name")) {
						response = "Pavlo";
					} else if (request.equalsIgnoreCase("last_name")) {
						response = "Fedyna";
					} else if (request.equalsIgnoreCase("age")) {
						response = "21";
					} else if (request.equalsIgnoreCase("address")) { 
						response = "Ukraine, Lviv";
					}
					
					ACLMessage reply = msg.createReply();
					reply.setPerformative(ACLMessage.INFORM);
					reply.setContent(request + " - \'" + response + "\'");
					send(reply);
				}
				block();
			}
		});
	}
}
