/*
 * $Id$
 *
 * Copyright (C) XX, 2006
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
 */

package fr.inrialpes.exmo.align.service;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Set;
import java.util.HashSet;
import java.util.Date;
import java.util.Random;
import java.net.URI;
import java.net.URISyntaxException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

/*
import org.semanticweb.owl.io.owl_rdf.OWLRDFParser;
import org.semanticweb.owl.util.OWLManager;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLException;
//import org.semanticweb.owl.util.OWLManager;
*/

import fr.inrialpes.exmo.align.impl.BasicRelation;
import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.URIAlignment;
//import fr.inrialpes.exmo.align.impl.OWLAPIAlignment;
import fr.inrialpes.exmo.align.impl.URICell;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Cell;

//import org.semanticweb.owl.model.OWLEntity;
//import org.semanticweb.owl.model.OWLOntology;

/**
 * This class caches the content of the alignment database. I.e.,
 * It loads the metadata in the hash table
 * It stores the alignment when requested
 * It 
 */

public class CacheImpl implements Cache {
    Hashtable alignmentTable = null;
    Hashtable ontologyTable = null;
	
    Statement st = null;
    ResultSet rs = null;
    Connection conn = null;
	
    final int CONNECTION_ERROR = 1;
    final int SUCCESS = 2;
    final int INIT_ERROR = 3;
	
    //**********************************************************************
    public CacheImpl( DBService service ) {
	try {
	    this.conn = service.getConnection();
	    st = (Statement) conn.createStatement();
	} catch(Exception e) {
	    // Rather raise an exception
	    System.err.println(e.toString());
	}
	alignmentTable = new Hashtable();
	ontologyTable = new Hashtable();
    }

    /**
     * loads the alignment descriptions from the database and put them in the
     * alignmentTable hashtable
     */
    public void init() throws SQLException  {
	loadAlignments( true );
    }

    //**********************************************************************
    /**
     * loads the alignment descriptions from the database and put them in the
     * alignmentTable hashtable
     * index them under the ontology URIs
     *
     * Beware, the Alignment API has two attributes:
     * onto1 is the OWLOntology object
     * uri1 is the URI object from which loading the ontologies
     * In the database we store:
     * owlontology1 the URI string of the ontology
     * file1 the URI string from which loading the ontologies
     * uri1 which should be the same as the last one...
     * Since alignments are indexed by the URI of the ontologies, we use
     * the "ouri1" temporary extension to contain this URI.
     */
    private void loadAlignments( boolean force ) throws SQLException {
	String query = null;
	String id = null;
	Alignment alignment = null;
	Vector idInfo = new Vector();
	
	if (force) {
	    // Retrieve the alignment ids
	    query = "select id " + "from alignment";
	    rs = (ResultSet) st.executeQuery(query);
	    while(rs.next()) {
		id = rs.getString("id");
		idInfo.add(id);	
	    }
	    
	    // For each alignment id store metadata
	    for( int i = 0; i < idInfo.size(); i ++ ) {
		id = (String)idInfo.get(i);
		alignment = retrieveDescription( id );
		recordAlignment( id, alignment, true );
	    }							
	}
    }

    protected Enumeration listAlignments() {
	return alignmentTable.elements();
    }

    /**
     * loads the description of alignments from the database and set them
     * in an alignment object
     */
    protected Alignment retrieveDescription( String id ){
	String query;
	String tag;
	String method;

	//*/3.0
	//Alignment result = new BasicAlignment();
	Alignment result = new URIAlignment();
		
	try {
	    // Get basic ontology metadata
	    query = "select * from alignment where id = '" + id  +"'";
	    rs = (ResultSet) st.executeQuery(query);
	    while(rs.next()) {
		// Either uri1 or file1
		result.setFile1( new URI(rs.getString("uri1")) ); 
		result.setFile2( new URI(rs.getString("uri2")) );
		result.setExtension( "ouri1", rs.getString("owlontology1") );
		result.setExtension( "ouri2", rs.getString("owlontology2") );
		result.setLevel(rs.getString("level"));
		result.setType(rs.getString("type"));	
	    }

	    // Get extension metadata
	    query = "select * from extension where id = '" + id + "'";
	    rs = (ResultSet) st.executeQuery(query);
	    while(rs.next()) {
		tag = rs.getString("tag");
		method = rs.getString("method");
		result.setExtension(tag, method);
	    }
	} catch (Exception e) { // URI exception that should not occur
	    System.err.println("Unlikely URI exception!");
	    e.printStackTrace();
	    return null;
	}
	// should be there
	//result.setExtension("fr.inrialpes.exmo.align.service.stored", "DATE");
	// not yet cached
	result.setExtension("fr.inrialpes.exmo.align.service.cached", "");
	return result;
    }

