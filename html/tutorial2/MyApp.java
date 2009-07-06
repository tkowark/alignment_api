/*
 * $Id$
 *
 * Copyright (C) INRIA, 2009
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 */

// Alignment API classes
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Parameters;

// Alignment API implementation classes
import fr.inrialpes.exmo.align.impl.BasicParameters;
import fr.inrialpes.exmo.align.impl.ObjectAlignment;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.method.StringDistAlignment;
import fr.inrialpes.exmo.align.impl.renderer.OWLAxiomsRendererVisitor;
import fr.inrialpes.exmo.align.parser.AlignmentParser;

// Jena
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;

// Pellet

// IDDL
import fr.inrialpes.exmo.iddl.IDDLReasoner;
import fr.inrialpes.exmo.iddl.conf.Semantics;

// SAX standard classes
import org.xml.sax.SAXException;

// DOM Standard classes
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathConstants;

// Java standard classes
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.File;
import java.io.FileWriter;
import java.net.URI;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

/**
 * MyApp
 *
 * Takes two files as arguments and align them.
 */

public class MyApp {

    String RESTServ = "http://aserv.inrialpes.fr/rest/";

    public static void main( String[] args ) {
	new MyApp().run( args );
    }

    public void run( String[] args ) {
	String myId = "JETest";
	Alignment al = null;
	URI uri1 = null;
	URI uri2 = null;
	//	String u1 = "http://alignapi.gforge.inria.fr/tutorial/edu.mit.visus.bibtex.owl";
	//	String u2 = "http://alignapi.gforge.inria.fr/tutorial/myOnto.owl";
	String method = "fr.inrialpes.exmo.align.impl.method.StringDistAlignment";
	Parameters params = new BasicParameters();
	try {
	    uri1 = new URI( u1 );
	    uri2 = new URI( u2 );
	} catch (URISyntaxException use) { use.printStackTrace(); }

	// ***** First exercise: matching *****
	// Try to find an alignment between two ontologies from the server
	String found = getFromURLString( RESTServ+"find?onto1="+u1+"&onto2="+u2, false );
	// If there exists alignments, ask for the first one
	NodeList alset = extractFromResult( found, "//findResponse/alignmentList/alid[1]/text()", false );

	// Alternative: match the ontologies from the server
	if ( alset.getLength() == -1 ) {
	    // * tested (must add force = true)
	    //String match = getFromURLString( RESTServ+"match?onto1="+u1+"&onto2="+u2+"&method="+method+"&pretty="+myId+"&action=Match", true );
	    // This returns a URI
	}

	// Ask it
	String alid = alset.item(0).getNodeValue();
	found = getFromURLString( RESTServ+"retrieve?method=fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor&id="+alid, false );
	alset = extractFromResult( found, "//retrieveResponse/result/*[1]",true );

	// This code is heavy as hell
	String xmlString = null;
	try {
	    // Set up the output transformer
	    TransformerFactory transfac = TransformerFactory.newInstance();
	    Transformer trans = transfac.newTransformer();
	    trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
	    trans.setOutputProperty(OutputKeys.INDENT, "yes");

	    // Print the DOM node
	    StringWriter sw = new StringWriter();
	    DOMSource source = new DOMSource( alset.item(0) );
	    trans.transform(source, new StreamResult(sw));
	    xmlString = sw.toString();

	    //System.out.println(xmlString);
	} catch (TransformerException e) {
	    e.printStackTrace();
	}

	// Parse it (better passing to the SAXHandler)
	try {
	    AlignmentParser aparser = new AlignmentParser(0);
	    Alignment alu = aparser.parseString( xmlString );
	    al = ObjectAlignment.toObjectAlignment((URIAlignment)alu);
	} catch (ParserConfigurationException pce) { 
	    pce.printStackTrace();
	} catch (SAXException saxe) { 
	    saxe.printStackTrace(); 
	} catch (IOException ioe) { 
	    ioe.printStackTrace();
	} catch (AlignmentException ae) { 
	    ae.printStackTrace();
	}

	if ( al == null ){ // Unfortunatelly no alignment was available
	    // Match the ontologies with a local algorithm
	    AlignmentProcess ap = new StringDistAlignment();
	    try {
		ap.init( uri1, uri2 );
		params.setParameter("stringFunction","smoaDistance");
		ap.align( (Alignment)null, params );
		al = ap;
	    } catch (AlignmentException ae) { ae.printStackTrace(); }
	}

	// Alternative: find an intermediate ontology between which there is matching + Compose it!

	// ***** Second exercise: merging/transforming *****

	// generate a merged ontology between the ontologies (OWLAxioms)
	// @@@@@@@@@@@@@@@@TODO => Set in a file...
	PrintWriter writer = null;
	File merged = new File( "/tmp/myresult.owl" );
	try {
	    writer = new PrintWriter ( new FileWriter( merged, false ), true );
	    AlignmentVisitor renderer = new OWLAxiomsRendererVisitor(writer);
	    al.render(renderer);
	} catch (UnsupportedEncodingException uee) {
	    uee.printStackTrace();
	} catch (AlignmentException ae) {
	    ae.printStackTrace();
	} catch (IOException ioe) { 
	    ioe.printStackTrace();
	} finally {
	    if ( writer != null ) {
		writer.flush();
		writer.close();
	    }
	}

	// Alternative: import the data from one ontology into the other

	// ***** Third exercise: querying *****

	// Use SPARQL to answer queries
	// @@@@@@@@@@@@@@@@TOTEST
	InputStream in = null;
	QueryExecution qe = null;
	try {
	    // Open the bloggers RDF graph from the filesystem
	    in = new FileInputStream( merged );
	    
	    // Create an empty in-memory model and populate it from the graph
	    Model model = ModelFactory.createDefaultModel();
	    //Model model = ModelFactory.createMemModelMaker().createModel();
	    model.read(in,null); // null base URI, since model URIs are absolute
	    in.close();
	
	    // Create a new query
	    String queryString = 
		"PREFIX foaf: <http://xmlns.com/foaf/0.1/> " +
		"SELECT ?url " +
		"WHERE {" +
		"      ?contributor foaf:name \"Jon Foobar\" . " +
		"      ?contributor foaf:weblog ?url . " +
		"      }";

	    Query query = QueryFactory.create(queryString);

	    // Execute the query and obtain results
	    qe = QueryExecutionFactory.create(query, model);
	    ResultSet results = qe.execSelect();

	    // Output query results	
	    ResultSetFormatter.out(System.out, results, query);
	} catch (FileNotFoundException fnfe) {
	    fnfe.printStackTrace();
	} catch (IOException ioe) {
	    ioe.printStackTrace();
	} finally {
	    // Important - free up resources used running the query
	    if ( qe != null ) qe.close();
	}

	// Alternative: Use Pellet to answer queries
	// @@@@@@@@@@@@@@@@TODO
	
	// ***** Fourth exercise: reasoning *****
	PelletReasoner reasoner = new PelletReasoner();
	OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	// should not be a file
	OWLOntology ontology = manager.loadOntology( merged );
	OWLAxiom axiom = df.getOWLSubClassAxiom( headache, pain );
	/*
	// test consistency of aligned ontologies
	IDDLReasoner reasoner = new IDDLReasoner( Semantics.DL );
	reasoner.addOntology( uri1 );
	reasoner.addOntology( uri2 );
	reasoner.addAlignment( align1 );
	// What to do if not consistent?
	if ( reasoner.isConsistent() ) {
	    Alignment al2 = new URIAlignment();
	    al2.init( uri1, uri2 );
	    // add the cell
	    //al2.addAlignCell( c2.getObject1(), c2.getObject2(), c2.getRelation().getRelation(), 1. );
	    reasoner.isEntailed( al2 );
         } else {
	System.err.println( "your alignment is inconsistent");
    }
	*/
	// 

    }

