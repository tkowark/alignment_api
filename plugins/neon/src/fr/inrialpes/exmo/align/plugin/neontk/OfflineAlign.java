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

package fr.inrialpes.exmo.align.plugin.neontk;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.util.Vector;
import java.util.Enumeration;

import org.semanticweb.kaon2.api.formatting.OntologyFileFormat;
import org.semanticweb.owl.align.Alignment;
import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.ObjectAlignment;
import fr.inrialpes.exmo.align.impl.URIAlignment;

import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Parameters;

//import com.ontoprise.api.formatting.OntoBrokerOntologyFileFormat;
//import com.ontoprise.config.IConfig;
//import com.ontoprise.config.IConfig.OntologyLanguage;
import com.ontoprise.ontostudio.io.ImportExportControl;

import fr.inrialpes.exmo.align.impl.BasicParameters;
import fr.inrialpes.exmo.align.onto.OntologyCache;
import fr.inrialpes.exmo.align.parser.AlignmentParser;
//import fr.inrialpes.exmo.align.impl.renderer.HTMLRendererVisitor;
import fr.inrialpes.exmo.align.impl.renderer.OWLAxiomsRendererVisitor;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
//import fr.inrialpes.exmo.align.impl.OWLAPIAlignment;

public class OfflineAlign {
	
	File  alignFolder = null;
	File  ontoFolder  = null;
	
	public OfflineAlign(File al, File on) {
		ontoFolder  = on; 
		alignFolder = al;
	}
	
   String matchAndExportAlign (String method, String proj1, String selectedNeOnOnto1, String proj2, String selectedNeOnOnto2) {	 
	  
	  //export ontologies
      ImportExportControl ieControl = new ImportExportControl();
      //Integer name1 = new Integer(AlignView.alignId++);  
	  //Integer name2 = new Integer(AlignView.alignId++);
	  File f1 = new File( selectedNeOnOnto1.replace("file:","") );
	  File f2 = new File( selectedNeOnOnto2.replace("file:","") );
	  
	  //String fname1 =  f1.getName();
	  //String fname2 =  f2.getName();
	  
	  //System.out.println("Filename 1="+ selectedNeOnOnto1);
	  //System.out.println("Filename 2="+ selectedNeOnOnto2);
	  
      Parameters p = new BasicParameters();
      AlignmentProcess A1 = null;
      //String htmlString = null;
      //Vector corrList = new Vector();
      Integer name = new Integer(AlignView.getNewAlignId());
      
      try {
  	
    	  Vector<URI> uris = new Vector<URI>();
    	  uris.add( new URI(selectedNeOnOnto1) );
    	  uris.add( new URI(selectedNeOnOnto2 ) );
	   
		  // Create alignment object
		Object[] mparams = {};
	  	Class alignmentClass = Class.forName(method);
	  	Class[] cparams = {};
	  	java.lang.reflect.Constructor alignmentConstructor = alignmentClass.getConstructor(cparams);
	  	A1 = (AlignmentProcess)alignmentConstructor.newInstance(mparams);
	  	A1.init( (URI)uris.get(0), (URI)uris.get(1), (OntologyCache)null );
	   
	  	A1.align((Alignment)null,p);
	  	
	  	//AlignView.alignObjects.clear();
	  	AlignView.alignmentTable.put( alignFolder.getAbsolutePath() + File.separator + name.toString(), (Alignment)A1 );
	  
	  	
	  	//for saving locally
	  	FileWriter rdfF = new FileWriter(new File( alignFolder.getAbsolutePath() + File.separator + name.toString()+ ".rdf" ));
	  	AlignmentVisitor rdfV = new RDFRendererVisitor(  new PrintWriter ( rdfF )  );
	  	A1.render(rdfV);
	  	rdfF.flush();
	  	rdfF.close();
	  	
	  	//	  for exporting to NeonToolkit
	  	FileWriter owlF    = new FileWriter(new File( ontoFolder.getAbsolutePath() + File.separator + name.toString()+ ".owl" ));
	  	
	  	AlignmentVisitor V = new OWLAxiomsRendererVisitor(  new PrintWriter ( owlF )  );
	  	
	  	//ObjectAlignment al = ObjectAlignment.toObjectAlignment( (URIAlignment)A1 );
		//al.render( V );
	  	A1.render(V);
	  	owlF.flush();
	  	owlF.close();
	
	  
	  } catch ( Exception ex ) { ex.printStackTrace(); };
	  
	  return alignFolder.getAbsolutePath() + File.separator + name.toString();
   }
   
   
   String trimAndExportAlign (Double thres, String id) {	 
		  
	      Integer name = new Integer(AlignView.getNewAlignId());
	      
	      Alignment A1 = AlignView.alignmentTable.get( id );
	      //BasicAlignment clonedA1 = (BasicAlignment)((BasicAlignment)A1).clone();
	      BasicAlignment clonedA1 = null;
	      
	      try {
	    
	      File exFile = new File(id + ".rdf");
				
		  AlignmentParser ap = new AlignmentParser(0);
		  ap.setEmbedded(true);
		  clonedA1 = (BasicAlignment) ap.parse(exFile.toURI().toString());
				
		  File fnRdf = new File( alignFolder.getAbsolutePath() + File.separator + name.toString()+ ".rdf" );
		  if (fnRdf.exists()) fnRdf.delete();
		  
		  FileWriter rdfF = new FileWriter( fnRdf );
		  AlignmentVisitor rdfV = new RDFRendererVisitor(  new PrintWriter ( rdfF )  );
		 
		  clonedA1.render(rdfV);
		  rdfF.flush();
		  rdfF.close();
		  
		  clonedA1.cut(thres);
	      AlignView.alignmentTable.put( alignFolder.getAbsolutePath() + File.separator + name.toString(), clonedA1 );
	         
		  File owlFile = new File( ontoFolder.getAbsolutePath() + File.separator + name.toString()+ ".owl");
		  if (owlFile.exists()) owlFile.delete();
		  
		  FileWriter owlF = new FileWriter( owlFile );
		  
		  AlignmentVisitor owlV = new OWLAxiomsRendererVisitor(  new PrintWriter ( owlF )  );
		  ObjectAlignment al = ObjectAlignment.toObjectAlignment( (URIAlignment)clonedA1 );
		  al.render( owlV );		     		  
		  //clonedA1.render(owlV);
		  owlF.flush();
		  owlF.close();	  
		  } 
		  catch ( Exception ex ) { ex.printStackTrace();};
		  
		  return alignFolder.getAbsolutePath() + File.separator + name.toString();
	   }
   