    /**
     * loads the full alignments from the database and put them in the
     * alignmentTable hastable
     * 
     * should be invoked when:
     * 	( result.getExtension("fr.inrialpes.exmo.align.service.cached") == ""
     * && result.getExtension("fr.inrialpes.exmo.align.service.stored") != "") {

     */
    protected Alignment retrieveAlignment( String id, Alignment result ) throws SQLException, AlignmentException, URISyntaxException//*/, OWLException 
{
	//*/3.0
	//OWLOntology o1 = null;
	//OWLOntology o2 = null;
	String query;
	//String tag;
	//String method;
	//*/3.0
	//OWLEntity ent1 = null, ent2 = null;
	URI ent1 = null, ent2 = null;
	Cell cell = null;

	// Load the ontologies
	//*/3.0
	//o1 = loadOntology(result.getFile1());
	//o2 = loadOntology(result.getFile2());
	result.setOntology1( new URI( result.getExtension( "ouri1" ) ) );
	result.setOntology2( new URI( result.getExtension( "ouri2" ) ) );

	// Get extension metadata
	// JE: this has been done already by getDescription (in all cases)
	//query = "select * from extension where id = '" + id + "'";
	//rs = (ResultSet) st.executeQuery(query);
	//while(rs.next()) {
	//    tag = rs.getString("tag");
	//    method = rs.getString("method");
	//    result.setExtension(tag, method);
	//}
	
	// Get cells
	query = "select * from cell where id = '" + id + "'";
	rs = (ResultSet) st.executeQuery(query);
	while(rs.next()) {
	    //*/3.0
	    //ent1 = (OWLEntity) o1.getClass(new URI(rs.getString("uri1")));
	    //ent2 = (OWLEntity) o2.getClass(new URI(rs.getString("uri2")));
	    ent1 = new URI(rs.getString("uri1"));
	    ent2 = new URI(rs.getString("uri2"));
	    if(ent1 == null || ent2 == null) break;
	    cell = result.addAlignCell(ent1, ent2, rs.getString("relation"), Double.parseDouble(rs.getString("measure")));
	    cell.setId(rs.getString("cell_id"));
	    cell.setSemantics(rs.getString("semantics"));
	}
	// It is here
	//result.setExtension("fr.inrialpes.exmo.align.service.stored", "DATE");
	// reset
	resetCacheStamp(result);

	return result;
    }

    private String generateAlignmentId() {
	// Generate an id based on a URI prefix + Date + random number
	Date date;
	String id;
	date = new Date();
	id = "http://localhost:8089/" + date.getTime() + "/" + randomNum();
	return id;
    }
    
    private int randomNum() {
	Random rand = new Random(System.currentTimeMillis());
	return Math.abs(rand.nextInt(1000)); 
    }

    //**********************************************************************
    /**
     * retrieve alignment metadata from id
     * This is more difficult because we return the alignment we have 
     * disreagarding if it is complete o only metadata
     */
    public Alignment getMetadata( String id ) throws Exception {
	Alignment result = (Alignment)alignmentTable.get( id );
	
	if ( result == null )
	    throw new Exception("getMetadata: Cannot find alignment");

	return result;
    }
	
    /**
     * retrieve full alignment from id (and cache it)
     */
    public Alignment getAlignment( String id ) throws Exception {
	Alignment result = (Alignment)alignmentTable.get( id );
	
	if ( result == null )
	    throw new Exception("getAlignment: Cannot find alignment");

	// If not cached, retrieve it now
	if ( result.getExtension("fr.inrialpes.exmo.align.service.cached") == "" 
	     && result.getExtension("fr.inrialpes.exmo.align.service.stored") != "") {
	    retrieveAlignment( id, result );
	}
	
	return result;
    }
	
