/*
 * QueryProcessor.java
 *
 * Created on March 20, 2006, 10:29 AM
 *
 */

package fr.inrialpes.exmo.queryprocessor;

/**
 *
 * @author Arun Sharma
 */
public interface QueryProcessor {
    /**
     * @param query -- The query string
     * @param type -- The query type, can be one of SELECT, ASK, CONSTRUCT, or DESCRIBE
     * @returns Result, result form depends on type
     */
    public Result query(String query, Type type);
    
    /**
     *@param query  -- The query string
     */
    public Result query(String query);

    /**
     *@param query -- The query string
     *@returns query results as string
     */
    public String queryWithStringResults(String query);
    
    /**
     *@param query -- the query string
     *@returns the type of the query
     */
    public int getType(String query);
    
    public void loadOntology(String uri);
    
}
