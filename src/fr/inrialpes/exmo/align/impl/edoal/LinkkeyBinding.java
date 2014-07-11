/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.inrialpes.exmo.align.impl.edoal;

import fr.inrialpes.exmo.align.parser.TypeCheckingVisitor;
import java.util.Arrays;
import java.util.List;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Visitable;

/**
 *
 * @author Nicolas Guillouet <nicolas@meaningengines.com>
 */
public class LinkkeyBinding {
    public static final String IN = "in";
    public static final String EQ = "eq";
    private static List<String> ALLOWED_TYPES = Arrays.asList(IN, EQ);
    
    private PathExpression expression1;
    private PathExpression expression2;
    private String type;
    
    public LinkkeyBinding(PathExpression expression1, PathExpression expression2, String type) throws AlignmentException {
        if(!ALLOWED_TYPES.contains(type)){
            throw new AlignmentException("The  type " + type + " is not allowed !");
        }
        this.type = type;
        this.expression1 = expression1;
        this.expression2 = expression2;
    }
    
    
    public String getType(){
        return type;
    }
    
    public PathExpression getExpression1(){
        return expression1;
    }
    public PathExpression getExpression2(){
        return expression2;
    }
}
