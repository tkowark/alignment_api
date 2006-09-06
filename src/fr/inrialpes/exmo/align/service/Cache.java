package fr.inrialpes.exmo.align.service;

import java.net.URI;
import java.util.Enumeration;
import org.semanticweb.owl.align.Alignment;

public interface Cache {
	int put(long id, URI uri1, URI uri2, Alignment alignmnet);
	Alignment get(long id);
	Enumeration get(URI uri);
	Enumeration get(URI uri1, URI uri2);
}
