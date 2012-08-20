/*
 * $Id$
 *
 * Copyright (C) INRIA, 2012
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA
 */

package fr.inrialpes.exmo.align.impl.renderer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Cell;

import fr.inrialpes.exmo.align.impl.edoal.Apply;
import fr.inrialpes.exmo.align.impl.edoal.ClassConstruction;
import fr.inrialpes.exmo.align.impl.edoal.ClassDomainRestriction;
import fr.inrialpes.exmo.align.impl.edoal.ClassExpression;
import fr.inrialpes.exmo.align.impl.edoal.ClassId;
import fr.inrialpes.exmo.align.impl.edoal.ClassOccurenceRestriction;
import fr.inrialpes.exmo.align.impl.edoal.ClassRestriction;
import fr.inrialpes.exmo.align.impl.edoal.ClassTypeRestriction;
import fr.inrialpes.exmo.align.impl.edoal.ClassValueRestriction;
import fr.inrialpes.exmo.align.impl.edoal.Comparator;
import fr.inrialpes.exmo.align.impl.edoal.Datatype;
import fr.inrialpes.exmo.align.impl.edoal.EDOALVisitor;
import fr.inrialpes.exmo.align.impl.edoal.Expression;
import fr.inrialpes.exmo.align.impl.edoal.InstanceExpression;
import fr.inrialpes.exmo.align.impl.edoal.InstanceId;
import fr.inrialpes.exmo.align.impl.edoal.PathExpression;
import fr.inrialpes.exmo.align.impl.edoal.PropertyConstruction;
import fr.inrialpes.exmo.align.impl.edoal.PropertyDomainRestriction;
import fr.inrialpes.exmo.align.impl.edoal.PropertyExpression;
import fr.inrialpes.exmo.align.impl.edoal.PropertyId;
import fr.inrialpes.exmo.align.impl.edoal.PropertyRestriction;
import fr.inrialpes.exmo.align.impl.edoal.PropertyTypeRestriction;
import fr.inrialpes.exmo.align.impl.edoal.PropertyValueRestriction;
import fr.inrialpes.exmo.align.impl.edoal.RelationCoDomainRestriction;
import fr.inrialpes.exmo.align.impl.edoal.RelationConstruction;
import fr.inrialpes.exmo.align.impl.edoal.RelationDomainRestriction;
import fr.inrialpes.exmo.align.impl.edoal.RelationExpression;
import fr.inrialpes.exmo.align.impl.edoal.RelationId;
import fr.inrialpes.exmo.align.impl.edoal.RelationRestriction;
import fr.inrialpes.exmo.align.impl.edoal.Transformation;
import fr.inrialpes.exmo.align.impl.edoal.Value;
import fr.inrialpes.exmo.align.impl.edoal.ValueExpression;
import fr.inrialpes.exmo.align.parser.SyntaxElement.Constructor;

/**
 * Translate BGP from alignment
 *
 * @author 
 * @version
 */

// JE: create a string... problem with increment.

public abstract class GraphPatternRendererVisitor extends IndentedRendererVisitor implements EDOALVisitor {

    Alignment alignment = null;
    Cell cell = null;
    Hashtable<String,String> nslist = null;
    
    private String instance = null;
    private String value = "";
    private String uriType = null;
    private String datatype = "";
    private Object valueRestriction = null;
    private boolean isValueRestriction = false;

    private Stack<String> stackBGP = new Stack<String>();
    private Stack<Constructor> stackOp = new Stack<Constructor>();
        
    private static int flagRestriction;

    private Constructor op = null;    
      
    private Integer nbCardinality = null;
    private String opOccurence = "";
    
    private static int numberNs;
	private static int number = 1;
	
    private static String sub = ""; 
    private static String obj = "";
    private String strBGP = "";
    private String strBGP_Or = "";
    private List<String> listBGP = new ArrayList<String>();
 
    private List<String> listCond = new ArrayList<String>();
 
    private Set<String> subjectsRestriction = new HashSet<String>();
    private Set<String> objectsRestriction = new HashSet<String>();
    protected Hashtable<String,String> prefixList = new Hashtable<String,String>();
	private static int countInList = 0;
    
    private static int count = 1;
	    
    public GraphPatternRendererVisitor( PrintWriter writer ){
		super( writer );
    }

    public static void resetVariablesName(String s, String o) {
    	count = 1;
    	sub = "?" + s;
    	obj = "?" + o + count;
    }   
    
