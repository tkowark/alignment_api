/*
 * $id: AServProtocolManager.java 1902 2014-03-17 19:39:04Z euzenat $
 *
 * Copyright (C) INRIA, 2006-2014
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
import fr.inrialpes.exmo.align.impl.BasicOntologyNetwork;
import fr.inrialpes.exmo.align.impl.Namespace;
import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.align.impl.ObjectAlignment;
import fr.inrialpes.exmo.align.impl.eval.DiffEvaluator;
import fr.inrialpes.exmo.align.impl.rel.EquivRelation;
import fr.inrialpes.exmo.align.service.osgi.Service;
import fr.inrialpes.exmo.align.service.msg.Message;
import fr.inrialpes.exmo.align.service.msg.AlignmentId;
import fr.inrialpes.exmo.align.service.msg.AlignmentIds;
import fr.inrialpes.exmo.align.service.msg.AlignmentMetadata;
import fr.inrialpes.exmo.align.service.msg.EntityList;
import fr.inrialpes.exmo.align.service.msg.EvalResult;
import fr.inrialpes.exmo.align.service.msg.OntologyNetworkId;
import fr.inrialpes.exmo.align.service.msg.OntologyURI;
import fr.inrialpes.exmo.align.service.msg.RenderedAlignment;
import fr.inrialpes.exmo.align.service.msg.TranslatedMessage;
import fr.inrialpes.exmo.align.service.msg.ErrorMsg;
import fr.inrialpes.exmo.align.service.msg.NonConformParameters;
import fr.inrialpes.exmo.align.service.msg.RunTimeError;
import fr.inrialpes.exmo.align.service.msg.UnknownAlignment;
import fr.inrialpes.exmo.align.service.msg.UnknownMethod;
import fr.inrialpes.exmo.align.service.msg.UnknownOntologyNetwork;
import fr.inrialpes.exmo.align.service.msg.UnreachableAlignment;
import fr.inrialpes.exmo.align.service.msg.UnreachableOntology;
import fr.inrialpes.exmo.align.service.msg.CannotRenderAlignment;
import fr.inrialpes.exmo.align.service.msg.UnreachableOntologyNetwork;
import fr.inrialpes.exmo.ontowrap.OntologyFactory;
import fr.inrialpes.exmo.ontowrap.Ontology;
import fr.inrialpes.exmo.ontowrap.LoadedOntology;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Evaluator;
import org.semanticweb.owl.align.OntologyNetwork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ClassNotFoundException;
import java.lang.InstantiationException;
import java.lang.NoSuchMethodException;
import java.lang.IllegalAccessException;
import java.lang.NullPointerException;
import java.lang.UnsatisfiedLinkError;
import java.lang.ExceptionInInitializerError;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.jar.Attributes.Name;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;
import java.util.zip.ZipEntry;

/**
 * This is the main class which controls the behaviour of the Alignment Server
 * It is as independent from the OWL API as possible.
 * However, it is still necessary to test for the reachability of an ontology and moreover to resolve its URI for that of its source.
 * For these reasons we still need a parser of OWL files here.
 */

public class AServProtocolManager implements Service {
    final static Logger logger = LoggerFactory.getLogger( AServProtocolManager.class );

    Cache alignmentCache = null;
    Properties commandLineParams = null;
    Set<String> renderers = null;
    Set<String> methods = null;
    Set<String> services = null;
    Set<String> evaluators = null;

    Hashtable<String,Directory> directories = null;

    // This should be stored somewhere
    int localId = 0; // surrogate of emitted messages
    String serverId = null; // id of this alignment server

    /*********************************************************************
     * Initialization and constructor
     *********************************************************************/

    public AServProtocolManager ( Hashtable<String,Directory> dir ) {
	directories = dir;
    }

    public void init( DBService connection, Properties prop ) throws AlignmentException {
	commandLineParams = prop;
	serverId = prop.getProperty("prefix");
	if ( serverId == null || serverId.equals("") )
	    serverId = "http://"+prop.getProperty("host")+":"+prop.getProperty("http");
	alignmentCache = new SQLCache( connection );
	alignmentCache.init( prop, serverId );
	renderers = implementations( "org.semanticweb.owl.align.AlignmentVisitor" );
	methods = implementations( "org.semanticweb.owl.align.AlignmentProcess" );
	methods.remove("fr.inrialpes.exmo.align.impl.DistanceAlignment"); // this one is generic, but not abstract
	services = implementations( "fr.inrialpes.exmo.align.service.AlignmentServiceProfile" );
	evaluators = implementations( "org.semanticweb.owl.align.Evaluator" );
    }

    public void close() {
	try { alignmentCache.close(); }
	catch (AlignmentException alex) { 
	    logger.trace( "IGNORED Exception", alex );
	}
    }

    public void reset() {
	try {
	    alignmentCache.reset();
	} catch (AlignmentException alex) {
	    logger.trace( "IGNORED Exception", alex );
	}
    }

    public void flush() {
	alignmentCache.flushCache();
    }

