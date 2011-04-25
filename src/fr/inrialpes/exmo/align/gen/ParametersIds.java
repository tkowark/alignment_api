package fr.inrialpes.exmo.align.gen;

/*
 * The list of all the modifications that can be applied to the test generator
 */

public class ParametersIds {
	public static String ADD_SUBCLASS    = "addSubClass";
	public static String REMOVE_SUBCLASS = "removeSubClass";
	public static String REMOVE_PROPERTY = "removeProperty";
	public static String REMOVE_COMMENT  = "removeComment";
	public static String LEVEL_FLATTENED = "levelFlattened";
	public static String ADD_PROPERTY    = "addProperty";
	public static String REMOVE_CLASSES  = "removeClasses";	//remove classes from level
	//add c classes beginning from level l -> the value of this parameters should be:
	//beginning_level.number_of_classes_to_add
	public static String ADD_CLASSES      = "addClasses";
	public static String RENAME_PROPERTIES="renameProperties";	//rename properties
	public static String RENAME_CLASSES   ="renameClasses";		//rename classes
	public static String RENAME_RESOURCES = "renameResources";	//value is null for this parameter
	
	
	public static String REMOVE_RESTRICTION= "removeRestriction";	//remove restrictions

        public static String REMOVE_INDIVIDUALS= "removeIndividuals";	//remove individuals
        public static String NO_HIERARCHY      = "noHierarchy";         //no hierarchy

}
