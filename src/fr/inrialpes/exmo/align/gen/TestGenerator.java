/*
 * $Id$
 *
 * Copyright (C) 2010-2011, INRIA
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

/* This program modifies the input ontology o according to the set of parameters p
   and returns the alignment between the initial ontology and the modified one
 */

package fr.inrialpes.exmo.align.gen;

import com.hp.hpl.jena.ontology.OntModel;
import fr.inrialpes.exmo.align.gen.inter.AlignedOntologyGenerator;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.align.service.jade.messageontology.Parameter;
import fr.inrialpes.exmo.ontowrap.Ontology;
import fr.inrialpes.exmo.ontowrap.jena25.JENAOntology;
import java.util.Properties;
import org.semanticweb.owl.align.Alignment;



public class TestGenerator implements AlignedOntologyGenerator {
    private Properties parameters;                                              //the set of parameters
    private OntModel model;                                                     //initial ontology
    private OntModel modifiedModel;                                             //modified ontology
    private Alignment alignment;                                                //the reference alignment

    public TestGenerator() {}
    
    //returns the modified ontology
    public Ontology getModifiedOntology() {
        JENAOntology onto = new JENAOntology();                                 //cast the model into Ontology
        onto.setOntology( this.modifiedModel );
        return onto;                                                            //return the ontology
    }

    //set the initial ontology
    public void setOntology( Ontology o ) {
        try {
            this.model = ((JENAOntology)o).getOntology();
            this.modifiedModel = ((JENAOntology)o).getOntology();
        } catch ( Exception ex ) {
            System.err.println( "Exception " + ex.getMessage() );
        }
    }


    public Alignment generate(Ontology o, Properties p) {
        //cast the model into a Jena OntModel
        if ( o instanceof JENAOntology ) {
            this.model = ((JENAOntology)o).getOntology();
            this.modifiedModel = ((JENAOntology)o).getOntology();
        }
        else {
            System.err.println("Error : The object given is not an OntModel");
            System.exit(-1);
        }

        this.parameters = p;
        this.alignment  = new URIAlignment();   
        OntologyModifier modifier = new OntologyModifier( this.model, this.modifiedModel, this.alignment);   //build the ontology modifier for the first time
        modifier.initializeAlignment();                                         //initialize the reference alignment
        int level = modifier.getMaxLevel();                                     //get the max level of the class hierarchy of the ontology

        System.out.println( "[-------------------------------------------------]" );
        for(String key : this.parameters.stringPropertyNames()) {
            String value = this.parameters.getProperty(key);                    //System.out.println( "[" +key + "] => [" + value + "]");
            //iterate through all the parameters
            Parameter param = new Parameter();                                  //build a parameter
            param.setName( key );
            param.setValue( value );
            modifier.modifyOntology( param );					//modify the ontology according to it
        }
        System.out.println( "[-------------------------------------------------]" );

                                                                                //saves the alignment into the file "referenceAlignment.rdf", null->System.out
        modifier.computeAlignment( "referenceAlignment.rdf" );                  //at the end, compute the reference alignment
        this.alignment = modifier.getAlignment();                                    //get the reference alignment
        this.modifiedModel = modifier.getModifiedOntology();                         //get the modified ontology
        return this.alignment;
    }

}
