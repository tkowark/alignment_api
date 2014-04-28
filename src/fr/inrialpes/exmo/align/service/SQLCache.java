/*
 * $Id$
 *
 * Copyright (C) Seungkeun Lee, 2006
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
 */

package fr.inrialpes.exmo.align.service;

//import java.util.Iterator;
//import java.util.Map.Entry;
import java.util.Vector;
//import java.util.Set;
import java.util.Date;
import java.util.Properties;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.BasicRelation;
import fr.inrialpes.exmo.align.impl.BasicOntologyNetwork;
import fr.inrialpes.exmo.align.impl.Annotations;
import fr.inrialpes.exmo.align.impl.Namespace;
import fr.inrialpes.exmo.align.impl.URIAlignment;

import fr.inrialpes.exmo.ontowrap.Ontology;

import org.semanticweb.owl.align.OntologyNetwork;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Cell;

/**
 * This class implements, in addition to VolatilCache,
 * the persistent storage of Alignments and networks
 * In a SQL database.
 *
 * It loads Alignments and network from the database
 * It stores them in the database on demand
 */

public class SQLCache extends VolatilCache implements Cache {
    final static Logger logger = LoggerFactory.getLogger( SQLCache.class );

    String host = null;
    String port = null;
    int rights = 1; // writing rights in the database (default is 1)

    final int VERSION = 461; // Version of the API to be stored in the database
    /* 300: initial database format
       301: ADDED alignment id as primary key
       302: ALTERd cached/stored/ouri tag forms
       310: ALTERd extension table with added URIs and method -> val 
       340: ALTERd size of relation in cell table (5 -> 25)
       400: ALTERd size of relation in cell table (5 -> 255 because of URIs)
            ALTERd all URI size to 255
	    ALTERd level size to 25
            ADDED cell_id as keys?
       450: ADDED ontology database / reduced alignment database
	    ADDED prefix in server
            ADDED dependency database (no action)
       461: CHANGED THE STICKER BECAUSE OF IMPLEMENTATION
     */

    DBService service = null;
    Connection conn = null;
	
    final int CONNECTION_ERROR = 1;
    final int SUCCESS = 2;
    final int INIT_ERROR = 3;

    //**********************************************************************

    public SQLCache( DBService serv ) {
	service = serv;
	try {
	    conn = service.getConnection();
	} catch( Exception e ) {
	    logger.warn( "Cannot connect to DB", e );
	}
	resetTables();
    }

    public void reset() throws AlignmentException {
	resetTables();
	// reload alignment descriptions
	try {
	    load( true );
	} catch ( SQLException sqlex ) {
	    throw new AlignmentException( "SQL exception", sqlex );
	}

    }

    /**
     * loads the alignment descriptions from the database and put them in the
     * alignmentTable hashtable
     */
    public void init( Properties p, String prefix ) throws AlignmentException {
	super.init( p, prefix );
	port = p.getProperty("http"); // bad idea
	host = p.getProperty("host");
	try {
	    Statement st = createStatement();
	    // test if a database is here, otherwise create it
	    ResultSet rs = conn.getMetaData().getTables(null,null, "server", new String[]{"TABLE"});
	    if ( !rs.next() ) {
		initDatabase();
	    } else {
		updateDatabase(); // in case it is necessary to upgrade
	    }
	    String pref = p.getProperty("prefix");
	    if ( pref == null || pref.equals("") ) {
		rs = st.executeQuery( "SELECT prefix FROM server WHERE port='port'" );
		while( rs.next() ) {
		    idprefix = rs.getString("prefix");
		}
	    }
	    // register by the database
	    registerServer( host, port, rights==1, idprefix );
	    // load alignment descriptions
	    load( true );
	    st.close();
	} catch (SQLException sqlex) {
	    throw new AlignmentException( "SQLException", sqlex );
	}
    }