    public void resetVariables(String s, String o) {
    	resetVariablesName(s, o);
    	strBGP = "";
		strBGP_Or = "";
		listBGP.clear();
		listCond.clear();
		stackBGP.clear();
		objectsRestriction.clear();
		flagRestriction = 0;
    }
    
    public List<String> getBGP(){
    	try {
    		listBGP.add(strBGP);
    	}
    	catch (Exception e) {
    		System.err.println("No query!");
    	}
    	return listBGP;
    }
    
    public List<String> getCondition(){
    	return listCond;
    }
    
    public String getPrefixDomain(URI u) {
    	String str = u.toString();
    	int index;
    	if (str.contains("#"))
    		index = str.lastIndexOf("#");
    	else
    		index = str.lastIndexOf("/");
    	return str.substring(0, index+1);
    }
    
    public String getPrefixName(URI u) {
    	String str = u.toString();
    	int index;
    	if (str.contains("#"))
    		index = str.lastIndexOf("#");
    	else
    		index = str.lastIndexOf("/");
    	return str.substring(index+1);
    }
    
    public static String getNamespace(){
    	return "ns" + numberNs++;
    }
    
    public void createQueryFile( String dir, String query ) {
    	BufferedWriter out = null;
    	try {
	    FileWriter writer = new FileWriter( dir+"query"+number +".rq" );
	    out = new BufferedWriter( writer );
	    number++;
	    out.write(query);
	    if ( out != null ) // there was at least one file
		out.close();
	} catch(IOException ioe) {
	    System.err.println(ioe);
	}
    }

    public void visit( Expression o ) throws AlignmentException {
	if ( o instanceof PathExpression ) visit( (PathExpression)o );
	else if ( o instanceof ClassExpression ) visit( (ClassExpression)o );
	else if ( o instanceof InstanceExpression ) visit( (InstanceExpression)o );
	else throw new AlignmentException( "Cannot dispatch Expression "+o );
    }

    // DONE
    public void visit( final PathExpression p ) throws AlignmentException {
		if ( p instanceof RelationExpression ) visit( (RelationExpression)p );
		else if ( p instanceof PropertyExpression ) visit( (PropertyExpression)p );
		else throw new AlignmentException( "Cannot dispatch PathExpression "+p );
    }

    // DONE
    public void visit( final ClassExpression e ) throws AlignmentException {    	
    	if ( e instanceof ClassId ) visit( (ClassId)e );
		else if ( e instanceof ClassConstruction )  visit( (ClassConstruction)e );
		else if ( e instanceof ClassRestriction )  	visit( (ClassRestriction)e );		
		else throw new AlignmentException( "Cannot dispatch ClassExpression "+e );
    }

    // DONE+TESTED
    public void visit( final ClassId e ) throws AlignmentException {
    	if ( e.getURI() != null ) {
    		String prefix = getPrefixDomain(e.getURI());
    		String tag = getPrefixName(e.getURI());
    		String shortCut;
    		if(!prefixList.containsKey(prefix)){
    			shortCut = getNamespace();
    			prefixList.put(prefix, shortCut);
    		}
    		else {
    			shortCut = prefixList.get(prefix);
    		}
    		//if (flagRestriction == 1){
				//strBGP += "?p rdfs:domain " + shortCut + ":"+ tag + " ." + NL;			
				//strBGP_Or += "?p rdfs:domain " + shortCut + ":"+ tag + " ." + NL;
    			//stackBGP.push(strBGP_Or);
    			//return;
    		//}
			if ( !subjectsRestriction.isEmpty() ) {
				Iterator<String> listSub = subjectsRestriction.iterator();
				while ( listSub.hasNext() ) {
					String str = listSub.next();
					strBGP += str + " a " + shortCut + ":"+ tag + " ." + NL;			
					strBGP_Or += str + " a " + shortCut + ":"+ tag + " ." + NL;
				}
				
				subjectsRestriction.clear();
			}
			else {
				strBGP += sub + " a " + shortCut + ":"+ tag + " ." + NL;			
				if (op != Constructor.NOT) {
					Stack<String> tempStack = new Stack<String>();
					if (stackBGP.isEmpty()){
						strBGP_Or = sub + " a " + shortCut + ":"+ tag + " ." + NL;
						tempStack.push(strBGP_Or);
					}
					else {
						while (!stackBGP.isEmpty()){
							strBGP_Or = stackBGP.pop();
							strBGP_Or += sub + " a " + shortCut + ":"+ tag + " ." + NL;
							tempStack.push(strBGP_Or);
						}						
					}
					while (!tempStack.isEmpty()){
						stackBGP.push(tempStack.pop());
					}
				}
				
			}
    	}
    }

