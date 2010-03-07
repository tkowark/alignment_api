/*
 * $Id: EDOALCell.java,v 1.2 2008/06/29 16:18:49 jeuzenat Exp $
 *
 * Sourceforge version 1.2 - 2008
 * Copyright (C) INRIA, 2007-2009
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

import java.io.PrintStream;
import java.io.IOException;
import java.util.Comparator;
import java.lang.ClassNotFoundException;
import java.lang.Float;
import java.lang.Double;
import java.net.URISyntaxException;
import java.net.URI;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.Relation;

import fr.inrialpes.exmo.align.impl.BasicCell;

import fr.inrialpes.exmo.align.impl.rel.*;

/**
 * This implements a particular of ontology correspondence when it
 * is a correspondence from the EDOAL Mapping Language.
 * 
 * The current implementation of this class consists of encapsulating
 * the EDOAL Mapping Rule object and reimplementing the ALignment API
 * accessors around it.
 * This is fine but there is another implementation that would be more
 * satisfactory:
 * 
 * Reimplementing the EDOAL Mapping Rules in terms of a proper Cell
 * with:
 * id: URI id
 * object1: Resource source
 * object2: Resource target
 * relation: The class name of the rule
 * measure: float measure
 *  -- no real use of direction.
 *
 * @author Jérôme Euzenat
 * @version $Id: EDOALCell.java,v 1.2 2008/06/29 16:18:49 jeuzenat Exp $ 
 */

public class EDOALCell extends BasicCell {

    // JE2009: This has been added for 
    private URI id; // This is the id

    public void accept( AlignmentVisitor visitor) throws AlignmentException {
        visitor.visit( this );
    }

    /** Creation **/
    public EDOALCell( String id, Expression ob1, Expression ob2, EDOALRelation rel, double m ) throws AlignmentException {
	super( id, (Object)ob1, (Object)ob2, rel, m );
    };

    public boolean equals( Cell c ) {
	if ( c instanceof EDOALCell ){
	    return ( object1.equals(c.getObject1()) && object2.equals(c.getObject2()) && strength == c.getStrength() && (relation.equals( c.getRelation() )) );
	} else {
	    return false;
	}
    }

    // JE// Maybe do it in case Expressions have URI
    public URI getObject1AsURI( Alignment al ) throws AlignmentException {
	return null;
	//throw new AlignmentException( "Cannot convert to URI "+object1 );
    }
    public URI getObject2AsURI( Alignment al ) throws AlignmentException {
	return null;
	//throw new AlignmentException( "Cannot convert to URI "+object2 );
    }

    public Cell inverse() throws AlignmentException {
	return (Cell)new EDOALCell( (String)null, (Expression)object2, (Expression)object1, (EDOALRelation)relation.inverse(), strength );
	// The same should be done for the measure
    }

}

