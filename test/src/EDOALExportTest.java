/*
 * $Id$
 *
 * Copyright (C) 2006 Digital Enterprise Research Insitute (DERI) Innsbruck
 * Sourceforge version 1.3 -- 2007
 * Copyright (C) INRIA, 2009-2010
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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Configuration;
import org.testng.annotations.Test;
//import org.testng.annotations.*;

import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.Visitable;

import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import fr.inrialpes.exmo.align.impl.Annotations;
import fr.inrialpes.exmo.align.impl.Namespace;
import fr.inrialpes.exmo.align.impl.edoal.EDOALAlignment;
import fr.inrialpes.exmo.ontowrap.Ontology;
import fr.inrialpes.exmo.ontowrap.BasicOntology;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.OutputStream;

// JE: the old imports

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.net.URI;
import java.net.URISyntaxException;

//import org.omwg.mediation.language.export.omwg.OmwgSyntaxFormat;
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

import fr.inrialpes.exmo.align.impl.edoal.TransfService;
import fr.inrialpes.exmo.align.impl.edoal.Value;
import fr.inrialpes.exmo.align.impl.edoal.Datatype;
import fr.inrialpes.exmo.align.impl.edoal.Comparator;


/**
 * These tests corresponds to the tests presented in the examples/omwg directory
 */

public class EDOALExportTest {

    //    private OmwgSyntaxFormat sf;
    private RDFRendererVisitor renderer;
    private PrintWriter writer;
    private ByteArrayOutputStream stream;

    @Test(groups = { "full", "omwg", "raw" })
    //@BeforeClass(groups = { "full", "omwg", "raw" })
    public void setUp() throws Exception {
	// JE2009: Suppress this once all tests pass with render instead of export
	//	sf = new OmwgSyntaxFormat();
    }

    private String render( Visitable v ) throws Exception {
	// JE2009: This can only be improved if we can change the stream
	stream = new ByteArrayOutputStream(); 
	writer = new PrintWriter ( new BufferedWriter(
				                 new OutputStreamWriter( stream, "UTF-8" )), true);
	renderer = new RDFRendererVisitor( writer );
	renderer.setIndentString("");	// Indent should be empty
	renderer.setNewLineString("");
	v.accept( renderer );
	writer.flush();
	writer.close();
	stream.close();
	return stream.toString();
    }

    @Test(groups = { "full", "omwg", "raw" }, dependsOnMethods = {"setUp"})
    public void testExportPath() throws Exception {
	/*
        assertEquals( render( new Path(new PropertyId(new URI("http://my.beauty.url")))),
		     "<edoal:Property rdf:about=\"http://my.beauty.url\"/>");
	assertEquals( render( Path.EMPTY ),"<edoal:Path rdf:resource=\"http://ns.inria.fr/edoal#emptyPath\"/>");
	*/
	final LinkedHashSet<PathExpression> expressions = new LinkedHashSet<PathExpression>(3);
	expressions.add( new RelationId(new URI("http://my.beauty.url")) );
	expressions.add( new RelationId(new URI("http://my.nasty.url")) );
	expressions.add( new RelationId(new URI("http://my.richi.url")) );
	assertEquals( render( new RelationConstruction( Constructor.COMP, expressions ) ),
		      "<edoal:Relation><edoal:compose rdf:parseType=\"Collection\">" +
		      "<edoal:Relation rdf:about=\"http://my.beauty.url\"/>" +
		      "<edoal:Relation rdf:about=\"http://my.nasty.url\"/>" +
		      "<edoal:Relation rdf:about=\"http://my.richi.url\"/>" +
		      "</edoal:compose></edoal:Relation>" );

	expressions.add( new PropertyId(new URI("http://my.final.url")) );
	assertEquals( render( new PropertyConstruction( Constructor.COMP, expressions ) ),
		      "<edoal:Property><edoal:compose rdf:parseType=\"Collection\">" +
		      "<edoal:Relation rdf:about=\"http://my.beauty.url\"/>" +
		      "<edoal:Relation rdf:about=\"http://my.nasty.url\"/>" +
		      "<edoal:Relation rdf:about=\"http://my.richi.url\"/>" +
		      "<edoal:Property rdf:about=\"http://my.final.url\"/>" +
		      "</edoal:compose></edoal:Property>" );
    }

