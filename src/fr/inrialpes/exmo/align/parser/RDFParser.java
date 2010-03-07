/*
 * $Id$
 *
 * Copyright (C) 2006 Digital Enterprise Research Insitute (DERI) Innsbruck
 * Sourceforge version 1.7 - 2008
 * Copyright (C) INRIA, 2008-2010
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA
 */

package fr.inrialpes.exmo.align.parser;

import org.semanticweb.owl.align.AlignmentException;

import fr.inrialpes.exmo.ontowrap.Ontology;
import fr.inrialpes.exmo.ontowrap.BasicOntology;

import fr.inrialpes.exmo.align.impl.Annotations;
import fr.inrialpes.exmo.align.impl.Namespace;

import fr.inrialpes.exmo.align.impl.edoal.EDOALAlignment;
import fr.inrialpes.exmo.align.impl.edoal.EDOALCell;
import fr.inrialpes.exmo.align.impl.edoal.EDOALRelation;
import fr.inrialpes.exmo.align.impl.edoal.Expression;
import fr.inrialpes.exmo.align.impl.edoal.Id;
import fr.inrialpes.exmo.align.impl.edoal.Expression;
import fr.inrialpes.exmo.align.impl.edoal.ClassExpression;
import fr.inrialpes.exmo.align.impl.edoal.ClassId;
import fr.inrialpes.exmo.align.impl.edoal.ClassConstruction;
import fr.inrialpes.exmo.align.impl.edoal.ClassRestriction;
import fr.inrialpes.exmo.align.impl.edoal.ClassTypeRestriction;
import fr.inrialpes.exmo.align.impl.edoal.ClassValueRestriction;
import fr.inrialpes.exmo.align.impl.edoal.ClassOccurenceRestriction;
import fr.inrialpes.exmo.align.impl.edoal.PathExpression;
import fr.inrialpes.exmo.align.impl.edoal.PropertyExpression;
import fr.inrialpes.exmo.align.impl.edoal.PropertyId;
import fr.inrialpes.exmo.align.impl.edoal.PropertyConstruction;
import fr.inrialpes.exmo.align.impl.edoal.PropertyRestriction;
import fr.inrialpes.exmo.align.impl.edoal.PropertyDomainRestriction;
import fr.inrialpes.exmo.align.impl.edoal.PropertyTypeRestriction;
import fr.inrialpes.exmo.align.impl.edoal.PropertyValueRestriction;
import fr.inrialpes.exmo.align.impl.edoal.RelationExpression;
import fr.inrialpes.exmo.align.impl.edoal.RelationId;
import fr.inrialpes.exmo.align.impl.edoal.RelationConstruction;
import fr.inrialpes.exmo.align.impl.edoal.RelationRestriction;
import fr.inrialpes.exmo.align.impl.edoal.RelationDomainRestriction;
import fr.inrialpes.exmo.align.impl.edoal.RelationCoDomainRestriction;
import fr.inrialpes.exmo.align.impl.edoal.InstanceExpression;
import fr.inrialpes.exmo.align.impl.edoal.InstanceId;

import fr.inrialpes.exmo.align.impl.edoal.TransfService;
import fr.inrialpes.exmo.align.impl.edoal.Value;
import fr.inrialpes.exmo.align.impl.edoal.Datatype;
import fr.inrialpes.exmo.align.impl.edoal.Comparator;

import fr.inrialpes.exmo.align.parser.SyntaxElement.Constructor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.LinkedList;

// JE2010: to be suppressed
// How do I shut up the logger?

import java.util.logging.Logger;
import java.util.logging.Handler;
import java.util.logging.Level;

// Yes we are relying on Jena for parsing RDF
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.RDFList;
import com.hp.hpl.jena.rdf.model.Container;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.impl.RDFDefaultErrorHandler;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * <p>
 * Parser for the omwg xml mapping syntax. The document reader is Jena, input can be any document 
 * which can be read by Jena. The input document format shall be consistent with format document -- 
 * Expressive alignment language and implementation.  You can also see the example input document--example.rdf
 * 
 * </p>
 * <p>
 * $Id$
 * </p>
 * 
 * @author Richard Pöttler
 * @version $Revision: 1.7 $
 * @date $Date: 2010-03-07 20:41:03 +0100 (Sun, 07 Mar 2010) $
 */
public class RDFParser {

    static Logger logger = Logger.getLogger(RDFParser.class.toString());

    static Model rDFModel;

    static private boolean debug = false;

