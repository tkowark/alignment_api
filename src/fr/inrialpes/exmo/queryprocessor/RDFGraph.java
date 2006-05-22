/*
 * RDFGraph.java
 *
 * Created on March 20, 2006, 11:10 AM
 *
 */

package fr.inrialpes.exmo.queryprocessor;

/**
 *
 * @author Arun Sharma
 */
public interface RDFGraph {
    /**
     *@returns RDF/XML representation of the graph
     */
    public String getXML();
    
    /**@returns rdf triples
     */
    public Triple[] getTriples();
    
    //TODO: getN3();
    //TODO: some JenaRepresentation
}
