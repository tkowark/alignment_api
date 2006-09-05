package fr.inrialpes.exmo.align.service;
import org.semanticweb.owl.align.Alignment;

public interface DBService {
	public long store(Alignment alignment);
	public Alignment find(long id);
	public int connect(String password);                              // password in database
	public int close();
}
