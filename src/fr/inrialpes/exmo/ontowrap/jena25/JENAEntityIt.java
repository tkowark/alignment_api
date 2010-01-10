/*
 * $Id$
 *
 * Copyright (C) INRIA, 2008-2010
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

package fr.inrialpes.exmo.ontowrap.jena25;

import java.net.URI;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.NoSuchElementException;

import com.hp.hpl.jena.ontology.OntResource;


public class JENAEntityIt implements Iterator<OntResource> {

	    private Iterator<OntResource> it;
	    private OntResource current;
	    private URI ontURI;

	    public JENAEntityIt(URI ontURI, Iterator<OntResource> entityIt) {
		this.ontURI = ontURI;
		this.it= entityIt;
	    }

	    private void setNext() {
		while (current==null) {
		    current = it.next();
		    if (current.getURI()==null) {// || !current.getURI().startsWith(ontURI.toString())) {
			current=null;
		    }
		}
	    }
	    public boolean hasNext() {
		try {
		    setNext();
		    return current!=null;
		}
		catch (NoSuchElementException e) {
			return false;
		}
	    }

	    public OntResource next() {
		setNext();
		OntResource returnR = current;
		current=null;
		return returnR;
	    }

	    public void remove() {
		throw new UnsupportedOperationException();
	    }
}
