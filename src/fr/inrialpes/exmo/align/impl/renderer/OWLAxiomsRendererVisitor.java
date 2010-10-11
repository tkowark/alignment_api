/*
 * $Id$
 *
 * Copyright (C) INRIA, 2003-2004, 2007-2010
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

package fr.inrialpes.exmo.align.impl.renderer; 

import java.util.Enumeration;
import java.util.Properties;
import java.io.PrintWriter;
import java.net.URI;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import org.semanticweb.owl.align.Visitable;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.Relation;

import fr.inrialpes.exmo.align.impl.Annotations;
import fr.inrialpes.exmo.align.impl.Namespace;
import fr.inrialpes.exmo.align.impl.Extensions;
import fr.inrialpes.exmo.align.impl.ObjectAlignment;
import fr.inrialpes.exmo.align.impl.BasicRelation;
import fr.inrialpes.exmo.align.impl.rel.*;

import fr.inrialpes.exmo.ontowrap.LoadedOntology;
import fr.inrialpes.exmo.ontowrap.OntowrapException;

import fr.inrialpes.exmo.align.parser.SyntaxElement;
import fr.inrialpes.exmo.align.parser.SyntaxElement.Constructor;

import fr.inrialpes.exmo.align.impl.edoal.Id;
import fr.inrialpes.exmo.align.impl.edoal.PathExpression;
import fr.inrialpes.exmo.align.impl.edoal.Expression;
import fr.inrialpes.exmo.align.impl.edoal.ClassExpression;
import fr.inrialpes.exmo.align.impl.edoal.ClassId;
import fr.inrialpes.exmo.align.impl.edoal.ClassConstruction;
import fr.inrialpes.exmo.align.impl.edoal.ClassRestriction;
import fr.inrialpes.exmo.align.impl.edoal.ClassTypeRestriction;
import fr.inrialpes.exmo.align.impl.edoal.ClassDomainRestriction;
import fr.inrialpes.exmo.align.impl.edoal.ClassValueRestriction;
import fr.inrialpes.exmo.align.impl.edoal.ClassOccurenceRestriction;
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

import fr.inrialpes.exmo.align.impl.edoal.Transformation;
import fr.inrialpes.exmo.align.impl.edoal.ValueExpression;
import fr.inrialpes.exmo.align.impl.edoal.Value;
import fr.inrialpes.exmo.align.impl.edoal.Apply;
import fr.inrialpes.exmo.align.impl.edoal.Datatype;
import fr.inrialpes.exmo.align.impl.edoal.Comparator;
import fr.inrialpes.exmo.align.impl.edoal.EDOALRelation;
import fr.inrialpes.exmo.align.impl.edoal.EDOALCell;
import fr.inrialpes.exmo.align.impl.edoal.EDOALAlignment;

/**
 * Renders an alignment as a new ontology merging these.
 *
 * @author Jérôme Euzenat
 * @version $Id$ 
 */

public class OWLAxiomsRendererVisitor extends IndentedRendererVisitor implements AlignmentVisitor {
    boolean heterogeneous = false;
    boolean edoal = false;
    Alignment alignment = null;
    LoadedOntology onto1 = null;
    LoadedOntology onto2 = null;
    Cell cell = null;
    Relation toProcess = null;

    private static Namespace DEF = Namespace.ALIGNMENT;
    
    public OWLAxiomsRendererVisitor( PrintWriter writer ){
	super( writer );
    }

    public void init( Properties p ) {
	if ( p.getProperty("heterogeneous") != null ) heterogeneous = true;
    };

    public void visit( Visitable o ) throws AlignmentException {
	if ( o instanceof Expression ) visit( (Expression)o );
	else if ( o instanceof ValueExpression ) visit( (ValueExpression)o );
	else if ( o instanceof Transformation ) visit( (Transformation)o );
	else if ( o instanceof Cell ) visit( (Cell)o );
	else if ( o instanceof Relation ) visit( (Relation)o );
	else if ( o instanceof Alignment ) visit( (Alignment)o );
	else throw new AlignmentException( "Cannot dispatch Expression "+o );
    }

    public void visit( Alignment align ) throws AlignmentException {
	alignment = align;
	if ( align instanceof ObjectAlignment ){
	    onto1 = (LoadedOntology)((ObjectAlignment)alignment).getOntologyObject1();
	    onto2 = (LoadedOntology)((ObjectAlignment)alignment).getOntologyObject2();
	} else if ( align instanceof EDOALAlignment ) {
	    edoal = true;
	} else {
	    throw new AlignmentException("OWLAxiomsRenderer: cannot render simple alignment. Turn them into ObjectAlignment, by toObjectAlignement() or use EDOALAlignment");
	}
	writer.print("<rdf:RDF"+NL);
	writer.print("    xmlns:owl=\"http://www.w3.org/2002/07/owl#\""+NL);
	writer.print("    xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\""+NL);
	writer.print("    xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\" "+NL);
	writer.print("    xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\">"+NL+NL);	
	writer.print("  <owl:Ontology rdf:about=\"\">"+NL);
	writer.print("    <rdfs:comment>Matched ontologies</rdfs:comment>"+NL);
	writer.print("    <rdfs:comment>Generated by fr.inrialpes.exmo.align.renderer.OWLAxiomsRendererVisitor</rdfs:comment>"+NL);
	for ( String[] ext : align.getExtensions() ){
	    writer.print("    <rdfs:comment>"+ext[1]+": "+ext[2]+"</rdfs:comment>"+NL);
	}
	writer.print("    <owl:imports rdf:resource=\""+align.getOntology1URI().toString()+"\"/>"+NL);
	writer.print("    <owl:imports rdf:resource=\""+align.getOntology2URI().toString()+"\"/>"+NL);
	writer.print("  </owl:Ontology>"+NL+NL);
	
	try {
	    for( Cell c : align ){
		Object ob1 = c.getObject1();
		Object ob2 = c.getObject2();
		
		if ( heterogeneous || edoal ||
		     ( onto1.isClass( ob1 ) && onto2.isClass( ob2 ) ) ||
		     ( onto1.isDataProperty( ob1 ) && onto2.isDataProperty( ob2 ) ) ||
		     ( onto1.isObjectProperty( ob1 ) && onto2.isObjectProperty( ob2 ) ) ||
		     ( onto1.isIndividual( ob1 ) && onto2.isIndividual( ob2 ) ) ) {
		    c.accept( this );
		}
	    } //end for
	} catch ( OntowrapException owex ) {
	    throw new AlignmentException( "Error accessing ontology", owex );
	}

	writer.print("</rdf:RDF>"+NL);
    }

