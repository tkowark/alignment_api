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
import net.didion.jwnl.data.PointerUtils;
import net.didion.jwnl.data.list.PointerTargetNodeList;
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
/**
 * Compute a distance between 2 strings using WordNet synonym
 * Get all the synonym for s1 , syno1
 * Get all the synonym for s2, syno2
 * compute intersection syno1(inter)syno2, I
 * compute union syno1(union)syno2, U
 * The distance = card I /card U
 * 
 * @param s1
 * @param s2
 * @return Distance between s1 & s2
 */

public double SynonymDistance (String s1,String s2){
	double result=0.000000;
	int cardI=0,cardU=0;
	int i=0,j=0,k=0;
	String Syno1[];
	String Syno2[];
	String I[]=null;
	String U[]=null;
	int nbmot1;
	int nbmot2;
	
	/** Build the list of synonym for s1 & s2 **/
	nbmot1=CountMaxSensSyno(s1);
	nbmot2=CountMaxSensSyno(s2);
	Syno1=new String[nbmot1];
	Syno2=new String[nbmot2];
    Syno1=GetAllSyno(s1);
    Syno2=GetAllSyno(s2);
        		/** Compute intersection between the list of synonym : I**/
I=Inter (Syno1,Syno2);
    	/** Compute union between the list of synonym  U **/
U=Union (Syno1,Syno2);	
	/** compute card I / card U **/
for (i=0;i<I.length;i++){if (I[i]!=null) {cardI++;}}
for (i=0;i<U.length;i++){if (U[i]!=null) {cardU++;}}
	if(cardU!=0){result=(double)cardI /(double) cardU;}
	return (result);
}
/**
 *Compute a distance between 2 strings using WordNet synonym
 * Get all the ...  for s1 , syno1
 * Get all the ...  for s2, syno2
 * compute intersection syno1(inter)syno2, I
 * compute union syno1(union)syno2, U
 * The distance = card I /card U
  
 * @param s1
 * @param s2
 * @return The Distance between s1 & s2
 */

public double CompleteDistance (String s1,String s2){
	double result=0.000000;
	int cardI=0,cardU=0;
	int i=0,j=0,k=0;
	String Syno1[];
	String Syno2[];
	String I[]=null;
	String U[]=null;
	int nbmot1;
	int nbmot2;
	
	/** Build the list of synonym for s1 & s2 **/
	nbmot1=CountMaxSens(s1);
	nbmot2=CountMaxSens(s2);
	Syno1=new String[nbmot1];
	Syno2=new String[nbmot2];
    Syno1=GetAll(s1);
    Syno2=GetAll(s2);
        		/** Compute intersection between the list of synonym : I**/
I=Inter (Syno1,Syno2);
    	/** Compute union between the list of synonym  U **/
U=Union (Syno1,Syno2);	
	/** compute card I / card U **/
for (i=0;i<I.length;i++){if (I[i]!=null) {cardI++;}}
for (i=0;i<U.length;i++){if (U[i]!=null) {cardU++;}}
	if(cardU!=0){result=(double)cardI /(double) cardU;}
	return (result);
}
/**
 * 
 * @param S
 * @return Number of sens for the word s
 */
