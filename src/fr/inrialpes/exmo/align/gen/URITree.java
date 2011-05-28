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

/* This program represents the class hierarchy.
   It retains only the URI of the classes .
*/

package fr.inrialpes.exmo.align.gen;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class URITree {
    private String URI;                 //the URI of the node
    private ArrayList<URITree> children;//the list of children
    private URITree parent;		//the parent of the node
    int depth;				//the depth of the node
    int maxDepth;			//the max depth of the node
	
    public URITree(String URI) {
        this.URI = URI;
        this.children = new ArrayList<URITree>();
        this.parent = null;
	this.depth = 0;
	this.maxDepth = 0;
    }

    //get the URI of the node
    public String getURI () {
        return this.URI;
    }
	
    //set the URI of the node
    public void setURI(String URI) {
        this.URI = URI;
    }
	
    //set the depth of the node
    public void setDepth (int depth) {
        this.depth = depth;
    }
	
    //get the depth of the node
    public int getDepth() {
        return this.depth;
    }
	
    //return the max depth
    public int getMaxDepth() {
        return this.maxDepth;
    }
	
    //set the parent of the node
    public void setParent ( URITree parent ) {
        this.parent = parent;
    }
	
    //get the parent of the node
    public URITree getParent () {
        return this.parent;
   }
	
    //returns a child from a specific position
    public URITree getChildAt ( int index ) {
        return this.children.get( index );
    }
	
    //return the list of children nodes
    public ArrayList<URITree> getChildrenList() {
        return this.children;
    }
	
    //returns the size of the children
    public int getChildrenSize() {
        return this.children.size();
    }

    //add the node with the childURI to the parent with the URI parentURI
    public void add(URITree root, String childURI, String parentURI) {
        URITree parent;
	parent = searchURITree(root, parentURI);                        //we search for the parent URITree
	addChild(root, parent, childURI);				//we add the new URITree
    }

    //add a child
    public void addChild (URITree root, URITree node, String URI) {
        int index = 0;
        URITree n = null;
		
	while ( index < node.getChildrenSize() ) {                      //if the child is already in the list
            if ( node.getChildAt( index ).getURI().equals( URI ) )
                return;
            index++;
        }

        index = 0;
	while ( index < root.getChildrenSize() ) {                      //we search among root children to see if the node is already there
            n = root.getChildrenList().get( index );
            if ( n.getURI().equals( URI ) )	{			//the node is already there
                root.getChildrenList().remove( n );                     //we remove the node
                break;
            }
            index++;
        }
        addChildToNode( node, URI );
    }
	
    //add child to a specific node
    public void addChildToNode(URITree node, String URI) {
        URITree child = new URITree( URI );                             //creates a new node
        child.setDepth( node.getDepth() + 1 );                          //set the depth of the node
		
        if ( this.maxDepth < node.getDepth()+1  )                       //keep track of the max depth of the hierarchy
            this.maxDepth = node.getDepth() + 1;
       
        child.setParent( node );                                        //set the parent of the node
        node.getChildrenList().add( child );                            //add the node to the parent children list
    }

    //renames the class from the tree after we have renamed the classes
    @SuppressWarnings("unchecked")
    public void renameTree(URITree root, Properties params) {
        int index = 0;
        URITree node = root;
        URITree ans = null;
        String uri;

        //the uri has change
        while ( index < root.getChildrenSize() ) {                      //we start to search recursively
            uri = root.getChildAt( index ).getURI();
            // System.out.println( "uri = " + uri );
            if ( params.containsKey( uri ) ) {
                if ( !params.get( uri ).equals( uri ) )
                    root.getChildAt( index ).setURI( (String)params.get( uri ) );
            }
            rename(root.getChildAt( index ), new ArrayList(),  0, params);
            index++;
        }
    }

    @SuppressWarnings("unchecked")
    public void rename(URITree node, List occurs, int depth, Properties params) {
        int index = 0;
        URITree ans = null;
        //verify if the label of the URITree is the one with the label of the URITree we search for
        String uri = node.getURI();
        if ( params.containsKey( uri ) ) {

            if ( !params.get( uri ).equals( uri ) ) {
                node.setURI( (String)params.get( uri ) );
            }
        }

        while( index < node.getChildrenSize()) {
            URITree n = node.getChildAt( index );
            occurs.add( node );
            rename( n, occurs, depth+1, params );
            occurs.remove( node );
            index++;
        }
    }

    //returns the URITree with the given URI
    @SuppressWarnings("unchecked")
    public URITree searchURITree(URITree root, String URI) {
        int index = 0;
        URITree node = root;
        URITree ans = null;
		
        if ( root.getURI().equals( URI ) )				//if the root has the URI as the URI searched
            return root;
		
        while ( index < root.getChildrenSize() ) {                      //we start to search recursively
            if ( root.getChildAt( index ).getURI().equals( URI ) )
                return root.getChildAt( index );                        //we found the node with the given URI
            ans = search(root.getChildAt( index ), new ArrayList(),  0, URI);
            if ( ans != null )
                return ans;
            index++;
        }
        return node;
    }

    @SuppressWarnings("unchecked")
    public URITree search(URITree node, List occurs, int depth, String URI) {
        int index = 0;
        URITree ans = null;
        //verify if the label of the URITree is the one with the label of the URITree we search for
        if ( node.getURI().equals( URI ) ) {
            ans = node;
            return ans;							//has found the parent
        }

        while( index < node.getChildrenSize()) {
            URITree n = node.getChildAt( index );
            occurs.add( node );
            ans = search( n, occurs, depth+1, URI );
            if ( ans != null )
                return ans;
            occurs.remove( node );
            index++;
        }
        return null;							//has not found the parent
    }

    //remove a child from the tree
    @SuppressWarnings("unchecked")
    public void removeFromURITree(URITree root, String URI) {
        int index = 0;
        int found = 0;

        while ( index < root.getChildrenSize() ) {
            if ( root.getChildAt( index ).getURI().equals( URI ) ) {
                root.getChildrenList().remove( index );			//found the node to delete
                return;
            }
			
            found = remove(root.getChildAt( index ), new ArrayList(),  0, URI);
            if ( found == 1 )
                return;
            index++;
        }
    }
	
    @SuppressWarnings("unchecked")
    public int remove(URITree node, List occurs, int depth, String URI) {
        int index = 0;
        int found = 0;

        if ( node.getURI().equals( URI ) ) {
            URITree parent = node.getParent();
            //add the node children to the parent of the node
            int cnt = 0; 						//reestablish the connection between nodes
            while ( cnt < node.getChildrenSize() ) {
                URITree child = node.getChildrenList().get( cnt );
                child.setDepth( node.getDepth() );                      //modify the depth
                child.setParent( parent );				//modify the parent
                parent.getChildrenList().add( child );                  //add the child to the parent of node
                cnt++;
            }
			
            parent.getChildrenList().remove( node );                    //remove the node from the children list
            found = 1;
            return found;						//has found the parent
        }
		
        while( index < node.getChildrenSize()) {
            URITree n = node.getChildAt( index );
            occurs.add( node );
            found = remove( n, occurs, depth+1, URI );
            if ( found == 1 )
                return found;
            occurs.remove( node );
            index++;
        }

        return found;
    }

    //get all the node from a specific level
    @SuppressWarnings("unchecked")
    public List<URITree> getNodesFromLevel (URITree root, int level) {
        List<URITree> nodes = new ArrayList<URITree>();                 //to store the nodes from a specific level
        int index = 0;
        if ( root.getDepth() == level )
            nodes.add( root );
        while ( index < root.getChildrenList().size() ) {
            getNodes ( root.getChildAt(index), new ArrayList(), 0, nodes, level );//recursively print all the children URITrees
            index++;
        }
        return nodes;							//return the list of nodes
    }

    @SuppressWarnings("unchecked")
    public void getNodes (URITree node, List occurs, int depth, List<URITree> nodes, int level) {
        int index = 0;
        if ( node.getDepth() == level )                                 //if it's on the level that we want, we add it to the hierarchy
            nodes.add( node );

        while( index < node.getChildrenList().size() ) {
            URITree n = node.getChildrenList().get(index);
            occurs.add( node );
            getNodes( n, occurs, depth+1, nodes, level );
            occurs.remove( node );
            index++;
        }
    }
	
    //change the depth if the nodes lower the level to node.getDepth()-1
    @SuppressWarnings("unchecked")
    public void changeDepth (URITree root, int level) {
        int index = 0;
        this.maxDepth = this.maxDepth-1;
        while ( index < root.getChildrenList().size() ) {
            change ( root.getChildAt(index), new ArrayList(), 0, level );
            index++;
        }
    }

    @SuppressWarnings("unchecked")
    public void change (URITree node, List occurs, int depth, int level) {
        int index = 0;
		
        if ( node.getDepth() > level ) 	{                               //if it's on the level that we want, we add it to the hierarchy
            int dept = node.getDepth();
            node.setDepth( dept-1 );
        }
		
        while( index < node.getChildrenList().size() ) {
            URITree n = node.getChildrenList().get(index);
            occurs.add( node );
            change( n, occurs, depth+1, level );
            occurs.remove( node );
            index++;
        }
    }
	
    //print the tree
    @SuppressWarnings("unchecked")
    public void printURITree( URITree root ) {
        int index = 0;
        //System.out.println( "[" + root.getURI() + "]" + "->" + root.getDepth() );
		
        while ( index < root.getChildrenList().size() ) {
            //recursively print all the children URITrees
            print(root.getChildAt(index), new ArrayList(),  0);
            index++;
        }
    }

    @SuppressWarnings("unchecked")
    public void print (URITree node, List occurs, int depth)  {
        int index = 0;
        indent( node.getDepth() );
        System.out.println( "[" + node.getURI() + "]" + "->" + node.getDepth() );
		
        while( index < node.getChildrenList().size() ) {
            URITree n = node.getChildrenList().get( index );
            occurs.add( node );
            print( n, occurs, depth+1 );
            occurs.remove( node );
            index++;
        }
    }

    protected void indent( int depth ) {
        for (int i = 0;  i < depth; i++) {
            System.out.print( "  " );
        }
    }
		
}