    /**
     * Initialisation of the structures
     * This creates an RDF Model which contains all the syntactic elements.
     * This is to be called before parsing, i.e., before exploring RDF resources
     */
    public static void initSyntax() {
	if ( rDFModel == null ) {
	    rDFModel = ModelFactory.createDefaultModel();
	    // The register is always initialised?
	    for ( SyntaxElement el : SyntaxElement.values() ) {
		// JE2010 This is incorrect because there can be Resources (classes)
		// and/or Property ... They may all be resources...
		if ( el.isProperty == true ) {
		    el.resource = rDFModel.createProperty( el.id() );
		} else {
		    el.resource = rDFModel.createResource( el.id() );
		}
		//register.put( el.getName(), el );
	    }
	}
    }

    /**
     * Parse the input model. The model shall include one statement that include (?,RDF.type,Alignment)
     * @param align
     * @return the result EDOALAlignment
     * @throws AlignmentException if there is any exception, throw AlignmentException that include describe infomation
     * and a caused exception.
     */
    public static EDOALAlignment parse( final Model align ) throws AlignmentException {
	// Initialize the syntax description
	initSyntax();
	// Shut up logging handling (should put a 
	com.hp.hpl.jena.rdf.model.impl.RDFDefaultErrorHandler.silent = true;
	//get the statement including alignment resource as rdf:type
	StmtIterator stmtIt = align.listStatements(null, RDF.type,(Resource)SyntaxElement.getResource("Alignment"));
	// take the first one if it exists
	Statement alignDoc;
	if (stmtIt.hasNext()) {
	    alignDoc = stmtIt.nextStatement();
	} else {
	    throw new AlignmentException("There is no alignment in the RDF docuemnt");
	}

	// Step from this statement
	final EDOALAlignment doc = parseAlignment( alignDoc.getSubject() );
	// JE 2010: Clean up the RDF stuff
	return doc;
//
//		 getting and adding the xml namespaces
//		 final NamedNodeMap attrs = root.getAttributes();
//		 for (int iCounter = 0; iCounter < attrs.getLength(); iCounter++) {
//		 final Node tempNode = attrs.item(iCounter);
//		 if (tempNode.getNodeName().equals("xmlns")) {
//		 doc.addNamespace(new Namespace(Namespace.DEFAULT_NS_PREFIX,
//		 new URI(tempNode.getNodeValue())));
//		 } else if (tempNode.getNodeName().startsWith("xmlns")) {
//		 doc.addNamespace(new Namespace(tempNode.getNodeName()
//		 .substring(6), new URI(tempNode.getNodeValue())));
//		 }
//		 }
    }

    // Below is the plumbing:
    // Load the RDF under an RDFModel
    // Call the above parse: RDFModel -> EDOALAlignment

    public static EDOALAlignment parse( final File file )
			throws AlignmentException {
	Model align = ModelFactory.createDefaultModel();
	try {
	    align.read(new FileInputStream(file), null);
	} catch ( FileNotFoundException fnfe ) {
	    throw new AlignmentException("RDFParser: There isn't such file: "
					 + file.getName(), fnfe);
	}
	return parse(align);
    }

    public static EDOALAlignment parse(final Reader is)
	throws AlignmentException {
	if (is == null) {
	    throw new AlignmentException("The inputstream must not be null");
	}
	Model align = ModelFactory.createDefaultModel();
	align.read(is, null);
	// debug align.write(System.out);
	return parse(align);
    }
    
    public static EDOALAlignment parse(final InputStream is)
	throws AlignmentException {
	if (is == null) {
	    throw new AlignmentException("The inputstream must not be null");
	}
	Model align = ModelFactory.createDefaultModel();
	align.read(is, null);
	//debug	align.write(System.out);
	return parse(align);
    }

    public static EDOALAlignment parse(final String file)
	throws AlignmentException {
	Model align = ModelFactory.createDefaultModel();
	align.read(file);
	return parse(align);
    }

