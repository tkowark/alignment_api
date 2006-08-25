package fr.inrialpes.exmo.align.service;

import java.util.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.mysql.jdbc.*;
import com.mysql.jdbc.ResultSet;
import com.mysql.jdbc.Statement;

import org.semanticweb.owl.align.*;
import org.semanticweb.owl.model.*;

import fr.inrialpes.exmo.align.impl.*;

public class StoreRDFFormat implements DBService{
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
	String s_extensions = "";
	String s_cell = "";

	
	public StoreRDFFormat() {
		try{
			Class.forName("com.mysql.jdbc.Driver").newInstance();	
		} catch(Exception e){
			System.out.println(e.toString());
		}
	}

	public int init(){
		try {
			conn = DriverManager.getConnection(url, "root", "1234");
			st = (Statement) conn.createStatement();
		} catch(Exception ex) {return -1;}
		return 1;
	}
	
	public int init(String id, String password){
		try {
			conn = DriverManager.getConnection(url, id, password);
			st = (Statement) conn.createStatement();
		} catch(Exception ex) {return -1;}
		return 1;
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
			    s_extensions = alignment.getExtension(tag);
			    query = "insert into method " + 
		        "(id, tag, extension) " +
		        "values (" + id + ",'" +  tag + "','" + s_extensions + "')";
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
				        "(id, cell_id, uri1, uri2, semantics, measure, relation) " +
				        "values (" + id + ",'" + temp[0] + "','" + temp[1] + "','" + temp[2] + "','" + temp[3] + "','" + temp[4] + "','" + temp[5] + "')";
					    st.executeUpdate(query);
				    }
				    
				} catch ( OWLException ex) { System.out.println( "getURI problem" + ex.toString() ); }
			}
		}
		catch (Exception e) {System.out.println(e.toString());}
		return id;
	}
	
	public String retrieve(long id){
		String result = "";
		String query;
		try {
			query = "select * " +
			        "from alignment " +
			        "where id = " + id;
			rs = (ResultSet) st.executeQuery(query);
			while(rs.next()) {
				result += "<?xml version='1.0' encoding='utf-8";
				result += "' standalone='no'?>\n";
				result += "<rdf:RDF xmlns='http://knowledgeweb.semanticweb.org/heterogeneity/alignment'\n         xml:base='http://knowledgeweb.semanticweb.org/heterogeneity/alignment'\n         xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'\n         xmlns:xsd='http://www.w3.org/2001/XMLSchema#'>\n";
				result += "<Alignment>\n  <xml>yes</xml>\n";
				result += "<level>";
				result += rs.getString(5);
				result += "</level>\n  <type>";
				result += rs.getString(4);
				result += "</type>\n";
				if( rs.getString(6) != "" )
					result += "  <onto1>"+rs.getString(6)+"</onto1>\n";
		    	if( rs.getString(7) != null )
		    		result += "  <onto2>"+rs.getString(7)+"</onto2>\n";
			    result += "  <uri1>";
			    result +=  rs.getString(8);
			    result += "</uri1>\n";
			    result += "  <uri2>";
			    result +=  rs.getString(9);
			    result += "</uri2>\n";
			}
		} catch (Exception e) {
			System.out.println(e.toString());
			return null;
		}
		try {
			query = "select * " +
			        "from method " +
			        "where id = " + id;
			rs = (ResultSet) st.executeQuery(query);
			while(rs.next()) {
				String tag = rs.getString(2);
				String extension = rs.getString(3);
				result +="  <"+tag+">"+extension+"</"+tag+">\n";
			}
		} catch (Exception e) {
			System.out.println(e.toString());
			return null;
		}
		
		try {
			query = "select * " +
			        "from cell " +
			        "where id = " + id;
			rs = (ResultSet) st.executeQuery(query);
			while(rs.next()) {
		    	result += "  <map>\n";
		    	result += "    <Cell";
		    	if ( rs.getString(2) != null ){
		    		s_cell += " rdf:resource=\"#"+rs.getString(2)+"\"";
		    	}
		    	result += ">\n      <entity1 rdf:resource='";
		    	result += rs.getString(3);
		    	result += "/>\n      <entity2 rdf:resource='";
		    	result += rs.getString(4);
		    	result += "'/>\n      <measure rdf:datatype='http://www.w3.org/2001/XMLSchema#float'>";
		    	result += rs.getString(5);
		    	result += "</measure>\n";
		    	if ( !rs.getString(6).equals("first-order") )
		    		result += "      <semantics>"+rs.getString(6)+"</semantics>\n";
		    	result += "      <relation>";
		    	result += rs.getString(7);			    
		    	result += "</relation>\n    </Cell>\n  </map>\n";
			}
		} catch (Exception e) {
			System.out.println(e.toString());
			return null;
		}

		result += "</Alignment>\n";
		result += "</rdf:RDF>\n";
		
		return result;
	}
	
	public int open(){
		try{
			String userid = "root";
			String pwd = "1234";
			
			conn = DriverManager.getConnection(url, userid, pwd);
			
		} catch(Exception ex){System.out.println(ex.toString());}
		return 1;
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
}
