/*
 * $Id$
 *
 * Copyright (C) Seungkeun Lee, 2006
 * Copyright (C) INRIA, 2006-2010
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
import java.util.Collection;
import java.util.Set;
import java.util.HashSet;
import java.util.Date;
import java.util.Random;
import java.util.Properties;
import java.net.URI;
import java.net.URISyntaxException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.BasicRelation;
import fr.inrialpes.exmo.align.impl.Annotations;
import fr.inrialpes.exmo.align.impl.Namespace;
import fr.inrialpes.exmo.align.impl.BasicParameters;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.align.impl.URICell;
import fr.inrialpes.exmo.align.impl.Namespace;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Cell;
import java.io.PrintStream;
import java.io.EOFException;

/**
 * This class caches the content of the alignment database. I.e.,
 * It loads the metadata in the hash table
 * It stores the alignment when requested
 * It 
 */

public class CacheImpl {
    Hashtable<String,Alignment> alignmentTable = null;
    Hashtable<URI,Set<Alignment>> ontologyTable = null;

    String host = null;
    String port = null;
    int rights = 1; // writing rights in the database (default is 1)

    final int VERSION = 340; // Version of the API to be stored in the database
    /* 300: initial database format
       301: added alignment id as primary key
       302: changed cached/stored/ouri tag forms
       310: changed extension table with added URIs and method -> val 
       340: changed size of relation in cell table (5 -> 25)
     */

    DBService service = null;
    Connection conn = null;
	
    final int CONNECTION_ERROR = 1;
    final int SUCCESS = 2;
    final int INIT_ERROR = 3;

    //static public final String SVCNS = "http://exmo.inrialpes.fr/align/service#";
    static public final String SVCNS = Namespace.ALIGNSVC.getUriPrefix();
    static public final String CACHED = "cached";
    static public final String STORED = "stored";
    static public final String OURI1 = "ouri1";
    static public final String OURI2 = "ouri2";
	
    //**********************************************************************

    public CacheImpl( DBService serv ) {
	service = serv;
	try {
	    conn = service.getConnection();
	} catch(Exception e) {
	    // Rather raise an exception
	    //System.err.println(e.toString());
	}
	alignmentTable = new Hashtable<String,Alignment>();
	ontologyTable = new Hashtable<URI,Set<Alignment>>();
    }

    public void reset() throws SQLException {
	alignmentTable = new Hashtable<String,Alignment>();
	ontologyTable = new Hashtable<URI,Set<Alignment>>();
	// reload alignment descriptions
	loadAlignments( true );
    }

    /**
     * loads the alignment descriptions from the database and put them in the
     * alignmentTable hashtable
     */
    public void init( Properties p ) throws SQLException, AlignmentException {
	port = p.getProperty("http"); // bad idea
	host = p.getProperty("host");
	Statement st = createStatement();
	// test if a database is here, otherwise create it
	ResultSet rs = conn.getMetaData().getTables(null,null, "server", new String[]{"TABLE"});
	if ( !rs.next() ) {
	    initDatabase();
	} else {
	    updateDatabase(); // in case it is necessary to upgrade
	}
	// register by the database
	st.executeUpdate("INSERT INTO server (host, port, edit, version) VALUES ('"+host+"','"+port+"','"+rights+"',"+VERSION+")");
	st.close();
	// load alignment descriptions
	loadAlignments( true );
    }

    public void close() throws SQLException  {
	Statement st = (Statement)conn.createStatement();
	// unregister by the database
	st.executeUpdate("DELETE FROM server WHERE host='"+host+"' AND port='"+port+"'");
	st.close();
	conn.close();
    }

    public Statement createStatement() throws SQLException {
	conn = service.getConnection();
	return conn.createStatement();
    }

