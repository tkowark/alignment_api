/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inrialpes.exmo.align.impl.edoal;

import fr.inrialpes.exmo.align.impl.Extensions;
import fr.inrialpes.exmo.align.parser.TypeCheckingVisitor;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.semanticweb.owl.align.AlignmentException;

/**
 *
 * @author Nicolas Guillouet <nicolas@meaningengines.com>
 */
public class Linkkey implements Extensable{
    
    private Set<LinkkeyBinding> bindings;
    protected Extensions extensions = null;
    
    public void accept(EDOALVisitor visitor) throws AlignmentException {
        visitor.visit(this);
    }
    
    public TypeCheckingVisitor.TYPE accept( TypeCheckingVisitor visitor ) throws AlignmentException {
	return visitor.visit(this);
    }

    public Linkkey() {
        bindings = new HashSet<>();
    }


    public void addBinding(LinkkeyBinding binding){
        bindings.add(binding);
    }
    
    public Set<LinkkeyBinding> bindings(){
        return bindings;
    }
    

    public Collection<String[]> getExtensions(){ 
	if ( extensions != null ) return extensions.getValues();
	else return null;
    }
    public void setExtensions( Extensions p ){
	extensions = p;
    }

    public void setExtension( String uri, String label, String value ) {
	if ( extensions == null ) extensions = new Extensions();
	extensions.setExtension( uri, label, value );
    };

    public String getExtension( String uri, String label ) {
	if ( extensions != null ) {
	    return extensions.getExtension( uri, label );
	} else {
	    return (String)null;
	}
    };
}