    /*
This is a test for ontowrap... not EDOAL.
Does bot work anymore because not visitable
    @Test(groups = { "full", "omwg", "raw" }, dependsOnMethods = {"setUp"})
    public void testExportOntology() throws Exception { //URISyntax
	Ontology onto = new BasicOntology();
	onto.setURI( new URI("http://path.to.the/source") );
	onto.setFormalism( "wsml" );
	onto.setFormURI( new URI("http://path.to.the/spec") );
	assertEquals( render( onto ), "<Ontology rdf:about=\"http://path.to.the/source\"><location>http://path.to.the/source</location><formalism><Formalism align:name=\"wsml\" align:uri=\"http://path.to.the/spec\"/></formalism></Ontology>");

	onto.setURI( new URI("http://path.to.the/target") );
	onto.setFormalism( "owl" );
	onto.setFormURI( new URI("http://path.to.the/owl") );
	assertEquals( render( onto ), "<Ontology rdf:about=\"http://path.to.the/target\"><location>http://path.to.the/target</location><formalism><Formalism align:name=\"owl\" align:uri=\"http://path.to.the/owl\"/></formalism></Ontology>");
    }
    */

    @Test(groups = { "full", "omwg", "raw" }, dependsOnMethods = {"setUp"})
    public void testExportInstanceExpression() throws Exception {
	final InstanceExpression toExport = new InstanceId(new URI("http://meine.tolle/instance#blah"));
	assertEquals( render( toExport ),
		      "<edoal:Instance rdf:about=\"http://meine.tolle/instance#blah\"/>" );
    }

    @Test(groups = { "full", "omwg", "raw" }, dependsOnMethods = {"setUp"})
    public void testExportClassExprSimple() throws Exception {
	final ClassExpression ce = new ClassId("Amertume");
	final String ref = "<edoal:Class rdf:about=\"Amertume\"/>";
	assertEquals( render( ce ), ref );
    }
    
    @Test(groups = { "full", "omwg", "raw" }, dependsOnMethods = {"setUp"})
    public void testExportClassExprSimpleError() throws Exception {
	// Should raise an error
	final ClassExpression ce = new ClassId("Amertume");
	final String ref = "<edoal:Class rdf:about=\"Amertume\"/>";
	assertEquals( render( ce ), ref );
    }
    
    @Test(groups = { "full", "omwg", "raw" }, dependsOnMethods = {"setUp"})
    public void testExportClassCond() throws Exception {
	ClassRestriction toExport = null;
	toExport = new ClassValueRestriction(new PropertyId(new URI("http://my.sister#age")),Comparator.GREATER,new Value("18"));
	assertEquals( render( toExport ), "<edoal:AttributeValueRestriction>"
	    + "<edoal:onAttribute><edoal:Property rdf:about=\"http://my.sister#age\"/></edoal:onAttribute>"
	    + "<edoal:comparator rdf:resource=\"http://www.w3.org/2001/XMLSchema#greater-than\"/>"
	    + "<edoal:value><edoal:Literal edoal:string=\"18\"/></edoal:value>"
			  + "</edoal:AttributeValueRestriction>" );
	toExport = new ClassTypeRestriction( new PropertyId(new URI("http://my.sister#age")), new Datatype("integer-under-100"));
	assertEquals( render( toExport ), "<edoal:PropertyTypeRestriction>"
	    + "<edoal:onAttribute><edoal:Property rdf:about=\"http://my.sister#age\"/></edoal:onAttribute>"
	    //+ "<edoal:comparator rdf:resource=\"http://www.w3.org/2001/XMLSchema#equals\"/>"
	    + "<edoal:datatype>integer-under-100</edoal:datatype>"
		+ "</edoal:PropertyTypeRestriction>" );
	toExport = new ClassOccurenceRestriction( new PropertyId(new URI("http://my.sister#age")), Comparator.GREATER, 18);
	assertEquals( render( toExport ), "<edoal:AttributeOccurenceRestriction>"
	    + "<edoal:onAttribute><edoal:Property rdf:about=\"http://my.sister#age\"/></edoal:onAttribute>"
	    + "<edoal:comparator rdf:resource=\"http://www.w3.org/2001/XMLSchema#greater-than\"/>"
	    + "<edoal:value>18</edoal:value>"
		      + "</edoal:AttributeOccurenceRestriction>" );
    }
    