    // **********************************************************************
    // LOADING FROM DATABASE
    /**
     * loads the alignment descriptions from the database and put them in the
     * alignmentTable hashtable
     * index them under the ontology URIs
     */
    private void loadAlignments( boolean force ) throws SQLException {
	String query = null;
	String id = null;
	Alignment alignment = null;
	Vector<String> idInfo = new Vector<String>();
	Statement st = createStatement();
	
	if (force) {
	    // Retrieve the alignment ids
	    ResultSet rs = (ResultSet) st.executeQuery("SELECT id FROM alignment");
	    while(rs.next()) {
		id = rs.getString("id");
		idInfo.add(id);	
	    }
	    
	    // For each alignment id store metadata
	    for( int i = 0; i < idInfo.size(); i ++ ) {
		id = idInfo.get(i);
		alignment = retrieveDescription( id );
		recordAlignment( id, alignment, true );
	    }							
	}
	st.close();
    }

    protected Enumeration<Alignment> listAlignments() {
	return alignmentTable.elements();
    }

    protected Collection<Alignment> alignments() {
	return alignmentTable.values();
    }

    protected void flushCache() {// throws AlignmentException
	for ( Alignment al : alignmentTable.values() ){
	    if ( al.getExtension( SVCNS, CACHED ) != ""
		 && al.getExtension( SVCNS, STORED ) != "" ) flushAlignment( al );
	};
    }

    /**
     * loads the description of alignments from the database and set them
     * in an alignment object
     *
     * Beware, the Alignment API has two attributes:
     * onto1 is the Ontology object
     * uri1 is the URI object from which loading the ontologies
     * In the database we store:
     * owlontology1 the URI string of the ontology
     * file1 the URI string from which loading the ontologies
     * uri1 which should be the same as the last one...
     * Since alignments are indexed by the URI of the ontologies, we use
     * the "ouri1" temporary extension to contain this URI.
     */
    protected Alignment retrieveDescription( String id ){
	String query;
	ResultSet rs;
	String tag;
	String value;

	URIAlignment result = new URIAlignment();
		
	try {
	    Statement st = createStatement();
	    // Get basic ontology metadata
	    query = "SELECT * FROM alignment WHERE id = '" + id  +"'";
	    rs = (ResultSet) st.executeQuery(query);
	    while(rs.next()) {
		// Either uri1 or file1
		result.setFile1( new URI( rs.getString("file1") ) ); 
		result.setFile2( new URI( rs.getString("file2") ) );
		result.getOntologyObject1().setURI( new URI(rs.getString("owlontology1"))  );
		result.getOntologyObject2().setURI( new URI(rs.getString("owlontology2"))  );
		result.setExtension( SVCNS, OURI1, rs.getString("owlontology1") );
		result.setExtension( SVCNS, OURI2, rs.getString("owlontology2") );
		result.setLevel(rs.getString("level"));
		result.setType(rs.getString("type"));	
	    }

	    // Get extension metadata
	    query = "SELECT * FROM extension WHERE id = '" + id + "'";
	    rs = (ResultSet) st.executeQuery(query);
	    while(rs.next()) {
		tag = rs.getString("tag");
		value = rs.getString("val");
		result.setExtension( rs.getString("uri"), tag, value);
	    }
	    st.close();
	} catch (Exception e) { // URI exception that should not occur
	    System.err.println("Unlikely URI exception!");
	    e.printStackTrace();
	    return null;
	}
	// has been extracted from the database
	//result.setExtension( SVCNS, STORED, "DATE");
	// not yet cached
	result.setExtension(SVCNS, CACHED, "");
	return result;
    }