private int CountMaxSens(String S){
	IndexWord index=null;
	Synset S1[]=null;
	int i=0;
	int sens1=0;
	int nbmot1=0;
	//PointerUtils utilitaire;
	 PointerTargetNodeList pointeur = null;
	
	try { index = Dictionary.getInstance().lookupIndexWord(POS.NOUN,S);}
	catch(Exception ex){ex.printStackTrace();
			                System.exit(-1);}
	if (index!=null){
						try {S1=index.getSenses();
						      sens1= index.getSenseCount();}
					 catch (JWNLException e) {e.printStackTrace();}
					  
	}
	for(i=0;i<sens1;i++){nbmot1=nbmot1+S1[i].getWordsSize();
	try{
		
	pointeur= PointerUtils.getInstance().getDirectHypernyms(S1[i]);
	System.out.println(pointeur.toString());}
	catch (JWNLException e){}
	
	}
	
	try { index = Dictionary.getInstance().lookupIndexWord(POS.VERB,S);}
	catch(Exception ex){ex.printStackTrace();
			                System.exit(-1);}
	if (index!=null){
						try {S1=index.getSenses();
						      sens1= index.getSenseCount();}
					 catch (JWNLException e) {e.printStackTrace();}
	}
	for(i=0;i<sens1;i++){nbmot1=nbmot1+S1[i].getWordsSize();}
	
	try { index = Dictionary.getInstance().lookupIndexWord(POS.ADJECTIVE,S);}
	catch(Exception ex){ex.printStackTrace();
			                System.exit(-1);}
	if (index!=null){
						try {S1=index.getSenses();
						      sens1= index.getSenseCount();}
					 catch (JWNLException e) {e.printStackTrace();}
	}
	for(i=0;i<sens1;i++){nbmot1=nbmot1+S1[i].getWordsSize();}
	
	try { index = Dictionary.getInstance().lookupIndexWord(POS.ADVERB,S);}
	catch(Exception ex){ex.printStackTrace();
			                System.exit(-1);}
	if (index!=null){
						try {S1=index.getSenses();
						      sens1= index.getSenseCount();}
					 catch (JWNLException e) {e.printStackTrace();}
	}
	for(i=0;i<sens1;i++){nbmot1=nbmot1+S1[i].getWordsSize();}
	
	return (nbmot1);

}
/**
 * 
 * @param S
 * @return Number of sens for the word s using synonym
 */
private int CountMaxSensSyno (String S){
	IndexWord index=null;
	Synset S1[]=null;
	int i=0;
	int sens1=0;
	int nbmot1=0;
	
	try { index = Dictionary.getInstance().lookupIndexWord(POS.NOUN,S);}
	catch(Exception ex){ex.printStackTrace();
			                System.exit(-1);}
	if (index!=null){
						try {S1=index.getSenses();
						      sens1= index.getSenseCount();}
					 catch (JWNLException e) {e.printStackTrace();}
	}
	for(i=0;i<sens1;i++){nbmot1=nbmot1+S1[i].getWordsSize();}
	
	try { index = Dictionary.getInstance().lookupIndexWord(POS.VERB,S);}
	catch(Exception ex){ex.printStackTrace();
			                System.exit(-1);}
	if (index!=null){
						try {S1=index.getSenses();
						      sens1= index.getSenseCount();}
					 catch (JWNLException e) {e.printStackTrace();}
	}
	for(i=0;i<sens1;i++){nbmot1=nbmot1+S1[i].getWordsSize();}
	
	try { index = Dictionary.getInstance().lookupIndexWord(POS.ADJECTIVE,S);}
	catch(Exception ex){ex.printStackTrace();
			                System.exit(-1);}
	if (index!=null){
						try {S1=index.getSenses();
						      sens1= index.getSenseCount();}
					 catch (JWNLException e) {e.printStackTrace();}
	}
	for(i=0;i<sens1;i++){nbmot1=nbmot1+S1[i].getWordsSize();}
	
	try { index = Dictionary.getInstance().lookupIndexWord(POS.ADVERB,S);}
	catch(Exception ex){ex.printStackTrace();
			                System.exit(-1);}
	if (index!=null){
						try {S1=index.getSenses();
						      sens1= index.getSenseCount();}
					 catch (JWNLException e) {e.printStackTrace();}
	}
	for(i=0;i<sens1;i++){nbmot1=nbmot1+S1[i].getWordsSize();}
	
	return (nbmot1);

}
/**
 * 
 * @param S
 * @return The tab with all the synonym of the word s
 */
