package fr.inrialpes.exmo.align.impl.renderer;


import java.io.PrintWriter;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.Relation;


import fr.inrialpes.exmo.align.impl.Annotations;
import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.Namespace;
import fr.inrialpes.exmo.align.parser.SyntaxElement;
import fr.inrialpes.exmo.align.impl.edoal.EDOALCell;
import fr.inrialpes.exmo.align.impl.edoal.Expression;
import fr.inrialpes.exmo.align.impl.edoal.Transformation;
import fr.inrialpes.exmo.ontowrap.Ontology;

import java.net.URI;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;


public class SILKRendererVisitor extends GraphPatternRendererVisitor implements AlignmentVisitor{


    Alignment alignment = null;
    Cell cell = null;
    Hashtable<String,String> nslist = null;
	boolean embedded = false;
	
	private static Namespace DEF = Namespace.ALIGNMENT;
	
	private List<String> listBGP1;
	private List<String> listBGP2;
	 
    private List<String> listCond1;
    private List<String> listCond2;
    
	public SILKRendererVisitor(PrintWriter writer) {
		super(writer);
	}   
	
	public SILKRendererVisitor(PrintWriter writer, String sub, String pred, String obj) {
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
    	// default behaviour
    	String extensionString = "";
    	alignment = align;
    	nslist = new Hashtable<String,String>();
            nslist.put( Namespace.RDF.prefix , Namespace.RDF.shortCut );
            nslist.put( Namespace.XSD.prefix , Namespace.XSD.shortCut );
    	// Get the keys of the parameter
    	int gen = 0;
    	for ( String[] ext : align.getExtensions() ) {
    	    String prefix = ext[0];
    	    String name = ext[1];
    	    String tag = nslist.get(prefix);
    	    //if ( tag.equals("align") ) { tag = name; }
    	    if ( prefix.equals( Namespace.ALIGNMENT.uri ) ) { tag = name; }
    	    else {
    		if ( tag == null ) {
    		    tag = "ns"+gen++;
    		    nslist.put( prefix, tag );
    		}
    		tag += ":"+name;
    	    }
    	    extensionString += INDENT+"<"+tag+">"+ext[2]+"</"+tag+">"+NL;
    	}
    	if ( embedded == false ) {
    	    writer.print("<?xml version='1.0' encoding='utf-8");
    	    writer.print("' standalone='no'?>"+NL);
    	}
    	indentedOutputln("<SILK>");
    	indentedOutputln("<Prefixes>");
    	indentedOutputln("<Prefix id=\"rdf\" namespace=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" />");
    	indentedOutputln("<Prefix id=\"rdfs\" namespace=\"http://www.w3.org/2000/01/rdf-schema#\" />");
    	indentedOutputln("<Prefix id=\"owl\" namespace=\"http://www.w3.org/2002/07/owl#\" />");
    	for ( Enumeration e = prefixList.keys() ; e.hasMoreElements(); ) {
		    String k = (String)e.nextElement();
		    indentedOutputln("<Prefix id=\""+k+" namespace=\""+prefixList.get(k)+"\" />");
		}
    	indentedOutputln("</Prefixes>"+NL);
    	indentedOutputln("<DataSources>"+NL);
    	
    	indentedOutputln("</DataSources>"+NL);
    	indentedOutputln("<Interlinks>");
    	for( Cell c : align ){ c.accept( this ); };
    	decreaseIndent();
    	indentedOutputln("</Interlinks>");
    	writer.print("</SILK>"+NL);
    
	}	
	
	public void visit(Cell cell) throws AlignmentException {
		
    	if ( subsumedInvocableMethod( this, cell, Cell.class ) ) return;
    	// default behaviour
    	this.cell = cell;      	
    	
    	URI u1 = cell.getObject1AsURI(alignment);
    	URI u2 = cell.getObject2AsURI(alignment);
    	if ( ( u1 != null && u2 != null)
    	     || alignment.getLevel().startsWith("2EDOAL") ){ //expensive test
    	    
    	    if ( alignment.getLevel().startsWith("2EDOAL") ) {
    	    	
    	    	resetVariables("s", "o");
	    		((Expression)(cell.getObject1())).accept( this );
	    		
	    		List<String> tempList = new ArrayList<String>(getBGP());
	    		listBGP1 = new ArrayList<String>(tempList);
	    		tempList = new ArrayList<String>(getCondition());
	    		listCond1 = new ArrayList<String>(tempList);
	    	
	    		resetVariables("x", "y");	    		
	    		((Expression)(cell.getObject2())).accept( this );
	    		tempList = new ArrayList<String>(getBGP());
	    		listBGP2 = new ArrayList<String>(tempList);
	    		tempList = new ArrayList<String>(getCondition());
	    		listCond2 = new ArrayList<String>(tempList);
	    	
	    		increaseIndent();
	    		indentedOutputln("<Interlink id=\"link\">");
	    		indentedOutputln("<LinkType>owl:sameAs</LinkType>");
	    		indentedOutputln("<SourceDataSet datasource=\"\"" + " var=\"s\">");
	    		indentedOutputln("<RestrictTo>");

	    		indentedOutput(listBGP1.get(listBGP1.size()-1));
	    		
	    		indentedOutputln("</RestrictTo>");
	    		indentedOutputln("</SourceDataSet>");

	    		indentedOutputln("<TargetDataSet datasource=\"\"" + " var=\"x\">");
	    		indentedOutputln("<RestrictTo>");

	    		indentedOutput(listBGP2.get(listBGP2.size()-1));
	    		
	    		indentedOutputln("</RestrictTo>");
	    		indentedOutputln("</TargetDataSet>"+NL);

	    		indentedOutputln("<LinkageRule>"+NL);
	    		indentedOutputln("</LinkageRule>"+NL);
	    		indentedOutputln("<Filter />"+NL);
	    		indentedOutputln("<Outputs>");	    		
	    		indentedOutputln("<Output type=\"file\">");
	    		indentedOutputln("<Param name=\"file\" value=\"link.nt\"/>");
	    		indentedOutputln(" <Param name=\"format\" value=\"ntriples\"/>");
	    		indentedOutputln("</Output>");
	    		indentedOutputln("</Outputs>");
	    		indentedOutputln("</Interlink>"+NL);
	    		decreaseIndent();

    	    }    		    		
    		decreaseIndent();
    	    
    	}
    
	}

	public void visit( Relation rel ) throws AlignmentException {
		if ( subsumedInvocableMethod( this, rel, Relation.class ) ) return;
		// default behaviour
		// rel.write( writer );
    }
	
}