    // Below is the real work
    /**
     * Parses a mapping document. The resource passed to this method must be a
     * <code>&lt;Alignment&gt;</code> tag.
     * 
     * @param node    the alignment resource
     * @return the parsed mapping document
     * @throws AlignmentException
     */
    static EDOALAlignment parseAlignment( final Resource node ) throws AlignmentException {
	if (node == null) {
	    throw new NullPointerException("Alignment must not be null");
	}

	try {
	    Ontology source = null;
	    Ontology target = null;
	    
	    // getting the id of the document
	    final URI id = getNodeId( node );
	    
	    final EDOALAlignment doc = new EDOALAlignment();
	    if ( id != null )
		doc.setExtension( Namespace.ALIGNMENT.uri, Annotations.ID, id.toString() );
	    
	    StmtIterator stmtIt = node.listProperties((Property)SyntaxElement.MAPPING_SOURCE.resource );
	    if ( stmtIt.hasNext() ) {
		source = parseOntology( stmtIt.nextStatement().getResource() );
	    } else {
		throw new AlignmentException( "Missing ontology "+"onto1" );
	    }
	    stmtIt = node.listProperties((Property)SyntaxElement.MAPPING_TARGET.resource );
	    if ( stmtIt.hasNext() ) {
		target = parseOntology( stmtIt.nextStatement().getResource() );
	    } else {
		throw new AlignmentException( "Missing ontology "+"onto2" );
	    }
	    stmtIt = node.listProperties((Property)SyntaxElement.LEVEL.resource );
	    if ( stmtIt.hasNext() ) {
		final String level = stmtIt.nextStatement().getString();
		if ((level != null) && (!level.equals(""))) {
		    doc.setLevel( level );
		}			    
	    } else {
		throw new AlignmentException( "Missing level " );
	    }
	    stmtIt = node.listProperties((Property)SyntaxElement.TYPE.resource );
	    if ( stmtIt.hasNext() ) {
		final String arity = stmtIt.nextStatement().getString();
		if ((arity != null) && (!arity.equals(""))) {
		    // JE2009: Certainly some control checking should be useful
		    doc.setType( arity );
		}
	    } else {
		throw new AlignmentException( "Missing type " );
	    }
	    
	    stmtIt = node.listProperties((Property)SyntaxElement.MAP.resource );
	    while (stmtIt.hasNext()) {
		Statement stmt = stmtIt.nextStatement();
		if ( debug ) System.err.println( "  ---------------> "+stmt );
		//doc.addRule(parseCell(stmt.getResource()));
		try { doc.addAlignCell( parseCell( stmt.getResource() ) ); }
		catch ( AlignmentException ae ) {
		    System.err.println( "Error "+ae );
		    ae.printStackTrace();
		}
		// rdf:type must be forgotten
	    }

	    // Remaining resources...
	    //else if ( !pred.equals( SyntaxElement.getResource("rdftype") ) ) { // Unknown is annotation
	    //	parseAnnotation( stmt, doc );
	    //}

	    if ( source != null && target != null ) {
		doc.init( source, target );
	    } else {
		throw new IllegalArgumentException("Missing ontology description");
	    }
	    return doc;
	    
	} catch (AlignmentException e) {
	    throw e;
	} catch (Exception e) {
	    throw new AlignmentException("There is some error in parsing alignment: " + node.getLocalName(), e);
	}
    }

    /**
     * Parse an ontology node <code>&lt;onto1&gt;</code> or
     * <code>&lt;onto2&gt;</code> Node to an Ontology object. The node must
     * contain the <code>&lt;onto...&gt;</code> element.
     * 
     * @param node
     *            the ontology node
     * @return the Ontology object
     * @throws NullPointerException
     *             if the node is null
     */
    static Ontology parseOntology(final Resource node) throws AlignmentException {
	if (node == null) {
	    throw new AlignmentException("The ontology node must not be null");
	}

	try {
	    Resource formu = node.getProperty((Property)SyntaxElement.FORMATT.resource).getResource();
	    final String formalismName = formu.getProperty((Property)SyntaxElement.NAME.resource).getString();
	    final String formalismUri = formu.getProperty((Property)SyntaxElement.URI.resource).getString();
	    final Statement location = node.getProperty((Property)SyntaxElement.LOCATION.resource);
	    Ontology onto = new BasicOntology();
	    onto.setURI( new URI( node.getURI() ) );
	    onto.setFormURI( new URI( formalismUri ) );
	    onto.setFormalism( formalismName );
	    if ( location != null ) onto.setFile( new URI( location.getString() ) );
	    return onto;
	} catch ( Exception e ) {
	    throw new AlignmentException("The ontology node isn't correct: "
					 + node.getLocalName(), e);
	}
    }