    public void visit( Cell cell ) throws AlignmentException {
	if ( cell.getId() != null ) writer.print(NL+NL+"<!-- "+cell.getId()+" -->"+NL);
	if ( cell instanceof EDOALCell ) {
	    visit( (EDOALCell)cell );
	} else {
	    this.cell = cell;
	    Object ob1 = cell.getObject1();
	    Object ob2 = cell.getObject2();
	    URI u1;
	    try {
		if ( cell.getRelation() instanceof SubsumedRelation ){
		    u1 = onto2.getEntityURI( cell.getObject2() );
		} else {
		    u1 = onto1.getEntityURI( ob1 );
		}
		if ( ob1 instanceof ClassExpression || onto1.isClass( ob1 ) ) {
		    writer.print("  <owl:Class rdf:about=\""+u1+"\">"+NL);
		    cell.getRelation().accept( this );
		    writer.print("  </owl:Class>"+NL);
		} else if ( ob1 instanceof PropertyExpression || onto1.isDataProperty( ob1 ) ) {
		    writer.print("  <owl:DatatypeProperty rdf:about=\""+u1+"\">"+NL);
		    cell.getRelation().accept( this );
		    writer.print("  </owl:DatatypeProperty>"+NL);
		} else if ( ob1 instanceof RelationExpression || onto1.isObjectProperty( ob1 ) ) {
		    writer.print("  <owl:ObjectProperty rdf:about=\""+u1+"\">"+NL);
		    cell.getRelation().accept( this );
		    writer.print("  </owl:ObjectProperty>"+NL);
		} else if ( ob1 instanceof InstanceExpression || onto1.isIndividual( ob1 ) ) {
		    writer.print("  <owl:Thing rdf:about=\""+u1+"\">"+NL);
		    cell.getRelation().accept( this );
		    writer.print("  </owl:Thing>"+NL);
		}
	    } catch ( OntowrapException owex ) {
		throw new AlignmentException( "Error accessing ontology", owex );
	    }
	}
    }

    public String getRelationName( LoadedOntology onto, Object ob, Relation rel ) {
	try {
	    if ( rel instanceof EquivRelation ) {
		if ( onto.isClass( ob ) ) {
		    return "owl:equivalentClass";
		} else if ( onto.isProperty( ob ) ) {
		    return "owl:equivalentProperty";
		} else if ( onto.isIndividual( ob ) ) {
		    return "owl:sameAs";
		}
	    } else if ( rel instanceof SubsumeRelation ) {
		if ( onto.isClass( ob ) ) {
		    return "rdfs:subClassOf";
		} else if ( onto.isProperty( ob ) ) {
		    return "rdfs:subPropertyOf";
		}
	    } else if ( rel instanceof SubsumedRelation ) {
		if ( onto.isClass( ob ) ) {
		    return "rdfs:subClassOf";
		} else if ( onto.isProperty( ob ) ) {
		    return "rdfs:subPropertyOf";
		}
	    } else if ( rel instanceof IncompatRelation ) {
		if ( onto.isClass( ob ) ) {
		    return "rdfs:disjointFrom";
		} else if ( onto.isIndividual( ob ) ) {
		    return "owl:differentFrom";
		}
	    }
	} catch ( OntowrapException owex ) {}; // return null anyway
	return null;
    }

    public void visit( EquivRelation rel ) throws AlignmentException {
	Object ob2 = cell.getObject2();
	String owlrel = getRelationName( onto2, ob2, rel );
	if ( owlrel == null ) throw new AlignmentException( "Cannot express relation "+rel );
	if ( !edoal ) {
	    try {
		writer.print("    <"+owlrel+" rdf:resource=\""+onto2.getEntityURI( ob2 )+"\"/>"+NL);
	    } catch ( OntowrapException owex ) {
		throw new AlignmentException( "Error accessing ontology", owex );
	    }
	} else {
	    if ( ob2 instanceof InstanceId ) {
		writer.print("    <"+owlrel+" rdf:resource=\""+((InstanceId)ob2).getURI()+"\"/>"+NL);
	    } else {
		writer.println("    <"+owlrel+">");
		((Expression)ob2).accept( this );
		writer.println("    </"+owlrel+">");
	    }
	}
    }

