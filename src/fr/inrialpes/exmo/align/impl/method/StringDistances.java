/*
 * $Id$
 *
 * Copyright (C) INRIA Rhône-Alpes, 2003-2005
 * Except for the Levenshtein class whose copyright is not claimed to
 * our knowledge.
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

/** 
 * This class implements various string distances that can be used on
 * various kind of strings.
 *
 * This includes:
 * - subStringDistance
 * - equality
 * - lowenhein (edit) distance
 * - n-gram distance
 *
 * @author Jérôme Euzenat
 * @version $Id$ 
 */

package fr.inrialpes.exmo.align.impl.method; 


public class StringDistances {

  //*****************************
  // Compute substring distance
  // = 1 - (2 | length of longest common substring | / |s1|+|s2|)
  //*****************************

    public static double subStringDistance (String s1, String s2) {
	if (s1 == null || s2 == null) {
	    throw new IllegalArgumentException("Strings must not be null");
	}
	
	int l1 = s1.length(); // length of s
	int l2 = s2.length(); // length of t
		
	if ((l1 == 0) && ( l2 == 0 )) return 0;
	if ((l1 == 0) || ( l2 == 0 )) return 1;

	int max = Math.min( l1, l2 ); // the maximal length of a subs
	int best = 0; // the best subs length so far
	    
	int i = 0; // iterates through s1
	int j = 0; // iterates through s2

	for( i=0; (i < l1) && (l1-i > best); i++ ){
	    j = 0;
	    while( l2-j > best ){
		int k = i;
		for( ; (j < l2 )
			 && (s1.charAt(k) != s2.charAt(j) ); j++) {};
		if ( j != l2 ) {// we have found a starting point
		    for( j++, k++; (j < l2) && (k < l1) && (s1.charAt(k) == s2.charAt(j)); j++, k++);
		    best = Math.max( best, k-i );
		}
	    }
	}
	//System.err.println(s1+" x "+s2+" = "+(1.0 - ((double)2*best / (l1+l2))));
	return (1.0 - ((double)2*best / (l1+l2)));
    }


    public static int equalDistance (String s, String t) {
	if (s == null || t == null) {
	    throw new IllegalArgumentException("Strings must not be null");
	}
	if ( s.equals(t) ) { return 1;} else {return 0;}
    }

    // JE: 30/05/2005: this has not been tested
    public static int ngramDistance(String s, String t) {
	int n = 3; // tri-grams for the moment
	if (s == null || t == null) {
	    throw new IllegalArgumentException("Strings must not be null");
	}
	int found = 0;
	for( int i=0; i < s.length()-n ; i++ ){
	    for( int j=0; j < t.length()-n; j++){
		int k = 0;
		for( ; (k<n) && s.charAt(i+k)==t.charAt(j+k); k++);
		if ( k == n ) found++;
	    }
	}
	return found;
    }

    /* Pointer was provided in Todd Hugues (Lockheed)
       Taken from http://www.merriampark.com/ldjava.htm
       Initial algorithm by Michael Gilleland
       Integrated in Apache Jakarta Commons
       Improved by Chas Emerick
       This algorithm should be taken appart of this file and reset in the
       context of a proper package name with an acceptable license terms.
       Hopefully, Jakarta Commons will provide this.
    */

    public static int levenshteinDistance (String s, String t) {
	if (s == null || t == null) {
	    throw new IllegalArgumentException("Strings must not be null");
	}
		
	/* The difference between this impl. and the previous is that, rather 
	  than creating and retaining a matrix of size s.length()+1 by 
	  t.length()+1,
	  we maintain two single-dimensional arrays of length s.length()+1.
	  The first, d, is the 'current working' distance array that maintains
	  the newest distance cost counts as we iterate through the characters
	  of String s.  Each time we increment the index of String t we are 
	  comparing, d is copied to p, the second int[]. Doing so allows us
	  to retain the previous cost counts as required by the algorithm
	  (taking the minimum of the cost count to the left, up one, and
	  diagonally up and to the left of the current cost count being
	  calculated).
	  (Note that the arrays aren't really copied anymore, just switched...
	  this is clearly much better than cloning an array or doing a
	  System.arraycopy() each time  through the outer loop.)
	  
	  Effectively, the difference between the two implementations is this
	  one does not cause an out of memory condition when calculating the LD
	  over two very large strings.  		
	*/		
		
	int n = s.length(); // length of s
	int m = t.length(); // length of t
		
	if (n == 0) return m;
	else if (m == 0) return n;

	int p[] = new int[n+1]; //'previous' cost array, horizontally
	int d[] = new int[n+1]; // cost array, horizontally
	int _d[]; //placeholder to assist in swapping p and d

	// indexes into strings s and t
	int i; // iterates through s
	int j; // iterates through t

	char t_j; // jth character of t

	int cost; // cost

	for (i = 0; i<=n; i++) p[i] = i;
	
	for (j = 1; j<=m; j++) {
	    t_j = t.charAt(j-1);
	    d[0] = j;
	    
	    for (i=1; i<=n; i++) {
		cost = s.charAt(i-1)==t_j ? 0 : 1;
		// minimum of cell to the left+1, to the top+1,
		// diagonally left and up +cost				
		d[i] = Math.min(Math.min(d[i-1]+1, p[i]+1),  p[i-1]+cost);  
	    }
	    
	    // copy current distance counts to 'previous row' distance counts
	    _d = p;
	    p = d;
	    d = _d;
	} 

	// our last action in the above loop was to switch d and p, so p now 
	// actually has the most recent cost counts
	//System.err.println(s+" x "+t+" = "+p[n]);
	return p[n];
    }
}