    /**
     * Parses a mapping rule. The parsed node must be a Cell resource including the mandatory Statement.
     * <code>&lt;Cell&gt;</code> tag.
     * 
     * @param node
     *            the <code>&lt;Cell&gt;</code> tag
     * @return the parsed rule
     * @exception AlignmentException
     */
    public static EDOALCell parseCell( final Resource node ) throws AlignmentException {
	if (node == null) {
	    throw new NullPointerException("The node must not be null");
	}
	try {
	    // JE2009: Should be better to use AlignmentAPI relation recognition
	    // determine the relation, the relation shall be Literal
	    final String relation = node.getProperty((Property)SyntaxElement.RULE_RELATION.resource).getString();
	    //Get the relation
	    final EDOALRelation type = new EDOALRelation( relation );
	    if (type == null) {	// I raise an error in this case anyway
		throw new IllegalArgumentException("Couln't parse the string \"" + relation
						   +"\" to a valid rule type");
	    }
	    
	    // parse the measure, the node shall be Literal and it's a number
	    final float m = node.getProperty((Property)SyntaxElement.MEASURE.resource).getFloat();
	    
	    // get the id
	    final URI id = getNodeId( node );
	    
	    //parsing the entity1 and entity2 
	    Resource entity1 = node.getProperty((Property)SyntaxElement.ENTITY1.resource).getResource();
	    Resource entity2 = node.getProperty((Property)SyntaxElement.ENTITY2.resource).getResource();

	    // JE2010:
	    // Here it would be better to check if the entity has a type.
	    // If both have none, then we get a old-style correspondence for free
	    // If it has one, let's go parsing
	    // I also assume that the factory only do dispatch
	    
	    Expression s = parseExpression( entity1 );
	    Expression t = parseExpression( entity2 );
	    if ( debug ) {
		System.err.println(" s : "+s);	    
		System.err.println(" t : "+t);
	    }

	    return new EDOALCell( id.toString(), s, t, type, m );
	} catch (Exception e) {  //wrap other type exception
	    logger.log(java.util.logging.Level.SEVERE, "The cell isn't correct:" + node.getLocalName() + " "+e.getMessage());
	    throw new AlignmentException("Cannot parse correspondence " + node.getLocalName(), e);
	}
    }

    // Here given the type of expression, this can be grand dispatch
    public static Expression parseExpression( final Resource node ) throws AlignmentException {
	Resource rdfType = node.getProperty( RDF.type ).getResource();
	if ( rdfType.equals( SyntaxElement.CLASS_EXPR.resource ) ||
	     rdfType.equals( SyntaxElement.PROPERTY_OCCURENCE_COND.resource ) ||
	     rdfType.equals( SyntaxElement.PROPERTY_TYPE_COND.resource ) ||
	     rdfType.equals( SyntaxElement.PROPERTY_VALUE_COND.resource ) ) {
	    return parseClass( node );
	} else if ( rdfType.equals( SyntaxElement.PROPERTY_EXPR.resource ) ||
		    rdfType.equals( SyntaxElement.DOMAIN_RESTRICTION.resource ) ||
		    rdfType.equals( SyntaxElement.TYPE_COND.resource ) ||
		    rdfType.equals( SyntaxElement.VALUE_COND.resource ) ) {
	    return parseProperty( node );
	} else if ( rdfType.equals( SyntaxElement.RELATION_EXPR.resource ) ||
		    rdfType.equals( SyntaxElement.DOMAIN_RESTRICTION.resource ) || // JE 2010: no chance
		    rdfType.equals( SyntaxElement.CODOMAIN_RESTRICTION.resource ) ) {
	    return parseRelation( node );
	} else if ( rdfType.equals( SyntaxElement.INSTANCE_EXPR.resource ) ) {
	    return parseInstance( node );
	} else {
	    throw new AlignmentException("There is no parser for entity "+rdfType.getLocalName());
	}
    }
    
