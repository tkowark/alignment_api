package x.y.z;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.util.Properties;

import javax.jws.WebService;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.AlignmentVisitor;

import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;

@WebService(endpointInterface="eu.sealsproject.omt.ws.matcher.AlignmentWS")
public class MyAlignmentWS extends MyAlignment implements AlignmentWS {

	@Override
	public String align(URI source, URI target) {
		   try {
			   init(source,target);
			   align((Alignment)null, new Properties());
			   SBWriter sbWriter = null;
			   try {
					sbWriter = new SBWriter(new BufferedWriter(new OutputStreamWriter( System.out, "UTF-8" )), true);
					AlignmentVisitor renderer = new RDFRendererVisitor(sbWriter);
					render(renderer);
		                        String alignment = sbWriter.toString();
					return alignment;
				}
			   catch(Exception e) { }
			   
		   } catch (AlignmentException e) {
			 		e.printStackTrace();
		   }
		   return null;
	}

}
