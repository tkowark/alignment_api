/*
 * $Id$
 *
 * Copyright (C) INRIA, 2012
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
import java.util.Hashtable;
import java.util.Set;
import java.util.HashSet;
import java.util.Properties;
import java.io.PrintWriter;
import java.net.URI;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.Relation;

import fr.inrialpes.exmo.align.impl.Annotations;
import fr.inrialpes.exmo.align.impl.Namespace;
import fr.inrialpes.exmo.align.impl.Extensions;
import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.ObjectCell;

import fr.inrialpes.exmo.ontowrap.LoadedOntology;
import fr.inrialpes.exmo.ontowrap.Ontology; //?

import fr.inrialpes.exmo.align.parser.SyntaxElement;
import fr.inrialpes.exmo.align.parser.SyntaxElement.Constructor;

import fr.inrialpes.exmo.align.impl.edoal.Id;
import fr.inrialpes.exmo.align.impl.edoal.PathExpression;
import fr.inrialpes.exmo.align.impl.edoal.Expression;
import fr.inrialpes.exmo.align.impl.edoal.ClassExpression;
import fr.inrialpes.exmo.align.impl.edoal.ClassId;
import fr.inrialpes.exmo.align.impl.edoal.ClassConstruction;
import fr.inrialpes.exmo.align.impl.edoal.ClassTypeRestriction;
import fr.inrialpes.exmo.align.impl.edoal.ClassDomainRestriction;
import fr.inrialpes.exmo.align.impl.edoal.ClassValueRestriction;
import fr.inrialpes.exmo.align.impl.edoal.ClassOccurenceRestriction;
import fr.inrialpes.exmo.align.impl.edoal.PropertyExpression;
import fr.inrialpes.exmo.align.impl.edoal.PropertyId;
import fr.inrialpes.exmo.align.impl.edoal.PropertyConstruction;
import fr.inrialpes.exmo.align.impl.edoal.PropertyDomainRestriction;
import fr.inrialpes.exmo.align.impl.edoal.PropertyTypeRestriction;
import fr.inrialpes.exmo.align.impl.edoal.PropertyValueRestriction;
import fr.inrialpes.exmo.align.impl.edoal.RelationExpression;
import fr.inrialpes.exmo.align.impl.edoal.RelationId;
import fr.inrialpes.exmo.align.impl.edoal.RelationConstruction;
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
import fr.inrialpes.exmo.align.impl.edoal.EDOALCell;
import fr.inrialpes.exmo.align.impl.edoal.EDOALVisitor;

/**
 * Renders an alignment in JSON (and practically in JSON-LD)
 * http://json-ld.org/spec/latest/json-ld-syntax/
 *
 * application/json <========= media type available
 *
 * @author Jérôme Euzenat
 * @version $Id$
 */

// TODO: JE2012
// - check "," in enumerations (not enough) and in structure (too many)
// - add test PropertyTypeRestriction
// - check namespace at the begining

public class JSONRendererVisitor extends IndentedRendererVisitor implements AlignmentVisitor,EDOALVisitor {

    Alignment alignment = null;
    Cell cell = null;
    Hashtable<String,String> nslist = null;
    boolean embedded = false; // if the output is XML embeded in a structure

    // We do not want a default namespace here
    private static Namespace DEF = Namespace.NONE;
    
    private boolean isPattern = false;
	
    public JSONRendererVisitor( PrintWriter writer ){
	super( writer );
	//NL = System.getProperty("line.separator");
    }

    public void init( Properties p ) {
	if ( p.getProperty( "embedded" ) != null 
	     && !p.getProperty( "embedded" ).equals("") ) embedded = true;
	if ( p.getProperty( "indent" ) != null )
	    INDENT = p.getProperty( "indent" );
	if ( p.getProperty( "newline" ) != null )
	    NL = p.getProperty( "newline" );
    }

