/*
 * $Id$
 *
 * Copyright (C) 2006 Digital Enterprise Research Insitute (DERI) Innsbruck
 * Sourceforge version 1.7 - 2006 -- then AttributeExpr.java
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

/**
 * <p>
 * A simple Id to represent a Property.
 * </p>
 * <p>
 * $Id$
 * </p>
 * 
 * @author richi
 * @version $Revision: 1.7 $
 * @date $Date: 2010-03-07 20:40:05 +0100 (Sun, 07 Mar 2010) $
 */
public class PropertyId extends PropertyExpression implements Id {
    /** Holds the identifier. */
    private String id;
	
    URI uri;

    public PropertyId( final URI u ) {
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
	return toString();
    }

    /**
     * <p>
     * Returns a simple description of this object. <b>The format of the
     * returned String is undocumented and subject to change.</b>
     * <p>
     * <p>
     * An expamle return String could be:
     * <code>attributeId: http://my/super/attribute</code>
     * </p>
     */
    public String toString() {
	return "PropertyId: " + id;
    }
	
}