    public void shutdown() {
	try { 
	    alignmentCache.close();
	    System.exit(0);
	} catch (AlignmentException alex) {
	    logger.trace( "IGNORED Exception", alex );
	}
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

    public Set<String> listevaluators(){
	return evaluators;
    }

    /*
    public Enumeration alignments(){
	return alignmentCache.listAlignments();
    }
    */
    public Collection<Alignment> alignments() {
	return alignmentCache.alignments();
    }

    public Collection<URI> ontologies() {
	return alignmentCache.ontologies();
    }
    
    public Collection<URI> networkOntologyUri(String uri) {
    	OntologyNetwork noo = null;
		try {
			noo = alignmentCache.getOntologyNetwork(uri);
		} catch (AlignmentException e) {
			e.printStackTrace();
		}
		return ((BasicOntologyNetwork) noo).getOntologies();
    }
    
    public Set<Alignment> networkAlignmentUri(String uri) {
    	OntologyNetwork noo = null;
		try {
			noo = alignmentCache.getOntologyNetwork(uri);
		} catch (AlignmentException e) {
			e.printStackTrace();
		}
		return ((BasicOntologyNetwork) noo).getAlignments();
    }
    
    public Collection<Alignment> alignments( URI uri1, URI uri2 ) {
	return alignmentCache.alignments( uri1, uri2 );
    }
    
    public Collection<URI> ontologyNetworkUris() {
    return ((VolatilCache) alignmentCache).ontologyNetworkUris();
    }

    public Collection<OntologyNetwork> ontologyNetworks() {
    	return alignmentCache.ontologyNetworks();
    }
 
    public String query( String query ){
	//return alignmentCache.query( query );
	return "Not available yet";
    }

    public String serverURL(){
	return serverId;
    }

    public String argline(){
	return commandLineParams.getProperty( "argline" );
    }

   /*********************************************************************
     * Basic protocol primitives
     *********************************************************************/

    // DONE
    // Implements: store (different from store below)
    public Message load( Properties params ) {
	boolean todiscard = false;
	// load the alignment
	String name = params.getProperty("url");
	String file = null;
	if ( name == null || name.equals("") ){
	    file  = params.getProperty("filename");
	    if ( file != null && !file.equals("") ) name = "file://"+file;
	}
	//logger.trace("Preparing for loading {}", name);
	Alignment al = null;
	try {
	    //logger.trace(" Parsing alignment");
	    AlignmentParser aparser = new AlignmentParser();
	    al = aparser.parse( name );
	    //logger.trace(" Alignment parsed");
	} catch (Exception e) {
	    return new UnreachableAlignment( params, newId(), serverId,name );
	}
	// We preserve the pretty tag within the loaded ontology
	String pretty = al.getExtension( Namespace.ALIGNMENT.uri, Annotations.PRETTY );
	if ( pretty == null ) pretty = params.getProperty("pretty");
	if ( pretty != null && !pretty.equals("") ) {
	    al.setExtension( Namespace.ALIGNMENT.uri, Annotations.PRETTY, pretty );
	}
	// register it
	String id = alignmentCache.recordNewAlignment( al, true );
	// if the file has been uploaded: discard it
	if ( al != null ) {
	    // try unlink
	}
	return new AlignmentId( params, newId(), serverId, id ,pretty );
    }

    // Implements: align
    @SuppressWarnings( "unchecked" )
    public Message align( Properties params ){
	Message result = null;
	// These are added to the parameters wich are in the message
	//for ( String key : commandLineParams ) {
	// Unfortunately non iterable
	for ( Enumeration<String> e = (Enumeration<String>)commandLineParams.propertyNames(); e.hasMoreElements();) { //[W:unchecked]
	    String key = e.nextElement();
	    if ( params.getProperty( key ) == null ){
		params.setProperty( key , commandLineParams.getProperty( key ) );
	    }
	}
	// Do the fast part (retrieve)
	result = retrieveAlignment( params );
	if ( result != null ) return result;
	// [JE2013:ID]
	String uri = alignmentCache.generateAlignmentUri();

	// [JE2013:ID]
	Aligner althread = new Aligner( params, uri );
	Thread th = new Thread(althread);
	// Do the slow part (align)
	if ( params.getProperty("async") != null ) {
	    th.start();
	    // Parameters are used
	    // [JE2013:ID]
	    return new AlignmentId( params, newId(), serverId, uri );
	} else {
	    th.start();
	    try{ th.join(); }
	    catch ( InterruptedException is ) {
		return new ErrorMsg( params, newId(), serverId,"Interrupted exception" );
	    };
	    return althread.getResult();
	}
    }

    /**
     * returns null if alignment not retrieved
     * Otherwise returns AlignmentId or an ErrorMsg
     */
    private Message retrieveAlignment( Properties params ){
	String method = params.getProperty("method");
	// find and access o, o'
	URI uri1 = null;
	URI uri2 = null;
	try {
	    uri1 = new URI( params.getProperty("onto1"));
	    uri2 = new URI( params.getProperty("onto2"));
	} catch (Exception e) {
	    return new NonConformParameters( params, newId(), serverId,"nonconform/params/onto" );
	};
	Set<Alignment> alignments = alignmentCache.getAlignments( uri1, uri2 );
	if ( alignments != null && params.getProperty("force") == null ) {
	    for ( Alignment al: alignments ){
		String meth2 = al.getExtension( Namespace.ALIGNMENT.uri, Annotations.METHOD );
		if ( meth2 != null && meth2.equals(method) ) {
		    return new AlignmentId( params, newId(), serverId,
					   al.getExtension( Namespace.ALIGNMENT.uri, Annotations.ID ) ,
					   al.getExtension( Namespace.ALIGNMENT.uri, Annotations.PRETTY ) );
		}
	    }
	}
	return (Message)null;
    }

    // DONE
    // Implements: query-aligned
    public Message existingAlignments( Properties params ){
	// find and access o, o'
	String onto1 = params.getProperty("onto1");
	String onto2 = params.getProperty("onto2");
	URI uri1 = null;
	URI uri2 = null;
	Set<Alignment> alignments = new HashSet<Alignment>();
	try {
	    if( onto1 != null && !onto1.equals("") ) {
		uri1 = new URI( onto1 );
	    }
	    if ( onto2 != null && !onto2.equals("") ) {
		uri2 = new URI( onto2 );
	    }
	    alignments = alignmentCache.getAlignments( uri1, uri2 );
	} catch (Exception e) {
	    return new ErrorMsg( params, newId(), serverId,"MalformedURI problem" );
	}; //done below
	String msg = "";
	String prettys = "";
	for ( Alignment al : alignments ) {
	    msg += al.getExtension( Namespace.ALIGNMENT.uri, Annotations.ID )+" ";
	    prettys += al.getExtension( Namespace.ALIGNMENT.uri, Annotations.PRETTY )+ ":";
	}
	return new AlignmentIds( params, newId(), serverId, msg, prettys );
    }

    public Message findCorrespondences( Properties params ) {
	// Retrieve the alignment
	Alignment al = null;
	String id = params.getProperty("id");
	try {
	    al = alignmentCache.getAlignment( id );
	} catch (Exception e) {
	    return new UnknownAlignment( params, newId(), serverId,id );
	}
	// Find matched
	URI uri = null;
	try {
	    uri = new URI( params.getProperty("entity") );
	} catch (Exception e) {
	    return new ErrorMsg( params, newId(), serverId,"MalformedURI problem" );
	};
	// Retrieve correspondences
	String msg = params.getProperty("strict");
	boolean strict = ( msg != null && !msg.equals("0") && !msg.equals("false") && !msg.equals("no") );
	msg = "";
	try {
	    Set<Cell> cells = al.getAlignCells1( uri );
	    if ( cells != null ) {
		for ( Cell c : cells ) {
		    if ( !strict || c.getRelation() instanceof EquivRelation ) {
			msg += c.getObject2AsURI( al )+" ";
		    }
		}
	    }
	} catch ( AlignmentException alex ) { // should never happen
	    return new ErrorMsg( params, newId(), serverId,"Unexpected Alignment API Error" );
	}
	return new EntityList( params, newId(), serverId, msg );
    }

    // ABSOLUTELY NOT IMPLEMENTED
    // But look at existingAlignments
    // Implements: find
    // This may be useful when calling WATSON
    public Message find(Properties params){
    //\prul{search-success}{a --request ( find (O, T) )--> S}{O' <= Match(O,T); S --inform (O')--> a}{reachable(O) & Match(O,T)!=null}
    //\prul{search-void}{a - request ( find (O, T) ) \rightarrow S}{S - failure (nomatch) \rightarrow a}{reachable(O)\wedge Match(O,T)=\emptyset}
    //\prul{search-unreachable}{a - request ( find (O, T) ) \rightarrow S}{S - failure ( unreachable (O) ) \rightarrow a}{\neg reachable(O)}
	return new OntologyURI( params, newId(), serverId,"Find not implemented" );
    }

    // Implements: translate
    // This should be applied to many more kind of messages with different kind of translation
    public Message translate(Properties params){
	// Retrieve the alignment
	String id = params.getProperty("id");
	BasicAlignment al = null;
	try {
	    // JE:This one is risky
	    al = (BasicAlignment)alignmentCache.getAlignment( id );
	} catch (Exception e) {
	    return new UnknownAlignment( params, newId(), serverId,id );
	}
	// Translate the query
	try {
	    String translation = al.rewriteSPARQLQuery( params.getProperty("query") );
	    return new TranslatedMessage( params, newId(), serverId,translation );
	} catch (AlignmentException e) {
	    return new ErrorMsg( params, newId(), serverId,e.toString() );
	}
    }

    // DONE
    // Implements: render
    public Message render( Properties params ){
	// Retrieve the alignment
	String id = params.getProperty( "id" );
	Alignment al = null;
	try {
	    logger.trace("Alignment sought for {}", id);
	    al = alignmentCache.getAlignment( id );
	    logger.trace("Alignment found");
	} catch (Exception e) {
	    return new UnknownAlignment( params, newId(), serverId,id );
	}
	// Render it
	String method = params.getProperty("method");
	PrintWriter writer = null;
	// Redirect the output in a String
	ByteArrayOutputStream result = new ByteArrayOutputStream(); 
	try { 
	    writer = new PrintWriter (
			  new BufferedWriter(
			       new OutputStreamWriter( result, "UTF-8" )), true);
	    AlignmentVisitor renderer = null;
	    try {
		Class[] cparams = { PrintWriter.class };
		Constructor rendererConstructor = Class.forName( method ).getConstructor( cparams );
		Object[] mparams = { (Object)writer };
		renderer = (AlignmentVisitor) rendererConstructor.newInstance( mparams );
	    } catch ( ClassNotFoundException cnfex ) {
		// should return the message
		logger.error( "Unknown method", cnfex );
		return new UnknownMethod( params, newId(), serverId,method );
	    }
	    renderer.init( params );
	    al.render( renderer );
	} catch ( AlignmentException e ) {
	    return new CannotRenderAlignment( params, newId(), serverId,id );
	} catch ( Exception e ) { // These are exceptions related to I/O
	    writer.flush();
	    //logger.trace( "Resulting rendering : {}", result.toString() );
	    logger.error( "Cannot render alignment", e );
	    return new Message( params, newId(), serverId,"Failed to render alignment" );
	} finally {
	    writer.flush();
	    writer.close();
	}
	return new RenderedAlignment( params, newId(), serverId, result.toString() );
    }


    /*********************************************************************
     * Extended protocol primitives
     *********************************************************************/

    // Implementation specific
    public Message store( Properties params ) {
	String id = params.getProperty("id");
	Alignment al = null;
	 
	try {
	    try {
	    	al = alignmentCache.getAlignment( id );
	    } catch(Exception ex) {
	    	logger.warn( "Unknown Id {} in Store", id );
	    }
	    // Be sure it is not already stored
	    if ( !alignmentCache.isAlignmentStored( al ) ) {

		alignmentCache.storeAlignment( id );
		 
		// Retrieve the alignment again
		al = alignmentCache.getAlignment( id );
		// for all directories...
		for ( Directory d : directories.values() ){
		    // Declare the alignment in the directory
		    try { d.register( al ); }
		    catch ( AServException e ) {
			logger.debug( "IGNORED Exception in alignment registering", e );
		    }
		}
	    }
	    // register by them
	    // Could also be an AlreadyStoredAlignment error
	    return new AlignmentId( params, newId(), serverId, id,
				   al.getExtension( Namespace.ALIGNMENT.uri, Annotations.PRETTY ));
	} catch (Exception e) {
	    return new UnknownAlignment( params, newId(), serverId,id );
	}
    }

    // Implementation specific
    public Message erase( Properties params ) {
	String id = params.getProperty("id");
	Alignment al = null;
	try {
	    al = alignmentCache.getAlignment( id );
	    // Erase it from directories
	    for ( Directory d : directories.values() ){
		try { d.register( al ); }
		catch ( AServException e ) { 
		    logger.debug( "IGNORED Cannot register alignment", e );
		}
	    }
	    // Erase it from storage
	    try {
		alignmentCache.eraseAlignment( id, true );
	    } catch ( Exception ex ) {
		logger.debug( "IGNORED Cannot erase alignment", ex );
	    }
	    // Should be a SuppressedAlignment
	    return new AlignmentId( params, newId(), serverId, id ,
				   al.getExtension( Namespace.ALIGNMENT.uri, Annotations.PRETTY ));
	} catch ( Exception ex ) {
	    return new UnknownAlignment( params, newId(), serverId,id );
	}
    }

    /*
     * Returns only the metadata of an alignment and returns it in 
     * parameters
     */
    public Message metadata( Properties params ){
	// Retrieve the alignment
	String id = params.getProperty("id");
	Alignment al = null;
	try {
	    al = alignmentCache.getMetadata( id );
	} catch (Exception e) {
	    return new UnknownAlignment( params, newId(), serverId,id );
	}
	// JE: Other possibility is to render the metadata through XMLMetadataRendererVisitor into content...
	// Put all the local metadata in parameters
	Properties p = new Properties();
	p.setProperty( "file1", al.getFile1().toString() );
	p.setProperty( "file2", al.getFile2().toString() );
	p.setProperty( Namespace.ALIGNMENT.uri+"#level", al.getLevel() );
	p.setProperty( Namespace.ALIGNMENT.uri+"#type", al.getType() );
	for ( String[] ext : al.getExtensions() ){
	    p.setProperty( ext[0]+ext[1], ext[2] );
	}
	return new AlignmentMetadata( params, newId(), serverId, id, p );
    }

    /*********************************************************************
     * Extra alignment primitives
     *
     * All these primitives must create a new alignment and return its Id
     * There is no way an alignment server could modify an alignment
     *********************************************************************/

    public Message trim( Properties params ) {
	// Retrieve the alignment
	String id = params.getProperty("id");
	Alignment al = null;
	try {
	    al = alignmentCache.getAlignment( id );
	} catch (Exception e) {
	    return new UnknownAlignment( params, newId(), serverId,id );
	}
	// get the trim parameters
	String type = params.getProperty("type");
	if ( type == null ) type = "hard";
	double threshold = Double.parseDouble( params.getProperty("threshold"));
	al = (BasicAlignment)((BasicAlignment)al).clone();
	try { al.cut( type, threshold );}
	catch (AlignmentException e) {
	    return new ErrorMsg( params, newId(), serverId,e.toString() );
	}
	String pretty = al.getExtension( Namespace.ALIGNMENT.uri, Annotations.PRETTY );
	if ( pretty != null ){
	    al.setExtension( Namespace.ALIGNMENT.uri, Annotations.PRETTY, pretty+"/trimmed "+threshold );
	};
	String newId = alignmentCache.recordNewAlignment( al, true );
	return new AlignmentId( params, newId(), serverId, newId,
			       al.getExtension( Namespace.ALIGNMENT.uri, Annotations.PRETTY ));
    }

    public Message harden( Properties params ){
	return new NonConformParameters( params, newId(), serverId, "Harden not implemented" );
    }

    public Message inverse( Properties params ){
	// Retrieve the alignment
	String id = params.getProperty("id");
	Alignment al = null;
	try {
	    al = alignmentCache.getAlignment( id );
	} catch (Exception e) {
	    return new UnknownAlignment( params, newId(), serverId,"unknown/Alignment/"+id );
	}

	// Invert it
	try { al = al.inverse(); }
	catch (AlignmentException e) {
	    return new ErrorMsg( params, newId(), serverId,e.toString() );
	}
	String pretty = al.getExtension( Namespace.ALIGNMENT.uri, Annotations.PRETTY );
	if ( pretty != null ){
	    al.setExtension( Namespace.ALIGNMENT.uri, Annotations.PRETTY, pretty+"/inverted" );
	};
	String newId = alignmentCache.recordNewAlignment( al, true );
	return new AlignmentId( params, newId(), serverId, newId,
			       al.getExtension( Namespace.ALIGNMENT.uri, Annotations.PRETTY ));
    }

    public Message meet( Properties params ){
	// Retrieve alignments
	return new NonConformParameters( params, newId(), serverId, "Meet not available" );
    }

    public Message join( Properties params ){
	// Retrieve alignments
	return new NonConformParameters( params, newId(), serverId, "Join not available" );
    }

    public Message compose( Properties params ){
	// Retrieve alignments
	return new NonConformParameters( params, newId(), serverId, "Compose not available" );
    }

    public Message eval( Properties params ){
	// Retrieve the alignment
	String id = params.getProperty("id");
	Alignment al = null;
	try {
	    al = alignmentCache.getAlignment( id );
	} catch (Exception e) {
	    return new UnknownAlignment( params, newId(), serverId,"unknown/Alignment/"+id );
	}
	// Retrieve the reference alignment
	String rid = params.getProperty("ref");
	Alignment ref = null;
	try {
	    ref = alignmentCache.getAlignment( rid );
	} catch (Exception e) {
	    return new UnknownAlignment( params, newId(), serverId,"unknown/Alignment/"+rid );
	}
	// Set the comparison method
	String classname = params.getProperty("method");
	if ( classname == null ) classname = "fr.inrialpes.exmo.align.impl.eval.PRecEvaluator";
	Evaluator eval = null;
	try {
	    Class[] cparams = { Alignment.class, Alignment.class };
	    Class<?> evaluatorClass = Class.forName( classname );
	    Constructor evaluatorConstructor = evaluatorClass.getConstructor( cparams );
	    Object [] mparams = { (Object)ref, (Object)al };
	    eval = (Evaluator)evaluatorConstructor.newInstance( mparams );
	} catch ( ClassNotFoundException cnfex ) {
	    logger.error( "Unknown method", cnfex );
	    return new UnknownMethod( params, newId(), serverId,classname );
	} catch ( InvocationTargetException itex ) {
	    String msg = itex.toString();
	    if ( itex.getCause() != null ) msg = itex.getCause().toString();
	    return new ErrorMsg( params, newId(), serverId,msg );
	} catch ( Exception ex ) {
	    return new ErrorMsg( params, newId(), serverId,ex.toString() );
	}
	// Compare it
	try { eval.eval( params); }
	catch ( AlignmentException e ) {
	    return new ErrorMsg( params, newId(), serverId,e.toString() );
	}
	// Could also be EvaluationId if we develop a more elaborate evaluation description
	return new EvalResult( params, newId(), serverId, classname, eval.getResults() );
    }

    public Message diff( Properties params ){
	// Retrieve the alignment
	String id1 = params.getProperty("id1");
	Alignment al1 = null;
	try {
	    al1 = alignmentCache.getAlignment( id1 );
	} catch (Exception e) {
	    return new UnknownAlignment( params, newId(), serverId,"unknown/Alignment/"+id1 );
	}
	// Retrieve the reference alignment
	String id2 = params.getProperty("id2");
	Alignment al2 = null;
	try {
	    al2 = alignmentCache.getAlignment( id2 );
	} catch (Exception e) {
	    return new UnknownAlignment( params, newId(), serverId,"unknown/Alignment/"+id2 );
	}
	try { 
	    DiffEvaluator diff = new DiffEvaluator( al1, al2 );
	    diff.eval( params ); 
	    // This will only work with HTML
	    return new EvalResult( params, newId(), serverId, diff.HTMLString(), (Properties)null );
	} catch (AlignmentException e) {
	    return new ErrorMsg( params, newId(), serverId,e.toString() );
	}
    }

    /**
     * Store evaluation result from its URI
     */
    public Message storeEval( Properties params ){
	return new ErrorMsg( params, newId(), serverId,"Not yet implemented" );
    }

    /**
     * Evaluate a track: a set of results
     */
    // It is also possible to try a groupeval ~> with a zipfile containing results
    //            ~~> But it is more difficult to know where is the reference (non public)
    // There should also be options for selecting the result display
    //            ~~> PRGraph (but this may be a Evaluator)
    //            ~~> Triangle
    //            ~~> Cross
    public Message groupEval( Properties params ){
	return new ErrorMsg( params, newId(), serverId,"Not yet implemented" );
    }

    /**
     * Store the result
     */
    public Message storeGroupEval( Properties params ){
	return new ErrorMsg( params, newId(), serverId,"Not yet implemented" );
    }

    /**
     * Retrieve the results (all registered result) of a particular test
     */
    public Message getResults( Properties params ){
	return new ErrorMsg( params, newId(), serverId,"Not yet implemented" );
    }

    public boolean storedAlignment( Properties params ) {
	// Retrieve the alignment
	String id = params.getProperty("id");
	Alignment al = null;
	try {
	    al = alignmentCache.getAlignment( id );
	} catch (Exception e) {
	    return false;
	}
	return alignmentCache.isAlignmentStored( al );
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
    public Message replywith(Properties params){

    //\prul{redirect}{a - request ( q(x)~reply-with:~i) \rightarrow S}{
    //Q \Leftarrow Q\cup\{\langle a, i, !i', q(x), S'\rangle\}\		\
    //S - request( q( R(x) )~reply-with:~i')\rightarrow S'}{S'\in C(q)}
	return new Message( params, newId(), serverId,"dummy//" );
    }

    // Implements: reply-to
    public Message replyto(Properties params){

    //\prul{handle-return}{S' - inform ( y~reply-to:~i') \rightarrow S}{
    //Q \Leftarrow Q-\{\langle a, i, i', _, S'\rangle\}\		\
    //S - inform( R^{-1}(y)~reply-to:~i)\rightarrow a}{\langle a, i, i', _, S'\rangle \in Q, \neg surr(y)}

    //\prul{handle-return}{S' - inform ( y~reply-to:~i') \rightarrow S}{
    //Q \Leftarrow Q-\{\langle a, i, i', _, S'\rangle\}\	\
    //R \Leftarrow R\cup\{\langle a, !y', y, S'\rangle\}\		\
    //S - inform( R^{-1}(y)~reply-to:~i)\rightarrow a}{\langle a, i, i', _, S'\rangle \in Q, surr(y)}
	return new Message( params, newId(), serverId,"dummy//" );
    }

    // Implements: failure
    public Message failure(Properties params){

    //\prul{failure-return}{S' - failure ( y~reply-to:~i') \rightarrow S}{
    //Q \Leftarrow Q-\{\langle a, i, i', _, S'\rangle\}\		\
    //S - failure( R^{-1}(y)~reply-to:~i)\rightarrow a}{\langle a, i, i', _, S'\rangle \in Q}
	return new Message( params, newId(), serverId,"dummy//" );
    }

    /*********************************************************************
     * Utilities: reaching and loading ontologies
     *********************************************************************/

    public LoadedOntology reachable( URI uri ){
	try { 
	    OntologyFactory factory = OntologyFactory.getFactory();
	    return factory.loadOntology( uri );
	} catch (Exception e) { return null; }
    }

    /*********************************************************************
     * Utilities: Finding the implementation of an interface
     *
     * This is starting causing "java.lang.OutOfMemoryError: PermGen space"
     * (when it was in static)
     * Remedied (for the moment by improving the visited cache)
     * This may also benefit by first filling the visited by the path of the
     * libraries we know.
     *
     * May be replaced by org.reflections (see reflectiveImplementations)
     * JE: Was unable to set it properly
     *********************************************************************/

    /*
    public Set<Class<?>> reflectiveImplementations( String interfaceName ) {
	Set<Class<?>> classes = null;
	try {
	    Class toclass = Class.forName( interfaceName );
	    //Reflections reflections = new Reflections("com.mycompany");
	    Reflections reflections = new Reflections( new ConfigurationBuilder() );
	    //Set<Class<? extends MyInterface>> 
	    classes = reflections.getSubTypesOf(toclass);
	} catch (ClassNotFoundException ex) {
	    logger.debug( "IGNORED Class {} not found!", interfaceName );
	}
	return classes;
	}*/

    /**
     * Display all the classes inheriting or implementing a given
     * interface in the currently loaded packages.
     * @param interfaceName the name of the interface to implement
     */
    public Set<String> implementations( String interfaceName ) {
	Set<String> list = new HashSet<String>();
	try {
	    Class toclass = Class.forName(interfaceName);
	    implementations( toclass, list );
	} catch (ClassNotFoundException ex) {
	    logger.debug( "IGNORED Class {} not found!", interfaceName );
	}
	return list;
    }

    public void implementations( Class tosubclass, Set<String> list ){
	Set<String> visited = new HashSet<String>();
	//visited.add();
	String classPath = System.getProperty("java.class.path",".");
	// Hack: this is not necessary
	//classPath = classPath.substring(0,classPath.lastIndexOf(File.pathSeparatorChar));
	//logger.trace( "CLASSPATH = {}", classPath );
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
			//logger.trace("DIR {}", file);
			String subs[] = file.list();
			for( int index = 0 ; index < subs.length ; index ++ ){
			    //logger.trace("    {}", subs[index]);
			    // IF class
			    if ( subs[index].endsWith(".class") ) {
				String classname = subs[index].substring(0,subs[index].length()-6);
				if (classname.startsWith(File.separator)) 
				    classname = classname.substring(1);
				classname = classname.replace(File.separatorChar,'.');
				if ( implementsInterface( classname, tosubclass ) ) {
				    list.add( classname );
				}
			    }
			}
		    } else {
			String canon = null;
			try {
			    canon = file.getCanonicalPath();
			} catch ( IOException ioex ) {
			    canon = file.toString();
			    logger.warn( "IGNORED Invalid Jar path", ioex );
			}
			if ( canon.endsWith(".jar") &&
			     !visited.contains( canon ) && 
			     file.exists() ) {
			    //logger.trace("JAR {}", file);
			    visited.add( canon );
			    JarFile jar = null;
			    try {
				jar = new JarFile( file );
				exploreJar( list, visited, tosubclass, jar );
				// Iterate on needed Jarfiles
				// JE(caveat): this deals naively with Jar files,
				// in particular it does not deal with section'ed MANISFESTs
				Attributes mainAttributes = jar.getManifest().getMainAttributes();
				String path = mainAttributes.getValue( Name.CLASS_PATH );
				//logger.trace("  >CP> {}", path);
				if ( path != null && !path.equals("") ) {
				    // JE: Not sure where to find the other Jars:
				    // in the path or at the local place?
				    //classPath += File.pathSeparator+file.getParent()+File.separator + path.replaceAll("[ \t]+",File.pathSeparator+file.getParent()+File.separator);
				    // This replaces the replaceAll which is not tolerant on Windows in having "\" as a separator
				    // Is there a way to make it iterable???
				    for( StringTokenizer token = new StringTokenizer(path," \t"); token.hasMoreTokens(); )
					classPath += File.pathSeparator+file.getParent()+File.separator+token.nextToken();
				}
			    } catch (NullPointerException nullexp) { //Raised by JarFile
				//logger.trace( "JarFile, file {} unavailable", file );
			    }
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
    
    public void exploreJar( Set<String> list, Set<String> visited, Class tosubclass, JarFile jar ) {
	Enumeration enumeration = jar.entries();
	while( enumeration != null && enumeration.hasMoreElements() ){
	    JarEntry entry = (JarEntry)enumeration.nextElement();
	    String entryName = entry.toString();
	    //logger.trace("    {}", entryName);
	    int len = entryName.length()-6;
	    if( len > 0 && entryName.substring(len).compareTo(".class") == 0) {
		entryName = entryName.substring(0,len);
		// Beware, in a Jarfile the separator is always "/"
		// and it would not be dependent on the current system anyway.
		//entryName = entryName.replaceAll(File.separator,".");
		entryName = entryName.replaceAll("/",".");
		if ( implementsInterface( entryName, tosubclass ) ) {
			    list.add( entryName );
		}
	    } else {
		String canon = entryName;
		try {
		    canon = new File( entryName ).getCanonicalPath();
		} catch ( IOException ioex ) {
		    logger.warn( "IGNORED Invalid Jar path", ioex );
		}
		if( canon.endsWith(".jar") &&
		    !visited.contains( canon ) ) { // a jar in a jar
		    //logger.trace("JAR {}", entryName);
		    visited.add( canon );
		    //logger.trace(  "jarEntry is a jarfile={}", je.getName() );
		    InputStream jarSt = null;
		    OutputStream out = null;
		    File f = null;
		    try {
			jarSt = jar.getInputStream( (ZipEntry)entry );
			f = File.createTempFile( "aservTmpFile"+visited.size(), "jar" );
			out = new FileOutputStream( f );
			byte buf[]=new byte[1024];
			int len1 ;
			while( (len1 = jarSt.read(buf))>0 )
			    out.write(buf,0,len1);
			JarFile inJar = new JarFile( f );
			exploreJar( list, visited, tosubclass, inJar );
		    } catch (IOException ioex) {
			logger.warn( "IGNORED Cannot read embedded jar", ioex );
		    } finally {
			try {
			    jarSt.close();
			    out.close();
			    f.delete();
			} catch (Exception ex) {};
		    }
		}
	    } 
	}
    }

    public boolean implementsInterface( String classname, Class tosubclass ) {
	try {
	    // This was used to ban classes with weird behaviour
	    // This is not needed anymore (kept as an example)
	    /*
	      if ( classname.equals("org.apache.xalan.extensions.ExtensionHandlerGeneral") || 
		 classname.equals("org.apache.log4j.net.ZeroConfSupport") 
		 ) {
		throw new ClassNotFoundException( "Classes breaking this work" );
		}*/
	    // This is a little crazy but at least save PermGem
	    // These are our largest libraries, but others may be to ban
	    // Hope that they do not implement any of our interfaces
	    if ( classname.startsWith( "org.apache.lucene" )
		 || classname.startsWith( "org.tartarus" )
		 || classname.startsWith( "com.hp.hpl.jena" )
		 || classname.startsWith( "org.apache.jena" )
		 || classname.startsWith( "arq." )
		 || classname.startsWith( "riotcmd" )
		 || classname.startsWith( "org.openjena" )
		 || classname.startsWith( "uk.ac.manchester.cs.owlapi" )
		 || classname.startsWith( "org.coode" )
		 || classname.startsWith( "org.semanticweb.owlapi" )
		 || classname.startsWith( "de.uulm.ecs.ai.owlapi" )
		 || classname.startsWith( "org.apache.xerces" )
		 || classname.startsWith( "org.apache.xml" )
		 || classname.startsWith( "org.apache.html" )
		 || classname.startsWith( "org.apache.wml" )
		 ) return false;
	    // JE: Here there is a bug that is that it is not possible
	    // to have ALL interfaces with this function!!!
	    // This is really stupid but that's life
	    // So it is compulsory that AlignmentProcess be declared 
	    // as implemented
	    Class cl = Class.forName(classname);
	    // It is possible to suppress here abstract classes by:
	    if ( java.lang.reflect.Modifier.isAbstract( cl.getModifiers() ) ) return false;
	    Class[] interfaces = cl.getInterfaces();
	    for ( int i=interfaces.length-1; i >= 0  ; i-- ){
		if ( interfaces[i] == tosubclass ) {
		    //logger.trace(" -j-> {}", classname);
		    return true;
		}
		//logger.trace("       I> {}", interfaces[i] );
	    }
	    // Not one of our classes
	} catch ( ExceptionInInitializerError eiie ) {
	} catch ( NoClassDefFoundError ncdex ) {
	} catch ( ClassNotFoundException cnfex ) {
	} catch ( UnsatisfiedLinkError ule ) {
	    //logger.trace("   ******** {}", classname);
	}
	return false;
    }

    protected class Aligner implements Runnable {
	private Properties params = null;
	private Message result = null;
	private String id = null;

	public Aligner( Properties p, String id ) {
	    params = p;
	    this.id = id;
	}

	public Message getResult() {
	    return result;
	}

	public void run() {
	    String method = params.getProperty("method");
	    // find and access o, o'
	    URI uri1 = null;
	    URI uri2 = null;

	    try {
		uri1 = new URI( params.getProperty("onto1"));
		uri2 = new URI( params.getProperty("onto2"));
	    } catch (Exception e) {
		result = new NonConformParameters( params, newId(), serverId,"nonconform/params/onto" );
		return;
	    };

	    // find initial alignment
	    Alignment init = null;
	    if ( params.getProperty("init") != null && !params.getProperty("init").equals("") ) {
		try {
		    //logger.trace(" Retrieving init");
		    try {
			init = alignmentCache.getAlignment( params.getProperty("init") );
		} catch (Exception e) {
			result = new UnknownAlignment( params, newId(), serverId,params.getProperty("init") );
			return;
		    }
		} catch (Exception e) {
		    result = new UnknownAlignment( params, newId(), serverId,params.getProperty("init") );
		    return;
		}
	    }
	    
	    // Create alignment object
	    try {
		if ( method == null )
		    method = "fr.inrialpes.exmo.align.impl.method.StringDistAlignment";
		Class<?> alignmentClass = Class.forName(method);
		Class[] cparams = {};
		Constructor alignmentConstructor = alignmentClass.getConstructor( cparams );
		Object[] mparams = {};
		AlignmentProcess aresult = (AlignmentProcess)alignmentConstructor.newInstance( mparams );
		try {
		    aresult.init( uri1, uri2 );
		    long time = System.currentTimeMillis();
		    aresult.align( init, params ); // add opts
		    long newTime = System.currentTimeMillis();
		    aresult.setExtension( Namespace.ALIGNMENT.uri, Annotations.TIME, Long.toString(newTime - time) );
		    aresult.setExtension( Namespace.ALIGNMENT.uri, Annotations.TIME, Long.toString(newTime - time) );
		    String pretty = params.getProperty( "pretty" );
		    if ( pretty != null && !pretty.equals("") )
			aresult.setExtension( Namespace.ALIGNMENT.uri, Annotations.PRETTY, pretty );
		} catch (AlignmentException e) {
		    // The unreachability test has already been done
		    // JE 15/1/2009: commented the unreachability test
		    if ( reachable( uri1 ) == null ){
			result = new UnreachableOntology( params, newId(), serverId,params.getProperty("onto1") );
		    } else if ( reachable( uri2 ) == null ){
			result = new UnreachableOntology( params, newId(), serverId,params.getProperty("onto2") );
		    } else {
			result = new NonConformParameters( params, newId(), serverId,"nonconform/params/"+e.getMessage() );
		    }
		    return;
		}
		// ask to store A'
		alignmentCache.recordNewAlignment( id, aresult, true );
		result = new AlignmentId( params, newId(), serverId, id,
			       aresult.getExtension( Namespace.ALIGNMENT.uri, Annotations.PRETTY ));
	    } catch ( ClassNotFoundException cnfex ) {
		logger.error( "Unknown method", cnfex );
		result = new UnknownMethod( params, newId(), serverId,method );
	    } catch (NoSuchMethodException e) {
		result = new RunTimeError( params, newId(), serverId, "No such method: "+method+"(Object, Object)" );
	    } catch (InstantiationException e) {
		result = new RunTimeError( params, newId(), serverId, "Instantiation" );
	    } catch (IllegalAccessException e) {
		result = new RunTimeError( params, newId(), serverId, "Cannot access" );
	    } catch (InvocationTargetException e) {
		result = new RunTimeError( params, newId(), serverId, "Invocation target" );
	    } catch (AlignmentException e) {
		result = new NonConformParameters( params, newId(), serverId, "nonconform/params/" );
	    } catch (Exception e) {
		result = new RunTimeError( params, newId(), serverId, "Unexpected exception :"+e );
	    }
	}
    }

  //* Ontology Networks */
    
    public Message loadonet( Properties params ) {

	// load the ontology network
	String name = params.getProperty("url");
	String file = null;
	if ( name == null || name.equals("") ){
	    file  = params.getProperty("filename");
	    if ( file != null && !file.equals("") ) name = "file://"+file;
	    }
	logger.trace("Preparing for loading {}", name);
	BasicOntologyNetwork noo = null;
	try {
	    noo = (BasicOntologyNetwork) BasicOntologyNetwork.read( name );
	    logger.trace(" Ontology network parsed");
	    } catch (Exception e) {
		  return new UnreachableOntologyNetwork( params, newId(), serverId, name );
		  }
	// We preserve the pretty tag within the loaded ontology network
	String pretty = noo.getExtension( Namespace.ALIGNMENT.uri, Annotations.PRETTY ); 
	if ( pretty == null ) pretty = params.getProperty("pretty");
	if ( pretty != null && !pretty.equals("") ) {
		noo.setExtension( Namespace.ALIGNMENT.uri, Annotations.PRETTY, pretty );
		}
	// register it
	String id = alignmentCache.recordNewNetwork( noo, true );
	logger.debug(" Ontology network loaded, id: {} total ontologies: {} total alignments: {}",id, noo.getOntologies().size(),noo.getAlignments().size());

    //=== The alignment has the id of the source file (e.g. file:///path/FileName.rdf)
	Set<Alignment> networkAlignments = networkAlignmentUri(id);
	for (Alignment al : networkAlignments) {
		String idAl = alignmentCache.recordNewAlignment( al, true );
	}
	
	return new OntologyNetworkId( params, newId(), serverId, id ,pretty );
    }

    
    public Message renderonet(Properties params) {
   //rdf render
    	OntologyNetwork noo = null;
   	    noo = new BasicOntologyNetwork();
    	String id = params.getProperty( "id" );
    	try {
			noo = alignmentCache.getOntologyNetwork(id);	
		} catch (AlignmentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
    	BasicOntologyNetwork newnoo = (BasicOntologyNetwork)noo;
		newnoo.setIndentString( "  " );
		newnoo.setNewLineString( System.getProperty("line.separator") );
		// Print it in a string	 
		ByteArrayOutputStream result = new ByteArrayOutputStream(); 
		PrintWriter writer = null;
		try {
			writer = new PrintWriter (
					  new BufferedWriter(
					       new OutputStreamWriter( result, "UTF-8" )), true);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
		    newnoo.write( writer );
		} catch (AlignmentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
		    writer.flush();
		    writer.close();
		}
	  	return new RenderedAlignment( params, newId(), serverId, result.toString() ); //or RenderedOntologyNetwork

    }

    public Message renderHTMLNetwork( Properties params ){
    	// Retrieve the alignment
    	String result = new String();
    	String idON = new String();
    	String pidON = new String();
    	String id = params.getProperty( "id" );
    	//String id = idON;
    	BasicOntologyNetwork noo = null;
    	try {
    	    logger.trace("Network sought for {}", id);
    	    noo = (BasicOntologyNetwork) alignmentCache.getOntologyNetwork(id);
    	    idON = noo.getExtension( Namespace.ALIGNMENT.uri, Annotations.ID );
			pidON = noo.getExtension( Namespace.ALIGNMENT.uri, Annotations.PRETTY );
    	    logger.trace("Network found");
    	} catch (Exception e) {
    	    return new UnknownOntologyNetwork( params, newId(), serverId,id );
    	    }
	    result = "<h1>" + id+ " ("+pidON+")" +"</h1>";
	    
	    result += "<table border=\"0\">\n";
	    result += "<h2>Ontologies</h2>\n";
    	Collection<URI> networkOntology = networkOntologyUri(id);
	    result += "<p><tr><th><b>Total ontologies: </b>" + networkOntology.size() + "</th></tr></p>";
	    result += "<table>\n";
	    result += "<ul>";
	    for ( URI onto : networkOntology ) {
	    	result += "<li><a href=\"" + onto.toString() +"\">"+ onto.toString() + "</a></li>";
	    }
	    result += "</ul>";
	    result += "</table>\n";
	    
    	result += "<h2>Alignments</h2>\n"; 
	    Set<Alignment> networkAlignments = networkAlignmentUri(id);
	    result += "<p><tr><th><b>Total alignments: </b>" + networkAlignments.size() + "</th></tr></p>";
	    result += "<table>\n";
	    result += "<ul>";
	    for (Alignment al : networkAlignments) {	
	    	String idAl = al.getExtension( Namespace.ALIGNMENT.uri, Annotations.ID );
		String pidAl = al.getExtension( Namespace.ALIGNMENT.uri, Annotations.PRETTY );
		if ( pidAl == null ) pidAl = idAl; else pidAl = idAl+" ("+pidAl+")";
	    	result += "<li><a href=\""+idAl+"\">"+pidAl+"</a></li>";
	    	}
	    result += "</ul>";
	    result += "</table>\n";
    	return new RenderedAlignment( params, newId(), serverId, result );
        }
    
    public boolean storedOntologyNetwork( Properties params ) {
    	// Retrieve the ontology network
    	String id = params.getProperty("id");
    	OntologyNetwork noo = null;
    	try {
    	    noo = alignmentCache.getOntologyNetwork( id );
    	} catch (Exception e) {
    	    return false;
    	}
    	return alignmentCache.isNetworkStored( noo );
        }
    
 
    public Message storeonet( Properties params ) {
    	String id = params.getProperty("id");
      	OntologyNetwork noo = null;
    	try {
    	    noo = alignmentCache.getOntologyNetwork( id );
    	    // Be sure it is not already stored
    	    if ( !alignmentCache.isNetworkStored(noo) ) {
	    		try {
					alignmentCache.storeOntologyNetwork( id );
				} catch (AlignmentException e) {
					return new UnknownOntologyNetwork( params, newId(), serverId,id );
				}
	    		return new OntologyNetworkId( params, newId(), serverId, id,
    				   ((BasicOntologyNetwork) noo).getExtension( Namespace.ALIGNMENT.uri, Annotations.PRETTY ));
    	    } else {
    	    	return new ErrorMsg( params, newId(), serverId,"Network already stored" );
    	    }
    	} catch (Exception e) {
    	    return new UnknownOntologyNetwork( params, newId(), serverId,id );
    	}

    }
    
    private Message retrieveAlignmentON( Properties params, URI uri1, URI uri2 ){
    	String method = params.getProperty("method");

    	Set<Alignment> alignments = alignmentCache.getAlignments( uri1, uri2 );
    	if ( alignments != null && params.getProperty("force") == null ) {
    	    for ( Alignment al: alignments ){
    		String meth2 = al.getExtension( Namespace.ALIGNMENT.uri, Annotations.METHOD );
    		if ( meth2 != null && meth2.equals(method) ) {
    		    return new AlignmentId( params, newId(), serverId,
    					   al.getExtension( Namespace.ALIGNMENT.uri, Annotations.ID ) ,
    					   al.getExtension( Namespace.ALIGNMENT.uri, Annotations.PRETTY ) );
    		}
    	    }
    	}
    	return (Message)null;
        }
  
    
public List<Message> alignonet( Properties params ) {
    	
    	
    	List<Message> result = new ArrayList<>();
    	//not finished
    	//parameters: onID, method, reflexive, symmetric
    	for ( Enumeration<String> e = (Enumeration<String>)commandLineParams.propertyNames(); e.hasMoreElements();) {
    	    String key = e.nextElement();
    	    if ( params.getProperty( key ) == null ){
    		params.setProperty( key , commandLineParams.getProperty( key ) );
    	    }
    	}
    	//prepare for
    	String id = params.getProperty("id");
    	Boolean reflexive = false;
    	if (params.getProperty("reflexive") != null) reflexive = true;
    	Boolean symmetric = false;
    	if (params.getProperty("symmetric") != null) symmetric = true;
    	
	    Collection<URI> networkOntologyA = networkOntologyUri(id);
	    Collection<URI> networkOntologyB = networkOntologyUri(id);
	    
	    // Reflexive and Symmetric
	    if ( reflexive && symmetric ) {
		    for ( URI ontoA : networkOntologyA ) {
		    	for ( URI ontoB : networkOntologyB ) {
		    		params.setProperty("onto1", ontoA.toString());
		    		params.setProperty("onto2", ontoB.toString());
		    		Message answer = align( params );
		        	if ( answer != null || answer.toString().contains("AlignmentId")) {
		        		result.add(answer);
		        	};
			    }
		    }
	    }
	    return result;
}

    
    // TO VERIFY
    public Message alignonet2( Properties params ) {
    	  	
      	//parameters: onID, method, reflexive, symmetric 	
       	OntologyNetwork noo = null;
    	Boolean reflexive = false;
    	Boolean symmetric = false;
    	String id = params.getProperty("id");
    	String method = params.getProperty("method");
    	if (params.getProperty("reflexive") != null) reflexive = true;
    	if (params.getProperty("symmetric") != null) symmetric = true;
    	
    	try {
	    noo = alignmentCache.getOntologyNetwork( id );
	} catch (AlignmentException e1) {
	    return new UnknownOntologyNetwork( params, newId(), serverId,id );
	}
    	logger.debug(" Before Network alignments results, id: {} total ontologies: {} total alignments: {}",id, noo.getOntologies().size(),noo.getAlignments().size());
    	
    	try { 
	    ((BasicOntologyNetwork)noo).match( method, reflexive, symmetric, params );
	} catch (AlignmentException e) {
    	    return new ErrorMsg( params, newId(), serverId,"Network alignment error" );
    	}
    	
    	logger.debug(" Network alignments results, id: {} total ontologies: {} total alignments: {}",id, noo.getOntologies().size(),noo.getAlignments().size());
    	return new OntologyNetworkId( params, newId(), serverId, id,
				      ((BasicOntologyNetwork) noo).getExtension( Namespace.ALIGNMENT.uri, Annotations.PRETTY ));
    }
    
    
    public Message trimonet( Properties params ) {
	  	
      	//parameters: onID, method, reflexive, symmetric 	
       	OntologyNetwork noo = null;
       	OntologyNetwork nooClone = null;
    	String id = params.getProperty("id");
    	String method = params.getProperty("type");
    	double threshold = Double.parseDouble(params.getProperty("threshold"));
	
    	try {
			noo = alignmentCache.getOntologyNetwork( id );
			} catch (AlignmentException e1) {
				return new UnknownOntologyNetwork( params, newId(), serverId,id );
				}
     	
    	try {
    		((BasicOntologyNetwork) noo).trim(method, threshold);
    		} catch (AlignmentException e) {
    	    return new ErrorMsg( params, newId(), serverId,"Network alignment error" );
    	}
    	return new OntologyNetworkId( params, newId(), serverId, id,
				   ((BasicOntologyNetwork) noo).getExtension( Namespace.ALIGNMENT.uri, Annotations.PRETTY ));
    }
   

    public Message listNetworkOntology( Properties params ) { //not UsED??
    	String result = "";   	
    	
    	OntologyNetwork noo = null;
   	    noo = new BasicOntologyNetwork();
    	String id = params.getProperty( "id" );
    	//URI onto2 = null;
   	
    	try {
			noo = alignmentCache.getOntologyNetwork(id);
			noo.getOntologies().size();
	    	Iterator<URI> iterator2 = noo.getOntologies().iterator();
	    	while (iterator2.hasNext()){
				URI onto2 = (URI) iterator2.next();
	    		result += "<li><a href=\""+onto2+"\">"+onto2+"</a></li>";
	    	}
	    	return new OntologyNetworkId( params, newId(), serverId, id ,result );
    	} catch (AlignmentException e) {
			return new UnknownOntologyNetwork( params, newId(), serverId, id );
		}
    }

}