    @Test(groups = { "full", "omwg", "raw" }, dependsOnMethods = {"setUp"})
    public void testExportClassExprOr() throws Exception {
	final Set<ClassExpression> expressions = new LinkedHashSet<ClassExpression>(3);
	expressions.add( new ClassId("Acidite") );
	expressions.add( new ClassId("Amertume") );
	expressions.add( new ClassId("Astreinngence") );
	final ClassExpression ce = new ClassConstruction( Constructor.OR, expressions );
	final String ref = "<edoal:Class>" + "<edoal:or rdf:parseType=\"Collection\">"
	    + "<edoal:Class rdf:about=\"Acidite\"/>"
	    + "<edoal:Class rdf:about=\"Amertume\"/>"
	    + "<edoal:Class rdf:about=\"Astreinngence\"/>" 
	    + "</edoal:or>"+"</edoal:Class>";
	assertEquals( render( ce ), ref );
    }
    
    @Test(groups = { "full", "omwg", "raw" }, dependsOnMethods = {"setUp"})
    public void testExportClassExprAnd() throws Exception {
	final Set<ClassExpression> expressions = new LinkedHashSet<ClassExpression>(3);
	expressions.add(new ClassId("Acidite"));
	expressions.add(new ClassId("Amertume"));
	expressions.add(new ClassId("Astreinngence"));
	final ClassExpression ce = new ClassConstruction( Constructor.AND, expressions );
	final String ref = "<edoal:Class>" + "<edoal:and rdf:parseType=\"Collection\">"
	    + "<edoal:Class rdf:about=\"Acidite\"/>"
	    + "<edoal:Class rdf:about=\"Amertume\"/>"
	    + "<edoal:Class rdf:about=\"Astreinngence\"/>" 
	    + "</edoal:and>"+"</edoal:Class>";
	assertEquals( render( ce ), ref );
    }
    
    @Test(groups = { "full", "omwg", "raw" }, dependsOnMethods = {"setUp"})
    public void testExportClassExprNot() throws Exception {
	final ClassExpression ce = new ClassConstruction(Constructor.NOT, 
					 Collections.singleton((ClassExpression)new ClassId("Acidite")));
	final String ref = "<edoal:Class>" + "<edoal:not>"
	    + "<edoal:Class rdf:about=\"Acidite\"/>" + "</edoal:not>"
	    + "</edoal:Class>";
	assertEquals( render( ce ), ref );
    }
    
    @Test(groups = { "full", "omwg", "raw" }, dependsOnMethods = {"setUp"})
    public void testExportClassExprOrCond() throws Exception {
	final Set<ClassExpression> expressions = new LinkedHashSet<ClassExpression>(3);
	expressions.add(new ClassId("Acidite"));
	expressions.add(new ClassId("Amertume"));
	expressions.add(new ClassId("Astreinngence"));
	expressions.add(new ClassValueRestriction( new PropertyId(new URI("http://vinum#age")), Comparator.GREATER, new Value("20")));
	final ClassExpression ce = new ClassConstruction( Constructor.OR, expressions );
	assertEquals( render( ce ), "<edoal:Class>" + "<edoal:or rdf:parseType=\"Collection\">"
	    + "<edoal:Class rdf:about=\"Acidite\"/>"
	    + "<edoal:Class rdf:about=\"Amertume\"/>"
	    + "<edoal:Class rdf:about=\"Astreinngence\"/>"
	    + "<edoal:AttributeValueRestriction>"
	    + "<edoal:onAttribute>"
	    + "<edoal:Property rdf:about=\"http://vinum#age\"/>"
	    + "</edoal:onAttribute>"
	    + "<edoal:comparator rdf:resource=\"http://www.w3.org/2001/XMLSchema#greater-than\"/>"
	    + "<edoal:value><edoal:Literal edoal:string=\"20\"/></edoal:value>"
	    + "</edoal:AttributeValueRestriction>"
	    + "</edoal:or>"+ "</edoal:Class>" );
    }

