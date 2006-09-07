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

import java.util.Vector;
import java.util.Enumeration;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.io.owl_rdf.OWLRDFParser;
import org.semanticweb.owl.util.OWLManager;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLException;

import fr.inrialpes.exmo.align.impl.BasicRelation;

public class DBServiceImpl implements DBService{
	int id = 0;
	Connection conn = null;
	String url = "jdbc:mysql://localhost:3306/DBService";
	Statement st = null;
	ResultSet rs = null;
	
	OWLOntology O1 = null;
	OWLOntology O2 = null;
	String type = null;
	String level = null;

	String s_O1 = "";
	String s_O2 = ""; 
	String s_File1 = "";
	String s_File2 = "";
	String s_uri1 = "";
	String s_uri2 = "";
	String s_method = "";
	String s_cell = "";
	
	final int CONNECTION_ERROR = 1;
	final int SUCCESS = 2;
	final int INIT_ERROR = 3;

	static Cache cache = null;
	
	public DBServiceImpl() {
		try{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		} catch(Exception e){
			System.out.println(e.toString());
		}
	}

	public int connect(String password){
		try {
			conn = DriverManager.getConnection(url, "root", password);
			st = (Statement) conn.createStatement();
		} catch(Exception ex) {return CONNECTION_ERROR;}
		
		if(init()) return SUCCESS;
		else return INIT_ERROR;
	}
	
	public int connect(String IPAddress, String id, String password) {
		try {
			conn = DriverManager.getConnection("jdbc:mysql://"+IPAddress+":3306/DBService", id, password);
			st = (Statement) conn.createStatement();
		} catch(Exception ex) {return CONNECTION_ERROR;}
		
		if(init()) {
			System.out.println("init success");
			return SUCCESS;
		}
		else return INIT_ERROR;
	}
	
	private boolean init() {
		try {
			cache = new CacheImpl(conn);
			if( cache.loading() == SUCCESS ) {
				System.out.println("chche loading success");
				return true;
			}
			else return false;
		} catch (Exception e) {return false;}
	}
	 	
	public long store(Alignment alignment){
		long id = 0;
		String query = null;
		id = nextID();

		try {
			O1 = (OWLOntology) alignment.getOntology1();
			O2 = (OWLOntology) alignment.getOntology2();
			s_O1 = O1.getLogicalURI().toString();
			s_O2 = O2.getLogicalURI().toString();

			if (alignment.getFile1() != null) s_File1 = alignment.getFile1().toString();
			if (alignment.getFile2() != null) s_File2 = alignment.getFile2().toString();
	
			s_uri1 = O1.getPhysicalURI().toString();
			s_uri2 = O2.getPhysicalURI().toString();

			type = alignment.getType();
			level = alignment.getLevel();
			
			query = "insert into alignment " + 
			        "(id, owlontology1, owlontology2, type, level, file1, file2, uri1, uri2) " +
			        "values (" + id + ",'" +  s_O1 + "','" + s_O2 + "','" + type + "','" + level + "','" + s_File1 + "','" + s_File2 + "','" + s_uri1 + "','" + s_uri2 + "')";
			st.executeUpdate(query);
			for( Enumeration e = alignment.getExtensions().getNames() ; e.hasMoreElements() ; ){
			    String tag = (String)e.nextElement();
			    s_method = alignment.getExtension(tag);
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
				    
				} catch ( OWLException ex) { System.out.println( "getURI problem" + ex.toString() ); }
			}
		}
		catch (Exception e) {System.out.println(e.toString());}
		return id;
	}
	
	public Alignment find(long id) {
		Alignment result = null;
		try {
			result = (Alignment) cache.get(id);
		} catch (Exception e) {System.out.println("Alignment Finding Exception");}		
		return result;
	}
	
	public Vector find(URI uri) { // find alignment list with an ontology uri
		Vector result = null;
		try {
			result = (Vector) cache.get(uri);
		} catch (Exception e) {System.out.println("Alignment Finding Exception");}		
		return result;	    	
	}                                
	
	public Vector find(URI uri1, URI uri2) { // find alignment list with two ontology uri
		Vector result = null;
		try {
			result = (Vector) cache.get(uri1, uri2);
		} catch (Exception e) {System.out.println("Alignment Finding Exception");}		
		return result;	    	
	}                    
	
	public synchronized long nextID(){
		long id = 0;
		try {
			st.executeUpdate("insert into id_seq (aa) values ('a')");
			rs = (ResultSet) st.executeQuery("select max(id) from id_seq");
			//System.out.println(rs.toString());
			while(rs.next()) {
				id = rs.getInt(1);
			}
			//id = rs.getBigDecimal(1).longValue();
		} catch(Exception ex){
			System.out.println(ex.toString());
			return -1;
		}
		return id;
	}
	
	public int open(String id, String password){
		try{
			String userid = null;;
			String pwd;
			userid = id;
			pwd = password;
			
			conn = DriverManager.getConnection(url, userid, pwd);
		} catch(Exception ex){System.out.println(ex.toString());}
		return 1;
	}
	
	public int close(){

		try {
			conn.close();
			st.close();
			rs.close();
			return 1;
		} catch (Exception ex) {
			return -1;
		}
	}
    
	public static OWLOntology loadOntology(URI uri) throws Exception {
		OWLRDFParser parser = new OWLRDFParser();
		parser.setConnection(OWLManager.getOWLConnection());
		return parser.parseOntology(uri);
	}
}
