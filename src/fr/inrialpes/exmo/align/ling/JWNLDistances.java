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
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.PointerType;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.dictionary.Dictionary;
import net.didion.jwnl.data.PointerUtils;
import net.didion.jwnl.data.list.PointerTargetNode;
import net.didion.jwnl.data.list.PointerTargetNodeList;
import net.didion.jwnl.data.list.PointerTargetTree;
import net.didion.jwnl.data.relationship.AsymmetricRelationship;
import net.didion.jwnl.data.relationship.Relationship;
import net.didion.jwnl.data.relationship.SymmetricRelationship;

/**
 * Compute a string distance using the JWNL API (WordNet API)
 * 
 * @author Jerome Pierson, David Loup
 * @version $Id: JWNLDistances.java,v 1.0 2004/08/04
 *  
 */
public class JWNLDistances {

	public static final double NOUN_WEIGHT = 0.60;

	public static final double ADJ_WEIGHT = 0.25;

	public static final double VERB_WEIGHT = 0.15;

	// Results tables
	double[][] nounsResults;
	double[][] verbsResults;
	double[][] adjectivesResults;
	
	// Weights tables (masks)
	double[][] nounsMasks;
	double[][] verbsMasks;
	double[][] adjectivesMasks;

	/**
	 * Initialize the JWNL API. Must be done one time before computing distance
	 * Need to configure the file_properties.xml located in the current
	 * directory (ontoalign)
	 *  
	 */
	public void Initialize() {
		try {
			JWNL.initialize(new FileInputStream("./file_properties.xml"));
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(-1);
		}
	}

	/**
	 * Compute a basic distance between 2 strings using WordNet synonym.
	 * 
	 * @param s1
	 * @param s2
	 * @return Distance between s1 & s2 (return 1 if s2 is a synonym of s1, else
	 *         return a BasicStringDistance between s1 & s2)
	 */
	public double BasicSynonymDistance(String s1, String s2) {
		double Dist = 0.0;
		double Dists1s2;
		int i, j, k = 0;
		int synonymNb = 0;
		int besti = 0, bestj = 0;
		int syno = 0;
		double DistTab[];
		IndexWord index = null;
		Synset Syno[] = null;

		// Modification 09/13/2004 by David Loup
		s1 = s1.toUpperCase();
		s2 = s2.toUpperCase();

		Dists1s2 = StringDistances.subStringDistance(s1, s2);

		try {
			// Lookup for first string
			index = Dictionary.getInstance().lookupIndexWord(POS.NOUN, s1);
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(-1);
		}
		// if found in the dictionary
		if (index != null) {
			try {
				// get the groups of synonyms for each sense
				Syno = index.getSenses();
			} catch (JWNLException e) {
				e.printStackTrace();
			}
			// number of senses for the word s1
			synonymNb = index.getSenseCount();
			DistTab = new double[synonymNb];
			// for each sense
			for (k = 0; k < synonymNb; k++) {
				// for each synonym of this sense
				for (j = 0; j < Syno[k].getWordsSize(); j++) {
					Dist = StringDistances.subStringDistance(Syno[k].getWord(j)
							.getLemma(), s2);
					if (Dist < Dists1s2) {
						Dists1s2 = Dist;
						besti = k;
						bestj = j;
					}
				}
			}
		}

		return Dists1s2;
	}