    @Test(groups = { "full", "omwg", "raw" }, dependsOnMethods = {"setUp"})
    public void testExportPropertyCond() throws Exception {
	assertEquals( render( new PropertyDomainRestriction(new ClassId("http://meine/tolle/restriction")) ),
		      "<edoal:DomainRestriction><edoal:class>"
		      + "<edoal:Class rdf:about=\"http://meine/tolle/restriction\"/>"
		      + "</edoal:class></edoal:DomainRestriction>" );
	assertEquals( render( new PropertyValueRestriction( Comparator.EQUAL, new Value("18"))),
		      "<edoal:ValueRestriction>"
		      + "<edoal:comparator rdf:resource=\"http://www.w3.org/2001/XMLSchema#equals\"/>"
		      + "<edoal:value><edoal:Literal edoal:string=\"18\"/></edoal:value>"
		      + "</edoal:ValueRestriction>" );
	assertEquals( render( new PropertyTypeRestriction(new Datatype("int"))),
		      "<edoal:TypeRestriction><edoal:datatype>int</edoal:datatype></edoal:TypeRestriction>" );
    }
    
    @Test(groups = { "full", "omwg", "raw" }, dependsOnMethods = {"setUp"})
    public void testExportPropertyExpr() throws Exception {
	final Set<PathExpression> expressions = new LinkedHashSet<PathExpression>(2);
	expressions.add(new PropertyId(new URI("http://mein/super/property0")));
	expressions.add(new PropertyId(new URI("http://mein/super/property1")));
	final PropertyId single = new PropertyId(new URI("http://mein/super/property"));
	
	PropertyExpression toExport = single;
	assertEquals( render( toExport), "<edoal:Property rdf:about=\"http://mein/super/property\"/>");
	toExport = new PropertyConstruction( Constructor.AND, expressions );
	assertEquals( render( toExport), "<edoal:Property><edoal:and rdf:parseType=\"Collection\">"
		     + "<edoal:Property rdf:about=\"http://mein/super/property0\"/>"
		     + "<edoal:Property rdf:about=\"http://mein/super/property1\"/>"
		     + "</edoal:and></edoal:Property>");
	// JE2009: Illegal
	/*
	toExport = new PropertyExpression(
			      single,
			      Collections
			      .singleton((PropertyExpression) new PropertyDomainRestriction(
											   new ClassId("http://my/super/class"),
													   Comparator.EQUAL)));
	assertEquals( render( toExport), "<edoal:Property rdf:about=\"http://mein/super/property\">"
		     + "<edoal:domainRestriction>"
		     + "<edoal:Class rdf:about=\"http://my/super/class\"/>"
		     + "</edoal:domainRestriction>" + "</edoal:Property>");
	*/

	final Set<PathExpression> expressions2 = new LinkedHashSet<PathExpression>(2);
	expressions2.add( new PropertyConstruction( Constructor.OR, expressions ));
	expressions2.add( new PropertyValueRestriction(Comparator.EQUAL,new Value("5")));
	toExport = new PropertyConstruction( Constructor.AND, expressions2 );
	assertEquals( render( toExport),  "<edoal:Property><edoal:and rdf:parseType=\"Collection\"><edoal:Property><edoal:or rdf:parseType=\"Collection\">"
		      + "<edoal:Property rdf:about=\"http://mein/super/property0\"/>"
		      + "<edoal:Property rdf:about=\"http://mein/super/property1\"/>"
		      + "</edoal:or></edoal:Property>"
		      + "<edoal:ValueRestriction>"
		      + "<edoal:comparator rdf:resource=\"http://www.w3.org/2001/XMLSchema#equals\"/>"
		      + "<edoal:value><edoal:Literal edoal:string=\"5\"/></edoal:value></edoal:ValueRestriction>"
		      + "</edoal:and></edoal:Property>");
	toExport = new PropertyConstruction( Constructor.NOT, Collections.singleton((PathExpression)new PropertyId(new URI("http://mein/super/property"))));
	// JE2009-ERROR-HERE
	/*
	assertEquals( sf.export( toExport), "<edoal:Property><edoal:not>"
		     + "<edoal:Property rdf:about=\"http://mein/super/property\"/>"
		     + "</edoal:not><edoal:transf rdf:resource=\"http://mein/transformator\"></edoal:transf></edoal:Property>" );
	*/
	// TODO: do the transf!!!
	// JE2010: do not know what to do
	//, new TransfService(new URI( "http://mein/transformator"), null));
    }

    // ------

