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

import org.semanticweb.kaon2.api.formatting.OntologyFileFormat;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Parameters;

import com.ontoprise.ontostudio.io.ImportExportControl;

import fr.inrialpes.exmo.align.impl.BasicParameters;
import fr.inrialpes.exmo.align.impl.OntologyCache;
import fr.inrialpes.exmo.align.impl.renderer.HTMLRendererVisitor;
import fr.inrialpes.exmo.align.impl.renderer.OWLAxiomsRendererVisitor;

public class OfflineAlign {
	
   String matchAndExportAlign (String method, String selectedNeOnOnto1, String selectedNeOnOnto2) {	 
	  
	  //export ontologies
      ImportExportControl ieControl = new ImportExportControl();
      Integer name1 = new Integer(SWTInterface.alignId++);  
	  Integer name2 = new Integer(SWTInterface.alignId++);
	  File f1 = new File("onto-" + name1.toString() + ".owl");
	  File f2 = new File("onto-" + name2.toString() + ".owl");
	  String fname1 = "file:" + f1.getAbsolutePath();
	  String fname2 = "file:" + f2.getAbsolutePath();
	  
	  System.out.println("Filename 1="+ fname1);
	  System.out.println("Filename 2="+ fname2);

	  try {
		  //String format1 = DatamodelPlugin.getDefault().getKaon2Connection( <project> ).getParameter(IConfig.ONTOLOGY_LANGUAGE)
		  ieControl.exportFileSystem( OntologyFileFormat.OWLEASY, selectedNeOnOnto1, fname1 );
		  ieControl.exportFileSystem( OntologyFileFormat.OWLEASY, selectedNeOnOnto2, fname2 );
		  
	  } catch  ( Exception ex ) { ex.printStackTrace(); };
	  
      
      Parameters p = new BasicParameters();
      AlignmentProcess A1 = null;
      //String htmlString = null;
      //Vector corrList = new Vector();
      Integer name = new Integer(SWTInterface.alignId++);
      
      try {
  	
    	  Vector<URI> uris = new Vector<URI>();
    	  uris.add( new URI("file:"+ f1.getAbsolutePath()) );
    	  uris.add( new URI("file:"+ f2.getAbsolutePath()) );
	   
		  // Create alignment object
		Object[] mparams = {};
	  	Class alignmentClass = Class.forName(method);
	  	Class[] cparams = {};
	  	java.lang.reflect.Constructor alignmentConstructor = alignmentClass.getConstructor(cparams);
	  	A1 = (AlignmentProcess)alignmentConstructor.newInstance(mparams);
	  	A1.init( (URI)uris.get(0), (URI)uris.get(1), (OntologyCache)null );
	   

	  	A1.align((Alignment)null,p);
	  	
	  	SWTInterface.alignObjects.clear();
	  	SWTInterface.alignObjects.add(A1);
	  
	  	//for exporting to NeonToolkit
	  	
	  
	  	FileWriter owlF = new FileWriter(new File( "align-" + name.toString()+ ".owl" ));
	  	AlignmentVisitor V = new OWLAxiomsRendererVisitor(  new PrintWriter ( owlF )  );
			     
	  	A1.render(V);
	  	owlF.close();
	
	  	
	  	String str1 =  fileToString(new File("align-" + name.toString()+ ".owl") );
		 
		
		//Add URI to OWL file : rethink !!!
	  	File f0 = new File( "align-" + name.toString()+ ".owl" );
	  	String s1 = str1.substring(0, str1.indexOf('>') + 1 );
	  	String s2 = str1.substring(str1.indexOf('>') + 2, str1.length());
		
	  	String[] ss2 = s1.split("xmlns");
	  	String s3 = "<?xml version=\"1.0\"?>\n" + ss2[0] + " ";
     						
	  	s3 = s3 + "xmlns=\"" + "file:" + f0.getAbsolutePath() + "#\"\n ";
	  	s3 = s3 + "xml:base=\"" + "file:" + f0.getAbsolutePath() + "\"\n ";
	  	s3 = s3 + "xmlns:owl=\"http://www.w3.org/2002/07/owl#\"\n " + "xmlns";
		
	  	for(int i=2; i<ss2.length;i++) {
			s3 = s3 + ss2[i];
		  	if(i != ss2.length-1 ) s3 = s3  + "xmlns";
	  	}
		
	  	
		File owlFile = new File( "align-" + name.toString()+ ".owl"  );
		FileWriter out = new FileWriter( owlFile );
		out.write( s3 + s2 );
			//out.write( answer );
		out.close();  
	  	 

	  	
	  	//String[] importedModules = ieControl.importFileSystem(proj, "align-" + name.toString()+ ".owl", null, null);
	  	//ieControl.addOntologies2Project(importedModules, proj);
	    
	  	//for displaying
	  	FileWriter htmlF = new FileWriter( "align.html" );
	  	AlignmentVisitor V1 = new HTMLRendererVisitor(
			    new PrintWriter ( htmlF ) );
	  
	  	A1.render(V1);
	  	htmlF.close();
	  
	  } catch ( Exception ex ) { ex.printStackTrace(); };
	  
	  return "align-" + name.toString()+ ".owl";
   }
   
   
   String trimAndExportAlign (Double thres) {	 
		  
	  
	      //String htmlString = null;
	      //Vector corrList = new Vector();
	      Integer name = new Integer(SWTInterface.alignId++);
	      
	      AlignmentProcess A1 = SWTInterface.alignObjects.get(0);
	      
	      try {
	    	 
	      A1.cut(thres);
	      SWTInterface.alignObjects.clear();
		  SWTInterface.alignObjects.add(A1);
	       
		  	
		  
		  FileWriter owlF = new FileWriter(new File( "align-" + name.toString()+ ".owl" ));
		  AlignmentVisitor V = new OWLAxiomsRendererVisitor(  new PrintWriter ( owlF )  );
				     
		  A1.render(V);
		  owlF.close();
		
		  	
		  String str1 =  fileToString(new File("align-" + name.toString()+ ".owl") );
			 
			
			//Add URI to OWL file : rethink !!!
		  File f0 = new File( "align-" + name.toString()+ ".owl" );
		  String s1 = str1.substring(0, str1.indexOf('>') + 1 );
		  String s2 = str1.substring(str1.indexOf('>') + 2, str1.length());
			
		  String[] ss2 = s1.split("xmlns");
		  String s3 = "<?xml version=\"1.0\"?>\n" + ss2[0] + " ";
	     						
		  s3 = s3 + "xmlns=\"" + "file:" + f0.getAbsolutePath() + "#\"\n ";
		  s3 = s3 + "xml:base=\"" + "file:" + f0.getAbsolutePath() + "\"\n ";
		  s3 = s3 + "xmlns:owl=\"http://www.w3.org/2002/07/owl#\"\n " + "xmlns";
			
		  for(int i=2; i<ss2.length;i++) {
				s3 = s3 + ss2[i];
			  	if(i != ss2.length-1 ) s3 = s3  + "xmlns";
		  	}
			
		  
				File owlFile = new File( "align-" + name.toString()+ ".owl"  );
				FileWriter out = new FileWriter( owlFile );
				out.write( s3 + s2 );
				//out.write( answer );
				out.close();  
		  	
		 
		    
		  	//for displaying
		  FileWriter htmlF = new FileWriter( "align.html" );
		  AlignmentVisitor V1 = new HTMLRendererVisitor(
				    new PrintWriter ( htmlF ) );
		  
		  A1.render(V1);
		  htmlF.close();
		  
		  } 
		  catch ( Exception ex ) { ex.printStackTrace();};
		  
		  return "align-" + name.toString()+ ".owl";
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
	    System.out.println(e.getMessage());
	     }
	   
	return texto;
	}

}