   public String[] getAllAlign() {
	    
	   if (AlignView.alignmentTable.keys() == null) return null;
	   Vector<String> v = new Vector<String>();
	   
	   for (Enumeration e = AlignView.alignmentTable.keys() ; e.hasMoreElements() ;) {
	       v.add((String)e.nextElement()); 
	   }
	   
	   String[] ls = new String[v.size()];
	   for(int i=0; i< v.size(); i++) ls[i] = v.get(i);
	   
	   return ls;	  
   }
   
   public void getAllAlignFromFiles() {
	    
	   String[] nameL = alignFolder.list();
       Vector<String> v = new Vector<String>();
	   
       for(int i=0; i< nameL.length; i++) 
    	   if(nameL[i].contains(".rdf"))  v.add(nameL[i]);
       
       try {
    	   	 
    	    AlignmentParser parser = new AlignmentParser( 0 );
    	    parser.setEmbedded( true );
    	   	
    	   	for(int i=0; i< v.size(); i++) {
    	   		
    	   		String key = v.get(i).replace(".rdf", "");
    	   		//System.out.println("Path ="+   alignFolder.getAbsolutePath() + File.separator  + v.get(i) );
    	   		AlignView.alignmentTable.put( alignFolder.getAbsolutePath() + File.separator + key , 
    	   				parser.parse(alignFolder.getAbsolutePath() + File.separator  + v.get(i)) );
    	   	}
			   
       } catch ( Exception ex ) { ex.printStackTrace();};
	      
   }
   
   public static String fileToString(File f){
	    String texto = "";
	int i=0;
	try{
	   
	   FileReader rd = new FileReader(f);
	   i = rd.read();
	    
	     while(i!=-1){
	          texto = texto+(char)i;
	          i = rd.read();
	     }
	 
	   }catch(IOException e){
	    System.err.println(e.getMessage());
	     }
	   
	return texto;
	}

}