    @Test(groups = { "full", "omwg", "raw" }, dependsOnMethods = {"setUp"})
    public void testExportRelationCondCond() throws Exception {
	RelationRestriction toExport = new RelationDomainRestriction(new ClassId("http://my/super/class"));
	assertEquals( render( toExport), "<edoal:DomainRestriction><edoal:class>"
		      + "<edoal:Class rdf:about=\"http://my/super/class\"/>"
		      + "</edoal:class></edoal:DomainRestriction>");
    toExport = new RelationCoDomainRestriction(new ClassId("http://my/super/class"));
	assertEquals( render( toExport), "<edoal:CodomainRestriction><edoal:class>"
	    + "<edoal:Class rdf:about=\"http://my/super/class\"/>"
		      + "</edoal:class></edoal:CodomainRestriction>");
    }

    @Test(groups = { "full", "omwg", "raw" }, dependsOnMethods = {"setUp"})
    public void testParseRelationExpr() throws Exception {
	
	RelationExpression toExport = new RelationId("http://my/super/relation");
	assertEquals( render(toExport), 
		      "<edoal:Relation rdf:about=\"http://my/super/relation\"/>");
	// JE2009: Illegal
	/*
	toExport = new RelationExpression(
			     new RelationId("http://my/super/relation"),
			     Collections
			     .singleton((RelationExpression) new RelationDomainRestriction(new ClassId("http://my/super/class"))));
	assertEquals( sf.export(toExport), 
	    "<edoal:Relation rdf:about=\"http://my/super/relation\">"
	    + "<edoal:domainRestriction>"
	    + "<edoal:Class rdf:about=\"http://my/super/class\"/>"
			  + "</edoal:domainRestriction>" + "</edoal:Relation>");
	*/


	// JE 2010: I could export it as well
	RelationExpression relexp = new RelationDomainRestriction(							  new ClassId("http://my/super/class"));

	final Set<PathExpression> expressions = new LinkedHashSet<PathExpression>(2);
	expressions.add(new RelationId("http://my/super/relation0"));
	expressions.add(new RelationId("http://my/super/relation1"));
	expressions.add( relexp );

	toExport = new RelationConstruction( Constructor.AND, expressions );
	assertEquals( render( toExport ), 
	    "<edoal:Relation>"
	    + "<edoal:and rdf:parseType=\"Collection\">"
	    + "<edoal:Relation rdf:about=\"http://my/super/relation0\"/>"
	    + "<edoal:Relation rdf:about=\"http://my/super/relation1\"/>"
	    + "<edoal:DomainRestriction><edoal:class>"
	    + "<edoal:Class rdf:about=\"http://my/super/class\"/>"
	    + "</edoal:class></edoal:DomainRestriction>" 
	    + "</edoal:and>" + "</edoal:Relation>");
	toExport = new RelationConstruction( Constructor.OR, expressions );
	assertEquals( render( toExport ), 
	    "<edoal:Relation>"
	    + "<edoal:or rdf:parseType=\"Collection\">"
	    + "<edoal:Relation rdf:about=\"http://my/super/relation0\"/>"
	    + "<edoal:Relation rdf:about=\"http://my/super/relation1\"/>"
	    + "<edoal:DomainRestriction><edoal:class>"
	    + "<edoal:Class rdf:about=\"http://my/super/class\"/>"
	    + "</edoal:class></edoal:DomainRestriction>" 
		      + "</edoal:or>" + "</edoal:Relation>");

	final Set<PathExpression> expressions2 = new LinkedHashSet<PathExpression>();
	expressions2.add(new RelationConstruction(Constructor.NOT,Collections.singleton((PathExpression)new RelationId("http://my/super/relation"))));
	expressions2.add(new RelationCoDomainRestriction(new ClassId("http://my/super/class")));

	toExport = new RelationConstruction( Constructor.AND, expressions2 );
	assertEquals( render( toExport ), 
	    "<edoal:Relation>"
	    + "<edoal:and rdf:parseType=\"Collection\">"
	    + "<edoal:Relation><edoal:not>"
	    + "<edoal:Relation rdf:about=\"http://my/super/relation\"/>"
	    + "</edoal:not></edoal:Relation>" 
	    + "<edoal:CodomainRestriction><edoal:class>"
	    + "<edoal:Class rdf:about=\"http://my/super/class\"/>"
	    + "</edoal:class></edoal:CodomainRestriction>" 
	    + "</edoal:and>" + "</edoal:Relation>");
	toExport = new RelationConstruction( Constructor.INVERSE, Collections.singleton((PathExpression)new RelationId("http://my/super/relation")));
	assertEquals( render( toExport ), 
	    "<edoal:Relation>"
	    + "<edoal:inverse>"
	    + "<edoal:Relation rdf:about=\"http://my/super/relation\"/>"
	    + "</edoal:inverse>" + "</edoal:Relation>");
	toExport = new RelationConstruction(Constructor.SYMMETRIC, Collections.singleton((PathExpression)new RelationId("http://my/super/relation")));
	assertEquals( render( toExport ), 
	    "<edoal:Relation>"
	    + "<edoal:symmetric>"
	    + "<edoal:Relation rdf:about=\"http://my/super/relation\"/>"
			  + "</edoal:symmetric>" + "</edoal:Relation>");
	toExport = new RelationConstruction(Constructor.TRANSITIVE, Collections.singleton((PathExpression)new RelationId("http://my/super/relation")));
	assertEquals( render( toExport ), 
	    "<edoal:Relation>"
	    + "<edoal:transitive>"
	    + "<edoal:Relation rdf:about=\"http://my/super/relation\"/>"
			  + "</edoal:transitive>" + "</edoal:Relation>");
	toExport = new RelationConstruction( Constructor.REFLEXIVE, Collections.singleton((PathExpression)new RelationId("http://my/super/relation")));
	assertEquals( render(toExport), 
	    "<edoal:Relation>"
	    + "<edoal:reflexive>"
	    + "<edoal:Relation rdf:about=\"http://my/super/relation\"/>"
			  + "</edoal:reflexive>" + "</edoal:Relation>" );
	
    }
    
