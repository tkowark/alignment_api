/*
 * $Id: WSAlignment.java 756 2008-07-17 15:30:07Z euzenat $
 *
 * Copyright (C) INRIA Rhône-Alpes, 2008
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

package fr.inrialpes.exmo.align.service;

import java.lang.ClassNotFoundException;
import java.util.Hashtable;
import java.util.HashSet;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.io.File;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.HttpURLConnection;
import java.net.ProtocolException;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.Relation;
import org.semanticweb.owl.align.Parameters;

import fr.inrialpes.exmo.align.onto.Ontology;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.align.impl.URICell;
import fr.inrialpes.exmo.align.impl.BasicParameters;
import fr.inrialpes.exmo.align.impl.Annotations;
import fr.inrialpes.exmo.align.parser.AlignmentParser;

 /**
  * Represents an ontology alignment relating entities identified by their URIs
  *
  * @author Jérôme Euzenat
  * @version $Id$
  */

public class WSAlignment extends URIAlignment implements AlignmentProcess {

     /*
     // JE: so far this is the code of URIAlignment...
     // So it can be discarded, we only need to store the ontologies
     public void init(Object o1, Object o2) throws AlignmentException {
	 if ( o1 instanceof Ontology && o2 instanceof Ontology ){
	     super.init( o1, o2 );
	 } else if ( o1 instanceof URI && o2 instanceof URI ) {
	     super.init( o1, o2 );
	     this.onto1.setURI( (URI)o1 );
	     this.onto2.setURI( (URI)o2 );
	 } else {
	     throw new AlignmentException("arguments must be URIs");
	 };
     }
     */

     private URL SOAPUrl = null;
     /**
      * The address of the web service (URL).
      * This can be overridden in subclasses or in parameters
      */
     private String serviceAddress = null;

     /**
      * Process matching
      * This does not work with regular AServ web service because it returns an URL
      **/
     public void align( Alignment alignment, Parameters params ) throws AlignmentException {
	 // Create the invokation message
	 if ( params.getParameter("wserver") != null )
	     serviceAddress = (String)params.getParameter("wserver");
	 try {
	     SOAPUrl = new URL( serviceAddress );
	 } catch (IOException ioex) {
	     throw new AlignmentException("Malformed service address");
	 }
	 String message = "<SOAP-ENV:Envelope\n   xmlns='http://exmo.inrialpes.fr/align/service'\n   xml:base='http://exmo.inrialpes.fr/align/service'\n    xmlns:SOAP-ENV=\'http://schemas.xmlsoap.org/soap/envelope/\'\n " +
					   "xmlns:xsi=\'http://www.w3.org/1999/XMLSchema-instance\'\n " + 
					   "xmlns:xsd=\'http://www.w3.org/1999/XMLSchema\'>\n" +
					   "  <SOAP-ENV:Body>\n";
	 // URI encoding
	 String uri1 = getOntology1URI().toString();
	 String uri2 = getOntology2URI().toString();
	 if ( uri1 == null || uri2 == null ){
	     throw new AlignmentException("Missing URIs");
	 }
	 message += "<url1>"+uri1+"</url1><url2>"+uri2+"</url2>";
	 // Parameter encoding
	 for (Enumeration e = params.getNames(); e.hasMoreElements();) {
	     String k = (String)e.nextElement();
	     System.out.println("  <"+k+">"+params.getParameter(k)+"</"+k+">");
	 }

	 message += "  </SOAP-ENV:Body>\n"+"</SOAP-ENV:Envelope>\n";
	 byte[] byteMess = message.getBytes();

	 // Connect with the web service (in parameter)
	 HttpURLConnection httpConn = null;
	 try {
	     httpConn = (HttpURLConnection)SOAPUrl.openConnection();
	     
	     // Create HTTP Request
	     httpConn.setRequestProperty( "Content-Length",
					  String.valueOf( byteMess.length ) );
	     httpConn.setRequestProperty("Content-Type","text/xml; charset=utf-8");
	     httpConn.setRequestProperty("SOAPAction","align");
	     httpConn.setRequestMethod( "POST" );
	     httpConn.setDoOutput(true);
	     httpConn.setDoInput(true);
	 } catch (ProtocolException pex) {
	     throw new AlignmentException("Cannot connect");
	 } catch (IOException ioex) {
	     throw new AlignmentException("Cannot connect");
	 }

	 // Send the request through the connection
	 try {
	     OutputStream out = httpConn.getOutputStream();
	     out.write( byteMess );    
	     out.close();
	 } catch (IOException ex) {
	     throw new AlignmentException("Cannot write");
	 }

	 // Get the result
	 /*
	 String answer = "";
	 try {
	     InputStreamReader isr = new InputStreamReader(httpConn.getInputStream());
	     BufferedReader in = new BufferedReader(isr);
	     String line;
	     while ((line = in.readLine()) != null) {
		 answer += line + "\n";
	     }
	     if (in != null) in.close();
	 } catch (IOException ex) {
	     throw new AlignmentException("Cannot read");
	 }
	 */

	 // Parse the result in this alignment
	 try {
	     AlignmentParser parser = new AlignmentParser( 0 );
	     parser.initAlignment( this );
	     parser.setEmbedded( true );
	     //parser.parseString( answer );
	     parser.parse( httpConn.getInputStream() );
	 } catch (SAXException saxex) {
	     throw new AlignmentException( "Malformed XML/SOAP result", saxex );
	 } catch (IOException ioex) {
	     throw new AlignmentException( "XML/SOAP parsing error", ioex );
	 } catch (javax.xml.parsers.ParserConfigurationException pcex) {
	     throw new AlignmentException( "XML/SOAP parsing error", pcex );
	 }
     }

