/*
 * $Id$
 *
 * Copyright (C) 2006 Digital Enterprise Research Insitute (DERI) Innsbruck
 * Sourceforge version 1.2 -- 2006
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

import fr.inrialpes.exmo.align.parser.AlignmentParser;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;

// JE: the old imports

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.net.URI;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import fr.inrialpes.exmo.align.impl.edoal.Path;
import fr.inrialpes.exmo.align.impl.edoal.Value;
import fr.inrialpes.exmo.align.impl.edoal.PropertyId;
import fr.inrialpes.exmo.align.impl.edoal.ClassId;
import fr.inrialpes.exmo.align.impl.edoal.InstanceId;
import fr.inrialpes.exmo.align.impl.edoal.RelationId;

// JE2009: These tests have been made for the old XPathParser
// which does not exist anymore... hence nothing can work
// Even the document builder is useless

/**
 * These tests corresponds to the tests presented in the examples/omwg directory
 */

public class EDOALParserTest {

    private static DocumentBuilder BUILDER;

    @Test(groups = { "full", "omwg", "raw" })
    public void setUp() throws Exception {
	final DocumentBuilderFactory FAC = DocumentBuilderFactory.newInstance();
	FAC.setValidating(false);
	FAC.setNamespaceAware(false);
	DocumentBuilder doc = null;
	try {
	    doc = FAC.newDocumentBuilder();
	} catch (ParserConfigurationException e) {
	    e.printStackTrace();
	}
	BUILDER = doc;
    }

    @Test(groups = { "full", "omwg", "raw" }, dependsOnMethods = {"setUp"})
    public void testParsePath() {
	// Does not work becaue it is supposed to work with RDF models...
	/*
	Utility uparser = new Utility();
	assertEquals( uparser.parsePath( "<Property rdf:about='http://my.beauty.url'/>" ),
		      
	final String[] toParse = new String[] {
	    "<Property rdf:about='http://my.beauty.url'/>",
	    "<Path rdf:resource='http://URI#empty'/>",
	    "<Path><first><Property rdf:about='http://my.beauty.url'/></first>"
	    + "<next><Path><first><Property rdf:about='http://my.nasty.url'/></first>"
	    + "<next><Path><first><Property rdf:about='http://my.richi.url'/></first>"
	    + "<next><Property rdf:about='http://my.final.url'/></next>"
	    + "</Path></next></Path></next></Path>" };
	final Path[] outcome = new Path[] {
	    new Path( new PropertyId( "http://my.beauty.url" ) ),
	    Path.EMPTY,
	    new Path(new PropertyId( "http://my.beauty.url"), "http://my.nasty.url",
		     "http://my.richi.url", "http://my.final.url") };
	try {
	    runParse(toParse, outcome, XpathParser.class.getDeclaredMethod(
									   "parsePath", Node.class));
	} catch (SecurityException e) {
	    e.printStackTrace();
	} catch (NoSuchMethodException e) {
	    e.printStackTrace();
	}
	*/
    }