    @Test(groups = { "full", "omwg" }, dependsOnMethods = {"setUp"})
    public void testExportCell() throws Exception {
	// JE2009: these types of MappingRule do not exist anymore: find the new way to do it.
	/*
	final MappingRule[] toExport = new MappingRule[] {
	    new Class2Class(null, Direction.EQUIVALENCE, new ClassId("http://my/super/class0"),
			    new ClassId("http://my/super/class1"), .5f),
	    new Relation2Class(null, Direction.MAPPING, new RelationId("http://my/super/attribute"),
			       new ClassId("http://my/super/instance"), .3f) };
	
	final String[] outcome = new String[] {
	    "<Cell>"
	    + "<entity1><Class rdf:about=\"http://my/super/class0\"></Class></entity1>"
	    + "<entity2><Class rdf:about=\"http://my/super/class1\"></Class></entity2>"
	    + "<measure>0.5</measure>"
	    + "<relation>ClassEquivalence</relation>" + "</Cell>",
	    "<Cell>"
	    + "<entity1><Relation rdf:about=\"http://my/super/attribute\"></Relation></entity1>"
	    + "<entity2><Class rdf:about=\"http://my/super/instance\"></Class></entity2>"
	    + "<measure>0.3</measure>"
	    + "<relation>RelationClassMapping</relation>"
	    + "</Cell>" };
	
	for (int i = 0; i < toExport.length; i++) {
	    assertEquals(outcome[i], sf.export(toExport[i]));
	}
	*/
    }
    