    public void visit( Alignment align ) throws AlignmentException {
	if ( subsumedInvocableMethod( this, align, Alignment.class ) ) return;
	// default behaviour
	String extensionString = "";
	alignment = align;
	nslist = new Hashtable<String,String>();
        nslist.put( Namespace.ALIGNMENT.prefix , Namespace.ALIGNMENT.shortCut );
        nslist.put( Namespace.RDF.prefix , Namespace.RDF.shortCut );
        nslist.put( Namespace.XSD.prefix , Namespace.XSD.shortCut );
	// how does it get there for RDF?
        nslist.put( Namespace.EDOAL.prefix , Namespace.EDOAL.shortCut );
	// Get the keys of the parameter
	int gen = 0;
	for ( String[] ext : align.getExtensions() ) {
	    String prefix = ext[0];
	    String name = ext[1];
	    String tag = nslist.get(prefix);
	    if ( tag == null ) {
		tag = "ns"+gen++;
		nslist.put( prefix, tag );
	    }
	    tag += ":"+name;
	    extensionString += INDENT+"\""+tag+"\" : \""+ext[2]+"\","+NL;
	}
	if ( align instanceof BasicAlignment ) {
	    for ( String label : ((BasicAlignment)align).getXNamespaces().stringPropertyNames() ) {
		if ( !label.equals("rdf") && !label.equals("xsd")
		     && !label.equals("<default>") )
		    extensionString += INDENT+"\""+label+"\" : \""+((BasicAlignment)align).getXNamespace( label )+"\","+NL;
	    }
	}
	writer.print("{ class : \""+align.getClass().getName()+"\","+NL );
	increaseIndent();
	indentedOutputln("@context : {");
	increaseIndent();
	for ( Enumeration e = nslist.keys() ; e.hasMoreElements(); ) {
	    String k = (String)e.nextElement();
	    indentedOutputln(nslist.get(k)+" : \""+k+"\",");
	}
	// Not sure that this is fully correct
	indentedOutputln("\""+SyntaxElement.MEASURE.print(DEF)+"\" : { \"@type\" : \"xsd:float\" },");
	decreaseIndent();
	indentedOutputln("},");
	indentedOutputln("@type : \""+SyntaxElement.ALIGNMENT.print(DEF)+"\"," ); //??
	String idext = align.getExtension( Namespace.ALIGNMENT.uri, Annotations.ID );
	if ( idext != null ) {
	    //indentedOutputln("\"rdf:about\" : \""+idext+"\",");
	    indentedOutputln("@id : \""+idext+"\",");
	}
	if ( alignment.getLevel().startsWith("2EDOALPattern") ) isPattern = true;
	indentedOutputln( "\""+SyntaxElement.LEVEL.print(DEF)+"\" : \""+align.getLevel()+"\",");
	indentedOutputln( "\""+SyntaxElement.TYPE.print(DEF)+"\" : \""+align.getType()+"\",");
	writer.print(extensionString);
	indentedOutputln( "\""+SyntaxElement.MAPPING_SOURCE.print(DEF)+"\" : " );
	increaseIndent();
	if ( align instanceof BasicAlignment ) {
	    printOntology( ((BasicAlignment)align).getOntologyObject1() );
	} else {
	    printBasicOntology( align.getOntology1URI(), align.getFile1() );
	}
	decreaseIndent();
	writer.print( ","+NL );
	indentedOutputln( "\""+SyntaxElement.MAPPING_TARGET.print(DEF)+"\" : " );
	increaseIndent();
	if ( align instanceof BasicAlignment ) {
	    printOntology( ((BasicAlignment)align).getOntologyObject2() );
	} else {
	    printBasicOntology( align.getOntology2URI(), align.getFile2() );
	}
	writer.print( ","+NL );
	decreaseIndent();
	indentedOutputln( "\""+SyntaxElement.MAP.print(DEF)+"\" : [" );
	increaseIndent();
	for( Cell c : align ){ 
	    c.accept( this ); 
	    writer.print( ","+NL );
	};
	decreaseIndent();
	indentedOutputln( "]" );
	decreaseIndent();
	indentedOutputln("}");
    }

    private void printBasicOntology ( URI u, URI f ) {
	indentedOutput("{ class : \""+SyntaxElement.ONTOLOGY.print(DEF)+"\","+NL);
	increaseIndent();
	//indentedOutput("\rdf:about\" : \""+u+"\","+NL);
	indentedOutput("@id : \""+u+"\","+NL);
	if ( f != null ) {
	    indentedOutput("\""+SyntaxElement.LOCATION.print(DEF)+"\" : \""+f+"\","+NL);
	} else {
	    indentedOutput("\""+SyntaxElement.LOCATION.print(DEF)+"\" : \""+u+"\","+NL);
	}
	decreaseIndent();
	indentedOutput("}");
    }