    public void close() throws AlignmentException  {
	try {
	    Statement st = createStatement();
	    // unregister by the database
	    st.executeUpdate( "DELETE FROM server WHERE host='"+host+"' AND port='"+port+"'" );
	    st.close();
	    conn.close();
	} catch (SQLException sqlex) {
	    throw new AlignmentException( "SQL Exception", sqlex );
	}
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
    private void load( boolean force ) throws SQLException {
	logger.debug( "Loading alignments..." );
	String id = null;
	Alignment alignment = null;
	Vector<String> idInfo = new Vector<String>();
	Statement st = createStatement();
	
	if (force) {
	    // Retrieve the alignment ids
	    ResultSet rs = st.executeQuery("SELECT id FROM alignment");
	    while(rs.next()) {
		id = rs.getString("id");
		idInfo.add(id);	
	    }
	    
	    // For each alignment id store metadata
	    for( int i = 0; i < idInfo.size(); i ++ ) {
		id = idInfo.get(i);
		alignment = retrieveDescription( id );
		recordAlignment( recoverAlignmentUri( id ), alignment, true );
	    }							
	}
	st.close();
    }
    
    /**
     * loads the description of alignments from the database and set them
     * in an alignment object
     */
    protected Alignment retrieveDescription( String id ){
	ResultSet rs;
	String tag;
	String value;

	logger.debug( "Loading alignment {}", id );
	URIAlignment result = new URIAlignment();
	Statement st = null;
	try {
	    st = createStatement();
	    // Get basic ontology metadata
	    rs = st.executeQuery( "SELECT * FROM alignment WHERE id = '" + id  +"'" );
	    while( rs.next() ) {
		result.setLevel(rs.getString("level"));
		result.setType(rs.getString("type"));	
	    }

	    // Get ontologies
	    rs = st.executeQuery( "SELECT * FROM ontology WHERE id = '" + id  +"'" );
	    while(rs.next()) {
		if ( rs.getBoolean("source") ) {
		    result.getOntologyObject1().setURI( new URI(rs.getString("uri"))  );
		    if ( rs.getString("file") != null ) 
		       result.setFile1( new URI( rs.getString("file") ) );
		    if ( rs.getString("formuri") != null ) 
			result.getOntologyObject1().setFormURI( new URI(rs.getString("formuri"))  );
		    if ( rs.getString("formname") != null ) 
			result.getOntologyObject1().setFormalism( rs.getString("formname")  );
		    result.setExtension( SVCNS, OURI1, rs.getString("uri") );
		} else {
		    result.getOntologyObject2().setURI( new URI(rs.getString("uri"))  );
		    if ( rs.getString("file") != null ) 
			result.setFile2( new URI( rs.getString("file") ) );
		    if ( rs.getString("formuri") != null ) 
			result.getOntologyObject2().setFormURI( new URI(rs.getString("formuri"))  );
		    if ( rs.getString("formname") != null ) 
			result.getOntologyObject2().setFormalism( rs.getString("formname")  );
		    result.setExtension( SVCNS, OURI2, rs.getString("uri") );
		}
	    }

	    // Get dependencies if necessary

	    // Get extension metadata
	    rs = st.executeQuery( "SELECT * FROM extension WHERE id = '" + id + "'" );
	    while(rs.next()) {
		tag = rs.getString("tag");
		value = rs.getString("val");
		result.setExtension( rs.getString("uri"), tag, value);
	    }
	} catch (Exception e) { // URI exception that should not occur
	    logger.debug( "IGNORED unlikely URI exception", e);
	    return null;
	} finally {
	    try { st.close(); } catch (Exception ex) {};
	}
	// has been extracted from the database
	//result.setExtension( SVCNS, STORED, "DATE");
	// not yet cached (this instruction should be useless)
	result.setExtension( SVCNS, CACHED, (String)null );
	return result;
    }

    /**
     * loads the full alignment from the database and put it in the
     * alignmentTable hastable
     * 
     * should be invoked when:
     * 	( result.getExtension(CACHED) == ""
     * && result.getExtension(STORED) != "") {

     */
    protected Alignment retrieveAlignment( String uri, Alignment alignment ) throws SQLException, AlignmentException, URISyntaxException {
	String id = stripAlignmentUri( uri );
	URI ent1 = null, ent2 = null;

	alignment.setOntology1( new URI( alignment.getExtension( SVCNS, OURI1 ) ) );
	alignment.setOntology2( new URI( alignment.getExtension( SVCNS, OURI2 ) ) );

	// Get cells
	Statement st = createStatement();
	Statement st2 = createStatement();
	ResultSet rs = st.executeQuery( "SELECT * FROM cell WHERE id = '" + id + "'" );
	while( rs.next() ) {
	    ent1 = new URI( rs.getString("uri1") );
	    ent2 = new URI( rs.getString("uri2") );
	    if ( ent1 == null || ent2 == null ) break;
	    Cell cell = alignment.addAlignCell(ent1, ent2, rs.getString("relation"), Double.parseDouble(rs.getString("measure")));
	    String cid = rs.getString( "cell_id" );
	    if ( cid != null && !cid.equals("") ) {
		if ( !cid.startsWith("##") ) {
		    cell.setId( cid );
		}
		ResultSet rse2 = st2.executeQuery("SELECT * FROM extension WHERE id = '" + cid + "'");
		while ( rse2.next() ){
		    cell.setExtension( rse2.getString("uri"), 
				       rse2.getString("tag"), 
				       rse2.getString("val") );
		}
	    }
	    cell.setSemantics( rs.getString( "semantics" ) );
	}

	// reset
	resetCacheStamp(alignment);
	st.close();
	return alignment;
    }
    
    //**********************************************************************
    // FETCHING FROM CACHE
	
    /**
     * retrieve full alignment from id (and cache it)
     */
    protected Alignment fetchAlignment( String uri, Alignment result ) throws AlignmentException {
	try { 
	    return retrieveAlignment( uri, result );
	} catch ( SQLException sqlex ) {
	    throw new AlignmentException( "getAlignment: SQL exception", sqlex );
	} catch ( URISyntaxException urisex ) {
	    logger.trace( "Cache: cannot read from DB", urisex );
	    throw new AlignmentException( "getAlignment: Cannot find alignment", urisex );
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
	if ( s == null ) return "NULL";
	String result = "'";
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
	return result + new String( chars, j, i-j ) + "'";
    }

    /**
     * Non publicised class
     */
    public void unstoreAlignment( String uri ) throws AlignmentException {
	Alignment alignment = getAlignment( uri );
	if ( alignment != null ) {
	    unstoreAlignment( uri, alignment );
	}
    }

    // Suppress it from the cache...
    public void unstoreAlignment( String uri, Alignment alignment ) throws AlignmentException {
	try {
	    Statement st = createStatement();
	    String id = stripAlignmentUri( uri );
	    try {
		conn.setAutoCommit( false );
		// Delete cell's extensions
		ResultSet rs = st.executeQuery( "SELECT cell_id FROM cell WHERE id='"+id+"'" );
		while ( rs.next() ){
		    String cid = rs.getString("cell_id");
		    if ( cid != null && !cid.equals("") ) {
			st.executeUpdate( "DELETE FROM extension WHERE id='"+cid+"'" );
		    }
		}
		st.executeUpdate("DELETE FROM cell WHERE id='"+id+"'");
		st.executeUpdate("DELETE FROM extension WHERE id='"+id+"'");
		st.executeUpdate("DELETE FROM ontology WHERE id='"+id+"'");
		st.executeUpdate("DELETE FROM dependency WHERE id='"+id+"'");
		st.executeUpdate("DELETE FROM alignment WHERE id='"+id+"'");
		alignment.setExtension( SVCNS, STORED, (String)null);
	    } catch ( SQLException sex ) {
		conn.rollback();
		logger.warn( "SQLError", sex );
		throw new AlignmentException( "SQL Exception", sex );
	    } finally {
		conn.setAutoCommit( false );
		st.close();
	    }
	} catch ( SQLException sex ) {
	    throw new AlignmentException( "Cannot establish SQL Connexion", sex );
	}
    }

    public void storeAlignment( String uri ) throws AlignmentException {
	String query = null;
	BasicAlignment alignment = (BasicAlignment)getAlignment( uri );
	String id = stripAlignmentUri( uri );
	Statement st = null;
	// We store stored date
	alignment.setExtension( SVCNS, STORED, new Date().toString());
	// We empty cached date
	alignment.setExtension( SVCNS, CACHED, (String)null );

	try {
	    // Try to store at most 3 times.
	    // Otherwise, an exception EOFException will be thrown (relation with Jetty???)
	    // [JE2013: Can we check this?]
	    for( int i=0; i < 3 ; i++ ) {
		try {
		    st = createStatement();
		    logger.debug( "Storing alignment {}�as {}", uri, id );
		    conn.setAutoCommit( false );
		    query = "INSERT INTO alignment " + 
			"(id, type, level) " +
			"VALUES (" +quote(id)+","+quote(alignment.getType())+","+quote(alignment.getLevel()) +")";
		    st.executeUpdate(query);
		    
		    recordOntology( st, id, true,
				    alignment.getOntology1URI(),
				    alignment.getFile1(), 
				    alignment.getOntologyObject1() );
		    recordOntology( st, id, false,
				    alignment.getOntology2URI(),
				    alignment.getFile2(), 
				    alignment.getOntologyObject2() );
		    
		    // store dependencies
		    for ( String[] ext : alignment.getExtensions() ) {
			String turi = ext[0];
			String tag = ext[1];
			String val = ext[2];
			query = "INSERT INTO extension " + 
			    "(id, uri, tag, val) " +
			    "VALUES (" + quote(id) + "," +  quote(turi) + "," +  quote(tag) + "," + quote(val) + ")";
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
				cellid = generateCellId();
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
				"VALUES (" + quote(id) + "," + quote(cellid) + "," + quote(uri1) + "," + quote(uri2) + "," + quote(strength) + "," + quote(sem) + "," + quote(rel) + ")";
			    st.executeUpdate(query);
			}
			if ( cellid != null && !cellid.equals("") && c.getExtensions() != null ) {
			    // Store extensions
			    for ( String[] ext : c.getExtensions() ) {
				String turi = ext[0];
				String tag = ext[1];
				String val = ext[2];
				query = "INSERT INTO extension " + 
				    "(id, uri, tag, val) " +
				    "VALUES (" + quote(cellid) + "," +  quote(turi) + "," +  quote(tag) + "," + quote(val) + ")";
				st.executeUpdate(query);
			    }
			}
		    }
		    st.close();
		} catch ( SQLException sex ) {
		    logger.warn( "SQLError", sex );
		    conn.rollback();
		    throw new AlignmentException( "SQLException", sex );
		} finally {
		    conn.setAutoCommit( true );
		}
		break;
	    }
	} catch (SQLException sqlex) {
	    throw new AlignmentException( "SQLRollBack issue", sqlex );
	}
	// We reset cached date
	resetCacheStamp(alignment);
    }

    // Do not add transaction here: this is handled by caller
    public void	recordOntology( Statement st, String id, boolean source, URI uri, URI file, Ontology onto ) throws SQLException {
	String sfile = "";
	String suri = "";
	if ( file != null ) sfile = file.toString();
	if ( uri != null ) suri = uri.toString();
	String query = null;
	logger.debug( "Recording ontology {} with file {}", suri, sfile );

	if ( onto != null ) {
	    query = "INSERT INTO ontology " + 
		"(id, uri, file, source, formname, formuri) " +
		"VALUES ("+quote(id)+","+ quote(suri)+","+quote(sfile)+"," +(source?'1':'0')+","+quote(onto.getFormalism())+","+quote(onto.getFormURI().toString())+")";
	} else {
	    query = "INSERT INTO ontology " + 
		"(id, uri, file, source) " +
		"VALUES ("+quote(id)+","+ quote(suri)+","+quote(sfile)+"," +(source?'1':'0')+")";
	    }
	st.executeUpdate(query);
    }

    public void storeOntologyNetwork( String uri ) throws AlignmentException {
	// TODO
    }

    public void unstoreOntologyNetwork( String uri, OntologyNetwork network ) throws AlignmentException {
	// TODO
    }

    // **********************************************************************
    // DATABASE CREATION AND UPDATING
    /*
      # server info

      create table server (
      host varchar(50),
      port varchar(5),
      prefix varchar(50),
      edit varchar(5)
      );
   

      # alignment info
      
      create table alignment (
      id varchar(100), 
      type varchar(5),
      level varchar(25),
      primary key (id));

      # ontology info

      create table ontology (
      id varchar(255), 
      uri varchar(255),
      file varchar(255),
      source boolean,
      formname varchar(50),
      formuri varchar(255)
      );

      # dependencies info

      create table dependency (
      id varchar(255), 
      dependsOn varchar(255)
      );

      # cell info

      create table cell(
      id varchar(100),
      cell_id varchar(255),
      uri1 varchar(255),
      uri2 varchar(255),
      semantics varchar(30),
      measure varchar(20),
      relation varchar(255));

      # extension info
      
      create table extension(
      id varchar(100),
      uri varchar(200),
      tag varchar(50),
      val varchar(500));
      
      # ontologies info 
      
      create table  (
      id varchar(255),
      idonet varchar(255),
      uri varchar(255),
      name varchar(50),
      file varchar(255));

      # ontologynetwork info 
      
      create table ontologynetwork(
      id varchar(255),
      uri varchar(255),
      name varchar(50),
      type varchar(50),
      file varchar(255));

    */

    public void initDatabase() throws SQLException {
	logger.info( "Initialising database" );
	Statement st = createStatement();
	try {
	    conn.setAutoCommit( false );
	    // Create tables
	    st.executeUpdate("CREATE TABLE alignment (id VARCHAR(100), type VARCHAR(5), level VARCHAR(25), primary key (id))");
	    st.executeUpdate("CREATE TABLE ontology (id VARCHAR(255), source BOOLEAN, uri VARCHAR(255), formname VARCHAR(50), formuri VARCHAR(255), file VARCHAR(255), primary key (id, source))");
	    st.executeUpdate("CREATE TABLE dependency (id VARCHAR(255), dependsOn VARCHAR(255))");
	    st.executeUpdate("CREATE TABLE cell(id VARCHAR(100), cell_id VARCHAR(255), uri1 VARCHAR(255), uri2 VARCHAR(255), semantics VARCHAR(30), measure VARCHAR(20), relation VARCHAR(255))");
	    st.executeUpdate("CREATE TABLE extension(id VARCHAR(100), uri VARCHAR(200), tag VARCHAR(50), val VARCHAR(500))");
	 // st.executeUpdate("CREATE TABLE ontologies (id VARCHAR(255), idonet VARCHAR(255), uri VARCHAR(255), name VARCHAR(50), file VARCHAR(255), primary key (id,idonet))");
     // st.executeUpdate("CREATE TABLE ontologynetwork (id VARCHAR(255), uri VARCHAR(255), name VARCHAR(50), type VARCHAR(50), file VARCHAR(255), primary key (id,uri))");
	    st.executeUpdate("CREATE TABLE server (host VARCHAR(50), port VARCHAR(5), prefix VARCHAR (50), edit BOOLEAN, version VARCHAR(5))");	    

	    /*
	    // EDOAL
	    st.executeUpdate("CREATE TABLE instexpr (intid VARCHAR(50), id VARCHAR(250), var VARCHAR(250))");
	    st.executeUpdate("CREATE TABLE instlist (joinid VARCHAR(50), intid VARCHAR(250))"); //intid in instexpr

	    st.executeUpdate("CREATE TABLE classexpr (intid VARCHAR(50), type VARCHAR(10), joinid VARCHAR(250), var VARCHAR(250))");
	    // type=uri: joinid = uri
	    // type=id:  joinid = uri
	    // type\in classconst: joinid = joinid in classlist
	    // type\in classrest: joinid = id in classrest
	    st.executeUpdate("CREATE TABLE classlist (joinid VARCHAR(50), intid VARCHAR(250))"); //intid in classexpr
	    st.executeUpdate("CREATE TABLE classrest (joinid VARCHAR(50), arg1 VARCHAR(250), arg2 VARCHAR(250))");
	    */

	    st.close();

	    // Register *DATABASE* Because of the values (that some do not like), this is a special statement
	    registerServer( "dbms", "port", false, idprefix );
	} catch ( SQLException sex ) {
	    logger.warn( "SQLError", sex );
	    conn.rollback();
	    throw sex;
	} finally {
	    conn.setAutoCommit( true );
	}
    }

    public void resetDatabase( boolean force ) throws SQLException, AlignmentException {
	Statement st = createStatement();
	try {
	    conn.setAutoCommit( false );
	    // Check that no one else is connected...
	    if ( force != true ){
		ResultSet rs = st.executeQuery("SELECT COUNT(*) AS rowcount FROM server WHERE edit=1");
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
	    st.executeUpdate("DROP TABLE IF EXISTS ontology");
	    st.executeUpdate("DROP TABLE IF EXISTS dependency");
	    st.executeUpdate("DROP TABLE IF EXISTS cell");
	    st.executeUpdate("DROP TABLE IF EXISTS extension");
	    //st.executeUpdate("DROP TABLE IF EXISTS ontologies");
	    //st.executeUpdate("DROP TABLE IF EXISTS ontologynetwork");
	    // Redo it
	    initDatabase();
	  
	    // Register *THIS* server, etc. characteristics (incl. version name)
	    registerServer( host, port, rights==1, idprefix );
	} catch ( SQLException sex ) {
	    logger.warn( "SQLError", sex );
	    conn.rollback();
	    throw sex;
	} finally {
	    st.close();
	    conn.setAutoCommit( true );
	}
    }
    
    private void registerServer( String host, String port, Boolean writeable, String prefix ) throws SQLException {
	// Register *THIS* server, etc. characteristics (incl. version name)
	PreparedStatement pst = conn.prepareStatement("INSERT INTO server (host, port, edit, version, prefix) VALUES (?,?,?,?,?)");
	pst.setString(1,host);
	pst.setString(2,port);
	pst.setBoolean(3,writeable);
	pst.setString(4,VERSION+"");
	pst.setString(5,idprefix);
	pst.executeUpdate();
	pst.close();
    }

    /*
     * A dummy method, since it exists just ALTER TABLE ... DROP and ALTER TABLE ... ADD in SQL Language.
     * each dbms has its own language for manipulating table columns....
     */
    public void renameColumn(Statement st, String tableName, String oldName, String newName, String newType) throws SQLException { 
	try {
	    conn.setAutoCommit( false );
	    st.executeUpdate("ALTER TABLE "+tableName+" ADD "+newName+" "+newType);
	    st.executeUpdate("UPDATE "+tableName+" SET "+newName+"="+oldName);
	    st.executeUpdate("ALTER TABLE "+tableName+" DROP "+oldName);  
	} catch ( SQLException sex ) {
	    logger.warn( "SQLError", sex );
	    conn.rollback();
	    throw sex;
	} finally {
	    conn.setAutoCommit( true );
	}
    }
    
    /*
    * Another dummy method, since it exists just ALTER TABLE ... DROP and ALTER TABLE ... ADD in SQL Language.
    * each dbms has its own language for manipulating table columns....     
    */
    public void changeColumnType(Statement st, String tableName, String columnName, String newType) throws SQLException { 
	try {
	    conn.setAutoCommit( false );
	    String tempName = columnName+"temp";
	    renameColumn(st,tableName,columnName,tempName,newType);
	    renameColumn(st,tableName,tempName,columnName,newType);
	} catch ( SQLException sex ) {
	    logger.warn( "SQLError", sex );
	    conn.rollback();
	    throw sex;
	} finally {
	    conn.setAutoCommit( true );
	}
    }

    public void updateDatabase() throws SQLException, AlignmentException {
	Statement st = createStatement();
	// get the version number (port is the entry which is always here)
	ResultSet rs = st.executeQuery("SELECT version FROM server WHERE port='port'");
	rs.next();
	int version = rs.getInt("version") ;
	if ( version < VERSION ) {
	    if ( version >= 302 ) {
		if ( version < 310 ) {
		    logger.info( "Upgrading to version 3.1" );
		    // ALTER database
		    renameColumn(st,"extension","method","val","VARCHAR(500)");
		    // case mysql
		    //st.executeUpdate("ALTER TABLE extension CHANGE method val VARCHAR(500)");
		   
		    st.executeUpdate("ALTER TABLE extension ADD uri VARCHAR(200);");
		    // Modify extensions
		    ResultSet rse = st.executeQuery("SELECT * FROM extension");
		    Statement st2 = createStatement();
		    while ( rse.next() ){
			String tag = rse.getString("tag");
			//logger.trace(" Treating tag {} of {}", tag, rse.getString("id"));
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
			    //logger.trace("  >> {} : {}", ns, name);
			    st2.executeUpdate("UPDATE extension SET tag='"+name+"', uri='"+ns+"' WHERE id='"+rse.getString("id")+"' AND tag='"+tag+"'");
			}
		    }
		}
		// Nothing to do with 340: subsumed by 400
		if ( version < 400 ) {
		    logger.info("Upgrading to version 4.0");
		    // ALTER database 
		    changeColumnType(st,"cell","relation", "VARCHAR(255)");
		    changeColumnType(st,"cell","uri1", "VARCHAR(255)");
		    changeColumnType(st,"cell","uri2", "VARCHAR(255)");
		    
		    changeColumnType(st,"alignment","level", "VARCHAR(255)");
		    changeColumnType(st,"alignment","uri1", "VARCHAR(255)");
		    changeColumnType(st,"alignment","uri2", "VARCHAR(255)");
		    changeColumnType(st,"alignment","file1", "VARCHAR(255)");
		    changeColumnType(st,"alignment","file2", "VARCHAR(255)");
		    
		    renameColumn(st,"alignment","owlontology1","ontology1", "VARCHAR(255)");
		    renameColumn(st,"alignment","owlontology2","ontology2", "VARCHAR(255)");
		}
		if ( version < 450 ) {
		    logger.info("Upgrading to version 4.5");
		    logger.info("Creating Ontology table");
		    st.executeUpdate("CREATE TABLE ontology (id VARCHAR(255), uri VARCHAR(255), source BOOLEAN, file VARCHAR(255), formname VARCHAR(50), formuri VARCHAR(255), primary key (id, source))");
		    ResultSet rse = st.executeQuery("SELECT * FROM alignment");
		    while ( rse.next() ){
			Statement st2 = createStatement();
			// No Ontology _type_ available then
		    	st2.executeUpdate("INSERT INTO ontology (id, uri, source, file) VALUES ('"+rse.getString("id")+"','"+rse.getString("uri1")+"','1','"+rse.getString("file1")+"')");
		    	st2.executeUpdate("INSERT INTO ontology (id, uri, source, file) VALUES ('"+rse.getString("id")+"','"+rse.getString("uri2")+"','0','"+rse.getString("file2")+"')");
		    }
		    logger.info("Cleaning up Alignment table");
		    st.executeUpdate("ALTER TABLE alignment DROP ontology1");  
		    st.executeUpdate("ALTER TABLE alignment DROP ontology2");  
		    st.executeUpdate("ALTER TABLE alignment DROP uri1");  
		    st.executeUpdate("ALTER TABLE alignment DROP uri2");  
		    st.executeUpdate("ALTER TABLE alignment DROP file1");  
		    st.executeUpdate("ALTER TABLE alignment DROP file2");  
		    logger.debug("Altering server table");
		    st.executeUpdate("ALTER TABLE server ADD prefix VARCHAR(50);");
		    st.executeUpdate("UPDATE server SET prefix='"+idprefix+"'");
		    logger.debug("Updating server with prefix");
		    Statement stmt = null;
		    try { // In all alignment
			conn.setAutoCommit( false );
			stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
						    ResultSet.CONCUR_UPDATABLE);
			ResultSet uprs = stmt.executeQuery( "SELECT id FROM alignment" );
			while ( uprs.next() ) {
			    String oldid = uprs.getString("id");
			    String newid = stripAlignmentUri( oldid );
			    //logger.trace("Updating {} to {}", oldid, newid );
			    uprs.updateString( "id", newid );
			    uprs.updateRow();
			    // In all cell (for id and cell_id)
			    st.executeUpdate("UPDATE cell SET id='"+newid+"' WHERE id='"+oldid+"'" );
			    // In all extension
			    st.executeUpdate("UPDATE extension SET id='"+newid+"' WHERE id='"+oldid+"'" );
			    // In all ontology
			    st.executeUpdate("UPDATE ontology SET id='"+newid+"' WHERE id='"+oldid+"'" );
			}
			// Now, for each cell, with an id,
			// either recast the id ... or not
			conn.commit();
		    } catch ( SQLException e ) {
			logger.warn( "IGNORED Failed to update", e );
		    } finally {
			if ( stmt != null ) { stmt.close(); }
			conn.setAutoCommit( true );
		    }
		    logger.info("Creating dependency table");
		    st.executeUpdate("CREATE TABLE dependency (id VARCHAR(255), dependsOn VARCHAR(255))");
		    logger.info("Fixing legacy errors in cached/stored");
		    st.executeUpdate( "UPDATE extension SET val=( SELECT e2.val FROM extension e2 WHERE e2.tag='cached' AND e2.id=extension.id ) WHERE tag='stored' AND val=''" );
		    // We should also implement a clean up (suppress all starting with http://)
		}
		if ( version < 461 ) {
		    logger.info("Upgrading to version 4.7");
		    // Nothing to be done so far
		    // 462:
		    // Ontology tables (modified ex tables)
		    // 463:
		    // Ontology networks (+3 tables)
		    // 464:
		    // EDOAL
		}
		// ALTER version
		st.executeUpdate("UPDATE server SET version='"+VERSION+"'");
	    } else {
		throw new AlignmentException( "Database must be upgraded ("+version+" -> "+VERSION+")" );
	    }
	}
	st.close();
    }
    
