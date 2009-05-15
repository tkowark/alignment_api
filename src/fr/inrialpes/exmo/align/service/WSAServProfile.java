/*
 * $Id$
 *
 * Copyright (C) INRIA, 2007-2008.
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

package fr.inrialpes.exmo.align.service;

import fr.inrialpes.exmo.align.impl.BasicParameters;
import fr.inrialpes.exmo.align.impl.Annotations;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.Parameters;

import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ByteArrayInputStream;

import java.util.Hashtable;
import java.util.Set;
import java.util.HashSet;
import java.util.Properties;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.jar.JarFile;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;
import java.util.jar.Attributes.Name;

import java.lang.NullPointerException;

// For message parsing
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * WSAServProfile: a SOAP over HTTP provile for the Alignment server
 * It uses the HTTP server of HTTPAServProfile
 * 
 * Improvements to come:
 * - provide WSDL from that channel as well
 * - implement request_id management (fully missing here)
 * - use XML/Xpath parsers [Make it namespace aware please]
 * - clean up
 */

public class WSAServProfile implements AlignmentServiceProfile {

    private int tcpPort;
    private String tcpHost;
    private int debug = 0;
    private AServProtocolManager manager;
    private static String wsdlSpec = null;

    private String myId;
    private String serverURL;
    private int localId = 0;

    private static DocumentBuilder BUILDER = null;


    // ==================================================
    // Socket & server code
    // ==================================================

    public void init( Parameters params, AServProtocolManager manager ) throws AServException {
	this.manager = manager;
	// This may register the WSDL file to some directory
	serverURL = manager.serverURL()+"/aserv/";
	myId = "SOAPoverHTTPInterface";
	localId = 0;	

	// New XML parsing stuff
	final DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
	fac.setValidating(false);
	fac.setNamespaceAware(false); // Change this!
	try { BUILDER = fac.newDocumentBuilder(); }
	catch (ParserConfigurationException e) {
	    throw new AServException( "Cannot initialize SOAP message parsing", e );
	}

	// Read the WSDL specification
	try {
	    String classPath = System.getProperty("java.class.path",".");
	    StringTokenizer tk = new StringTokenizer(classPath,File.pathSeparator);
	    Set<String> visited = new HashSet<String>();
	    classPath = "";
	    while ( tk != null && tk.hasMoreTokens() ){
		StringTokenizer tk2 = tk;
		tk = null;
		// Iterate on Classpath
		while ( tk2 != null && tk2.hasMoreTokens() ) {
		    File file = new File( tk2.nextToken() );
		    if ( file.isDirectory() ) {
		    } else if ( file.toString().endsWith(".jar") &&
				!visited.contains( file.toString() ) &&
				file.exists() ) {
			visited.add( file.toString() );
			try { 
			    JarFile jar = new JarFile( file );
			    Enumeration enumeration = jar.entries();
			    while( enumeration != null && enumeration.hasMoreElements() ){
				JarEntry entry = (JarEntry)enumeration.nextElement();
				String classname = entry.toString();
				if ( classname.equals("fr/inrialpes/exmo/align/service/aserv.wsdl") ){
				    // Parse it
				    InputStream is = jar.getInputStream( entry );
				    BufferedReader in = new BufferedReader(new InputStreamReader(is));
				    String line;
				    while ((line = in.readLine()) != null) {
					wsdlSpec += line + "\n";
				    }
				    if (in != null) in.close();
				    wsdlSpec = wsdlSpec.replace( "%%ASERVADDRESS%%", serverURL );
				    // exit
				    enumeration = null;
				    tk2 = null;
				    tk = null;
				    classPath = "";
				}
			    }
			    if ( wsdlSpec == null ){
				// Iterate on needed Jarfiles
				// JE(caveat): this deals naively with Jar files,
				// in particular it does not deal with section'ed MANISFESTs
				Attributes mainAttributes = jar.getManifest().getMainAttributes();
				String path = mainAttributes.getValue( Name.CLASS_PATH );
				if ( debug > 0 ) System.err.println("  >CP> "+path);
				if ( path != null && !path.equals("") ) {
				    // JE: Not sure where to find the other Jars:
				    // in the path or at the local place?
				    classPath += File.pathSeparator+file.getParent()+File.separator + path.replaceAll("[ \t]+",File.pathSeparator+file.getParent()+File.separator);
				}
			    }
			} catch (NullPointerException nullexp) { //Raised by JarFile
			    System.err.println("Warning "+file+" unavailable");
			}
		    }
		}
		if ( !classPath.equals("") ) {
		    tk =  new StringTokenizer(classPath,File.pathSeparator);
		    classPath = "";
		}
	    }
	} catch (IOException ioex) {
	    ioex.printStackTrace();
	}
    }

