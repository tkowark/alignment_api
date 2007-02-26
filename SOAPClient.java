/** 
 * SOAPClient4XG. Read the SOAP envelope file passed as the second
 * parameter, pass it to the SOAP endpoint passed as the first parameter, and
 * print out the SOAP envelope passed as a response.  with help from Michael
 * Brennan 03/09/01
 * 
 * java SOAPClient4XG http://services.xmethods.net:80/soap/servlet/rpcrouter weatherreq.xml
 *
 * @author  Bob DuCharme
 * @version 1.1
 * @param   SOAPUrl      URL of SOAP Endpoint to send request.
 * @param   xmlFile2Send A file with an XML document of the request.  
 *
 * 5/23/01 revision: SOAPAction added
*/

import java.io.*;
import java.net.*;

public class SOAPClient {
    public static void main(String[] args) throws Exception {

        if (args.length  < 2) {
            System.err.println("Usage:  java SOAPClient4XG " +
                               "http://soapURL soapEnvelopefile.xml" +
                               " [SOAPAction]");
				System.err.println("SOAPAction is optional.");
            System.exit(1);
        }

        String SOAPUrl      = args[0];
        String xmlFile2Send = args[1];

		  String SOAPAction = "";
        if (args.length  > 2) 
				SOAPAction = args[2];
				
        // Create the connection where we're going to send the file.
        URL url = new URL(SOAPUrl);
        URLConnection connection = url.openConnection();
        HttpURLConnection httpConn = (HttpURLConnection) connection;

        // Open the input file. After we copy it to a byte array, we can see
        // how big it is so that we can set the HTTP Cotent-Length
        // property. (See complete e-mail below for more on this.)

        FileInputStream fin = new FileInputStream(xmlFile2Send);

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
    
        // Copy the SOAP file to the open connection.
        copy(fin,bout);
        fin.close();

        byte[] b = bout.toByteArray();

        // Set the appropriate HTTP parameters.
        httpConn.setRequestProperty( "Content-Length",
                                     String.valueOf( b.length ) );
        httpConn.setRequestProperty("Content-Type","text/xml; charset=utf-8");
		httpConn.setRequestProperty("SOAPAction",SOAPAction);
        httpConn.setRequestMethod( "POST" );
        httpConn.setDoOutput(true);
        httpConn.setDoInput(true);
		
		System.out.println("test111"+httpConn.toString());
        // Everything's set up; send the XML that was read in to b.
        OutputStream out = httpConn.getOutputStream();
        out.write( b );    
        out.close();

        // Read the response and write it to standard out.

        InputStreamReader isr =
            new InputStreamReader(httpConn.getInputStream());
        BufferedReader in = new BufferedReader(isr);

        String inputLine;

        while ((inputLine = in.readLine()) != null)
            System.out.println(inputLine);

        in.close();
    }

  // copy method from From E.R. Harold's book "Java I/O"
  public static void copy(InputStream in, OutputStream out) 
   throws IOException {

    // do not allow other threads to read from the
    // input or write to the output while copying is
    // taking place

    synchronized (in) {
      synchronized (out) {

        byte[] buffer = new byte[256];
        while (true) {
          int bytesRead = in.read(buffer);
          if (bytesRead == -1) break;
          out.write(buffer, 0, bytesRead);
        }
      }
    }
  } 
}

