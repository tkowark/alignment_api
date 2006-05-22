/*
 * QueryMediator.java
 *
 * Created on May 20, 2006, 12:15 AM
 *
 */

package fr.inrialpes.exmo.align.service;

import fr.inrialpes.exmo.queryprocessor.impl.QueryProcessorImpl;
import org.semanticweb.owl.align.Alignment;

/**
 *
 * @author Arun Sharma
 */
public class QueryMediator extends QueryProcessorImpl {
    
    private String query;
    private Alignment alignment;
    
    public QueryMediator(String aQuery) {
        query = aQuery;
    }
    
    public QueryMediator(Alignment a, String aQuery)  {
        query = aQuery;
        alignment = a;
    }
    
    /**
     * @aQuery query to be re-written
     * @ a Alignment
     * @ return -- rewritten query, Current code just replaces all the prefix namespaces, if present, in the query by actual IRIs
     * TODO: rewrite the mainQuery variable (which is currently returned) to a query based on the given alignment     
     */    
    public String rewriteQuery(String aQuery, Alignment a)  {
        aQuery = aQuery.toLowerCase();
        String mainQuery = ""; 
        if(aQuery.contains("prefix"))  {
            String[] pref = aQuery.split("prefix");               
            for(int j =0; j < pref.length; j++)  {
                String str = "";
                if(!pref[0].equals(""))   
                    str = pref[0];
                else
                    str = pref[pref.length-1];
                mainQuery = str.substring(str.indexOf('>') +1, str.length());
            }
                
            for(int i = 0; i < pref.length; i++)  {       
                String currPrefix = pref[i].trim();       
                if(!currPrefix.equals("") && currPrefix.indexOf('<') != -1 && currPrefix.indexOf('>') != -1)  {
                    int begin = currPrefix.indexOf('<');
                    int end = currPrefix.indexOf('>');
                    String ns = currPrefix.substring(0, begin).trim();
                    String iri = currPrefix.substring(begin+1, end).trim();         
                    mainQuery = mainQuery.replaceAll(ns, iri);            
                }
            }
        }
        
        else
            mainQuery = aQuery;
        return mainQuery;
    }
    
}
