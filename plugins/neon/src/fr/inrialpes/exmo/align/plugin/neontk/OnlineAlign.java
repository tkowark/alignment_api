/*
 * $Id$
 *
 * Copyright (C) INRIA Rh�ne-Alpes, 2007-2008
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA
 */

package fr.inrialpes.exmo.align.plugin.neontk;

import java.io.BufferedReader;
 
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
 
import java.net.URL;
import java.net.URLConnection;
import java.util.Vector;

import javax.swing.JOptionPane;
 
import org.semanticweb.owl.align.Parameters;

import fr.inrialpes.exmo.align.impl.BasicParameters;
 

public class OnlineAlign {
		
		//public AlignmentClient ws = null;
		public  String HOST = null;
		public  String PORT = null;
		public  String WSDL = "7777";
		public  boolean connected = false;
		URL SOAPUrl = null;
		String SOAPAction = null;
		
		
	    public OnlineAlign( String htmlPort, String host)  {
	    	try {
	    		HOST = host;
	    		PORT = htmlPort;
	    		
	    		SOAPUrl = new URL( "http://" + host + ":" + htmlPort + "/aserv" );
	    		
	    	} catch ( Exception ex ) { ex.printStackTrace(); };
	    	
	    }
	    /*
	    public boolean isConnected() {
	    	boolean conn = true;
	    	try {
	    		connection = SOAPUrl.openConnection();
	    		httpConn = (HttpURLConnection) connection;
	    		httpConn.setDoOutput(true);
		        httpConn.setDoInput(true);

		         
		        OutputStream out = httpConn.getOutputStream();
	    		 
	    	} catch ( Exception ex ) { conn = false; ex.printStackTrace(); };
	    	
            return conn;
	    }
	    */
	    public String trimAlign(String alignId, String thres) {
	    	
			String answer = null;
			try {
				// Read parameters
				 
				Parameters params = new BasicParameters();
				params.setParameter( "host", HOST );
				//params.setParameter( "http", PORT );
				//params.setParameter( "wsdl", WSDL );
				params.setParameter( "command","trim");
				params.setParameter( "arg1",alignId);
				params.setParameter( "arg2",thres);
					
				// Create the SOAP message
				String message = createMessage( params );
				  
				System.out.println("HOST= :"+ HOST + ", PORT=  " + PORT + ",  Action = "+ SOAPAction);
				System.out.println("Message :"+ message);
				
				// Send message
				answer = sendMessage( message, params );
				
				System.out.println("Trim Align="+ answer);
			}
			catch ( Exception ex ) { ex.printStackTrace(); };
			if(! connected ) return null; 
			return answer;
			
	    }
	    
	    public String getMethods() {
	    	
			String answer = null;
		     
			try {
				// Read parameters
				 
				Parameters params = new BasicParameters();
				params.setParameter( "host", HOST );
				//params.setParameter( "http", PORT );
				//params.setParameter( "wsdl", WSDL );
				params.setParameter( "command","list");
				params.setParameter( "arg1","methods");
					
				// Create the SOAP message
				String message = createMessage( params );
				  
				System.out.println("HOST= :"+ HOST + ", PORT=  " + PORT + ",  Action = "+ SOAPAction);
				System.out.println("Message :"+ message);
				
				// Send message
				answer = sendMessage( message, params );
			}
			catch ( Exception ex ) { ex.printStackTrace(); };
			if(! connected ) return null; 
			return answer;
		 
				 
			
	    }
	    
	    public String findAlignForOntos(String onto1, String onto2) {
	    	
			String answer = null;
		     
			try {
				// Read parameters 
				Parameters params = new BasicParameters();
				params.setParameter( "host", HOST );
				//params.setParameter( "http", PORT );
				//params.setParameter( "wsdl", WSDL );
				params.setParameter( "command","find");
				params.setParameter( "arg1", onto1);
				params.setParameter( "arg2", onto2);	
				// Create the SOAP message
				String message = createMessage( params );
				  
				System.out.println("HOST= :"+ HOST + ", PORT=  " + PORT + ",  Action = "+ SOAPAction);
				System.out.println("Message :"+ message);
				
				// Send message
				answer = sendMessage( message, params );
			}
			catch ( Exception ex ) { ex.printStackTrace(); };
			if(! connected ) return null; 
				   
		    return answer;
			 
	    }
	    
