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

import java.net.URI;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Hashtable;
import java.util.Vector;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.io.owl_rdf.OWLRDFParser;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.util.OWLManager;

import fr.inrialpes.exmo.align.impl.BasicAlignment;

public class CacheImpl implements Cache {
	Hashtable alignmentTable = null;
	
	Statement st = null;
	ResultSet rs = null;
	Connection conn = null;
	
	final int CONNECTION_ERROR = 1;
	final int SUCCESS = 2;
	final int INIT_ERROR = 3;
	
	public CacheImpl(Connection conn) {
		try {
			this.conn = conn;
			st = (Statement) conn.createStatement();
		} catch(Exception e) {System.out.println(e.toString());}
	}
	
	public int loading() {
		alignmentTable = new Hashtable();
		if(loadAlignment(true)) return SUCCESS;
		else return INIT_ERROR;
	}
		
	private boolean loadAlignment(boolean force){
		String query = null;
		String id = null;
		Alignment alignment = null;
		Vector idInfo = new Vector();
		
		if (force) {
			try {
				query = "select id " + "from alignment";

		        rs = (ResultSet) st.executeQuery(query);
		        System.out.println("1234");
				while(rs.next()) {
					id = rs.getString("id");
					idInfo.add(id);	
					System.out.println(id);
				}

				for( int i = 0; i < idInfo.size(); i ++ ) {
					id = (String) idInfo.get(i);
					alignment = retrieve(Long.parseLong(id));
					alignmentTable.put(id, alignment);
				}							
			} catch	(Exception e) {System.out.println("Hashtable loading error1");}
			return true;
		}
		else return false;
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
		
	private boolean loadOne(long id, Alignment alignment, boolean force){
		if (force) {
			try {
				alignmentTable.put(Long.toString(id), alignment);
			} catch	(Exception e) {System.out.println("Hashtable loading error2");}
			return true;
		}
		else return false;
	}		

	public Alignment get(long id) {
		Alignment result = null;		
		String query = null;
		
		try {
			result = (Alignment) alignmentTable.get(Long.toString(id));
			
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
		} catch (Exception e) {System.out.println("Alignment Finding Exception1");}		
		return result;
	}
	
	public Vector get(URI uri) {
		Vector result = new Vector();

		Alignment temp = null;		
		String query = null;
		
		try {
			query = "select id " + "from alignment " + "where uri1 = \'" + uri + "\' OR uri2 = \'" + uri + "\'";
		    System.out.println(query);   
		    rs = (ResultSet) st.executeQuery(query);

		    if(rs == null) return null;
		    else {
		        while (rs.next()) {
		        	System.out.println("1234");
		        	temp = get(Long.parseLong(rs.getString("id")));
		        	result.add(temp);
		        }
		    }						
		} catch (Exception e) {System.out.println("Alignment Finding Exception2");}		
		return result;
	}
	
	public Vector get(URI uri1, URI uri2) {
		Vector result = new Vector();

		Alignment temp = null;		
		String query = null;
		
		try {
			query = "select id " + "from alignment " + "where uri1 = \'" + uri1 + "\' AND uri2 = \'" + uri2 + "\'";
		        
		    rs = (ResultSet) st.executeQuery(query);

		    if(rs == null) return null;
		    else {
		        while (rs.next()) {
		        	temp = get(Long.parseLong(rs.getString("id")));
		        	result.add(temp);
		        }
		    }						
		} catch (Exception e) {System.out.println("Alignment Finding Exception3");}		
		return result;
	}
	
	public static OWLOntology loadOntology(URI uri) throws Exception {
		OWLRDFParser parser = new OWLRDFParser();
		parser.setConnection(OWLManager.getOWLConnection());
		return parser.parseOntology(uri);
	}
}