    public void visit( SubsumeRelation rel ) throws AlignmentException {
	Object ob2 = cell.getObject2();
	String owlrel = getRelationName( onto2, ob2, rel );
	if ( owlrel == null ) throw new AlignmentException( "Cannot express relation "+rel );
	if ( !edoal ) {
	try {
	    writer.print("    <"+owlrel+" rdf:resource=\""+onto2.getEntityURI( ob2 )+"\"/>"+NL);
	} catch ( OntowrapException owex ) {
	    throw new AlignmentException( "Error accessing ontology", owex );
	}
	} else {
	    writer.println("    <"+owlrel+">");
	    ((Expression)ob2).accept( this );
	    writer.println("    </"+owlrel+">");
	}
    }

    public void visit( SubsumedRelation rel ) throws AlignmentException {
	Object ob1 = cell.getObject1();
	String owlrel = getRelationName( onto1, ob1, rel );
	if ( owlrel == null ) throw new AlignmentException( "Cannot express relation "+rel );
	if ( !edoal ) {
	try {
	    writer.print("    <"+owlrel+" rdf:resource=\""+onto1.getEntityURI( ob1 )+"\"/>"+NL);
	} catch ( OntowrapException owex ) {
	    throw new AlignmentException( "Error accessing ontology", owex );
	}
	} else {
	    writer.println("    <"+owlrel+">");
	    ((Expression)ob1).accept( this );
	    writer.println("    </"+owlrel+">");
	}
    }

    public void visit( IncompatRelation rel ) throws AlignmentException {
	Object ob2 = cell.getObject2();
	String owlrel = getRelationName( onto2, ob2, rel );
	if ( owlrel == null ) throw new AlignmentException( "Cannot express relation "+rel );
	if ( !edoal ) {
	try {
	    writer.print("    <"+owlrel+" rdf:resource=\""+onto2.getEntityURI( ob2 )+"\"/>"+NL);
	} catch ( OntowrapException owex ) {
	    throw new AlignmentException( "Cannot find entity URI", owex );
	}
	} else {
	    writer.println("    <"+owlrel+">");
	    ((Expression)ob2).accept( this );
	    writer.println("    </"+owlrel+">");
	}
    }

    public void visit( Relation rel ) throws AlignmentException {
	if ( rel instanceof EDOALRelation ) visit( (EDOALRelation)rel );
	else {
	    // JE: I do not understand why I need this,
	    // but this seems to be the case...
	    try {
		Method mm = null;
		if ( Class.forName("fr.inrialpes.exmo.align.impl.rel.EquivRelation").isInstance(rel) ){
		    mm = this.getClass().getMethod("visit",
						   new Class [] {Class.forName("fr.inrialpes.exmo.align.impl.rel.EquivRelation")});
		} else if (Class.forName("fr.inrialpes.exmo.align.impl.rel.SubsumeRelation").isInstance(rel) ) {
		    mm = this.getClass().getMethod("visit",
						   new Class [] {Class.forName("fr.inrialpes.exmo.align.impl.rel.SubsumeRelation")});
		} else if (Class.forName("fr.inrialpes.exmo.align.impl.rel.SubsumedRelation").isInstance(rel) ) {
		    mm = this.getClass().getMethod("visit",
						   new Class [] {Class.forName("fr.inrialpes.exmo.align.impl.rel.SubsumedRelation")});
		} else if (Class.forName("fr.inrialpes.exmo.align.impl.rel.IncompatRelation").isInstance(rel) ) {
		    mm = this.getClass().getMethod("visit",
						   new Class [] {Class.forName("fr.inrialpes.exmo.align.impl.rel.IncompatRelation")});
		}
		if ( mm != null ) mm.invoke(this,new Object[] {rel});
		else {
		    if ( Class.forName("fr.inrialpes.exmo.align.impl.BasicRelation").isInstance(rel) ){
			try {
			    // This is only for individuals
			    Object ob2 = cell.getObject2();
			    if ( onto2.isIndividual( ob2 ) ) { 
				// ob1 has been checked before
				// It would be better to check that r is a relation of one of the ontologies by
				// onto1.isObjectProperty( onto1.getEntity( new URI ( r ) ) )
				String r = ((BasicRelation)rel).getRelation();
				if ( r!=null && !r.equals("") ) {
				    URI u2 = onto2.getEntityURI( ob2 );
				    writer.print("    <"+r+" rdf:resource=\""+u2+"\"/>"+NL);
				}
			    }
			} catch ( OntowrapException owex ) {
			    throw new AlignmentException( "Error accessing ontology", owex );
			}
		    }
		}
	    } catch (IllegalAccessException e) {
		e.printStackTrace();
	    } catch (ClassNotFoundException e) {
		e.printStackTrace();
	    } catch (NoSuchMethodException e) {
		e.printStackTrace();
	    } catch (InvocationTargetException e) { 
		e.printStackTrace();
	    }
	}
    };

    public void visit( EDOALCell cell ) throws AlignmentException {
	this.cell = cell;
	toProcess = cell.getRelation();
	if ( ((EDOALRelation)toProcess).getDirection() != EDOALRelation.Direction.SUBSUMES
	     && ((EDOALRelation)toProcess).getDirection() != EDOALRelation.Direction.HASINSTANCE ) {
	    ((Expression)cell.getObject1()).accept( this );
	} else {
	    ((Expression)cell.getObject2()).accept( this );
	}
	writer.print(NL);
    }