    public void printOntology( Ontology onto ) {
	URI u = onto.getURI();
	URI f = onto.getFile();
	indentedOutputln("{ class : \""+onto.getClass().getName()+"\",");
	increaseIndent();
	//indentedOutput("\"rdf:about\" : \""+u+"\","+NL);
	indentedOutput("@id : \""+u+"\","+NL);
	if ( f != null ) {
	    indentedOutput("\""+SyntaxElement.LOCATION.print(DEF)+"\" : \""+f+"\"");
	} else {
	    indentedOutput("\""+SyntaxElement.LOCATION.print(DEF)+"\" : \""+u+"\"");
	}
	if ( onto.getFormalism() != null ) {
	    writer.print(","+NL);
	    indentedOutputln("\""+SyntaxElement.FORMATT.print(DEF)+"\" : ");
	    increaseIndent();
	    indentedOutputln("{ class : \""+SyntaxElement.FORMALISM.print(DEF)+"\"," );
	    increaseIndent();
	    indentedOutputln("\""+SyntaxElement.NAME.print(DEF)+"\" : \""+onto.getFormalism()+"\",");
	    indentedOutputln("\""+SyntaxElement.URI.print()+"\" : \""+onto.getFormURI()+"\"");
	    decreaseIndent();
	    indentedOutputln("}");
	    decreaseIndent();
	}
	decreaseIndent();
	indentedOutput("}");
    }

    // TODO (EDOAL-PART)
    public void visit( Cell cell ) throws AlignmentException {
	if ( subsumedInvocableMethod( this, cell, Cell.class ) ) return;
	// default behaviour
	this.cell = cell;
	URI u1 = cell.getObject1AsURI(alignment);
	URI u2 = cell.getObject2AsURI(alignment);
	if ( ( u1 != null && u2 != null)
	     || alignment.getLevel().startsWith("2EDOAL") ){ //expensive test
	    indentedOutputln("{ class : \""+cell.getClass().getName()+"\",");
	    increaseIndent();
	    if ( cell.getId() != null && !cell.getId().equals("") ){
		//indentedOutputln("\"rdf:about\" : \""+cell.getId()+"\",");
		indentedOutputln("@id : \""+cell.getId()+"\",");
	    }
	    if ( alignment.getLevel().startsWith("2EDOAL") ) {
		indentedOutputln("\""+SyntaxElement.ENTITY1.print(DEF)+"\" : ");
		increaseIndent();
		((Expression)(cell.getObject1())).accept( this );
		decreaseIndent();
		writer.print(","+NL);
		indentedOutputln("\""+SyntaxElement.ENTITY2.print(DEF)+"\" : ");
		increaseIndent();
		((Expression)(cell.getObject2())).accept( this );
		decreaseIndent();
		writer.print(","+NL);
		if ( cell instanceof EDOALCell ) { // Here put the transf
		    Set<Transformation> transfs = ((EDOALCell)cell).transformations();
		    if ( transfs != null ) {
			for ( Transformation transf : transfs ){
			    indentedOutputln("\""+SyntaxElement.TRANSFORMATION.print(DEF)+"\" : ");
			    increaseIndent();
			    transf.accept( this );
			    decreaseIndent();
			    writer.print(NL);
			}
		    }
		}
	    } else {
		indentedOutputln("\""+SyntaxElement.ENTITY1.print(DEF)+"\" : \""+u1.toString()+"\",");
		indentedOutputln("\""+SyntaxElement.ENTITY2.print(DEF)+"\" : \""+u2.toString()+"\",");
	    }
	    indentedOutput("\""+SyntaxElement.RULE_RELATION.print(DEF)+"\" : \"");
	    cell.getRelation().accept( this );
	    writer.print("\","+NL);
	    indentedOutputln("\""+SyntaxElement.MEASURE.print(DEF)+"\" : \""+cell.getStrength()+"\",");
	    if ( cell.getSemantics() != null &&
		 !cell.getSemantics().equals("") &&
		 !cell.getSemantics().equals("first-order") )
		indentedOutputln("\""+SyntaxElement.SEMANTICS.print(DEF)+"\" : \""+cell.getSemantics()+"\",");
	    if ( cell.getExtensions() != null ) {
		for ( String[] ext : cell.getExtensions() ){
		    indentedOutputln(ext[1]+" : \""+ext[2]+"\","+NL);
		}
	    }
	    decreaseIndent();
	    indentedOutput("}");
	}
    }

    // DONE: could also be a qualified class name
    public void visit( Relation rel ) throws AlignmentException {
	if ( subsumedInvocableMethod( this, rel, Relation.class ) ) return;
	// default behaviour
	rel.write( writer );
    };

    // ********** EDOAL

    public void renderVariables( Expression expr ) {
	if ( expr.getVariable() != null ) {
	    indentedOutputln("\""+SyntaxElement.VAR.print(DEF)+"\" : \""+expr.getVariable().name()+"\",");
	}
    }