	    public String getAllAlign() {
	    	
			String answer = null;
		     
			try {
				// Read parameters
				 
				Parameters params = new BasicParameters();
				params.setParameter( "host", HOST );
				//params.setParameter( "http", PORT );
				//params.setParameter( "wsdl", WSDL );
				params.setParameter( "command","list");
				params.setParameter( "arg1","alignments");
					
				// Create the SOAP message
				String message = createMessage( params );
				  
				System.out.println("HOST= :"+ HOST + ", PORT=  " + PORT + ",  Action = "+ SOAPAction);
				System.out.println("Message :"+ message);
				
				// Send message
				answer = sendMessage( message, params );
			}
			catch ( Exception ex ) { ex.printStackTrace(); };
			
			if(! connected ) return null; 
				   
			return answer;
			 
				 
			
	    }
	    
	    public String getAlignId(String method, String onto1, String onto2) {
	    	
	    	String[] aservArgAlign = new String[6];		
	    	String answer = null ;
				
				System.out.println("Uri 1="+ onto1);
			    System.out.println("Uri 2="+ onto2);
			    
			    Parameters params = new BasicParameters();
				params.setParameter( "host", HOST );
				//params.setParameter( "http", PORT );
				//params.setParameter( "wsdl", WSDL );
				params.setParameter( "command","match");
				params.setParameter( "arg1", method);
				params.setParameter( "arg2", onto1);
				params.setParameter( "arg3", onto2);
				
			    try {
			    	// Read parameters
			    	// Create the SOAP message
			    	String message = createMessage( params );
			  
			    	System.out.println("URL SOAP :"+ SOAPUrl+ ",  Action:"+ SOAPAction);
			    	System.out.println("Message :"+ message);
			
			    	// Send message
			    	answer = sendMessage( message, params );
			 
			    	// Displays it
			    	System.out.println("alignId="+ answer);
			    }
			    catch ( Exception ex ) { ex.printStackTrace(); };
			    if(! connected ) return null; 
			    return answer;
			 
	    }
	    
		
	    public String getOWLAlignment(String alignId) {
		
		//retrieve alignment for storing in OWL file
		
		 
		Parameters params = new BasicParameters();
		params.setParameter( "host", HOST );
		//params.setParameter( "http", PORT );
		//params.setParameter( "wsdl", WSDL );
		params.setParameter( "command","retrieve");
		params.setParameter( "arg1", alignId);
		params.setParameter( "arg2", "fr.inrialpes.exmo.align.impl.renderer.OWLAxiomsRendererVisitor");
		
		String answer=null;
	     
		try {
			// Read parameters
			//Parameters params = ws.readParameters( aservArgRetrieve );
			
			// Create the SOAP message
			String message = createMessage( params );

			System.out.println("URL SOAP :"+ SOAPUrl + ",  Action:"+  SOAPAction);
			System.out.println("Message :" + message);
			
			// Send message
			answer = sendMessage( message, params );
			if(! connected ) return null; 
			
			System.out.println("OwlAlign="+ answer);
			
		} catch ( Exception ex ) { ex.printStackTrace();  };
			 
			 
			// Cut SOAP header
		String []  cutResult = answer.split("result");
		
		if(cutResult==null) return null;
			
		String str = "";
			
		for(int i= 0; i< cutResult.length; i++){
		  	
			if(i >= 1 && i <= cutResult.length -2)
					str = str + cutResult[i];
		}
			
		//System.out.println("OwlAlign STR ="+ str);
		
		if(str.equals("")) return null;
		
		String str1 = str.substring(1, str.length() - 3);
			
			//extract id from "alid" 
		//String []  sali = alignId.split("/");
		//String uniqueId = sali[sali.length-2].concat(sali[sali.length-1]);
			
			
			//Add URI to OWL file : rethink !!!
		String s1 = str1.substring(0, str1.indexOf('>') + 1 );
		String s2 = str1.substring(str1.indexOf('>') + 2, str1.length());
			
		String[] ss2 = s1.split("xmlns");
		String s3 = "<?xml version=\"1.0\"?>\n" + ss2[0] + " ";
         						
		s3 = s3 + "xmlns=\"" + alignId  + "#\"\n ";
		s3 = s3 + "xml:base=\"" + alignId  + "\"\n ";
		s3 = s3 + "xmlns:owl=\"http://www.w3.org/2002/07/owl#\"\n " + "xmlns";
			
		for(int i=2; i<ss2.length;i++) {
				s3 = s3 + ss2[i];
			  	if(i != ss2.length-1 ) s3 = s3  + "xmlns";
		}
			
		return s3 + s2;
	    }
	    
