package fr.inrialpes.exmo.align.service;

import java.net.URI;
import java.sql.ResultSet;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.semanticweb.owl.align.Alignment;

public class CacheImpl implements Cache {
	Hashtable alignmentTable = null;
	
	
	public CacheImpl() {
		
	}
	
	private Hashtable loadAll(boolean force){
		Hashtable result = null;
		return result;
	}
	
	public int put(long id, URI uri1, URI uri2, Alignment alignmnet) {
		
		return 0;
	}
	public Alignment get(long id) {
		Alignment result = null;
		return result;
	}
	public Enumeration get(URI uri) {
		Enumeration result = null;
		return result;
	}
	public Enumeration get(URI uri1, URI uri2) {
		Enumeration result = null;
		return result;
	}
}
