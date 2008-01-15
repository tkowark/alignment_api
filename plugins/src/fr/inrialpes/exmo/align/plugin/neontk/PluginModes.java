package fr.inrialpes.exmo.align.plugin.neontk; 

//Code based in one file of the Prompt Plugin

//Modified by Fabio Oliveira
//15/04/06

import java.util.HashMap;

public class PluginModes {
    static public final String a = "Based on Alignment Server, this Plug-in ";
    //static public final String b = "Results ";

    //static public final String [] _modes = {a, b};
     static public final String [] _modes = {""};
     //static public final String aa = "performs mapping between two ontologies based on similarity heuristics of the individual entities (concepts, relations and instances). ";
     static public final String aa = "computes an alignment between two ontologies. ";
     //static public final String bb = "are given as pairs of aligned entities are dependent on a threshold value.";

    static private HashMap _details = new HashMap (_modes.length);
    static private boolean _detailsSet = false;

    public static String getDetails (String mode) {
      if (!_detailsSet) setDetails ();
      return (String)_details.get(mode);
    }

    public static String[] getModes () {return _modes;}


    private static void setDetails () {
      //_details.put(a, aa);
      _detailsSet = true;
    }

}