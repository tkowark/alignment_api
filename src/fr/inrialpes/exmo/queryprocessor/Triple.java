/*
 * Triple.java
 *
 * Created on March 20, 2006, 11:12 AM
 *
 */

package fr.inrialpes.exmo.queryprocessor;

/**
 *
 * @author Arun Sharma
 */
public interface Triple {
    
    public String subject = "";
    public String object = "";
    public String predicate = "";
        
    public String getObject();
    
    public void setObject(String obj);
    
    public String getPredicate() ;
    
    public void setPredicate(String pred);
    
    public String getSubject();
    
    public void setSubject(String sub);
}