    /*
    @Test(groups = { "full", "omwg" }, dependsOnMethods = {"setUp"})
    public void testParseOntology() {
	final String[] toParse = new String[] {
	    "<onto1><formalism name='wsml' uri='http://path.to.the/spec'/>"
	    + "<uri>http://path.to.the/source</uri></onto1>",
	    "<onto2><formalism name='owl' uri='http://path.to.the/owl'/>"
	    + "<uri>http://path.to.the/target</uri></onto2>" };
	final OntologyId[] outcome = new OntologyId[] {
	    new OntologyId(new URI("http://path.to.the/source"), "wsml",
			   new URI("http://path.to.the/spec")),
	    new OntologyId(new URI("http://path.to.the/target"), "owl",
			   new URI("http://path.to.the/owl")) };
	try {
	    runParse(toParse, outcome, XpathParser.class.getDeclaredMethod(
									   "parseOntology", Node.class));
	} catch (SecurityException e) {
	    e.printStackTrace();
	} catch (NoSuchMethodException e) {
	    e.printStackTrace();
	}
    }

    @Test(groups = { "full", "omwg" }, dependsOnMethods = {"setUp"})
    public void testParseInstanceExpression() {
	final String[] toParse = new String[] { "<Instance rdf:about='http://meine.tolle/instance#blah'/>" };
	final InstanceExpr[] outcome = new InstanceExpr[] { new InstanceExpr(
									     new InstanceId("http://meine.tolle/instance#blah")) };
	try {
	    runParse(toParse, outcome, XpathParser.class.getDeclaredMethod(
									   "parseInstanceExpr", Node.class));
	} catch (SecurityException e) {
	    e.printStackTrace();
	} catch (NoSuchMethodException e) {
	    e.printStackTrace();
	}
    }
    
    @Test(groups = { "full", "omwg" }, dependsOnMethods = {"setUp"})
    public void testParseRestriction() {
	final String[] toParse = new String[] {
	    "<Restriction><onProperty>"
	    + "<Property rdf:about='http://my.sister#age'/></onProperty>"
	    + "<comparator rdf:resource='greater-than'/>"
	    + "<value>18</value></Restriction>",
	    "<Restriction><onProperty>\n\t<Path>"
	    + "\n\t<first><Property rdf:about='hasTerroir'/></first>"
	    + "<next><Property rdf:about='LocatedIn'/></next>"
	    + "</Path>\n\t</onProperty>"
	    + "<comparator rdf:resource='equal'/>"
	    + "<value>Europe</value></Restriction>" };
	final RestrictionConst[] outcome = new RestrictionConst[] {
	    new RestrictionConst(new Path("http://my.sister#age"),
				 new Restriction(new SimpleValue("18"),
						 Comparator.GREATER)),
	    new RestrictionConst(new Path("hasTerroir", "LocatedIn"),
				 new Restriction(new SimpleValue("Europe"),
						 Comparator.EQUAL)) };
	try {
	    runParse(toParse, outcome, XpathParser.class.getDeclaredMethod(
									   "parseRestriction", Node.class));
	} catch (SecurityException e) {
	    e.printStackTrace();
	} catch (NoSuchMethodException e) {
	    e.printStackTrace();
	}
    }

    @Test(groups = { "full", "omwg" }, dependsOnMethods = {"setUp"})
    public void testParseClassCond() {
	final String[] toParse = new String[] {
	    "<attributeValueCondition>"
	    + "<Restriction>"
	    + "<onProperty><Property rdf:about='http://my.sister#age'/></onProperty>"
	    + "<comparator ref:resource='greater-than'/>"
	    + "<value>18</value>" + "</Restriction>"
	    + "</attributeValueCondition>",
	    "<attributeTypeCondition>"
	    + "<Restriction>"
	    + "<onProperty><Property rdf:about='http://my.sister#age'/></onProperty>"
	    + "<comparator ref:resource='equal'/>"
	    + "<value>18</value>" + "</Restriction>"
	    + "</attributeTypeCondition>",
	    "<attributeOccurenceCondition>"
	    + "<Restriction>"
	    + "<onProperty><Property rdf:about='http://my.sister#age'/></onProperty>"
	    + "<comparator ref:resource='greater-than'/>"
	    + "<value>18</value>" + "</Restriction>"
	    + "</attributeOccurenceCondition>" };
	final Set[] outcome = new Set[] {
	    Collections.singleton(new PropertyValueCondition(new Path(
								       "http://my.sister#age"), new Restriction(
														new SimpleValue("18"), Comparator.GREATER))),
	    Collections.singleton(new PropertyTypeCondition(new Path(
								      "http://my.sister#age"), new Restriction(
													       new SimpleValue("18"), Comparator.EQUAL))),
	    Collections.singleton(new PropertyOccurenceCondition(new Path(
									   "http://my.sister#age"), new Restriction(
														    new SimpleValue("18"), Comparator.GREATER))) };
	
	try {
	    runParseWithNodelist(toParse, outcome, XpathParser.class
				 .getDeclaredMethod("parseClassCond", NodeList.class));
	} catch (SecurityException e) {
	    e.printStackTrace();
	} catch (NoSuchMethodException e) {
	    e.printStackTrace();
	}
    }

    @Test(groups = { "full", "omwg" }, dependsOnMethods = {"setUp"})
    public void testParseClassExpr() {
	final String[] toParse = new String[] {
	    "<Class>" + "<or parse:Type='Collection'>"
	    + "<Class rdf:about='Acidite'/>"
	    + "<Class rdf:about='Amertume'/>"
	    + "<Class rdf:about='Astreinngence'/>" + "</or>"
	    + "</Class>",
	    "<Class>" + "<and parse:Type='Collection'>"
	    + "<Class rdf:about='Acidite'/>"
	    + "<Class rdf:about='Amertume'/>"
	    + "<Class rdf:about='Astreinngence'/>" + "</and>"
	    + "</Class>",
	    "<Class>" + "<not parse:Type='Collection'>"
	    + "<Class rdf:about='Acidite'/>" + "</not>"
	    + "</Class>",
	    "<Class>" + "<or parse:Type='Collection'>"
	    + "<Class rdf:about='Acidite'/>"
	    + "<Class rdf:about='Amertume'/>"
	    + "<Class rdf:about='Astreinngence'/>" + "</or>"
	    + "<attributeValueCondition>" + "<Restriction>"
	    + "<onProperty>"
	    + "<Property rdf:about='http://vinum#age'/>"
	    + "</onProperty>"
	    + "<comparator ref:resource='greater-than'/>"
	    + "<value>20</value>" + "</Restriction>"
	    + "</attributeValueCondition>" + "</Class>",
	    "<Class rdf:about='Amertume'>" + "<attributeValueCondition>"
	    + "<Restriction>" + "<onProperty>"
	    + "<Property rdf:about='http://vinum#age'/>"
	    + "</onProperty>"
	    + "<comparator ref:resource='greater-than'/>"
	    + "<value>20</value>" + "</Restriction>"
	    + "</attributeValueCondition>" + "</Class>",
	    "<Class rdf:about='Amertume'></Class>" };
	final Set<ExpressionDefinition> expressions = new HashSet<ExpressionDefinition>(
											3);
	expressions.add(new ClassId("Acidite"));
	expressions.add(new ClassId("Amertume"));
	expressions.add(new ClassId("Astreinngence"));
	
	final ExpressionDefinition ref = new ComplexExpression(expressions,
							       Operator.OR);
	
	final Set<ClassCondition> conditions0 = Collections
	    .singleton((ClassCondition) new PropertyValueCondition(
								    new Path("http://vinum#age"), new Restriction(
														  new SimpleValue("20"), Comparator.GREATER), ref));
	final Set<ClassCondition> conditions1 = Collections
	    .singleton((ClassCondition) new PropertyValueCondition(
								    new Path("http://vinum#age"), new Restriction(
														  new SimpleValue("20"), Comparator.GREATER),
								    new ClassId("Amertume")));
	
	final ClassExpr[] outcome = new ClassExpr[] {
	    new ClassExpr(expressions, Operator.OR, Collections.EMPTY_SET),
	    new ClassExpr(expressions, Operator.AND, Collections.EMPTY_SET),
	    new ClassExpr(
			  Collections
			  .singleton((ExpressionDefinition) new ClassId(
									"Acidite")), Operator.NOT,
			  Collections.EMPTY_SET),
	    new ClassExpr(expressions, Operator.OR, conditions0),
	    new ClassExpr(new ClassId("Amertume"), conditions1),
	    new ClassExpr(new ClassId("Amertume"), null) };

	try {
	    runParse(toParse, outcome, XpathParser.class.getDeclaredMethod(
									   "parseClassExpr", Node.class));
	} catch (SecurityException e) {
	    e.printStackTrace();
	} catch (NoSuchMethodException e) {
	    e.printStackTrace();
	}
    }

    @Test(groups = { "full", "omwg" }, dependsOnMethods = {"setUp"})
    public void testParsePropertyCond() {
	final String[] toParse = new String[] {
	    "<valueCondition>" + "<value>18</value>"
	    + "<comparator rdf:resource='equal'/>"
	    + "</valueCondition>",
	    "<domainRestriction>"
	    + "<Class rdf:about='http://meine/tolle/restriction'></Class>"
	    + "</domainRestriction>",
	    "<typeCondition>int</typeCondition>" };
	final Object[] outcome = new Object[] {
	    Collections.singleton(new ValueCondition(new Restriction(
								     new SimpleValue("18"), Comparator.EQUAL))),
	    Collections.singleton(new DomainPropertyCondition(
							       new Restriction(new ClassId(
											   "http://meine/tolle/restriction"),
									       Comparator.EQUAL))),
	    Collections.singleton(new TypeCondition(new Restriction(
								    new SimpleValue("int"), Comparator.EQUAL))) };
	try {
	    runParseWithNodelist(toParse, outcome, XpathParser.class
				 .getDeclaredMethod("parsePropertyCond", NodeList.class));
	} catch (SecurityException e) {
	    e.printStackTrace();
	} catch (NoSuchMethodException e) {
	    e.printStackTrace();
	}
    }

    @Test(groups = { "full", "omwg" }, dependsOnMethods = {"setUp"})
    public void testParsePropertyExpr() {
	final String[] toParse = new String[] {
	    "<Property rdf:about='http://mein/super/property'></Property>",
	    "<Property><and rdf:parseType='Collection'>"
	    + "<Property rdf:about='http://mein/super/property0'></Property>"
	    + "<Property rdf:about='http://mein/super/property1'></Property>"
	    + "</and></Property>",
	    "<Property rdf:about='http://mein/super/property'>"
	    + "<domainRestriction>"
	    + "<Class rdf:about='http://my/super/class'></Class>"
	    + "</domainRestriction>" + "</Property>",
	    "<Property><or rdf:parseType='Collection'>"
	    + "<Property rdf:about='http://mein/super/property0'></Property>"
	    + "<Property rdf:about='http://mein/super/property1'></Property>"
	    + "</or>"
	    + "<valueCondition><value>5</value>"
	    + "<comparator rdf:resource='equals'/></valueCondition>"
	    + "</Property>",
	    "<Property><not>"
	    + "<Property rdf:about='http://mein/super/property'></Property>"
	    + "</not><transf rdf:resource='http://mein/transformator'></transf></Property>" };
	
	final Set<ExpressionDefinition> expressions = new HashSet<ExpressionDefinition>(
											2);
	expressions.add(new PropertyId("http://mein/super/property0"));
	expressions.add(new PropertyId("http://mein/super/property1"));
	final PropertyId single = new PropertyId("http://mein/super/property");
	
	// TODO: do the transf!!!
	final Object[] outcome = new Object[] {
	    new PropertyExpr(single, null),
	    new PropertyExpr(expressions, Operator.AND, null),
	    new PropertyExpr(
			      single,
			      Collections
			      .singleton((PropertyCondition) new DomainPropertyCondition(
											   new Restriction(new ClassId(
														       "http://my/super/class"),
													   Comparator.EQUAL), single))),
	    new PropertyExpr(
			      expressions,
			      Operator.OR,
			      Collections
			      .singleton((PropertyCondition) new ValueCondition(
										 new Restriction(new SimpleValue("5"),
												 Comparator.EQUAL),
										 new ComplexExpression(expressions,
												       Operator.OR)))),
	    new PropertyExpr(Collections
			      .singleton((ExpressionDefinition) new PropertyId(
										"http://mein/super/property")), Operator.NOT,
			      null) };
	try {
	    runParse(toParse, outcome, XpathParser.class.getDeclaredMethod(
									   "parsePropertyExpr", Node.class));
	} catch (SecurityException e) {
	    e.printStackTrace();
	} catch (NoSuchMethodException e) {
	    e.printStackTrace();
	}
    }

    @Test(groups = { "full", "omwg" }, dependsOnMethods = {"setUp"})
    public void testParseRelationCond() {
	final String[] toParse = new String[] {
	    "<domainRestriction>"
	    + "<Class rdf:about='http://my/super/class'></Class>"
	    + "</domainRestriction>",
	    "<codomainRestriction>"
	    + "<Class rdf:about='http://my/super/class'></Class>"
	    + "</codomainRestriction>" };
	final Set[] outcome = new Set[] {
	    Collections.singleton(new DomainRelationCondition(
							      new Restriction(new ClassId("http://my/super/class"),
									      Comparator.EQUAL))),
	    Collections.singleton(new CoDomainRelationCondition(
								new Restriction(new ClassId("http://my/super/class"),
										Comparator.EQUAL))) };
	
	try {
	    runParseWithNodelist(toParse, outcome, XpathParser.class
				 .getDeclaredMethod("parseRelationCond", NodeList.class));
	} catch (SecurityException e) {
	    e.printStackTrace();
	} catch (NoSuchMethodException e) {
	    e.printStackTrace();
	}
    }

    @Test(groups = { "full", "omwg" }, dependsOnMethods = {"setUp"})
    public void testParseRelationExpr() {
	final String[] toParse = new String[] {
	    "<Relation rdf:about='http://my/super/relation'></Relation>",
	    "<Relation rdf:about='http://my/super/relation'>"
	    + "<domainRestriction>"
	    + "<Class rdf:about='http://my/super/class'></Class>"
	    + "</domainRestriction>" + "</Relation>",
	    "<Relation>"
	    + "<and rdf:parseType='Collection'>"
	    + "<Relation rdf:about='http://my/super/relation0'></Relation>"
	    + "<Relation rdf:about='http://my/super/relation1'></Relation>"
	    + "</and>" + "<domainRestriction>"
	    + "<Class rdf:about='http://my/super/class'></Class>"
	    + "</domainRestriction>" + "</Relation>",
	    "<Relation>"
	    + "<or rdf:parseType='Collection'>"
	    + "<Relation rdf:about='http://my/super/relation0'></Relation>"
	    + "<Relation rdf:about='http://my/super/relation1'></Relation>"
	    + "</or>" + "</Relation>",
	    "<Relation>"
	    + "<not>"
	    + "<Relation rdf:about='http://my/super/relation'></Relation>"
	    + "</not>" + "<codomainRestriction>"
	    + "<Class rdf:about='http://my/super/class'></Class>"
	    + "</codomainRestriction>" + "</Relation>",
	    "<Relation>"
	    + "<inverse>"
	    + "<Relation rdf:about='http://my/super/relation'></Relation>"
	    + "</inverse>" + "</Relation>",
	    "<Relation>"
	    + "<symmetric>"
	    + "<Relation rdf:about='http://my/super/relation'></Relation>"
	    + "</symmetric>" + "</Relation>",
	    "<Relation>"
	    + "<transitive>"
	    + "<Relation rdf:about='http://my/super/relation'></Relation>"
	    + "</transitive>" + "</Relation>",
	    "<Relation>"
	    + "<reflexive>"
	    + "<Relation rdf:about='http://my/super/relation'></Relation>"
	    + "</reflexive>" + "</Relation>" };
	
	final Set<RelationId> expressions = new HashSet<RelationId>(2);
	expressions.add(new RelationId("http://my/super/relation0"));
	expressions.add(new RelationId("http://my/super/relation1"));
	
	final RelationExpr[] outcome = new RelationExpr[] {
	    new RelationExpr(new RelationId("http://my/super/relation"),
			     null),
	    new RelationExpr(
			     new RelationId("http://my/super/relation"),
			     Collections
			     .singleton((RelationCondition) new DomainRelationCondition(
											new Restriction(new ClassId(
														    "http://my/super/class"),
													Comparator.EQUAL),
											new RelationId(
												       "http://my/super/relation")))),
	    new RelationExpr(
			     expressions,
			     Operator.AND,
			     Collections
			     .singleton((RelationCondition) new DomainRelationCondition(
											new Restriction(new ClassId(
														    "http://my/super/class"),
													Comparator.EQUAL),
											new ComplexExpression(expressions,
													      Operator.AND)))),
	    new RelationExpr(expressions, Operator.OR, null),
	    new RelationExpr(
			     Collections.singleton(new RelationId(
								  "http://my/super/relation")),
			     Operator.NOT,
			     Collections
			     .singleton((RelationCondition) new CoDomainRelationCondition(
											  new Restriction(new ClassId(
														      "http://my/super/class"),
													  Comparator.EQUAL),
											  new ComplexExpression(
														Collections
														.singleton(new RelationId(
																	  "http://my/super/relation")),
														Operator.NOT)))),
	    new RelationExpr(Collections.singleton(new RelationId(
								  "http://my/super/relation")), Operator.INVERSE, null),
	    new RelationExpr(Collections.singleton(new RelationId(
								  "http://my/super/relation")), Operator.SYMMETRIC, null),
	    new RelationExpr(Collections.singleton(new RelationId(
								  "http://my/super/relation")), Operator.TRANSITIVE, null),
	    new RelationExpr(Collections.singleton(new RelationId(
								  "http://my/super/relation")), Operator.REFLEXIVE, null) };
	
	try {
	    runParse(toParse, outcome, XpathParser.class.getDeclaredMethod(
									   "parseRelationExpr", Node.class));
	} catch (SecurityException e) {
	    e.printStackTrace();
	} catch (NoSuchMethodException e) {
	    e.printStackTrace();
	}
    }

    @Test(groups = { "full", "omwg" }, dependsOnMethods = {"setUp"})
    public void testParseCell() {
	final String[] toParse = new String[] {
	    
	    "<Cell>"
	    + "<entity1><Class rdf:about='http://my/super/class0'></Class></entity1>"
	    + "<entity2><Class rdf:about='http://my/super/class1'></Class></entity2>"
	    + "<measure>0.5</measure>"
	    + "<relation>ClassEquivalence</relation>" + "</Cell>",
	    "<Cell>"
	    + "<entity1><Relation rdf:about='http://my/super/attribute'></Relation></entity1>"
	    + "<entity2><Class rdf:about='http://my/super/instance'></Class></entity2>"
	    + "<measure>0.3</measure>"
	    + "<relation>RelationClassMapping</relation>"
	    + "</Cell>" };
	final Object[] outcome = new Object[] {
	    new Class2Class(null, Direction.EQUIVALENCE, new ClassExpr(
								       new ClassId("http://my/super/class0"), null),
			    new ClassExpr(new ClassId("http://my/super/class1"),
					  null), .5f),
	    new Relation2Class(null, Direction.MAPPING, new RelationExpr(
									 new RelationId("http://my/super/attribute"), null),
			       new ClassExpr(new ClassId("http://my/super/instance"),
					     null), .3f) };
	try {
	    runParse(toParse, outcome, XpathParser.class.getDeclaredMethod(
									   "parseCell", Node.class));
	} catch (SecurityException e) {
	    e.printStackTrace();
	} catch (NoSuchMethodException e) {
	    e.printStackTrace();
	}
    }

    @Test(groups = { "full", "omwg" }, dependsOnMethods = {"setUp"})
    public void testParseAlignment() {
	final String[] toParse = new String[] {
	    "<Alignment>" + "<dc:identifier rdf:resource='http://asdf'/>"
	    + "<onto1>"
	    + "<formalism name='wsml' uri='http://wsml'/>"
	    + "<uri>http://source</uri>" + "</onto1>" + "<onto2>"
	    + "<formalism name='wsml' uri='http://wsml'/>"
	    + "<uri>http://target</uri>" + "</onto2>"
	    + "</Alignment>",
	    "<Alignment>" + "<dc:identifier rdf:resource='http://asdf'/>"
	    + "<onto1>"
	    + "<formalism name='wsml' uri='http://wsml'/>"
	    + "<uri>http://source</uri>" + "</onto1>" + "<onto2>"
	    + "<formalism name='wsml' uri='http://wsml'/>"
	    + "<uri>http://target</uri>" + "</onto2>"
	    + "<level>2oml</level>" + "<type>**</type>"
	    + "</Alignment>" };
	
	final MappingDocument doc0 = new MappingDocument(
							 new URI("http://asdf"), new OntologyId(
												new URI("http://source"), "wsml",
												new URI("http://wsml")), new OntologyId(new URI(
																		"http://target"), "wsml", new URI("http://wsml")));
	final MappingDocument doc1 = new MappingDocument(
							 new URI("http://asdf"), new OntologyId(
												new URI("http://source"), "wsml",
												new URI("http://wsml")), new OntologyId(new URI(
																		"http://target"), "wsml", new URI("http://wsml")));
	doc1.setLevel(Level.LEVEL2OML);
	doc1.setArity(Arity.NONE_NONE);
	
	final Object[] outcome = new Object[] { doc0, doc1 };

	try {
	    runParse(toParse, outcome, XpathParser.class.getDeclaredMethod(
									   "parseAlignment", Node.class));
	} catch (SecurityException e) {
	    e.printStackTrace();
	} catch (NoSuchMethodException e) {
	    e.printStackTrace();
	}
    }

    @Test(groups = { "full", "omwg" }, dependsOnMethods = {"setUp"})
    public void testParsePov() {
	// parse path
	try {
	    String toParse = "<parent><Property rdf:about='http://my/super/uri'/></parent>";
	    Document doc = BUILDER.parse(new ByteArrayInputStream(toParse
								  .getBytes()));
	    assertEquals("Couldn't parse a path pov", new Path(
							       "http://my/super/uri"), XpathParser.parsePov(doc
													    .getFirstChild()));
	} catch (SAXException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	}
	// parse uri
	try {
	    String toParse = "<parent>http://my/super/pov</parent>";
	    Document doc = BUILDER.parse(new ByteArrayInputStream(toParse
								  .getBytes()));
	    assertEquals("Couldn't parse a path pov", new URI(
							      "http://my/super/pov"), XpathParser.parsePov(doc
													   .getFirstChild()));
	} catch (SAXException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	}
	// parse value
	try {
	    String toParse = "<parent>5</parent>";
	    Document doc = BUILDER.parse(new ByteArrayInputStream(toParse
								  .getBytes()));
	    assertEquals("Couldn't parse a path pov", new SimpleValue("5"),
			 XpathParser.parsePov(doc.getFirstChild()));
	} catch (SAXException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    @Test(groups = { "full", "omwg" }, dependsOnMethods = {"setUp"})
    public void testExotic() {
	final String toParse = "<Class>" + "<and rdf:parseType='Collection'>"
	    + "<Class rdf:about='c1'>" + "<attributeValueCondition>"
	    + "<Restriction>" + "<onAttribute>"
	    + "<Attribute rdf:about='a1'/>" + "</onAttribute>"
	    + "<comparator rdf:resource='equal'/>" + "<value>val1</value>"
	    + "</Restriction>" + "</attributeValueCondition>" + "</Class>"
	    + "<Class rdf:about='c2'>" + "<attributeValueCondition>"
	    + "<Restriction><onAttribute>" + "<Attribute rdf:about='a2'/>"
	    + "</onAttribute>" + "<comparator rdf:resource='equal'/>"
	    + "<value>val2</value>" + "</Restriction>"
	    + "</attributeValueCondition>" + "</Class>" + "</and>"
	    + "<attributeValueCondition>" + "<Restriction><onAttribute>"
	    + "<Attribute rdf:about='a0'/>" + "</onAttribute>"
	    + "<comparator rdf:resource='equal'/>" + "<value>val0</value>"
	    + "</Restriction>" + "</attributeValueCondition>" + "</Class>";
	
	ClassId c1 = new ClassId("c1");
	ClassId c2 = new ClassId("c2");
	ComplexExpression combined = new ComplexExpression(Arrays
							   .asList(new ClassId[] { c1, c2 }), Operator.AND);
	ClassCondition con0 = new AttributeValueCondition(new Path("a0"),
							  new Restriction(new SimpleValue("val0"), Comparator.EQUAL),
							  combined);
	ClassCondition con1 = new AttributeValueCondition(new Path("a1"),
							  new Restriction(new SimpleValue("val1"), Comparator.EQUAL), c1);
	ClassCondition con2 = new AttributeValueCondition(new Path("a2"),
							  new Restriction(new SimpleValue("val2"), Comparator.EQUAL), c2);
	ClassExpr ce = new ClassExpr(combined, Arrays
				     .asList(new ClassCondition[] { con0, con1, con2 }));
	try {
	    ByteArrayInputStream is = new ByteArrayInputStream(toParse
							       .getBytes());
	    Document d = BUILDER.parse(is);
	    assertEquals(ce, XpathParser.parseClassExpr(d.getFirstChild()));
	} catch (SAXException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    @Test(groups = { "full", "omwg" }, dependsOnMethods = {"setUp"})
    public static void runParse(final String[] toParse, final Object[] outcome,
				final Method method) {
	int counter = 0;
	for (final String xml : toParse) {
	    final ByteArrayInputStream is = new ByteArrayInputStream(xml
								     .getBytes());
	    try {
		final Document doc = BUILDER.parse(is);
		final Object o = method.invoke(null, doc.getFirstChild());
		assertEquals("Couldn't parse: " + xml, outcome[counter++], o);
	    } catch (SAXException e) {
		e.printStackTrace();
	    } catch (IOException e) {
		e.printStackTrace();
	    } catch (IllegalArgumentException e) {
		e.printStackTrace();
	    } catch (IllegalAccessException e) {
		e.printStackTrace();
	    } catch (InvocationTargetException e) {
		e.printStackTrace();
	    } finally {
		try {
		    is.close();
		} catch (IOException e) {
		    e.printStackTrace();
		}
	    }
	}
    }
    
    @Test(groups = { "full", "omwg" }, dependsOnMethods = {"setUp"})
    public static void runParseWithNodelist(final String[] toParse,
					    final Object[] outcome, final Method method) {
	int counter = 0;
	for (final String xml : toParse) {
	    final ByteArrayInputStream is = new ByteArrayInputStream(xml
								     .getBytes());
	    try {
		final Document doc = BUILDER.parse(is);
		final Object o = method.invoke(null, doc.getChildNodes());
		assertEquals("Couldn't parse: " + xml, outcome[counter++], o);
	    } catch (SAXException e) {
		e.printStackTrace();
	    } catch (IOException e) {
		e.printStackTrace();
	    } catch (IllegalArgumentException e) {
		e.printStackTrace();
	    } catch (IllegalAccessException e) {
		e.printStackTrace();
	    } catch (InvocationTargetException e) {
		e.printStackTrace();
	    } finally {
		try {
		    is.close();
		} catch (IOException e) {
		    e.printStackTrace();
		}
	    }
	}
    }
*/

}
