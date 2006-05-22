/*
 * Result.java
 *
 * Created on March 20, 2006, 10:55 AM
 *
 */

package fr.inrialpes.exmo.queryprocessor;

import java.util.Collection;

/**
 *
 * @author Arun Sharma
 */
public interface Result {
    /**@returns the type of the result set
     */
    public int getType();
    
    /**@returns the reslut for ASK type queries
     */
    public boolean getAskResult() throws QueryTypeMismatchException;
    
    /**
     *@returns the RDF graph for construct queries
     */
    public RDFGraph getConstructResult() throws QueryTypeMismatchException;
    
    /**@returns a collection set for SELECT queries
     */
    public Collection getSelectResult() throws QueryTypeMismatchException;

    /**@returns an XML string for the SELECT queries
     */
    public String getSelectResultasXML() throws QueryTypeMismatchException;

}
