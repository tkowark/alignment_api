/** 
 * Example of connection to the Alignment Server through  HTTP/SOAP 
 * Inspired from SOAPClient4XG by Bob DuCharme
 * $Id$
 *
*/

import java.io.*;
import java.net.*;

public class Client {
    public static void main(String[] args) throws Exception {
        String SOAPUrl      = "http://localhost:8089/aserv";
        String MessageBegin = "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\'http://schemas.xmlsoap.org/soap/envelope/\' " +
			                  "xmlns:xsi=\'http://www.w3.org/1999/XMLSchema-instance\' " + 
			                  "xmlns:xsd=\'http://www.w3.org/1999/XMLSchema\'> " +
			                  "<SOAP-ENV:Body>";
	// Create input message and URL
	String SOAPAction = "align";  // define the specific action name
	//String SOAPAction = "listmethods";  // define the specific action name
	String MessageBody = "<url1>http://alignapi.gforge.inria.fr/tutorial/myOnto.owl</url1>" +  // defind the profer argument for the specific action name
	    "<url2>http://alignapi.gforge.inria.fr/tutorial/edu.mit.visus.bibtex.owl</url2>" +
	    "<method>fr.inrialpes.exmo.align.impl.method.SubsDistNameAlignment</method>" +
	    "<force>false</force>";
	String MessageEnd = "</SOAP-ENV:Body> "+"</SOAP-ENV:Envelope>";

	String Message = MessageBegin + MessageBody + MessageEnd;
        // Create the connection where we're going to send the file.
        
	System.out.println("Send to "+SOAPUrl+" :: "+SOAPAction);
	System.out.println("==>");
	System.out.println(Message);
	System.out.println("");

	// Create the connection
	URL url = new URL(SOAPUrl);
        URLConnection connection = url.openConnection();
        HttpURLConnection httpConn = (HttpURLConnection) connection;

        byte[] b = Message.getBytes();

	// Create HTTP Request
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
        String inputLine = in.readLine();
	//       while ((inputLine = in.readLine()) != null)
        System.out.println("Receive ==>");
	System.out.println(inputLine);

        in.close();
    }
}