    // DONE+TESTED
    public void visit( final ClassConstruction e ) throws AlignmentException {		
    	op = e.getOperator();
		countInList = 0;
		if (op == Constructor.OR){			
			int size = e.getComponents().size();
			Stack<String> tempStack = new Stack<String>();
			Stack<String> tempResult = new Stack<String>();
			while(!stackBGP.empty()){
		    	tempStack.push(stackBGP.pop());		    	
		    }
			for ( final ClassExpression ce : e.getComponents() ) {
			    strBGP += "{" + NL;
			    if(op != null){
					stackOp.push(op);
				}
			    stackBGP.clear();
			    if(!tempStack.empty()){
			    	for(int i=0; i<tempStack.size(); i++)
			    		stackBGP.push(tempStack.get(i));
			    }
			    countInList++;
			    visit( ce );
			    
			    if(op == Constructor.NOT)
			    	stackBGP.clear();
			    
			    if(!stackOp.isEmpty())
					op = stackOp.pop();
			   
			    while(!stackBGP.empty()){
			    	tempResult.push(stackBGP.pop());
			    }		    
			    
			    size--;
			    if(size != 0)
			    	strBGP += "}" + " UNION " + NL;
			    else
			    	strBGP += "}" + NL;
			}
			while(!tempResult.empty()){
				stackBGP.push(tempResult.pop());
		    }
		}
		else if (op == Constructor.NOT){
			
			for ( final ClassExpression ce : e.getComponents() ) {
			    strBGP +="MINUS {" + NL + INDENT;			    
			    if(op != null){
					stackOp.push(op);
				}
			    visit( ce );
			    if(!stackOp.isEmpty())
					op = stackOp.pop();
			    strBGP +="}" + NL;			    
			}
			
		}
		else {			
			for ( final ClassExpression ce : e.getComponents() ) {
			    if(op != null){
					stackOp.push(op);
				}			    
			    visit( ce );			    
			    if(!stackOp.isEmpty())
					op = stackOp.pop();			    
			}
		}
		
		if(stackOp.isEmpty()) {
			if(op == Constructor.NOT || flagRestriction == 1)
				stackBGP.clear();
			while (!stackBGP.empty()){	    		
	    		listBGP.add(stackBGP.pop());	    		
	    	}
		}

		op = null;		
    }
    
    // DONE+TESTED
    public void visit(final ClassRestriction e) throws AlignmentException {    	
    	if ( e instanceof ClassValueRestriction ) visit( (ClassValueRestriction)e );
		else if ( e instanceof ClassTypeRestriction )  visit( (ClassTypeRestriction)e );
		else if ( e instanceof ClassDomainRestriction )  visit( (ClassDomainRestriction)e );
		else if ( e instanceof ClassOccurenceRestriction ) visit( (ClassOccurenceRestriction)e );
		else throw new AlignmentException( "Cannot dispatch ClassExpression "+e );
    	objectsRestriction.clear();
    }

    // DONE+TESTED
    public void visit( final ClassValueRestriction c ) throws AlignmentException {
	    instance = null;
	    value = "";
	    isValueRestriction = true;
	    flagRestriction = 1;
	    visit( c.getValue() );
	    flagRestriction = 0;
	    
	    if(instance != null)
	    	valueRestriction = instance;
	    else if(!value.equals(""))
	    	valueRestriction = value;
	    
		visit( c.getRestrictionPath() );
		
		isValueRestriction = false;
		String temp = obj;
		Iterator<String> listObj = objectsRestriction.iterator();
		while ( listObj.hasNext() ) {
			obj = listObj.next();				
			flagRestriction = 1;
			visit( c.getValue() );
			flagRestriction = 0;
		}		
		
		valueRestriction = null;
		obj = temp;
		if(op == Constructor.AND){		
			obj = "?o" + ++count;
		}
		
    }

