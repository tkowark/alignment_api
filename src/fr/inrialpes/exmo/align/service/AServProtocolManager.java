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
 * A small part of this class (within the implementations() method) is:
 * Copyright (C) Daniel Le Berre, 2001
 * from his util.RTSI class
 * which contains a few lines:
 * Copyright (C) 2002-2005, FullSpan Software
 * under BSD licence
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of FullSpan Software nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
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
import java.lang.reflect.InvocationTargetException;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.net.URI;
import java.util.Hashtable;
import java.util.Set;
import java.util.HashSet;
import java.util.Enumeration;
import java.util.Iterator;

import java.io.File;
import java.net.URL;
import java.net.JarURLConnection;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.lang.UnsatisfiedLinkError;

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
	if ( alignments != null && params.getParameter("force") != null ) {
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
     * Utilities: Finding the subclasses of a class
     *********************************************************************/

    /**
     * Display all the classes inheriting or implementing a given
     * class in the currently loaded packages.
     * @param tosubclassname the name of the class to inherit from
     */
    public static Set implementations( String tosubclassname ) {
	Set list = new HashSet();
	try {
	    Class tosubclass = Class.forName(tosubclassname);
	    Package [] pcks = Package.getPackages();
	    for (int i=0;i<pcks.length;i++) {
		implementations( pcks[i].getName(), tosubclass, list );
	    }
	} catch (ClassNotFoundException ex) {
	    System.err.println("Class "+tosubclassname+" not found!");
	}
	return list;
    }

    /**
     * Display all the classes inheriting or implementing a given
     * class in a given package.
     * @param pckgname the fully qualified name of the package
     * @param tosubclass the Class object to inherit from
     */
    public static Set implementations( String pckgname, Class tosubclass, Set list ) {
	//if (debug > 0 ) System.err.println( "Searching in "+pckgname );
	
	// Code from JWhich
	// ======
	// Translate the package name into an absolute path
	String name = new String(pckgname);
	if (!name.startsWith("/")) {
	    name = "/" + name;
	}	
	name = name.replace('.','/');

  	// Get a File object for the package
  	//URL url = RTSI.class.getResource(name);
	URL url = tosubclass.getResource(name);
	// URL url = ClassLoader.getSystemClassLoader().getResource(name);
	//System.out.println(name+"->"+url);

	// Happens only if the jar file is not well constructed, i.e.
	// if the directories do not appear alone in the jar file like here:
	// 
	//          meta-inf/
	//          meta-inf/manifest.mf
	//          commands/                  <== IMPORTANT
	//          commands/Command.class
	//          commands/DoorClose.class
	//          commands/DoorLock.class
	//          commands/DoorOpen.class
	//          commands/LightOff.class
	//          commands/LightOn.class
	//          RTSI.class
	//
	if ( url != null ) {
	    File directory = new File(url.getFile());
	    if ( directory != null && directory.exists()) {
		// Get the list of the files contained in the package
		String [] files = directory.list();
		for (int i=0;i<files.length;i++) {
		    // we are only interested in .class files
		    if (files[i].endsWith(".class")) {
			// removes the .class extension
			String classname = files[i].substring(0,files[i].length()-6);
			try {
			    Class[] cls = Class.forName(pckgname+"."+classname).getInterfaces();
			    for ( int j=0; j < cls.length ; j++ ){
				if ( cls[j] == tosubclass ) {
				    //if (debug > 0 ) System.err.println(" -d-> "+pckgname+"."+classname );
				    list.add( pckgname+"."+classname );
				}
			    }
			// Not one of our classes
			} catch (ClassNotFoundException cnfex) {
			} catch (UnsatisfiedLinkError ule) {
			}
		    }
		}
	    } else {
		try {
		    // It does not work with the filesystem: we must
		    // be in the case of a package contained in a jar file.
		    JarURLConnection conn = (JarURLConnection)url.openConnection();
		    String starts = conn.getEntryName();
		    JarFile jfile = conn.getJarFile();
		    Enumeration e = jfile.entries();
		    while (e.hasMoreElements()) {
			ZipEntry entry = (ZipEntry)e.nextElement();
			String entryname = entry.getName();
			if (entryname.startsWith(starts)
			    //JE: suppressing this line
			    // Without it, id does not follow subdirs
			    //&& (entryname.lastIndexOf('/')<=starts.length())
			    && entryname.endsWith(".class")) {
			    String classname = entryname.substring(0,entryname.length()-6);
			    if (classname.startsWith("/")) 
				classname = classname.substring(1);
			    classname = classname.replace('/','.');
			    try {
				Class[] cls = Class.forName(classname).getInterfaces();
				for ( int i=0; i < cls.length ; i++ ){
				    if ( cls[i] == tosubclass ) {
					//if (debug > 0 ) System.err.println(" -j-> "+classname);
					list.add( classname );
				    }
				}
			    // Not one of our classes
			    } catch ( NoClassDefFoundError ncdex ) {
			    } catch (ClassNotFoundException cnfex) {
			    } catch (UnsatisfiedLinkError ule) {
			    }
			}
		    }
		} catch (IOException ioex) {
		    ioex.printStackTrace();
		}	
	    }
	}
	return list;
    }
    
}
