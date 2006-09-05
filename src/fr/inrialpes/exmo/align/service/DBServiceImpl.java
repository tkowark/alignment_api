package fr.inrialpes.exmo.align.service;

import java.util.*;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import com.mysql.jdbc.ResultSet;
import com.mysql.jdbc.Statement;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.io.owl_rdf.OWLRDFParser;
import org.semanticweb.owl.util.OWLManager;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLException;

import fr.inrialpes.exmo.align.impl.BasicRelation;
import fr.inrialpes.exmo.align.impl.BasicAlignment;

import java.util.Hashtable;

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

	static Hashtable cache = null;
	
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
			
			cache = loadAll(true);
		} catch(Exception ex) {return -1;}
		return 1;
	}
	
	private Hashtable loadAll(boolean force){
		Hashtable result = new Hashtable();
		String query = null;
		String id = null;
		Alignment alignment = null;
		Vector v = new Vector();
		
		if (force) {
			try {
				query = "select id " + "from alignment";
		        
		        rs = (ResultSet) st.executeQuery(query);
		        
				while(rs.next()) {
					id = rs.getString("id");
					v.add(id);					
				}

				for( int i = 0; i < v.size(); i ++ ) {
					id = (String) v.get(i);
					alignment = retrieve(Long.parseLong(id));
					result.put(id,alignment);	
				}
							
			} catch	(Exception e) {System.out.println("Hashtable loading error");}
			return result;
		}
		else return null;
	}
	
	private boolean loadOne(long id, Alignment alignment, boolean force){
		if (force) {
			try {
				cache.put(Long.toString(id), alignment);
			} catch	(Exception e) {System.out.println("Hashtable loading error");}
			return true;
		}
		else return false;
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
		String query = null;
		try {
			result = (Alignment) cache.get(Long.toString(id));
			
			if (result == null) {
				query = "select id " + "from alignment " + "where id = " + id;
		        
		        rs = (ResultSet) st.executeQuery(query);

		        if(rs == null) return null;
		        else {
		        	while (rs.next()) {
		        		result = retrieve(id);
		        		loadOne(id, result, true);	
		        	}
		        }
			}
			
		} catch (Exception e) {System.out.println("Alignment Finding Exception");}		
		return result;
	}
	
	protected Alignment retrieve(long id){
		OWLOntology o1 = null;
		OWLOntology o2 = null;
		String query;
		String tag;
		String method;
		OWLEntity ent1 = null, ent2 = null;
		Cell cell = null;
				
		Alignment result = new BasicAlignment();
		
		try {
			query = "select * " +
			        "from alignment " +
			        "where id = " + id;
			rs = (ResultSet) st.executeQuery(query);
			
			while(rs.next()) {
				o1 = loadOntology(new URI(rs.getString("uri1")));
				o2 = loadOntology(new URI(rs.getString("uri2")));
			
				result.setLevel(rs.getString("level"));
				result.setType(rs.getString("type"));			
				result.setOntology1(o1);
				result.setOntology2(o2);
			}
			
			query = "select * " +
					"from extension " +
					"where id = " + id;
			
			rs = (ResultSet) st.executeQuery(query);

			while(rs.next()) {
				tag = rs.getString("tag");
				method = rs.getString("method");
				result.setExtension(tag, method);
			}

			query = "select * " +
	        		"from cell " +
	        		"where id = " + id;
			rs = (ResultSet) st.executeQuery(query);

			while(rs.next()) {
				ent1 = (OWLEntity) o1.getClass(new URI(rs.getString("uri1")));
				ent2 = (OWLEntity) o2.getClass(new URI(rs.getString("uri2")));
		
				if(ent1 == null || ent2 == null) break;
				cell = result.addAlignCell(ent1, ent2, rs.getString("relation"), Double.parseDouble(rs.getString("measure")));
				cell.setId(rs.getString("cell_id"));
				cell.setSemantics(rs.getString("semantics"));
			}
		} catch (Exception e) {
			System.out.println("No problem");
			System.out.println(e.toString());
			return null;
		}
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
