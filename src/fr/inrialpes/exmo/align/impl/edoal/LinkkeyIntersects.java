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
public class LinkkeyIntersects  extends LinkkeyBinding{
    
    public LinkkeyIntersects(PathExpression expression1, PathExpression expression2) throws AlignmentException {
        super(expression1, expression2);
    }
    
    public void accept( EDOALVisitor visitor ) throws AlignmentException {
	visitor.visit( this );
    }
}