    public void close(){
	// This may unregister the WSDL file to some directory
    }
    
    // ==================================================
    // API parts
    // ==================================================

    /**
     * HTTP protocol implementation
     * each call of the protocol is a direct URL
     * and the answer is through the resulting page (RDF? SOAP? HTTP?)
     * Not implemented yet
     * but reserved if appears useful
     */
    public String protocolAnswer( String uri, String perf, Properties header, Parameters param ) {
	String method = null;
	String message = null;
	String restful = (String)param.getParameter("restful");
	if ( restful == null ) {
		method= header.getProperty("SOAPAction");
		message = ((String)param.getParameter("content")).trim();
	} else {
		method= perf;
	}

	// Create the DOM tree for the SOAP message
	Document domMessage = null;
	if( restful == null )
	try {
		 
	    domMessage = BUILDER.parse( new ByteArrayInputStream( message.getBytes()) );
	    // DECIDE WHAT TO DO WITH THESE ERRORS
	    // CERTAINLY RETURN A "CANNOT PARSE REQUEST"
	} catch  ( IOException ioex ) {
	    ioex.printStackTrace();
	} catch  ( SAXException saxex ) {
	    //System.err.println(" BUILDER problem.");
	    saxex.printStackTrace();
	}

	// JE: Note that we are namespace unaware here
	String SOAPMsg = "<SOAP-ENV:Envelope\n   xmlns='http://exmo.inrialpes.fr/align/service'\n   xml:base='http://exmo.inrialpes.fr/align/service'\n   xmlns:SOAP-ENV='http://schemas.xmlsoap.org/soap/envelope/'\n   xmlns:xsi='http://www.w3.org/1999/XMLSchema-instance'\n   xmlns:xsd='http://www.w3.org/1999/XMLSchema'>\n  <SOAP-ENV:Body>\n";

	String RESTMsg = "";
	String msg="";

	if ( perf.equals("WSDL") || method.equals("wsdlRequest") ) {
	    msg += wsdlAnswer();
	} else if ( method.equals("listalignmentsRequest") || method.equals("listalignments") ) {
	    msg += "      <listalignmentsResponse>\n      <alignmentList>";
	    for( Enumeration e = manager.alignments(); e.hasMoreElements(); ){
		String id = ((Alignment)e.nextElement()).getExtension(Annotations.ALIGNNS, Annotations.ID);
		msg += "        <alid>"+id+"</alid>\n";
	    }
	    msg += "      </alignmentList>\n    </listalignmentsResponse>\n";
	    // -> List of URI
	} else if ( method.equals("listmethodsRequest") || method.equals("listmethods") ) { // -> List of String
	    msg += "    <listmethodsResponse>\n      <classList>\n";
	    for( Iterator it = manager.listmethods().iterator(); it.hasNext(); ) {
		String mt = (String) it.next();
		msg += "        <method>"+mt+"</method>\n";
	    }
	    msg += "      </classList>\n    </listmethodsResponse>\n";
	} else if ( method.equals("listrenderersRequest") || method.equals("listrenderers") ) { // -> List of String
	    msg += "    <listrenderersResponse>\n      <classList>\n";
	    for( Iterator it = manager.listrenderers().iterator(); it.hasNext(); ) {
		msg += "        <renderer>"+it.next()+"</renderer>\n";
	    }
	    msg += "      </classList>\n    </listrenderersResponse>\n";
	} else if ( method.equals("listservicesRequest") || method.equals("listservices") ) { // -> List of String
	    msg += "    <listservicesResponse>\n      <classList>\n";
	    for( Iterator it = manager.listservices().iterator(); it.hasNext(); ) {
		msg += "        <service>"+it.next()+"</service>\n";
	    }
	    msg += "      </classList>\n    </listservicesResponse>\n";
	} else if ( method.equals("storeRequest") || method.equals("store") ) { // URI -> URI
	    Message answer = null;
	    msg += "    <storeResponse>\n";
	    Parameters params = param;
	     
	    if( restful == null ) {
		params = getParameters( domMessage );
		if ( params.getParameter( "alid" ) == null ) {
		     answer = new NonConformParameters(0,(Message)null,myId,"",message,(Parameters)null);
		} else {
		     params.setParameter( "id", params.getParameter( "alid" ) );
	    	}
	    }

	    if ( answer == null )
		answer = manager.store( new Message(newId(),(Message)null,myId,serverURL,(String)params.getParameter( "id" ), params) );
	    if ( answer instanceof ErrorMsg ) {
		msg += displayError( answer );
	    } else {
		msg += displayAnswer( answer );
	    }
	    msg += "    </storeResponse>\n";
	} else if ( method.equals("invertRequest") || method.equals("invert") ) { // URI -> URI
	    Message answer = null;
	    Parameters params = param;
	    if( restful == null ) {
		params = getParameters( domMessage );
 	    }
	    msg += "    <invertResponse>\n";

	    if ( params.getParameter( "alid" ) == null ) {
		     answer = new NonConformParameters(0,(Message)null,myId,"",message,(Parameters)null);
	    } else {
		     params.setParameter( "id", params.getParameter( "alid" ) );
	    }

	    if ( answer == null )
		answer = manager.inverse( new Message(newId(),(Message)null,myId,serverURL, (String)params.getParameter( "id" ), params) );
	    if ( answer instanceof ErrorMsg ) {
		msg += displayError( answer );
	    } else {
		msg += displayAnswer( answer );
	    }
	    msg += "    </invertResponse>\n";
	} else if ( method.equals("cutRequest") || method.equals("cut") ) { // URI * string * float -> URI
	    Message answer = null;	    
	    Parameters params = param;
	    if( restful == null ) {
		params = getParameters( domMessage );
		 
 	    }

	    msg += "    <cutResponse>\n";

	    if ( params.getParameter( "alid" ) == null ) {
		     answer = new NonConformParameters(0,(Message)null,myId,"",message,(Parameters)null);
	    } else {
		     params.setParameter( "id", params.getParameter( "alid" ) );
	    }

	    if ( params.getParameter( "method" ) == null ) {
		params.setParameter( "method", "hard" );
	    }

	    if ( params.getParameter( "threshold" ) == null ) {
		answer = new NonConformParameters(0,(Message)null,myId,"",message,(Parameters)null);
	    }

	    if ( answer == null )
		answer = manager.cut( new Message(newId(),(Message)null,myId,serverURL,(String)params.getParameter( "id" ), params) );
	    if ( answer instanceof ErrorMsg ) {
		msg += displayError( answer );
	    } else {
		msg += displayAnswer( answer );
	    }
	    msg += "    </cutResponse>\n";
	} else if ( method.equals("matchRequest") || method.equals("match") ) { // URL * URL * URI * String * boolean * (params) -> URI
	    Message answer = null;
	    msg += "    <matchResponse>\n";
	    Parameters params = param;
	    if( restful == null ) {
		params = getParameters( domMessage );
	    	if ( params.getParameter( "url1" ) == null ) {
		     answer = new NonConformParameters(0,(Message)null,myId,"",message,(Parameters)null);
		} else {
		     params.setParameter( "onto1", params.getParameter( "url1" ) );
	    	}

	    	if ( params.getParameter( "url2" ) == null ) {
		     answer = new NonConformParameters(0,(Message)null,myId,"",message,(Parameters)null);
		} else {
		     params.setParameter( "onto2", params.getParameter( "url2" ) );
	    	}
	    }

	    if ( answer == null )
		 answer = manager.align( new Message(newId(),(Message)null,myId,serverURL,"", params) );
 
	    if ( answer instanceof ErrorMsg ) {
		msg += displayError( answer );
	    } else {
		msg += displayAnswer( answer );
	    }
	    msg += "</matchResponse>\n";
	} else if ( method.equals("align") ) { // URL * URL * (params) -> URI
	    // This is a dummy method for emulating a WSAlignement service
	    Message answer = null;
	    msg += "    <alignResponse>\n";
	    Parameters params = param;
	    if( restful == null ) {
		params = getParameters( domMessage );
	    	if ( params.getParameter( "url1" ) == null ) {
		    answer = new NonConformParameters(0,(Message)null,myId,"",message,(Parameters)null);
		} else {
			params.setParameter( "onto1", params.getParameter( "url1" ) );
	    	}

	    	if ( params.getParameter( "url2" ) == null ) {
		     answer = new NonConformParameters(0,(Message)null,myId,"",message,(Parameters)null);
		} else {
	             params.setParameter( "onto2", params.getParameter( "url2" ) );
	    	}

	    	if ( params.getParameter( "wsmethod" ) == null ) {
		     params.setParameter( "method", "fr.inrialpes.exmo.align.impl.method.StringDistAlignment" );
		} else {
		     params.setParameter( "method", params.getParameter( "wsmethod" ) );
	    	}
	    }

	    if ( answer == null ) {
		Message result = manager.align( new Message(newId(),(Message)null,myId,serverURL,"", params) );
		if ( result instanceof ErrorMsg ) {
		    answer = result;
		} else {
		    params = new BasicParameters();
		    params.setParameter( "id",  result.getContent() );
		    if ( params.getParameter( "id" ) == null ) {
			answer = new NonConformParameters(0,(Message)null,myId,"",message,(Parameters)null);
		    }
		    params.setParameter( "method",  "fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor" );
		    params.setParameter( "embedded", "true" );
			 
		    if ( answer == null )
			answer = manager.render( new Message(newId(),(Message)null,myId,serverURL, "", params) );
		}
	    }

	    if ( answer instanceof ErrorMsg ) {
		msg += displayError( answer );
		 
	    } else {
		// JE: Depending on the type we should change the MIME type
		// This should be returned in answer.getParameters()
		msg += "      <result>\n" + answer.getContent() + "      </result>\n";
	    }
	    msg += "    </alignResponse>\n";
	} else if ( method.equals("findRequest") || method.equals("find") ) { // URI * URI -> List of URI
	    Message answer = null;
	    msg += "    <findResponse>\n";
	    Parameters params = param;		

	    if( restful == null ) {
		params = getParameters( domMessage );
		if ( params.getParameter( "url1" ) == null ) {
		     answer = new NonConformParameters(0,(Message)null,myId,"",message,(Parameters)null);
		} else {
		     params.setParameter( "onto1", params.getParameter( "url1" ) );
	    	}

	    	if ( params.getParameter( "url2" ) == null ) {
		     answer = new NonConformParameters(0,(Message)null,myId,"",message,(Parameters)null);
		} else {
		     params.setParameter( "onto2", params.getParameter( "url2" ) );
		}
	    }

	    if ( answer == null ) {
		    answer = manager.existingAlignments( new Message(newId(),(Message)null,myId,serverURL,"", params) );
            }
	    	    
	    if ( answer instanceof ErrorMsg ) {
		msg += displayError( answer );
		 
	    } else {
		msg += displayAnswer( answer );
	    }
	    msg += "    </findResponse>\n";
	     
	} else if ( method.equals("retrieveRequest") || method.equals("retrieve")) { // URI * method -> XML
	    
	    Message answer = null;
	    msg += "    <retrieveResponse>\n";		
            Parameters params = param;		

	    if( restful == null ) {
		params = getParameters( domMessage );
		if ( params.getParameter( "alid" ) == null ) {
		     answer = new NonConformParameters(0,(Message)null,myId,"",message,(Parameters)null);
		} else {
		     params.setParameter( "id", params.getParameter( "alid" ) );
	    	}

	    	if ( params.getParameter( "method" ) == null )
		     answer = new NonConformParameters(0,(Message)null,myId,"",message,(Parameters)null);
	    }

	    if ( answer == null )
		params.setParameter( "embedded", "true" );
		answer = manager.render( new Message(newId(),(Message)null,myId,serverURL, "", params) );
	    if ( answer instanceof ErrorMsg ) {
		msg += displayError( answer );
	    } else {
		// JE: Depending on the type we should change the MIME type
		// This should be returned in answer.getParameters()
		msg += "      <result>\n" + answer.getContent() + "      \n</result>";
	    }
	    msg += "    </retrieveResponse>\n";
	} else if ( method.equals("metadataRequest") || method.equals("metadata") ) { // URI -> XML
	    Message answer = null;
	    msg += "    <metadataResponse>\n";
	    Parameters params = param;		
	
	    if( restful == null ) {
		params = getParameters( domMessage );
		if ( params.getParameter( "alid" ) == null ) {
		     answer = new NonConformParameters(0,(Message)null,myId,"",message,(Parameters)null);
		} else {
		     params.setParameter( "id", params.getParameter( "alid" ) );
	    	}
	    }
	    if ( answer == null ) {
		     params.setParameter( "embedded", "true" );
		     params.setParameter( "method", "fr.inrialpes.exmo.align.impl.renderer.XMLMetadataRendererVisitor");
		     answer = manager.render( new Message(newId(),(Message)null,myId,serverURL, "", params) );
            }
	    	    
	    if ( answer instanceof ErrorMsg ) {
		msg += displayError( answer );
		 
	    } else {
		msg += displayAnswer( answer );
	    }
	    msg += "    </metadataResponse>\n";
	} else if ( method.equals("loadRequest") || method.equals("load") ) { // URL -> URI
	    msg += "    <loadResponse>\n";
	    Parameters params = getParameters( domMessage );
	    Message answer = null;

	    if ( params.getParameter( "url" ) == null &&
		 param.getParameter( "filename" ) != null ) {
		// HTTP Server has stored it in filename (HTMLAServProfile)
		params.setParameter( "url",  "file://"+param.getParameter( "filename" ) );
	    }
	    if ( answer == null )
		answer = manager.load( new Message(newId(),(Message)null,myId,serverURL,"", params) );
	    if ( answer instanceof ErrorMsg ) {
		msg += displayError( answer );
	    } else {
		msg += displayAnswer( answer );
	    }
	    msg += "    </loadResponse>\n";
	} else if ( method.equals("loadfileRequest") ) { // XML -> URI
	    Parameters params = getParameters( domMessage );
	    Message answer = null;
	    msg += "    <loadResponse>\n";

	    if ( params.getParameter( "url" ) == null ) {
		answer = new NonConformParameters(0,(Message)null,myId,"",message,(Parameters)null);
	    }
	    if ( answer == null )
		answer = manager.load( new Message(newId(),(Message)null,myId,serverURL,"", params) );
	    if ( answer instanceof ErrorMsg ) {
		msg += displayError( answer );
	    } else {
		msg += displayAnswer( answer );
	    }
	    msg += "    </loadResponse>\n";
	} else if ( method.equals("translateRequest") ) { // XML * URI -> XML
	    msg += "    <translateResponse>\n";
	    // Not done yet
	    msg += "    </translateResponse>\n";
	} else {
	    msg += "    <UnRecognizedAction />\n";
	}

	SOAPMsg += msg + "</SOAP-ENV:Body>\n</SOAP-ENV:Envelope>\n";
	RESTMsg += msg;
	if( restful == null ) return SOAPMsg;
	else return RESTMsg;
    }

    public static String wsdlAnswer() { return wsdlSpec; }

    private Parameters getParameters( Document doc ){
	Parameters params = new BasicParameters();
	XPath path = XPathFactory.newInstance().newXPath();
	try {
	    XPathExpression expr = path.compile("//Envelope/Body/*");
	    NodeList result = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
	    // Believe it or not, NodeList has no iterator!
	    for (int i = 0; i < result.getLength(); i++) {
		Node item = result.item(i);
		// Check getNodeType() please
		String key = item.getNodeName();
		if ( key != null ) {
		    String val = item.getTextContent().trim();
		    if ( key.equals("param") ) {
			key = item.getAttributes().getNamedItem("name").getNodeValue();
		    }
		    params.setParameter( key, val );
		}
	    }
	} catch (XPathExpressionException e) {
	  System.err.println( "[getParameters] XPath exception: should not occur");
	} catch (NullPointerException e) {
	  System.err.println( "[getParameters] NullPointerException: should not occur");
	}
	return params;
    }

    private String displayAnswer ( Message answer ) {
	return answer.SOAPString();
    }

    private String displayError( Message answer ) {
	return answer.SOAPString();
    }

    private int newId() { return localId++; }
}
