/*
 * $Id$
 *
 * Copyright (C) INRIA Rhône-Alpes, 2004
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

import java.io.PrintStream;
import java.io.IOException;
import java.util.Enumeration;
import java.net.URI;

import org.semanticweb.owl.align.Parameters;

/**
 * Represents the implementation of a similarity measure
 *
 * @author Jérôme Euzenat
 * @version $Id$ 
 */

public interface Similarity
{
    // These parameters contains usually:
    // ontology1 and ontology2
    // It would be better if they where explicit...
    // Apparently the initialize also compute the similarity
    public void initialize( Parameters p );
    public double getSimilarity( URI u1, URI u2 );
}

