package fr.inrialpes.exmo.align.gen;

import java.util.Enumeration;
import java.util.Vector;

import fr.inrialpes.exmo.align.service.jade.messageontology.Parameter;
import org.semanticweb.owl.align.Parameters;

/*
 * Retains the list of parameters that the test generator must receive
 */

public class GeneratorParameters implements Parameters {
	Vector<Parameter> parameters;
	
	public GeneratorParameters ( ) {			//the constructor
		this.parameters = new Vector<Parameter>();
	}

	//get the names of parameters
	@Override
	public Enumeration<String> getNames() {
		if ( this.parameters.isEmpty() )			//no parameter
			return null;
		
		Vector<String> names = new Vector<String>();//return the list of names if parameter is no empty
		for ( Parameter p : this.parameters ) 
			names.add( p.getName() );
		return names.elements();
	}

	//get the value of the parameter name
	//return null if the parameter is unset or it's not on the vector
	public String getValue(String name){
		for ( Parameter p : this.parameters )
			if ( p.getName().equals( name ) )
				return p.getValue();
		return null;
	}
	
	//get the parameter
	//return (null) if no corresponding parameter exist
	@Override
	public String getParameter(String name) {
		if ( this.parameters.isEmpty() )			//empty vector
			return null;
		for ( Parameter p : this.parameters )		//check if the parameter exists
			if ( p.getName().equals( name ) )
				return p.getName();
		return null;								//the parameter does not exists
	}

	//set the value of a parameter
	@Override
	public void setParameter(String name, String value) {
		for ( Parameter p : this.parameters )		//check if we have already the entry
			if ( p.getName().equals( name ) )  {
				p.setValue( value );
                                return;
                    }
 		
		Parameter p = new Parameter();				//add a new one if the parameter is not on the 
		p.setName( name );
		p.setValue( value );
		this.parameters.add( p );					//else add a new entry in parameters
	}

	//unset the value of a parameter
	@Override
	public void unsetParameter(String name) {
		for ( Parameter p : this.parameters )		//check if the parameter is in the list
			if ( p.getName().equals( name ) )
				p.setValue( null );					//unset the value of the parameter
	}
	
	//returns the parameter from the position index
	public Parameter getParameter (int index) {
		return this.parameters.get( index );
	}
	
	//print the parameters
	@Override	
	public void write() {		
		System.out.println( "Parameters:" );
		for ( Parameter p : this.parameters )
			System.out.println( "Name [" + p.getName() + "]	Value [" + p.getValue() + "]" );	
	}
	
	public int size() {
		return this.parameters.size();				//returns the list of all parameters
	}
	

}
