/*
 * $Id$
 *
 * Copyright (C) INRIA Rhône-Alpes, 2003-2004
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



package fr.inrialpes.exmo.align.ling; 

import fr.inrialpes.exmo.align.impl.StringDistances;

import java.io.FileInputStream;
import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.dictionary.Dictionary;
/** 
 * Compute a string distance using the JWNL API (WordNet API)
 * 
 * @author Jerome Pierson
 * @version $Id: JWNLDistances.java,v 1.0 2004/08/04 
 */
class JWNLDistances{
	
/**
 * Initialize the JWNL API. Must be done one time before computing distance
 *  Need to configure the  file_properties.xml located in the current directory (ontoalign)
 *  
  */	
public void Initialize(){
try{JWNL.initialize(new FileInputStream("./file_properties.xml"));}
catch(Exception ex){ex.printStackTrace();
	                	System.exit(-1);}
}	                	
/**
 * Compute a basic distance between 2 strings using WordNet synonym.
 * 
 * @param s1
 * @param s2
 * @return Distance between s1 & s2
 */
public double BasicSynonymDistance(String s1, String s2){
double Dist =0.0;
double Dists1s2;
int i,j,k=0;
int sens=0;
int besti=0,bestj=0;
int syno=0;
double DistTab[];
IndexWord index = null;
Synset Syno[] = null;

Dists1s2= StringDistances.subStringDistance( s1, s2 );

try { index = Dictionary.getInstance().lookupIndexWord(POS.NOUN,s1);}
catch(Exception ex){ex.printStackTrace();
	                	System.exit(-1);}
if (index!=null){
		try {Syno=index.getSenses();}
		 catch (JWNLException e) {e.printStackTrace();}
	 	sens= index.getSenseCount();
		DistTab=new double[sens];
		for (k=0;k<sens;k++){
		 	for(j=0;j<Syno[k].getWordsSize();j++){
				Dist = StringDistances.subStringDistance( Syno[k].getWord(j).getLemma(), s2 );
			    if (Dist < Dists1s2){Dists1s2=Dist;
			    					besti=k;
			    					bestj=j;}
		  	}
		}	   	
 	}

return Dists1s2;
}

}