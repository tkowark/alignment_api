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

/* 
 * Generates a single test by taking as input either an ontology
 * or an ontology and an alignment and generating a modified ontology
 * and an alignment between this one and either the initial ontology
 * or the first ontology of the alignments.
 *
 * Alterations are specified in a list of parameters.
 */

package fr.inrialpes.exmo.align.gen;

import com.hp.hpl.jena.ontology.OntModel;
import fr.inrialpes.exmo.align.gen.inter.AlignedOntologyGenerator;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.ontowrap.Ontology;
import fr.inrialpes.exmo.ontowrap.jena25.JENAOntology;
import java.util.Properties;
import org.semanticweb.owl.align.Alignment;

public class TestGenerator implements AlignedOntologyGenerator {
    private Properties parameters;                                              //the set of parameters
    private Properties params;                                                  //the modifications
    private OntModel model;                                                     //initial ontology
    private OntModel modifiedModel;                                             //modified ontology
    private Alignment alignment;                                                //the reference alignment
     private String namespace = "";                                             //the namespace
    private OntologyModifier modifier = null;                                   //the modifier

    public TestGenerator() {}

    //returns the modified ontology
    public Ontology getModifiedOntology() {
        JENAOntology onto = new JENAOntology();                                 //cast the model into Ontology
        onto.setOntology( this.modifiedModel );
        return onto;                                                            //return the ontology
    }

    //set the initial ontology
    public void setOntology( Ontology o ) {
        if ( o instanceof JENAOntology ) {
            this.model = ((JENAOntology)o).getOntology();
            this.modifiedModel = ((JENAOntology)o).getOntology();
        } else {
            System.err.println("Error : The object given is not an OntModel");
            System.exit(-1);
        }
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    //return the newNamespace
    public String getNamespace() {
        return this.namespace;
    }

    //set the parameters
    // JE: modifier may not have been initialised
    public void setParameters(Properties p) {
        parameters = p;
        modifier.setProperties(parameters);
    }

    //get the parameters
    public Properties getParameters() {
        parameters = modifier.getProperties();                             //get the properties
        return parameters;
    }

    //generate the alingnment
    /**
     * This in fact is dependent on:
     * - p
     * - model
     * - modifiedModel
     * - alignment
     */
    public Alignment generate( Ontology o, Properties p ) {
        this.params = p;
        this.alignment  = new URIAlignment();
	// Initialise the modifier class
        modifier = new OntologyModifier( this.model, this.modifiedModel, this.alignment);   //build the ontology modifier for the first time
        modifier.setNewNamespace( this.namespace );
	// JE: This should be put OUT of the modifier (this generate Properties... instead of alignments...)
        modifier.initializeAlignment();                                         // initialize the reference alignment

	// Apply all modifications
        //System.out.println( "[-------------------------------------------------]" );
        for( String key : this.params.stringPropertyNames() ) {
            String value = this.params.getProperty(key);
	    //System.out.println( "[" +key + "] => [" + value + "]");
	    modifier.modifyOntology( key, value );					//modify the ontology according to it
        }
        //System.out.println( "[-------------------------------------------------]" );

        // JE: this refalign should be variabilised
	//saves the alignment into the file "refalign.rdf", null->System.out
        modifier.computeAlignment( "refalign.rdf" );                  //at the end, compute the reference alignment
        this.alignment = modifier.getAlignment();                               //get the reference alignment
        this.modifiedModel = modifier.getModifiedOntology();                    //get the modified ontology
        return this.alignment;
    }

    //generate the alingnment
    //newParams => keeps track of the previous modifications
    public Alignment generate(Ontology o, Properties p, Properties newParams) {
        this.params = p;
        this.alignment  = new URIAlignment();
        modifier = new OntologyModifier( this.model, this.modifiedModel, this.alignment);   //build the ontology modifier for the first time
	// JE: These two lines (one commented and the next one) are the only difference with the other
	// The first one is a different between incremental (an alignment has been initialised)
        //modifier.initializeAlignment();                                         //initialize the reference alignment

        modifier.setProperties(newParams);
        //set the new namespace
        modifier.setNewNamespace( this.namespace );

        //System.out.println( "[-------------------------------------------------]" );
        for( String key : this.params.stringPropertyNames() ) {
            String value = this.params.getProperty(key);
	    //System.out.println( "[" +key + "] => [" + value + "]");
            modifier.modifyOntology( key, value );					//modify the ontology according to it
        }
        //System.out.println( "[-------------------------------------------------]" );

                                                                                //saves the alignment into the file "referenceAlignment.rdf", null->System.out
        modifier.computeAlignment( "refalign.rdf" );                            //at the end, compute the reference alignment
        this.alignment = modifier.getAlignment();                               //get the reference alignment
        this.modifiedModel = modifier.getModifiedOntology();                    //get the modified ontology
        return this.alignment;
    }


}
