/*
 * $Id$
 *
 * Copyright (C) 2006 Digital Enterprise Research Insitute (DERI) Innsbruck
 * Sourceforge version 1.5 - 2006 -- then RelationExpr
 * Copyright (C) INRIA, 2009
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

package fr.inrialpes.exmo.align.impl.edoal;

import java.net.URI;
import java.net.URISyntaxException;

import org.semanticweb.owl.align.AlignmentException;

/**
 * A simple Id to represent a Relation.
 * 
 * @author richi
 * 
 */
public class RelationId extends RelationExpression implements Id {
    /** Holds the identifier. */
    private String id;
    
    URI uri;

    public RelationId(final String id) throws AlignmentException {
	if (id == null) {
	    throw new NullPointerException("The id must not be null");
	}
	if (id.length() <= 0) {
	    throw new IllegalArgumentException(
					       "The id must be longer than 0 characters");
	}
	this.id = id;
	try {
	    uri = new URI( id );
	} catch ( URISyntaxException mfuex ) {
	    throw new AlignmentException( "Not an URI "+id, mfuex );
	}
    }
    
    public RelationId( final URI u ) {
	if ( u == null ) {
	    throw new NullPointerException("The URI must not be null");
	}
	uri = u;
	id = u.toString();
    }
    
    public URI getURI(){
	return uri;
    }

    public String plainText() {
	return id;
    }

    /**
     * <p>
     * Returns a simple description of this object. <b>The format of the
     * returned String is undocumented and subject to change.</b>
     * <p>
     * <p>
     * An expamle return String could be:
     * <code>RelationId: http://my/super/class</code>
     * </p>
     */
    public String toString() {
	return "RelationId: " + id;
    }

    public int hashCode() {
	int result = 17;
	result = result * 37 + uri.hashCode();
	return result;
    }

    public boolean equals(final Object obj) {
	if (obj == this) {
	    return true;
	}
	if (!(obj instanceof RelationId)) {
	    return false;
	}
	RelationId i = (RelationId) obj;
	return id.equals(i.id);
    }
    /*
    public Object clone() {
	return super.clone();
    }
    */
}