    @Test(groups = { "full", "omwg", "raw" }, dependsOnMethods = {"setUp"})
	public void testExportAlignment() throws Exception {
	
	Ontology o1 = new BasicOntology();
	o1.setURI( new URI("http://source") );
	o1.setFormalism( "wsml" );
	o1.setFormURI( new URI("http://wsml") );
	Ontology o2 = new BasicOntology();
	o2.setURI( new URI("http://target") );
	o2.setFormalism( "wsml" );
	o2.setFormURI( new URI("http://wsml") );
	final EDOALAlignment doc = new EDOALAlignment();
	doc.setExtension( Namespace.ALIGNMENT.uri, Annotations.ID, "http://asdf" );
	doc.init( o1, o2 );
	
	assertEquals( render( doc ), 
"<?xml version='1.0' encoding='utf-8' standalone='no'?><rdf:RDF xmlns='http://knowledgeweb.semanticweb.org/heterogeneity/alignment#'"+
         " xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'"+
         " xmlns:xsd='http://www.w3.org/2001/XMLSchema#'"+
         " xmlns:align='http://knowledgeweb.semanticweb.org/heterogeneity/alignment#'"+
         " xmlns:edoal='http://ns.inria.org/edoal/1.0/#'>"+
		      "<Alignment rdf:about=\"http://asdf\"><xml>yes</xml><level>2EDOAL</level><type>**</type><id>http://asdf</id>"
	    + "<onto1>"
	    + "<Ontology rdf:about=\"http://source\"><location>http://source</location>"
	    + "<formalism><Formalism align:name=\"wsml\" align:uri=\"http://wsml\"/></formalism>"
	    + "</Ontology>" + "</onto1>" + "<onto2>"
	    + "<Ontology rdf:about=\"http://target\"><location>http://target</location>"
	    + "<formalism><Formalism align:name=\"wsml\" align:uri=\"http://wsml\"/></formalism>"
	    + "</Ontology>" + "</onto2>"
		      + "</Alignment>" +"</rdf:RDF>" );
	doc.setType( "1*" );
	assertEquals( render( doc ), 
"<?xml version='1.0' encoding='utf-8' standalone='no'?><rdf:RDF xmlns='http://knowledgeweb.semanticweb.org/heterogeneity/alignment#'"+
         " xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'"+
         " xmlns:xsd='http://www.w3.org/2001/XMLSchema#'"+
         " xmlns:align='http://knowledgeweb.semanticweb.org/heterogeneity/alignment#'"+
         " xmlns:edoal='http://ns.inria.org/edoal/1.0/#'>"+
		      "<Alignment rdf:about=\"http://asdf\"><xml>yes</xml><level>2EDOAL</level><type>1*</type><id>http://asdf</id>"
	    + "<onto1>"
	    + "<Ontology rdf:about=\"http://source\"><location>http://source</location>"
	    + "<formalism><Formalism align:name=\"wsml\" align:uri=\"http://wsml\"/></formalism>"
	    + "</Ontology>" + "</onto1>" + "<onto2>"
	    + "<Ontology rdf:about=\"http://target\"><location>http://target</location>"
	    + "<formalism><Formalism align:name=\"wsml\" align:uri=\"http://wsml\"/></formalism>"
	    + "</Ontology>" + "</onto2>"
		      + "</Alignment>" +"</rdf:RDF>" );
    }

    // JE2009: Incorrect one
    /*
    @Test(groups = { "full", "omwg", "raw" }, dependsOnMethods = {"setUp"})
    public void testExportRelationExprExotic() throws Exception {
	final String reference = "<edoal:Relation>"
	    + "<edoal:and rdf:parseType=\"Collection\">"
	    + "<edoal:DomainRestriction><edoal:class>"
	    + "<edoal:Class rdf:about=\"r2\"/>" + "</edoal:class></edoal:DomainRestriction>"
	    + "<edoal:Relation rdf:about=\"c1\"/>"
	    + "<edoal:DomainRestriction><edoal:class>" + "<edoal:Class rdf:about=\"r1\"/>"
	    + "</edoal:class></edoal:DomainRestriction>"
	    + "<edoal:DomainRestriction><edoal:class>" + "<edoal:Class rdf:about=\"r0\"/>"
	    + "</edoal:class></edoal:DomainRestriction>" + "</edoal:and>" + "</edoal:Relation>";
	
	RelationId c1 = new RelationId("c1");
	RelationId c2 = new RelationId("c2");
	ComplexExpression combined = new ComplexExpression(Arrays
							   .asList(new RelationId[] { c1, c2 }), Constructor.AND);
	RelationRestriction con0 = new RelationDomainRestriction(new ClassId("r0")), combined);
	RelationRestriction con1 = new RelationDomainRestriction(ClassId("r1")), c1);
	RelationRestriction con2 = new RelationDomainRestriction(ClassId("r2")), c2);
	RelationExpression ce = new RelationConstruction(combined, Arrays
					   .asList(new RelationRestriction[] { con0, con1, con2 }));
	
	assertEquals( sf.export(ce), reference );
    }
    */

