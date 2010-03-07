/*
 * $Id$
 *
 * Copyright (C) 2006 Digital Enterprise Research Insitute (DERI) Innsbruck
 * Sourceforge version 1.5 - 2006 -- then AttributeOccurenceCondition.java
 * Copyright (C) INRIA, 2009-2010
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

/**
 * <p>
 * Represents a attributeOccurenceRestriction tag for a ClassExpressions.
 * </p>
 * <p>
 * Created on 24-Mar-2005 Committed by $Author: poettler_ric $
 * </p>
 * <p>
 * $Id: ClassOccurenceRestriction.java,v 1.5 2006/11/15 16:01:17 poettler_ric
 * Exp $
 * </p>
 * 
 * @author Francois Scharffe
 * @author Adrian Mocan
 * @author Richard Pöttler
 * @version $Revision: 1.6 $ $Date$
 */
public class ClassOccurenceRestriction extends ClassRestriction implements Cloneable {

    Comparator comparator = null;
    int occurence = 1;

    /**
     * Constructs a attributeOccurenceRestriction with the given restriction.
     * 
     * @param attribute
     *            the attribute on which the restriction should be applied
     * @param restriction
     *            the restriction for the domain
     * @throws NullPointerException
     *             if the restriction is null
     */
    public ClassOccurenceRestriction( final PathExpression p, Comparator c, int n ) {
	super( p );
	constrainedPath = p;
	comparator = c;
	occurence = n;
    }

    public int getOccurence() { return occurence; }
    public void setOccurence( int n ) { occurence = n; }
    public Comparator getComparator() { return comparator; }
    public void setComparator( Comparator c ) { comparator = c; }
    /*
    public Object clone() {
	return super.clone();
    }
    */
}
