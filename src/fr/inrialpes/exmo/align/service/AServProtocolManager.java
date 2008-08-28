/*
 * $Id$
 *
 * Copyright (C) INRIA Rhône-Alpes, 2006-2008
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
import fr.inrialpes.exmo.align.impl.Annotations;
import fr.inrialpes.exmo.align.impl.BasicParameters;
import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.align.impl.ObjectAlignment;
import fr.inrialpes.exmo.align.onto.OntologyFactory;
import fr.inrialpes.exmo.align.onto.OntologyCache;
import fr.inrialpes.exmo.align.onto.Ontology;
import fr.inrialpes.exmo.align.onto.LoadedOntology;

import org.semanticweb.owl.align.Parameters;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Evaluator;

import java.sql.SQLException;

import java.lang.ClassNotFoundException;
import java.lang.InstantiationException;
import java.lang.NoSuchMethodException;
import java.lang.IllegalAccessException;
import java.lang.NullPointerException;
import java.lang.UnsatisfiedLinkError;
import java.lang.ExceptionInInitializerError;
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
    Parameters commandLineParams = null;
    Set<String> renderers = null;
    Set<String> methods = null;
    Set<String> services = null;
    Set<String> evaluators = null;

    OntologyCache loadedOntologies = null;
    Hashtable<String,Directory> directories = null;

    // This should be stored somewhere
    int localId = 0; // surrogate of emitted messages
    String myId = null; // id of this alignment server

    /*********************************************************************
     * Initialization and constructor
     *********************************************************************/

    public AServProtocolManager ( Hashtable<String,Directory> dir ) {
	directories = dir;
    }

    public void init( DBService connection, Parameters p ) throws SQLException, AlignmentException {
	alignmentCache = new CacheImpl( connection );
	commandLineParams = p;
	alignmentCache.init( p );
	myId = "http://"+p.getParameter("host")+":"+p.getParameter("http");
	renderers = implementations( "org.semanticweb.owl.align.AlignmentVisitor" );
	methods = implementations( "org.semanticweb.owl.align.AlignmentProcess" );
	methods.remove("fr.inrialpes.exmo.align.impl.DistanceAlignment"); // this one is generic
	services = implementations( "fr.inrialpes.exmo.align.service.AlignmentServiceProfile" );
	evaluators = implementations( "org.semanticweb.owl.align.Evaluator" );
	loadedOntologies = new OntologyCache();
    }

    public void close() {
	try { alignmentCache.close(); }
	catch (SQLException sqle) { sqle.printStackTrace(); }
    }

    public void reset() {
	try {
	    alignmentCache.reset();
	} catch (SQLException sqle) { sqle.printStackTrace(); }
    }

    public void flush() {
	alignmentCache.flushCache();
    }

    public void shutdown() {
	try { 
	    alignmentCache.close();
	    System.exit(0);
	} catch (SQLException sqle) { sqle.printStackTrace(); }
    }

    private int newId() { return localId++; }

    /*********************************************************************
     * Extra administration primitives
     *********************************************************************/

    public Set<String> listmethods (){
	return methods;
    }

    public Set<String> listrenderers(){
	return renderers;
    }

    public Set<String> listservices(){
	return services;
    }

    public Enumeration alignments(){
	return alignmentCache.listAlignments();
    }

    public String query( String query ){
	//return alignmentCache.query( query );
	return "Not available yet";
    }

    public String serverURL(){
	return myId;
    }

   /*********************************************************************
     * Basic protocol primitives
     *********************************************************************/

    // DONE
    // Implements: store (different from store below)
    public Message load( Message mess ) {
	boolean todiscard = false;
	Parameters params = mess.getParameters();
	// load the alignment
	String name = (String)params.getParameter("url");
	String file = null;
	if ( name == null || name.equals("") ){
	    file  = (String)params.getParameter("filename");
	    if ( file != null && !file.equals("") ) name = "file://"+file;
	}
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
	// if the file has been uploaded: discard it
	if ( init != null && file != null ) {
	    // try unlink
	}
	return new AlignmentId(newId(),mess,myId,mess.getSender(),id,(Parameters)null);
    }

    // Implements: align
    public Message align( Message mess ){
	Message result = null;
	Parameters p = mess.getParameters();
	/*
	  // JE: This remains here for historical reasons.
	  // It is a threat to security since it used to unveil to all interfaces
	  // database parameters!
	  for (Enumeration<String> e = commandLineParams.getNames(); e.hasMoreElements();) {
	    String key = e.nextElement();
	    if ( p.getParameter( key ) == null ){
		p.setParameter( key , commandLineParams.getParameter( key ) );
	    }
	}
	*/
	// Do the fast part (retrieve)
	result = retrieveAlignment( mess );
	if ( result != null ) return result;
	String id = alignmentCache.generateAlignmentId();

	Aligner althread = new Aligner( mess, id );
	Thread th = new Thread(althread);
	// Do the slow part (align)
	if ( mess.getParameters().getParameter("async") != null ) {
	    th.start();
	    // Parameters are used
	    return new AlignmentId(newId(),mess,myId,mess.getSender(),id,mess.getParameters());
	} else {
	    th.start();
	    try{ th.join(); }
	    catch ( InterruptedException is ) {};
	    return althread.getResult();
	}
    }

    private Message retrieveAlignment( Message mess ){
	Parameters params = mess.getParameters();
	String method = (String)params.getParameter("method");
	// find and access o, o'
	URI uri1 = null;
	URI uri2 = null;
	Ontology onto1 = null;
	Ontology onto2 = null;
	try {
	    uri1 = new URI((String)params.getParameter("onto1"));
	    uri2 = new URI((String)params.getParameter("onto2"));
	} catch (Exception e) {
	    return new NonConformParameters(newId(),mess,myId,mess.getSender(),"nonconform/params/onto",(Parameters)null);
	};
	if ( ( onto1 = reachable( uri1 ) ) == null ){
	    return new UnreachableOntology(newId(),mess,myId,mess.getSender(),(String)params.getParameter("onto1"),(Parameters)null);
	} else if ( ( onto2 = reachable( uri2 ) ) == null ){
	    return new UnreachableOntology(newId(),mess,myId,mess.getSender(),(String)params.getParameter("onto2"),(Parameters)null);
	}
	// Try to retrieve first
	Set alignments = alignmentCache.getAlignments( onto1.getURI(), onto2.getURI() );
	if ( alignments != null && params.getParameter("force") == null ) {
	    for ( Iterator it = alignments.iterator(); it.hasNext() ; ){
		Alignment al = ((Alignment)it.next());
		if ( al.getExtension( Annotations.ALIGNNS, Annotations.METHOD ).equals(method) )
		    return new AlignmentId(newId(),mess,myId,mess.getSender(),al.getExtension( Annotations.ALIGNNS, Annotations.ID ),(Parameters)null);

	    }
	}
	return (Message)null;
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
	    msg += ((Alignment)it.next()).getExtension( Annotations.ALIGNNS, Annotations.ID );
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
	    try {
		renderer.init( params );
		al.render( renderer );
	    } catch ( AlignmentException aex ) {
		al = ObjectAlignment.toObjectAlignment( (URIAlignment)al, (OntologyCache)null );
		al.render( renderer );
	    }
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

    // Implementation specific
    public Message store( Message mess ){
	String id = mess.getContent();
	try {
	    Alignment al = alignmentCache.getAlignment( id );
	    // Be sure it is not already stored
	    if ( !alignmentCache.isAlignmentStored( al ) ){
		alignmentCache.storeAlignment( id );
		// Retrieve the alignment again
		al = alignmentCache.getAlignment( id );
		// for all directories...
		for ( Directory d : directories.values() ){
		    // Declare the alignment in the directory
		    try { d.register( al ); }
		    catch (AServException e) { e.printStackTrace(); }// ignore
		}
	    }
	    // register by them
	    // Could also be an AlreadyStoredAlignment error
	    return new AlignmentId(newId(),mess,myId,mess.getSender(),id,(Parameters)null);
	} catch (Exception e) {
	    return new UnknownAlignment(newId(),mess,myId,mess.getSender(),id,(Parameters)null);
	}
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
	params.setParameter( Annotations.ALIGNNS+"level", al.getLevel() );
	params.setParameter( Annotations.ALIGNNS+"type", al.getType() );
	for ( Object ext : ((BasicParameters)al.getExtensions()).getValues() ){
	    params.setParameter( ((String[])ext)[0]+((String[])ext)[1], ((String[])ext)[2] );
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
	if ( method == null ) method = "hard";
	double threshold = Double.parseDouble((String)mess.getParameters().getParameter("threshold"));
	al = (BasicAlignment)((BasicAlignment)al).clone();
	try { al.cut( method, threshold );}
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

	// Invert it
	try { al = al.inverse(); }
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

    // It is also possible to try a groupeval ~> with a zipfile containing results
    //            ~~> But it is more difficut to know where is the reference (non public)
    // There should also be options for selecting the result display
    //            ~~> PRGraph (but this may be a Evaluator)
    //            ~~> Triangle
    //            ~~> Cross
    public Message eval( Message mess ){
	Parameters params = mess.getParameters();
	// Retrieve the alignment
	String id = (String)params.getParameter("id");
	Alignment al = null;
	try {
	    al = alignmentCache.getAlignment( id );
	} catch (Exception e) {
	    return new UnknownAlignment(newId(),mess,myId,mess.getSender(),"unknown/Alignment/"+id,(Parameters)null);
	}
	// Retrieve the reference alignment
	String rid = (String)params.getParameter("ref");
	Alignment ref = null;
	try {
	    ref = alignmentCache.getAlignment( rid );
	} catch (Exception e) {
	    return new UnknownAlignment(newId(),mess,myId,mess.getSender(),"unknown/Alignment/"+rid,(Parameters)null);
	}
	// Set the comparison method
	String classname = (String)params.getParameter("method");
	if ( classname == null ) classname = "fr.inrialpes.exmo.align.impl.eval.PRecEvaluator";
	Evaluator eval = null;
	try {
	    Object [] mparams = {(Object)ref, (Object)al};
	    Class oClass = Class.forName("org.semanticweb.owl.align.Alignment");
	    Class[] cparams = { oClass, oClass };
	    Class evaluatorClass =  Class.forName(classname);
	    java.lang.reflect.Constructor evaluatorConstructor = evaluatorClass.getConstructor(cparams);
	    eval = (Evaluator)evaluatorConstructor.newInstance(mparams);
	} catch (Exception ex) {
	    return new ErrorMsg(newId(),mess,myId,mess.getSender(),"dummy//",(Parameters)null);
	}
	// Compare it
	try { eval.eval(params); }
	catch (AlignmentException e) {
	    return new ErrorMsg(newId(),mess,myId,mess.getSender(),"dummy//",(Parameters)null);
	}
	// Return it, not easy
	// Should be evaluation results...
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
	if ( al.getExtension(CacheImpl.SVCNS, CacheImpl.STORED) != null && al.getExtension(CacheImpl.SVCNS, CacheImpl.STORED) != "" ) {
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

    public LoadedOntology reachable( URI uri ){
	try { 
	    OntologyFactory factory = OntologyFactory.newInstance();
	    return factory.loadOntology( uri, loadedOntologies );
	} catch (Exception e) { return null; }
    }

    /*********************************************************************
     * Utilities: Finding the implementation of an interface
     *********************************************************************/

    public static void implementations( Class tosubclass, Set<String> list , boolean debug ){
	Set<String> visited = new HashSet<String>();
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
			//System.err.println("DIR "+file);
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
				    // JE: Here there is a bug that is that it is not possible
				    // to have ALL interfaces with this function!!!
				    // This is really stupid but that's life
				    // So it is compulsory that AlignmentProcess be declared 
				    // as implemented
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
				} catch (ExceptionInInitializerError eiie) {
				    // This one has been added for OMWG, this is a bad error
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
				    // Beware, in a Jarfile the separator is always "/"
				    // and it would not be dependent on the current system anyway.
				    //classname = classname.replaceAll(File.separator,".");
				    classname = classname.replaceAll("/",".");
				    try {
					if ( classname.equals("org.apache.xalan.extensions.ExtensionHandlerGeneral") ) throw new ClassNotFoundException( "Stupid JAVA/Xalan bug");
					Class cl = Class.forName(classname);
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
				    } catch (ExceptionInInitializerError eiie) {
				    // This one has been added for OMWG, this is a bad error
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
				//classPath += File.pathSeparator+file.getParent()+File.separator + path.replaceAll("[ \t]+",File.pathSeparator+file.getParent()+File.separator);
				// This replaces the replaceAll which is not tolerant on Windows in having "\" as a separator
				for( StringTokenizer token = new StringTokenizer(path," \t"); token.hasMoreTokens(); )
				    classPath += File.pathSeparator+file.getParent()+File.separator+token.nextToken();
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
    public static Set<String> implementations( String interfaceName ) {
	Set<String> list = new HashSet<String>();
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

    protected class Aligner implements Runnable {
	private Message mess = null;
	private Message result = null;
	private String id = null;

	public Aligner( Message m, String id ) {
	    mess = m;
	    this.id = id;
	}

	public Message getResult() {
	    return result;
	}

	public void run() {
	    Parameters params = mess.getParameters();
	    String method = (String)params.getParameter("method");
	    // find and access o, o'
	    URI uri1 = null;
	    URI uri2 = null;

	    try {
		uri1 = new URI((String)params.getParameter("onto1"));
		uri2 = new URI((String)params.getParameter("onto2"));
	    } catch (Exception e) {
		result = new NonConformParameters(newId(),mess,myId,mess.getSender(),"nonconform/params/onto",(Parameters)null);
		return;
	    };
	    // The unreachability test has already been done

	    // find initial alignment
	    Alignment init = null;
	    if ( params.getParameter("init") != null && !params.getParameter("init").equals("") ) {
		try {
		    //if (debug > 0) System.err.println(" Retrieving init");
		    try {
			init = alignmentCache.getAlignment( (String)params.getParameter("init") );
		} catch (Exception e) {
			result = new UnknownAlignment(newId(),mess,myId,mess.getSender(),(String)params.getParameter("init"),(Parameters)null);
			return;
		    }
		} catch (Exception e) {
		    result = new UnknownAlignment(newId(),mess,myId,mess.getSender(),(String)params.getParameter("init"),(Parameters)null);
		    return;
		}
	    }
	    
	    // Create alignment object
	    try {
		Object[] mparams = {};
		if ( method == null )
		    method = "fr.inrialpes.exmo.align.impl.method.StringDistAlignment";
		Class alignmentClass = Class.forName(method);
		Class[] cparams = {};
		java.lang.reflect.Constructor alignmentConstructor = alignmentClass.getConstructor(cparams);
		AlignmentProcess aresult = (AlignmentProcess)alignmentConstructor.newInstance(mparams);
		try {
		    aresult.init( uri1, uri2, loadedOntologies );
		    long time = System.currentTimeMillis();
		    aresult.align( init, params ); // add opts
		    long newTime = System.currentTimeMillis();
		    aresult.setExtension( Annotations.ALIGNNS, Annotations.TIME, Long.toString(newTime - time) );
		} catch (AlignmentException e) {
		    result = new NonConformParameters(newId(),mess,myId,mess.getSender(),"nonconform/params/"+e.getMessage(),(Parameters)null);
		    return;
		}
		// ask to store A'
		alignmentCache.recordNewAlignment( id, aresult, true );
	    } catch (ClassNotFoundException e) {
		result = new RunTimeError(newId(),mess,myId,mess.getSender(),"Class not found: "+method,(Parameters)null);
	    } catch (NoSuchMethodException e) {
		result = new RunTimeError(newId(),mess,myId,mess.getSender(),"No such method: "+method+"(Object, Object)",(Parameters)null);
	    } catch (InstantiationException e) {
		result = new RunTimeError(newId(),mess,myId,mess.getSender(),"Instantiation",(Parameters)null);
	    } catch (IllegalAccessException e) {
		result = new RunTimeError(newId(),mess,myId,mess.getSender(),"Cannot access",(Parameters)null);
	    } catch (InvocationTargetException e) {
		result = new RunTimeError(newId(),mess,myId,mess.getSender(),"Invocation target",(Parameters)null);
	    } catch (AlignmentException e) {
		result = new NonConformParameters(newId(),mess,myId,mess.getSender(),"nonconform/params/",(Parameters)null);
	    } catch (Exception e) {
		result = new RunTimeError(newId(),mess,myId,mess.getSender(),"Unexpected exception (wrong class name?)",(Parameters)null);
	    }
	    loadedOntologies.clear(); // not always necessary
	    result = new AlignmentId(newId(),mess,myId,mess.getSender(),id,(Parameters)null);
	}
    }

    
}
