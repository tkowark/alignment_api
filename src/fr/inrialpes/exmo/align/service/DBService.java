package fr.inrialpes.exmo.align.service;
import org.semanticweb.owl.align.*;

public interface DBService {
	public long store(Alignment alignment);
	public String retrieve(long id);
	public int init();                              // without userID, password in database
	public int init(String id, String password);    // with userID, password in database
	public int close();
}
