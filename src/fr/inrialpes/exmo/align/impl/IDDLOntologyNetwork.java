/*
 * $Id: IDDLOntologyNetwork.java 987 2009-05-27 13:48:33Z euzenat $
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

package fr.inrialpes.exmo.align.impl; 

import java.lang.Cloneable;
import java.lang.Iterable;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.Hashtable;
import java.net.URI;

import fr.inrialpes.exmo.ontowrap.Ontology;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.LogicOntologyNetwork;

import fr.inrialpes.exmo.iddl.IDDLReasoner;
import fr.inrialpes.exmo.iddl.conf.Semantics;

/**
 * Represents a distributed system of aligned ontologies or network of ontologies.
 *
 * @author Jérôme Euzenat
 * @version $Id: BasicOntologyNetwork.java 987 2009-05-27 13:48:33Z euzenat $ 
 */

public class IDDLOntologyNetwork extends BasicOntologyNetwork implements LogicOntologyNetwork {

    IDDLReasoner reasoner = null;
    String semantics = "";

    protected void init(){
	if ( reasoner == null ){
	    if ( semantics.equals("DL") ) {
		reasoner = new IDDLReasoner( Semantics.DL );
	    } else {
		reasoner = new IDDLReasoner( Semantics.IDDL );
	    }
	    for( URI u : getOntologies() ){
		reasoner.addOntology( u );
	    }
	    for( Alignment al : alignments ){
		reasoner.addAlignment( al );
	    }
	}
    }

    public void setSemantics( String s ){
	semantics = s;
	if ( reasoner != null ) {
	    if ( semantics.equals( "DL" ) ) {
		reasoner.setSemantics( Semantics.DL );
	    } else {
		reasoner.setSemantics( Semantics.IDDL );
	    }
	}
    };
    public String getSemantics(){
	return semantics;
    };
    public boolean isConsistent(){
	init();
	return reasoner.isConsistent();
    }; 
    public boolean isEntailed( Alignment al ){
	init();
	return reasoner.isEntailed( al );
    };

}