    /**
     * loads the full alignments from the database and put them in the
     * alignmentTable hastable
     * 
     * should be invoked when:
     * 	( result.getExtension(CACHED) == ""
     * && result.getExtension(STORED) != "") {

     */
    protected Alignment retrieveAlignment( String id, Alignment alignment ) throws SQLException, AlignmentException, URISyntaxException {
	String query;
	URI ent1 = null, ent2 = null;

	Statement st = createStatement();

	alignment.setOntology1( new URI( alignment.getExtension( SVCNS, OURI1 ) ) );
	alignment.setOntology2( new URI( alignment.getExtension( SVCNS, OURI2 ) ) );

	// Get cells
	query = "SELECT * FROM cell WHERE id = '" + id + "'";
	ResultSet rs = (ResultSet) st.executeQuery(query);
	while(rs.next()) {
	    ent1 = new URI(rs.getString("uri1"));
	    ent2 = new URI(rs.getString("uri2"));
	    if(ent1 == null || ent2 == null) break;
	    Cell cell = alignment.addAlignCell(ent1, ent2, rs.getString("relation"), Double.parseDouble(rs.getString("measure")));
	    cell.setId(rs.getString("cell_id"));
	    cell.setSemantics(rs.getString("semantics"));

	}

	// JE: I must now retrieve all the extensions of the cells
	for ( Cell cell: alignment ){
	    String cid = cell.getId();
	    if ( cid != null && !cid.equals("") ){
		query = "SELECT * FROM extension WHERE id = '" + cid + "'";
		ResultSet rse = (ResultSet) st.executeQuery(query);
		while ( rse.next() ){
		    cell.setExtension( rse.getString("uri"), 
				       rse.getString("tag"), 
				       rse.getString("val") );
		}
	    }
	}
	// reset
	resetCacheStamp(alignment);
	st.close();
	return alignment;
    }
    
    /**
     * unload the cells of an alignment...
     * This should help retrieving some space
     * 
     * should be invoked when:
     * 	( result.getExtension(CACHED) != ""
     *  && obviously result.getExtension(STORED) != ""
     */
    protected void flushAlignment( Alignment alignment ) {// throws AlignmentException
	//alignment.removeAllCells();
	// reset
    	alignment.setExtension( SVCNS, CACHED, "" );
    }
    
    // Public because this is now used by AServProtocolManager
    public String generateAlignmentId() {
	// Generate an id based on a URI prefix + Date + random number
	return "http://"+host+":"+port+"/alid/" + new Date().getTime() + "/" + randomNum();
    }
    
    private String generateCellId( String alId ) {
	// Generate an id based on a URI prefix + Date + random number
	int end = alId.indexOf("/alid/");
	if ( end == -1 || alId.indexOf( '#' ) != -1 ) {
	    return "http://"+host+":"+port+"/cellid/" + new Date().getTime() + "/" + randomNum();
	} else {
	    return alId + "#" + randomNum();
	}
    }
    
    private int randomNum() {
	Random rand = new Random(System.currentTimeMillis());
	return Math.abs(rand.nextInt(1000)); 
    }

    //**********************************************************************
    // FETCHING FROM CACHE
    /**
     * retrieve alignment metadata from id
     * This is more difficult because we return the alignment we have 
     * disreagarding if it is complete o only metadata
     */
    public Alignment getMetadata( String id ) throws AlignmentException {
	Alignment result = alignmentTable.get( id );
	if ( result == null )
	    throw new AlignmentException("getMetadata: Cannot find alignment");
	return result;
    }
	
    /**
     * retrieve full alignment from id (and cache it)
     */
    public Alignment getAlignment( String id ) throws AlignmentException, SQLException {
	Alignment result = null;
	try {
	    result = alignmentTable.get( id );
	} catch(Exception ex) {
	    System.err.println("Unknown exception: Id =" + id);
	    ex.printStackTrace();
	}
	
	if ( result == null ) {
	    System.err.println("Cache: Id =" + id + " is not found.");
	    throw new AlignmentException("getAlignment: Cannot find alignment");
	}

	// If not cached, retrieve it now
	if ( result.getExtension( SVCNS, CACHED) == "" 
	     && result.getExtension(SVCNS, STORED) != "") {
	    try { retrieveAlignment( id, result ); }
	    catch (URISyntaxException urisex) {
		System.err.println("Cache: cannot read from DB");
		throw new AlignmentException("getAlignment: Cannot find alignment", urisex);
	    };
	}
	return result;
    }
	
    public Set<Alignment> getAlignments( URI uri ) {
	return ontologyTable.get( uri );
    }