    /**
     * The current implementation is not satisfying:
     * EDOALRelation is deconnected from Relation (for historical purposes)
     * This is left this way because the complete relations should be reengineered
     */
    public void visit( EDOALRelation o ) throws AlignmentException {
	String relName;
	boolean reversed = false;
	Object ob2 = cell.getObject2();
	if ( o.getDirection() == EDOALRelation.Direction.EQUIVALENCE ) {
	    if ( ob2 instanceof ClassExpression ) {
		relName = "owl:equivalentClass";
	    } else if ( ob2 instanceof PropertyExpression || ob2 instanceof RelationExpression ) {
		relName = "owl:equivalentProperty";
	    } else if ( ob2 instanceof InstanceExpression ) {
		relName = "owl:sameAs";
	    } else throw new AlignmentException( "Equivalence relation cannot apply to "+o );
	} else if (  o.getDirection() == EDOALRelation.Direction.DISJOINTFROM ) {
	    if ( ob2 instanceof ClassExpression ) {
		relName = "owl:disjointFrom";
	    } else if ( ob2 instanceof InstanceExpression ) {
		relName = "owl:differentFrom";
	    } else throw new AlignmentException( "Disjointness relation cannot apply to "+o );
	} else if (  o.getDirection() == EDOALRelation.Direction.SUBSUMES ) {
	    reversed = true;
	    if ( ob2 instanceof ClassExpression ) {
		relName = "owl:subClassOf";
	    } else if ( ob2 instanceof PropertyExpression || ob2 instanceof RelationExpression ) {
		relName = "owl:subPropertyOf";
	    } else throw new AlignmentException( "Subsumption relation cannot apply to "+o );
	} else if (  o.getDirection() == EDOALRelation.Direction.SUBSUMEDBY ) {
	    if ( ob2 instanceof ClassExpression ) {
		relName = "owl:subClassOf";
	    } else if ( ob2 instanceof PropertyExpression || ob2 instanceof RelationExpression ) {
		relName = "owl:subPropertyOf";
	    } else throw new AlignmentException( "Subsumption relation cannot apply to "+o );
	} else if (  o.getDirection() == EDOALRelation.Direction.INSTANCEOF ) {
	    relName = "rdf:type";
	} else if (  o.getDirection() == EDOALRelation.Direction.HASINSTANCE ) {
	    reversed = true;
	    relName = "rdf:type";
	} else {
	    throw new AlignmentException( "Cannot deal with relation "+o );
	}
	writer.print("  <"+relName+">"+NL);
	increaseIndent();
	if ( reversed ) {
	    ((Expression)cell.getObject1()).accept( this );
	} else {
	    ((Expression)ob2).accept( this );
	}
	decreaseIndent();
	writer.print(NL+"  </"+relName+">");
    }

    // ******* EDOAL

    public void visit( Expression o ) throws AlignmentException {
	if ( o instanceof ClassExpression ) visit( (ClassExpression)o );
	else if ( o instanceof RelationRestriction ) visit( (RelationRestriction)o );
	else if ( o instanceof PropertyRestriction ) visit( (PropertyRestriction)o );
	else if ( o instanceof ClassRestriction ) visit( (ClassRestriction)o );
	else if ( o instanceof PathExpression ) visit( (PathExpression)o );
	else if ( o instanceof PropertyExpression ) visit( (PropertyExpression)o );
	else if ( o instanceof InstanceExpression ) visit( (InstanceExpression)o );
	else if ( o instanceof RelationExpression ) visit( (RelationExpression)o );
	else throw new AlignmentException( "Cannot dispatch Expression "+o );
    }

    public void visit( final PathExpression p ) throws AlignmentException {
	if ( p instanceof RelationExpression ) visit( (RelationExpression)p );
	else if ( p instanceof PropertyExpression ) visit( (PropertyExpression)p );
	else throw new AlignmentException( "Cannot dispatch PathExpression "+p );
    }

    public void visit( final ClassExpression e ) throws AlignmentException {
	if ( e instanceof ClassId ) visit( (ClassId)e );
	else if ( e instanceof ClassConstruction )  visit( (ClassConstruction)e );
	else if ( e instanceof ClassRestriction )  visit( (ClassRestriction)e );
	else throw new AlignmentException( "Cannot dispatch ClassExpression "+e );
    }

    public void visit( final ClassId e ) throws AlignmentException {
	if ( toProcess == null ) {
	    indentedOutput("<owl:Class "+SyntaxElement.RDF_ABOUT.print(DEF)+"=\""+e.getURI()+"\"/>");
	} else {
	    Relation toProcessNext = toProcess;
	    toProcess = null;
	    indentedOutput("<owl:Class "+SyntaxElement.RDF_ABOUT.print(DEF)+"=\""+e.getURI()+"\">"+NL);
	    increaseIndent();
	    toProcessNext.accept( this );
	    writer.print(NL);
	    decreaseIndent();
	    indentedOutput("</owl:Class>");
	}
    }

