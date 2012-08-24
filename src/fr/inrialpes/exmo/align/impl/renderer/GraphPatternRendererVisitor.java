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
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
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
import fr.inrialpes.exmo.align.impl.edoal.ClassTypeRestriction;
import fr.inrialpes.exmo.align.impl.edoal.ClassValueRestriction;
import fr.inrialpes.exmo.align.impl.edoal.Comparator;
import fr.inrialpes.exmo.align.impl.edoal.Datatype;
import fr.inrialpes.exmo.align.impl.edoal.EDOALVisitor;
import fr.inrialpes.exmo.align.impl.edoal.InstanceId;
import fr.inrialpes.exmo.align.impl.edoal.PathExpression;
import fr.inrialpes.exmo.align.impl.edoal.PropertyConstruction;
import fr.inrialpes.exmo.align.impl.edoal.PropertyDomainRestriction;
import fr.inrialpes.exmo.align.impl.edoal.PropertyId;
import fr.inrialpes.exmo.align.impl.edoal.PropertyTypeRestriction;
import fr.inrialpes.exmo.align.impl.edoal.PropertyValueRestriction;
import fr.inrialpes.exmo.align.impl.edoal.RelationCoDomainRestriction;
import fr.inrialpes.exmo.align.impl.edoal.RelationConstruction;
import fr.inrialpes.exmo.align.impl.edoal.RelationDomainRestriction;
import fr.inrialpes.exmo.align.impl.edoal.RelationId;
import fr.inrialpes.exmo.align.impl.edoal.Transformation;
import fr.inrialpes.exmo.align.impl.edoal.Value;
import fr.inrialpes.exmo.align.parser.SyntaxElement.Constructor;

/**
 * Translate correspondences into Graph Patterns
 *
 * @author 
 * @version
 */

// JE: create a string... problem with increment.

public abstract class GraphPatternRendererVisitor extends IndentedRendererVisitor implements EDOALVisitor {

    Alignment alignment = null;
    Cell cell = null;
    Hashtable<String,String> nslist = null;
    protected boolean ignoreerrors = false;
    protected static boolean blanks = false;
    protected boolean weakens = false;
    private boolean inClassRestriction = false;
    private String instance = null;
    private String value = "";
    private String uriType = null;
    private String datatype = "";
    private Object valueRestriction = null;

    private Stack<String> stackBGP = new Stack<String>();
    private Stack<String> stackOR = new Stack<String>();
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
    protected Hashtable<String,String> listBGP = new Hashtable<String,String>();
 
    private String condition = "";
 
    private Set<String> subjectsRestriction = new HashSet<String>();
    private Set<String> objectsRestriction = new HashSet<String>();
    protected Hashtable<String,String> prefixList = new Hashtable<String,String>();
    
    private static int count = 1;
	    
    public GraphPatternRendererVisitor( PrintWriter writer ){
		super( writer );
    }

    public static void resetVariablesName( String s, String o ) {
    	count = 1;
    	sub = "?" + s;
    	if ( blanks ) {
    		obj = "_:" + o + count;
    	}
    	else {
    		obj = "?" + o + count;
    	}
    }   
    
    public void resetVariables( String s, String o ) {
    	resetVariablesName(s, o);
    	strBGP = "";
		strBGP_Or = "";
		condition = "";
		listBGP.clear();
		stackBGP.clear();
		objectsRestriction.clear();
		flagRestriction = 0;
    }
    
    public String getGP(){
    	return strBGP;
    }
    
    public Hashtable<String,String> getBGP() {
    	return listBGP;
    }
    
    public String getPrefixDomain( URI u ) {
    	String str = u.toString();
    	int index;
    	if ( str.contains("#") )
    		index = str.lastIndexOf("#");
    	else
    		index = str.lastIndexOf("/");
    	return str.substring(0, index+1);
    }
    