    // DONE+TESTED
    public void visit( final ClassTypeRestriction c ) throws AlignmentException {	
    	String str = "";
    	datatype = "";
    	visit( c.getRestrictionPath() );
		if ( !objectsRestriction.isEmpty() ) {
			Iterator<String> listObj = objectsRestriction.iterator();
			int size = objectsRestriction.size();
			if (size > 0) {
				increaseIndent();
				str = "FILTER (datatype(" + listObj.next() + ") = ";				
				visit( c.getType() );
				str += "xsd:" + datatype;				
			}
			while ( listObj.hasNext() ) {
				str += " && datatype(" + listObj.next() + ") = ";				
				visit( c.getType() );
				str += "xsd:" + datatype;
			}
			str += ")" + NL;
			while(countInList > listCond.size())
				listCond.add("");
			if(countInList>0)
				listCond.add(countInList-1, str);
			else
				listCond.add(countInList, str);
			strBGP += str;
		}
		objectsRestriction.clear();
    }

    // DONE+TESTED
    public void visit( final ClassDomainRestriction c ) throws AlignmentException {					
    	visit( c.getRestrictionPath() );
    	Iterator<String> listObj = objectsRestriction.iterator();
    	while ( listObj.hasNext() ) {
			subjectsRestriction.add(listObj.next());			
		}
    	if(listBGP.size() > 0)
    		strBGP_Or = listBGP.remove(listBGP.size()-1);
    	if(stackBGP.size() > 0)
    		stackBGP.remove(stackBGP.size()-1);
    	visit( c.getDomain() );
    	stackBGP.add(strBGP_Or);
    	objectsRestriction.clear();
    }

    // DONE+TESTED
    public void visit( final ClassOccurenceRestriction c ) throws AlignmentException {
		if(c.getComparator().getURI().equals( Comparator.EQUAL.getURI())) {
			nbCardinality = c.getOccurence();
			opOccurence = "=";
		}
		if(c.getComparator().getURI().equals( Comparator.GREATER.getURI())) {
			nbCardinality = c.getOccurence();
			opOccurence = ">";
		}
		if(c.getComparator().getURI().equals( Comparator.LOWER.getURI())) {
			nbCardinality = c.getOccurence();
			opOccurence = "<";
		}
		
		visit( c.getRestrictionPath() );	
		Iterator<String> listObj = objectsRestriction.iterator();
		if (op == Constructor.COMP) {			
			strBGP += "FILTER(COUNT(" + listObj.next() + ")" + opOccurence + nbCardinality + ")" +NL;		    
		}
		else{
			while ( listObj.hasNext() ) {
				strBGP += "FILTER(COUNT(" + listObj.next() + ")" + opOccurence + nbCardinality + ")" +NL;	
			}
		}
		if(stackBGP.size() > 0)
    		stackBGP.remove(stackBGP.size()-1);
		nbCardinality = null;
		opOccurence = "";
    }
    
    // DONE
    public void visit(final PropertyExpression e) throws AlignmentException {
	if ( e instanceof PropertyId ) visit( (PropertyId)e );
	else if ( e instanceof PropertyConstruction ) visit( (PropertyConstruction)e );
	else if ( e instanceof PropertyRestriction ) visit( (PropertyRestriction)e );
	else throw new AlignmentException( "Cannot dispatch ClassExpression "+e );
    }
	
    // DONE
    public void visit(final PropertyId e) throws AlignmentException {
    	if ( e.getURI() != null ) {	
    		String prefix = getPrefixDomain(e.getURI());
    		String tag = getPrefixName(e.getURI());
    		String shortCut;
    		if(!prefixList.containsKey(prefix)){
    			shortCut = getNamespace();
    			prefixList.put(prefix, shortCut);
    		}
    		else {
    			shortCut = prefixList.get(prefix);
    		}
    		String temp = obj;
    		if(op == null && valueRestriction != null)
    			obj = "\"" + valueRestriction.toString() + "\"";
		    strBGP += sub + " " + shortCut + ":"+ tag + " " + obj + " ." +NL;
		    if (op != Constructor.NOT) {
				Stack<String> tempStack = new Stack<String>();
				if (stackBGP.isEmpty()){
					strBGP_Or = sub + " " + shortCut + ":"+ tag + " " + obj + " ." +NL;
					tempStack.push(strBGP_Or);
				}
				else {
					while (!stackBGP.isEmpty()){
						strBGP_Or = stackBGP.pop();
						strBGP_Or += sub + " " + shortCut + ":"+ tag + " " + obj + " ." +NL;
						tempStack.push(strBGP_Or);
					}						
				}
				while (!tempStack.isEmpty()){
					stackBGP.push(tempStack.pop());
				}
			}
    		obj = temp;
		}
    }

