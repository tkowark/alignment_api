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

package fr.inrialpes.exmo.align.gen.alt;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.Ontology;

import java.util.Properties;
import java.util.Enumeration;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

import fr.inrialpes.exmo.align.gen.Alterator;
import fr.inrialpes.exmo.align.gen.ParametersIds;

import org.semanticweb.owl.align.Alignment;

public class EmptyModification extends BasicAlterator {

    protected boolean relocateSource = false;

    public EmptyModification( OntModel o ) {
	modifiedModel = o;
	// get the default namespace of the model
	modifiedOntologyNS = modifiedModel.getNsPrefixURI("");
    };

    // Clearly here setDebug, setNamespace are important

    public Alterator modify( Properties params ) {
	relocateSource = ( params.getProperty( "copy101" ) != null );

	if ( alignment == null ) {
	    initOntologyNS = modifiedOntologyNS;

	    alignment = new Properties();

	    for ( OntClass cls : modifiedModel.listNamedClasses().toList() ) {
		if ( cls.getNameSpace().equals( modifiedOntologyNS ) ) {
		    String uri = cls.getURI();
		    alignment.put( uri, uri ); //add them to the initial alignment
		} 
	    }
	    for ( OntProperty prop : modifiedModel.listAllOntProperties().toList() ) {
		if ( prop.getNameSpace().equals( modifiedOntologyNS ) ) {
		    String uri = prop.getURI();
		    alignment.put( uri, uri ); //add them to the initial alignment
		}
	    }
	}
	return this;
    }

    // In case of 101, I want to have the empty test
    public void relocateTest( String base1, String base2 ) {
	super.relocateTest( relocateSource?base2:base1, base2 );
    }    

    //the initial reference alignment
    public void initializeAlignment( Properties al ) {
        alignment = al;

        Enumeration e = alignment.propertyNames();
        String aux = (String)e.nextElement();
        initOntologyNS = aux.substring(0, aux.lastIndexOf("#")+1);
    }

}
