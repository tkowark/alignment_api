/*
 * $Id$
 *
 * Copyright (C) XX, 2006
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

import org.semanticweb.owl.io.owl_rdf.OWLRDFParser;
import org.semanticweb.owl.util.OWLManager;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLException;
//import org.semanticweb.owl.util.OWLManager;

import fr.inrialpes.exmo.align.impl.BasicRelation;
import fr.inrialpes.exmo.align.impl.BasicAlignment;

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
	    //System.err.println("1234");
	    while(rs.next()) {
		id = rs.getString("id");
		idInfo.add(id);	
		//System.err.println(id);
	    }
	    
	    // For each alignment id store metadata
	    for( int i = 0; i < idInfo.size(); i ++ ) {
		id = (String)idInfo.get(i);
		alignment = retrieveDescription( id );
		recordAlignment( id, alignment, true );
	    }							
	}
    }

    /**
     * loads the description of alignments from the database and set them
     * in an alignment object
     */
    protected Alignment retrieveDescription( String id ){
	String query;
	String tag;
	String method;
				
	Alignment result = new BasicAlignment();
		
	try {
	    // Get basic ontology metadata
	    query = "select * "+"from alignment "+"where id = " + id;
	    rs = (ResultSet) st.executeQuery(query);
	    while(rs.next()) {
		result.setFile1(new URI(rs.getString("uri1"))); 
		result.setFile2(new URI(rs.getString("uri2"))); 
		result.setLevel(rs.getString("level"));
		result.setType(rs.getString("type"));	
	    }

	    // Get extension metadata
	    query = "select * "+"from extension "+"where id = " + id;
	    rs = (ResultSet) st.executeQuery(query);
	    while(rs.next()) {
		tag = rs.getString("tag");
		method = rs.getString("method");
		result.setExtension(tag, method);
	    }
	} catch (Exception e) {
	    System.err.println("No problem");
	    System.err.println(e.toString());
	    return null;
	}
	result.setExtension("fr.inrialpes.exmo.align.service.stored", "DATE");
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
    protected Alignment retrieveAlignment( String id, Alignment result ) throws SQLException, AlignmentException, URISyntaxException, OWLException {
	OWLOntology o1 = null;
	OWLOntology o2 = null;
	String query;
	String tag;
	String method;
	OWLEntity ent1 = null, ent2 = null;
	Cell cell = null;

	// Load the ontologies
	o1 = loadOntology(result.getFile1());
	o2 = loadOntology(result.getFile2());
	result.setOntology1(o1);
	result.setOntology1(o2);
	
	// Get extension metadata
	query = "select * "+"from extension "+"where id = " + id;
	rs = (ResultSet) st.executeQuery(query);
	while(rs.next()) {
	    tag = rs.getString("tag");
	    method = rs.getString("method");
	    result.setExtension(tag, method);
	}
	
	// Get cells
	query = "select * "+"from cell "+"where id = " + id;
	rs = (ResultSet) st.executeQuery(query);
	while(rs.next()) {
	    ent1 = (OWLEntity) o1.getClass(new URI(rs.getString("uri1")));
	    ent2 = (OWLEntity) o2.getClass(new URI(rs.getString("uri2")));
	    if(ent1 == null || ent2 == null) break;
	    cell = result.addAlignCell(ent1, ent2, rs.getString("relation"), Double.parseDouble(rs.getString("measure")));
	    cell.setId(rs.getString("cell_id"));
	    cell.setSemantics(rs.getString("semantics"));
	}
	result.setExtension("fr.inrialpes.exmo.align.service.stored", "DATE");
	// Put the date here
	result.setExtension("fr.inrialpes.exmo.align.service.cached", "DATE");

	return result;
    }

    private String generateAlignmentId() {
	// Generate an id based on a URI prefix + Date + random number
    Date date;
    String id;
    date = new Date();
    id = "http://blavlacestmoi" + date.getTime() + randomNum();
	return id;
    }
    
    private int randomNum() {
    Random rand = new Random(System.currentTimeMillis());
    return Math.abs(rand.nextInt(1000)); 
    }

    //**********************************************************************
    /**
     * retrieve alignment metadata from id
     */
    public Alignment getMetadata( String id ) {
	Alignment result = null;		
//	String query = null;
	
	result = (Alignment)alignmentTable.get( id );
	
// Raise an exception if no result (by Seungkeun)
	if(result == null) {
		System.out.println("Metadata Loading Error in CacheImpl.getMetadata");
	}		
	return result;
    }
	
    /**
     * retrieve full alignment from id (and cache it)
     */
    public Alignment getAlignment( String id ) throws Exception {
	Alignment result = null;		
//	String query = null;
	
	result = (Alignment)alignmentTable.get( id );
	
//	 Raise an exception if no result
	if(result == null) { 
		System.out.println("Metadata Loading Error in CacheImpl.getMetadata");
	}		
	else if ( result.getExtension("fr.inrialpes.exmo.align.service.cached") == "" && result.getExtension("fr.inrialpes.exmo.align.service.stored") != "") {
	    retrieveAlignment( id, result );
	}	
	
	return result;
    }
	
    //**********************************************************************
    public Set getAlignments( URI uri ) {
	return (Set)ontologyTable.get( uri );
    }

    public Set getAlignments( URI uri1, URI uri2 ) {
	// Crete the set and compare
	return (Set)ontologyTable.get( uri1 );
    }

    /**
     * records newly created alignment
     */
    public void recordAlignment( Alignment alignment, boolean force ){
	recordAlignment( generateAlignmentId(), alignment, force );
    }

    /**
     * records alignment identified by id
     */
    public void recordAlignment( String id, Alignment alignment, boolean force ){
	if ( force || alignmentTable.get( id ) == null ) {
	    Set s1 = (Set)ontologyTable.get( alignment.getFile1() );
	    if ( s1 == null ) {
		s1 = new HashSet();
		ontologyTable.put( alignment.getFile1(), s1 );
	    }
	    s1.add( alignment );
	    Set s2 = (Set)ontologyTable.get( alignment.getFile2() );
	    if ( s2 == null ) {
		s2 = new HashSet();
		ontologyTable.put( alignment.getFile1(), s2 );
	    }
	    s2.add( alignment );
	    alignmentTable.put( id, alignment );
	}
    }		

    //**********************************************************************
    public void storeAlignment( String id ) throws Exception {
	String query = null;
	Alignment alignment = null;

	alignment = getAlignment( id );

	try {
	    OWLOntology O1 = (OWLOntology)alignment.getOntology1();
	    OWLOntology O2 = (OWLOntology)alignment.getOntology2();
	    String s_O1 = O1.getLogicalURI().toString();
	    String s_O2 = O2.getLogicalURI().toString();
	    
	    String s_File1 = null;
	    String s_File2 = null;
	    if (alignment.getFile1() != null) 
		s_File1 = alignment.getFile1().toString();
	    if (alignment.getFile2() != null) 
		s_File2 = alignment.getFile2().toString();
	    
	    String s_uri1 = O1.getPhysicalURI().toString();
	    String s_uri2 = O2.getPhysicalURI().toString();
	    
	    String type = alignment.getType();
	    String level = alignment.getLevel();
			
	    query = "insert into alignment " + 
		"(id, owlontology1, owlontology2, type, level, file1, file2, uri1, uri2) " +
		"values (" + id + ",'" +  s_O1 + "','" + s_O2 + "','" + type + "','" + level + "','" + s_File1 + "','" + s_File2 + "','" + s_uri1 + "','" + s_uri2 + "')";
	    st.executeUpdate(query);
	    for( Enumeration e = alignment.getExtensions().getNames() ; e.hasMoreElements() ; ){
		String tag = (String)e.nextElement();
		String s_method = alignment.getExtension(tag);
		query = "insert into extension " + 
		    "(id, tag, method) " +
		    "values (" + id + ",'" +  tag + "','" + s_method + "')";
		st.executeUpdate(query);
	    }
	    
	    for( Enumeration e = alignment.getElements() ; e.hasMoreElements(); ){
		Cell c = (Cell)e.nextElement();
		String temp[] = new String[10];
		try {
		    if ( ((OWLEntity)c.getObject1()).getURI() != null && ((OWLEntity)c.getObject2()).getURI() != null ){
			if ( c.getId() != null ){
			    temp[0] = c.getId();
			} 
			else temp[0] = "";
			temp[1] = ((OWLEntity)c.getObject1()).getURI().toString();
			temp[2] = ((OWLEntity)c.getObject2()).getURI().toString();
			temp[3] = c.getStrength() + "";
			if ( !c.getSemantics().equals("first-order") )
			    temp[4] = c.getSemantics();
			else temp[4] = "";
			temp[5] =  ((BasicRelation)c.getRelation()).getRelation();	
			query = "insert into cell " + 
			    "(id, cell_id, uri1, uri2, measure, semantics, relation) " +
			    "values (" + id + ",'" + temp[0] + "','" + temp[1] + "','" + temp[2] + "','" + temp[3] + "','" + temp[4] + "','" + temp[5] + "')";
			st.executeUpdate(query);
		    }
				    
		} catch ( OWLException ex) {
		    // Raise an exception
		    System.err.println( "getURI problem" + ex.toString() ); }
	    }
	} catch (Exception e) {
	    System.err.println(e.toString());
	}
    }

    //**********************************************************************
    public static OWLOntology loadOntology( URI uri ) throws OWLException {
	OWLRDFParser parser = new OWLRDFParser();
	parser.setConnection(OWLManager.getOWLConnection());
	return parser.parseOntology(uri);
    }
}