    // DONE
    public void visit(final PropertyConstruction e) throws AlignmentException {
    	op = e.getOperator();
    	
		if (op == Constructor.OR){			
			int size = e.getComponents().size();
			Stack<String> tempStack = new Stack<String>();
			Stack<String> tempResult = new Stack<String>();
			while(!stackBGP.empty()){
		    	tempStack.push(stackBGP.pop());		    	
		    }
			if(valueRestriction != null)
				obj = "\"" + valueRestriction.toString() + "\"";
			for ( final PathExpression re : e.getComponents() ) {
			    strBGP += "{" +NL;
			    if(op != null){
					stackOp.push(op);
				}
			    stackBGP.clear();
			    if(!tempStack.empty()){
			    	for(int i=0; i<tempStack.size(); i++)
			    		stackBGP.push(tempStack.get(i));
			    }
			    countInList++;
			    visit( re );
			    if(op == Constructor.NOT)
			    	stackBGP.clear();
			    if(!stackOp.isEmpty())
					op = stackOp.pop();
		    
			    while(!stackBGP.empty()){
			    	tempResult.push(stackBGP.pop());
			    }
			    size--;
			    if(size != 0)
			    	strBGP += "}" + " UNION " + NL;			    
			    else
			    	strBGP += "}" +NL;
			}
			while(!tempResult.empty()){
				stackBGP.push(tempResult.pop());
		    }
			objectsRestriction.add(obj);
		}
		else if (op == Constructor.NOT){			
			for ( final PathExpression re : e.getComponents() ) {
			    strBGP += "MINUS {" +NL;
			    if(op != null){
					stackOp.push(op);
				}
			    visit( re );
			    if(!stackOp.isEmpty())
					op = stackOp.pop();
			}			
			strBGP += "}" +NL;
		}
		else if (op == Constructor.COMP){			
			int size = e.getComponents().size();
			String tempSub = sub;			
			
			for ( final PathExpression re : e.getComponents() ) {
			    writer.print(linePrefix);			    
			    if(op != null){
					stackOp.push(op);
				}
			    visit( re );
			    if(!stackOp.isEmpty())
					op = stackOp.pop();
			    size--;
			    if(size != 0){
			    	sub = obj;
			    	if(size == 1 && valueRestriction != null)
			    		obj = "\"" + valueRestriction.toString() + "\"";
			    	else
			    		obj = "?o" + ++count;			    	
			    }
			}
			objectsRestriction.add(obj);
			sub = tempSub;
		}		
		else {
			
			int size = e.getComponents().size();
			if(valueRestriction != null)
				obj = "\"" + valueRestriction.toString() + "\"";
			for ( final PathExpression re : e.getComponents() ) {
			    			 
			    if(op != null){
					stackOp.push(op);
				}
			    visit( re );
			    if(!stackOp.isEmpty())
					op = stackOp.pop();
			    size--;	    
			    objectsRestriction.add(obj);
			    if(size != 0 && valueRestriction == null){
			    	obj = "?o" + ++count;			    	
			    }
			}		
		}
		if(stackOp.isEmpty()) {
			if(op == Constructor.NOT)
				stackBGP.clear();
			while (!stackBGP.empty()){	    		
	    		listBGP.add(stackBGP.pop());	    		
	    	}
		}
		op = null;
		obj = "?o" + ++count;
    }
    
    // DONE
    public void visit(final PropertyRestriction e) throws AlignmentException {
    	if ( e instanceof PropertyValueRestriction ) visit( (PropertyValueRestriction)e );
		else if ( e instanceof PropertyDomainRestriction ) visit( (PropertyDomainRestriction)e );
		else if ( e instanceof PropertyTypeRestriction ) visit( (PropertyTypeRestriction)e );
		else throw new AlignmentException( "Cannot dispatch ClassExpression "+e );
    	//resetVariablesName();    
    }
	