    //**********************************************************************
    public Set getAlignments( URI uri ) {
	return (Set)ontologyTable.get( uri );
    }

    public Set getAlignments( URI uri1, URI uri2 ) {
	// Create the set and compare
	Set result = new HashSet();
	Set potentials = (Set)ontologyTable.get( uri1 );
	if ( potentials != null ) {
	    for( Iterator it = potentials.iterator(); it.hasNext(); ) {
		Alignment al = (Alignment)it.next();
		// This is not the best because URI are not resolved here...
		if ( al.getExtension("ouri2").equals( uri2.toString() ) ) result.add( al );
	    }
	}
	return result;
    }

    /**
     * records newly created alignment
     */
    public String recordNewAlignment( Alignment alignment, boolean force ) {
	try { return recordNewAlignment( generateAlignmentId(), alignment, force );
	} catch (Exception e) { return (String)null; }
    }

    /**
     * records alignment identified by id
     */
    //*/3.0 In fact this assumes an URIAlignment
    public String recordNewAlignment( String id, Alignment al, boolean force ) throws AlignmentException {
	Alignment alignment = al;
	//if ( alignment instanceof OWLAPIAlignment )
	//    alignment = ((OWLAPIAlignment)al).toURIAlignment();
	// Set the Ontology URIs
	//*/3.0
	//try {
	    //*/3.0
	    //alignment.setExtension("ouri1", ((OWLOntology)alignment.getOntology1()).getLogicalURI().toString());
	    //alignment.setExtension("ouri2", ((OWLOntology)alignment.getOntology2()).getLogicalURI().toString());
	alignment.setExtension("ouri1", alignment.getOntology1URI().toString());
	alignment.setExtension("ouri2", alignment.getOntology2URI().toString());
	    //*/3.0
	    //} catch (OWLException e) {
	    //System.err.println("Unexpected OWL Exception");
	    //e.printStackTrace();
	    //}
	// Index
	recordAlignment( id, alignment, force );
	// Not yet stored
	alignment.setExtension("fr.inrialpes.exmo.align.service.stored", "");
	// Cached now
	resetCacheStamp(alignment);
	return id;
    }

    /**
     * records alignment identified by id
     */
    public String recordAlignment( String id, Alignment alignment, boolean force ){
	// record the Id!
	if ( alignment.getExtension("id") == null )
	    alignment.setExtension( "id", id );
	// Store it
	try {
	    URI ouri1 = new URI( alignment.getExtension("ouri1") );
	    URI ouri2 = new URI( alignment.getExtension("ouri2") );
	    if ( force || alignmentTable.get( id ) == null ) {
		Set s1 = (Set)ontologyTable.get( ouri1 );
		if ( s1 == null ) {
		    s1 = new HashSet();
		    ontologyTable.put( ouri1, s1 );
		}
		s1.add( alignment );
		Set s2 = (Set)ontologyTable.get( ouri2 );
		if ( s2 == null ) {
		    s2 = new HashSet();
		    ontologyTable.put( ouri2, s2 );
		}
		s2.add( alignment );
		alignmentTable.put( id, alignment );
	    }
	    return id;
	} catch (Exception e) {
	    System.err.println("Unlikely URI exception!");
	    e.printStackTrace();
	    return (String)null;
	}
    }

