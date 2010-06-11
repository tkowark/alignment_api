package x.y.z;

import eu.sealsproject.omt.ws.matcher.AlignmentWS;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.util.Properties;

import javax.jws.WebService;

import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.AlignmentVisitor;

import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;

@WebService(endpointInterface="eu.sealsproject.omt.ws.matcher.AlignmentWS")
public class AlignmentWSImpl implements AlignmentWS {

	   public String align(URI source, URI target) {
		  		   
		   AlignmentProcess alignProcess = new MyAlignment();
		   String alignment = null;
		   try {
			    alignProcess.init(source,target);
			    alignProcess.align(null, new Properties());
			    SBWriter sbWriter = null;
				try {
					sbWriter = new SBWriter(new BufferedWriter(new OutputStreamWriter( System.out, "UTF-8" )), true);
				}
				catch(Exception e) { }
				AlignmentVisitor renderer = new RDFRendererVisitor(sbWriter);
				alignProcess.render(renderer);
				return sbWriter.toString();
				
		   } catch (AlignmentException e1) {
	  	            e1.printStackTrace();
		   }
		   return alignment;
	}
}
