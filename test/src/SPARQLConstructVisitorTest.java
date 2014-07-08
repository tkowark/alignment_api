
import fr.inrialpes.exmo.align.impl.edoal.ClassId;
import fr.inrialpes.exmo.align.impl.edoal.EDOALAlignment;
import fr.inrialpes.exmo.align.impl.edoal.EDOALCell;
import fr.inrialpes.exmo.align.impl.edoal.Expression;
import fr.inrialpes.exmo.align.impl.edoal.PropertyId;
import fr.inrialpes.exmo.align.impl.rel.EquivRelation;
import fr.inrialpes.exmo.align.impl.renderer.SPARQLConstructRendererVisitor;
import fr.inrialpes.exmo.align.test.Utils;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.util.Enumeration;
import java.util.Properties;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.Relation;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Nicolas Guillouet <nicolas.guillouet@inria.fr>
 */
public class SPARQLConstructVisitorTest {

    // Read the alignement that will be rendered by everyone
    @BeforeClass(groups = {"full", "impl", "raw"})
    private void init() throws Exception {
    }

    /**
     * Where we test SPARQL Queries Construction for simple relation (beetween
     * two properties)
     *
     * @throws Exception
     */
    @Test(groups = {"full", "impl", "raw"})
    public void ConstructSimplePropertiesRelation() throws Exception {
        EDOALAlignment alignment = new EDOALAlignment();
        Relation opusRelation = new EquivRelation();
        Expression opusExpression1 = new PropertyId(new URI("http://exmo.inrialpes.fr/connectors#opus"));
        Expression opusExpression2 = new PropertyId(new URI("http://purl.org/ontology/mo/opus"));
        EDOALCell opusCell = new EDOALCell("1", opusExpression1, opusExpression2, opusRelation, 1.0);
        alignment.addAlignCell(opusCell);

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        SPARQLConstructRendererVisitor renderer = new SPARQLConstructRendererVisitor(writer);
        Properties properties = new Properties();
        renderer.init(properties);
        alignment.render(renderer);

        String expectedQuery1 = "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
                + "PREFIX ns0:<http://exmo.inrialpes.fr/connectors#>\n"
                + "PREFIX ns1:<http://purl.org/ontology/mo/>\n"
                + "CONSTRUCT {\n"
                + "?s ns0:opus ?o .\n"
                + "}\n"
                + "WHERE {\n"
                + "?s ns1:opus ?o .\n"
                + "}\n";

        String expectedQuery2 = "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
                + "PREFIX ns0:<http://exmo.inrialpes.fr/connectors#>\n"
                + "PREFIX ns1:<http://purl.org/ontology/mo/>\n"
                + "CONSTRUCT {\n"
                + "?s ns1:opus ?o .\n"
                + "}\n"
                + "WHERE {\n"
                + "?s ns0:opus ?o .\n"
                + "}\n";
        assertEquals(renderer.getQuery(opusCell, 0), expectedQuery1);
        assertEquals(renderer.getQuery(opusCell, 1), expectedQuery2);
        
        //For remote sparql endpoint : 
        
        
        String remoteServiceURIName = "http://example.org/remoteSparql";
        URI remoteServiceURI = new URI(remoteServiceURIName);
        expectedQuery1 = String.format("PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
                + "PREFIX ns0:<http://exmo.inrialpes.fr/connectors#>\n"
                + "PREFIX ns1:<http://purl.org/ontology/mo/>\n"
                + "CONSTRUCT {\n"
                + "?s ns0:opus ?o .\n"
                + "}\n"
                + "WHERE {\n"
                + "SERVICE <%s> {\n"
                + "?s ns1:opus ?o .\n"
                + "}\n"
                + "}\n", remoteServiceURIName);

        expectedQuery2 = String.format("PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
                + "PREFIX ns0:<http://exmo.inrialpes.fr/connectors#>\n"
                + "PREFIX ns1:<http://purl.org/ontology/mo/>\n"
                + "CONSTRUCT {\n"
                + "?s ns1:opus ?o .\n"
                + "}\n"
                + "WHERE {\n"
                + "SERVICE <%s> {\n"
                + "?s ns0:opus ?o .\n"
                + "}\n"
                + "}\n", remoteServiceURIName);
        assertEquals(renderer.getQuery(opusCell, 0, remoteServiceURI), expectedQuery1);
        assertEquals(renderer.getQuery(opusCell, 1, remoteServiceURI), expectedQuery2);
    }

