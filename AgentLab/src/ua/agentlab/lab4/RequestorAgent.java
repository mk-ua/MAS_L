package ua.agentlab.lab4;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.domain.AMSService;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class RequestorAgent extends Agent {
	private AMSAgentDescription[] agents = null;
	
	public void setup() {
		System.out.println("Started : " + getAID().getName());
		
		try {
			SearchConstraints c = new SearchConstraints();
			c.setMaxResults(new Long(-1));
			agents = AMSService.search(this, new AMSAgentDescription(), c);
		} catch (Exception e) {
			System.out.println("Problem searching AMS: " + e);
			e.printStackTrace();
			doDelete();
			return;
		}
		
		addBehaviour(new CyclicBehaviour(this) {
			public void action() {
				ACLMessage msg = receive(MessageTemplate.MatchPerformative(ACLMessage.INFORM));
				if (msg != null) {
					System.out.println(getClass().getName() + " – " + myAgent.getLocalName() + " request result: " + msg.getContent());
				}
				block();
			}
		});
		
		SequentialBehaviour seqBehaviour = new SequentialBehaviour(this);
		seqBehaviour.addSubBehaviour(new OneShotBehaviour() {			
			@Override
			public void action() {
				sendRequestMessage("first_name");
			}
		});
		seqBehaviour.addSubBehaviour(new OneShotBehaviour() {			
			@Override
			public void action() {
				sendRequestMessage("last_name");
			}
		});
		seqBehaviour.addSubBehaviour(new OneShotBehaviour() {			
			@Override
			public void action() {
				sendRequestMessage("age");
			}
		});
		seqBehaviour.addSubBehaviour(new OneShotBehaviour() {			
			@Override
			public void action() {
				sendRequestMessage("address");
			}
		});
		seqBehaviour.addSubBehaviour(new OneShotBehaviour() {			
			@Override
			public void action() {
				sendRequestMessage("school");
			}
		});
		
		addBehaviour(seqBehaviour);
	}
	
	private void sendRequestMessage(String request) {
		for (AMSAgentDescription agent : agents) {
			AID agentID = agent.getName();
			ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
			msg.addReceiver(agentID);
			msg.setLanguage("English");
			msg.setContent(request);
			send(msg);
		}
	}
}