    //**********************************************************************
    // ONTOLOGY NETWORKS
    /*
     * 
     * 
     */
  
    /*
      // JE: This adds an Ontology in THE NETWORK
    // put the meta data of the ontology network
    // - structure json/html
    // - type=
    public void putOntologyNetwork( URI onuri, String structure, String type ) throws AlignmentException {
	onetwork = new BasicOntologyNetwork();
	onetwork.onName = structure; 
	onetwork.onType = type;
	onetwork.onFile = null;
	onetworkTable.put(onuri,onetwork);
	//TODO handle error in put
    }
    */
    
    /*
    // fetch all the ontologies of the network
    // TODO where should be placed?
    // To put in LOVAService.java
import fr.inrialpes.exmo.ontowrap.onetparser.JSONFetcher;  //TODO it shouldn't be here?

    public void fetchLOVOntologyNetwork( URI onuri, String structure, String type ) throws AlignmentException {
	oonetworkTable = JSONFetcher.TypeFetcher(onuri, structure, type);
    }
    */
    
    /*
    // JE: This code was for storing ontologies...
    // And still relying on a global variable
    // Apparently, this should work with ONE NETWORK...
    public void	recordOntologyNetwork( String id ) throws SQLException {
    	Statement st = null;
    	id = stripNetworkUri( id );
    	st = createStatement();
    	Set<URI> keys = onetworkTable.keySet();
        for(URI key: keys)
	    if ( key != null ) {
    		String uri = key.toString();
    		String query = null;
    		logger.debug( "Recording ontology {} with file {}", onetworkTable.keys() );
		query = "INSERT INTO ontologynetwork " + 
		    "(id, uri, name, type, file) " +
		    "VALUES ("+
		    quote(id)+","+
		    quote(uri)+","+
		    quote(onetwork.onName)+","+
		    quote(onetwork.onType)+","+
		    quote(onetwork.onFile)+")";
		System.out.println("El query:  "+ query);
		st.executeUpdate(query);
	    }
    	//recordOONetwork(id);
    	//TODO add START TRANSACTION; (Insert into ontologynetwork and ontologies) .. COMMIT;
    }
    */
    
    /*
    public void	recordOONetwork(String idonet) throws SQLException {
        int totOnto = 0;
    	Statement st = null;
    	st = createStatement();
    	Set<Entry<URI, OONetwork>> set = oonetworkTable.entrySet();
        Iterator<Entry<URI, OONetwork>> it = set.iterator();
        while (it.hasNext()) {
	    Map.Entry<URI, OONetwork> entry = (Map.Entry<URI, OONetwork>) it.next();
	    URI uri = entry.getKey();
	    String urionto = uri.toString();
	    OONetwork oonetwork = entry.getValue();
	    String id = generateId()+totOnto;
	    
	    if ( urionto != null ) {
		String query = null;
		logger.debug( "Recording ontology {} with file {}", urionto );
		query = "INSERT INTO ontologies " + 
		    "(id, idonet, uri, name, file) " +
		    "VALUES ("+
		    quote(id)+","+
		    quote(idonet)+","+
		    quote(urionto)+","+
		    quote(oonetwork.oonName)+","+
		    quote(oonetwork.oonFile)+")";
		System.out.println("El query:  "+ query);
		totOnto++;
	    	st.executeUpdate(query);
	    }
        }
    	st.close();
    }*/
    
}
