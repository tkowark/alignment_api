/*
 * $Id$
 *
 * Copyright (C) 2006 Digital Enterprise Research Insitute (DERI) Innsbruck
 * Sourceforge version 1.5 - 2006
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

public class ClassDomainRestriction extends ClassRestriction implements Cloneable {

    ClassExpression domain = null;

    /**
     * Constructs a typeCondition with the given restriction.
     * 
     * @param res
     *            the restriction for the domain
     * @param target
     *            the target expression which should be restricted
     * @throws NullPointerException
     *             if the restriction is null
     */
    public ClassDomainRestriction(final PathExpression p,
				final ClassExpression cl) {
	super(p);
	// Check that this is a property
	domain = cl;
    }

    public ClassExpression getDomain() {
	return domain;
    }

    public void setDomain( ClassExpression cl ) {
	domain = cl;
    }

}