    /**
     * returns the alignmants between two ontologies
     * if one of the ontologies is null, then return them all
     */
    public Set<Alignment> getAlignments( URI uri1, URI uri2 ) {
	Set<Alignment> result;
	Set<Alignment> potential = new HashSet<Alignment>();

	// Just does not work properly if there is a uri2 but no result
	if ( uri2 != null ){
	    String uri2String = uri2.toString();
	    for(  Alignment al : ontologyTable.get( uri2 ) ) {
		if ( al.getExtension(SVCNS, OURI2).equals( uri2String ) ) {
		    potential.add( al );
		}
	    }
	} 
	if ( uri1 != null ) {
	    if ( potential.isEmpty() ) potential = ontologyTable.get( uri1 );
	    result = new HashSet<Alignment>();
	    for(  Alignment al : potential ) {
		String uri1String = uri1.toString();
		// This is not the best because URI are not resolved here...
		if ( al.getExtension(SVCNS, OURI1).equals( uri1String ) ) {
		    result.add( al );
		}
	    }
	} else { result = potential; }
	return result;
    }

    //**********************************************************************
    // RECORDING ALIGNMENTS
    /**
     * records newly created alignment
     */
    public String recordNewAlignment( Alignment alignment, boolean force ) {
	try { return recordNewAlignment( generateAlignmentId(), alignment, force );
	} catch (AlignmentException ae) { return (String)null; }
    }

    /**
     * records alignment identified by id
     */
    public String recordNewAlignment( String id, Alignment al, boolean force ) throws AlignmentException {
	Alignment alignment = al;
 
	alignment.setExtension(SVCNS, OURI1, alignment.getOntology1URI().toString());
	alignment.setExtension(SVCNS, OURI2, alignment.getOntology2URI().toString());
	// Index
	recordAlignment( id, alignment, force );
	// Not yet stored
	alignment.setExtension(SVCNS, STORED, (String)null);
	// Cached now
	resetCacheStamp(alignment);
	return id;
    }

    /**
     * records alignment identified by id
     */
    public String recordAlignment( String id, Alignment alignment, boolean force ){
	// record the Id!
	//CLD put in comment this line for allowing to create a new ID for any alignment  
	//if ( alignment.getExtension( Namespace.ALIGNMENT.uri, Annotations.ID ) == null )
	alignment.setExtension( Namespace.ALIGNMENT.uri, Annotations.ID, id );
 
	// Store it
	try {
	    URI ouri1 = new URI( alignment.getExtension( SVCNS, OURI1) );
	    URI ouri2 = new URI( alignment.getExtension( SVCNS, OURI2) );
	    if ( force || alignmentTable.get( id ) == null ) {
		Set<Alignment> s1 = ontologyTable.get( ouri1 );
		if ( s1 == null ) {
		    s1 = new HashSet<Alignment>();
		    ontologyTable.put( ouri1, s1 );
		}
		s1.add( alignment );
		Set<Alignment> s2 = ontologyTable.get( ouri2 );
		if ( s2 == null ) {
		    s2 = new HashSet<Alignment>();
		    ontologyTable.put( ouri2, s2 );
		}
		s2.add( alignment );
		alignmentTable.put( id, alignment );
	    }
	    return id;
	} catch (Exception e) {
	    //System.err.println("Unlikely URI exception!");
	    e.printStackTrace();
	    return null;
	}
    }
    
    //**********************************************************************
    // STORING IN DATABASE
    /**
     * quote:
     * Prepare a string to be used in SQL queries by preceeding occurences of
     * "'", """, and "\" by a "\".
     * This should be implemented at a lower level within Java itself
     * (or the sql package).
     * This function is used here for protecting everything to be entered in
     * the database
     */
    public String quote( String s ) {
	String result = null;
	char[] chars = s.toCharArray();
	int j = 0;
	int i = 0;
	char c;
	for ( ; i < chars.length; i++ ){
	    c = chars[i];
	    if ( c == '\'' || c == '"' || c == '\\' ) {
		result += new String( chars, j, i-j ) + "\\" + c;
		j = i+1;
	    };
	}
	if ( result != null ) {
	    return result + new String( chars, j, i-j );
	} else {
	    return s;
	}
    }

