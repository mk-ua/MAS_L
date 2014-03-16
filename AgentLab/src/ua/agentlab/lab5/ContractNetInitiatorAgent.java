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

import jade.core.Agent;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;
import jade.domain.FIPANames;

import java.util.Date;
import java.util.Vector;
import java.util.Enumeration;

/**
   This example shows how to implement the initiator role in 
   a FIPA-contract-net interaction protocol. In this case in particular 
   we use a <code>ContractNetInitiator</code>  
   to assign a dummy task to the agent that provides the best offer
   among a set of agents (whose local
   names must be specified as arguments).
   @author Giovanni Caire - TILAB
 */
public class ContractNetInitiatorAgent extends Agent {
	private int nResponders;
	
	protected void setup() { 
  	// Read names of responders as arguments
  	Object[] args = getArguments();
  	if (args != null && args.length > 1) {
  		final String car = args[0].toString();
  		nResponders = args.length - 1;
  		
  		System.out.println("Buying car '" + car + "' in " + nResponders + " auto shops.");
  		System.out.println("Auto shpos:");
  		
  		ACLMessage initMessage = new ACLMessage(ACLMessage.CFP);
  		for (int i = 1; i < args.length; ++i) {
  			System.out.println("'" + args[i] + "'");
  			initMessage.addReceiver(new AID((String) args[i], AID.ISLOCALNAME));
  		}
  		System.out.println("--------------------------------------------");
		initMessage.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
		// We want to receive a reply in 10 secs
		initMessage.setReplyByDate(new Date(System.currentTimeMillis() + 10000));
		initMessage.setContent("buy-car: " + car);
		
		addBehaviour(new ContractNetInitiator(this, initMessage) {
			
			protected void handlePropose(ACLMessage propose, Vector v) {
				System.out.println("Auto shop "+propose.getSender().getLocalName()+" proposed " + car + " for " +propose.getContent() + "$");
			}
			
			protected void handleRefuse(ACLMessage refuse) {
				System.out.println("Auto shop "+refuse.getSender().getLocalName()+" doesn't have car: " + car);
			}
			
			protected void handleFailure(ACLMessage failure) {
				if (failure.getSender().equals(myAgent.getAMS())) {
					System.out.println("Auto shop does not exist anymore");
				}
				else {
					System.out.println("Auto shop "+failure.getSender().getLocalName()+" failed");
				}
				nResponders--;
			}
			
			protected void handleAllResponses(Vector responses, Vector acceptances) {
				if (responses.size() < nResponders) {
					System.out.println("Timeout expired: missing "+(nResponders - responses.size())+" auto shops");
				}
				// Evaluate proposals.
				int bestPrice = Integer.MAX_VALUE;
				AID bestProposer = null;
				ACLMessage accept = null;
				Enumeration e = responses.elements();
				while (e.hasMoreElements()) {
					ACLMessage msg = (ACLMessage) e.nextElement();
					if (msg.getPerformative() == ACLMessage.PROPOSE) {
						ACLMessage reply = msg.createReply();
						reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
						reply.setContent("Price is too high!");
						acceptances.addElement(reply);
						int proposal = Integer.parseInt(msg.getContent());
						if (proposal < bestPrice) {
							bestPrice = proposal;
							bestProposer = msg.getSender();
							accept = reply;
						}
					}
				}
				// Accept the proposal of the best proposer
				if (accept != null) {
					System.out.println("Accepting proposal "+bestPrice+"$ from auto shop '"+bestProposer.getLocalName() + "'");
					accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
					accept.setContent("You propose the best price!");
				} else {
					System.out.println("Couldn't by car '" + car + "' because any auto shop has this car");
				}
			}
			
			protected void handleInform(ACLMessage inform) {
				System.out.println("Auto shop '"+inform.getSender().getLocalName()+"' successfully sold car '" + car);
			}
		});
  	}
  	else {
  		System.out.println("No auto shops specified.");
  	}
  } 
}

