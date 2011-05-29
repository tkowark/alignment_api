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

/*  This program is based on the ClassHierarchy.java example.
    Iterates through all the classes in the hierarchy and builds the class hierarchy
 */


package fr.inrialpes.exmo.align.gen;

//Jena
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.Restriction;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.vocabulary.OWL;
//Java
import java.util.*;

public class ClassHierarchy {
    private URITree root;							//the root of the tree
    private Map m_anonIDs = new HashMap();
    private int m_anonCount = 0;
    private int maxDepth;							//the max depth of class hierarchy

    public ClassHierarchy() {}

    //return the root of the hierarchy
    public URITree getRootClassHierarchy () {
        return this.root;
    }

    //return the max level of the hierarchy
    public int getMaxLevel() {
        this.maxDepth = this.root.getMaxDepth();
        return this.maxDepth;
    }

    //print class hierarchy
    public void printClassHierarchy() {
        System.out.println( "[--------------------]" );
        System.out.println( "The class hierarchy" );
        root.printURITree(this.root);						//we print the tree
        System.out.println( "The class hierachy" );
        System.out.println( "[---------------------]" );
    }

    public void addClass (String childURI, String parentURI) {
        URITree node = null;
        //node = this.root.searchURITree( this.root, parentURI );               //the node from the hierarchy with the identifier parentURI
        this.getRootClassHierarchy().add(this.root, parentURI, childURI);       //we add the new childURI to the hierarchy
        //node._addChildToNode(this.root, parentURI, childURI);
    }

    //updates the class hierarchy
    public void updateClassHierarchy (Properties params) {
        this.root.renameTree(this.root, params);
    }

    //returns the list of classes from level level
    public List<OntClass> getClassesFromLevel (OntModel model, int level) {
        List <URITree> nodes = this.getNodesFromLevel(level);			//get all the nodes from the specific level
        ArrayList<OntClass> classes = new ArrayList<OntClass>();
        for ( int i=0; i<nodes.size(); i++ )
            classes.add( model.getOntClass( nodes.get(i).getURI() ) );          //builds the list of classes
        return classes;
    }

    //get the nodes from a specific level
    public List<URITree> getNodesFromLevel (int level) {
        return this.getRootClassHierarchy().getNodesFromLevel(this.root, level);
    }

    //remove URI from the class hierarchy
    public void removeUri(URITree root, String uri) {
        root.removeFromURITree(root, uri);					//remove URI uri from the class hierarchy
    }

    //remove the URI of the class from the hierarchy
    //returns the parent of the class if it exists or 0 other way -> not owl:Thing
    public OntClass removeClass ( OntModel model, OntClass cls ) {
        URITree node = this.root.searchURITree( this.root, cls.getURI() );	//search for the class URI in the class hierarchy
        URITree parentNode = null;

        int depth = node.getDepth();						//get the depth of the node
        parentNode = node.getParent();						//get the parent of the node
        for ( URITree child : node.getChildrenList() ) {                        //change the parent of the subclasses of the node
            child.setDepth( depth );						//change the depth of the child
            child.setParent( parentNode );					//set the parent of the child
            parentNode.getChildrenList().add( child );                          //add the child to the parent children
        }

        parentNode.getChildrenList().remove( node );                            //remove the node from children list
        if ( depth == 1 ) {							//the parent is owl:Thing
            OntClass thing = model.createClass( OWL.Thing.getURI() );           //Thing class
            return thing;
        }
        else
            return model.getOntClass( node.getParent().getURI() );              //return the parent class
    }

    //return a random class from the level - level
    public OntClass getRandomClassFromLevel( OntModel model, int level ) {
        Random rdm = new Random();
        List<URITree> childrenNodes = getNodesFromLevel( level );		//get the list of nodes from the level level
        int index = rdm.nextInt( childrenNodes.size() );			//get a random number between 0 and the number_of_classes_from_that_level
        return model.getOntClass( childrenNodes.get(index).getURI() );          //returns the class from the position that we have selected -> the random number
    }


    //modifies the class hierarchy after we have flattened it
    public boolean flattenClassHierarchy ( OntModel model, int level, ArrayList<OntClass> childClasses,
                                        ArrayList<OntClass> parentClasses, ArrayList<OntClass> superLevelClasses) {

        List<URITree> childrenNodes = getNodesFromLevel( level );

        URITree parentNode = null;
        URITree superNode = null;
        boolean active = true;
        for ( URITree childNode : childrenNodes ) {				//for each child class
            parentNode = childNode.getParent();					//get the parent node

            superNode = parentNode.getParent();					//get the parents of the parent node

            childClasses.add( model.getOntClass( childNode.getURI() ) );        //build the list of children classes
            parentClasses.add( model.getOntClass( parentNode.getURI() ) );	//build the list of parent classes

            if ( !superNode.getURI().equals( "Thing" ) ) {			//if the parent of the child class is not owl:Thing (1st level class)
                superLevelClasses.add( model.getOntClass( superNode.getURI() ) );//build the list of the parents of the parent classes
                active = true;							//set the flag -> we don't have a 1st level class
            }
            else {
                active = false;							//set the flag -> we have a 1st level class
            }
        }

        flattenHierarchy( childrenNodes );					//modify the links among the nodes from the class hierarchy
        this.getRootClassHierarchy().changeDepth ( this.getRootClassHierarchy(), level );//change the depth
        return active;
    }