    public String getFromURLString( String u, boolean print ){
	URL url = null;
	String result = "<?xml version='1.0'?>";
	try {
	    url = new URL( u );
	    BufferedReader in = new BufferedReader(
    				new InputStreamReader(
    				  url.openStream()));
	    String inputLine;
	    while ((inputLine = in.readLine()) != null) {
		if (print) System.out.println(inputLine);
		result += inputLine;
	    }
	    in.close();
	}
	catch ( MalformedURLException mue ) { mue.printStackTrace(); }
	catch ( IOException mue ) { mue.printStackTrace(); }
	return result;
    }

    public NodeList extractFromResult( String found, String path, boolean print ){
	Document document = null;
	NodeList nodes = null;
	try { // Parse the returned stringAS XML
	    DocumentBuilder parser =
		DocumentBuilderFactory.newInstance().newDocumentBuilder();
	    document = parser.parse(new ByteArrayInputStream( found.getBytes() ));
	} catch ( ParserConfigurationException pce ) { pce.printStackTrace(); }
	catch ( SAXException se ) { se.printStackTrace(); }
	catch ( IOException ioe ) { ioe.printStackTrace(); }

	try { // Apply the Xpath expression
	    XPathFactory factory = XPathFactory.newInstance();
	    XPath xpath = factory.newXPath();
	    //XPathExpression expr = xpath.compile("//book[author='Neal Stephenson']/title/text()");
	    XPathExpression expr = xpath.compile( path );
	    Object result = expr.evaluate( document, XPathConstants.NODESET );
	    nodes = (NodeList)result;
	    if ( print ) {
		for ( int i = 0; i < nodes.getLength(); i++) {
		    System.out.println(nodes.item(i).getNodeValue()); 
		}
	    }
	} catch (XPathExpressionException xpee) { xpee.printStackTrace(); }
	return nodes;
    }
}