    //**********************************************************************
    public void storeAlignment( String id ) throws Exception {
	String query = null;
	Alignment alignment = null;

	alignment = getAlignment( id );
	//if ( alignment instanceof OWLAPIAlignment )
	//    alignment = ((OWLAPIAlignment)alignment).toURIAlignment();

	// We store stored date
	alignment.setExtension("fr.inrialpes.exmo.align.service.stored", new Date().toString());
	// We empty cached date
	alignment.setExtension("fr.inrialpes.exmo.align.service.cached", "");

	try {
	    // owlontology attribute
	    // JE: This cannot work if the ontology is not loaded!
	    // which should not be the case but who knows?
	    //OWLOntology O1 = (OWLOntology)alignment.getOntology1();
	    //OWLOntology O2 = (OWLOntology)alignment.getOntology2();
	    //String s_O1 = O1.getLogicalURI().toString();
	    //String s_O2 = O2.getLogicalURI().toString();
	    String s_O1 = alignment.getExtension("ouri1");
	    String s_O2 = alignment.getExtension("ouri2");
	    
	    // file attribute
	    String s_File1 = null;
	    String s_File2 = null;
	    if (alignment.getFile1() != null) 
		s_File1 = alignment.getFile1().toString();
	    if (alignment.getFile2() != null) 
		s_File2 = alignment.getFile2().toString();
	    
	    // uri attribute
	    //*/3.0
	    // JE: This cannot work if the ontology is not loaded!
	    // which should not be the case but who knows?
	    //String s_uri1 = ((OWLOntology)alignment.getOntology1()).getPhysicalURI().toString();
	    //String s_uri2 = ((OWLOntology)alignment.getOntology2()).getPhysicalURI().toString();
	    String s_uri1 = alignment.getOntology1URI().toString();
	    String s_uri2 = alignment.getOntology2URI().toString();

	    String type = alignment.getType();
	    String level = alignment.getLevel();
			
	    query = "insert into alignment " + 
		"(id, owlontology1, owlontology2, type, level, file1, file2, uri1, uri2) " +
		"values ('" + id + "','" +  s_O1 + "','" + s_O2 + "','" + type + "','" + level + "','" + s_File1 + "','" + s_File2 + "','" + s_uri1 + "','" + s_uri2 + "')";
	    st.executeUpdate(query);
	    for( Enumeration e = alignment.getExtensions().getNames() ; e.hasMoreElements() ; ){
		String tag = (String)e.nextElement();
		String s_method = alignment.getExtension(tag);
		query = "insert into extension " + 
		    "(id, tag, method) " +
		    "values ('" + id + "','" +  tag + "','" + s_method + "')";
		st.executeUpdate(query);
	    }
	    
	    for( Enumeration e = alignment.getElements() ; e.hasMoreElements(); ){
		Cell c = (Cell)e.nextElement();
		String temp[] = new String[10];
		//*/3.0
		//try {
		    //*/3.0
		    //if ( ((OWLEntity)c.getObject1()).getURI() != null && ((OWLEntity)c.getObject2()).getURI() != null ){
		    if ( c.getObject1() != null && c.getObject2() != null ){
			if ( c.getId() != null ){
			    temp[0] = c.getId();
			} 
			else temp[0] = "";
			//*/3.0
			//temp[1] = ((OWLEntity)c.getObject1()).getURI().toString();
			//temp[2] = ((OWLEntity)c.getObject2()).getURI().toString();
			temp[1] = c.getObject1AsURI().toString();
			temp[2] = c.getObject2AsURI().toString();
			temp[3] = c.getStrength() + "";
			if ( !c.getSemantics().equals("first-order") )
			    temp[4] = c.getSemantics();
			else temp[4] = "";
			temp[5] =  ((BasicRelation)c.getRelation()).getRelation();	
			query = "insert into cell " + 
			    "(id, cell_id, uri1, uri2, measure, semantics, relation) " +
			    "values ('" + id + "','" + temp[0] + "','" + temp[1] + "','" + temp[2] + "','" + temp[3] + "','" + temp[4] + "','" + temp[5] + "')";
			st.executeUpdate(query);
		    }
		
		    //*/3.0
		    //		} catch ( OWLException ex) {
		    // Raise an exception
		    //System.err.println( "getURI problem" + ex.toString() ); }
	    }
	} catch (Exception e) { e.printStackTrace(); };
	// We reset cached date
	resetCacheStamp(alignment);
    }

    //**********************************************************************
    public void resetCacheStamp( Alignment result ){
	result.setExtension("fr.inrialpes.exmo.align.service.cached", new Date().toString() );
    }

    public void cleanUpCache() {
	// for each alignment in the table
	// set currentDate = Date();
	// if ( DateFormat.parse( result.getExtension("fr.inrialpes.exmo.align.service.cached") ).before( ) ) {
	// - for each ontology if no other alignment => unload
	// - clean up cells
	// }
    }

    //**********************************************************************
    /*public OWLOntology loadOntology( URI uri ) throws OWLException {
	OWLRDFParser parser = new OWLRDFParser();
	parser.setConnection(OWLManager.getOWLConnection());
	return parser.parseOntology(uri);
	}*/
}