    public static ClassExpression parseClass( final Resource node ) throws AlignmentException {
	if ( debug ) {
	    StmtIterator it = node.listProperties();
	    while ( it.hasNext() ) System.err.println( "   > "+it.next() );
	}
	Resource rdfType = node.getProperty(RDF.type).getResource();
	if ( rdfType.equals( SyntaxElement.CLASS_EXPR.resource ) ) {
	    URI id = getNodeId( node );
	    if ( id != null ) {
		return new ClassId( id );
	    } else {
		Statement stmt = null;
		Constructor op = null;
		// Using a List preserves the order... useful mostly for COMPOSE
		// Given the Jena encoding of Collection, LinkedList seems the best
		List<ClassExpression> clexpr = new LinkedList<ClassExpression>();
		if ( node.hasProperty( (Property)SyntaxElement.AND.resource ) ) {
		    op = SyntaxElement.AND.getOperator();
		    // listProperties would give them all
		    stmt = node.getProperty( (Property)SyntaxElement.AND.resource );
		} else if ( node.hasProperty( (Property)SyntaxElement.OR.resource ) ) { 
		    op = SyntaxElement.OR.getOperator();
		    stmt = node.getProperty( (Property)SyntaxElement.OR.resource );
		} else if ( node.hasProperty( (Property)SyntaxElement.NOT.resource ) ) {
		    op = SyntaxElement.NOT.getOperator();
		    stmt = node.getProperty( (Property)SyntaxElement.NOT.resource );
		} else {
		    throw new AlignmentException( "Class statement must containt one constructor or Id : "+node );
		}
		//JE2010MUSTCHECK
		Resource coll = stmt.getResource(); //JE2010MUSTCHECK
		if ( op == SyntaxElement.NOT.getOperator() ) {
		    clexpr.add( parseClass( coll ) );
		} else { // Jena encode these collections as first/rest statements
		    // THIS IS HORRIBLE BUT I DID NOT FOUND BETTER!
		    while ( !RDF.nil.getURI().equals( coll.getURI() ) ) {
			clexpr.add( parseClass( coll.getProperty( RDF.first ).getResource() ) );
			coll = coll.getProperty( RDF.rest ).getResource();
		    }
		}
		return new ClassConstruction( op, clexpr );
	    }
	} else {
	    if ( !rdfType.equals( SyntaxElement.PROPERTY_OCCURENCE_COND.resource ) &&
		 !rdfType.equals( SyntaxElement.PROPERTY_TYPE_COND.resource ) &&
		 !rdfType.equals( SyntaxElement.PROPERTY_VALUE_COND.resource ) ) {
		throw new AlignmentException( "Bad class restriction type : "+rdfType );
	    }
	    PathExpression pe;
	    Comparator comp;
	    // Find onProperty
	    Statement stmt = node.getProperty( (Property)SyntaxElement.ONPROPERTY.resource );
	    if ( stmt == null ) throw new AlignmentException( "Required edoal:onProperty property" );
	    //JE2010MUSTCHECK
	    pe = parsePathExpression( stmt.getResource() );
	    if ( rdfType.equals( SyntaxElement.PROPERTY_TYPE_COND.resource ) ) {
		// Datatype could also be defined as objets...? (like rdf:resource="")
		// Or classes? OF COURSE????
		stmt = node.getProperty( (Property)SyntaxElement.DATATYPE.resource );
		if ( stmt == null ) throw new AlignmentException( "Required edoal:datatype property" );
		RDFNode nn = stmt.getObject();
		if ( nn.isLiteral() ) {
		    return new ClassTypeRestriction( pe, new Datatype( ((Literal)nn).getString() ) );
		} else {
		    throw new AlignmentException( "Bad edoal:datatype value" );
		}
	    } else {
		// Find comparator
		// JE2010: This is not good as comparator management...
		stmt = node.getProperty( (Property)SyntaxElement.COMPARATOR.resource );
		if ( stmt == null ) throw new AlignmentException( "Required edoal:comparator property" );
		URI id = getNodeId( stmt.getResource() );
		if ( id != null ) comp = new Comparator( id );
		else throw new AlignmentException("Cannot parse anonymous individual");
		/*
		  try {
		  comp = new Comparator ( new URI( stmt.getResource().getURI() ) );
		  } catch ( URISyntaxException usex ) {
		  throw new AlignmentException( "Bad URI for comparator: "+stmt.getResource().getURI(), usex );
		  }
		*/
		if ( rdfType.equals( SyntaxElement.PROPERTY_OCCURENCE_COND.resource ) ) {
		    stmt = node.getProperty( (Property)SyntaxElement.VALUE.resource );
		    if ( stmt == null ) throw new AlignmentException( "Required edoal:value property" );
		    RDFNode nn = stmt.getObject();
		    if ( nn.isLiteral() ) {
			return new ClassOccurenceRestriction( pe, comp, ((Literal)nn).getInt() );
		    } else {
			throw new AlignmentException( "Bad occurence specification : "+nn );
		    }
		} else if ( rdfType.equals( SyntaxElement.PROPERTY_VALUE_COND.resource ) ) {
		    stmt = node.getProperty( (Property)SyntaxElement.VALUE.resource );
		    if ( stmt == null ) throw new AlignmentException( "Required edoal:value property" );
		    RDFNode nn = stmt.getObject();
		    if ( nn.isLiteral() ) {
			return new ClassValueRestriction( pe, comp, new Value( ((Literal)nn).getString() ) );
		    } else if ( nn.isResource() ) {
			// get the type
			Resource nnType = ((Resource)nn).getProperty(RDF.type).getResource();
			if ( nnType.equals( SyntaxElement.INSTANCE_EXPR.resource ) ) {
			    return new ClassValueRestriction( pe, comp, parseInstance( (Resource)nn ) );
			} else {
			    return new ClassValueRestriction( pe, comp, parsePathExpression( (Resource)nn ) );
			} // This one will raise the error
		    } else {
			throw new AlignmentException( "Bad edoal:value value" );
		    }
		}
	    }
	}
	return null;
    }