	    public String getHTMLAlignment(String alignId) {
			
	    	//retrieve alignment for displaying
			
			Parameters params = new BasicParameters();
			params.setParameter( "host", HOST );
			//params.setParameter( "http", PORT );
			//params.setParameter( "wsdl", WSDL );
			params.setParameter( "command","retrieve");
			params.setParameter( "arg1", alignId);
			params.setParameter( "arg2", "fr.inrialpes.exmo.align.impl.renderer.HTMLRendererVisitor");
			
			Vector corrList = new Vector();
			 
			String answer = null;
			
			try {
				// Read parameters
				 
				//Parameters params = ws.readParameters( aservArgRetrieve );
				
				// Create the SOAP message
				String message = createMessage( params );
				  
				System.out.println("URL SOAP :"+ SOAPUrl+ ",  Action:"+ SOAPAction);
				System.out.println("Message :"+ message);
				
				// Send message
				answer = sendMessage( message, params );
				 
				//corrList = getCorresFromAnswer( answer, "tr", "#" );
		    	
			}
			catch ( Exception ex ) { ex.printStackTrace();  };
			if(! connected ) return null; 
			return answer;
			
	   }
	    
	    public String storeAlign(String alignId) {
			
	    	//retrieve alignment for displaying
			
			Parameters params = new BasicParameters();
			params.setParameter( "host", HOST );
			//params.setParameter( "http", PORT );
			//params.setParameter( "wsdl", WSDL );
			params.setParameter( "command","store");
			params.setParameter( "arg1", alignId);
			 
			 
			String answer = null;
			
			try {
				// Read parameters
				 
				//Parameters params = ws.readParameters( aservArgRetrieve );
				
				// Create the SOAP message
				String message = createMessage( params );
				  
				System.out.println("URL SOAP :"+ SOAPUrl+ ",  Action:"+ SOAPAction);
				System.out.println("Message :"+ message);
				
				// Send message
				answer = sendMessage( message, params );
				 
				System.out.println("Store Align="+ answer);
				//corrList = getCorresFromAnswer( answer, "tr", "#" );
		    	
			}
			catch ( Exception ex ) { ex.printStackTrace() ;};
			
			if(! connected ) return null; 
			
			return answer;
			
	   }  	
	    
