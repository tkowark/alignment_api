/*
 * $Id$ Copyright
 * (C) University of Montréal, 2004-2005 Copyright (C) INRIA Rhône-Alpes,
 * 2004-2005 This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the License,
 * or (at your option) any later version. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU Lesser General Public License for more details. You should have
 * received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */

package fr.inrialpes.exmo.align.ling;

import fr.inrialpes.exmo.align.impl.method.StringDistances;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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
 * @author Jerome Pierson, David Loup
 * @version $Id: JWNLDistances.java,v 1.0 2004/08/04
 */
public class JWNLDistances {

    public static final double NOUN_WEIGHT = 0.60;

    public static final double ADJ_WEIGHT  = 0.25;

    public static final double VERB_WEIGHT = 0.15;
    
    private static final double MINIMUM_DISTANCE = 0.05;

    // Results tables
    double[][]                 nounsResults;

    double[][]                 verbsResults;

    double[][]                 adjectivesResults;

    // Weights tables (masks)
    double[][]                 nounsMasks;

    double[][]                 verbsMasks;

    double[][]                 adjectivesMasks;
    
    private FileInputStream fis;

    // tokens depending on their nature
    // PG: These are now global variables.
    private Hashtable nouns1        = new Hashtable();
    private Hashtable adjectives1   = new Hashtable();
    private Hashtable verbs1        = new Hashtable();
    private Hashtable nouns2        = new Hashtable();
    private Hashtable adjectives2   = new Hashtable();
    private Hashtable verbs2        = new Hashtable();
    
    /**
     * Initialize the JWNL API. Must be done one time before computing distance
     * Need to configure the file_properties.xml located in the current
     * directory (ontoalign)
     */
    public void Initialize() {
        try {
            JWNL.initialize(new FileInputStream("./file_properties.xml"));
        }
        catch (Exception ex) {
            ex.printStackTrace();
            System.exit(-1);
        }
    }

    /**
     * Compute a basic distance between 2 strings using WordNet synonym.
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

        Dists1s2 = StringDistances.subStringDistance(s1,
            s2);

        try {
            // Lookup for first string
            index = Dictionary.getInstance().lookupIndexWord(POS.NOUN,
                s1);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            System.exit(-1);
        }
        // if found in the dictionary
        if (index != null) {
            try {
                // get the groups of synonyms for each sense
                Syno = index.getSenses();
            }
            catch (JWNLException e) {
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
                            .getLemma(),
                        s2);
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
        Dictionary dictionary = Dictionary.getInstance();
        double sim = 0.0;
        double dists1s2;
        IndexWord index = null;

        dists1s2 = StringDistances.subStringDistance(s1, s2);
        if (dists1s2 < MINIMUM_DISTANCE) {
            return (1 - dists1s2);
        }
        
        if (s1.equals(s2) || s1.toLowerCase().equals(s2.toLowerCase())) {
            return 1;
        }
        else {
            if (s1.equals(s1.toUpperCase()) || s1.equals(s1.toLowerCase())) {
                try {
                    // Lookup for first string
                    index = dictionary.lookupIndexWord(POS.NOUN, s1);
                    if (index == null) {
                        index = Dictionary.getInstance()
                                .lookupIndexWord(POS.ADJECTIVE, s1);
                    }
                    if (index == null) {
                        index = Dictionary.getInstance().lookupIndexWord(POS.VERB, s1);
                    }
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                    System.exit(-1);
                }
                // if not found in the dictionary
                if (index == null) {
                    return (1 - dists1s2);
                }
                else {
                    sim = compareComponentNames(s1, s2);
                }
            }
            else {
                sim = compareComponentNames(s1, s2);
            }
        }
        return Math.max(sim, 1 - dists1s2);
        // return sim;
    }
    
    /**
     * Updates the values contained in specified mask and results matrix.
     * @param hash1 First hashtable (<i>eg.</i> containing the nouns
     * of the first ontology).
     * @param hash2 Second hashtable (<i>eg.</i> containing the nouns
     * of the second ontology).
     * @param mask A mask matrix.
     * @param results A result matrix.
     * @deprecated The use of a mask is deprecated.
     */
    private void updateMaskAndResults(Hashtable hash1, Hashtable hash2, double[][] mask, double[][] results) {
        Enumeration enum1, enum2;
        String token1, token2;
        IndexWord index1, index2;
        int x, y;
        double nb1, nb2;
        
        x = 0;
        y = 0;
        enum1 = hash1.keys();
        while (enum1.hasMoreElements()) {
            token1 = (String) enum1.nextElement();
            index1 = (IndexWord) hash1.get(token1);
            enum2 = hash2.keys();
            while (enum2.hasMoreElements()) {
                token2 = (String) enum2.nextElement();
                index2 = (IndexWord) hash2.get(token2);
                
                nb1 = getNumberOfOccurences(token1, 1);
                nb2 = getNumberOfOccurences(token2, 2);
                
                mask[x][y] = 2 / (nb1 + nb2);
                results[x][y] = computeTokenSimilarity(index1, index2);
                
                y++;
            }
            x++;
        }
    }

