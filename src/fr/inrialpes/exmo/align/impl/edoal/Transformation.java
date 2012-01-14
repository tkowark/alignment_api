/*
 * $Id$
 *
 * Copyright (C) INRIA, 2010, 2012
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 */

package fr.inrialpes.exmo.align.impl.edoal;

import org.semanticweb.owl.align.AlignmentException;

import fr.inrialpes.exmo.align.parser.TypeCheckingVisitor;

/**
 * This implements a transformation of an entity into another.
 * The transformation is specified usually through function and can go one way or bith ways 
 *
 * @author Jérôme Euzenat
 * @version $Id$ 
 */

public class Transformation {

    // JE: bad values...
    private String type; // "oo", "o-" or "-o"
    private ValueExpression expr1;
    private ValueExpression expr2;

    public void accept( EDOALVisitor visitor) throws AlignmentException {
        visitor.visit( this );
    }

    public void accept( TypeCheckingVisitor visitor ) throws AlignmentException {
	visitor.visit(this);
    }

    /** Creation **/
    public Transformation( String type, ValueExpression ob1, ValueExpression ob2 ) throws AlignmentException {
	this.type = type;
	expr1 = ob1;
	expr2 = ob2;
    };

    public ValueExpression getObject1() {
	return expr1;
    }
    public ValueExpression getObject2() {
	return expr2;
    }
    public String getType() {
	return type;
    }

    public Transformation inverse() throws AlignmentException {
	String newType;
	if ( type.equals("oo") ) newType = type;
	else if ( type.equals( "o-" ) ) newType = "-o";
	else if ( type.equals( "-o" ) ) newType = "o-";
	else throw new AlignmentException( "Incorrect type specification : "+type );
	return new Transformation( newType, expr2, expr1 );
    }

}

