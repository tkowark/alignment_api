/*
 * $Id: JadeFIPAAServiceAgent.java 335 2006-10-05 10:02:02Z euzenat $
 *
 * Copyright (C) Orange R&D, 2006
 * Copyright (C) INRIA Rhône-Alpes, 2006
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

import jade.core.Agent;
import jade.core.Profile;
import jade.core.behaviours.*;
import jade.lang.acl.*;

import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;

import jade.util.Logger;

public class JadeFIPAAServiceAgent extends Agent {

		public static final String SERVICE_NAME = "Alignment";
		public static final String SERVICE_TYPE = "Alignment-service";
		
		private Logger myLogger = Logger.getMyLogger(getClass().getName());
		
		protected void setup() {
	 		myLogger.log(Logger.INFO, "Hallo World! My name is "+getAID().getName());
	 		
	 		// Read arguments
			Object[] args = getArguments();
			if (args != null) {
				for (int i = 0; i < args.length; ++i) {
					myLogger.log(Logger.INFO, "Arg-"+i+" = "+args[i]);
				}
			}

			// Add initial behaviours
			addBehaviour(new CyclicBehaviour(this) {
				public void action() {
					ACLMessage msg = myAgent.receive();
					if (msg != null) {
						myLogger.log(Logger.INFO, "Received message: "+msg);
						ACLMessage reply = msg.createReply();
						reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
						myAgent.send(reply);
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
		
		
	
	
}