    public void visit( final ClassConstruction e ) throws AlignmentException {
	Relation toProcessNext = toProcess;
	toProcess = null;
	final Constructor op = e.getOperator();
	String owlop = null;
	// Very special treatment
	if ( toProcessNext != null && e.getComponents().size() == 0 ) {
	    if ( op == Constructor.AND ) owlop = "http://www.w3.org/2002/07/owl#Thing";
	    else if ( op == Constructor.OR ) owlop = "http://www.w3.org/2002/07/owl#Nothing";
	    else if ( op == Constructor.NOT ) throw new AlignmentException( "Complement constructor cannot be empty");
	    indentedOutput("<owl:Class "+SyntaxElement.RDF_ABOUT.print(DEF)+"=\""+owlop+"\">"+NL);
	    increaseIndent();
	    toProcessNext.accept( this ); 
	    writer.print(NL);
	    decreaseIndent();
	    indentedOutput("</owl:Class>");
	} else {
	    if ( op == Constructor.AND ) owlop = "intersectionOf";
	    else if ( op == Constructor.OR ) owlop = "unionOf";
	    else if ( op == Constructor.NOT ) owlop = "complementOf";
	    else throw new AlignmentException( "Unknown class constructor : "+op );
	    if ( e.getComponents().size() == 0 ) {
		if ( op == Constructor.AND ) indentedOutput("<owl:Thing/>");
		else if ( op == Constructor.OR ) indentedOutput("<owl:Nothing/>");
		else throw new AlignmentException( "Complement constructor cannot be empty");
	    } else {
		indentedOutput("<owl:Class>"+NL);
		increaseIndent();
		indentedOutput("<owl:"+owlop);
		if ( ( (op == Constructor.AND) || (op == Constructor.OR) ) ) 
		    writer.print(" "+SyntaxElement.RDF_PARSETYPE.print(DEF)+"=\"Collection\"");
		writer.print(">"+NL);
		increaseIndent();
		for (final ClassExpression ce : e.getComponents()) {
		    writer.print(linePrefix);
		    ce.accept( this );
		    writer.print(NL);
		}
		decreaseIndent();
		indentedOutput("</owl:"+owlop+">"+NL);
		if ( toProcessNext != null ) { toProcessNext.accept( this ); writer.print(NL); }
		decreaseIndent();
		indentedOutput("</owl:Class>");
	    }
	}
    }

    public void visit(final ClassRestriction e) throws AlignmentException {
	if ( e instanceof ClassValueRestriction ) visit( (ClassValueRestriction)e );
	else if ( e instanceof ClassTypeRestriction )  visit( (ClassTypeRestriction)e );
	else if ( e instanceof ClassDomainRestriction )  visit( (ClassDomainRestriction)e );
	else if ( e instanceof ClassOccurenceRestriction )  visit( (ClassOccurenceRestriction)e );
	else throw new AlignmentException( "Cannot dispatch ClassExpression "+e );
    }

    public void visit( final ClassValueRestriction c ) throws AlignmentException {
	Relation toProcessNext = toProcess;
	toProcess = null;
	indentedOutput("<owl:Restriction>"+NL);
	increaseIndent();
	indentedOutput("<owl:onProperty>"+NL);
	increaseIndent();
	c.getRestrictionPath().accept( this );
	decreaseIndent();
	writer.print(NL);
	indentedOutputln("</owl:onProperty>");
	ValueExpression ve = c.getValue();
	if ( ve instanceof Value ) {
	    indentedOutput("<owl:hasValue");
	    if ( ((Value)ve).getType() != null ) {
		writer.print( " rdf:datatype=\""+((Value)ve).getType()+"\"" );
	    }
	    writer.print( ">"+((Value)ve).getValue()+"</owl:hasValue>"+NL);
	} else if ( ve instanceof InstanceId ) {
	    indentedOutput("<owl:hasValue>"+NL);
	    increaseIndent();
	    ve.accept( this );
	    decreaseIndent();
	    writer.print(NL);
	    indentedOutput("</owl:hasValue>"+NL);
	} else throw new AlignmentException( "OWL does not support path constraints in hasValue : "+ve );
	if ( toProcessNext != null ) { toProcessNext.accept( this ); writer.print(NL); }
	decreaseIndent();
	indentedOutput("</owl:Restriction>");
    }

    public void visit( final ClassTypeRestriction c ) throws AlignmentException {
	Relation toProcessNext = toProcess;
	toProcess = null;
	indentedOutput("<owl:Restriction>"+NL);
	increaseIndent();
	indentedOutput("<owl:onProperty>"+NL);
	increaseIndent();
	c.getRestrictionPath().accept( this );
	writer.print(NL);
	decreaseIndent();
	indentedOutput("</owl:onProperty>"+NL);
	indentedOutput("<owl:allValuesFrom>"+NL);
	increaseIndent();
	visit( c.getType() ); // JE2010 ??
	writer.print(NL);
	decreaseIndent();
	indentedOutput("</owl:allValuesFrom>"+NL);
	if ( toProcessNext != null ) { toProcessNext.accept( this ); writer.print(NL); }
	decreaseIndent();
	indentedOutput("</owl:Restriction>");
    }

