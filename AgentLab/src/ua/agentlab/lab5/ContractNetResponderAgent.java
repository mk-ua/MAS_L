/**
 * ***************************************************************
 * JADE - Java Agent DEvelopment Framework is a framework to develop
 * multi-agent systems in compliance with the FIPA specifications.
 * Copyright (C) 2000 CSELT S.p.A.
 * 
 * GNU Lesser General Public License
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation,
 * version 2.1 of the License.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 * **************************************************************
 */
package ua.agentlab.lab5;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.FailureException;

/**
   This example shows how to implement the responder role in 
   a FIPA-contract-net interaction protocol. In this case in particular 
   we use a <code>ContractNetResponder</code>  
   to participate into a negotiation where an initiator needs to assign
   a task to an agent among a set of candidates.
   @author Giovanni Caire - TILAB
 */
public class ContractNetResponderAgent extends Agent {
	private static final String[] allCars = new String[]{"VW", "BMW", "Honda", "Ford", "Nissan", "Audi"};
	
	private final Map<String, Integer> cars = new HashMap<String, Integer>();
	private final Random rand = new Random();

	protected void setup() {
		System.out.println("Auto shop "+getLocalName()+" waiting for CFP request...");
		for (String c : allCars) {
			if (rand.nextBoolean()) {
				cars.put(c, 1000 + rand.nextInt(1001));
				System.out.println("Car '" + c + "', price = " + cars.get(c) + "$");
			}
		}
		System.out.println("--------------------------------------------");
		MessageTemplate template = MessageTemplate.and(
				MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
				MessageTemplate.MatchPerformative(ACLMessage.CFP) );

		addBehaviour(new ContractNetResponder(this, template) {
			@Override
			protected ACLMessage handleCfp(ACLMessage cfp) throws NotUnderstoodException, RefuseException {
				System.out.println("Auto shop "+getLocalName()+": CFP received from "+cfp.getSender().getLocalName()+". Action is "+cfp.getContent());
				String car = cfp.getContent().substring("buy-car: ".length());
				Integer price = cars.get(car);
				if (price != null) {
					// We provide a proposal
					System.out.println("Auto shop "+getLocalName()+": proposing car '" + car + "' for " + price + "$");
					ACLMessage propose = cfp.createReply();
					propose.setPerformative(ACLMessage.PROPOSE);
					propose.setContent(price.toString());
					return propose;
				}
				else {
					// We refuse to provide a proposal
					System.out.println("Auto shop "+getLocalName()+": refuse because we don't have car '" + car + "'");
					throw new RefuseException("evaluation-failed");
				}
			}

			@Override
			protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose,ACLMessage accept) throws FailureException {
				System.out.println("Auto shop "+getLocalName()+": Proposal accepted");
				if (performAction()) {
					System.out.println("Auto shop "+getLocalName()+": car buying successfully performed: " + accept.getContent());
					ACLMessage inform = accept.createReply();
					inform.setPerformative(ACLMessage.INFORM);
					return inform;
				}
				else {
					System.out.println("Auto shop "+getLocalName()+": buying failed because of disaster");
					throw new FailureException("disaster");
				}	
			}

			protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
				System.out.println("Auto shop "+getLocalName()+": proposal for bying car rejected. Reason: " + reject.getContent());
			}
		} );
	}

	private boolean performAction() {
		// Simulate action execution by generating a random number
		return rand.nextInt(10) > 6;
	}
}

