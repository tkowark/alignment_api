/*
 * $Id$
 *
 * Copyright (C) 2011-2012, INRIA
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

/* This program represents the class hierarchy.
   It retains only the URI of the classes .
*/

package fr.inrialpes.exmo.align.gen;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import fr.inrialpes.exmo.align.gen.alt.BasicAlterator;

public class URITree {
    private String URI;                 //the URI of the node
    private ArrayList<URITree> children;//the list of children
    private URITree parent;		//the parent of the node
    int depth;				//the depth of the node
    int maxDepth;			//the max depth of the node
	
    public URITree( String URI ) {
        this.URI = URI;
        children = new ArrayList<URITree>();
        parent = null;
	depth = 0;
	maxDepth = 0;
    }

    //get the URI of the node
    public String getURI () {
        return URI;
    }
	
    //set the URI of the node
    public void setURI( String URI ) {
        this.URI = URI;
    }
	
    //set the depth of the node
    public void setDepth( int depth ) {
        this.depth = depth;
    }
	
    //get the depth of the node
    public int getDepth() {
        return depth;
    }
	
    //return the max depth
    public int getMaxDepth() {
        return maxDepth;
    }
	
    //set the parent of the node
    public void setParent( URITree parent ) {
        this.parent = parent;
    }
	
    //get the parent of the node
    public URITree getParent () {
        return parent;
   }
	
    //returns a child from a specific position
    public URITree getChildAt ( int index ) {
        return children.get( index );
    }
	
    //return the list of children nodes
    public ArrayList<URITree> getChildrenList() {
        return children;
    }
	
    //returns the size of the children
    public int getChildrenSize() {
        return children.size();
    }

    //add the node with the childURI to the parent with the URI parentURI
    public void add( URITree root, String childURI, String parentURI ) {
        //adds the node to the class hierarchy -> a class might have more than one 
        //superclass we have to add it to each superclass in part, not only to one
        _addChildToNode( root, parentURI, childURI );
    }

    //add a child
    public void addChild( URITree root, URITree node, String URI ) {
	// If already child, forget it
	for ( URITree n : node.getChildrenList() ) {
            if ( n.getURI().equals( URI ) ) return;
        }
	// If already existing, suppress it
	URITree toRemove = null;
	for ( URITree n : root.getChildrenList() ) {
	    if ( n.getURI().equals( URI ) ) {
		toRemove = n;
		break;
	    }
        }
	root.getChildrenList().remove( toRemove );
	// Now, go and create it
        addChildToNode( node, URI );
    }
	
    //add child to a specific node
    public void addChildToNode( URITree node, String URI ) {
        URITree child = new URITree( URI );                                     //creates a new node
        child.setDepth( node.getDepth()+1 );                                  //set the depth of the node
        if ( maxDepth < node.getDepth()+1 )                               //keeps track of the max depth of the hierarchy
            maxDepth = node.getDepth()+1;
        child.setParent( node );                                                //sets the parent of the node
        node.getChildrenList().add( child );                                    //adds the node to the parent children list
    }

    //renames the class from the tree after we have renamed the classes
    public void renameTree( Properties alignment ) {
	rename( alignment, (String)alignment.get( "##" ) );
    }

    @SuppressWarnings("unchecked")
    public void rename( Properties alignment, String ns ) {
	String key = BasicAlterator.getLocalName( getURI() );
	String val = (String)alignment.get( key );
	if ( val != null && !val.equals( key ) ) setURI( ns+val );
	for ( URITree child : getChildrenList() ) {
	    child.rename( alignment, ns );
	}
    }

    //returns the URITree with the given URI
    @SuppressWarnings("unchecked")
    public void _addChildToNode(URITree root, String parentURI, String childURI) {
        if ( root.getURI().equals( parentURI ) ) {				//if the root has the URI as the URI searched
            //addChildToNode(root, URI);
            addChild ( root, root, childURI );                                    //then add the child
            return;
        } else {
	    for( URITree node : root.getChildrenList() ) {                              //we start to search recursively
		if ( node.getURI().equals( parentURI ) ) {
		    //addChildToNode(root.getChildAt(index), URI);                  //we found the node with the given URI, then we add the child
		    addChild( root, node, childURI );
		}
		_addChild( root, node, 0, parentURI, childURI );
	    }
	}
    }

    @SuppressWarnings("unchecked")
    public void  _addChild(URITree root, URITree node, int depth, String parentURI, String childURI) {
        int index = 0;
        URITree ans = null;
        //verify if the label of the URITree is the one with the label of the URITree we search for
        if ( node.getURI().equals( parentURI ) ) {
            //addChildToNode(node, URI);					//has found the parent
            addChild (root, node, childURI);
        }

        while( index < node.getChildrenSize()) {
            URITree n = node.getChildAt( index );
            _addChild( root, n, depth+1, parentURI, childURI );
            index++;
        }
    }