    public void visit( final ClassDomainRestriction c ) throws AlignmentException {
	Relation toProcessNext = toProcess;
	toProcess = null;
	indentedOutput("<owl:Restriction>"+NL);
	increaseIndent();
	indentedOutput("<owl:onProperty>"+NL);
	increaseIndent();
	c.getRestrictionPath().accept( this );
	writer.print(NL);
	decreaseIndent();
	indentedOutput("</owl:onProperty>"+NL);
	if ( c.isUniversal() ) {
	    indentedOutput("<owl:allValuesFrom>"+NL);
	} else {
	    indentedOutput("<owl:someValuesFrom>"+NL);
	}
	increaseIndent();
	c.getDomain().accept( this );
	writer.print(NL);
	decreaseIndent();
	if ( c.isUniversal() ) {
	    indentedOutput("</owl:allValuesFrom>"+NL);
	} else {
	    indentedOutput("</owl:someValuesFrom>"+NL);
	}
	if ( toProcessNext != null ) { toProcessNext.accept( this ); writer.print(NL); }
	decreaseIndent();
	indentedOutput("</owl:Restriction>");
    }

    // TOTEST
    public void visit( final ClassOccurenceRestriction c ) throws AlignmentException {
	Relation toProcessNext = toProcess;
	toProcess = null;
	indentedOutput("<owl:Restriction>"+NL);
	increaseIndent();
	indentedOutput("<owl:onProperty>"+NL);
	increaseIndent();
	c.getRestrictionPath().accept( this );
	writer.print(NL);
	decreaseIndent();
	indentedOutput("</owl:onProperty>"+NL);
	String cardinality = null;
	Comparator comp = c.getComparator();
	if ( comp == Comparator.EQUAL ) cardinality = "cardinality";
	else if ( comp == Comparator.LOWER ) cardinality = "maxCardinality";
	else if ( comp == Comparator.GREATER ) cardinality = "minCardinality";
	else throw new AlignmentException( "Unknown comparator : "+comp.getURI() );
	indentedOutput("<owl:"+cardinality+" rdf:datatype=\"&xsd;nonNegativeInteger\">");
	writer.print(c.getOccurence());
	writer.print("</owl:"+cardinality+">"+NL);
	if ( toProcessNext != null ) { toProcessNext.accept( this ); writer.print(NL); }
	decreaseIndent();
	indentedOutput("</owl:Restriction>");
    }
    
    public void visit(final PropertyExpression e) throws AlignmentException {
	if ( e instanceof PropertyId ) visit( (PropertyId)e );
	else if ( e instanceof PropertyConstruction ) visit( (PropertyConstruction)e );
	else if ( e instanceof PropertyRestriction ) visit( (PropertyRestriction)e );
	else throw new AlignmentException( "Cannot dispatch ClassExpression "+e );
    }
	
    public void visit(final PropertyId e) throws AlignmentException {
	if ( toProcess == null ) {
	    indentedOutput("<owl:DatatypeProperty "+SyntaxElement.RDF_ABOUT.print(DEF)+"=\""+e.getURI()+"\"/>");
	} else {
	    Relation toProcessNext = toProcess;
	    toProcess = null;
	    indentedOutput("<owl:DatatypeProperty "+SyntaxElement.RDF_ABOUT.print(DEF)+"=\""+e.getURI()+"\">"+NL);
	    increaseIndent();
	    toProcessNext.accept( this );
	    writer.print(NL);
	    decreaseIndent();
	    indentedOutput("</owl:DatatypeProperty>");
	}
    }

    /**
     * OWL, and in particular OWL 2, does not allow for more Relation (ObjectProperty)
     * and Property (DataProperty) constructor than owl:inverseOf
     * It is thus imposible to transcribe our and, or and not constructors.
     */
    public void visit(final PropertyConstruction e) throws AlignmentException {
	Relation toProcessNext = toProcess;
	toProcess = null;
	indentedOutput("<owl:DatatypePropety>"+NL);
	increaseIndent();
	final Constructor op = e.getOperator();
	String owlop = null;
	// JE: FOR TESTING
	//owlop = "FORTESTING("+op.name()+")";
	if ( owlop == null ) throw new AlignmentException( "Cannot translate property construction in OWL : "+op );
	indentedOutput("<owl:"+owlop);
	if ( (op == Constructor.AND) || (op == Constructor.OR) || (op == Constructor.COMP) ) writer.print(" "+SyntaxElement.RDF_PARSETYPE.print(DEF)+"=\"Collection\"");
	writer.print(">"+NL);
	increaseIndent();
	if ( (op == Constructor.AND) || (op == Constructor.OR) || (op == Constructor.COMP) ) {
	    for ( final PathExpression pe : e.getComponents() ) {
		writer.print(linePrefix);
		pe.accept( this );
		writer.print(NL);
	    }
	} else {
	    for (final PathExpression pe : e.getComponents()) {
		pe.accept( this );
		writer.print(NL);
	    }
	}
	decreaseIndent();
	indentedOutput("</owl:"+owlop+">"+NL);
	if ( toProcessNext != null ) { toProcessNext.accept( this ); writer.print(NL); }
	decreaseIndent();
	indentedOutput("</owl:DatatypePropety>");
    }
    
    public void visit(final PropertyRestriction e) throws AlignmentException {
	if ( e instanceof PropertyValueRestriction ) visit( (PropertyValueRestriction)e );
	else if ( e instanceof PropertyDomainRestriction ) visit( (PropertyDomainRestriction)e );
	else if ( e instanceof PropertyTypeRestriction ) visit( (PropertyTypeRestriction)e );
	else throw new AlignmentException( "Cannot dispatch ClassExpression "+e );
    }
	
