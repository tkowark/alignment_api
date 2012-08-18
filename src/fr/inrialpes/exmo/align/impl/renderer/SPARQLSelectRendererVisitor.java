package fr.inrialpes.exmo.align.impl.renderer;

import fr.inrialpes.exmo.align.impl.edoal.Expression;
import java.io.PrintWriter;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.Relation;
import java.net.URI;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

public class SPARQLSelectRendererVisitor extends GraphPatternRendererVisitor implements AlignmentVisitor{


    Alignment alignment = null;
    Cell cell = null;
    Hashtable<String,String> nslist = null;
	boolean embedded = false;
	
	private List<String> listBGP1;
	private List<String> listBGP2;

    private List<String> listCond1;
    private List<String> listCond2;
    
	public SPARQLSelectRendererVisitor(PrintWriter writer) {
		super(writer);
	}   

	public void init(Properties p) {
		if ( p.getProperty( "embedded" ) != null 
			     && !p.getProperty( "embedded" ).equals("") ) embedded = true;
		if ( p.getProperty( "indent" ) != null )
			INDENT = p.getProperty( "indent" );
		if ( p.getProperty( "newline" ) != null )
		    NL = p.getProperty( "newline" );
	}

	public void visit(Alignment align) throws AlignmentException {
		if ( subsumedInvocableMethod( this, align, Alignment.class ) ) return;
		alignment = align;		
		for( Cell c : align ){ c.accept( this ); };    	
	}	

	public void visit(Cell cell) throws AlignmentException {
		
    	if ( subsumedInvocableMethod( this, cell, Cell.class ) ) return;
    	String query = "";
    	this.cell = cell;    	
    	
    	URI u1 = cell.getObject1AsURI(alignment);
    	URI u2 = cell.getObject2AsURI(alignment);
    	if ( ( u1 != null && u2 != null)
    	     || alignment.getLevel().startsWith("2EDOAL") ){
    	    	resetVariables("s", "o");
	    		((Expression)(cell.getObject1())).accept( this );
	    		
	    		listBGP1 = new ArrayList<String>(getBGP());
	    		listCond1 = new ArrayList<String>(getCondition());
	    	
	    		resetVariables("s", "o");	    		
	    		((Expression)(cell.getObject2())).accept( this );
	    		listBGP2 = new ArrayList<String>(getBGP());
	    		listCond2 = new ArrayList<String>(getCondition());	    	
	    		
	    		for ( Enumeration e = prefixList.keys() ; e.hasMoreElements(); ) {
	    		    String k = (String)e.nextElement();
	    		    query += "PREFIX "+prefixList.get(k)+":<"+k+">"+NL;
	    		}
	    		query += "SELECT ?s WHERE {"+NL;
	    		query += listBGP1.get(listBGP1.size()-1)+NL;
	    		query += "}"+NL;	    		
	    		if(System.getProperty("Split")=="true")
	    			createQueryFiles(query);
	    		else
	    			indentedOutputln(query);
		    			    		
	    		query="";
	    		for ( Enumeration e = prefixList.keys() ; e.hasMoreElements(); ) {
	    		    String k = (String)e.nextElement();
	    		    query += "PREFIX "+prefixList.get(k)+":<"+k+">"+NL;
	    		}
	    		query += "SELECT ?s WHERE {"+NL;
	    		query += listBGP2.get(listBGP2.size()-1)+NL;
	    		query += "}"+NL;
	    		if(System.getProperty("Split")=="true")
	    			createQueryFiles(query);
	    		else
	    			indentedOutputln(query);
    	   }
    
	}

	public void visit( Relation rel ) throws AlignmentException {
		if ( subsumedInvocableMethod( this, rel, Relation.class ) ) return;
		// default behaviour
		// rel.write( writer );
    }
	
}
