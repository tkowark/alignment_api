/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inrialpes.exmo.align.impl.edoal;

import fr.inrialpes.exmo.align.parser.TypeCheckingVisitor;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.semanticweb.owl.align.AlignmentException;

/**
 *
 * @author Nicolas Guillouet <nicolas@meaningengines.com>
 */
public class Linkkey {
    public static final String PLAIN = "plain";
    public static final String WEAK = "weak";
    public static final String STRONG = "strong";
    private static List<String> ALLOWED_TYPES = Arrays.asList(PLAIN, WEAK, STRONG);
    
    private String type;
    private Set<LinkkeyBinding> bindings;
    
    public void accept(EDOALVisitor visitor) throws AlignmentException {
        visitor.visit(this);
    }
    
    public TypeCheckingVisitor.TYPE accept( TypeCheckingVisitor visitor ) throws AlignmentException {
	return visitor.visit(this);
    }

    public Linkkey() throws AlignmentException {
        this(PLAIN);
    }

    public Linkkey(String type) throws AlignmentException {
        if(!ALLOWED_TYPES.contains(type)){
            throw new AlignmentException("The  type " + type + " is not allowed !");
        }
        this.type = type;
        bindings = new HashSet<>();
    }

    public void addBinding(LinkkeyBinding binding){
        bindings.add(binding);
    }
    
    public Set<LinkkeyBinding> bindings(){
        return bindings;
    }
    
    public String getType() {
        return type;
    }
}
