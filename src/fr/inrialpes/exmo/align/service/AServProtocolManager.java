/*
 * $Id$
 *
 * Copyright (C) INRIA Rhône-Alpes, 2006-2007
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
 *
 */

package fr.inrialpes.exmo.align.service;

import fr.inrialpes.exmo.align.parser.AlignmentParser;
import fr.inrialpes.exmo.align.impl.BasicParameters;
import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.OntologyCache;

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

import java.lang.ClassNotFoundException;
import java.lang.InstantiationException;
import java.lang.NoSuchMethodException;
import java.lang.IllegalAccessException;
import java.lang.NullPointerException;
import java.lang.UnsatisfiedLinkError;
import java.lang.reflect.InvocationTargetException;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.net.JarURLConnection;
import java.util.Hashtable;
import java.util.Set;
import java.util.HashSet;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.jar.Attributes.Name;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * This is the main class that control the behaviour of the Alignment Server
 * It is as independent from the OWL API as possible.
 * However, it is still necessary to test for the reachability of an ontology and moreover to resolve its URI for that of its source.
 * For these reasons we still need a parser of OWL files here.
 */

public class AServProtocolManager {

    CacheImpl alignmentCache = null;
    Set renderers = null;
    Set methods = null;
    Set services = null;

    OntologyCache loadedOntologies = null;
    OWLRDFErrorHandler handler = null;

    // This should be stored somewhere
    int localId = 0; // surrogate of emitted messages
    String myId = null; // id of this alignment server

    /*********************************************************************
     * Initialization and constructor
     *********************************************************************/

    public AServProtocolManager () {
    }

    public void init( DBService connection, Parameters p ) throws SQLException {
	alignmentCache = new CacheImpl( connection );
	alignmentCache.init( p );
	// dummy string
	myId = "http://"+p.getParameter("host")+":"+p.getParameter("port");
	renderers = implementations( "org.semanticweb.owl.align.AlignmentVisitor" );
	methods = implementations( "org.semanticweb.owl.align.AlignmentProcess" );
	methods.remove("fr.inrialpes.exmo.align.impl.DistanceAlignment"); // this one is generic
	services = implementations( "fr.inrialpes.exmo.align.service.AlignmentServiceProfile" );
	loadedOntologies = new OntologyCache();
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
	try { alignmentCache.close(); }
	catch (SQLException sqle) { sqle.printStackTrace(); }
    }

    private int newId() { return localId++; }

    /*********************************************************************
     * Extra administration primitives
     *********************************************************************/

    // DONE
    public Set listmethods (){
	return methods;
    }

    // DONE
    public Set listrenderers(){
	return renderers;
    }

    // DONE
    public Set listservices(){
	return services;
    }

    // DONE
    public Enumeration alignments(){
	return alignmentCache.listAlignments();
    }

    public String query( String query ){
	//return alignmentCache.query( query );
	return "Not available yet";
    }

   /*********************************************************************
     * Basic protocol primitives
     *********************************************************************/

    // DONE
    // Implements: store (different from store below)
    public Message load( Message mess ) {
	Parameters params = mess.getParameters();
	// load the alignment
	String name = (String)params.getParameter("url");
	//if (debgug > 0) System.err.println("Preparing for "+name);
	Alignment init = null;
	try {
	    //if (debug > 0) System.err.println(" Parsing init");
	    AlignmentParser aparser = new AlignmentParser(0);
	    init = aparser.parse( name );
	    //if (debug > 0) System.err.println(" Init parsed");
	} catch (Exception e) {
	    return new UnreachableAlignment(newId(),mess,myId,mess.getSender(),name,(Parameters)null);
	}
	// register it
	String id = alignmentCache.recordNewAlignment( init, true );
	return new AlignmentId(newId(),mess,myId,mess.getSender(),id,(Parameters)null);
    }