    public double compareComponentNames(String s1, String s2) {
        Vector s1Tokens;
        Vector s2Tokens;
        Dictionary dictionary = Dictionary.getInstance(); 
        IndexWord indexNoun1, indexNoun2;
        IndexWord indexAdj1, indexAdj2;
        IndexWord indexVerb1, indexVerb2;
        Iterator pIt, gIt;
        Vector vg, vp;
        String token1, token2;
        double simAsAdj, simAsNoun, simAsVerb;
        double maxSim;
        double[][] simMatrix;
        int i, j;
        
        s1Tokens = tokenize(s1);
        s2Tokens = tokenize(s2);

        // tokens storage

        vg = (s1Tokens.size() >= s2Tokens.size()) ? s1Tokens : s2Tokens;
        vp = (s1Tokens.size() >= s2Tokens.size()) ? s2Tokens : s1Tokens;

        // TODO: Don't forget to switch comments.
        // Initializes the tokens hashtables.
        /*this.nouns1        = new Hashtable();
        this.adjectives1   = new Hashtable();
        this.verbs1        = new Hashtable();
        this.nouns2        = new Hashtable();
        this.adjectives2   = new Hashtable();
        this.verbs2        = new Hashtable();
        */
        
        simMatrix = new double[vg.size()][vp.size()];
        
        i = 0;
        gIt = vg.iterator();
        try {
            while (gIt.hasNext()) {
                token1 = (String) gIt.next();
                
                indexNoun1 = dictionary.lookupIndexWord(POS.NOUN, token1);
                indexAdj1 = dictionary.lookupIndexWord(POS.ADJECTIVE, token1);
                indexVerb1 = dictionary.lookupIndexWord(POS.VERB, token1);

                j = 0;
                pIt = vp.iterator();
                while (pIt.hasNext()) {
                    token2 = (String) pIt.next();
                    
                    indexNoun2 = dictionary.lookupIndexWord(POS.NOUN, token2);
                    indexAdj2 = dictionary.lookupIndexWord(POS.ADJECTIVE, token2);
                    indexVerb2 = dictionary.lookupIndexWord(POS.VERB, token2);
                    
                    simAsAdj = this.computeTokenSimilarity(indexAdj1, indexAdj2);
                    maxSim = simAsAdj;
                    simAsNoun = this.computeTokenSimilarity(indexNoun1, indexNoun2);
                    maxSim = (simAsNoun > maxSim) ? simAsNoun : maxSim;
                    simAsVerb = this.computeTokenSimilarity(indexVerb1, indexVerb2);
                    maxSim = (simAsVerb > maxSim) ? simAsVerb : maxSim;
                    
                    simMatrix[i][j] = maxSim;
                    j++;
                }
                i++;
            }
        }
        catch (JWNLException ex) {
            ex.printStackTrace();
        }
        
        return bestMatch(simMatrix);
        
        /*
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

        if (! (nouns1.isEmpty() && nouns2.isEmpty())) {
            this.updateMaskAndResults(
                this.nouns1,
                this.nouns2,
                this.nounsMasks,
                this.nounsResults);
            weightSum += NOUN_WEIGHT;
        }
        
        double nounsBestMatch = bestMatch(nounsResults, nounsMasks);

        if (! (verbs1.isEmpty() && verbs2.isEmpty())) {
            this.updateMaskAndResults(
                this.verbs1,
                this.verbs2,
                this.verbsMasks,
                this.verbsResults);
            weightSum += VERB_WEIGHT;
        }
        
        double verbsBestMatch = bestMatch(verbsResults, verbsMasks);

        if (! (adjectives1.isEmpty() && adjectives2.isEmpty())) {
            this.updateMaskAndResults(
                this.adjectives1,
                this.adjectives2,
                this.adjectivesMasks,
                this.adjectivesResults);
            weightSum += ADJ_WEIGHT;
        }

        double adjBestMatch = bestMatch(adjectivesResults, adjectivesMasks);

        if (weightSum == 0) {
            // System.err.println("failed");
            return 0;
        }

        double result = (nounsBestMatch * NOUN_WEIGHT
            + verbsBestMatch
            * VERB_WEIGHT + adjBestMatch * ADJ_WEIGHT) / weightSum;
        // Sytem.out.println("res = "+result);
        return result;
        */
    }

