/*
 * $Id$
 *
 * Copyright (C) INRIA Rhône-Alpes, 2003-2005
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

package fr.inrialpes.exmo.align.impl; 

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.lang.UnsupportedOperationException;

/**
 * This class builds a composite iterator from two iterators
 * This helps writing more concise code.
 *
 * This is the naive implementation (can be optimized)
 *
 * @author Jérôme Euzenat
 * @version $Id$ 
 */

public final class ConcatenatedIterator implements Iterator {
    private Iterator it1 = null;
    private Iterator it2 = null;
    public ConcatenatedIterator ( Iterator i1, Iterator i2 ){
	it1 = i1;
	it2 = i2;
    }
    public boolean hasNext() {
	if ( it1.hasNext() || it2.hasNext() ) return true;
	else return false;
    }
    public Object next() throws NoSuchElementException {
	if ( it1.hasNext() ) return it1.next();
	else return it2.next();
    }
    public void remove() throws UnsupportedOperationException {
	throw new UnsupportedOperationException();
    }
}