    public Message loadfile( Message mess ) {
	Parameters params = mess.getParameters();
	// the alignment content is within the parameters
	// ?? JE: rather in the content
	Alignment al = null;
	try {
	    //if (debug > 0) System.err.println(" Parsing init");
	    AlignmentParser aparser = new AlignmentParser(0);
	    al = aparser.parseString( mess.getContent() );
	    //if (debug > 0) System.err.println(" Init parsed");
	} catch (Exception e) {
	    // Maybe not this message
	    // And (String)null may not be the best idea...
	    return new UnreachableAlignment(newId(),mess,myId,mess.getSender(),(String)null,(Parameters)null);
	}
	// register it
	String id = alignmentCache.recordNewAlignment( al, true );
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
	OWLOntology onto1 = null;
	OWLOntology onto2 = null;
	try {
	    uri1 = new URI((String)params.getParameter("onto1"));
	    uri2 = new URI((String)params.getParameter("onto2"));
	} catch (Exception e) {
	    return new NonConformParameters(newId(),mess,myId,mess.getSender(),"nonconform/params/onto",(Parameters)null);
	};
	// find n
	Alignment init = null;
	if ( params.getParameter("init") != null && !params.getParameter("init").equals("") ) {
	    try {
		//if (debug > 0) System.err.println(" Retrieving init");
		try {
		    init = alignmentCache.getAlignment( (String)params.getParameter("init") );
		} catch (Exception e) {
		    return new UnknownAlignment(newId(),mess,myId,mess.getSender(),(String)params.getParameter("init"),(Parameters)null);
		}
	    } catch (Exception e) {
		return new UnknownAlignment(newId(),mess,myId,mess.getSender(),(String)params.getParameter("init"),(Parameters)null);
	    }
	}
	if ( ( onto1 = reachable( uri1 ) ) == null ){
	    return new UnreachableOntology(newId(),mess,myId,mess.getSender(),(String)params.getParameter("onto1"),(Parameters)null);
	} else if ( ( onto2 = reachable( uri2 ) ) == null ){
	    return new UnreachableOntology(newId(),mess,myId,mess.getSender(),(String)params.getParameter("onto2"),(Parameters)null);
	}

	// Try to retrieve first
	Set alignments = null;
	try {
	    // This is OWLAPI specific but there is no other way...
	    alignments = alignmentCache.getAlignments( onto1.getLogicalURI(), onto2.getLogicalURI() );
	} catch (OWLException e) {
	    // Unexpected OWLException!
	}
	String id = null;
	if ( alignments != null && params.getParameter("force") == null ) {
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
		//Object[] mparams = {(Object)onto1, (Object)onto2 };
		Object[] mparams = {};
		if ( method == null )
		    method = "fr.inrialpes.exmo.align.impl.method.StringDistAlignment";
		Class alignmentClass = Class.forName(method);
		Class[] cparams = {};
		java.lang.reflect.Constructor alignmentConstructor = alignmentClass.getConstructor(cparams);
		AlignmentProcess result = (AlignmentProcess)alignmentConstructor.newInstance(mparams);
		result.init( uri1, uri2, loadedOntologies );
		//result.setFile1(uri1);
		//result.setFile2(uri2);
		// call alignment algorithm
		long time = System.currentTimeMillis();
		try {
		    result.align( init, params ); // add opts

		} catch (AlignmentException e) {
			return new NonConformParameters(newId(),mess,myId,mess.getSender(),"nonconform/params/",(Parameters)null);
		}
		long newTime = System.currentTimeMillis();
		result.setExtension( "time", Long.toString(newTime - time) );
		// ask to store A'
		id = alignmentCache.recordNewAlignment( result, true );
	    } catch (ClassNotFoundException e) {
		return new RunTimeError(newId(),mess,myId,mess.getSender(),"Class not found: "+method,(Parameters)null);
	    } catch (NoSuchMethodException e) {
		return new RunTimeError(newId(),mess,myId,mess.getSender(),"No such method: "+method+"(Object, Object)",(Parameters)null);
	    } catch (InstantiationException e) {
		return new RunTimeError(newId(),mess,myId,mess.getSender(),"Instantiation",(Parameters)null);
	    } catch (IllegalAccessException e) {
		return new RunTimeError(newId(),mess,myId,mess.getSender(),"Cannot access",(Parameters)null);
	    } catch (InvocationTargetException e) {
		return new RunTimeError(newId(),mess,myId,mess.getSender(),"Invocation target",(Parameters)null);
	    } catch (AlignmentException e) {
		return new NonConformParameters(newId(),mess,myId,mess.getSender(),"nonconform/params/",(Parameters)null);
	    }
	}