    public boolean isAlignmentStored( Alignment alignment ) {
	if ( alignment.getExtension( SVCNS, STORED ) != null &&
	     !alignment.getExtension( SVCNS, STORED ).equals("") )
	    return true;
	else return false;
    }

    /**
     * Non publicised class
     */
    public void unstoreAlignment( String id ) throws SQLException, AlignmentException {
	Alignment alignment = getAlignment( id );
	if ( alignment != null ) {
	    Statement st = createStatement();
	    String query = "DELETE FROM extension WHERE id=''";
	    st.executeUpdate(query);
	    query = "DELETE FROM alignment WHERE id=''";
	    st.executeUpdate(query);
	    query = "DELETE FROM cell WHERE id=''";
	    st.executeUpdate(query);
	    alignment.setExtension( SVCNS, STORED, (String)null);
	    st.close();
	}
    }

    public void storeAlignment( String id ) throws AlignmentException, SQLException  {
	String query = null;
	Alignment alignment = getAlignment( id );
	Statement st = null;
	// We store stored date
	alignment.setExtension( SVCNS, STORED, new Date().toString());
	// We empty cached date
	alignment.setExtension( SVCNS, CACHED, "");

	//Try to store at most 3 times. Otherwise, an exception EOFException will be thrown.
	for(int i=0; i < 3 ; i++) {
	    st = createStatement();
	    try {
	    	String s_O1 = alignment.getExtension(SVCNS, OURI1);
	    	String s_O2 = alignment.getExtension(SVCNS, OURI2);
		
	    	// file attribute
		String s_File1 = null;
	    	String s_File2 = null;
	    	if (alignment.getFile1() != null) 
			s_File1 = alignment.getFile1().toString();
	    	if (alignment.getFile2() != null) 
			s_File2 = alignment.getFile2().toString();
	    
	    	// uri attribute
	    	String s_uri1 = alignment.getOntology1URI().toString();
	    	String s_uri2 = alignment.getOntology2URI().toString();

	    	String type = alignment.getType();
	    	String level = alignment.getLevel();
			
	    	query = "INSERT INTO alignment " + 
		    "(id, owlontology1, owlontology2, type, level, file1, file2, uri1, uri2) " +
		    "VALUES ('" + quote(id) + "','" +  quote(s_O1) + "','" + quote(s_O2) + "','" + quote(type) + "','" + quote(level) + "','" + quote(s_File1) + "','" + quote(s_File2) + "','" + quote(s_uri1) + "','" + quote(s_uri2) + "')";
	    	st.executeUpdate(query);
	    	for ( String[] ext : alignment.getExtensions() ) {
		    String uri = ext[0];
		    String tag = ext[1];
		    String val = ext[2];
		    query = "INSERT INTO extension " + 
			"(id, uri, tag, val) " +
			"VALUES ('" + quote(id) + "','" +  quote(uri) + "','" +  quote(tag) + "','" + quote(val) + "')";
		    st.executeUpdate(query);
	    	}

	    	for( Cell c : alignment ) {
		    String cellid = null;
		    if ( c.getObject1() != null && c.getObject2() != null ){
			cellid = c.getId();
			if ( cellid != null ){
			    if ( cellid.startsWith("#") ) {
				cellid = alignment.getExtension( Namespace.ALIGNMENT.uri, Annotations.ID ) + cellid;
			    }
			} else if ( c.getExtensions() != null ) {
			    // JE: In case of extensions create an ID
			    c.setId( generateCellId( id ) );
			    cellid = c.getId();
			}
			else cellid = "";
			String uri1 = c.getObject1AsURI(alignment).toString();
			String uri2 = c.getObject2AsURI(alignment).toString();
			String strength = c.getStrength() + ""; // crazy Java
			String sem;
			if ( !c.getSemantics().equals("first-order") )
			    sem = c.getSemantics();
			else sem = "";
			String rel =  ((BasicRelation)c.getRelation()).getRelation();	
			query = "INSERT INTO cell " + 
			    "(id, cell_id, uri1, uri2, measure, semantics, relation) " +
			    "VALUES ('" + quote(id) + "','" + quote(cellid) + "','" + quote(uri1) + "','" + quote(uri2) + "','" + quote(strength) + "','" + quote(sem) + "','" + quote(rel) + "')";
			st.executeUpdate(query);
		    }
		    if ( cellid != null && !cellid.equals("") && c.getExtensions() != null ) {
		    	// JE: I must now store all the extensions
		    	// JE:EXT
			for ( String[] ext : c.getExtensions() ) {
			    String uri = ext[0];
			    String tag = ext[1];
			    String val = ext[2];
			    query = "INSERT INTO extension " + 
				"(id, uri, tag, val) " +
				"VALUES ('" + quote(cellid) + "','" +  quote(uri) + "','" +  quote(tag) + "','" + quote(val) + "')";
			    st.executeUpdate(query);
			}
		    }
	    	}
	    } catch (Exception e) { 
		// We should cancel this... IF NOT STORED!
		//System.err.println("Cannot store id=" + id );
		alignment.setExtension( SVCNS, STORED, (String)null );
		e.printStackTrace();
		continue;
	    };
	    break;
	}
	st.close();
	// We reset cached date
	resetCacheStamp(alignment);
    }