    public void visit( final ClassId e ) throws AlignmentException {
	indentedOutput("{ class : \""+SyntaxElement.CLASS_EXPR.print(DEF)+"\","+NL);
	increaseIndent();
	if ( e.getURI() != null ){
	    //indentedOutput("\rdf:about\" : \""+u+"\","+NL);
	    indentedOutput("@id : \""+e.getURI()+"\","+NL);
	}
	if ( isPattern ) renderVariables( e );
	decreaseIndent();
	indentedOutput("}");
    }

    public void visit( final ClassConstruction e ) throws AlignmentException {
	final Constructor op = e.getOperator();
	String sop = SyntaxElement.getElement( op ).print(DEF) ;
	indentedOutput("{ class : \""+SyntaxElement.CLASS_EXPR.print(DEF)+"\","+NL);
	increaseIndent();
	if ( isPattern ) renderVariables( e );
	indentedOutput("\""+sop+"\" : ["+NL);
	increaseIndent();
	for ( final ClassExpression ce : e.getComponents() ) {
	    writer.print(linePrefix);
	    ce.accept( this );
	    writer.print(NL);
	}
	decreaseIndent();
	indentedOutputln("]");
	decreaseIndent();
	indentedOutput("}");
    }

    public void visit( final ClassValueRestriction c ) throws AlignmentException {
	indentedOutput("{ class : \""+SyntaxElement.VALUE_COND.print(DEF)+"\","+NL);
	increaseIndent();
	if ( isPattern ) renderVariables( c );
	indentedOutput("\""+SyntaxElement.ONPROPERTY.print(DEF)+"\" : "+NL);
	increaseIndent();
	c.getRestrictionPath().accept( this );
	decreaseIndent();
	writer.print(","+NL);
	indentedOutput("\""+SyntaxElement.COMPARATOR.print(DEF)+"\" : \""+c.getComparator().getURI()+"\","+NL);
	indentedOutput("\""+SyntaxElement.VALUE.print(DEF)+"\" : "+NL);
	increaseIndent();
	c.getValue().accept( this );
	writer.print(NL);
	decreaseIndent();
	decreaseIndent();
	indentedOutput("}");
    }

    public void visit( final ClassTypeRestriction c ) throws AlignmentException {
	indentedOutput("{ class : \""+SyntaxElement.TYPE_COND.print(DEF)+"\","+NL);
	increaseIndent();
	if ( isPattern ) renderVariables( c );
	indentedOutput("\""+SyntaxElement.ONPROPERTY.print(DEF)+"\" : "+NL);
	increaseIndent();
	c.getRestrictionPath().accept( this );
	writer.print(","+NL);
	c.getType().accept( this );
	writer.print(NL);
	decreaseIndent();
	decreaseIndent();
	indentedOutput("}");
    }

    public void visit( final ClassDomainRestriction c ) throws AlignmentException {
	indentedOutput("{ class : \""+SyntaxElement.DOMAIN_RESTRICTION.print(DEF)+"\","+NL);
	increaseIndent();
	if ( isPattern ) renderVariables( c );
	indentedOutput("\""+SyntaxElement.ONPROPERTY.print(DEF)+"\" : "+NL);
	increaseIndent();
	c.getRestrictionPath().accept( this );
	decreaseIndent();
	writer.print(","+NL);
	if ( c.isUniversal() ) {
	    indentedOutput("\""+SyntaxElement.ALL.print(DEF)+"\" : "+NL);
	} else {
	    indentedOutput("\""+SyntaxElement.EXISTS.print(DEF)+"\" : "+NL);
	}
	increaseIndent();
	c.getDomain().accept( this );
	writer.print(NL);
	decreaseIndent();
	decreaseIndent();
	indentedOutput("}");
    }

    public void visit( final ClassOccurenceRestriction c ) throws AlignmentException {
	indentedOutput("{ class : \""+SyntaxElement.OCCURENCE_COND.print(DEF)+"\","+NL);
	increaseIndent();
	if ( isPattern ) renderVariables( c );
	indentedOutput("\""+SyntaxElement.ONPROPERTY.print(DEF)+"\" : "+NL);
	increaseIndent();
	c.getRestrictionPath().accept( this );
	decreaseIndent();
	writer.print(","+NL);
	indentedOutput("\""+SyntaxElement.COMPARATOR.print(DEF)+"\" : \""+c.getComparator().getURI()+"\","+NL);
	indentedOutput("\""+SyntaxElement.VALUE.print(DEF)+"\" : "+NL);
	increaseIndent();
	indentedOutputln("{ class : \""+SyntaxElement.LITERAL.print(DEF)+"\",");
	increaseIndent();
	indentedOutputln("\""+SyntaxElement.ETYPE.print(DEF)+"\" : \"xsd:int\",");
	indentedOutputln("\""+SyntaxElement.STRING.print(DEF)+"\" : \""+c.getOccurence()+"\"");
	decreaseIndent();
	indentedOutputln("}");
	decreaseIndent();
	decreaseIndent();
	indentedOutput("}");
    }
    
