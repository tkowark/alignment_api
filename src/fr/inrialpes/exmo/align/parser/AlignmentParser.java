/*
 * $Id$
 *
 * Copyright (C) INRIA Rhône-Alpes, 2003-2005, 2007
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

package fr.inrialpes.exmo.align.parser;

//Imported SAX classes
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

//Imported JAXP Classes
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;

//Imported JAVA classes
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.lang.Integer;
import java.lang.Double;
import java.util.Hashtable;

//*/3.0
//import org.semanticweb.owl.util.OWLManager;
//import org.semanticweb.owl.model.OWLOntology;
//import org.semanticweb.owl.model.OWLEntity;
//import org.semanticweb.owl.model.OWLException;
//import org.semanticweb.owl.io.owl_rdf.OWLRDFParser;
//import org.semanticweb.owl.io.owl_rdf.OWLRDFErrorHandler;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.AlignmentException;
import fr.inrialpes.exmo.align.impl.URIAlignment;

/**
 * This class allows the creation of a parser for an Alignment file.
 * The class is called by:
 * AlignmentParser parser = new AlignmentParser( debugLevel );
 * Alignment alignment = parser.parse( uri );
 * This new version (January 2004) parses the alignment description in
 * RDF/XML/OWL format
 */

public class AlignmentParser extends DefaultHandler {
	
    /**
     * level of debug/warning information
     */
    protected int debugMode = 0;
    
    /**
     * a URI to a process
     */
    protected String uri = null;
    
    /**
     * the first Ontology 
     */
    //*/3.0
    // OWLOntology onto1 = null;
    Object onto1 = null;
    
    /**
     * the second Ontology 
     */
    //*/3.0
    // OWLOntology onto2 = null;
    Object onto2 = null;

    /**
     * The currently loaded ontologies
     */
    //protected Hashtable ontologies = null;
    
    /**
     * the alignment that is parsed
     * We always create a URIAlignment (we could also use a BasicAlignment).
     * This is a pitty but the idea of creating a particular alignment
     * is not in accordance with using an interface.
     */
    //*/3.0
    //protected BasicAlignment alignment = null;
    protected URIAlignment alignment = null;
    
    /**
     * the content found as text...
     */
    protected String content = null;
    
    /**
     * the first entity of a cell
     */
    //*/3.0
    //protected OWLEntity cl1 = null;
    protected Object cl1 = null;
    
    /**
     * the second entity of a cell
     */
    //*/3.0
    //protected OWLEntity cl2 = null;
    protected Object cl2 = null;
    
    /**
     * the relation content as text...
     */
    protected Cell cell = null;
    
    /**
     * the relation content as text...
     */
    protected String relation = null;
    
    /**
     * the cell id as text...
     */
    protected String id = null;

    /**
     * the semantics of the cell (default first-order)...
     */
    protected String sem = null;

    /**
     * the measure content as text...
     */
    protected String measure = null;
    
    /**
     * XML Parser
1     */
    protected SAXParser parser = null;

    /**
     * The parsing level, if equal to 2 we are in the Alignment
     * and can find metadata
     */
    protected int parselevel = 0;
    
    /** 
     * Creates an XML Parser.
     * @param debugMode The value of the debug mode
     */
    public AlignmentParser( int debugMode) throws ParserConfigurationException, SAXException {
	this.debugMode = debugMode;
	SAXParserFactory parserFactory = SAXParserFactory.newInstance();
	if (debugMode > 0) {
	    parserFactory.setValidating(true);
	} else {
	    parserFactory.setValidating(false);
	}
	parserFactory.setNamespaceAware(true);
	parser = parserFactory.newSAXParser();
    }
    
    /** 
     * Parses the document corresponding to the URI given in parameter
     * If the current process has links (import or include) to others documents then they are 
     * parsed.
     * @param uri URI of the document to parse
     * @param loaded should be replaced by OntologyCache (by useless)
     * @deprecated use parse( URI ) instead
     */
    public Alignment parse( String uri, Hashtable loaded ) throws SAXException, IOException {
	return parse( uri );
    }

    /** 
     * Parses the document corresponding to the URI given in parameter
     * If the current process has links (import or include) to others documents then they are 
     * parsed.
     * @param uri URI of the document to parse
     */
    public Alignment parse( String uri ) throws SAXException, IOException {
	this.uri = uri;
	//ontologies = loaded;
	parser.parse(uri,this);
	return alignment;
    }
    