    public double computeTokenSimilarity(IndexWord index1, IndexWord index2) {
        // the max number of common concepts between the two tokens
        double maxCommon = 0;

        // the two lists giving the best match
        PointerTargetNodeList best1 = new PointerTargetNodeList();
        PointerTargetNodeList best2 = new PointerTargetNodeList();

        // the two lists currently compared
        PointerTargetNodeList ptnl1 = new PointerTargetNodeList();
        PointerTargetNodeList ptnl2 = new PointerTargetNodeList();

        if (index1 != null && index2 != null) {
            // The two tokens exist in WordNet, we find the "depth"
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
                // System.out.println("common = " + maxCommon);
                // System.out.println("value = "
                // + ((2 * maxCommon) / (best1.size() + best2.size())));
                // if (best1 != null) best1.print();
                // if (best2 != null) best2.print();
                if (best1.isEmpty() && best2.isEmpty())
                    return 0;
                return (2 * maxCommon / (best1.size() + best2.size()));
            }
            catch (JWNLException je) {
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

                        PointerTargetNodeList adjSynonymList = 
                            PointerUtils.getInstance().getSynonyms(synset1);

                        Iterator listIt = adjSynonymList.iterator();
                        // browse lists
                        while (listIt.hasNext()) {
                            PointerTargetNode ptn = (PointerTargetNode) listIt.next();
                            if (ptn.getSynset() == synset2) {
                                value = 1;
                            }
                        }
                    }
                }
                // System.out.println("value = " + value);
                return value;
            }
            catch (JWNLException je) {
                je.printStackTrace();
                System.exit(-1);
            }
        }
        return 0;
    }


	public boolean isAlphaNum(char c) {
		return (c >= 'A') && (c <= 'Z') || (c >= 'a') && (c <= 'z') || (c >= '0') && (c <= '9');
	}

	public boolean isAlpha(char c) {
		return (c >= 'A') && (c <= 'Z') || (c >= 'a') && (c <= 'z');
	}

	public boolean isAlphaCap(char c) {
		return (c >= 'A') && (c <= 'Z');
	}

	public boolean isAlphaSmall(char c) {
		return (c >= 'a') && (c <= 'z');
	}
	
	// the new tokenizer
	// firsst looks for non-alphanumeric chars in the string
	// if any, they will be taken as the only delimiters
	// otherwise the standard naming convention will be assumed:
	// words start with a capital letter
	// substring of capital letters will be seen as a whole
	// if it is a suffix
	// otherwise the last letter will be taken as the new token
	// start
	public Vector tokenize(String s) {
		String str1 = s;
		int sLength = s.length();
		Vector vTokens = new Vector();
		
		// 1. detect possible delimiters
		// starts on the first character of the string
		int tkStart = 0;
		int tkEnd = 0;
		
		// looks for the first delimiter
		while (tkStart < sLength  && isAlpha (str1.charAt(tkStart))) {
			tkStart++;
		}
		
		// if there is one then the tokens will be the
		// substrings between delimiters
		if (tkStart < sLength){
			
			// reset start and look for the first token
			tkStart = 0;

			// ignore leading separators
			while (tkStart < sLength  && ! isAlpha (str1.charAt(tkStart))) {
				tkStart++;
			}

			tkEnd = tkStart+1;

			while (tkStart < sLength) {

				// consumption of the token
				while (tkEnd < sLength  && isAlpha (str1.charAt(tkEnd))) {
					tkEnd++;
				}
				// creation
				vTokens.add(str1.substring(tkStart, tkEnd));
				
				// ignoring intermediate delimiters
								
				while (tkEnd < sLength  && !isAlpha (str1.charAt(tkEnd))) {
					tkEnd++;
				}
			
				tkStart=tkEnd;
			}

			
		}
		
		// else the standard naming convention will be used
		// TO DO :: include numbers: (between parts)
		// 
		else{
			// start at the beginning of the string
			tkStart = 0;			

			tkEnd = tkStart+1;

			while (tkStart < sLength) {
				// INV: thStart is always the first char of a token or the
				// position after the string end
				// consumption of the leading Caps
				while (tkEnd < sLength  && isAlphaCap (str1.charAt(tkEnd))) {
					tkEnd++;
				}

				// at this point tkEnd is pointing at:
				// a) the first small letter OR
				// b) the end of the string
				// c) [NOT YET DONE] a number
				
				// if a) look whether there are more than one caps in a row
				if (tkEnd < sLength) {
					
					// if there are several this should be an abbreviation
					// so make a token out of them, till the second last one
					// and update the position of tkStart
					if (tkEnd - tkStart > 1) {
						
						vTokens.add(str1.substring(tkStart, tkEnd-1));
						tkStart = tkEnd-1;
					
					}
					else {
						// if there is only one, this is the beginning of the
						// token
						// so just go on
					}
					
				}
				else {
					// if b) i.e., this is the string end, just create the token
					// 
				}
				// at this point th Start is the leading letter of a token that
				// has at least one
				// small letter (regardless of whether its first one is a
				// capital or not)
				// OR it is of length one
				
				while (tkEnd < sLength  && isAlphaSmall (str1.charAt(tkEnd))) {
					tkEnd++;
				}
				
				
				vTokens.add(str1.substring(tkStart, tkEnd));
				tkStart=tkEnd;			

			}
			

		}

		// PV: Debug 
		// System.out.println("Tokens = "+ vTokens.toString());		
			return vTokens;
	}
	
    
    
    // PG: The method now returns an instance of Vector.
    /**
     * @param s A string.
     * @return a vector containing a collection of tokens.
     */
    public Vector tokenizeDep(String s) {
        Vector sTokens = new Vector();
        String str1 = s;
        
        // starts on the second character of the string
        int start = 0;
        // PV igore leading separators
        while (start < str1.length() &&
                ((str1.charAt(start) < 'A') || (str1.charAt(start) > 'z'))
                || ((str1.charAt(start) < 'a') && (str1.charAt(start) > 'Z'))) {
                start++;
        }

        int car = start + 1;
        while (car < str1.length()) {
            while (car < str1.length() &&
                    (str1.charAt(car) >= 'a') &&
                    (str1.charAt(car) <= 'z')) {
                // PV while (car < str1.length() && (str1.charAt(car) > 'Z')) {
                // PV while (car < str1.length() && !(str1.charAt(car) < 'Z')) {
                car++;
            }
            // PV : correction of the separator problem
            if ((car < str1.length()) &&
                    (str1.charAt(car) >= 'A') &&
                    (str1.charAt(car) <= 'Z')) {
                // PV : leave single capitals with the previous token
                // (abbreviations)
                if ((car < str1.length() - 1) &&
                        ((str1.charAt(car+1) < 'a') ||
                        (str1.charAt(car+1) > 'z'))) {
                    car++;
                }
                else if (car == str1.length() - 1) {
                    car++;
                }
            }
            sTokens.add(str1.substring(start, car));
            start = car;
            // PV igore leading separators
            while (start < str1.length() &&
                    ((str1.charAt(start) < 'A') || (str1.charAt(start) > 'z')
                    || (str1.charAt(start) < 'a') && (str1.charAt(start) > 'Z') )) {
                start++;
            }
            car = start + 1;
        }
        // PV: Debug System.out.println("Tokens = "+ sTokens.toString());
    
        return sTokens;
    }

    /**
     * TODO Look up for other things than nouns
     * @param word
     * @return
     */
    public void lookUpWord(String word, Hashtable nouns, Hashtable adjectives,
            Hashtable verbs) {
        IndexWord index = null;
        
        try {
            // Lookup for word in adjectives
            index = Dictionary.getInstance().lookupIndexWord(POS.ADJECTIVE, word);
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
        }
        catch (Exception ex) {
            ex.printStackTrace();
            System.exit(-1);
        }
    }
    
    public void display(Synset syn) {
        String str = "";
        for (int s = 0; s < syn.getWordsSize(); s++) {
            str += syn.getWord(s);
        }
        // System.out.println(str);
    }

    public int getCommonConcepts(PointerTargetNodeList list1,
        PointerTargetNodeList list2) {
        int cc = 0;
        int i = 1;
        while (i <= Math.min(list1.size(),
            list2.size()) && ((PointerTargetNode) list1.get(list1.size() - i))
                .getSynset() == ((PointerTargetNode) list2
                .get(list2.size() - i)).getSynset()) {
            cc++;
            i++;
        }
        return cc;

    }
    
    //private double bestMatch(double matrix[][], double mask[][]) {
    private double bestMatch(double matrix[][]) {

        int nbrLines = matrix.length;

        if (nbrLines == 0)
            return 0;

        int nbrColumns = matrix[0].length;
        double sim = 0;

        int minSize = (nbrLines >= nbrColumns) ? nbrColumns : nbrLines;

        if (minSize == 0)
            return 0;

        for (int k = 0; k < minSize; k++) {
            double max_val = 0;
            int max_i = 0;
            int max_j = 0;
            for (int i = 0; i < nbrLines; i++) {
                for (int j = 0; j < nbrColumns; j++) {
                    if (max_val < matrix[i][j]) {
                        max_val = matrix[i][j];

                        /* mods
                        if (matrix[i][j] > 0.3)
                            max_val = matrix[i][j];
                        else
                            max_val = matrix[i][j] * mask[i][j];
                        end mods */
                        
                        max_val = matrix[i][j];
                        
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

    /**
     * @author Philippe Guégan
     * @param token A token.
     * @param n The number of the ontology (typically 1 or 2).
     * @return the number of occurences of the token in the hashtables
     * nouns, adjectives and verbs.
     */
    public int getNumberOfOccurences(String token, int n) {
        switch (n) {
            case 1:
                return getNumberOfOccurences(token,
                    this.nouns1,
                    this.adjectives1,
                    this.verbs1);
            case 2:
                return getNumberOfOccurences(token,
                    this.nouns2,
                    this.adjectives2,
                    this.verbs2);
            default:
                return 0;
        }
    }
    
    // Find the number of occurences of a words in different categories
    public int getNumberOfOccurences(String token, Hashtable nouns,
        Hashtable adj, Hashtable verbs) {
        int nb = 0;
        if (nouns.containsKey(token))
            nb++;
        if (adj.containsKey(token))
            nb++;
        if (verbs.containsKey(token))
            nb++;
        return nb;
    }

    public void displayMatrix(double[][] matrix) {
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                System.out.println("[" + matrix[i][j]
                    + "]");
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
        String s1 = "French997Guy";
        String s2 = "Dutch_Goaly";
//        try {
//            IndexWord index1 = Dictionary.getInstance().getIndexWord(POS.NOUN, s1);
//            IndexWord index2 = Dictionary.getInstance().getIndexWord(POS.NOUN, s2);
//            System.out.println(j.computeTokenSimilarity(index1, index2));
//        }
//        catch (JWNLException e) {
//            e.printStackTrace();
//        }
        System.out.println("SimWN = " + j.compareComponentNames(s1, s2));
//        System.out.println("Sim = " + j.computeSimilarity(s1, s2));
//        System.out.println("SimOld = " + (1 - j.BasicSynonymDistance(s1, s2)));
//        System.out.println("SimSubs = " + (1 - StringDistances.subStringDistance(s1, s2)));
    }
}