    // JE2010: Here is the problem again with DOMAIN (for instance)
    static PathExpression parsePathExpression( final Resource node ) throws AlignmentException {
	Resource rdfType = node.getProperty(RDF.type).getResource();
	if ( rdfType.equals( SyntaxElement.PROPERTY_EXPR.resource ) ||
	     rdfType.equals( SyntaxElement.DOMAIN_RESTRICTION.resource ) ||
	     rdfType.equals( SyntaxElement.TYPE_COND.resource ) ||
	     rdfType.equals( SyntaxElement.VALUE_COND.resource ) ) {
	    return parseProperty( node );
	} else if ( rdfType.equals( SyntaxElement.RELATION_EXPR.resource ) ||
	     rdfType.equals( SyntaxElement.CODOMAIN_RESTRICTION.resource ) ||
	     rdfType.equals( SyntaxElement.VALUE_COND.resource ) ) {
	    return parseRelation( node );
	} else throw new AlignmentException( "Cannot parse path expression ("+rdfType+"): "+node );
	
    }

    // rdf:parseType="Collection" is supposed to preserve the order ()
    // Jena indeed always preserves the order so this can be used
    static PropertyExpression parseProperty( final Resource node ) throws AlignmentException {
	Resource rdfType = node.getProperty(RDF.type).getResource();
	Statement stmt = null;
	if ( rdfType.equals( SyntaxElement.PROPERTY_EXPR.resource ) ) {
	    URI id = getNodeId( node );
	    if ( id != null ) {
		return new PropertyId( id );
	    } else {
		Constructor op = null;
		List<PathExpression> clexpr = new LinkedList<PathExpression>();
		if ( node.hasProperty( (Property)SyntaxElement.AND.resource ) ) {
		    op = SyntaxElement.AND.getOperator();
		    stmt = node.getProperty( (Property)SyntaxElement.AND.resource );
		} else if ( node.hasProperty( (Property)SyntaxElement.OR.resource ) ) { 
		    op = SyntaxElement.OR.getOperator();
		    stmt = node.getProperty( (Property)SyntaxElement.OR.resource );
		} else if ( node.hasProperty( (Property)SyntaxElement.COMPOSE.resource ) ) { 
		    op = SyntaxElement.COMPOSE.getOperator();
		    stmt = node.getProperty( (Property)SyntaxElement.COMPOSE.resource );
		} else if ( node.hasProperty( (Property)SyntaxElement.NOT.resource ) ) {
		    op = SyntaxElement.NOT.getOperator();
		    stmt = node.getProperty( (Property)SyntaxElement.NOT.resource );
		} else {
		    throw new AlignmentException( "Property statement must containt one constructor or Id : "+node );
		}
		Resource coll = stmt.getResource(); //JE2010MUSTCHECK
		if ( op == SyntaxElement.NOT.getOperator() ) {
		    clexpr.add( parseProperty( coll ) );
		} else if ( op == SyntaxElement.COMPOSE.getOperator() ) {
		    // THIS IS HORRIBLE BUT I DID NOT FOUND BETTER!
		    while ( !RDF.nil.getURI().equals( coll.getURI() ) ) {
			// In this present case, I have to parse a series of Relations
			// followed by a Property
			Resource newcoll = coll.getProperty( RDF.rest ).getResource(); //JE2010MUSTCHECK
			if ( !RDF.nil.getURI().equals( newcoll.getURI() ) ) {
			    clexpr.add( parseRelation( coll.getProperty( RDF.first ).getResource() ) );
			} else {
			    clexpr.add( parseProperty( coll.getProperty( RDF.first ).getResource() ) );
			}
			coll = newcoll;
		    }
		} else { // This is a first/rest statements
		    // THIS IS HORRIBLE BUT I DID NOT FOUND BETTER!
		    while ( !RDF.nil.getURI().equals( coll.getURI() ) ) {
			//JE2010MUSTCHECK
			clexpr.add( parseProperty( coll.getProperty( RDF.first ).getResource() ) );
			coll = coll.getProperty( RDF.rest ).getResource();
		    }
		}
		return new PropertyConstruction( op, clexpr );
	    }
	} else if ( rdfType.equals( SyntaxElement.DOMAIN_RESTRICTION.resource ) ) {
	    stmt = node.getProperty( (Property)SyntaxElement.TOCLASS.resource );
	    if ( stmt == null ) throw new AlignmentException( "Required edoal:toClass property" );
	    RDFNode nn = stmt.getObject();
	    if ( nn.isResource() ) {
		return new PropertyDomainRestriction( parseClass( (Resource)nn ) );
	    } else {
		throw new AlignmentException( "Incorrect class expression "+nn );
	    } 
	} else if ( rdfType.equals( SyntaxElement.TYPE_COND.resource ) ) {
	    // Datatype could also be defined as objets...? (like rdf:resource="")
	    // Or classes? OF COURSE????
	    stmt = node.getProperty( (Property)SyntaxElement.DATATYPE.resource );
	    if ( stmt == null ) throw new AlignmentException( "Required edoal:datatype property" );
	    RDFNode nn = stmt.getObject();
	    if ( nn.isLiteral() ) {
		return new PropertyTypeRestriction( new Datatype( ((Literal)nn).getString() ) );
	    } else {
		throw new AlignmentException( "Bad edoal:datatype value" );
	    }
	} else if ( rdfType.equals( SyntaxElement.VALUE_COND.resource ) ) {
	    // Find comparator
	    // JE2010: This is not good as comparator management...
	    stmt = node.getProperty( (Property)SyntaxElement.COMPARATOR.resource );
	    if ( stmt == null ) throw new AlignmentException( "Required edoal:comparator property" );
	    URI id = getNodeId( stmt.getResource() );
	    if ( id == null ) throw new AlignmentException("Bad comparator");
	    Comparator comp = new Comparator( id );
	    /*
	      try {
	      comp = new Comparator ( new URI( stmt.getResource().getURI() ) );
	      } catch ( URISyntaxException usex ) {
	      throw new AlignmentException( "Bad URI for comparator: "+stmt.getResource().getURI(), usex );
	      }
	    */
	    stmt = node.getProperty( (Property)SyntaxElement.VALUE.resource );
	    if ( stmt == null ) throw new AlignmentException( "Required edoal:value property" );
	    RDFNode nn = stmt.getObject();
	    if ( nn.isLiteral() ) {
		return new PropertyValueRestriction( comp, new Value( ((Literal)nn).getString() ) );
	    } else if ( nn.isResource() ) {
		// get the type
		Resource nnType = ((Resource)nn).getProperty(RDF.type).getResource();
		if ( nnType.equals( SyntaxElement.INSTANCE_EXPR.resource ) ) {
		    return new PropertyValueRestriction( comp, parseInstance( (Resource)nn ) );
		} else {
		    //return new ClassValueRestriction( pe, comp, parsePathExpression( (Resource)nn ) );
		    throw new AlignmentException( "Connot restrict value to "+nnType );
		} 
	    } else {
		throw new AlignmentException( "Bad edoal:value value" );
	    }
	} else {
	    throw new AlignmentException("There is no pasrser for entity "+rdfType.getLocalName());
	}
    }

