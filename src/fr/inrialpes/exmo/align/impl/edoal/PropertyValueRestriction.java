/*
 * $Id$
 *
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
 * Represents a attributeValueRestriction tag for a ClassExpressions.
 * </p>
 * <p>
 * Created on 24-Mar-2005 Committed by $Author: poettler_ric $
 * </p>
 * <p>
 * $Id$
 * </p>
 * 
 * @author Francois Scharffe
 * @author Adrian Mocan
 * @author Richard Pöttler
 * @version $Revision: 1.6 $ $Date: 2010-03-07 20:40:05 +0100 (Sun, 07 Mar 2010) $
 */
public class PropertyValueRestriction extends PropertyRestriction implements Cloneable {

    Comparator comparator = null;
    ValueExpression value = null;

    /**
     * Constructs a attributeValueRestriction with the given restriction.
     * 
     * @param attribute
     *            the attribute on which the restriction should be applied
     * @throws NullPointerException
     *             if the restriction is null
     */
    public PropertyValueRestriction() {
	super();
    }

    /**
     * Constructs a attributeValueRestriction with the given restriction.
     * 
     * @param attribute
     *            the attribute on which the restriction should be applied
     * @param restriction
     *            the restriction for the domain
     * @param target
     *            the target expression which should be restricted
     * @throws NullPointerException
     *             if the restriction is null
     */
    public PropertyValueRestriction(final Comparator comp, final ValueExpression v) {
	super();
	comparator = comp;
	value = v;
    }

    public Comparator getComparator(){
	return comparator;
    }
    
    public void setComparator( Comparator comp ){
	comparator = comp;
    }
    
    public ValueExpression getValue(){
	return value;
    }
    public void setValue( ValueExpression v ){
	value = v;
    }
    
    /*
    public Object clone() {
	return super.clone();
    }
    */
}