    public void visit(final PropertyValueRestriction c) throws AlignmentException {
	Relation toProcessNext = toProcess;
	toProcess = null;
	indentedOutput("<owl:DatatypeProperty>"+NL);
	increaseIndent();
	indentedOutput("<rdfs:range>"+NL);
	increaseIndent();
	indentedOutput("<rdfs:Datatype>"+NL);
	increaseIndent();
	indentedOutput("<owl:oneOf>"+NL);
	increaseIndent();
	// In EDOAL, this does only contain one value and is thus rendered as:
	indentedOutput("<rdf:Description>"+NL);
	increaseIndent();
	ValueExpression ve = c.getValue();
	if ( ve instanceof Value ) {
	    indentedOutput("<rdf:first");
	    if ( ((Value)ve).getType() != null ) {
		writer.print( " rdf:datatype=\""+((Value)ve).getType()+"\"" );
	    }
	    writer.print( ">"+((Value)ve).getValue()+"</rdf:first>"+NL);
	} else {
	    indentedOutput("<rdf:first>"+NL);
	    ve.accept( this );
	    writer.print("</rdf:first>"+NL);
	    indentedOutput("<rdf:rest rdf:resource=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#nil\"/>"+NL);
	}
	decreaseIndent();
	indentedOutput("</rdf:Description>"+NL);
	// This is incorrect for more than one value... see the OWL:
	/*
         <rdfs:Datatype>
          <owl:oneOf>
           <rdf:Description>
            <rdf:first rdf:datatype="&xsd;integer">1</rdf:first>
             <rdf:rest>
              <rdf:Description>
               <rdf:first rdf:datatype="&xsd;integer">2</rdf:first>
               <rdf:rest rdf:resource="http://www.w3.org/1999/02/22-rdf-syntax-ns#nil"/>
              </rdf:Description>
             </rdf:rest>
            </rdf:Description>
           </owl:oneOf>
          </rdfs:Datatype>
	*/
	decreaseIndent();
	indentedOutput("</owl:oneOf>"+NL);
	decreaseIndent();
	indentedOutput("</rdfs:Datatype>"+NL);
	decreaseIndent();
	indentedOutput("</rdfs:range>"+NL);
	if ( toProcessNext != null ) { toProcessNext.accept( this ); writer.print(NL); }
	decreaseIndent();
	indentedOutput("</owl:DatatypeProperty>");
    }

    public void visit(final PropertyDomainRestriction c) throws AlignmentException {
	Relation toProcessNext = toProcess;
	toProcess = null;
	indentedOutput("<owl:DatatypeProperty>"+NL);
	increaseIndent();
	indentedOutput("<rdfs:domain>"+NL);
	increaseIndent();
	c.getDomain().accept( this );
	writer.print(NL);
	decreaseIndent();
	indentedOutput("</rdfs:domain>"+NL);
	if ( toProcessNext != null ) { toProcessNext.accept( this ); writer.print(NL); }
	decreaseIndent();
	indentedOutput("</owl:DatatypeProperty>");
    }

    public void visit(final PropertyTypeRestriction c) throws AlignmentException {
	Relation toProcessNext = toProcess;
	toProcess = null;
	indentedOutput("<owl:DatatypeProperty>"+NL);
	increaseIndent();
	indentedOutput("<rdfs:range>"+NL);
	increaseIndent();
	c.getType().accept( this );
	decreaseIndent();
	indentedOutput("</rdfs:range>"+NL);
	if ( toProcessNext != null ) { toProcessNext.accept( this ); writer.print(NL); }
	decreaseIndent();
	indentedOutput("</owl:DatatypeProperty>");
    }
    
    public void visit( final RelationExpression e ) throws AlignmentException {
	if ( e instanceof RelationId ) visit( (RelationId)e );
	else if ( e instanceof RelationRestriction ) visit( (RelationRestriction)e );
	else if ( e instanceof RelationConstruction ) visit( (RelationConstruction)e );
	else throw new AlignmentException( "Cannot dispatch ClassExpression "+e );
    }
	
    public void visit( final RelationId e ) throws AlignmentException {
	if ( toProcess == null ) {
	    indentedOutput("<owl:ObjectProperty "+SyntaxElement.RDF_ABOUT.print(DEF)+"=\""+e.getURI()+"\"/>");
	} else {
	    Relation toProcessNext = toProcess;
	    toProcess = null;
	    indentedOutput("<owl:ObjectProperty "+SyntaxElement.RDF_ABOUT.print(DEF)+"=\""+e.getURI()+"\">"+NL);
	    increaseIndent();
	    toProcessNext.accept( this );
	    writer.print(NL);
	    decreaseIndent();
	    indentedOutput("</owl:ObjectProperty>");
	}
    }