    static RelationExpression parseRelation( final Resource node ) throws AlignmentException {
	Resource rdfType = node.getProperty(RDF.type).getResource();
	Statement stmt = null;
	if ( rdfType.equals( SyntaxElement.RELATION_EXPR.resource ) ) {
	    URI id = getNodeId( node );
	    if ( id != null ) {
		return new RelationId( id );
	    } else {
		Constructor op = null;
		// JE2010: does not compile with RelationExpression !!
		List<PathExpression> clexpr = new LinkedList<PathExpression>();
		if ( node.hasProperty( (Property)SyntaxElement.AND.resource ) ) {
		    op = SyntaxElement.AND.getOperator();
		    stmt = node.getProperty( (Property)SyntaxElement.AND.resource );
		} else if ( node.hasProperty( (Property)SyntaxElement.OR.resource ) ) { 
		    op = SyntaxElement.OR.getOperator();
		    stmt = node.getProperty( (Property)SyntaxElement.OR.resource );
		} else if ( node.hasProperty( (Property)SyntaxElement.COMPOSE.resource ) ) { 
		    op = SyntaxElement.COMPOSE.getOperator();
		    stmt = node.getProperty( (Property)SyntaxElement.COMPOSE.resource );
		} else if ( node.hasProperty( (Property)SyntaxElement.NOT.resource ) ) {
		    op = SyntaxElement.NOT.getOperator();
		    stmt = node.getProperty( (Property)SyntaxElement.NOT.resource );
		} else if ( node.hasProperty( (Property)SyntaxElement.INVERSE.resource ) ) {
		    op = SyntaxElement.INVERSE.getOperator();
		    stmt = node.getProperty( (Property)SyntaxElement.INVERSE.resource );
		} else if ( node.hasProperty( (Property)SyntaxElement.REFLEXIVE.resource ) ) {
		    op = SyntaxElement.REFLEXIVE.getOperator();
		    stmt = node.getProperty( (Property)SyntaxElement.REFLEXIVE.resource );
		} else if ( node.hasProperty( (Property)SyntaxElement.SYMMETRIC.resource ) ) {
		    op = SyntaxElement.SYMMETRIC.getOperator();
		    stmt = node.getProperty( (Property)SyntaxElement.SYMMETRIC.resource );
		} else if ( node.hasProperty( (Property)SyntaxElement.TRANSITIVE.resource ) ) {
		    op = SyntaxElement.TRANSITIVE.getOperator();
		    stmt = node.getProperty( (Property)SyntaxElement.TRANSITIVE.resource );
		} else {
		    throw new AlignmentException( "Relation statement must containt one constructor or Id : "+node );
		}
		Resource coll = stmt.getResource(); //JE2010MUSTCHECK
		if ( op == SyntaxElement.NOT.getOperator() ||
		     op == SyntaxElement.INVERSE.getOperator() || 
		     op == SyntaxElement.REFLEXIVE.getOperator() || 
		     op == SyntaxElement.SYMMETRIC.getOperator() || 
		     op == SyntaxElement.TRANSITIVE.getOperator() ) {
		    clexpr.add( parseRelation( coll ) );
		} else { // This is a first/rest statements
		    // THIS IS HORRIBLE BUT I DID NOT FOUND BETTER!
		    while ( !RDF.nil.getURI().equals( coll.getURI() ) ) {
			//JE2010MUSTCHECK
			clexpr.add( parseRelation( coll.getProperty( RDF.first ).getResource() ) );
			coll = coll.getProperty( RDF.rest ).getResource();
		    }
		}
		return new RelationConstruction( op, clexpr );
	    }
	} else if ( rdfType.equals( SyntaxElement.DOMAIN_RESTRICTION.resource ) ) {
	    stmt = node.getProperty( (Property)SyntaxElement.TOCLASS.resource );
	    if ( stmt == null ) throw new AlignmentException( "Required edoal:toClass property" );
	    RDFNode nn = stmt.getObject();
	    if ( nn.isResource() ) {
		return new RelationDomainRestriction( parseClass( (Resource)nn ) );
	    } else {
		throw new AlignmentException( "Incorrect class expression "+nn );
	    } 
	} else if ( rdfType.equals( SyntaxElement.CODOMAIN_RESTRICTION.resource ) ) {
	    stmt = node.getProperty( (Property)SyntaxElement.TOCLASS.resource );
	    if ( stmt == null ) throw new AlignmentException( "Required edoal:toClass property" );
	    RDFNode nn = stmt.getObject();
	    if ( nn.isResource() ) {
		return new RelationCoDomainRestriction( parseClass( (Resource)nn ) );
	    } else {
		throw new AlignmentException( "Incorrect class expression "+nn );
	    } 
	} else {
	    throw new AlignmentException("There is no pasrser for entity "+rdfType.getLocalName());
	}
    }