private String[] GetAllSyno(String S){
	String Syno[]=null;
	IndexWord index=null;
	Synset[] Synso=null;
	int sens=0;
	int nbmot=0;
	int i=0,j=0,k=0,l=0;
	boolean trouve=false;
	int strcomp=0;
	
	nbmot=CountMaxSens(S);
	Syno=new String[nbmot];

	try { index = Dictionary.getInstance().lookupIndexWord(POS.NOUN,S);}
	catch(Exception ex){ex.printStackTrace();
			                System.exit(-1);}
	if (index!=null){
						try {Synso=index.getSenses();
						sens= index.getSenseCount();}
					 catch (JWNLException e) {e.printStackTrace();}
	}
	
	for (i=0;i<sens;i++){
		for(j=0;j<Synso[i].getWordsSize();j++){
			for(l=0;l<k;l++){
				if (Synso[i].getWord(j).getLemma().compareTo(Syno[l])==0){trouve=true;}
				}
			if (trouve!=true){Syno[k++] =  Synso[i].getWord(j).getLemma();}
			trouve=false;
		}
	}
	try { index = Dictionary.getInstance().lookupIndexWord(POS.VERB,S);}
	catch(Exception ex){ex.printStackTrace();
			                System.exit(-1);}
	if (index!=null){
						try {Synso=index.getSenses();
						sens= index.getSenseCount();}
					 catch (JWNLException e) {e.printStackTrace();}
	}
	
	for (i=0;i<sens;i++){
		for(j=0;j<Synso[i].getWordsSize();j++){
			for(l=0;l<k;l++){
				if (Synso[i].getWord(j).getLemma().compareTo(Syno[l])==0){trouve=true;}
				}
			if (trouve!=true){
			Syno[k++] = Synso[i].getWord(j).getLemma();}
			trouve=false;
		}
	}
	
	try { index = Dictionary.getInstance().lookupIndexWord(POS.ADJECTIVE,S);}
	catch(Exception ex){ex.printStackTrace();
			                System.exit(-1);}
	if (index!=null){
						try {Synso=index.getSenses();
						sens= index.getSenseCount();}
					 catch (JWNLException e) {e.printStackTrace();}
	}
	
	for (i=0;i<sens;i++){
		for(j=0;j<Synso[i].getWordsSize();j++){
			for(l=0;l<k;l++){
				if (Synso[i].getWord(j).getLemma().compareTo(Syno[l])==0){trouve=true;}
				}
			if (trouve!=true){
			Syno[k++] =	Synso[i].getWord(j).getLemma();}
			trouve=false;
		}
	}
	
	try { index = Dictionary.getInstance().lookupIndexWord(POS.ADVERB,S);}
	catch(Exception ex){ex.printStackTrace();
			                System.exit(-1);}
	if (index!=null){
						try {Synso=index.getSenses();
						sens= index.getSenseCount();}
					 catch (JWNLException e) {e.printStackTrace();}
	}
	for (i=0;i<sens;i++){
		for(j=0;j<Synso[i].getWordsSize();j++){
			for(l=0;l<k;l++){
				if (Synso[i].getWord(j).getLemma().compareTo(Syno[l])==0){trouve=true;}
				}
			if (trouve!=true){
			Syno[k++] =	 Synso[i].getWord(j).getLemma();}
			trouve=false;
		}
	}
	return(Syno);
}

/**
 * 
 * @param S
 * @return The tab with all the ... of the word s
 */