  /** 
   * Called by the XML parser at the begining of an element.
   * The corresponing graph component is create for each element.
   *
   * @param namespaceURI 	The namespace of the current element
   * @param pName 			The local name of the current element
   * @param qname					The name of the current element 
   * @param atts 					The attributes name of the current element 
   */
    public void startElement(String namespaceURI, String pName, String qname, Attributes atts) throws SAXException {
	if(debugMode > 2) 
	    System.err.println("startElement AlignmentParser : " + pName);
	parselevel++;
	if(namespaceURI.equals("http://knowledgeweb.semanticweb.org/heterogeneity/alignment"))  {
		    //*/3.0
	    //try {
		if (pName.equals("relation")) {
		} else if (pName.equals("semantics")) {
		} else if (pName.equals("measure")) {
		} else if (pName.equals("entity2")) {
		    if(debugMode > 2) 
			System.err.println(" resource = " + atts.getValue("rdf:resource"));
		    //*/3.0
		    //cl2 = (OWLEntity)getEntity( onto2, atts.getValue("rdf:resource") );
		    try {
			cl2 = new URI( atts.getValue("rdf:resource") );
		    } catch (URISyntaxException e) {
			throw new SAXException("Malformed URI: "+atts.getValue("rdf:resource"));
		    }
		} else if (pName.equals("entity1")) {
		    if(debugMode > 2) 
			System.err.println(" resource = " + atts.getValue("rdf:resource"));
		    //*/3.0
		    //cl1 = (OWLEntity)getEntity( onto1, atts.getValue("rdf:resource") );
		    try {
			cl1 = new URI( atts.getValue("rdf:resource") );
		    } catch (URISyntaxException e) {
			throw new SAXException("Malformed URI: "+atts.getValue("rdf:resource"));
		    }
		} else if (pName.equals("Cell")) {
		    if ( alignment == null )
			{ throw new SAXException("No alignment provided"); };
		    if ( atts.getValue("rdf:ID") != null ){
			id = atts.getValue("rdf:ID");
		    } else if ( atts.getValue("rdf:about") != null ){
			id = atts.getValue("rdf:about");
		    }
		    sem = null;
		    measure = null;
		    relation = null;
		    cl1 = null;
		    cl2 = null;
		} else if (pName.equals("map")) {
		    try {
			//*/3.0
			//if ( onto2 == null ){
			//    onto2 = loadOntology( alignment.getFile2() );
			//    if ( onto2 == null ) {
			//	throw new SAXException("Cannot find ontology"+alignment.getFile2());
			//    }
			//}
			alignment.setOntology2( onto2 );
			//*/3.0
			//if ( onto1 == null ){
			//    onto1 = loadOntology( alignment.getFile1() );
			//    if ( onto1 == null ) {
			//	throw new SAXException("Cannot find ontology"+alignment.getFile1());
			//    }
			//}
			alignment.setOntology1( onto1 );
		    } catch ( AlignmentException e ) {
			throw new SAXException("Catched alignment exception", e );
		    }
		} else if (pName.equals("onto2")) {
		} else if (pName.equals("onto1")) {
		} else if (pName.equals("uri2")) {
		} else if (pName.equals("uri1")) {
		} else if (pName.equals("type")) {
		} else if (pName.equals("level")) {
		} else if (pName.equals("xml")) {
		} else if (pName.equals("Alignment")) {
		    //*/3.0
		    //alignment = new BasicAlignment();
		    alignment = new URIAlignment();
		} else {
		    if ( debugMode > 0 ) System.err.println("[AlignmentParser] Unknown element name : "+pName);
		    //throw new SAXException("[AlignmentParser] Unknown element name : "+pName);
		};
		//*/3.0
		//} catch ( OWLException e ) { throw new SAXException("[AlignmentParser] OWLException raised"); }; 
	} else if(namespaceURI.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#"))  {
	    if ( !pName.equals("RDF") ) {
		throw new SAXException("[AlignmentParser] unknown element name: "+pName); };
	} else {
	    if ( parselevel != 2 ) throw new SAXException("[AlignmentParser] Unknown namespace : "+namespaceURI);
	}
    }

    //*/3.0
    //private OWLEntity getEntity( OWLOntology ontology, String name ) throws OWLException, SAXException {
    private Object getEntity( Object ontology, String name ) throws SAXException {
	URI uri = null;

	try { uri = new URI(name);}
	catch (URISyntaxException e) {throw new SAXException("[AlignmentParser] bad URI syntax : "+name);}

	//*/3.0
	//OWLEntity result = (OWLEntity)ontology.getClass( uri );
	//if ( result == null ) result = (OWLEntity)ontology.getDataProperty( uri );
	//if ( result == null ) result = (OWLEntity)ontology.getObjectProperty( uri );
	//if ( result == null ) result = (OWLEntity)ontology.getIndividual( uri );
	//return result;
	return uri;
    }

    /**
     * Put the content in a variable
     */
    public void characters(char ch[], int start, int length) {
	content = new String( ch, start, length );
	if(debugMode > 2) 
	    System.err.println("content AlignmentParser : " + content);
    }

    /*
    // Change proposed by Sabine Massmann
    // If to be integrated, then put it in the proper place
    // There is no reasons to test for Double in characters
   public void characters(char ch[], int start, int length) {
       String oldContent = "" + content;
       content = new String( ch, start, length );
       if ( content != null && !content.equals("\n") 
	    && !content.startsWith("\n ") 
	    //	    && oldContent.contains(".")
	    && oldContent.indexOf('.',0) != -1
	    ){
	   oldContent = oldContent.concat(content);
	   try {
	       double test = Double.parseDouble(oldContent);
	       content = oldContent;
	   } catch (NumberFormatException e) {
	       // TODO Auto-generated catch block
	       // e.printStackTrace();
	   }
       }
       if(debugMode > 2)
	   System.err.println("content AlignmentParser : " + content);
   }
    */

