package fr.inrialpes.exmo.align.service.jade;

import java.io.IOException;

import fr.inrialpes.exmo.align.service.jade.JadeFIPAAServiceAgent;

import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.util.leap.Properties;
import jade.wrapper.*;


public class JadeFIPAAServProfile  {

	public void init( int port ) throws IOException {
		
		 Properties props = new Properties();
		 try {

		      // Get a hold on JADE runtime
		      Runtime rt = Runtime.instance();

		      // Exit the JVM when there are no more containers around
		      rt.setCloseVM(true);

		      // Launch a complete platform on the 8888 port
		      
/** Profile with no MTP( Message Transfer Protocol
		      props.setProperty("nomtp", "true");
		      Profile pMain = new ProfileImpl(props);
		      **/
		      // create a default Profile
              Profile pMain = new ProfileImpl(null, 8888, null);

		      System.out.println("Launching a whole in-process platform..."+pMain);
		     AgentContainer mc = rt.createMainContainer(pMain);

		      AgentController custom = mc.createNewAgent("JadeFIPAAServiceAgent", JadeFIPAAServiceAgent.class.getName(), null);
		      custom.start();

		    }
		    catch(Exception e) {
		      e.printStackTrace();
		    }

		  }

		
		
	   
	public void close(){
		
	}
}