    // JE2009: Incorrect one
    /*
    @Test(groups = { "full", "omwg", "raw" }, dependsOnMethods = {"setUp"})
    public void testExportPropertyExprExotic() throws Exception {
	final String reference = "<edoal:Property>"
	    + "<edoal:and rdf:parseType=\"Collection\">"
	    + "<edoal:Property rdf:about=\"c2\">" + "<edoal:valueRestriction>"
	    + "<edoal:value>val2</edoal:value>"
	    + "<edoal:comparator rdf:resource=\"http://www.w3.org/2001/XMLSchema#equals\"/>" + "</edoal:valueRestriction>"
	    + "</edoal:Property>" + "<edoal:Property rdf:about=\"c1\">"
	    + "<edoal:valueRestriction>" + "<edoal:value>val1</edoal:value>"
	    + "<edoal:comparator rdf:resource=\"http://www.w3.org/2001/XMLSchema#equals\"/>" + "</edoal:valueRestriction>"
	    + "</edoal:Property>" + "</edoal:and>" + "<edoal:valueRestriction>"
	    + "<edoal:value>val0</edoal:value>"
	    + "<edoal:comparator rdf:resource=\"http://www.w3.org/2001/XMLSchema#equals\"/>" + "</edoal:valueRestriction>"
	    + "</edoal:Property>";
	
	PropertyId c1 = new PropertyId("c1");
	PropertyId c2 = new PropertyId("c2");
	ComplexExpression combined = new ComplexExpression(Arrays
							   .asList(new PropertyId[] { c1, c2 }), Constructor.AND);
	PropertyRestriction con0 = new PropertyValueRestriction(new Restriction(
								     new Value("val0"), Comparator.EQUAL), combined);
	PropertyRestriction con1 = new PropertyValueRestriction(new Restriction(
								     new Value("val1"), Comparator.EQUAL), c1);
	PropertyRestriction con2 = new PropertyValueRestriction(new Restriction(
								     new Value("val2"), Comparator.EQUAL), c2);
	PropertyExpression ce = new PropertyConstruction(combined, Arrays
					     .asList(new PropertyRestriction[] { con0, con1, con2 }));
	
	assertEquals( sf.export(ce), reference );
    }
    */

    // JE2009: Incorrect one
    /*    
    @Test(groups = { "full", "omwg" }, dependsOnMethods = {"setUp"})
    public void testExportClassExprExotic() throws Exception {
	final String reference = "<edoal:Class>"
	    + "<edoal:and rdf:parseType=\"Collection\">"
	    + "<edoal:Class rdf:about=\"c2\">" + "<edoal:attributeValueCondition>"
	    + "<edoal:Restriction>"
	    + "<edoal:onProperty><edoal:Property rdf:about=\"a2\"/></edoal:onProperty>"
	    + "<edoal:comparator rdf:resource=\"http://www.w3.org/2001/XMLSchema#equals\"/>"
	    + "<edoal:value>val2</edoal:value>" + "</edoal:Restriction>"
	    + "</edoal:attributeValueCondition>" + "</edoal:Class>"
	    + "<edoal:Class rdf:about=\"c1\">" + "<edoal:attributeValueCondition>"
	    + "<edoal:Restriction>"
	    + "<edoal:onProperty><edoal:Property rdf:about=\"a1\"/></edoal:onProperty>"
	    + "<edoal:comparator rdf:resource=\"http://www.w3.org/2001/XMLSchema#equals\"/>"
	    + "<edoal:value>val1</edoal:value>" + "</edoal:Restriction>"
	    + "</edoal:attributeValueCondition>" + "</edoal:Class>" + "</edoal:and>"
	    + "<edoal:attributeValueCondition>" + "<edoal:Restriction>"
	    + "<edoal:onProperty><edoal:Property rdf:about=\"a0\"/></edoal:onProperty>"
	    + "<edoal:comparator rdf:resource=\"http://www.w3.org/2001/XMLSchema#equals\"/>"
	    + "<edoal:value><edoal:Literal edoal:string=\"val0\"/></edoal:value>" + "</edoal:Restriction>"
	    + "</edoal:attributeValueCondition>" + "</edoal:Class>";
	
	ClassId c1 = new ClassId("c1");
	ClassId c2 = new ClassId("c2");
	ComplexExpression combined = new ComplexExpression(Arrays
							   .asList(new ClassId[] { c1, c2 }), Constructor.AND);
	ClassRestriction con0 = new ClassValueRestriction(new PropertyId("a0"), Comparator.EQUAL,new Value("val0")),
							  combined);
	ClassRestriction con1 = new ClassValueRestriction(new PropertyId("a1"), Comparator.EQUAL, new Value("val1")), c1);
	ClassRestriction con2 = new ClassValueRestriction(new PropertyId("a2"), Comparator.EQUAL,new Value("val2")), c2);
	ClassExpression ce = new ClassExpression(combined, Arrays
				     .asList(new ClassRestriction[] { con0, con1, con2 }));
	
	assertEquals(reference, sf.export(ce));
    }
    */

}