    //**********************************************************************
    // CACHE MANAGEMENT (Not implemented yet)
    public void resetCacheStamp( Alignment result ){
	result.setExtension(SVCNS, CACHED, new Date().toString() );
    }

    public void cleanUpCache() {
	// for each alignment in the table
	// set currentDate = Date();
	// if ( DateFormat.parse( result.getExtension(SVCNS, CACHED) ).before( ) ) {
	// - for each ontology if no other alignment => unload
	// - clean up cells
	// }
    }

    // **********************************************************************
    // DATABASE CREATION AND UPDATING
    /*
      # server info

      create table server (
      host varchar(50),
      port varchar(5),
      edit varchar(5)
      );
   

      # alignment info
      
      create table alignment (
      id varchar(100), 
      owlontology1 varchar(250),
      owlontology2 varchar(250),
      type varchar(5),
      level varchar(1),
      file1 varchar(250),
      file2 varchar(250),
      uri1 varchar(250),
      uri2 varchar(250),
      primary key (id));

      # cell info

      create table cell(
      id varchar(100),
      cell_id varchar(250),
      uri1 varchar(250),
      uri2 varchar(250),
      semantics varchar(30),
      measure varchar(20),
      relation varchar(5));

      # extension info
      
      create table extension(
      id varchar(100),
      uri varchar(200),
      tag varchar(50),
      val varchar(500));

    */

    public void initDatabase() throws SQLException {
	Statement st = createStatement();
	// Create tables
	st.executeUpdate("CREATE TABLE alignment (id VARCHAR(100), owlontology1 VARCHAR(250), owlontology2 VARCHAR(250), type VARCHAR(5), level VARCHAR(1), file1 VARCHAR(250), file2 VARCHAR(250), uri1 VARCHAR(250), uri2 VARCHAR(250), primary key (id))");
	st.executeUpdate("CREATE TABLE cell(id VARCHAR(100), cell_id VARCHAR(250), uri1 VARCHAR(250), uri2 VARCHAR(250), semantics VARCHAR(30), measure VARCHAR(20), relation VARCHAR(10))");
	st.executeUpdate("CREATE TABLE extension(id VARCHAR(100), uri VARCHAR(200), tag VARCHAR(50), val VARCHAR(500))");
	st.executeUpdate("CREATE TABLE server (host VARCHAR(50), port VARCHAR(5), edit BOOLEAN, version VARCHAR(5))");
	st.close();

	// Because of the values (that some do not like, this is a special statement
	PreparedStatement pst = conn.prepareStatement("INSERT INTO server (host, port, edit, version) VALUES ('dbms','port',?,?)");
	pst.setBoolean(1,false);
	pst.setString(2,VERSION+"");
	pst.executeUpdate();
	pst.close();
    }

