package fr.inrialpes.exmo.QueryProcessor.impl;

/**
 * @author Arun Sharma
 */

import java.util.Set;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import java.net.URL;
import java.io.File;
import java.io.FileInputStream;
import java.io.DataInputStream;
import java.io.PrintStream;
import java.io.FileOutputStream;
import java.io.StringWriter;

import org.mindswap.pellet.output.TableData;
import org.mindswap.pellet.output.OutputFormatter;
import fr.inrialpes.exmo.elster.picster.markup.Razor;
import fr.inrialpes.exmo.elster.picster.markup.MarkupModel;
import org.mindswap.pellet.output.ATermAbstractSyntaxRenderer;
import fr.inrialpes.exmo.elster.picster.*;
import org.mindswap.pellet.output.ATermRenderer;

import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.ResultSetFormatter;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import org.mindswap.markup.media.MarkupImageModel;

import fr.inrialpes.exmo.QueryProcessor.Result;
import fr.inrialpes.exmo.QueryProcessor.Type;
import fr.inrialpes.exmo.QueryProcessor.QueryProcessor;
import fr.inrialpes.exmo.QueryProcessor.impl.ResultImpl;
/**
 *
 * @author Arun Sharma
 */
public class QueryProcessorImpl implements QueryProcessor {
    
    String query;
    Model aModel;
    ResultSet aResultSet;
    Set aResults = new HashSet();
    Hashtable annotation = new Hashtable();
    
    public QueryProcessorImpl()  {
        
    }
    public QueryProcessorImpl(Model m) {        
        aModel = m;    
    }
    
    public Result query(String aQuery)  {
        query = aQuery;
        Query myquery = QueryFactory.create(query);
	QueryExecution qe = QueryExecutionFactory.create(myquery, aModel);		
        System.err.println("Executing  query:   " + query);               
        aResultSet = qe.execSelect();
        
         while (aResultSet.hasNext())
                {
                    QuerySolution aSolution = aResultSet.nextSolution();
                    Resource aRes = (Resource)aSolution.get("uri");
                    aResults.add(aRes.getURI());
                    getAnnotations(aRes, aRes.getURI());
                    Property p = aModel.getProperty("http://www.mindswap.org/~glapizco/technical.owl#", "depiction");
                    StmtIterator stmtr = aRes.listProperties(p);
                    //String imguri = "";
                    String imaAnnos = "";
                    while(stmtr.hasNext())  {
                        Statement st = stmtr.nextStatement();
                        String imguri = st.getObject().toString();
                        System.out.println("Depiction URI = " + imguri);
                        if(!aResults.contains(imguri))
                            aResults.add(imguri);
                        String thisImgAnno = getAnnotations(aRes, imguri);
                        imaAnnos = imaAnnos + thisImgAnno + "<image>";
                    }
                    //getAnnotations(aRes, imguri);
                    annotation.put(aRes.getURI(), imaAnnos);
                }
        
        qe.close();        
        ResultImpl res = new ResultImpl(aResults);
        return res;
    }
    
   
    public ResultSet performQuery()  {
        Query myquery = QueryFactory.create(query);
	QueryExecution qe = QueryExecutionFactory.create(myquery, aModel);		
        System.err.println("Executing  query:   " + query);               
        aResultSet = qe.execSelect();
        
         while (aResultSet.hasNext())
                {
                    QuerySolution aSolution = aResultSet.nextSolution();
                    Resource aRes = (Resource)aSolution.get("uri");
                    aResults.add(aRes.getURI());
                    getAnnotations(aRes, aRes.getURI());
                    Property p = aModel.getProperty("http://www.mindswap.org/~glapizco/technical.owl#", "depiction");
                    StmtIterator stmtr = aRes.listProperties(p);
                    //String imguri = "";
                    String imaAnnos = "";
                    while(stmtr.hasNext())  {
                        Statement st = stmtr.nextStatement();
                        String imguri = st.getObject().toString();
                        System.out.println("Depiction URI = " + imguri);
                        if(!aResults.contains(imguri))
                            aResults.add(imguri);
                        String thisImgAnno = getAnnotations(aRes, imguri);
                        imaAnnos = imaAnnos + thisImgAnno + "<image>";
                    }
                    //getAnnotations(aRes, imguri);
                    annotation.put(aRes.getURI(), imaAnnos);
                }
        
        qe.close();
        return aResultSet;
    }
    
    public String getAnnotations(Resource res, String mediaUri)  {
        String queryAnno = "";
        String uri = res.getURI();
        String instanceFileName = uri.substring(uri.lastIndexOf('/')+1, uri.lastIndexOf('.')) + ".rdf";
        String annoFileName = "";
        if(!uri.equals(mediaUri))  {
            if(mediaUri.contains("region"))  {
             String temp = mediaUri.substring(0, mediaUri.indexOf("region")-1);
             annoFileName = temp.substring(temp.lastIndexOf('/')+1, temp.length() ) + ".rdf";
             }
             else
               annoFileName = mediaUri.substring(mediaUri.lastIndexOf('/')+1, mediaUri.length()) + ".rdf";
        }
        System.out.println("\n\n\nInstanceFileName = " + instanceFileName);
        System.out.println("\n\n\nAnnotationFileName = " + annoFileName);
        
        try  {
        FileInputStream in = new FileInputStream(new File("annotations/" + annoFileName));
        DataInputStream ds = new DataInputStream(in);
        String annotations = "";
        while(ds.available() != 0) {
            annotations = annotations + ds.readLine();
            annotations = annotations + "\n";
        }       
        OntModel m = ModelFactory.createOntologyModel();
        StmtIterator stms = res.listProperties();
        m.add(stms);
        StringWriter sr  = new StringWriter();
        m.write(sr, "RDF/XML-ABBREV");
        String instance = sr.toString();        
      /*  queryAnno = "<" + annoFileName + ">" + annotations + "</" + annoFileName + ">"
                                + "<" + instanceFileName + ">" + instance + "</" + instanceFileName + ">";               
        */        
        queryAnno = annoFileName + "<divider>" +annotations + "<divider>"+ instanceFileName + "<divider>" + instance;
        } catch(Exception e)  {
            System.err.println("Annotation found not found");
        }
         //annotation.put(mediaUri, queryAnno);
        return queryAnno;
    }
    
    public String getAnnotationforUri(String uri)  {
        String aAnno = (String) annotation.get(uri);
        return aAnno;
    }
    
    public Set getResourceUrisAsSet(ResultSet aResultSet)  {
       /* Set aResults = new HashSet();
        while (aResultSet.hasNext())
                {
                }*/
        return aResults;
    }
        
    public void showImages(Set mResults, Razor myApp)  {
        System.out.println("Reached show Images");
        Vector imgUrls = new Vector();
        Iterator itr = mResults.iterator();
        while(itr.hasNext())  {
            String uri = (String) itr.next();
            String imageLocation = (String)MarkupImageModel.getUriMap().get(uri);
            if(imageLocation == null)
                imageLocation = (String)MarkupImageModel.getRegUriMap().get(uri);
            imgUrls.add(imageLocation);
        }
        
        for(int j = 0; j < imgUrls.size(); j++)  {
            try  {
                String url = (String) imgUrls.get(j);
                System.out.println("Image Location = " + url);
                URL aUrl = new URL(url);
                myApp.loadURL(aUrl);
            } catch(Exception e)  {
                System.out.println("Error in image location");
            }
        }
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
