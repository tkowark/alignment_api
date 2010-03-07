/*
 * $Id$
 *
 * Copyright (C) 2006 Digital Enterprise Research Insitute (DERI) Innsbruck
 * Sourceforge version 1.2 - 2006
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
 * Id to represent a simple valuestring.
 * </p>
 * <p>
 * $Id: Value.java,v 1.2 2006/10/30 15:24:43 poettler_ric Exp $
 * </p>
 * 
 * @author richi
 * @version $Revision: 1.2 $
 * @date $Date$
 */
public class Value { //implements Cloneable, Visitable {

    /** Holds the value */
    private String value;

    /**
     * Constructs an object with the given value.
     * 
     * @param value
     *            the value for this object.
     * @throws NullPointerException
     *             if the value is {@code null}
     * @throws IllegalArgumentException
     *             if the value isn't longer than 0
     */
    public Value( final String value ) {
	if (value == null) {
	    throw new NullPointerException("The value must not be null");
	}
	this.value = value;
    }

    //    public void accept(AlignmentVisitor visitor) throws AlignmentException {
    //	visitor.visit(this);
    //    }

    public String plainText() {
	return value;
    }

    public int hashCode() {
	return value.hashCode();
    }

    public boolean equals(final Object o) {
	if ( o == this ) {
	    return true;
	}
	if (!(o instanceof Value)) {
	    return false;
	}
	Value s = (Value) o;
	return value.equals(s.value);
    }
    /*
    public Object clone() {
	return super.clone();
    }
    */

    /**
     * <p>
     * Returns a short description about this object. <b>The format of the
     * returned string is undocumentd and subject to change.</b>
     * </p>
     * <p>
     * An example return string could be: {@code 15}
     * </p>
     */
    public String toString() {
	return value;
    }
}