    /** 
     * Called by the XML parser at the end of an element.
     *
     * @param namespaceURI 	The namespace of the current element
     * @param pName 			The local name of the current element
     * @param qname					The name of the current element 
     */
    public  void endElement(String namespaceURI,String pName, String qName ) throws SAXException {
	if(debugMode > 2) 
	    System.err.println("endElement AlignmentParser : " + pName);
	if(namespaceURI.equals("http://knowledgeweb.semanticweb.org/heterogeneity/alignment"))  {
	    try {
		if (pName.equals("relation")) {
		    relation = content;
		} else if (pName.equals("measure")) {
		    measure = content;
		} else if (pName.equals("semantics")) {
		    sem = content;
		} else if (pName.equals("entity2")) {
		} else if (pName.equals("entity1")) {
		} else if (pName.equals("Cell")) {
		    if(debugMode > 1) {
			System.err.print(" " + cl1);
			System.err.print(" " + cl2);
			System.err.print(" " + relation);
			System.err.println(" " + Double.parseDouble(measure));
		    }
		    if ( cl1 == null || cl2 == null ) {
			// Maybe we could just print this out and fail in the end.
			//throw new SAXException( "Missing entity "+cl1+" "+cl2 );
			// The cell is void
			System.err.println("Warning (cell voided), missing entity "+cl1+" "+cl2 );
		    } else if ( measure == null || relation == null ){
			cell = alignment.addAlignCell( cl1, cl2);
		    } else {
			cell = alignment.addAlignCell( cl1, cl2, relation, Double.parseDouble(measure) );}
		    if ( id != null ) cell.setId( id );
		    if ( sem != null ) cell.setSemantics( sem );
		} else if (pName.equals("map")) {
		} else if (pName.equals("uri1")) {
		    //*/3.0
		    //onto1 = (OWLOntology)ontologies.get( content );
		    try {
			onto1 = new URI( content );
		    } catch (URISyntaxException e) {
			throw new SAXException("uri1: malformed URI");
		    }
		} else if (pName.equals("uri2")) {
		    //*/3.0
		    //onto2 = (OWLOntology)ontologies.get( content );
		    try {
			onto2 = new URI( content );
		    } catch (URISyntaxException e) {
			throw new SAXException("uri2: malformed URI");
		    }
		} else if (pName.equals("onto2")) {
		    try { alignment.setFile2( new URI( content ) );
		    } catch (URISyntaxException e) {
			throw new SAXException("onto2: malformed URI");
		    }
		} else if (pName.equals("onto1")) {
		    try { alignment.setFile1( new URI( content ) );
		    } catch (URISyntaxException e) {
			throw new SAXException("onto1: malformed URI");
		    }
		} else if (pName.equals("type")) {
		    alignment.setType( content );
		} else if (pName.equals("level")) {
		    alignment.setLevel( content );
		} else if (pName.equals("xml")) {
		    //if ( content.equals("no") )
		    //	{ throw new SAXException("Non parseable alignment"); }
		} else if (pName.equals("Alignment")) {
		} else {
		    if ( debugMode > 0 )
			System.err.println("[AlignmentParser] Unknown element name : "+pName);
		    //throw new SAXException("[AlignmentParser] Unknown element name : "+pName);
		};
		//*/3.0
	    } catch ( AlignmentException e ) { throw new SAXException("[AlignmentParser] OWLException raised"); };
	} else if(namespaceURI.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#"))  {
	    if ( !pName.equals("RDF") ) {
		throw new SAXException("[AlignmentParser] unknown element name: "+pName); };
	} else {
	    if ( parselevel == 2 ){
		alignment.setExtension( pName, content );
	    } else throw new SAXException("[AlignmentParser] Unknown namespace : "+namespaceURI);
	}
	parselevel--;
    } //end endElement
    
    /** Can be used for loading the ontology if it is not available **/
    //*/3.0
    /*
    private OWLOntology loadOntology( URI ref ) throws SAXException, OWLException {
	OWLOntology parsedOnt = null;
	OWLRDFParser parser = new OWLRDFParser();
	OWLRDFErrorHandler handler = new OWLRDFErrorHandler(){
		public void owlFullConstruct( int code, String message ) 
		    throws SAXException {
		}
		public void owlFullConstruct(int code, String message, Object o)
		    throws SAXException {
		}
		public void error( String message ) throws SAXException {
		    throw new SAXException( message.toString() );
		}
		public void warning( String message ) throws SAXException {
		    System.out.println("WARNING: " + message);
		}
	    };
	parser.setOWLRDFErrorHandler( handler );
	parser.setConnection( OWLManager.getOWLConnection() );
	try {
	    parsedOnt = parser.parseOntology( ref );
	    ontologies.put( ref.toString(), parsedOnt );
	    return parsedOnt;
	} catch ( Exception e ) {
	    throw new SAXException("[AlignmentParser] Error during parsing : "+ref);
	}
    }
*/
 
}//end class
    
