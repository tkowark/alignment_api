/*
 * $Id$
 *
 * Copyright (C) INRIA Rh�ne-Alpes, 2007-2008
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

package fr.inrialpes.exmo.align.plugin.webcontent;

import javax.jws.WebService;
import org.weblab_project.core.model.ComposedUnit;
import org.weblab_project.core.model.Document;
import org.weblab_project.core.model.MediaUnit;
import org.weblab_project.services.exception.WebLabException;
import org.weblab_project.core.model.text.Text;
import org.weblab_project.services.ontologyalignment.types.ProcessArgs;
import org.weblab_project.services.ontologyalignment.types.AddResourceArgs;
import org.weblab_project.services.ontologyalignment.types.AddResourceReturn;
import org.weblab_project.services.ontologyalignment.types.ProcessReturn;
import org.weblab_project.services.ontologyalignment.OntologyAlignment;
import org.weblab_project.services.ontologyalignment.ProcessException;
import org.weblab_project.services.ontologyalignment.AddResourceException;
import org.weblab_project.core.model.ontology.Ontology;
import org.weblab_project.core.model.Annotation;

//import fr.inrialpes.exmo.align.plugin.neontk.OnlineAlign;
import java.net.URI;

@WebService(endpointInterface =
"org.weblab_project.services.ontologyalignment.OntologyAlignment")
public class OntologyAlignmentImpl implements OntologyAlignment {

public ProcessReturn process(ProcessArgs args) throws ProcessException {

//System.out.println("Alignment Hello");

String defaultHost = "aserv.inrialpes.fr";
String defaultPort = "80";
String[] methodList = new String[8];

methodList[0] = "fr.inrialpes.exmo.align.impl.method.NameEqAlignment";
methodList[1] = "fr.inrialpes.exmo.align.impl.method.StringDistAlignment";
methodList[2] = "fr.inrialpes.exmo.align.impl.method.SMOANameAlignment";
methodList[3] = "fr.inrialpes.exmo.align.impl.method.SubsDistNameAlignment";
methodList[4] = "fr.inrialpes.exmo.align.impl.method.StrucSubsDistAlignment";
methodList[5] = "fr.inrialpes.exmo.align.impl.method.NameAndPropertyAlignment";
methodList[6] = "fr.inrialpes.exmo.align.impl.method.ClassStructAlignment";
methodList[7] = "fr.inrialpes.exmo.align.impl.method.EditDistNameAlignment";
 
WSInterface onAlign = new WSInterface(defaultPort, defaultHost);

Ontology onto1 = args.getOnto1();
Ontology onto2 = args.getOnto2();
String uri1 = onto1.getUri();
String uri2 = onto2.getUri();
String matchMethod = methodList[7];
String alignURI = null;

if (!uri1.startsWith("http://") || !uri2.startsWith("http://") ) {
	WebLabException ex = new WebLabException();
	ex.setErrorMessage("ERROR : Ontology URI.");
	ex.setErrorId("OntologyAlignment");
	throw new ProcessException("ProcessException : ", ex);
 			   			 
} else {
				   		
	alignURI = onAlign.getAlignId( matchMethod, uri1, uri2  );
	if(alignURI==null || alignURI.equals(""))  {
		WebLabException ex = new WebLabException();
		ex.setErrorMessage("ERROR : Alignment URI.");
		ex.setErrorId("OntologyAlignment");
		throw new ProcessException("ProcessException : ", ex);
	}  
}
						
				 
ProcessReturn out = new ProcessReturn();
Annotation annot = new Annotation(); 
annot.setUri(alignURI);
out.setAnnotation(annot);
 
return out;

}

public AddResourceReturn addResource(AddResourceArgs args) throws AddResourceException {
AddResourceReturn out = new AddResourceReturn();
return out;
}

}