    /**
     * OWL, and in particular OWL 2, does not allow for more Relation (ObjectProperty)
     * and Property (DataProperty) constructor than owl:inverseOf
     * It is thus imposible to transcribe our and, or and not constructors.
     * Moreover, they have no constructor for the symmetric, transitive and reflexive
     * closure and the compositional closure (or composition) can only be obtained by
     * defining a property subsumed by this closure through an axiom.
     * It is also possible to rewrite the reflexive closures as axioms as well.
     * But the transitive closure can only be obtained through subsuption.
     */
    public void visit( final RelationConstruction e ) throws AlignmentException {
	Relation toProcessNext = toProcess;
	toProcess = null;
	indentedOutput("<owl:ObjectProperty>"+NL);
	increaseIndent();
	final Constructor op = e.getOperator();
	String owlop = null;
	if ( op == Constructor.INVERSE ) owlop = "owl:inverseOf";
	// JE: FOR TESTING
	//owlop = "FORTESTING("+op.name()+")";
	if ( owlop == null ) throw new AlignmentException( "Cannot translate relation construction in OWL : "+op );
	indentedOutput("<owl:"+owlop);
	if ( (op == Constructor.OR) || (op == Constructor.AND) || (op == Constructor.COMP) ) writer.print(" "+SyntaxElement.RDF_PARSETYPE.print(DEF)+"=\"Collection\"");
	writer.print(">"+NL);
	increaseIndent();
	if ( (op == Constructor.AND) || (op == Constructor.OR) || (op == Constructor.COMP) ) {
	    for (final PathExpression re : e.getComponents()) {
		writer.print(linePrefix);
		re.accept( this );
		writer.print(NL);
	    }
	} else { // NOT... or else: enumerate them
	    for (final PathExpression re : e.getComponents()) {
		re.accept( this );
		writer.print(NL);
	    }
	}
	decreaseIndent();
	indentedOutput("</owl:"+owlop+">"+NL);
	if ( toProcessNext != null ) { toProcessNext.accept( this ); writer.print(NL); }
	decreaseIndent();
	indentedOutput("</owl:ObjectProperty>");
    }
    
    public void visit( final RelationRestriction e ) throws AlignmentException {
	if ( e instanceof RelationCoDomainRestriction ) visit( (RelationCoDomainRestriction)e );
	else if ( e instanceof RelationDomainRestriction ) visit( (RelationDomainRestriction)e );
	else throw new AlignmentException( "Cannot dispatch ClassExpression "+e );
    }
	
    public void visit(final RelationCoDomainRestriction c) throws AlignmentException {
	Relation toProcessNext = toProcess;
	toProcess = null;
	indentedOutput("<owl:ObjectProperty>"+NL);
	increaseIndent();
	indentedOutput("<rdfs:range>"+NL);
	increaseIndent();
	c.getCoDomain().accept( this );
	writer.print(NL);
	decreaseIndent();
	indentedOutput("</rdfs:range>"+NL);
	if ( toProcessNext != null ) { toProcessNext.accept( this ); writer.print(NL); }
	decreaseIndent();
	indentedOutput("</owl:ObjectProperty>");
    }

    public void visit(final RelationDomainRestriction c) throws AlignmentException {
	Relation toProcessNext = toProcess;
	toProcess = null;
	indentedOutput("<owl:ObjectProperty>"+NL);
	increaseIndent();
	indentedOutput("<rdfs:domain>"+NL);
	increaseIndent();
	c.getDomain().accept( this );
	writer.print(NL);
	decreaseIndent();
	indentedOutput("</rdfs:domain>"+NL);
	if ( toProcessNext != null ) { toProcessNext.accept( this ); writer.print(NL); }
	decreaseIndent();
	indentedOutput("</owl:ObjectProperty>");
    }
    
    public void visit( final InstanceExpression e ) throws AlignmentException {
	if ( e instanceof InstanceId ) visit( (InstanceId)e );
	else throw new AlignmentException( "Cannot handle InstanceExpression "+e );
    }

    public void visit( final InstanceId e ) throws AlignmentException {
	if ( toProcess == null ) {
	    indentedOutput("<owl:Individual "+SyntaxElement.RDF_ABOUT.print(DEF)+"=\""+e.getURI()+"\"/>");
	} else {
	    Relation toProcessNext = toProcess;
	    toProcess = null;
	    indentedOutput("<owl:Individual "+SyntaxElement.RDF_ABOUT.print(DEF)+"=\""+e.getURI()+"\">"+NL);
	    increaseIndent();
	    toProcessNext.accept( this );
	    writer.print(NL);
	    decreaseIndent();
	    indentedOutput("</owl:Individual>");
	}
    }
    
    public void visit( final ValueExpression e ) throws AlignmentException {
	if ( e instanceof InstanceExpression ) visit( (InstanceExpression)e );
	else if ( e instanceof PathExpression )  visit( (PathExpression)e );
	else if ( e instanceof Apply )  visit( (Apply)e );
	else if ( e instanceof Value )  visit( (Value)e );
	else throw new AlignmentException( "Cannot dispatch ClassExpression "+e );
    }

    // Unused: see ClassValueRestriction above
    public void visit( final Value e ) throws AlignmentException {
    }
	
    // OWL does not allow for function calls
    public void visit( final Apply e ) throws AlignmentException {
	throw new AlignmentException( "Cannot render function call in OWL "+e );
    }

    // Not implemented. We only ignore transformations in OWL
    public void visit( final Transformation transf ) throws AlignmentException {
    }

    /**
     * Our Datatypes are only strings identifying datatypes.
     * For OWL, they should be considered as built-in types because we do 
     * not know how to add other types.
     * Hence we could simply have used a rdfs:Datatype="<name>"
     *
     * OWL offers further possiblities, such as additional owl:withRestriction
     * clauses
     */
    public void visit( final Datatype e ) {
	indentedOutput("<owl:Datatype><owl:onDataType rdf:resource=\""+e.plainText()+"\"/></owl:Datatype>");
    }
	

}