private String[] GetAll(String S){
	String Syno[]=null;
	IndexWord index=null;
	Synset[] Synso=null;
	int sens=0;
	int nbmot=0;
	int i=0,j=0,k=0,l=0;
	boolean trouve=false;
	int strcomp=0;
	
	nbmot=CountMaxSens(S);
	Syno=new String[nbmot];

	try { index = Dictionary.getInstance().lookupIndexWord(POS.NOUN,S);}
	catch(Exception ex){ex.printStackTrace();
			                System.exit(-1);}
	if (index!=null){
						try {Synso=index.getSenses();
						sens= index.getSenseCount();}
					 catch (JWNLException e) {e.printStackTrace();}
	}
	
	for (i=0;i<sens;i++){
		for(j=0;j<Synso[i].getWordsSize();j++){
			for(l=0;l<k;l++){
				if (Synso[i].getWord(j).getLemma().compareTo(Syno[l])==0){trouve=true;}
				}
			if (trouve!=true){Syno[k++] =  Synso[i].getWord(j).getLemma();}
			trouve=false;
		}
	}
	try { index = Dictionary.getInstance().lookupIndexWord(POS.VERB,S);}
	catch(Exception ex){ex.printStackTrace();
			                System.exit(-1);}
	if (index!=null){
						try {Synso=index.getSenses();
						sens= index.getSenseCount();}
					 catch (JWNLException e) {e.printStackTrace();}
	}
	
	for (i=0;i<sens;i++){
		for(j=0;j<Synso[i].getWordsSize();j++){
			for(l=0;l<k;l++){
				if (Synso[i].getWord(j).getLemma().compareTo(Syno[l])==0){trouve=true;}
				}
			if (trouve!=true){
			Syno[k++] = Synso[i].getWord(j).getLemma();}
			trouve=false;
		}
	}
	
	try { index = Dictionary.getInstance().lookupIndexWord(POS.ADJECTIVE,S);}
	catch(Exception ex){ex.printStackTrace();
			                System.exit(-1);}
	if (index!=null){
						try {Synso=index.getSenses();
						sens= index.getSenseCount();}
					 catch (JWNLException e) {e.printStackTrace();}
	}
	
	for (i=0;i<sens;i++){
		for(j=0;j<Synso[i].getWordsSize();j++){
			for(l=0;l<k;l++){
				if (Synso[i].getWord(j).getLemma().compareTo(Syno[l])==0){trouve=true;}
				}
			if (trouve!=true){
			Syno[k++] =	Synso[i].getWord(j).getLemma();}
			trouve=false;
		}
	}
	
	try { index = Dictionary.getInstance().lookupIndexWord(POS.ADVERB,S);}
	catch(Exception ex){ex.printStackTrace();
			                System.exit(-1);}
	if (index!=null){
						try {Synso=index.getSenses();
						sens= index.getSenseCount();}
					 catch (JWNLException e) {e.printStackTrace();}
	}
	for (i=0;i<sens;i++){
		for(j=0;j<Synso[i].getWordsSize();j++){
			for(l=0;l<k;l++){
				if (Synso[i].getWord(j).getLemma().compareTo(Syno[l])==0){trouve=true;}
				}
			if (trouve!=true){
			Syno[k++] =	 Synso[i].getWord(j).getLemma();}
			trouve=false;
		}
	}
	return(Syno);
}

private String[] Inter(String[] S1, String[] S2){
	String result[]=null;
	int i=0,j=0,k=0,l=0;
	boolean trouve=false;
	
	i=S1.length;
	j=S2.length;
	if(i<j){result = new String[j];}
	else result=new String[i];
	
	for (i=0;i<S1.length;i++){
		for(j=0;j<S2.length;j++){
			if(S1[i]!=null && S2[j]!=null){
				if (S1[i].compareTo(S2[j])==0){trouve=true;}
			}
		}
		if (trouve==true){result[k++]=S1[i];}
		trouve=false;
	}
	return(result);
}

private String[] Union(String[] S1,String[] S2){
	String result[]=null;
	int i=0,j=0,k=0,l=0;
	boolean trouve=false;
	
	i=S1.length;
	j=S2.length;
	result = new String[i+j];
	
	for (i=0;i<S1.length;i++){result[k++]=S1[i];}
	for(j=0;j<S2.length;j++){
		for(l=0;l<S1.length;l++){
			
			if(S1[l]!=null && S2[j]!=null){
				if (S2[j].compareTo(S1[l])==0){trouve=true;}
			}
		}
		if(trouve==false){result[k++]=S2[j];}
		trouve=false;
	}
	return(result);
}

}