    // DONE
    public void visit(final PropertyValueRestriction c) throws AlignmentException {
    	
    	if(c.getComparator().getURI().equals( Comparator.EQUAL.getURI())){    		
    		strBGP += sub + " ?p ";
    		strBGP_Or = sub + " ?p ";
    		visit( c.getValue() );
    		strBGP += "\"" + value + "\" ." + NL;
    		strBGP_Or += "\"" + value + "\" ." + NL;    		    		
    	}
    	else if(c.getComparator().getURI().equals( Comparator.GREATER.getURI())){    		
    		strBGP += sub + " ?p " + obj + " ." + NL;
    		strBGP_Or = sub + " ?p " + obj + " ." + NL;
			
    		String str = "FILTER (xsd:" + uriType + "(" + obj + ") > ";
    		flagRestriction = 1;
    		visit( c.getValue() );
    		flagRestriction = 0;
			str += "\"" + value + "\")" + NL;
			while(countInList > listCond.size())
				listCond.add("");
			if(countInList>0)
				listCond.add(countInList-1, str);
			else
				listCond.add(countInList, str);
			strBGP += str;
    	}
    	else if(c.getComparator().getURI().equals( Comparator.LOWER.getURI())){    		
    		strBGP += sub + " ?p " + obj + " ." + NL;    		
    		strBGP_Or = sub + " ?p " + obj + " ." + NL;
			
    		String str = "FILTER (xsd:" + uriType + "(" + obj + ") < ";
    		flagRestriction = 1;
    		visit( c.getValue() );
    		flagRestriction = 0;
			str += "\"" + value + "\")" + NL;
			while(countInList > listCond.size())
				listCond.add("");
			if(countInList>0)
				listCond.add(countInList-1, str);
			else
				listCond.add(countInList, str);
			strBGP += str;
    	}
    	stackBGP.push(strBGP_Or);
    	//else throw new AlignmentException( "Cannot dispatch Comparator "+c );
    }

    // DONE
    public void visit(final PropertyDomainRestriction c) throws AlignmentException {
    	flagRestriction = 1;
		strBGP += sub + " ?p " + obj + " ." + NL;    		
		strBGP_Or = sub + " ?p " + obj + " ." + NL;		

    	visit( c.getDomain() );
    	
    	flagRestriction = 0;
	
    }

    // DONE
    public void visit(final PropertyTypeRestriction c) throws AlignmentException {
    	
    	String str = "FILTER (datatype(" + objectsRestriction + ") = ";
		
		if ( !objectsRestriction.isEmpty() ) {
			Iterator<String> listObj = objectsRestriction.iterator();
			int size = objectsRestriction.size();
			if (size > 0) {
				increaseIndent();
				str = "FILTER (datatype(" + listObj.next() + ") = ";				
				visit( c.getType() );
				str += "xsd:" + datatype;				
			}
			while ( listObj.hasNext() ) {
				str += " && datatype(" + listObj.next() + ") = ";				
				visit( c.getType() );
				str += "xsd:" + datatype;
			}
			str += ")" + NL;
			while(countInList > listCond.size())
				listCond.add("");
			if(countInList>0)
				listCond.add(countInList-1, str);
			else
				listCond.add(countInList, str);
			strBGP += str;
		}
		objectsRestriction.clear();
    }
    
    // DONE
    public void visit( final RelationExpression e ) throws AlignmentException {
    	if ( e instanceof RelationId ) visit( (RelationId)e );
		else if ( e instanceof RelationRestriction ) visit( (RelationRestriction)e );
		else if ( e instanceof RelationConstruction ) visit( (RelationConstruction)e );
		else throw new AlignmentException( "Cannot dispatch ClassExpression "+e );
    }
	
    // DONE
    public void visit( final RelationId e ) throws AlignmentException {
		if ( e.getURI() != null ) {
			String prefix = getPrefixDomain(e.getURI());
    		String tag = getPrefixName(e.getURI());
    		String shortCut;
    		if(!prefixList.containsKey(prefix)){
    			shortCut = getNamespace();
    			prefixList.put(prefix, shortCut);
    		}
    		else {
    			shortCut = prefixList.get(prefix);
    		}
			strBGP += sub + " " + shortCut + ":"+ tag + "";
			String str = sub + " " + shortCut + ":"+ tag + "";
		    
		    if (op == Constructor.TRANSITIVE){
		    	strBGP += "*";
		    	str += "*";
		    }
		    	
		    if(op == null && valueRestriction != null)
	    		obj = valueRestriction.toString();
	    	
		    strBGP += " " + obj + " ." + NL;
		    str += " " + obj + " ." + NL;
		    
		    if (op != Constructor.NOT) {
				Stack<String> tempStack = new Stack<String>();
				if (stackBGP.isEmpty()){
					strBGP_Or = str;
					tempStack.push(strBGP_Or);
				}
				else {
					while (!stackBGP.isEmpty()){
						strBGP_Or = stackBGP.pop();
						strBGP_Or += str;
						tempStack.push(strBGP_Or);
					}						
				}
				while (!tempStack.isEmpty()){
					stackBGP.push(tempStack.pop());
				}
			}
		}
    }

