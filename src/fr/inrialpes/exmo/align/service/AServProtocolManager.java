/*
 * $Id$
 *
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

package fr.inrialpes.exmo.align.service;

import fr.inrialpes.exmo.align.parser.AlignmentParser;

import org.semanticweb.owl.align.Parameters;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.AlignmentException;

import org.semanticweb.owl.util.OWLManager;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.io.owl_rdf.OWLRDFParser;
import org.semanticweb.owl.io.owl_rdf.OWLRDFErrorHandler;
import org.semanticweb.owl.io.ParserException;

import org.xml.sax.SAXException;
import java.sql.SQLException;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.util.Hashtable;
import java.util.Set;
import java.util.HashSet;
import java.util.Enumeration;
import java.util.Iterator;

public class AServProtocolManager {

    Hashtable renderers = null; // language -> class
    Hashtable methods = null; // name -> class
    Hashtable services = null; // name -> service
    CacheImpl alignmentCache = null;

    Hashtable loadedOntologies = null;
    OWLRDFErrorHandler handler = null;

    // This should be stored somewhere
    int localId = 0; // surrogate of emitted messages
    String myId = null; // id of this alignment server

    /*********************************************************************
     * Initialization and constructor
     *********************************************************************/

    public AServProtocolManager () {
	renderers = new Hashtable();
	methods = new Hashtable();
	services = new Hashtable();
    }

    public void init( DBService connection, Parameters p ) throws SQLException {
	alignmentCache = new CacheImpl( connection );
	alignmentCache.init();
	// dummy string
	myId = "localhost://alignserv";
	// This is ugly but it seems that Java does not provides the 
	// opportunity to find the loaded implementations of an interface!
	// Read all these parameters from the database
	methods.put("Name equality","fr.inrialpes.exmo.align.impl.method.NameEqAlignment");
	methods.put("SMOA","fr.inrialpes.exmo.align.impl.method.SMOANameAlignment");
	methods.put("String distance","fr.inrialpes.exmo.align.impl.method.StringDistAlignment");
	renderers.put("COWL","fr.inrialpes.exmo.align.impl.renderer.COWLMappingRendererVisitor");
	renderers.put("HTML","fr.inrialpes.exmo.align.impl.renderer.HTMLRendererVisitor");
	renderers.put("OWL","fr.inrialpes.exmo.align.impl.renderer.OWLAxiomsRendererVisitor");
	renderers.put("RDF/XML","fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor");
	renderers.put("SEKT/OWMG","fr.inrialpes.exmo.align.impl.renderer.SEKTMappingRendererVisitor");
	renderers.put("SKOS","fr.inrialpes.exmo.align.impl.renderer.SKOSRendererVisitor");
	renderers.put("SWRL","fr.inrialpes.exmo.align.impl.renderer.SWRLRendererVisitor");
	renderers.put("XSLT","fr.inrialpes.exmo.align.impl.renderer.XSLTRendererVisitor");
	loadedOntologies = new Hashtable();
	handler = new OWLRDFErrorHandler() {
		public void owlFullConstruct(int code, String message)
		    throws SAXException {
		}
		public void owlFullConstruct(int code, String message, Object o)
		    throws SAXException {
		}
		public void error(String message) throws SAXException {
		    throw new SAXException(message.toString());
		}
		public void warning(String message) throws SAXException {
		    System.err.println("WARNING: " + message);
		}
	    };
    }

    public void close() {
    }

    private int newId() { return localId++; }

    /*********************************************************************
     * Extra administration primitives
     *********************************************************************/

    // DONE BUT UNSATISFACTORY
    public Set listmethods (){
	Set result = new HashSet();
	for (Enumeration e = methods.elements() ; e.hasMoreElements() ;) {
	    result.add(e.nextElement());
	}
	return result;
    }

    // DONE BUT UNSATISFACTORY
    public Set listrenderers(){
	Set result = new HashSet();
	for (Enumeration e = renderers.elements() ; e.hasMoreElements() ;) {
	    result.add(e.nextElement());
	}
	return result;
    }

    // DONE
    public Enumeration alignments(){
	return alignmentCache.listAlignments();
    }

    /*********************************************************************
     * Basic protocol primitives
     *********************************************************************/

    // DONE
    // Implements: store (different from store below)
    public Message load( Message mess ){
	Parameters params = mess.getParameters();
	// load the alignment
	String name = (String)params.getParameter("url");
	System.err.println("Preparing for "+name);
	Alignment init = null;
	try {
	    //if (debug > 0) System.err.println(" Parsing init");
	    AlignmentParser aparser = new AlignmentParser(0);
	    init = aparser.parse( name, loadedOntologies );
	    //if (debug > 0) System.err.println(" Init parsed");
	} catch (Exception e) {
	    return new UnreachableAlignment(newId(),mess,myId,mess.getSender(),name,(Parameters)null);
	}
	System.err.println("For recording");
	// register it
	String id = alignmentCache.recordNewAlignment( init, true );
	System.err.println("For returning");
	return new AlignmentId(newId(),mess,myId,mess.getSender(),id,(Parameters)null);
    }

    // DONE
    // Implements: align
    public Message align(Message mess){
	Parameters params = mess.getParameters();
	String method = (String)params.getParameter("method");
	// find and access o, o'
	URI uri1 = null;
	URI uri2 = null;
	try {
	    uri1 = new URI((String)params.getParameter("onto1"));
	    uri2 = new URI((String)params.getParameter("onto2"));
	} catch (Exception e) {}; //done below
	OWLOntology onto1 = reachable( uri1 );
	OWLOntology onto2 = reachable( uri2 );
	if ( onto1 == null ){
	    // Unreachable
	    return new UnreachableOntology(newId(),mess,myId,mess.getSender(),(String)params.getParameter("onto1"),(Parameters)null);
	} else if ( onto2 == null ){
	    return new UnreachableOntology(newId(),mess,myId,mess.getSender(),(String)params.getParameter("onto2"),(Parameters)null);
	} else {
	    // find n
	    Alignment init = null;
	    if ( params.getParameter("init") != null && !params.getParameter("init").equals("") ) {
		try {
		    //if (debug > 0) System.err.println(" Retrieving init");
		    //AlignmentParser aparser = new AlignmentParser(0);
		    //init = aparser.parse((String)params.getParameter("init"), loadedOntologies);
		    try {
			init = alignmentCache.getAlignment( (String)params.getParameter("init") );
		    } catch (Exception e) {
			return new UnknownAlignment(newId(),mess,myId,mess.getSender(),(String)params.getParameter("init"),(Parameters)null);
		    }
		    // Not really useful
		    onto1 = (OWLOntology)init.getOntology1();
		    onto2 = (OWLOntology)init.getOntology2();
		    //if (debug > 0) System.err.println(" Init retrieved");
		} catch (Exception e) {
		    return new UnknownAlignment(newId(),mess,myId,mess.getSender(),(String)params.getParameter("init"),(Parameters)null);
		}
	    }
	    // Try to retrieve first
	    Set alignments = null;
	    try {
		alignments = alignmentCache.getAlignments( onto1.getLogicalURI(), onto2.getLogicalURI() );
	    } catch (OWLException e) {
		// Unexpected OWLException!
	    }
	    String id = null;
	    if ( alignments != null ) {
		for ( Iterator it = alignments.iterator(); it.hasNext() && (id == null); ){
		    Alignment al = ((Alignment)it.next());
		    if ( al.getExtension( "method" ).equals(method) )
			id = al.getExtension( "id" );
		}
	    }
	    // Otherwise compute
	    if ( id == null ){
		// Create alignment object
		try {
		    Object[] mparams = {(Object)onto1, (Object)onto2 };
		    if ( method == null )
			method = "fr.inrialpes.exmo.align.impl.method.StringDistAlignment";
		    Class alignmentClass = Class.forName(method);
		    Class oClass = Class.forName("org.semanticweb.owl.model.OWLOntology");
		    Class[] cparams = { oClass, oClass };
		    java.lang.reflect.Constructor alignmentConstructor = alignmentClass.getConstructor(cparams);
		    AlignmentProcess result = (AlignmentProcess)alignmentConstructor.newInstance(mparams);
		    result.setFile1(uri1);
		    result.setFile2(uri2);
		    // call alignment algorithm
		    long time = System.currentTimeMillis();
		    result.align(init, params); // add opts
		    long newTime = System.currentTimeMillis();
		    result.setExtension( "time", Long.toString(newTime - time) );
		    // ask to store A'
		    id = alignmentCache.recordNewAlignment( result, true );
		} catch (Exception e) {
		    return new NonConformParameters(newId(),mess,myId,mess.getSender(),"nonconform/params/",(Parameters)null);
		}
	    }
	    // JE: In non OWL-API-based version, here unload ontologies
	    // return A' surrogate
	    return new AlignmentId(newId(),mess,myId,mess.getSender(),id,(Parameters)null);
	}
    }

    // DONE
    // Implements: query-aligned
    public Message existingAlignments( Message mess ){
	Parameters params = mess.getParameters();
	// find and access o, o'
	URI uri1 = null;
	URI uri2 = null;
	try {
	    uri1 = new URI((String)params.getParameter("onto1"));
	    uri2 = new URI((String)params.getParameter("onto2"));
	} catch (Exception e) {
	    return new ErrorMsg(newId(),mess,myId,mess.getSender(),"MalformedURI problem",(Parameters)null);
	}; //done below
	// This useless and is done only for launching the error
	//OWLOntology onto1 = reachable( uri1 );
	//OWLOntology onto2 = reachable( uri2 );
	//if ( onto1 == null ){
	    // Unreachable
	//    return new UnreachableOntology(newId(),mess,myId,mess.getSender(),(String)params.getParameter("onto1"),(Parameters)null);
	//} else if ( onto2 == null ){
	//    return new UnreachableOntology(newId(),mess,myId,mess.getSender(),(String)params.getParameter("onto2"),(Parameters)null);
	//} else {
	//    try {
		// find it
	//	Set alignments = alignmentCache.getAlignments( onto1.getLogicalURI(), onto2.getLogicalURI() );
		Set alignments = alignmentCache.getAlignments( uri1, uri2 );
		String msg = "";
		for( Iterator it = alignments.iterator(); it.hasNext(); ){
		    //ids.put( ((Alignment)it.next()).getExtension( "id" ) );
		    msg += ((Alignment)it.next()).getExtension( "id" );
		    msg += " ";
		}
		return new AlignmentIds(newId(),mess,myId,mess.getSender(),msg,(Parameters)null);
		//    } catch (Exception e) { // URI problems in gLU
		//e.printStackTrace();
		//return new ErrorMsg(newId(),mess,myId,mess.getSender(),"getlogicalURI problem on the ontologies",(Parameters)null);
		//}
		//}
    }

    // ABSOLUTELY NOT IMPLEMENTED
    // Implements: find
    public Message find(Message mess){
    //\prul{search-success}{a - request ( find (O, T) ) \rightarrow S}{O' \Leftarrow Match(O,T)\\S - inform (O') \rightarrow a}{reachable(O)\wedge Match(O,T)\not=\emptyset}

    //\prul{search-void}{a - request ( find (O, T) ) \rightarrow S}{S - failure (nomatch) \rightarrow a}{reachable(O)\wedge Match(O,T)=\emptyset}

    //\prul{search-unreachable}{a - request ( find (O, T) ) \rightarrow S}{S - failure ( unreachable (O) ) \rightarrow a}{\neg reachable(O)}
	return new OntologyURI(newId(),mess,myId,mess.getSender(),"dummy//",(Parameters)null);
    }

    // Implements: translate
    public Message translate(Message mess){

//\prul{translate-success}{a - request ( translate ( M, n)) \rightarrow S}{\langle O, O', A\rangle \Leftarrow Retrieve(n)\\m'\Leftarrow Translate(m,A)\\S - inform ( m' ) \rightarrow a}{Retrieve(n)\not=\emptyset}

//\prul{translate-unknown}{a - request ( translate ( M, n)) \rightarrow S}{S - failure ( unknown (n) )  \rightarrow a}{Retrieve(n)=\emptyset}
	return new TranslatedMessage(newId(),mess,myId,mess.getSender(),"dummy//",(Parameters)null);
    }

    // DONE
    // Implements: render
    public Message render( Message mess ){
	Parameters params = mess.getParameters();
	// Retrieve the alignment
	String id = (String)params.getParameter("id");
	Alignment al = null;
	try {
	    al = alignmentCache.getAlignment( id );
	} catch (Exception e) {
	    return new UnknownAlignment(newId(),mess,myId,mess.getSender(),id,(Parameters)null);
	}
	// Render it
	String method = (String)params.getParameter("method");
	AlignmentVisitor renderer = null;
	// Redirect the output in a String
	ByteArrayOutputStream result = new ByteArrayOutputStream(); 
	PrintWriter writer = null;
	try { 
	    writer = new PrintWriter (
			  new BufferedWriter(
			       new OutputStreamWriter( result, "UTF-8" )), true);
	    try {
		Object[] mparams = {(Object) writer };
		java.lang.reflect.Constructor[] rendererConstructors =
		    Class.forName(method).getConstructors();
		renderer =
		    (AlignmentVisitor) rendererConstructors[0].newInstance(mparams);
	    } catch (Exception ex) {
		// should return the message
		return new UnknownMethod(newId(),mess,myId,mess.getSender(),method,(Parameters)null);
	    }
	    al.render(renderer);
	    writer.flush();
	    writer.close();
	} catch (Exception e) {
	    // These are exceptions related to I/O
	    writer.flush();
	    writer.close();
	    System.err.println(result.toString());
	    e.printStackTrace();
	}

	return new RenderedAlignment(newId(),mess,myId,mess.getSender(),result.toString(),(Parameters)null);
    }

    /*********************************************************************
     * Extended protocol primitives
     *********************************************************************/

    // DONE
    // Implementation specific
    public Message store( Message mess ){
	String id = mess.getContent();
	try {
	    alignmentCache.storeAlignment( id );
	} catch (Exception e) {
	    return new UnknownAlignment(newId(),mess,myId,mess.getSender(),id,(Parameters)null);
	}
	return new AlignmentId(newId(),mess,myId,mess.getSender(),id,(Parameters)null);
    }

    public Message getmetadata(Message mess){

    //\prul{get-processor-success}{a - request ( metadata ( n )) \rightarrow S}{\langle O, O', A\rangle \Leftarrow Retrieve(n)\\P\Leftarrow Metadata(A)\\S - inform ( P~language:~l ) \rightarrow a}{Retrieve(n)\not=\emptyset}

    //\prul{get-processor-unknown}{a - request ( metadata ( n )) \rightarrow S}{S - failure ( unknown (n) ) \rightarrow a}{Retrieve(n)=\emptyset}

	return new RenderedAlignment(newId(),mess,myId,mess.getSender(),"dummy//",(Parameters)null);
    }

    /*********************************************************************
     * Extra alignment primitives
     *
     * All these primitives must create a new alignment and return its Id
     * There is no way an alignment server could modify an alignment
     *********************************************************************/

    public Message cut( Message mess ){
	return new AlignmentId(newId(),mess,myId,mess.getSender(),"dummy//",(Parameters)null);
    }

    public Message harden( Message mess ){
	return new AlignmentId(newId(),mess,myId,mess.getSender(),"dummy//",(Parameters)null);
    }

    public Message inverse( Message mess ){
	Parameters params = mess.getParameters();
	// Retrieve the alignment
	String id = (String)params.getParameter("id");
	Alignment al = null;
	try {
	    al = alignmentCache.getAlignment( id );
	} catch (Exception e) {
	    return new UnknownAlignment(newId(),mess,myId,mess.getSender(),"unknown/Alignment/"+id,(Parameters)null);
	}
	if ( params.getParameter("id") == null ){
	    // Copy the alignment
	}
	// Invert it
	try { al.inverse(); }
	catch (AlignmentException e) {
	    return new ErrorMsg(newId(),mess,myId,mess.getSender(),"dummy//",(Parameters)null);
	}
	return new AlignmentId(newId(),mess,myId,mess.getSender(),"dummy//",(Parameters)null);
    }

    public Message meet( Message mess ){
	// Retrieve alignments
	return new AlignmentId(newId(),mess,myId,mess.getSender(),"dummy//",(Parameters)null);
    }

    public Message join( Message mess ){
	// Retrieve alignments
	return new AlignmentId(newId(),mess,myId,mess.getSender(),"dummy//",(Parameters)null);
    }

    public Message compose( Message mess ){
	// Retrieve alignments
	return new AlignmentId(newId(),mess,myId,mess.getSender(),"dummy//",(Parameters)null);
    }

    /*********************************************************************
     * Network of alignment server implementation
     *********************************************************************/

    // Implements: reply-with
    public Message replywith(Message mess){

    //\prul{redirect}{a - request ( q(x)~reply-with:~i) \rightarrow S}{
    //Q \Leftarrow Q\cup\{\langle a, i, !i', q(x), S'\rangle\}\		\
    //S - request( q( R(x) )~reply-with:~i')\rightarrow S'}{S'\in C(q)}
	return new Message(newId(),mess,myId,mess.getSender(),"dummy//",(Parameters)null);
    }

    // Implements: reply-to
    public Message replyto(Message mess){

    //\prul{handle-return}{S' - inform ( y~reply-to:~i') \rightarrow S}{
    //Q \Leftarrow Q-\{\langle a, i, i', _, S'\rangle\}\		\
    //S - inform( R^{-1}(y)~reply-to:~i)\rightarrow a}{\langle a, i, i', _, S'\rangle \in Q, \neg surr(y)}

    //\prul{handle-return}{S' - inform ( y~reply-to:~i') \rightarrow S}{
    //Q \Leftarrow Q-\{\langle a, i, i', _, S'\rangle\}\	\
    //R \Leftarrow R\cup\{\langle a, !y', y, S'\rangle\}\		\
    //S - inform( R^{-1}(y)~reply-to:~i)\rightarrow a}{\langle a, i, i', _, S'\rangle \in Q, surr(y)}
	return new Message(newId(),mess,myId,mess.getSender(),"dummy//",(Parameters)null);
    }

    // Implements: failure
    public Message failure(Message mess){

    //\prul{failure-return}{S' - failure ( y~reply-to:~i') \rightarrow S}{
    //Q \Leftarrow Q-\{\langle a, i, i', _, S'\rangle\}\		\
    //S - failure( R^{-1}(y)~reply-to:~i)\rightarrow a}{\langle a, i, i', _, S'\rangle \in Q}
	return new Message(newId(),mess,myId,mess.getSender(),"dummy//",(Parameters)null);
    }

    /*********************************************************************
     * Utilities
     *********************************************************************/

    public OWLOntology reachable( URI uri ){
	try { return loadOntology( uri ); }
	catch (Exception e) {
	    return (OWLOntology)null;
	}
    }

    public OWLOntology loadOntology(URI uri)
	throws ParserException, OWLException {
	// Test if not loaded...
	OWLOntology parsedOnt = null;
	OWLRDFParser parser = new OWLRDFParser();
	parser.setOWLRDFErrorHandler(handler);
	parser.setConnection(OWLManager.getOWLConnection());
	parsedOnt = parser.parseOntology(uri);
	loadedOntologies.put(uri.toString(), parsedOnt);
	return parsedOnt;
    }

    // this should be done at some point...
    private void unloadOntology( OWLOntology o ){
    }

}
