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

import com.hp.hpl.jena.ontology.OntClass;

import java.util.Properties;
import java.util.List;
import java.util.ArrayList;

import fr.inrialpes.exmo.align.gen.Alterator;
import fr.inrialpes.exmo.align.gen.ParametersIds;

public class SuppressHierarchy extends BasicAlterator {

    public SuppressHierarchy( Alterator om ) {
	initModel( om );
    };

    public Alterator modify( Properties params ) {
	String p = params.getProperty( ParametersIds.NO_HIERARCHY );
	if ( p == null ) return null;
        int level = getMaxLevel();
        while ( getMaxLevel() != 1 ) { // JE: dangerous to not use level
            //this.classHierarchy.printClassHierarchy();
            noHierarchy ( level );
            level--;
        }
	return this; // useless
    };

    // flatten level
    public void noHierarchy ( int level ) {
        if ( level == 1 ) return;
        int size;
        boolean active = false;
        ArrayList<OntClass> levelClasses = new ArrayList<OntClass>();		//the list of classes from that level
        ArrayList<OntClass> parentLevelClasses = new ArrayList<OntClass>();	//the list of parent of the child classes from that level
        ArrayList<OntClass> superLevelClasses = new ArrayList<OntClass>();	//the list of parent of the parent classes from that level
        buildClassHierarchy();                                                  //check if the class hierarchy is built
        active = this.classHierarchy.flattenClassHierarchy( modifiedModel, level, levelClasses, parentLevelClasses, superLevelClasses);
        size = levelClasses.size();

        for ( int i=0; i<size; i++ ) {
            OntClass childClass = levelClasses.get( i );			//child class
            OntClass parentClass = parentLevelClasses.get( i );                 //parent class
            //all the classes are subclasses of owl: Thing
            if (  active ) {                                                    //if ( !parentClass.getURI().equals( "Thing" ) ) {
               OntClass superClass = superLevelClasses.get( i );                //parent class of the child class parents
               childClass.addSuperClass( superClass );
               parentClass.removeSubClass( childClass );
            } else {
                parentClass.removeSubClass( childClass );
            }
        }
    }

    //must have the max level of the class hierarchy
    public int getMaxLevel() {
        buildClassHierarchy();                                                  //check if the class hierarchy is built
        return classHierarchy.getMaxLevel();
    }

}