    //modify the links between the nodes from the class hierarchy
    public void flattenHierarchy (List<URITree> childrenNodes ) {
        URITree parentNode;

        for ( URITree childNode : childrenNodes ) {
            parentNode = childNode.getParent();					//get the parent of the node
            childNode.setParent( parentNode.getParent() );			//change the parent of my superclass to the [parent of the "parent node"]

            childNode.setDepth( parentNode.getDepth() );			//change it's depth

            parentNode.getChildrenList().remove( childNode );			//remove it from the children list of parentNode
            parentNode.getParent().getChildrenList().add( childNode );		//add it to the children list of superClass
        }
    }

    //builds the class hierarchy
    @SuppressWarnings("unchecked")
    public void buildClassHierarchy(OntModel model) {
        Iterator i =  model.listHierarchyRootClasses();
        this.root = new URITree( "Thing" );					//the root of the hierarchy is always owl:Thing

        //get the list of root classes
        List<OntClass> ontologyClasses = model.listClasses().toList();
        List<OntClass> rootClasses = new ArrayList<OntClass>();// = model.listClasses().toList();
        for ( OntClass cls : ontologyClasses ) {
            if ( cls.isHierarchyRoot() ) {
                rootClasses.add( cls );
            }
        }

        for ( OntClass rootClass : rootClasses ) {
            if ( !rootClass.isAnon() )                                          //if a root class is not an anonymous class
                getClass( rootClass, new ArrayList(), 0 ) ;
            else {
                for ( Iterator it = rootClass.listSubClasses(); it.hasNext(); ) {
                    getClass ( (OntClass)it.next(), new ArrayList(), 1 );
                }
            }
        }
        this.maxDepth = this.root.getMaxDepth();
    }

    @SuppressWarnings("unchecked")
    public void getClass (OntClass cls, List occurs, int depth) {
        renderClassDescription( cls, depth );

        if ( cls.canAs( OntClass.class )  &&  !occurs.contains( cls ) ) {	// recurse to the next level down
            for (Iterator i = cls.listSubClasses( true );  i.hasNext(); ) {
                OntClass sub = (OntClass) i.next();
                occurs.add( cls );						// we push this expression on the occurs list before we recurse
                getClass( sub, occurs, depth + 1 );
                occurs.remove( cls );
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void renderClassDescription( OntClass c, int depth ) {
        if (c.isRestriction()) {
            renderRestriction( (Restriction) c.as( Restriction.class ) );
        }
        else {
            if ( !c.isAnon() ) {
                String uri;							//the URI of the child
                String parentURI = "";						//the URI of the parent
                int found = 0;
                OntClass aux = null;
                uri = c.getURI();

                for ( Iterator it = c.listSuperClasses(  ); it.hasNext(); ) {     //search to see if the class has a superclass which is not anonymous
                    aux = (OntClass)it.next();
                    if ( !aux.isAnon() ) {					//is not an anonymous class
                        found = 1;						//got the parent
                        parentURI = aux.getURI();

                        //it the superclass is .../#Thing
                        if ( parentURI.contains( "Thing" ) )
                            this.getRootClassHierarchy().add(this.root, uri, "Thing");
                        else
                            this.root.add(this.root, uri, parentURI);
                    }
                }

                if ( found == 0 ) 						//has no parent until now
                    this.getRootClassHierarchy().add(this.root, uri, "Thing");
            }
            else {
                renderAnonymous( c, "class" );					//an anonymous class
            }
        }
    }

    // Render a URI
    protected String renderURI( PrefixMapping prefixes, String uri ) {
        return prefixes.shortForm( uri );
    }

    protected void renderRestriction( Restriction r ) {
        if ( !r.isAnon() ) {
            renderURI( r.getModel(), r.getURI() );
        }
        else {
            renderAnonymous( r, "restriction" );
        }
        renderURI( r.getModel(), r.getOnProperty().getURI() );
    }

    // Render an anonymous class or restriction
    @SuppressWarnings("unchecked")
    protected void renderAnonymous( Resource anon, String name ) {
        String anonID = (String) m_anonIDs.get( anon.getId() );
        if (anonID == null) {
            anonID = "a-" + m_anonCount++;
            m_anonIDs.put( anon.getId(), anonID );
        }
    }

    // Generate the indentation
    protected void indent( int depth ) {
        for (int i = 0;  i < depth; i++) {
            System.out.print( "  " );
        }
    }

}