	public double computeSimilarity(String s1, String s2) {
		double sim = 0.0;
		double Dists1s2;
		int i, j, k = 0;
		int synonymNb = 0;
		int besti = 0, bestj = 0;
		int syno = 0;
		double DistTab[];
		IndexWord index = null;
		Synset Syno1[], Syno2[] = null;

		Dists1s2 = StringDistances.subStringDistance(s1, s2);

		if (s1.equals(s2)) {
			//System.out.println(s1+" - "+s2+" = "+ (1-Dists1s2) + "|" + sim);
			return 1;
		}
		else {

			if (s1.equals(s1.toUpperCase()) || s1.equals(s1.toLowerCase())) {
				try {
					// Lookup for first string
					index = Dictionary.getInstance().lookupIndexWord(POS.NOUN,
							s1);
					if (index == null) {
						index = Dictionary.getInstance().lookupIndexWord(
								POS.ADJECTIVE, s1);
					}
					if (index == null) {
						index = Dictionary.getInstance().lookupIndexWord(
								POS.VERB, s1);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
					System.exit(-1);
				}
				// if not found in the dictionary
				if (index == null) {
					//System.out.println(s1+" - "+s2+" = "+ (1-Dists1s2) + "|" + sim);
					return (1 - Dists1s2);
				}
				else {
					sim = computeBestMatch(s1, s2);
				}
			} else {
				sim = computeBestMatch(s1, s2);
			}
		}
		//System.out.println(s1+" - "+s2+" = "+ (1-Dists1s2) + "||" + sim);
		return Math.max(sim,1 - Dists1s2);
		//return sim;
	}

	public double computeBestMatch(String s1, String s2) {
		Vector s1Tokens = new Vector();
		Vector s2Tokens = new Vector();
		tokenizes(s1, s1Tokens);
		tokenizes(s2, s2Tokens);

		// tokens storage
		Vector vg;
		Vector vp;
		vg = s1Tokens.size() >= s2Tokens.size() ? s1Tokens : s2Tokens;
		vp = s1Tokens.size() >= s2Tokens.size() ? s2Tokens : s1Tokens;

		double dist = 0;

		// tokens depending on their nature
		Hashtable nouns1 = new Hashtable();
		Hashtable adjectives1 = new Hashtable();
		Hashtable verbs1 = new Hashtable();

		Hashtable nouns2 = new Hashtable();
		Hashtable adjectives2 = new Hashtable();
		Hashtable verbs2 = new Hashtable();

		// Put tokens in the right category
		Iterator gIt = vg.iterator();
		while (gIt.hasNext()) {
			lookUpWord((String) gIt.next(), nouns1, adjectives1, verbs1);
		}
		Iterator pIt = vp.iterator();
		while (pIt.hasNext()) {
			lookUpWord((String) pIt.next(), nouns2, adjectives2, verbs2);
		}
		//System.out.println(nouns1);
		//System.out.println("Adj : "+adjectives1.size()+"\n"+adjectives1);
		//System.out.println(verbs1);
		//System.out.println(nouns2);
		//System.out.println("Adj : "+adjectives2.size()+"\n"+adjectives2);
		//System.out.println(verbs2);

		nounsResults = new double[nouns1.size()][nouns2.size()];
		verbsResults = new double[verbs1.size()][verbs2.size()];
		adjectivesResults = new double[adjectives1.size()][adjectives2.size()];

		nounsMasks = new double[nouns1.size()][nouns2.size()];
		fillWithOnes(nounsMasks);
		verbsMasks = new double[verbs1.size()][verbs2.size()];
		fillWithOnes(verbsMasks);
		adjectivesMasks = new double[adjectives1.size()][adjectives2.size()];
		fillWithOnes(adjectivesMasks);

		
		double weightSum = 0;

		int posX = 0;
		int posY = 0;

		if (!(nouns1.size() == 0 && nouns2.size() == 0)) {
			Enumeration nounsEnum1 = nouns1.keys();
			while (nounsEnum1.hasMoreElements()) {
				posY = 0;
				String token1 = (String) nounsEnum1.nextElement();
				IndexWord index1 = (IndexWord) nouns1.get(token1);
				Enumeration nounsEnum2 = nouns2.keys();
				while (nounsEnum2.hasMoreElements()) {
					String token2 = (String) nounsEnum2.nextElement();
					IndexWord index2 = (IndexWord) nouns2.get(token2);
					double nb1 = getNumberOfOccurences(token1,nouns1,adjectives1,verbs1);
					double nb2 = getNumberOfOccurences(token2,nouns2,adjectives2,verbs2);
					nounsMasks[posX][posY] = 1/(nb1 + nb2);
					double sim = findMatchForTokens(index1, index2);
					nounsResults[posX][posY] = sim ;
					//Sytem.out.println(token1 + " -- " + token2 + " = " + sim);
					posY++;
				}
				posX++;
			}
			weightSum += NOUN_WEIGHT;
		}
		//Sytem.out.println("Nouns");
		//displayMatrix(nounsResults);
		//Sytem.out.println("Nouns Masks");
		//displayMatrix(nounsMasks);
		double nounsBestMatch = bestMatch(nounsResults, nounsMasks);
		//Sytem.out.println("nouns BM = "+nounsBestMatch);

		posX = 0;
		posY = 0;
		if (!(verbs1.size() == 0 && verbs2.size() == 0)) {
			Enumeration verbsEnum1 = verbs1.keys();
			while (verbsEnum1.hasMoreElements()) {
				posY = 0;
				String token1 = (String) verbsEnum1.nextElement();
				IndexWord index1 = (IndexWord) verbs1.get(token1);
				Enumeration verbsEnum2 = verbs2.keys();
				while (verbsEnum2.hasMoreElements()) {
					String token2 = (String) verbsEnum2.nextElement();
					IndexWord index2 = (IndexWord) verbs2.get(token2);
					double nb1 = getNumberOfOccurences(token1,nouns1,adjectives1,verbs1);
					double nb2 = getNumberOfOccurences(token2,nouns2,adjectives2,verbs2);
					verbsMasks[posX][posY] = 1/(nb1 + nb2) ;
					double sim = findMatchForTokens(index1, index2);
					verbsResults[posX][posY] = sim ;
					//Sytem.out.println(token1 + " -- " + token2 + " = " + sim);
					posY++;
				}
				posX++;
			}
			weightSum += VERB_WEIGHT;
		}
		//Sytem.out.println("Verbs");
		//displayMatrix(verbsResults);
		//Sytem.out.println("Verbs Masks");
		//displayMatrix(verbsMasks);
		double verbsBestMatch = bestMatch(verbsResults, verbsMasks);
		//Sytem.out.println("verbs BM = "+verbsBestMatch);

		posX = 0;
		posY = 0;
		if (!(adjectives1.size() == 0 && adjectives2.size() == 0)) {

			Enumeration adjEnum1 = adjectives1.keys();
			while (adjEnum1.hasMoreElements()) {
				posY = 0;
				String token1 = (String) adjEnum1.nextElement();
				IndexWord index1 = (IndexWord) adjectives1.get(token1);
				Enumeration adjEnum2 = adjectives2.keys();
				while (adjEnum2.hasMoreElements()) {
					String token2 = (String) adjEnum2.nextElement();
					IndexWord index2 = (IndexWord) adjectives2.get(token2);
					double nb1 = getNumberOfOccurences(token1,nouns1,adjectives1,verbs1);
					double nb2 = getNumberOfOccurences(token2,nouns2,adjectives2,verbs2);
					adjectivesMasks[posX][posY] = 1/(nb1 + nb2);
					double sim = findMatchForAdj(index1, index2);
					adjectivesResults[posX][posY] = sim ;
					//Sytem.out.println(token1 + " -- " + token2 + " = " + sim);
					posY++;
				}
				posX++;
			}
			weightSum += ADJ_WEIGHT;
		}
		//Sytem.out.println("Adjectives");
		//displayMatrix(adjectivesResults);
		//Sytem.out.println("Adjectives Masks");
		//displayMatrix(adjectivesMasks);
		double adjBestMatch = bestMatch(adjectivesResults, adjectivesMasks);
		//Sytem.out.println("adjs BM = "+adjBestMatch);

		if (weightSum == 0) {
			//System.err.println("failed");
			return 0;
		}

		double result = (nounsBestMatch * NOUN_WEIGHT + verbsBestMatch
				* VERB_WEIGHT + adjBestMatch * ADJ_WEIGHT)
				/ weightSum;
		//Sytem.out.println("res = "+result);
		return result;
	}

	public double findMatchForTokens(IndexWord index1, IndexWord index2) {
		// the max number of common concepts between the two tokens
		double maxCommon = 0;

		// the two lists giving the best match
		PointerTargetNodeList best1 = new PointerTargetNodeList();
		PointerTargetNodeList best2 = new PointerTargetNodeList();

		// the two lists currently compared
		PointerTargetNodeList ptnl1 = new PointerTargetNodeList();
		PointerTargetNodeList ptnl2 = new PointerTargetNodeList();

		if (index1 != null && index2 != null) {
			// The two tokens existe in WordNet, we find the "depth"
			try {
				// Best match between current lists
				int maxBetweenLists = 0;

				// Synsets for each token
				Synset[] Syno1 = index1.getSenses();
				Synset[] Syno2 = index2.getSenses();
				for (int i = 0; i < index1.getSenseCount(); i++) {

					Synset synset1 = Syno1[i];
					for (int k = 0; k < index2.getSenseCount(); k++) {

						Synset synset2 = Syno2[k];

						List hypernymList1 = PointerUtils.getInstance()
								.getHypernymTree(synset1).toList();
						List hypernymList2 = PointerUtils.getInstance()
								.getHypernymTree(synset2).toList();

						Iterator list1It = hypernymList1.iterator();
						// browse lists
						while (list1It.hasNext()) {
							ptnl1 = (PointerTargetNodeList) list1It.next();
							Iterator list2It = hypernymList2.iterator();
							while (list2It.hasNext()) {
								ptnl2 = (PointerTargetNodeList) list2It.next();

								int cc = getCommonConcepts(ptnl1, ptnl2);
								if (cc > maxBetweenLists) {
									maxBetweenLists = cc;
									best1 = ptnl1;
									best2 = ptnl2;
								}
							}
						}
						if (maxBetweenLists > maxCommon) {
							maxCommon = maxBetweenLists;
						}
					}
				}
				//System.out.println("common = " + maxCommon);
				//System.out.println("value = "
				//		+ ((2 * maxCommon) / (best1.size() + best2.size())));
				//if (best1 != null) best1.print();
				//if (best2 != null) best2.print();
				if (best1.size() == 0 && best2.size() == 0)
					return 0;
				return (2 * maxCommon / (best1.size() + best2.size()));
			} catch (JWNLException je) {
				je.printStackTrace();
				System.exit(-1);
			}
		}
		return 0;
	}

	public double findMatchForAdj(IndexWord index1, IndexWord index2) {
		// the max number of common concepts between the two tokens
		double value = 0;

		if (index1 != null && index2 != null) {
			// The two tokens existe in WordNet, we find the "depth"
			try {
				// Synsets for each token
				Synset[] Syno1 = index1.getSenses();
				Synset[] Syno2 = index2.getSenses();
				for (int i = 0; i < index1.getSenseCount(); i++) {

					Synset synset1 = Syno1[i];
					for (int k = 0; k < index2.getSenseCount(); k++) {

						Synset synset2 = Syno2[k];

						PointerTargetNodeList adjSynonymList = PointerUtils
								.getInstance().getSynonyms(synset1);

						Iterator listIt = adjSynonymList.iterator();
						// browse lists
						while (listIt.hasNext()) {
							PointerTargetNode ptn = (PointerTargetNode) listIt
									.next();
							if (ptn.getSynset() == synset2) {
								value = 1;
							}
						}
					}
				}
				//System.out.println("value = " + value);
				return value;
			} catch (JWNLException je) {
				je.printStackTrace();
				System.exit(-1);
			}
		}
		return 0;
	}

	public void tokenizes(String s, Vector sTokens) {
		String str1 = s;
		// starts on the second character of the string
		int start = 0;
		int car = start + 1;
		while (car < str1.length()) {
			while (car < str1.length() && !(str1.charAt(car) < 'Z')) {
				car++;
			}
			sTokens.add(str1.substring(start, car));
			start = car;
			car = start + 1;
		}
	}

	/**
	 * TODO Look up for other things than nouns
	 * 
	 * @param word
	 * @return
	 */
	public void lookUpWord(String word, Hashtable nouns, Hashtable adjectives,
			Hashtable verbs) {
		IndexWord index = null;
		try {
			// Lookup for word in adjectives
			index = Dictionary.getInstance().lookupIndexWord(POS.ADJECTIVE,
					word);
			if (index != null) {
				adjectives.put(word, index);
			}
			// Lookup for word in nouns
			index = Dictionary.getInstance().lookupIndexWord(POS.NOUN, word);
			if (index != null) {
				nouns.put(word, index);
			}
			// Lookup for word in verbs
			index = Dictionary.getInstance().lookupIndexWord(POS.VERB, word);
			if (index != null) {
				verbs.put(word, index);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(-1);
		}
	}

	public void display(Synset syn) {
		String str = "";
		for (int s = 0; s < syn.getWordsSize(); s++) {
			str += syn.getWord(s);
		}
		//System.out.println(str);
	}

	public int getCommonConcepts(PointerTargetNodeList list1,
			PointerTargetNodeList list2) {
		int cc = 0;
		int i = 1;
		while (i <= Math.min(list1.size(), list2.size())
				&& ((PointerTargetNode) list1.get(list1.size() - i))
						.getSynset() == ((PointerTargetNode) list2.get(list2
						.size()
						- i)).getSynset()) {
			cc++;
			i++;
		}
		return cc;

	}

	private double bestMatch(double matrix[][], double[][] mask) {


		int nbrLines = matrix.length;
		
		if (nbrLines ==0 )
				return 0 ;
				
		int nbrColumns = matrix[0].length;
		double sim = 0;

		int minSize = (nbrLines >= nbrColumns) ? nbrColumns : nbrLines;
		
		if (minSize ==0 )
			return 0 ;
			
		for (int k = 0; k < minSize; k++) {
			double max_val = 0;
			int max_i = 0;
			int max_j = 0;
			for (int i = 0; i < nbrLines; i++) {
				for (int j = 0; j < nbrColumns; j++) {
					if (max_val < matrix[i][j]) {
						max_val = matrix[i][j];
						
						// mods
						if (matrix[i][j] > 0.3) max_val = matrix[i][j];
						else max_val = matrix[i][j] * mask[i][j];
						// end mods
						
						max_i = i;
						max_j = j;
					}
				}
			}
			for (int i = 0; i < nbrLines; i++) {
				matrix[i][max_j] = 0;
			}
			for (int j = 0; j < nbrColumns; j++) {
				matrix[max_i][j] = 0;
			}
			sim += max_val;
		}
		return sim / (nbrLines + nbrColumns - minSize);
	}

	// Find the number of occurences of a words in different categories
	public static int getNumberOfOccurences(String token, Hashtable nouns,
			Hashtable adj, Hashtable verbs) {
		int nb = 0;
		if (nouns.get(token) != null)
			nb++;
		if (adj.get(token) != null)
			nb++;
		if (verbs.get(token) != null)
			nb++;
		return nb;
	}

	public void displayMatrix(double[][] matrix) {
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++) {
				System.out.println("[" + matrix[i][j] + "]");
			}
		}
	}
	
	public void fillWithOnes(double[][] matrix) {
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++) {
				matrix[i][j] = 1;
			}
		}
	}

	/* Getters */
	public double[][] getAdjectivesResults() {
		return adjectivesResults;
	}

	public double[][] getNounsResults() {
		return nounsResults;
	}

	public double[][] getVerbsResults() {
		return verbsResults;
	}

	public static void main(String[] args) {
		Vector v = new Vector();
		JWNLDistances j = new JWNLDistances();
		j.Initialize();
		String s1 = "Monograph";
		String s2 = "Book";
		System.out.println("Sim = "+ j.computeSimilarity(s1, s2));
		System.out.println("SimOld = "+ (1 - j.BasicSynonymDistance(s1, s2)));
	}
}