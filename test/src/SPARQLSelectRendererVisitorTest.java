
import fr.inrialpes.exmo.align.impl.edoal.EDOALAlignment;
import fr.inrialpes.exmo.align.impl.renderer.SPARQLSelectRendererVisitor;
import fr.inrialpes.exmo.align.test.Utils;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.util.Enumeration;
import java.util.Properties;
import org.semanticweb.owl.align.Cell;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;
import org.testng.annotations.Test;

/**
 *
 * @author Nicolas Guillouet <nicolas.guillouet@inria.fr>
 */
public class SPARQLSelectRendererVisitorTest {

    @Test(groups = {"full", "impl", "raw"})
    public void QueryFromSimpleLinkkey() throws Exception {
        String alignmentFileName = "alignment2.rdf";
        EDOALAlignment alignment = Utils.loadAlignement(alignmentFileName);
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        SPARQLSelectRendererVisitor renderer = new SPARQLSelectRendererVisitor(writer);
        Properties properties = new Properties();
        renderer.init(properties);
        alignment.render(renderer);
        assertEquals(alignment.nbCells(), 1);
        Enumeration<Cell> cells = alignment.getElements();
        Cell cell = cells.nextElement();
        URI remoteURI = new URI("http://example.org/data.rdf");
	// THIS DOES THE edoal:type="in" How to do equal?
        String expectedQuery1 = "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
                + "PREFIX ns0:<http://exmo.inria.fr/connectors#>\n"
                + "PREFIX ns1:<http://purl.org/ontology/mo/>\n"
	    //+ "CONSTRUCT { ?s1 owl:sameAs ?s2 }"
	        + "SELECT ?s1 ?s2"
                + "WHERE {\n"
                + "?s1 rdf:type ns0:RootElement .\n"
                + "?s1 ns0:number ?o1 ."
                + "SERVICE <http://example.org/data.rdf> {"
                + "?s2 rdf:type ns1:MusicalWork . \n"
                + "?s2 ns1:opus ?o1"
                + "}\n"
                + "}\n";

        String expectedQuery2 = "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
                + "PREFIX ns0:<http://exmo.inria.fr/connectors#>\n"
                + "PREFIX ns1:<http://purl.org/ontology/mo/>\n"
                + "CONSTRUCT ?s1 ?s2"
                + "WHERE {\n"
                + "?s2 rdf:type ns1:MusicalWork . \n"
                + "?s2 ns1:opus ?o1"
                + "SERVICE <http://example.org/data.rdf> {"
                + "?s1 rdf:type ns0:RootElement .\n"
                + "?s1 ns0:number ?o1 ."
                + "}\n"
                + "}\n";
        assertEquals(renderer.getQuery(cell, 0, remoteURI), expectedQuery1, "FOR alignment file " + alignmentFileName);
        assertEquals(renderer.getQuery(cell, 1, remoteURI), expectedQuery2, "FOR alignment file " + alignmentFileName);
        fail("HAVE TODO");
    }

    @Test(groups = {"full", "impl", "raw"})
    public void HaveTodo() throws Exception {
        fail("HAVE TODO : with (in + eq) => OPTIONAL (with SERVICE call on remote) / many correspondances / With transformations / On local (FROM ...) or remote sparql endpoint (SERVICE) ");
    }

}