    public void visit(final PropertyId e) throws AlignmentException {
	indentedOutput("{ class : \""+SyntaxElement.PROPERTY_EXPR.print(DEF)+"\","+NL);
	increaseIndent();
	if ( e.getURI() != null ){
	    //indentedOutput("\rdf:about\" : \""+u+"\","+NL);
	    indentedOutput("@id : \""+e.getURI()+"\","+NL);
	}
	if ( isPattern ) renderVariables( e );
	decreaseIndent();
	indentedOutput("}");
    }

    public void visit(final PropertyConstruction e) throws AlignmentException {
	final Constructor op = e.getOperator();
	String sop = SyntaxElement.getElement( op ).print(DEF) ;
	indentedOutput("{ class : \""+SyntaxElement.PROPERTY_EXPR.print(DEF)+"\","+NL);
	increaseIndent();
	if ( isPattern ) renderVariables( e );
	indentedOutput("\""+sop+"\" : ["+NL);
	increaseIndent();
	for ( final PathExpression pe : e.getComponents() ) {
	    writer.print(linePrefix);
	    pe.accept( this );
	    writer.print(NL);
	}
	decreaseIndent();
	indentedOutputln("]");
	decreaseIndent();
	indentedOutput("}");
    }

    public void visit(final PropertyValueRestriction c) throws AlignmentException {
	indentedOutput("{ class : \""+SyntaxElement.PROPERTY_VALUE_COND.print(DEF)+"\","+NL);
	increaseIndent();
	if ( isPattern ) renderVariables( c );
	indentedOutput("\""+SyntaxElement.COMPARATOR.print(DEF)+"\" : \""+c.getComparator().getURI()+"\","+NL);
	indentedOutput("\""+SyntaxElement.VALUE.print(DEF)+"\" : "+NL);
	increaseIndent();
	c.getValue().accept( this );
	writer.print(NL);
	decreaseIndent();
	decreaseIndent();
	indentedOutput("}");
    }

    public void visit(final PropertyDomainRestriction c) throws AlignmentException {
	indentedOutput("{ class : \""+SyntaxElement.PROPERTY_DOMAIN_COND.print(DEF)+"\","+NL);
	increaseIndent();
	if ( isPattern ) renderVariables( c );
	indentedOutput("\""+SyntaxElement.TOCLASS.print(DEF)+"\" : "+NL);
	increaseIndent();
	c.getDomain().accept( this );
	writer.print(NL);
	decreaseIndent();
	decreaseIndent();
	indentedOutput("}");
    }

    public void visit(final PropertyTypeRestriction c) throws AlignmentException {
	indentedOutput("{ class : \""+SyntaxElement.PROPERTY_TYPE_COND.print(DEF)+"\","+NL);
	increaseIndent();
	if ( isPattern ) renderVariables( c );
	c.getType().accept( this );
	writer.print(NL);
	decreaseIndent();
	indentedOutput("}");
    }
    
    public void visit( final RelationId e ) throws AlignmentException {
	indentedOutput("{ class : \""+SyntaxElement.RELATION_EXPR.print(DEF)+"\","+NL);
	increaseIndent();
	if ( e.getURI() != null ){
	    //indentedOutput("\rdf:about\" : \""+u+"\","+NL);
	    indentedOutput("@id : \""+e.getURI()+"\","+NL);
	}
	if ( isPattern ) renderVariables( e );
	decreaseIndent();
	indentedOutput("}");
    }

    public void visit( final RelationConstruction e ) throws AlignmentException {
	final Constructor op = e.getOperator();
	String sop = SyntaxElement.getElement( op ).print(DEF) ;
	indentedOutput("{ class : \""+SyntaxElement.RELATION_EXPR.print(DEF)+"\","+NL);
	increaseIndent();
	if ( isPattern ) renderVariables( e );
	indentedOutput("\""+sop+"\" : ["+NL);
	increaseIndent();
	for ( final PathExpression re : e.getComponents() ) {
	    writer.print(linePrefix);
	    re.accept( this );
	    writer.print(NL);
	}
	decreaseIndent();
	indentedOutputln("]");
	decreaseIndent();
	indentedOutput("}");
    }
	