    // DONE
    public void visit( final RelationConstruction e ) throws AlignmentException {
		
		op = e.getOperator();

		if (op == Constructor.OR){			
			int size = e.getComponents().size();
			Stack<String> tempStack = new Stack<String>();
			Stack<String> tempResult = new Stack<String>();
			while(!stackBGP.empty()){
		    	tempStack.push(stackBGP.pop());		    	
		    }
			if(valueRestriction != null)
				obj = valueRestriction.toString();
			String temp = obj;
			for ( final PathExpression re : e.getComponents() ) {
			    writer.print(linePrefix);
			    strBGP += "{" + NL;
			    if(op != null){
					stackOp.push(op);
				}
			    stackBGP.clear();
			    if(!tempStack.empty()){
			    	for(int i=0; i<tempStack.size(); i++)
			    		stackBGP.push(tempStack.get(i));
			    }
			    countInList++;
			    visit( re );
			    if(op == Constructor.NOT){
			    	stackBGP.clear();
			    }
			    if(!stackOp.isEmpty())
					op = stackOp.pop();
			    
			    while(!stackBGP.empty()){
			    	tempResult.push(stackBGP.pop());
			    }
			    obj = temp;
			    size--;
			    if(size != 0)
			    	strBGP += "}" + "UNION " + NL;
			    else
			    	strBGP += "}" + NL;
			}
			while(!tempResult.empty()){
				stackBGP.push(tempResult.pop());
		    }
			objectsRestriction.add(obj);
		}
		else if (op == Constructor.NOT){			
			for ( final PathExpression re : e.getComponents() ) {
				strBGP += "MINUS {" + NL;
			    if(op != null){
					stackOp.push(op);
				}
			    visit( re );
			    if(!stackOp.isEmpty())
					op = stackOp.pop();
			    strBGP += "}" + NL;
			}			
		}
		else if (op == Constructor.COMP){
			int size = e.getComponents().size();
			String temp = sub;
			for ( final PathExpression re : e.getComponents() ) {			   
			    if(op != null){
					stackOp.push(op);
				}
			    visit( re );
			    if(!stackOp.isEmpty())
					op = stackOp.pop();
			    size--;
			    if(size != 0) {
			    	sub = obj;
			    	if(size == 1 && valueRestriction != null)
			    		obj = valueRestriction.toString();
			    	else {
			    		obj = "?o" + ++count;
			    		objectsRestriction.add(obj);
			    	}
			    					    	
			    }			    
			}
			
			sub = temp;
		}
		else if (op == Constructor.INVERSE){
			String tempSub = sub;
			for ( final PathExpression re : e.getComponents() ) {
			    writer.print(linePrefix);			 
			    String temp = sub;
			    sub = obj;
			    obj = temp;
			    if(op != null){
					stackOp.push(op);
				}
			    visit( re );
			    if(!stackOp.isEmpty())
					op = stackOp.pop();
			    sub = tempSub;
			}
		}
		else if (op == Constructor.SYMMETRIC){					
			String tempSub = sub;
			for ( final PathExpression re : e.getComponents() ) {
			    strBGP += "{" + NL;
			    if(op != null){
					stackOp.push(op);
				}
			    visit( re );
			    if(!stackOp.isEmpty())
					op = stackOp.pop();
			    objectsRestriction.add(obj);
			    String temp = sub;
			    sub = obj;
			    obj = temp;
			    strBGP += "} UNION {" + NL;
			    if(op != null){
					stackOp.push(op);
				}
			    visit( re );
			    if(!stackOp.isEmpty())
					op = stackOp.pop();
			    objectsRestriction.add(obj);
			    strBGP +="}" + NL;
			    
			}
			sub = tempSub;
		}
		else if (op == Constructor.TRANSITIVE){						
			for ( final PathExpression re : e.getComponents() ) {			    
			    if(op != null){
					stackOp.push(op);
				}
			    visit( re );	
			    if(!stackOp.isEmpty())
					op = stackOp.pop();
			}
		}
		else if (op == Constructor.REFLEXIVE){						
			String str = obj;
			for ( final PathExpression re : e.getComponents() ) {			    		    
			    if(op != null){
					stackOp.push(op);
				}
			    obj = sub;
			    visit( re );
			    obj = str;
			    if(!stackOp.isEmpty())
					op = stackOp.pop();
			}
		}
		else {
			
			int size = e.getComponents().size();
			if(valueRestriction != null)
				obj = valueRestriction.toString();
			for ( final PathExpression re : e.getComponents() ) {
						 
			    if(op != null){
					stackOp.push(op);
				}
			    visit( re );
			    if(!stackOp.isEmpty())
					op = stackOp.pop();
			    size--;
			    objectsRestriction.add(obj);
			    if(size != 0 && valueRestriction == null){
			    	obj = "?o" + ++count;			    	
			    }
			}		
		}
		
		if(stackOp.isEmpty()) {
			if(op == Constructor.NOT)
				stackBGP.clear();
			while (!stackBGP.empty()){	    		
	    		listBGP.add(stackBGP.pop());	    		
	    	}
		}
		op = null;
		obj = "?o" + ++count;
    }
    
