package fr.inrialpes.exmo.ontowrap;

public class Annotation {

    private final String value;
    private final String language;
    
    public Annotation(String value, String language) {
	this.value=value;
	this.language=language.intern();
    }
    
    public Annotation(String value) {
	this.value=value;
	this.language=null;
    }
    
    public String getValue() {
	return value;
    }
    
    public String getLanguage() {
	return language;
    }
    
    public String toString() {
	return value+'@'+language;
    }
    
    public int hashCode() {
	return value.hashCode();
    }
    
    public boolean equals(Object o) {
	if (o instanceof Annotation) {
	    Annotation a = (Annotation) o;
	    return value.equals(a.value) && language==a.language;
	}
	return false;
    }
}