    public String getPrefixName( URI u ) {
    	String str = u.toString();
    	int index;
    	if ( str.contains("#") )
    		index = str.lastIndexOf("#");
    	else
    		index = str.lastIndexOf("/");
    	return str.substring( index+1 );
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
	    out.write( query );
	    if ( out != null ) // there was at least one file
		out.close();
	} catch(IOException ioe) {
	    System.err.println( ioe );
	}
    }

    public void visit( final ClassId e ) throws AlignmentException {
    	if ( e.getURI() != null ) {
    		String prefix = getPrefixDomain(e.getURI());
    		String tag = getPrefixName(e.getURI());
    		String shortCut;
    		if( !prefixList.containsKey(prefix) ){
    			shortCut = getNamespace();
    			prefixList.put( prefix, shortCut );
    		}
    		else {
    			shortCut = prefixList.get( prefix );
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
				if ( stackBGP.size() > 0 ) {
					stackBGP.remove( stackBGP.size()-1 );
					stackBGP.push( strBGP_Or );
				}
				subjectsRestriction.clear();
			}
			else {
				strBGP += sub + " a " + shortCut + ":"+ tag + " ." + NL;			
				if ( op != Constructor.NOT ) {
					Stack<String> tempStack = new Stack<String>();
					if ( stackBGP.empty() ){
						strBGP_Or = sub + " a " + shortCut + ":"+ tag + " ." + NL;
						tempStack.push( strBGP_Or );
					}
					else {
						while ( !stackBGP.empty() ) {
							strBGP_Or = stackBGP.pop();
							strBGP_Or += sub + " a " + shortCut + ":"+ tag + " ." + NL;
							tempStack.push( strBGP_Or );
						}						
					}
					while ( !tempStack.empty() ){
						stackBGP.push( tempStack.pop() );
					}				
				}
			}
    	}
    }

    public void visit( final ClassConstruction e ) throws AlignmentException {		
    	String str = "";
    	
    	op = e.getOperator();
		if (op == Constructor.OR) {
			str = "";
			int size = e.getComponents().size();
			Stack<String> tempStack = new Stack<String>();
			
			while( !stackBGP.empty() ) {
		    	tempStack.push(stackBGP.pop());		    	
		    }
			for ( final ClassExpression ce : e.getComponents() ) {
			    strBGP += "{" + NL;
			    if( op != null ) {
					stackOp.push( op );
				}
			    stackBGP.clear();
			    if( !tempStack.empty() ) {
			    	for( int i=0; i<tempStack.size(); i++ ) {
			    		stackBGP.push( tempStack.get(i) );			    		
			    	}
			    }
			    ce.accept( this );
			    if(!stackOp.empty()) {
					op = stackOp.pop();
				}
			    if( op == Constructor.NOT ) {
			    	stackBGP.clear();			    
			    }
			    while ( !stackBGP.empty() ) {	    		
		    		listBGP.put( stackBGP.pop(), condition );
		    	}
							    
			    size--;
			    if( size != 0 )
			    	strBGP += "}" + " UNION " + NL;
			    else
			    	strBGP += "}" + NL;
			}			
			
		}
		else if ( op == Constructor.NOT ) {
			str = "";
			for ( final ClassExpression ce : e.getComponents() ) {
			    str = "FILTER (NOT EXISTS {" + NL;
			    strBGP += "FILTER (NOT EXISTS {" + NL;
			    if( op != null ) {
					stackOp.push( op );
				}
			    ce.accept( this );			    
			    if( !stackOp.empty() )
					op = stackOp.pop();
			    str += "})" + NL;
			    strBGP += "})" + NL;
			    condition = str;
			    while ( !stackBGP.empty() ) {	    		
		    		listBGP.put( stackBGP.pop(), condition );	    		
		    	}				
			}			
		}
		else {			
			for ( final ClassExpression ce : e.getComponents() ) {
			    if( op != null ) {
					stackOp.push( op );
				}			    
			    ce.accept( this );
			    if( !stackOp.empty() )
					op = stackOp.pop();			    
			}
		}
		condition = "";
    }

    public void visit( final ClassValueRestriction c ) throws AlignmentException {
    	String str = "";
    	instance = "";
	    value = "";
	    flagRestriction = 1;
	    c.getValue().accept( this );
	    flagRestriction = 0;
	    
	    if( !instance.equals("") )
	    	valueRestriction = instance;
	    else if( !value.equals("") )
	    	valueRestriction = value;
	    
		if( c.getComparator().getURI().equals( Comparator.GREATER.getURI() ) ) {
			opOccurence = ">";
			inClassRestriction = true;
		}
		if( c.getComparator().getURI().equals( Comparator.LOWER.getURI() ) ) {
			opOccurence = "<";
			inClassRestriction = true;
		}
		flagRestriction = 1;
	    c.getRestrictionPath().accept( this );
	    flagRestriction = 0;
		String temp = obj;
		if ( inClassRestriction && !objectsRestriction.isEmpty() ) {
			Iterator<String> listObj = objectsRestriction.iterator();
			if (op == Constructor.COMP) {			
				String tmp = "";
				while ( listObj.hasNext() )
					tmp = listObj.next();
				str = "FILTER (" + tmp + opOccurence + valueRestriction + ")" +NL;		    
			}
			else {
				while ( listObj.hasNext() ) {
					str += "FILTER (" + listObj.next() + opOccurence + valueRestriction + ")" +NL;	
				}
			}
			strBGP += str;
			condition = str;
			if ( !stackOR.empty() ) {
				while ( !stackOR.empty() ) {	    		
		    		listBGP.put( stackOR.pop(), condition );	    		
		    	}
			}
			else {
				while ( !stackBGP.empty() ) {	    		
		    		listBGP.put( stackBGP.pop(), condition );	    		
		    	}
			}
		}
		valueRestriction = null;
		inClassRestriction = false;
		condition = "";
		obj = temp;
		if( op == Constructor.AND ){		
			if ( blanks ) {
	    		obj = "_:o" + ++count;
	    	}
	    	else {
	    		obj = "?o" + ++count;
	    	} 
		}
    }

    public void visit( final ClassTypeRestriction c ) throws AlignmentException {	
    	String str = "";
    	datatype = "";
    	flagRestriction = 1;
    	c.getRestrictionPath().accept( this );
    	flagRestriction = 0;
		if ( !objectsRestriction.isEmpty() ) {
			Iterator<String> listObj = objectsRestriction.iterator();
			int size = objectsRestriction.size();
			if ( size > 0 ) {
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
			
			strBGP += str;
			condition = str;
			if ( !stackOR.empty() ) {
				while ( !stackOR.empty() ) {	    		
		    		listBGP.put( stackOR.pop(), condition );	    		
		    	}
			}
			else {
				while ( !stackBGP.empty() ) {	    		
		    		listBGP.put( stackBGP.pop(), condition );	    		
		    	}
			}
		}
		objectsRestriction.clear();
    }

    public void visit( final ClassDomainRestriction c ) throws AlignmentException {					
    	flagRestriction = 1;
    	c.getRestrictionPath().accept( this );
    	flagRestriction = 0;
    	Iterator<String> listObj = objectsRestriction.iterator();
    	while ( listObj.hasNext() ) {
			subjectsRestriction.add(listObj.next());			
		}
    	c.getDomain().accept( this );
    	if ( !stackOR.empty() ) {
			while ( !stackOR.empty() ) {	    		
	    		listBGP.put( stackOR.pop(), condition );	    		
	    	}
		}
		else {
			while ( !stackBGP.empty() ) {	    		
	    		listBGP.put( stackBGP.pop(), condition );	    		
	    	}
		}
    	objectsRestriction.clear();
    }

    public void visit( final ClassOccurenceRestriction c ) throws AlignmentException {
		String str="";
		inClassRestriction = true;
    	if( c.getComparator().getURI().equals( Comparator.EQUAL.getURI() ) ) {
			nbCardinality = c.getOccurence();
			opOccurence = "=";
		}
		if( c.getComparator().getURI().equals( Comparator.GREATER.getURI() ) ) {
			nbCardinality = c.getOccurence();
			opOccurence = ">";
		}
		if( c.getComparator().getURI().equals( Comparator.LOWER.getURI() ) ) {
			nbCardinality = c.getOccurence();
			opOccurence = "<";
		}
		flagRestriction = 1;
		c.getRestrictionPath().accept( this );	
		flagRestriction = 0;
		if ( !objectsRestriction.isEmpty() ) {
			Iterator<String> listObj = objectsRestriction.iterator();
			if (op == Constructor.COMP) {			
				String tmp = "";
				while ( listObj.hasNext() )
					tmp = listObj.next();
				str += "FILTER(COUNT(" + tmp + ")" + opOccurence + nbCardinality + ")" +NL;	    
			}
			else{
				while ( listObj.hasNext() ) {
					str += "FILTER(COUNT(" + listObj.next() + ")" + opOccurence + nbCardinality + ")" +NL;	
				}
			}			
			
			strBGP += str;
			condition = str;
			if ( !stackOR.empty() ) {
				while ( !stackOR.empty() ) {	    		
		    		listBGP.put( stackOR.pop(), condition );	    		
		    	}
			}
			else {
				while ( !stackBGP.empty() ) {	    		
		    		listBGP.put( stackBGP.pop(), condition );	    		
		    	}
			}
		}
		nbCardinality = null;
		opOccurence = "";
		inClassRestriction = false;
		condition = "";
    }
    
    public void visit( final PropertyId e ) throws AlignmentException {
    	if ( e.getURI() != null ) {	
    		String prefix = getPrefixDomain( e.getURI() );
    		String tag = getPrefixName( e.getURI() );
    		String shortCut;
    		if( !prefixList.containsKey( prefix ) ){
    			shortCut = getNamespace();
    			prefixList.put( prefix, shortCut );
    		}
    		else {
    			shortCut = prefixList.get( prefix );
    		}
    		String temp = obj;
    		if( valueRestriction != null && !inClassRestriction && op != Constructor.COMP && flagRestriction == 1 )
    			obj = "\"" + valueRestriction.toString() + "\"";
    		if ( flagRestriction == 1 && inClassRestriction )
				objectsRestriction.add(obj);
    		
		    strBGP += sub + " " + shortCut + ":"+ tag + " " + obj + " ." +NL;
		    if ( op != Constructor.NOT ) {
				Stack<String> tempStack = new Stack<String>();
				if ( stackBGP.isEmpty() ) {
					strBGP_Or = sub + " " + shortCut + ":"+ tag + " " + obj + " ." +NL;
					tempStack.push( strBGP_Or );
				}
				else {
					while ( !stackBGP.isEmpty() ){
						strBGP_Or = stackBGP.pop();
						strBGP_Or += sub + " " + shortCut + ":"+ tag + " " + obj + " ." +NL;
						tempStack.push( strBGP_Or );
					}						
				}
				while ( !tempStack.isEmpty() ){
					stackBGP.push( tempStack.pop() );
				}
			}
    		obj = temp;    		
		}
    }

    public void visit( final PropertyConstruction e ) throws AlignmentException {
    	op = e.getOperator();
    	String str = "";
		if ( op == Constructor.OR ){	
			int size = e.getComponents().size();
			Stack<String> tempStack = new Stack<String>();
			while ( !stackBGP.empty() ) {
		    	tempStack.push( stackBGP.pop() );		    	
		    }
			if ( valueRestriction != null && !inClassRestriction )
				obj = "\"" + valueRestriction.toString() + "\"";
			for ( final PathExpression re : e.getComponents() ) {
			    strBGP += "{" +NL;
			    if ( op != null ) {
					stackOp.push(op);
				}
			    stackBGP.clear();
			    if ( !tempStack.empty() ) {
			    	for( int i=0; i<tempStack.size(); i++ )
			    		stackBGP.push( tempStack.get(i) );
			    }
			    re.accept( this );
			    if ( op == Constructor.NOT )
			    	stackBGP.clear();
			    if( !stackOp.isEmpty() )
					op = stackOp.pop();
		    
			    while ( !stackBGP.empty() ) {
			    	if ( flagRestriction == 0 )
			    		listBGP.put( stackBGP.pop(), condition );
			    	else
			    		stackOR.push( stackBGP.pop() );
		    	}
			    size--;
			    if( size != 0 )
			    	strBGP += "}" + " UNION " + NL;			    
			    else
			    	strBGP += "}" +NL;
			}		    
			objectsRestriction.add( obj );
		}
		else if ( op == Constructor.NOT ) {	
			for ( final PathExpression re : e.getComponents() ) {
				str = "FILTER (NOT EXISTS {" + NL;
			    strBGP += "FILTER (NOT EXISTS {" + NL;
			    if( op != null ) {
					stackOp.push( op );
				}
			    re.accept( this );			    
			    if( !stackOp.empty() )
					op = stackOp.pop();
			    str += "})" + NL;
			    strBGP += "})" + NL;
			    condition = str;
			    while ( !stackBGP.empty() && flagRestriction == 0 ) {	    		
		    		listBGP.put( stackBGP.pop(), condition );	    		
		    	}
			}			
		}
		else if ( op == Constructor.COMP ){			
			int size = e.getComponents().size();
			String tempSub = sub;			
			
			for ( final PathExpression re : e.getComponents() ) {
			    if ( op != null ) {
					stackOp.push(op);
				}
			    re.accept( this );
			    if ( !stackOp.isEmpty() )
					op = stackOp.pop();
			    size--;
			    if ( size != 0 ) {
			    	sub = obj;
			    	if( size == 1 && valueRestriction != null && !inClassRestriction ) {
			    		obj = "\"" + valueRestriction.toString() + "\"";
			    	}
			    	else {
			    		if ( blanks ) {
				    		obj = "_:o" + ++count;
				    	}
				    	else {
				    		obj = "?o" + ++count;
				    	}
			    	}
			    }
			}
			objectsRestriction.add( obj );
			sub = tempSub;
		}		
		else {
			
			int size = e.getComponents().size();
			if ( valueRestriction != null && !inClassRestriction )
				obj = "\"" + valueRestriction.toString() + "\"";
			for ( final PathExpression re : e.getComponents() ) {
			    			 
			    if ( op != null ) {
					stackOp.push( op );
				}
			    re.accept( this );
			    if(!stackOp.isEmpty())
					op = stackOp.pop();
			    size--;	    
			    objectsRestriction.add( obj );
			    if( size != 0 && valueRestriction == null ){
			    	if ( blanks ) {
			    		obj = "_:o" + ++count;
			    	}
			    	else {
			    		obj = "?o" + ++count;
			    	}		    	
			    }
			}		
		}
		
		if ( blanks ) {
    		obj = "_:o" + ++count;
    	}
    	else {
    		obj = "?o" + ++count;
    	}
    }

    public void visit( final PropertyValueRestriction c ) throws AlignmentException {
    	String str = "";
    	if ( c.getComparator().getURI().equals( Comparator.EQUAL.getURI() ) ) {    		
    		strBGP += sub + " ?p ";
    		strBGP_Or = sub + " ?p ";
    		c.getValue().accept( this );
    		strBGP += "\"" + value + "\" ." + NL;
    		strBGP_Or += "\"" + value + "\" ." + NL;    		    		
    	}
    	else if ( c.getComparator().getURI().equals( Comparator.GREATER.getURI() ) ) {    		
    		strBGP += sub + " ?p " + obj + " ." + NL;
    		strBGP_Or = sub + " ?p " + obj + " ." + NL;
			
    		str = "FILTER (xsd:" + uriType + "(" + obj + ") > ";
    		flagRestriction = 1;
    		c.getValue().accept( this );
    		flagRestriction = 0;
			str += "\"" + value + "\")" + NL;			
    	}
    	else if ( c.getComparator().getURI().equals( Comparator.LOWER.getURI() ) ) {    		
    		strBGP += sub + " ?p " + obj + " ." + NL;    		
    		strBGP_Or = sub + " ?p " + obj + " ." + NL;
			
    		str = "FILTER (xsd:" + uriType + "(" + obj + ") < ";
    		flagRestriction = 1;
    		c.getValue().accept( this );
    		flagRestriction = 0;
			str += "\"" + value + "\")" + NL;
    	}
    	
		strBGP += str;
    	stackBGP.push(strBGP_Or);
    	//else throw new AlignmentException( "Cannot dispatch Comparator "+c );
    }

    public void visit( final PropertyDomainRestriction c ) throws AlignmentException {
    	flagRestriction = 1;
		strBGP += sub + " ?p " + obj + " ." + NL;    		
		strBGP_Or = sub + " ?p " + obj + " ." + NL;		

		c.getDomain().accept( this );
    	
    	flagRestriction = 0;
	
    }

    public void visit( final PropertyTypeRestriction c ) throws AlignmentException {
    	
    	String str = "FILTER (datatype(" + objectsRestriction + ") = ";
		
		if ( !objectsRestriction.isEmpty() ) {
			Iterator<String> listObj = objectsRestriction.iterator();
			int size = objectsRestriction.size();
			if ( size > 0 ) {
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
			
			strBGP += str;
		}
		objectsRestriction.clear();
    }
    
    public void visit( final RelationId e ) throws AlignmentException {
		if ( e.getURI() != null ) {
			String prefix = getPrefixDomain(e.getURI());
    		String tag = getPrefixName(e.getURI());
    		String shortCut;
    		if ( !prefixList.containsKey(prefix) ) {
    			shortCut = getNamespace();
    			prefixList.put( prefix, shortCut );
    		}
    		else {
    			shortCut = prefixList.get( prefix );
    		}
			strBGP += sub + " " + shortCut + ":"+ tag + "";
			String str = sub + " " + shortCut + ":"+ tag + "";
		    
		    if ( op == Constructor.TRANSITIVE ) {
		    	strBGP += "*";
		    	str += "*";
		    }
		    if( valueRestriction != null && !inClassRestriction && op != Constructor.COMP && flagRestriction == 1 )			    
					obj = valueRestriction.toString();
		    if ( flagRestriction == 1 && inClassRestriction )
					objectsRestriction.add(obj);
	    	
		    strBGP += " " + obj + " ." + NL;
		    str += " " + obj + " ." + NL;
		    
		    if ( op != Constructor.NOT ) {
				Stack<String> tempStack = new Stack<String>();
				if ( stackBGP.isEmpty() ){
					strBGP_Or = str;
					tempStack.push(strBGP_Or);
				}
				else {
					while (!stackBGP.isEmpty()){
						strBGP_Or = stackBGP.pop();
						strBGP_Or += str;
						tempStack.push( strBGP_Or );
					}						
				}
				while ( !tempStack.isEmpty() ) {
					stackBGP.push( tempStack.pop() );
				}
			}
		}
    }

    public void visit( final RelationConstruction e ) throws AlignmentException {
		String str = "";
		op = e.getOperator();

		if ( op == Constructor.OR )  {	
			int size = e.getComponents().size();
			Stack<String> tempStack = new Stack<String>();
			while ( !stackBGP.empty() ) {
		    	tempStack.push(stackBGP.pop());		    	
		    }
			if ( valueRestriction != null && !inClassRestriction )
				obj = valueRestriction.toString();
			String temp = obj;
			for ( final PathExpression re : e.getComponents() ) {
			    writer.print(linePrefix);
			    strBGP += "{" + NL;
			    if ( op != null ) {
					stackOp.push( op );
				}
			    stackBGP.clear();
			    if(!tempStack.empty()){
			    	for(int i=0; i<tempStack.size(); i++)
			    		stackBGP.push(tempStack.get(i));
			    }
			    re.accept( this );
			    if ( op == Constructor.NOT ) {
			    	stackBGP.clear();
			    }
			    if ( !stackOp.isEmpty() )
					op = stackOp.pop();
			    
			    while ( !stackBGP.empty() ) {
			    	if ( flagRestriction == 0 )
			    		listBGP.put( stackBGP.pop(), condition );
			    	else
			    		stackOR.push( stackBGP.pop() );	    		
		    	}
			    obj = temp;
			    size--;
			    if ( size != 0 )
			    	strBGP += "}" + "UNION " + NL;
			    else
			    	strBGP += "}" + NL;
			}
			
			objectsRestriction.add( obj );
		}
		else if ( op == Constructor.NOT ) {		
			for ( final PathExpression re : e.getComponents() ) {
				str = "FILTER (NOT EXISTS {" + NL;
			    strBGP += "FILTER (NOT EXISTS {" + NL;
			    if( op != null ) {
					stackOp.push( op );
				}
			    re.accept( this );			    
			    if( !stackOp.empty() )
					op = stackOp.pop();
			    str += "})" + NL;
			    strBGP += "})" + NL;
			    condition = str;
			    while ( !stackBGP.empty() && flagRestriction == 0 ) {	    		
		    		listBGP.put( stackBGP.pop(), condition );	    		
		    	}
			}			
		}
		else if ( op == Constructor.COMP ) {
			int size = e.getComponents().size();
			String temp = sub;
			for ( final PathExpression re : e.getComponents() ) {			   
			    if ( op != null ) {
					stackOp.push( op );
				}
			    re.accept( this );
			    if( !stackOp.isEmpty() )
					op = stackOp.pop();
			    size--;
			    if( size != 0 ) {
			    	sub = obj;
			    	if ( size == 1 && valueRestriction != null && !inClassRestriction ) {
			    		obj = valueRestriction.toString();
			    	}
			    	else {
			    		if ( blanks ) {
				    		obj = "_:o" + ++count;
				    	}
				    	else {
				    		obj = "?o" + ++count;				    		
				    	}
			    		objectsRestriction.add( obj );
			    	}
			    					    	
			    }			    
			}			
			sub = temp;
		}
		else if ( op == Constructor.INVERSE ) {
			String tempSub = sub;
			for ( final PathExpression re : e.getComponents() ) {
			    String temp = sub;
			    sub = obj;
			    obj = temp;
			    if( op != null ) {
					stackOp.push( op );
				}
			    re.accept( this );
			    if ( !stackOp.isEmpty() )
					op = stackOp.pop();
			    sub = tempSub;
			}
		}
		else if ( op == Constructor.SYMMETRIC ) {
			String tempSub = sub;
			for ( final PathExpression re : e.getComponents() ) {
			    strBGP += "{" + NL;
			    if ( op != null ) {
					stackOp.push(op);
				}
			    re.accept( this );
			    if( !stackOp.isEmpty() )
					op = stackOp.pop();
			    objectsRestriction.add( obj );
			    String temp = sub;
			    sub = obj;
			    obj = temp;
			    strBGP += "} UNION {" + NL;
			    if( op != null ) {
					stackOp.push(op);
				}
			    re.accept( this );
			    if(!stackOp.isEmpty())
					op = stackOp.pop();
			    objectsRestriction.add( obj );
			    strBGP +="}" + NL;
			    
			}
			sub = tempSub;
		}
		else if (op == Constructor.TRANSITIVE){						
			for ( final PathExpression re : e.getComponents() ) {			    
			    if(op != null){
					stackOp.push(op);
				}
			    re.accept( this );
			    if(!stackOp.isEmpty())
					op = stackOp.pop();
			}
		}
		else if ( op == Constructor.REFLEXIVE ) {						
			String strObj = obj;
			for ( final PathExpression re : e.getComponents() ) {			    		    
			    if ( op != null ) {
					stackOp.push( op );
				}
			    obj = sub;
			    re.accept( this );
			    obj = strObj;
			    if(!stackOp.isEmpty())
					op = stackOp.pop();
			}
		}
		else {
			
			int size = e.getComponents().size();
			if ( valueRestriction != null && !inClassRestriction )
				obj = valueRestriction.toString();
			for ( final PathExpression re : e.getComponents() ) {
						 
			    if ( op != null ) {
					stackOp.push( op );
				}
			    re.accept( this );
			    if ( !stackOp.isEmpty() )
					op = stackOp.pop();
			    size--;
			    objectsRestriction.add( obj );
			    if ( size != 0 && valueRestriction == null ) {
			    	if ( blanks ) {
			    		obj = "_:o" + ++count;
			    	}
			    	else {
			    		obj = "?o" + ++count;
			    	}			    	
			    }
			}		
		}
		
		if ( blanks ) {
    		obj = "_:o" + ++count;
    	}
    	else {
    		obj = "?o" + ++count;
    	}
    }
	
    public void visit(final RelationCoDomainRestriction c) throws AlignmentException {
    	flagRestriction = 1;		
    	
    	c.getCoDomain().accept( this );
    	
    	flagRestriction = 0;
    }

    public void visit(final RelationDomainRestriction c) throws AlignmentException {

    	flagRestriction = 1;
		
		strBGP += sub + " ?p " + obj + " ." + NL;    		
		
		c.getDomain().accept( this );
    	
    	flagRestriction = 0;
    }

    public void visit( final InstanceId e ) throws AlignmentException {
		if ( e.getURI() != null ) {
			String prefix = getPrefixDomain( e.getURI() );
    		String tag = getPrefixName( e.getURI() );
    		String shortCut;
    		if ( !prefixList.containsKey( prefix) ){
    			shortCut = getNamespace();
    			prefixList.put( prefix, shortCut );
    		}
    		else {
    			shortCut = prefixList.get( prefix );
    		}
			if ( flagRestriction != 1 )
				strBGP += shortCut + ":"+ tag + " ?p ?o1 ." +NL;
			else
				instance = shortCut + ":"+ tag;
		}
    }
    
    public void visit( final Value e ) throws AlignmentException {
    	if (e.getType() != null) {
	    	String str = e.getType().toString();
	    	int index;
	    	if ( str.contains("#") )
	    		index = str.lastIndexOf("#");
	    	else
	    		index = str.lastIndexOf("/");
	    	uriType = str.substring( index+1 );
    	}
    	value = e.getValue();
    }
	
    public void visit( final Apply e ) throws AlignmentException {}

    public void visit( final Transformation transf ) throws AlignmentException {}

    public void visit( final Datatype e ) throws AlignmentException {
    	int index;
    	if ( e.getType().contains("#") )
    		index = e.getType().lastIndexOf("#");
    	else
    		index = e.getType().lastIndexOf("/");
    	datatype = e.getType().substring( index+1 );
    }

}