    static InstanceExpression parseInstance( final Resource node ) throws AlignmentException {
	Resource rdfType = node.getProperty(RDF.type).getResource();
	if ( rdfType.equals( SyntaxElement.INSTANCE_EXPR.resource ) ) {
	    URI id = getNodeId( node );
	    if ( id != null ) return new InstanceId( id );
	    else throw new AlignmentException("Cannot parse anonymous individual");
	} else {
	    throw new AlignmentException("There is no pasrser for entity "+rdfType.getLocalName());
	}
    }

    static Value parseValue( final Resource node ) throws AlignmentException {
	return null;
    }

    static URI getNodeId( final Resource node ) throws AlignmentException {
	final String idS = node.getURI();
	if ((idS != null) && (idS.length() > 0)) {
	    try {
		return new URI(idS);
	    } catch ( URISyntaxException usex ) {
		throw new AlignmentException( "Incorrect URI: "+idS );
	    }
	} else {
	    return null;
	}
    }

    /**
     * Parses a given annotaion in the the given node.
     * 
     * @param node
     *            which is the parent of the annotation node
     * @param e
     *            the tag which contains the annotation.
     * @return the parsed annotation, with the id set to the element and the
     *         value set to the text of the parsed node, or null, if nothing
     *         could be found
     * @throws NullPointerException
     *             if the node or the element is null
     */
    static void parseAnnotation(final Statement stmt, EDOALAlignment al ) throws AlignmentException {
	try {
	    final String anno = stmt.getString();
	    if ((anno != null) && (anno.length() > 0)) {
		URI uri = new URI ( stmt.getPredicate().getURI() );
		String name = uri.getFragment();
		String prefix = uri.getScheme()+":"+uri.getSchemeSpecificPart();
		if ( name == null ) {
		    int pos = prefix.lastIndexOf('/');
		    name = prefix.substring( pos+1 );
		    prefix = prefix.substring( 0, pos+1 );
		} else { prefix += "#"; }
		// JE: this will not work for stuff like dc:creator which has no fragment!
		al.setExtension( prefix, name, anno );
	    }
	} catch (Exception e1) {
	    // JE2009: Would be better to silently ignore annotations
	    // Or report them in a bunch
	    throw new AlignmentException("The annotation is not correct", e1);
	}
    }
    

}
