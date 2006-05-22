/*
 * ResultImpl.java
 *
 * Created on May 21, 2006, 10:49 PM
 *
 * 
 */

package fr.inrialpes.exmo.queryprocessor.impl;

import fr.inrialpes.exmo.queryprocessor.*;
import java.util.*;
/**
 *
 * @author Arun Sharma
 */
public class ResultImpl implements Result{
    
    Set aSet = new HashSet();
    
    public ResultImpl(Set set) {
        aSet = set;
    }
    
    /**@returns a collection set for SELECT queries
     */
    public Collection getSelectResult() throws QueryTypeMismatchException  {
        return aSet;
    }
    
     public int getType()  {
         return 0;
     }
    public boolean getAskResult() throws QueryTypeMismatchException  {
        return false;
    }
    public RDFGraph getConstructResult() throws QueryTypeMismatchException  {
        return null;
    }
    public String getSelectResultasXML() throws QueryTypeMismatchException  {
        return null;
    }
}
