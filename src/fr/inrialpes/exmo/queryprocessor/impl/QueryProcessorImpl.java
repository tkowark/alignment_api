package fr.inrialpes.exmo.queryprocessor.impl;

/**
 * @author Arun Sharma
 */

import java.util.Set;
import java.util.HashSet;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QueryExecution;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Model;

import fr.inrialpes.exmo.queryprocessor.Result;
import fr.inrialpes.exmo.queryprocessor.Type;
import fr.inrialpes.exmo.queryprocessor.QueryProcessor;
import fr.inrialpes.exmo.queryprocessor.impl.ResultImpl;
/**
 *
 * @author Arun Sharma
 */
public class QueryProcessorImpl implements QueryProcessor {
    
    Model aModel;
    ResultSet aResultSet;
    Set aResults = new HashSet();    
    
    public QueryProcessorImpl()  {
        
    }
    public QueryProcessorImpl(Model m) {        
        aModel = m;    
    }
    
    public Result query(String query)  {        
        Query myquery = QueryFactory.create(query);
	QueryExecution qe = QueryExecutionFactory.create(myquery, aModel);		
        System.err.println("Executing  query:   " + query);               
        aResultSet = qe.execSelect();
        
         while (aResultSet.hasNext())
                {
                    QuerySolution aSolution = aResultSet.nextSolution();
                    Resource aRes = (Resource)aSolution.get("uri");
                    aResults.add(aRes);                    
                }
        
        qe.close();        
        ResultImpl res = new ResultImpl(aResults);
        return res;
    }
    
    public Result query(String query, Type type)  {
        return null;
    }
    
    public String queryWithStringResults(String query)  {
        return null;
    }
    
    public int getType(String query)  {
        return 0;
    }
    
    public void loadOntology(String uri)  {
        
    }
}
