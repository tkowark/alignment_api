/*
 * $Id$
 *
 * Copyright (C) 2006, INRIA Rhône-Alpes
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 */

// Alignment API classes
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Parameters;
import org.semanticweb.owl.align.Evaluator;

// Alignment API implementation classes
import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.BasicParameters;
import fr.inrialpes.exmo.align.impl.method.StringDistAlignment;
import fr.inrialpes.exmo.align.impl.renderer.SWRLRendererVisitor;
import fr.inrialpes.exmo.align.impl.eval.PRecEvaluator;
import fr.inrialpes.exmo.align.parser.AlignmentParser;

// OWL API classes
import org.semanticweb.owl.util.OWLManager;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.io.owl_rdf.OWLRDFParser;
import org.semanticweb.owl.io.owl_rdf.OWLRDFErrorHandler;

// SAX standard classes
import org.xml.sax.SAXException;

// Java standard classes
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.File;
import java.net.URI;
import java.util.Hashtable;

/**
 * The Skeleton of code for embeding the alignment API
 *
 * Takes two files as arguments and align them.
 */

public class MyApp {

    static OWLRDFErrorHandler handler = null;
    static Hashtable loaded = null;

    public static void main( String[] args ) {
	OWLOntology onto1 = null;
	OWLOntology onto2 = null;
	Parameters params = new BasicParameters();

	try {
	    // Initializing ontology parsers
	    initErrorHandler();
	    loaded = new Hashtable();
	
	    // Loading ontologies
	    if (args.length >= 2) {
		onto1 = loadOntology(args[0]);
		onto2 = loadOntology(args[1]);
	    } else {
		System.err.println("Need two arguments to proceed");
		return ;
	    }

	    // Run two diffent alignment methods (e.g., ngram distance and smoa)
	    AlignmentProcess a1 = new StringDistAlignment( onto1, onto2 );
	    params.setParameter("stringFunction","smoaDistance");
	    a1.align( (Alignment)null, params );
	    AlignmentProcess a2 = new StringDistAlignment( onto1, onto2 );
	    params = new BasicParameters();
	    params.setParameter("stringFunction","ngramDistance");
	    a1.align( (Alignment)null, params );

	    // Merge the two results.
	    ((BasicAlignment)a1).ingest(a2);

	    // Test its f-measure.
	    AlignmentParser aparser = new AlignmentParser(0);
	    Alignment reference = aparser.parse( "file://localhost"+(new File ( "refalign.rdf" ) . getAbsolutePath()), loaded );
	    Evaluator evaluator = new PRecEvaluator( reference, a1 );

	    // Threshold at various thresholds (and maybe various threshold methods)
	    for ( int i = 0; i <= 10 ; i = i+2 ){
		a1.cut( ((double)i)/10 );
		evaluator.eval( new BasicParameters() );
		System.err.println("Threshold "+(((double)i)/10)+" : "+((PRecEvaluator)evaluator).getFmeasure());
		}

	    // If it is over a certain value, output the result as SWRL rules.
	    PrintWriter writer = new PrintWriter (
				  new BufferedWriter(
		                   new OutputStreamWriter( System.out, "UTF-8" )), true);
	    AlignmentVisitor renderer = new SWRLRendererVisitor(writer);
	    a1.render(renderer);
	    writer.flush();
	    writer.close();

	} catch (Exception e) { e.printStackTrace(); };
    }

    // Initializes RDF Parser error handlers
    private static void initErrorHandler() throws Exception {
	try {
	    handler = new OWLRDFErrorHandler() {
		    public void owlFullConstruct(int code, String message)
			throws SAXException {
		    }
		public void owlFullConstruct(int code, String message, Object o)
		    throws SAXException {
		}
		    public void error(String message) throws SAXException {
			throw new SAXException(message.toString());
		    }
		    public void warning(String message) throws SAXException {
			System.err.println("WARNING: " + message);
		    }
		};
	} catch (Exception ex) {
	    throw ex;
	}
    }

    // Load an ontology in the OWL API
    public static OWLOntology loadOntology(String filename)
	throws Exception {
	URI uri = new URI ("file://localhost"+(new File ( filename ) . getAbsolutePath()));
	if (uri == null) { throw new OWLException( "Bad filename" ); }
	OWLRDFParser parser = new OWLRDFParser();
	parser.setOWLRDFErrorHandler(handler);
	parser.setConnection(OWLManager.getOWLConnection());
	OWLOntology parsedOnt = parser.parseOntology(uri);
	loaded.put( uri, parsedOnt );
	return parsedOnt;
    }

}