    //returns the URITree with the given URI
    @SuppressWarnings("unchecked")
    public URITree searchURITree( String URI ) {
        if ( getURI().equals( URI ) ) return this;                         //if the root has the URI as the URI searched
	for ( URITree node : getChildrenList() ) {                  //we start to search recursively
            URITree ans = node.searchURITree( URI );
            if ( ans != null ) return ans;
        }
        return null;
    }

    // JE: commented because never used
    /*
    //remove a child from the tree
    @SuppressWarnings("unchecked")
    public void removeFromURITree( URITree root, String URI ) {
        int index = 0;
        int found = 0;

        while ( index < root.getChildrenSize() ) {
            if ( root.getChildAt( index ).getURI().equals( URI ) ) {
                root.getChildrenList().remove( index );                         //found the node to delete
                //return;
            }
			
            remove(root.getChildAt( index ), 0, URI);
            index++;
        }
    }
	
    @SuppressWarnings("unchecked")
    public void remove( URITree node, int depth, String URI) {
        int index = 0;
        int found = 0;

        if ( node.getURI().equals( URI ) ) {
            URITree parent = node.getParent();
            //add the node children to the parent of the node
            int cnt = 0;                                                        //reestablish the connection between nodes
            while ( cnt < node.getChildrenSize() ) {
                URITree child = node.getChildrenList().get( cnt );
                child.setDepth( node.getDepth() );                              //modify the depth
                child.setParent( parent );                                      //modify the parent
                parent.getChildrenList().add( child );                          //add the child to the parent of node
                cnt++;
            }	
            parent.getChildrenList().remove( node );                            //remove the node from the children list
        }
		
        while( index < node.getChildrenSize()) {
            URITree n = node.getChildAt( index );
            remove( n, depth+1, URI );
            index++;
        }

    }
    */
    //get all the node from a specific level
    @SuppressWarnings("unchecked")
    public List<URITree> getNodesFromLevel (URITree root, int level) {
        List<URITree> nodes = new ArrayList<URITree>();                         //to store the nodes from a specific level
        int index = 0;
        if ( root.getDepth() == level )
            nodes.add( root );
        while ( index < root.getChildrenList().size() ) {
            getNodes ( root.getChildAt(index), 0, nodes, level );//recursively print all the children URITrees
            index++;
        }
        return nodes;                                                           //return the list of nodes
    }

    @SuppressWarnings("unchecked")
    public void getNodes (URITree node, int depth, List<URITree> nodes, int level) {
        int index = 0;
        if ( node.getDepth() == level )                                         //if it's on the level that we want, we add it to the hierarchy
            nodes.add( node );

        while( index < node.getChildrenList().size() ) {
            URITree n = node.getChildrenList().get(index);
            getNodes( n, depth+1, nodes, level );
            index++;
        }
    }
	
    //change the depth if the nodes lower the level to node.getDepth()-1
    @SuppressWarnings("unchecked")
    public void changeDepth( URITree root, int level ) {
        int index = 0;
        maxDepth--;
        while ( index < root.getChildrenList().size() ) {
            change ( root.getChildAt(index), 0, level );
            index++;
        }
    }

    @SuppressWarnings("unchecked")
    public void change (URITree node, int depth, int level) {
        int index = 0;
		
        if ( node.getDepth() > level ) 	{                                       //if it's on the level that we want, we add it to the hierarchy
            int dept = node.getDepth();
            node.setDepth( dept-1 );
        }
		
        while( index < node.getChildrenList().size() ) {
            URITree n = node.getChildrenList().get(index);
            change( n, depth+1, level );
            index++;
        }
    }
	
    //print the tree
    @SuppressWarnings("unchecked")
    public void printURITree( URITree root ) {
        int index = 0;
        //System.err.println( "[" + root.getURI() + "]" + "->" + root.getDepth() );
		
        while ( index < root.getChildrenList().size() ) {
            //recursively print all the children URITrees
            print(root.getChildAt(index), 0);
            index++;
        }
    }

    @SuppressWarnings("unchecked")
    public void print (URITree node, int depth)  {
        int index = 0;
        indent( node.getDepth() );
        //System.err.println( "[" + node.getURI() + "]" + "->" + node.getDepth() );
		
        while( index < node.getChildrenList().size() ) {
            URITree n = node.getChildrenList().get( index );
            print( n, depth+1 );
            index++;
        }
    }

    protected void indent( int depth ) {
        for (int i = 0;  i < depth; i++) {
            System.out.print( "  " );
        }
    }
		
}


