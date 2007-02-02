/*
 * $Id$
 *
 * Copyright (C) Orange R&D, 2006-2007
 * Copyright (C) INRIA Rhône-Alpes, 2006-2007
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */


package fr.inrialpes.exmo.align.service.jade;


import org.semanticweb.owl.align.Parameters;

import fr.inrialpes.exmo.align.impl.BasicParameters;
import fr.inrialpes.exmo.align.service.AServProtocolManager;
import fr.inrialpes.exmo.align.service.ErrorMsg;
import fr.inrialpes.exmo.align.service.Message;
import jade.core.Agent;
import jade.core.Profile;
// Identify them
import jade.core.behaviours.*;
import jade.lang.acl.*;

import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;

import jade.util.Logger;

public class JadeFIPAAServiceAgent extends Agent {

	public static final String SERVICE_NAME = "Alignment";
	public static final String SERVICE_TYPE = "Alignment-service";

	private Logger myLogger = Logger.getMyLogger(getClass().getName());
	private String myId;
	private String serverId;
	private AServProtocolManager manager;
	private int localId=0;

	protected void setup() {
		myLogger.log(Logger.INFO, getAID().getName()+" started");

		// Read arguments
		Object[] args = getArguments();
		if (args != null) {
			for (int i = 0; i < args.length; ++i) {
				myLogger.log(Logger.INFO, "Arg-"+i+" = "+args[i]);
			}

			manager=(AServProtocolManager) args[0];
		}

		myId = "LocalHTMLInterface";
		serverId = "dummy";
		localId = 0;

		// Add initial behaviours
		addBehaviour(new CyclicBehaviour(this) {
			public void action() {

				String perf; // performative
				String info; //parameters

				MessageTemplate tpl =MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
				//ACLMessage msg = myAgent.receive();
				ACLMessage msg = myAgent.receive(tpl);
				if (msg != null) {
					//System.out.println("Alignement Agent " + myAgent.getLocalName() + " receive : " + msg.getContent() + " from "+ msg.getSender());
					perf=msg.getContent().substring(0,msg.getContent().indexOf("::"));
					info = msg.getContent().substring(msg.getContent().indexOf("::")+2, msg.getContent().length());
					//myLogger.log(Logger.INFO, "Received message: "+msg);
					Parameters params = new BasicParameters();
					if (perf.equals("ALIGN")){
						Message answer = manager.align(new Message(newId(), (Message)null,myId,serverId,"",params));
						if(!(answer instanceof ErrorMsg)){
							ACLMessage JADEanswer=msg.createReply();
							msg.setPerformative(ACLMessage.INFORM);
							JADEanswer.setContent(answer.getContent());
							myAgent.send(JADEanswer);
						}else{myLogger.log(Logger.WARNING, answer.getContent());}
					}else if (perf.equals("LOAD")){
						params.setParameter("url", info);
						params.setParameter("id", info);
						Message answer = manager.load(new Message(newId(), (Message)null,myId,serverId,"",params));
						if(!(answer instanceof ErrorMsg)){
							ACLMessage JADEanswer=msg.createReply();
							msg.setPerformative(ACLMessage.INFORM);
							JADEanswer.setContent(answer.getContent());
							myAgent.send(JADEanswer);
						}else{myLogger.log(Logger.WARNING, answer.getContent());}
					}else {
						ACLMessage reply = msg.createReply();
						reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
						myAgent.send(reply);
					}
				}
				else {
					block();
				}
			}
		});


		// Register with the DF
		registerWithDF();
	}

	protected void takeDown() {

		myLogger.log(Logger.INFO, "Agent Alignement Service close");
		this.doDelete();
	}

	private void registerWithDF() {
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setName(getLocalName()+'-'+SERVICE_NAME);
		sd.setType(SERVICE_TYPE);
		dfd.addServices(sd);
		try {
			myLogger.log(Logger.INFO, "Registering with DF...");
			DFService.register(this, dfd);
			myLogger.log(Logger.INFO, "Registration OK.");
		}
		catch (FIPAException fe) {
			myLogger.log(Logger.WARNING, "Error registering with DF.", fe);
		}
	}

	private int newId(){return localId++;}


}