    // DONE
    public void visit( final RelationRestriction e ) throws AlignmentException {
		if ( e instanceof RelationCoDomainRestriction ) visit( (RelationCoDomainRestriction)e );
		else if ( e instanceof RelationDomainRestriction ) visit( (RelationDomainRestriction)e );
		else throw new AlignmentException( "Cannot dispatch ClassExpression "+e );
		//resetVariablesName();
    }
	
    // DONE
    public void visit(final RelationCoDomainRestriction c) throws AlignmentException {
    	flagRestriction = 1;		
    	
    	visit( c.getCoDomain() );
    	
    	flagRestriction = 0;
    }

    // DONE
    public void visit(final RelationDomainRestriction c) throws AlignmentException {

    	flagRestriction = 1;
		
		strBGP += sub + " ?p " + obj + " ." + NL;    		
		
    	visit( c.getDomain() );
    	
    	flagRestriction = 0;
    }
    
    // DONE
    public void visit( final InstanceExpression e ) throws AlignmentException {
	if ( e instanceof InstanceId ) visit( (InstanceId)e );
	else throw new AlignmentException( "Cannot handle InstanceExpression "+e );
    }

    // DONE+TESTED
    public void visit( final InstanceId e ) throws AlignmentException {
		if ( e.getURI() != null ) {
			String prefix = getPrefixDomain(e.getURI());
    		String tag = getPrefixName(e.getURI());
    		String shortCut;
    		if(!prefixList.containsKey(prefix)){
    			shortCut = getNamespace();
    			prefixList.put(prefix, shortCut);
    		}
    		else {
    			shortCut = prefixList.get(prefix);
    		}
			if(flagRestriction != 1)
				strBGP += shortCut + ":"+ tag + " ?p ?o1 ." +NL;
			else
				instance = shortCut + ":"+ tag;
		}
    }
    
    // DONE+TESTED
    public void visit( final ValueExpression e ) throws AlignmentException {
	if ( e instanceof InstanceExpression )visit( (InstanceExpression)e );	
	else if ( e instanceof PathExpression )  {
		if(isValueRestriction) 
			return;
		else
			visit( (PathExpression)e );
	}
	else if ( e instanceof Apply )  visit( (Apply)e );
	else if ( e instanceof Value )  visit( (Value)e );
	else throw new AlignmentException( "Cannot dispatch ClassExpression "+e );
    }

    public void visit( final Value e ) throws AlignmentException {
    	if (e.getType() != null) {
	    	String str = e.getType().toString();
	    	int index;
	    	if (str.contains("#"))
	    		index = str.lastIndexOf("#");
	    	else
	    		index = str.lastIndexOf("/");
	    	uriType = str.substring(0, index+1);
    	}
    	value = e.getValue();
    }
	
    public void visit( final Apply e ) throws AlignmentException {

    }

    public void visit( final Transformation transf ) throws AlignmentException {

    }

    // DONE
    public void visit( final Datatype e ) throws AlignmentException {
    	int index;
    	if (e.getType().contains("#"))
    		index = e.getType().lastIndexOf("#");
    	else
    		index = e.getType().lastIndexOf("/");
    	datatype = e.getType().substring(0, index+1);
    }

}
