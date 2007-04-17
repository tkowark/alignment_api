package fr.inrialpes.exmo.align.service.jade.messageontology;


import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: Action
* @author ontology bean generator
* @version 2007/03/19, 17:12:29
*/
public class Action implements Predicate {

   /**
* Protege name: hasParameter
   */
   private List hasParameter = new ArrayList();
   public void addHasParameter(Parameter elem) { 
     List oldList = this.hasParameter;
     hasParameter.add(elem);
   }
   public boolean removeHasParameter(Parameter elem) {
     List oldList = this.hasParameter;
     boolean result = hasParameter.remove(elem);
     return result;
   }
   public void clearAllHasParameter() {
     List oldList = this.hasParameter;
     hasParameter.clear();
   }
   public Iterator getAllHasParameter() {return hasParameter.iterator(); }
   public List getHasParameter() {return hasParameter; }
   public void setHasParameter(List l) {hasParameter = l; }

}