    public void visit(final RelationCoDomainRestriction c) throws AlignmentException {
	indentedOutput("{ class : \""+SyntaxElement.RELATION_CODOMAIN_COND.print(DEF)+"\","+NL);
	increaseIndent();
	if ( isPattern ) renderVariables( c );
	indentedOutput("\""+SyntaxElement.TOCLASS.print(DEF)+"\" : "+NL);
	increaseIndent();
	c.getCoDomain().accept( this );
	writer.print(NL);
	decreaseIndent();
	decreaseIndent();
	indentedOutput("}");
    }

    public void visit(final RelationDomainRestriction c) throws AlignmentException {
	indentedOutput("{ class : \""+SyntaxElement.RELATION_DOMAIN_COND.print(DEF)+"\","+NL);
	increaseIndent();
	if ( isPattern ) renderVariables( c );
	indentedOutput("\""+SyntaxElement.TOCLASS.print(DEF)+"\" : "+NL);
	increaseIndent();
	c.getDomain().accept( this );
	writer.print(NL);
	decreaseIndent();
	decreaseIndent();
	indentedOutput("}");
    }

    public void visit( final InstanceId e ) throws AlignmentException {
	indentedOutput("{ class : \""+SyntaxElement.INSTANCE_EXPR.print(DEF)+"\","+NL);
	increaseIndent();
	if ( e.getURI() != null ){
	    //indentedOutput("\rdf:about\" : \""+u+"\","+NL);
	    indentedOutput("@id : \""+e.getURI()+"\","+NL);
	}
	if ( isPattern ) renderVariables( e );
	decreaseIndent();
	indentedOutput("}");
    }
    
    public void visit( final Value e ) throws AlignmentException {
	indentedOutputln("{ class : \""+SyntaxElement.LITERAL.print(DEF)+"\",");
	increaseIndent();
	if ( e.getType() != null ) {
	    indentedOutputln("\""+SyntaxElement.ETYPE.print(DEF)+"\" : \""+e.getType()+"\",");
	}
	indentedOutputln("\""+SyntaxElement.STRING.print(DEF)+"\" : \""+e.getValue()+"\"");
	decreaseIndent();
	indentedOutput("}");
    }
	
    public void visit( final Apply e ) throws AlignmentException {
	indentedOutputln("{ class : \""+SyntaxElement.APPLY.print(DEF)+"\",");
	increaseIndent();
	indentedOutputln("\""+SyntaxElement.OPERATOR.print(DEF)+"\" : \""+e.getOperation()+"\",");
	indentedOutputln("\""+SyntaxElement.ARGUMENTS.print(DEF)+"\" : [");
	increaseIndent();
	for ( final ValueExpression ve : e.getArguments() ) {
	    writer.print(linePrefix);
	    ve.accept( this );
	    writer.print(","+NL);
	}
	decreaseIndent();
	indentedOutputln("]");
	decreaseIndent();
	indentedOutput("}");
    }

    public void visit( final Transformation transf ) throws AlignmentException {
	indentedOutputln("{ class : \""+SyntaxElement.TRANSF.print(DEF)+"\"");
	increaseIndent();
	indentedOutputln("\""+SyntaxElement.TRDIR.print(DEF)+"\" : \""+transf.getType()+"\",");
	indentedOutputln("\""+SyntaxElement.TRENT1.print(DEF)+"\" : ");
	increaseIndent();
	transf.getObject1().accept( this );
	decreaseIndent();
	writer.print(","+NL);
	indentedOutputln("\""+SyntaxElement.TRENT2.print(DEF)+"\" : ");
	increaseIndent();
	transf.getObject2().accept( this );
	decreaseIndent();
	writer.print(NL);
	decreaseIndent();
	indentedOutput("}");
    }

    public void visit( final Datatype e ) throws AlignmentException {
	indentedOutputln("\""+SyntaxElement.EDATATYPE.print(DEF)+"\" : ");
	increaseIndent();
	indentedOutput("{ class : \""+SyntaxElement.DATATYPE.print(DEF)+"\","+NL);
	increaseIndent();
	indentedOutput("@type : \""+e.getType()+"\" }");
	decreaseIndent();
	decreaseIndent();
    }
	
}
