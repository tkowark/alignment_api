/*
 * $Id$
 *
 * Copyright (C) 2011, INRIA
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 */


package fr.inrialpes.exmo.align.gen.inter;

import com.hp.hpl.jena.ontology.OntModel;
import org.semanticweb.owl.align.Alignment;
import java.util.Properties;

public interface AlignedOntologyGenerator {

    //generate an Alignment refering to the generated Ontology as onto2 (to be saved)
    public Alignment generate( OntModel o, Properties p );

}
