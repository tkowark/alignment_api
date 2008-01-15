/*
 * $Id$
 *
 * Copyright (C) INRIA Rhône-Alpes, 2003-2007
 * Copyright (C) CNR Pisa, 2005
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

import java.net.URI;

/**
 * Store the information regarding ontologies in a specific structure
 */

public class Ontology {
    protected URI uri = null;
    protected URI file = null;
    protected Object onto = null;
    protected URI formalismURI = null;
    protected String formalism = null;

    public Ontology() {};

    public URI getURI() { return uri; }
    public URI getFile() { return file; }
    public Object getOntology() { return onto; }
    public URI getFormURI() { return formalismURI; }
    public String getFormalism() { return formalism; }

    public void setURI( URI uri ) { this.uri = uri; }
    public void setFile( URI file ) { this.file = file; }
    public void setOntology( Object o ) { this.onto = o; }
    public void setFormURI( URI u ) { formalismURI = u; }
    public void setFormalism( String name ) { formalism = name; }
}