	    public String createMessage( Parameters params ) throws Exception {
	        String messageBegin = "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\'http://schemas.xmlsoap.org/soap/envelope/\' " +
				                  "xmlns:xsi=\'http://www.w3.org/1999/XMLSchema-instance\' " + 
				                  "xmlns:xsd=\'http://www.w3.org/1999/XMLSchema\'>" +
				                  "<SOAP-ENV:Body>";
		String messageBody = "";
		String cmd = (String)params.getParameter( "command" );
		if ( cmd.equals("list" ) ) {
		    String arg = (String)params.getParameter( "arg1" );
		    if ( arg.equals("methods" ) ){  
			SOAPAction = "listmethodsRequest";
		    } else if ( arg.equals("renderers" ) ){
			SOAPAction = "listrenderersRequest";
		    } else if ( arg.equals("services" ) ){
			SOAPAction = "listservicesRequest";
		    } else if ( arg.equals("alignments" ) ){
			SOAPAction = "listalignmentsRequest";
		    } else {
			//usage();
			System.exit(-1);
		    }
		} else if ( cmd.equals("wsdl" ) ) {
		    SOAPAction = "wsdlRequest";
		} else if ( cmd.equals("find" ) ) {
		    SOAPAction = "findRequest";
		    String uri1 = (String)params.getParameter( "arg1" );
		    String uri2 = (String)params.getParameter( "arg2" );
		    if ( uri2 == null ){
			//usage();
			System.exit(-1);
		    }
		    messageBody = "<uri1>"+uri1+"</uri1><uri2>"+uri2+"</uri2>";
		} else if ( cmd.equals("match" ) ) {
		    SOAPAction = "matchRequest";
		    String uri1 = (String)params.getParameter( "arg1" );
		    String uri2 = (String)params.getParameter( "arg2" );
		    if ( uri2 == null ){
			//usage();
			System.exit(-1);
		    }
		    String method = null;
		    String arg3 = (String)params.getParameter( "arg3" );
		    if ( arg3 != null ) {
			method = uri1; uri1 = uri2; uri2 = arg3;
		    }
		    arg3 = (String)params.getParameter( "arg4" );
		    messageBody = "<url1>"+uri1+"</url1><url2>"+uri2+"</url2>";
		    if ( method != null )
			messageBody += "<method>"+method+"</method>";
		    //fr.inrialpes.exmo.align.impl.method.SubsDistNameAlignment
		    if ( arg3 != null )
			messageBody += "<force>"+arg3+"</force>";
		} else if ( cmd.equals("trim" ) ) {
		    SOAPAction = "cutRequest";
		    String id = (String)params.getParameter( "arg1" );
		    String thres = (String)params.getParameter( "arg2" );
		    if ( thres == null ){
			//usage();
			//System.exit(-1);
		    }
		    String method = null;
		    String arg3 = (String)params.getParameter( "arg3" );
		    if ( arg3 != null ) {
			method = thres; thres = arg3;
		    }
		    messageBody = "<alid>"+id+"</alid><threshold>"+thres+"</threshold>";
		    if ( method != null )
			messageBody += "<method>"+method+"</method>";
		} else if ( cmd.equals("invert" ) ) {
		    SOAPAction = "invertRequest";
		    String uri = (String)params.getParameter( "arg1" );
		    if ( uri == null ){
			//usage();
			//System.exit(-1);
		    }
		    messageBody = "<alid>"+uri+"</alid>";
		} else if ( cmd.equals("store" ) ) {
		    SOAPAction = "storeRequest";
		    String uri = (String)params.getParameter( "arg1" );
		    if ( uri == null ){
			//usage();
			//System.exit(-1);
		    }
		    messageBody = "<alid>"+uri+"</alid>";
		} else if ( cmd.equals("load" ) ) {
		    String url = (String)params.getParameter( "arg1" );
		    if ( url == null ){
			SOAPAction = "loadRequest";
			//usage();
			//System.exit(-1);
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			String line;
			String content = "";
			while ((line = in.readLine()) != null) {
			    content += line + "\n";
			}
			if (in != null) in.close();
			System.err.println(content);
			messageBody = "<content>"+content+"</content>";
		    } else {
			SOAPAction = "loadfileRequest";
			messageBody = "<url>"+url+"</url>";
		    }
		    /* This may read the input stream!
				// Most likely Web service request
				int length = request.getContentLength();
				char [] mess = new char[length+1];
				try { 
				    new BufferedReader(new InputStreamReader(request.getInputStream())).read( mess, 0, length);
				} catch (Exception e) {
				    e.printStackTrace(); // To clean up
				}
				params.setProperty( "content", new String( mess ) );
		    */
		} else if ( cmd.equals("retrieve" ) ) {
		    SOAPAction = "retrieveRequest";
		    String uri = (String)params.getParameter( "arg1" );
		    String method = (String)params.getParameter( "arg2" );
		    if ( method == null ){
			//usage();
			//System.exit(-1);
		    }
		    messageBody = "<alid>"+uri+"</alid><method>"+method+"</method>";
		} else if ( cmd.equals("metadata" ) ) {
		    SOAPAction = "metadata";
		    String uri = (String)params.getParameter( "arg1" );
		    String key = (String)params.getParameter( "arg2" );
		    if ( key == null ){
			//usage();
			//System.exit(-1);
		    }
		    messageBody = "<alid>"+uri+"</alid><key>"+key+"</key>";
		} else {
		    //usage();
		    //System.exit(-1);
		}
			// Create input message and URL
		String messageEnd = "</SOAP-ENV:Body>"+"</SOAP-ENV:Envelope>";
		String message = messageBegin + messageBody + messageEnd;
		return message;
	    }
	    
	    public String sendMessage( String message, Parameters param )   {
	    	// Create the connection
	        	 
	    
    		
	        byte[] b = message.getBytes();
	        
	        String answer = "";
	            // Create HTTP Request
	        try {
	        	 
	    		URLConnection connection = SOAPUrl.openConnection();
	        	HttpURLConnection httpConn = (HttpURLConnection) connection;
	            httpConn.setRequestProperty( "Content-Length",
	                                         String.valueOf( b.length ) );
	            httpConn.setRequestProperty("Content-Type","text/xml; charset=utf-8");
	            httpConn.setRequestProperty("SOAPAction",SOAPAction);
	            httpConn.setRequestMethod( "POST" );
	            httpConn.setDoOutput(true);
	            httpConn.setDoInput(true);

	            // Send the request through the connection
	            OutputStream out = httpConn.getOutputStream();
	       
	        	out.write( b );    
	        	out.close();

	            // Read the response and write it to standard output
	            InputStreamReader isr = new InputStreamReader(httpConn.getInputStream());
	            BufferedReader in = new BufferedReader(isr);
	        
	            String line;
	            while ((line = in.readLine()) != null) {
	            	answer += line + "\n";
	            }
	            if (in != null) in.close();
	        } catch  (Exception ex) { connected= false; ex.printStackTrace() ; return null;}
	        
	        connected = true;
	    	return answer;
	    }
	    
}
