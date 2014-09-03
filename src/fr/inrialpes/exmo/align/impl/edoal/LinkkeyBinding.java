/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.inrialpes.exmo.align.impl.edoal;

import org.semanticweb.owl.align.AlignmentException;

/**
 *
 * @author Nicolas Guillouet <nicolas@meaningengines.com>
 */
public abstract class LinkkeyBinding {
    
    private PathExpression expression1;
    private PathExpression expression2;
    
    public LinkkeyBinding(PathExpression expression1, PathExpression expression2) throws AlignmentException {
        this.expression1 = expression1;
        this.expression2 = expression2;
    }
   
    
    public PathExpression getExpression1(){
        return expression1;
    }
    public PathExpression getExpression2(){
        return expression2;
    }
    
    public abstract void accept( EDOALVisitor visitor ) throws AlignmentException;
    
}