     /*
    public void setOntology1(Object ontology) throws AlignmentException {
	if ( ontology instanceof URI || ontology instanceof Ontology ){
	    super.setOntology1( ontology );
	} else {
	    throw new AlignmentException("arguments must be URIs");
	};
    };

    public void setOntology2(Object ontology) throws AlignmentException {
	if ( ontology instanceof URI || ontology instanceof Ontology ){
	    super.setOntology2( ontology );
	} else {
	    throw new AlignmentException("arguments must be URIs");
	};
    };
*/

    /** Cell methods **/
     /*
    // JE: so far this is the code of URIAlignment...
    public Cell addAlignCell(String id, Object ob1, Object ob2, Relation relation, double measure) throws AlignmentException {
        if ( !( ob1 instanceof URI && ob2 instanceof URI ) )
	    throw new AlignmentException("arguments must be URIs");

	return super.addAlignCell( id, ob1, ob2, relation, measure);
    };
    // JE: so far this is the code of URIAlignment...
    public Cell addAlignCell(Object ob1, Object ob2, String relation, double measure) throws AlignmentException {
 
        if ( !( ob1 instanceof URI && ob2 instanceof URI ) )
	    throw new AlignmentException("arguments must be URIs");

	return super.addAlignCell( ob1, ob2, relation, measure);
    };
    // JE: so far this is the code of URIAlignment...
    public Cell addAlignCell(Object ob1, Object ob2) throws AlignmentException {
 
        if ( !( ob1 instanceof URI && ob2 instanceof URI ) )
	    throw new AlignmentException("arguments must be URIs");

	return super.addAlignCell( ob1, ob2 );
    };
    // JE: so far this is the code of URIAlignment...
    public Cell createCell(String id, Object ob1, Object ob2, Relation relation, double measure) throws AlignmentException {
	return (Cell)new URICell( id, (URI)ob1, (URI)ob2, relation, measure );
    }
     */

    /**
     * Generate a copy of this alignment object
     */
    // JE: this is a mere copy of the method in BasicAlignement...
    // Should be usefull to have a better way to do it [28/7/2008: DO IT]
    public Object clone() {
	WSAlignment align = new WSAlignment();
	try { align.init( (URI)getOntology1(), (URI)getOntology2() ); }
	catch ( AlignmentException e ) {};
	align.setType( getType() );
	align.setLevel( getLevel() );
	align.setFile1( getFile1() );
	align.setFile2( getFile2() );
	for ( Object ext : ((BasicParameters)extensions).getValues() ){
	    align.setExtension( ((String[])ext)[0], ((String[])ext)[1], ((String[])ext)[2] );
	    }
	String oldid = align.getExtension( Annotations.ALIGNNS, "id" );
	if ( oldid != null && !oldid.equals("") ) {
	    align.setExtension( Annotations.ALIGNNS, "derivedFrom", oldid );
	    align.getExtensions().unsetParameter( Annotations.ALIGNNS+"id" );
	}
	align.setExtension( Annotations.ALIGNNS, "method", "http://exmo.inrialpes.fr/align/impl/URIAlignment#clone" );
	try {
	    align.ingest( this );
	} catch (AlignmentException ex) { ex.printStackTrace(); }
	return align;
    }

}