    public void resetDatabase( boolean force ) throws SQLException, AlignmentException {
	Statement st = createStatement();
	// Check that no one else is connected...
	if ( force != true ){
	    ResultSet rs = (ResultSet) st.executeQuery("SELECT COUNT(*) AS rowcount FROM server WHERE edit=1");
	    rs.next();
	    int count = rs.getInt("rowcount") ;
	    rs.close() ;
	    if ( count > 1 ) {
		throw new AlignmentException("Cannot init database: other processes use it");
	    }
	}
	// Suppress old database if exists
	st.executeUpdate("DROP TABLE IF EXISTS server");
	st.executeUpdate("DROP TABLE IF EXISTS alignment");
	st.executeUpdate("DROP TABLE IF EXISTS cell");
	st.executeUpdate("DROP TABLE IF EXISTS extension");
	// Redo it
	initDatabase();
	st.close();
	
	// Register this server, etc. characteristics (incl. version name)
	PreparedStatement pst = conn.prepareStatement("INSERT INTO server (host, port, edit, version) VALUES (?,?,?,?)");
	pst.setString(1,host);
	pst.setString(2,port);
	pst.setBoolean(3,rights==1);
	pst.setString(4,VERSION+"");
	pst.executeUpdate();
	pst.close();
    }

    public void updateDatabase() throws SQLException, AlignmentException {
	Statement st = createStatement();
	// get the version number
	ResultSet rs = (ResultSet) st.executeQuery("SELECT version FROM server WHERE port='port'");
	rs.next();
	int version = rs.getInt("version") ;
	if ( version < VERSION ) {
	    if ( version >= 302 ) {
		if ( version < 310 ) {
		    // Change database
		    st.executeUpdate("ALTER TABLE extension CHANGE method val VARCHAR(500)");
		    st.executeUpdate("ALTER TABLE extension ADD uri VARCHAR(200);");
		    // Modify extensions
		    ResultSet rse = (ResultSet) st.executeQuery("SELECT * FROM extension");
		    Statement st2 = createStatement();
		    while ( rse.next() ){
			String tag = rse.getString("tag");
			//System.err.println(" Treating tag "+tag+" of "+rse.getString("id"));
			if ( !tag.equals("") ){
			    int pos;
			    String ns;
			    String name;
			    if ( (pos = tag.lastIndexOf('#')) != -1 ) {
				ns = tag.substring( 0, pos );
				name = tag.substring( pos+1 );
			    } else if ( (pos = tag.lastIndexOf(':')) != -1 && pos > 5 ) {
				ns = tag.substring( 0, pos )+"#";
				name = tag.substring( pos+1 );
			    } else if ( (pos = tag.lastIndexOf('/')) != -1 ) {
				ns = tag.substring( 0, pos+1 );
				name = tag.substring( pos+1 );
			    } else {
				ns = Namespace.ALIGNMENT.uri;
				name = tag;
			    }
			    //System.err.println("  >> "+ns+" : "+name);
			    st2.executeUpdate("UPDATE extension SET tag='"+name+"', uri='"+ns+"' WHERE id='"+rse.getString("id")+"' AND tag='"+tag+"'");
			}
		    }
		}
		if ( version < 340 ) {
		    // Change database
		    st.executeUpdate("ALTER TABLE cell CHANGE relation relation VARCHAR(25)");
		}
		// Change version
		st.executeUpdate("UPDATE server SET version='"+VERSION+"' WHERE port='port'");
	    } else {
		throw new AlignmentException("Database must be upgraded ("+version+" -> "+VERSION+")");
	    }
	}
	st.close();
    }

}