    /**
     * Where we test SPARQL Queries Construction for classes relation
     *
     * @throws Exception
     */
    @Test(groups = {"full", "impl", "raw"})
    public void ConstructSimpleClassesRelation() throws Exception {
        EDOALAlignment alignment = new EDOALAlignment();
        Relation classesRelation = new EquivRelation();
        Expression rootElementExpression = new ClassId(new URI("http://exmo.inrialpes.fr/connectors#RootElement"));
        Expression musicalWorkExpression = new ClassId(new URI("http://purl.org/ontology/mo/MusicalWork"));
        EDOALCell classCell = new EDOALCell("1", rootElementExpression, musicalWorkExpression, classesRelation, 1.0);
        alignment.addAlignCell(classCell);

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        SPARQLConstructRendererVisitor renderer = new SPARQLConstructRendererVisitor(writer);
        Properties properties = new Properties();
        renderer.init(properties);
        alignment.render(renderer);

        String expectedQuery1 = "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
                + "PREFIX ns0:<http://exmo.inrialpes.fr/connectors#>\n"
                + "PREFIX ns1:<http://purl.org/ontology/mo/>\n"
                + "CONSTRUCT {\n"
                + "?s rdf:type ns0:RootElement .\n"
                + "}\n"
                + "WHERE {\n"
                + "?s rdf:type ns1:MusicalWork .\n"
                + "}\n";

        String expectedQuery2 = "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
                + "PREFIX ns0:<http://exmo.inrialpes.fr/connectors#>\n"
                + "PREFIX ns1:<http://purl.org/ontology/mo/>\n"
                + "CONSTRUCT {\n"
                + "?s rdf:type ns1:MusicalWork .\n"
                + "}\n"
                + "WHERE {\n"
                + "?s rdf:type ns0:RootElement .\n"
                + "}\n";
        assertEquals(renderer.getQuery(classCell, 0), expectedQuery1);
        assertEquals(renderer.getQuery(classCell, 1), expectedQuery2);
    }

    @Test(groups = {"full", "impl", "raw"})
    public void ConstructComposePropertyRelation() throws Exception {
        String[] alignmentFilesNames = {"alignment1.rdf"};
        for (String alignmentFileName : alignmentFilesNames) {
            EDOALAlignment alignment = Utils.loadAlignement(alignmentFileName);
            StringWriter stringWriter = new StringWriter();
            PrintWriter writer = new PrintWriter(stringWriter);
            SPARQLConstructRendererVisitor renderer = new SPARQLConstructRendererVisitor(writer);
            Properties properties = new Properties();
            renderer.init(properties);
            alignment.render(renderer);
            assertEquals(alignment.nbCells(), 1);
            Enumeration<Cell> cells = alignment.getElements();
            Cell cell = cells.nextElement();

            String expectedQuery1 = "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
                    + "PREFIX ns1:<http://purl.org/NET/c4dm/keys.owl#>\n"
                    + "PREFIX ns0:<http://exmo.inrialpes.fr/connectors#>\n"
                    + "PREFIX ns3:<http://www.w3.org/2000/01/rdf-schema#>\n"
                    + "PREFIX ns2:<http://purl.org/ontology/mo/>\n"
                    + "CONSTRUCT {\n"
                    + "?s ns0:key ?o .\n"
                    + "}\n"
                    + "WHERE {\n"
                    + "?o3 rdf:type ns1:Key .\n"
                    + "?o3 ns2:key ?o4 .\n"
                    + "?o4 ns3:label ?o .\n"
                    + "}\n";

            String expectedQuery2 = "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
                    + "PREFIX ns1:<http://purl.org/NET/c4dm/keys.owl#>\n"
                    + "PREFIX ns0:<http://exmo.inrialpes.fr/connectors#>\n"
                    + "PREFIX ns3:<http://www.w3.org/2000/01/rdf-schema#>\n"
                    + "PREFIX ns2:<http://purl.org/ontology/mo/>\n"
                    + "CONSTRUCT {\n"
                    + "_:o3 rdf:type ns1:Key .\n"
                    + "_:o3 ns2:key _:o4 .\n"
                    + "_:o4 ns3:label ?o .\n"
                    + "}\n"
                    + "WHERE {\n"
                    + "?s ns0:key ?o .\n"
                    + "}\n";
            assertEquals(renderer.getQuery(cell, 0), expectedQuery1, "FOR alignment file " + alignmentFileName);
            assertEquals(renderer.getQuery(cell, 1), expectedQuery2, "FOR alignment file " + alignmentFileName);
        }
    }

}