	// JE: In non OWL-API-based version, here unload ontologies
	loadedOntologies.clear(); // not always necessary
	// return A' surrogate
	return new AlignmentId(newId(),mess,myId,mess.getSender(),id,(Parameters)null);
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
	Set alignments = alignmentCache.getAlignments( uri1, uri2 );
	String msg = "";
	for( Iterator it = alignments.iterator(); it.hasNext(); ){
	    msg += ((Alignment)it.next()).getExtension( "id" );
	    msg += " ";
	}
	return new AlignmentIds(newId(),mess,myId,mess.getSender(),msg,(Parameters)null);
    }

    // ABSOLUTELY NOT IMPLEMENTED
    // Implements: find
    // This may be useful when calling WATSON
    public Message find(Message mess){
    //\prul{search-success}{a - request ( find (O, T) ) \rightarrow S}{O' \Leftarrow Match(O,T)\\S - inform (O') \rightarrow a}{reachable(O)\wedge Match(O,T)\not=\emptyset}

    //\prul{search-void}{a - request ( find (O, T) ) \rightarrow S}{S - failure (nomatch) \rightarrow a}{reachable(O)\wedge Match(O,T)=\emptyset}

    //\prul{search-unreachable}{a - request ( find (O, T) ) \rightarrow S}{S - failure ( unreachable (O) ) \rightarrow a}{\neg reachable(O)}
	return new OntologyURI(newId(),mess,myId,mess.getSender(),"dummy//",(Parameters)null);
    }

    // Implements: translate
    // This should be applied to many more kind of messages with different kind of translation
    public Message translate(Message mess){
	Parameters params = mess.getParameters();
	// Retrieve the alignment
	String id = (String)params.getParameter("id");
	Alignment al = null;
	try {
	    al = alignmentCache.getAlignment( id );
	} catch (Exception e) {
	    return new UnknownAlignment(newId(),mess,myId,mess.getSender(),id,(Parameters)null);
	}
	// Translate the query
	try {
	    String translation = QueryMediator.rewriteSPARQLQuery( (String)params.getParameter("query"), al );
	    return new TranslatedMessage(newId(),mess,myId,mess.getSender(),translation,(Parameters)null);
	} catch (AlignmentException e) {
	    return new ErrorMsg(newId(),mess,myId,mess.getSender(),e.toString(),(Parameters)null);
	}
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
	    // Strange that I do not catch the AlignmentException raised when OWL is needed
	    writer.flush();
	    writer.close();
	} catch (AlignmentException e) {
	    writer.flush();
	    writer.close();
	    return new UnknownMethod(newId(),mess,myId,mess.getSender(),method,(Parameters)null);
	} catch (Exception e) { // These are exceptions related to I/O
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

    /*
     * Returns only the metadata of an alignment and returns it in 
     * parameters
     */
    public Message metadata( Message mess ){
	// Retrieve the alignment
	String id = (String)mess.getParameters().getParameter("id");
	Alignment al = null;
	try {
	    al = alignmentCache.getMetadata( id );
	} catch (Exception e) {
	    return new UnknownAlignment(newId(),mess,myId,mess.getSender(),id,(Parameters)null);
	}
	// JE: Other possibility is to render the metadata through XMLMetadataRendererVisitor into content...
	// Put all the local metadata in parameters
	Parameters params = new BasicParameters();
	params.setParameter( "file1", al.getFile1() );
	params.setParameter( "file2", al.getFile2() );
	params.setParameter( "level", al.getLevel() );
	params.setParameter( "type", al.getType() );
	Parameters extensions = al.getExtensions();
	for ( Enumeration e = extensions.getNames(); e.hasMoreElements(); ){
	    String name = (String)e.nextElement();
	    params.setParameter( name, extensions.getParameter( name ) );
	}
	return new AlignmentMetadata(newId(),mess,myId,mess.getSender(),id,params);
    }

    /*********************************************************************
     * Extra alignment primitives
     *
     * All these primitives must create a new alignment and return its Id
     * There is no way an alignment server could modify an alignment
     *********************************************************************/

    public Message cut( Message mess ) {
	// Retrieve the alignment
	String id = (String)mess.getParameters().getParameter("id");
	Alignment al = null;
	try {
	    al = alignmentCache.getAlignment( id );
	} catch (Exception e) {
	    return new UnknownAlignment(newId(),mess,myId,mess.getSender(),id,(Parameters)null);
	}
	// get the cut parameters
	String method = (String)mess.getParameters().getParameter("method");
	double threshold = Double.parseDouble((String)mess.getParameters().getParameter("threshold"));
	al = (BasicAlignment)((BasicAlignment)al).clone();
	try { al.cut( method, threshold ); }
	catch (AlignmentException e) {
	    return new ErrorMsg(newId(),mess,myId,mess.getSender(),"dummy//",(Parameters)null);
	}
	String newId = alignmentCache.recordNewAlignment( al, true );
	return new AlignmentId(newId(),mess,myId,mess.getSender(),newId,(Parameters)null);
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
	//try { al = al.clone().inverse(); }
	try { al.inverse(); }
	catch (AlignmentException e) {
	    return new ErrorMsg(newId(),mess,myId,mess.getSender(),"dummy//",(Parameters)null);
	}
	String newId = alignmentCache.recordNewAlignment( al, true );
	return new AlignmentId(newId(),mess,myId,mess.getSender(),newId,(Parameters)null);
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

    public boolean storedAlignment( Message mess ) {
	// Retrieve the alignment
	String id = (String)mess.getParameters().getParameter("id");
	Alignment al = null;
	try {
	    al = alignmentCache.getAlignment( id );
	} catch (Exception e) {
	    return false;
	}
	if ( al.getExtension(CacheImpl.STORED) != null && al.getExtension(CacheImpl.STORED) != "" ) {
	    return true;
	} else {
	    return false;
	}
    }

    /*********************************************************************
     * Network of alignment server implementation
     *********************************************************************/

    /**
     * Ideal network implementation protocol:
     *
     * - publication (to some directory)
     * registerID
     * publishServices
     * unregisterID
     * (publishRenderer)
     * (publishMethods) : can be retrieved through the classical interface.
     *  requires a direcory
     *
     * - subscribe style
     * subscribe() : ask to receive new metadata
     * notify( metadata ) : send new metadata to subscriber
     * unsubscribe() :
     * update( metadata ) : update some modification
     *   requires to store the subscribers
     *
     * - query style: this is the classical protocol that can be done through WSDL
     * getMetadata()
     * getAlignment()
     *   requires to store the node that can be 
     */

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
     * Utilities: reaching and loading ontologies
     *********************************************************************/

    public OWLOntology reachable( URI uri ){
	try { return loadOntology( uri ); }
	catch (Exception e) {
	    e.printStackTrace();
	    return (OWLOntology)null;
	}
    }

    public OWLOntology loadOntology( URI uri ) throws ParserException, OWLException {
	// Test if not loaded...
	OWLOntology parsedOnt = null;
	OWLRDFParser parser = new OWLRDFParser();
	parser.setOWLRDFErrorHandler(handler);
	parser.setConnection(OWLManager.getOWLConnection());
	parsedOnt = parser.parseOntology( uri );
	if ( loadedOntologies != null )
	    loadedOntologies.recordOntology( uri, parsedOnt );
	return parsedOnt;
    }

    /*********************************************************************
     * Utilities: Finding the implementation of an interface
     *********************************************************************/

    public static void implementations( Class tosubclass, Set list , boolean debug ){
	Set<String> visited = new HashSet();
	String classPath = System.getProperty("java.class.path",".");
	// Hack: this is not necessary
	//classPath = classPath.substring(0,classPath.lastIndexOf(File.pathSeparatorChar));
	if ( debug ) System.err.println(classPath);
	StringTokenizer tk = new StringTokenizer(classPath,File.pathSeparator);
	classPath = "";
	while ( tk != null && tk.hasMoreTokens() ){
	    StringTokenizer tk2 = tk;
	    tk = null;
	    // Iterate on Classpath
	    while ( tk2.hasMoreTokens() ) {
		try {
		    File file = new File( tk2.nextToken() );
		    if ( file.isDirectory() ) {
			System.err.println("DIR "+file);
			String subs[] = file.list();
			for(int index = 0 ; index < subs.length ; index ++ ){
			    if ( debug ) System.err.println("    "+subs[index]);
			    // IF class
			    if ( subs[index].endsWith(".class") ) {
				String classname = subs[index].substring(0,subs[index].length()-6);
				if (classname.startsWith(File.separator)) 
				    classname = classname.substring(1);
				classname = classname.replace(File.separatorChar,'.');
				try {
				    Class[] cls = Class.forName(classname).getInterfaces();
				    for ( int i=0; i < cls.length ; i++ ){
					if ( cls[i] == tosubclass ) {
					    if (debug ) System.err.println(" -j-> "+classname);
					    list.add( classname );
					}
					if ( debug ) System.err.println("       I> "+cls[i] );
				    }
				    // Not one of our classes
				} catch ( NoClassDefFoundError ncdex ) {
				} catch (ClassNotFoundException cnfex) {
				} catch (UnsatisfiedLinkError ule) {
				}
			    }
			}
		    } else if ( file.toString().endsWith(".jar") &&
				!visited.contains( file.toString() ) &&
				file.exists() ) {
			if ( debug ) System.err.println("JAR "+file);
			visited.add( file.toString() );
			try { 
			    JarFile jar = new JarFile( file );
			    Enumeration enumeration = jar.entries();
			    while( enumeration.hasMoreElements() ){
				String classname = enumeration.nextElement().toString();
				if ( debug ) System.err.println("    "+classname);
				int len = classname.length()-6;
				if( len > 0 && classname.substring(len).compareTo(".class") == 0) {
				    classname = classname.substring(0,len);
				    classname = classname.replaceAll(File.separator,".");
				    try {
					if ( classname.equals("org.apache.xalan.extensions.ExtensionHandlerGeneral") ) throw new ClassNotFoundException( "Stupid JAVA/Xalan bug");
					Class cl = Class.forName(classname);
					//Class cl = Class.forName(classname);
					Class[] ints = cl.getInterfaces();
					for ( int i=0; i < ints.length ; i++ ){
					    if ( ints[i] == tosubclass ) {
						if (debug ) System.err.println(" -j-> "+classname);
						list.add( classname );
					    }
					    if ( debug ) System.err.println("       I> "+ints[i] );
					}
				    } catch ( NoClassDefFoundError ncdex ) {
				    } catch ( ClassNotFoundException cnfex ) {
					if ( debug ) System.err.println("   ******** "+classname);
				    } catch ( UnsatisfiedLinkError ule ) {
				    }
				}
			    }
			    // Iterate on needed Jarfiles
			    // JE(caveat): this deals naively with Jar files,
			    // in particular it does not deal with section'ed MANISFESTs
			    Attributes mainAttributes = jar.getManifest().getMainAttributes();
			    String path = mainAttributes.getValue( Name.CLASS_PATH );
			    if ( debug ) System.err.println("  >CP> "+path);
			    if ( path != null && !path.equals("") ) {
				// JE: Not sure where to find the other Jars:
				// in the path or at the local place?
				classPath += File.pathSeparator + path.replaceAll("[ \t]+",File.pathSeparator+file.getParent()+File.separator);
			    }
			} catch (NullPointerException nullexp) { //Raised by JarFile
			    System.err.println("Warning "+file+" unavailable");
			}
		    }
		} catch( IOException e ) {
		    continue;
		}
	    }
	    if ( !classPath.equals("") ) {
		tk =  new StringTokenizer(classPath,File.pathSeparator);
		classPath = "";
	    }
	}
    }

    /**
     * Display all the classes inheriting or implementing a given
     * interface in the currently loaded packages.
     * @param interfaceName the name of the interface to implement
     */
    public static Set implementations( String interfaceName ) {
	Set list = new HashSet();
	try {
	    Class toclass = Class.forName(interfaceName);
	    //Package [] pcks = Package.getPackages();
	    //for (int i=0;i<pcks.length;i++) {
		//System.err.println(interfaceName+ ">> "+pcks[i].getName() );
		//implementations( pcks[i].getName(), toclass, list );
		//}
	    implementations( toclass, list, false );
	} catch (ClassNotFoundException ex) {
	    System.err.println("Class "+interfaceName+" not found!");
	}
	return list;
    }

    
}
