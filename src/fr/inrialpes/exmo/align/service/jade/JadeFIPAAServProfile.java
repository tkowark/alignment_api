/*
 * $Id$
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

import fr.inrialpes.exmo.align.service.jade.JadeFIPAAServiceAgent;
import fr.inrialpes.exmo.align.service.AlignmentServiceProfile;
import fr.inrialpes.exmo.align.service.AServProtocolManager;
import fr.inrialpes.exmo.align.service.AServException;

import org.semanticweb.owl.align.Parameters;

import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.util.Logger;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;


public class JadeFIPAAServProfile implements AlignmentServiceProfile {


	private AgentContainer mc;
	private AgentController algagentcontroller;
	private Logger myLogger = Logger.getMyLogger(getClass().getName());

	public void init( Parameters params, AServProtocolManager manager ) throws AServException {
		int port = 8888;
		int debug = 0;
		Object args[] = new Object[2];
		
		//set up the manager as an argument to pass to the JADEFIPAAServiceAgent
		args[0]=manager;
		
		// set up the Parameters as an argument to pass to the JADEFIPAServiceAgent
		args[1]=params;
		
		if ( params.getParameter( "jade" ) != null )
			port = Integer.parseInt( (String)params.getParameter( "jade" ) );
		if ( params.getParameter( "debug" ) != null )
			debug = ((Integer)params.getParameter( "debug" )).intValue() - 1;

		/**		
	Properties props = new Properties();
		 **/
		try {
			// Get a hold on JADE runtime
			Runtime rt = Runtime.instance();

			// Exit the JVM when there are no more containers around
			rt.setCloseVM(true);

			/** Profile with no MTP( Message Transfer Protocol
		props.setProperty("nomtp", "true");
		Profile pMain = new ProfileImpl(props);
			 **/
			// create a default Profile
			Profile pMain = new ProfileImpl(null, port, null);

			if ( debug > 0 )
				System.out.println("Launching a whole in-process platform..."+pMain);
			mc = rt.createMainContainer(pMain);
			algagentcontroller = mc.createNewAgent("JadeFIPAAServiceAgent", JadeFIPAAServiceAgent.class.getName(), args);
			algagentcontroller.start();
		}
		catch(Exception e) {
			throw new AServException ( "Cannot launch Jade Server" , e );
		}
	}

	public void close(){
		try{
			algagentcontroller.kill();
			mc.kill();
			myLogger.log(Logger.INFO, "Agent Alignement close");
		}
		catch (ControllerException e){myLogger.log(Logger.WARNING, "Error killing the alignment agent."); }
	}
	
}
