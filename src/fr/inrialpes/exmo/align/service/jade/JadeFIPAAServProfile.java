/*
 * $Id: AlignmentService.java 335 2006-10-05 10:02:02Z euzenat $
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

import java.io.IOException;

import fr.inrialpes.exmo.align.service.jade.JadeFIPAAServiceAgent;
import fr.inrialpes.exmo.align.service.AlignmentServiceProfile;
import fr.inrialpes.exmo.align.service.AServProtocolManager;
import fr.inrialpes.exmo.align.service.AServException;

import org.semanticweb.owl.align.Parameters;

import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.util.leap.Properties;
// JE: find them
import jade.wrapper.*;


public class JadeFIPAAServProfile implements AlignmentServiceProfile {

    public void init( Parameters params, AServProtocolManager manager ) throws AServException {
	//	public void init( int port ) throws IOException {
	int port = 8888;
	int debug = 0;
	
	if ( params.getParameter( "jadeport" ) != null )
	    port = Integer.parseInt( (String)params.getParameter( "jadeport" ) );
	if ( params.getParameter( "debug" ) != null )
	    debug = Integer.parseInt( (String)params.getParameter( "debug" ) ) - 1;
	
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
	    AgentContainer mc = rt.createMainContainer(pMain);
	    
	    AgentController custom = mc.createNewAgent("JadeFIPAAServiceAgent", JadeFIPAAServiceAgent.class.getName(), null);
	    custom.start();
	}
	catch(Exception e) {
	    throw new AServException ( "Cannot launch Jade Server" , e );
	    //	    e.printStackTrace();
	}
    }

    public void close(){
    }
}
