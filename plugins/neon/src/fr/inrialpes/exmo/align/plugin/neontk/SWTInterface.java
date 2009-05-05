/*
 * $Id$
 *
 * Copyright (C) INRIA, 2007-2008
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.InputStream;
import java.io.FileInputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionListener;
import java.awt.Frame;

import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.awt.event.ActionEvent;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Toolkit;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.HashMap;

import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JSplitPane;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.ProgressMonitor;
import javax.swing.ProgressMonitorInputStream;
//import javax.swing.JFileChooser;
import javax.swing.JTextArea;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import java.awt.event.MouseAdapter;
//import javax.swing.SwingConstants;
import javax.swing.JScrollPane;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.Timer;
import javax.swing.SwingUtilities;
import org.eclipse.swt.SWT;
 
import java.net.HttpURLConnection;

//import java.net.URL;
import java.net.URLConnection;

 

//import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.Alignment;
//import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.AlignmentVisitor;
//import org.semanticweb.owl.align.Parameters;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

//import org.eclipse.core.resources.ResourcesPlugin;
//import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IPath;
//import org.eclipse.core.resources.IProject;
//import org.semanticweb.kaon2.api.Ontology;
//import org.semanticweb.kaon2.api.KAON2Connection;
import org.semanticweb.kaon2.api.OntologyManager;
//import org.semanticweb.kaon2.api.KAON2Exception;
//import org.semanticweb.kaon2.api.formatting.OntologyFileFormat;
import com.ontoprise.config.IConfig;
import com.ontoprise.ontostudio.datamodel.DatamodelPlugin;
import com.ontoprise.ontostudio.datamodel.DatamodelTypes;
//import com.ontoprise.ontostudio.gui.navigator.module.ModuleTreeElement;
import com.ontoprise.ontostudio.io.ImportExportControl;
//import com.ontoprise.ontostudio.gui.commands.CreateProject;
import com.ontoprise.ontostudio.gui.commands.project.CreateProject;
import com.ontoprise.ontostudio.datamodel.exception.ControlException;
//import com.ontoprise.ontostudio.gui.navigator.project.ProjectControl;
//import org.semanticweb.kaon2.api.formatting.OntologyFileFormat;
//import org.eclipse.core.runtime.IProgressMonitor;
//import com.ontoprise.ontostudio.owl.datamodel.*;
//import fr.inrialpes.exmo.align.onto.OntologyCache;
import fr.inrialpes.exmo.align.onto.OntologyFactory;
import fr.inrialpes.exmo.align.parser.AlignmentParser;
import fr.inrialpes.exmo.align.onto.owlapi10.OWLAPIOntologyFactory;

import org.semanticweb.owl.align.Cell;
//import fr.inrialpes.exmo.align.impl.Ontology;
import fr.inrialpes.exmo.align.impl.BasicCell;
//import org.semanticweb.owl.align.Relation;

import fr.inrialpes.exmo.align.impl.BasicAlignment;
//import fr.inrialpes.exmo.align.impl.BasicParameters;
import fr.inrialpes.exmo.align.impl.ObjectAlignment;
import fr.inrialpes.exmo.align.impl.URIAlignment;
//import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import fr.inrialpes.exmo.align.impl.renderer.HTMLRendererVisitor;
import fr.inrialpes.exmo.align.impl.renderer.OWLAxiomsRendererVisitor;

public class SWTInterface extends JPanel {

    private static final long serialVersionUID = 330;
    
	private static JSplitPane _mainSplitter = new JSplitPane (JSplitPane.VERTICAL_SPLIT);
	
	 
	JLabel hostName, portName, serverName, methodName;//, alignProjLabel;
	
	//JComboBox hostList, portList;
	
	String selectedHost  =  null;
	String selectedPort  =  null;
	String selectedOnto1 =  null;
	String selectedOnto2 =  null;
	
	String selectedLocalAlign =  null;
	static String selectedAlign =  null;
	
	public static Hashtable<String,Alignment>  alignmentTable = new Hashtable<String,Alignment>();
	static String [] forUniqueness = new String[0];
	static int alignId = 0;

	JComponent phrases;
	
    JTextField fileName1, fileName2, hostField, portField, serverField, methodField;  
    SWTInterface frame;
     
    JEditorPane htmlView;
    
    JButton cancelButton, discardButton,  resButton, ontoRefresh,
    		localAlignImportButton, alignUploadButton, 
    		localAlignTrimButton, connButton, goButton, offlineButton, onlineButton;
    
    static JButton alignStoreButton, alignImportButton, serverAlignTrimButton, mapButton, alignFindButton, allAlignButton, fetchButton;
    
    JDialog connDialog, matchDialog; 
    //JPanel pane2;
	Component result = null;
    JComboBox strategy, renderer, localAlignBox, ontoBox1, ontoBox2;
     

    static JComboBox alignBox;
    JLabel strat, render, ontoLabel1, ontoLabel2, alignLabel, localAlignLabel;
     
    //lists obtained from from server
    public String [] methodList = new String[0];
    
    //lists obtained from Neontk
    public String [] ontoList = new String[0];
    public HashMap<String,String> onto_proj = new HashMap<String,String>(0); 
    //public String [] NeOnOntoList = new String[0];
    public static String [] alignIdList = new String[0];
    public String [] localAlignIdList = new String[0];
   
    public Vector	 corrList = new Vector();
    public String [] aservArgs = null;
    public String [] aservArgRetrieve = null;
    public String [] aservArgAlign = null;
    public String [] files = new String[2];
    int ind=0, rend=0, count=0;
	
    boolean online = false;
	//boolean chosenPort = false;
    String defaultHost = "aserv.inrialpes.fr";
    //String defaultPort = "8089";
    String defaultPort = "80";
    String matchMethod = "fr.inrialpes.exmo.align.impl.method.NameEqAlignment";
    String wserver = "http://kameleon.ijs.si/ontolight/ontolight.asmx";
    String wsmethod="";
    //rethink
    
	String alignProject = "AlignmentProject";
	
	public OnlineAlign   onAlign  = null;
	public OfflineAlign  offAlign = null;
	public File ontoFolder = null;
	public File alignFolder = null;
	public static File basicFolder = null;
	 
	 
	public void offlineInit(boolean init){
		//OntologyFactory.setDefaultFactory("fr.inrialpes.exmo.align.onto.owlapi2.OWLAPI2OntologyFactory");
		online = false;
		offAlign = new OfflineAlign( alignFolder, ontoFolder );

		ontoBox1.setEnabled(true);
		ontoBox2.setEnabled(true);
		ontoRefresh.setEnabled(true);
		strategy.setEnabled(true);
		
		mapButton.setEnabled(true);
		
		localAlignBox.setEnabled(true);
		
		onlineButton.setEnabled(true);
		
		localAlignImportButton.setEnabled(true);
		allAlignButton.setEnabled(true);
		
		//threshold.setEnabled(true);
		localAlignTrimButton.setEnabled(true);	 
		serverAlignTrimButton.setEnabled(false);	 
		
		alignFindButton.setEnabled(false);
		
		alignImportButton.setEnabled(false);
		//fetchButton.setEnabled(false);
		alignUploadButton.setEnabled(false);
		
		alignBox.setEnabled(false);
	 
		alignStoreButton.setEnabled(false);
		
		offlineButton.setEnabled(false);
		 
		strategy.removeAllItems();
		alignBox.removeAllItems();
		localAlignBox.removeAllItems();
		ontoBox1.removeAllItems();
		ontoBox2.removeAllItems();
		
		ontoList = new String[0];
		onto_proj = new HashMap<String,String>(0); 
		//NeOnOntoList = new String[0];
		alignIdList = new String[0];
		//localAlignIdList = new String[0];
		
		selectedOnto1 =  null;
		selectedOnto2 =  null;
		selectedAlign =  null;
		//selectedNeOnOnto1 =  null;
		//selectedNeOnOnto2 =  null;
		
		//methodList = new String[9];
		methodList = new String[8];
		methodList[0] = "fr.inrialpes.exmo.align.impl.method.NameEqAlignment";
		strategy.addItem(methodList[0]);
		methodList[1] = "fr.inrialpes.exmo.align.impl.method.StringDistAlignment";
		strategy.addItem(methodList[1]);
		methodList[2] = "fr.inrialpes.exmo.align.impl.method.SMOANameAlignment";
		strategy.addItem(methodList[2]);
		methodList[3] = "fr.inrialpes.exmo.align.impl.method.SubsDistNameAlignment";
		strategy.addItem(methodList[3]);
		methodList[4] = "fr.inrialpes.exmo.align.impl.method.StrucSubsDistAlignment";
		strategy.addItem(methodList[4]);
		methodList[5] = "fr.inrialpes.exmo.align.impl.method.NameAndPropertyAlignment";
		strategy.addItem(methodList[5]);
		methodList[6] = "fr.inrialpes.exmo.align.impl.method.ClassStructAlignment";
		strategy.addItem(methodList[6]);
		methodList[7] = "fr.inrialpes.exmo.align.impl.method.EditDistNameAlignment";
		strategy.addItem(methodList[7]);
		//methodList[8] = "fr.inrialpes.exmo.align.ling.JWNLAlignment";
		//strategy.addItem(methodList[8]);
		
		if(init) offAlign.getAllAlignFromFiles();
		
		String[] list = offAlign.getAllAlign();
		if(list!=null) {
			localAlignIdList = new String[list.length];
			localAlignBox.removeAllItems();
			
			forUniqueness = new String[list.length]; 
			       
			for(int i=0; i< list.length; i++){
				localAlignIdList[i]= list[i];
				localAlignBox.addItem(list[i]);
				File f = new File(list[i]);
				forUniqueness[i] = f.getName();
			}

			if(localAlignIdList.length > 0) { 
				selectedLocalAlign = localAlignIdList[0];
			}
		}
	}
	
	public static int getNewAlignId() {
	   
	   boolean found = false;
	   alignId++;
	   while(!found) {
		   
		   boolean sw = false;
		   for(int i=0;i< forUniqueness.length; i++) 
			   if(forUniqueness[i].equals((new Integer(alignId)).toString())) {
				   alignId++;
				   sw = true;
				   break;
			   }
	   
		   if( !sw ) found = true;
		   
	   }
		   
	   return alignId;
	}
	
	public static void resetAlignList(String alignId) {
		
		alignIdList = new String[1];
		alignIdList[0] = alignId;
		selectedAlign = alignIdList[0];
		if(alignBox.getItemCount() > 0) alignBox.removeAllItems();
		alignBox.addItem(selectedAlign);
		    
		}
	
	public static void resetActionButtons( boolean flag ) {
		
		alignImportButton.setEnabled( flag );
		alignStoreButton.setEnabled( flag );
		serverAlignTrimButton.setEnabled( flag );
		//mapButton.setEnabled( flag );
		//alignFindButton.setEnabled( flag );
		//allAlignButton.setEnabled( flag );
	
		}
	
	public static void set3Buttons( ) { 
		mapButton.setEnabled( true );
		alignFindButton.setEnabled( true );
		allAlignButton.setEnabled( true );
		}
	
	public static void setFetchButton( boolean flag) { 
		fetchButton.setEnabled( flag );
		//alignFindButton.setEnabled( flag );
		//allAlignButton.setEnabled( flag );
		alignImportButton.setEnabled( !flag );
		alignStoreButton.setEnabled( !flag );
		serverAlignTrimButton.setEnabled( !flag );
		}
	
	 
	
    public void initialize() {

	//selectedHost = "aserv.inrialpes.fr";
	//selectedPort = "8089";
    
    IWorkspaceRoot root =  org.eclipse.core.resources.ResourcesPlugin.getWorkspace().getRoot();
    IPath location = root.getLocation();
    String  path = location.toOSString();
    
    htmlView = new JEditorPane();
    htmlView.setEditable(false);
    htmlView.setContentType("text/html");
    htmlView.setBackground( this.getBackground() ); 
    htmlView.setBounds( 0, 0, 500, 500 ); 
    
    //ontoFolder = new File(path + location.SEPARATOR + "onto");
    ontoFolder = new File(path + location.SEPARATOR + "align");
    
    if (!ontoFolder.exists()) ontoFolder.mkdir();
    basicFolder = new File(path + location.SEPARATOR );
    alignFolder = new File(path + location.SEPARATOR + "align");
    if (!alignFolder.exists()) alignFolder.mkdir();
    
    //System.out.println("alignFolder=" + alignFolder.getAbsolutePath());
    
    offlineButton = new JButton("Offline",null);
    offlineButton.addActionListener(new ActionListener(){
    public void actionPerformed(ActionEvent e) {
    		if (e.getSource() == offlineButton) {
    			offlineInit(false);	 
    		}
        };
    	});
    	
    onlineButton = new JButton("Online",null);
    onlineButton.addActionListener(new ActionListener(){
    @SuppressWarnings("deprecation")
	public void actionPerformed(ActionEvent e) {
    		if (e.getSource() == onlineButton) {
    			online = true;
    			
				JOptionPane pane = new JOptionPane();
				connDialog = pane.createDialog(null, "Input for connection");
				connDialog.setSize(new Dimension(300,120));
				
				Container cont = connDialog.getContentPane();
				Component[] comps = cont.getComponents();
				for(int i=0;i< comps.length ;i++) { cont.remove(comps[i]);}
				
				cont.add(connButton);
				cont.add(cancelButton);
				
				JPanel jPane = new JPanel (new GridLayout (3, 2, 20, 10));
				      
			    jPane.add(hostName); jPane.add(hostField);
			    jPane.add(portName); jPane.add(portField);
			    jPane.add(connButton); 	 
			    jPane.add(cancelButton);
			     
				connDialog.setContentPane(jPane);
				connDialog.setVisible(true);
				ontoBox1.removeAllItems();
				ontoBox2.removeAllItems();
    		}
    	
        };
    	});
    	
	selectedHost = defaultHost;
	hostName = new JLabel( "Host name" );
	hostField = new JTextField( defaultHost, 15 );
	hostField.setEnabled(true);
	hostField.setEditable( true );
	
	selectedPort = defaultPort;
	portName = new JLabel( "Port" );
	portField = new JTextField( defaultPort, 4 );
	portField.setEnabled(true);
	portField.setEditable( true );
	
	serverName = new JLabel( "wserver" );
	serverField = new JTextField( wserver , 50 );
	serverField.setEnabled(true);
	serverField.setEditable( true );
	
	methodName = new JLabel( "wsmethod" );
	methodField = new JTextField( "", 50 );
	methodField.setEnabled(true);
	methodField.setEditable( true );
	
	connButton = new JButton("Connect",null);
	connButton.setEnabled(true);

	connButton.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent e) {
        		if (e.getSource() == connButton) {
        			
        			// for connecting to server
        			
        			selectedHost = hostField.getText();
    				selectedPort = portField.getText();
    				//parentComponent = getParent();
    				onAlign = new OnlineAlign(selectedPort, selectedHost );
    			 
        			String list[] = onAlign.getMethods();
        			if(list == null || list.length ==0) { 
				    	JOptionPane.showMessageDialog(null, "Impossible connection!","Warning",2);
        			    return;
        			}
				  
        			methodList = new String[list.length];
        			strategy.removeAllItems();
        							
        			for(int i=0; i< list.length; i++){
        			 
        				methodList[i]= list[i]; 
        				strategy.addItem(list[i]);					
        			}	
        			
        			 
        			ontoBox1.setEnabled(true);
        			ontoBox2.setEnabled(true);
        			strategy.setEnabled(true);
        			ontoRefresh.setEnabled(true);
        			mapButton.setEnabled(true);
        			 
        			alignBox.setEnabled(true);
        			localAlignBox.setEnabled(true);
    				
        			alignImportButton.setEnabled(false);
        			alignStoreButton.setEnabled(false);
        			serverAlignTrimButton.setEnabled(false);
    				//fetchButton.setEnabled(true);
    				
    				localAlignImportButton.setEnabled(true);
    				alignUploadButton.setEnabled(true);
    				alignFindButton.setEnabled(true);
    				allAlignButton.setEnabled(true);
    				
    				offlineButton.setEnabled(true);
    				onlineButton.setEnabled(false);
    				
    				localAlignTrimButton.setEnabled(true);
  				
    				ontoRefresh.setEnabled(true);
    				 
    				mapButton.setEnabled(true);
    				strategy.setEnabled(true);
    				
    				online = true;
    				
        			connDialog.dispose();			
        			
				    } 
            	
		};
        });
	

	cancelButton = new JButton("Cancel",null);
	cancelButton.setEnabled(true);
	cancelButton.addActionListener(new ActionListener(){
	    public void actionPerformed(ActionEvent e) {
	    		if (e.getSource() == cancelButton) {
	    			//System.out.println("Cancel");
	    			connDialog.dispose();
	    			
	    		}
	    };
	    });
	
	discardButton = new JButton("Discard",null);
	discardButton.setEnabled(true);
	discardButton.addActionListener(new ActionListener(){
	    public void actionPerformed(ActionEvent e) {
	    		if (e.getSource() == discardButton) {
	    			//System.out.println("Cancel");
	    			matchDialog.dispose();
	    			
	    		}
	    };
	    });
	
	alignLabel  = new JLabel("Server alignments");
	alignBox    = new JComboBox(alignIdList);
	alignBox.setEnabled(false);
	alignBox  = new JComboBox(alignIdList);
    alignBox.setEnabled(false);
    
    alignBox.setMaximumRowCount(20);
    alignBox.addItemListener(new ItemListener() {
		public void itemStateChanged(ItemEvent event)
		{
	      int id = 0;
		  if (event.getStateChange()==ItemEvent.SELECTED)
			id = alignBox.getSelectedIndex();
		    
		    selectedAlign =  alignIdList[id];
		    
		    //System.out.println("Align selected =" + selectedAlign);
		}
	});
    
    localAlignLabel  = new JLabel("Local alignments  ");
	localAlignBox    = new JComboBox(localAlignIdList);
	localAlignBox.setEnabled(false);
	localAlignBox    = new JComboBox(localAlignIdList);
    localAlignBox.setEnabled(false);
    
    localAlignBox.setMaximumRowCount(20);
    localAlignBox.addItemListener(new ItemListener() {
		public void itemStateChanged(ItemEvent event)
		{
	      int id = 0;
		  if (event.getStateChange()==ItemEvent.SELECTED)
			id = localAlignBox.getSelectedIndex();
		    
		    selectedLocalAlign =  localAlignIdList[id];
		}
	});
    
    localAlignImportButton = new JButton("Export",null);
	localAlignImportButton.setEnabled(false);
	localAlignImportButton.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent e) {
        		if (e.getSource() == localAlignImportButton) {
        			if(selectedLocalAlign == null) {
				    		JOptionPane.showMessageDialog(null, "Choose an alignment ID from list!","Warning",2);
				    		return;
        			}
				        
	        		File fn = new File(selectedLocalAlign + ".owl");
	        		//if (fn.exists()) fn.delete();
	        		//System.out.println("Filename off to export :"+ fn.getAbsolutePath());
	        		
	        		StringWriter htmlMessage = new StringWriter();
					AlignmentVisitor htmlV = new HTMLRendererVisitor(  new PrintWriter ( htmlMessage )  );
					
					try {
						SWTInterface.alignmentTable.get(selectedLocalAlign).render( htmlV );
					
						htmlView.setText( htmlMessage.toString() );
			        
						htmlView.setLocation( new Point( 0, 0 ) );
				       
						htmlView.setVisible(true);
					
		 				String inputName=null;
	        				        					
	        			inputName = JOptionPane.showInputDialog(null, "Enter a project name", "AlignmentProject");
	        					
	        			if (inputName==null || inputName.equals("")) return;
	    						
	    				String[] projects = DatamodelPlugin.getDefault().getOntologyProjects();
	    	    		if(projects!=null) {
	    	    		boolean found = false;	
	    	    				
	    	    					for(int i=0; i < projects.length; i++) {
	    	    					 
	    	    						if(projects[i].equals(inputName)) { 
	    	    						found = true;break;
	    	    						}
	    	    					}
	    	    				
	    	    					if(!found) {
	    	    						Properties proper = new Properties();
	    	    						proper.put(IConfig.ONTOLOGY_LANGUAGE.toString(), IConfig.OntologyLanguage.OWL.toString());
	    	    						new CreateProject(	inputName, DatamodelTypes.RAM, proper ).run();
	            				
	    	    					}
	    					 
	    	    				ImportExportControl ieControl = new ImportExportControl();	
	    	    				URI uris[] = new URI[1];
	    	    				uris[0] = new File(fn.getAbsolutePath()).toURI();
	    	    				ieControl.importFileSystem(inputName, uris, null);
	    	    				//ieControl.addOntologies2Project(importedModules, inputName);
	    	    				}
	        			}
						catch ( Exception ex ) { ex.printStackTrace();};
	        			
					 }//choose an align
				    	
        		
		}
		});
	
	alignUploadButton = new JButton("Upload",null);
	alignUploadButton.setEnabled(false);
	alignUploadButton.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent e) {
        		if (e.getSource() == alignUploadButton) {
        			if(selectedLocalAlign == null) 
				    	JOptionPane.showMessageDialog(null, "Choose an alignment ID from list!","Warning",2);
				    else {      
				    	
		        		//File fn = new File( selectedLocalAlign + ".rdf" );
		        		//System.out.println("file name off to export :"+ fn.getName());
		        		
		        		try {
		        			String uploaded = onAlign.uploadAlign(selectedLocalAlign + ".rdf");
		        			
		        			localAlignIdList = new String[1];
		        			localAlignIdList[0] = selectedLocalAlign;
        					localAlignBox.removeAllItems();
        					localAlignBox.addItem(selectedLocalAlign);	 
        					
        					JOptionPane.showMessageDialog(null, "Uploaded alignment : "+ uploaded,"Warning",2);
		        			
		        		} catch ( Exception ex ) { ex.printStackTrace();}
				    }
        		}
		}
		});
		
	fetchButton = new JButton("Fetch",null);
	fetchButton.setEnabled(false);
	fetchButton.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent e) {
				
			    resetActionButtons( false );
    				
        		if (e.getSource() == fetchButton) {
        			
        			if(selectedAlign == null) { 
				    	JOptionPane.showMessageDialog(null, "Choose an alignment ID from list!","Warning",2);
    					return;
        			}
        			/*
        			alignIdList = new String[1];
					alignIdList[0] = selectedAlign;
					alignBox.removeAllItems();
					alignBox.addItem(selectedAlign);
					onAlign.getRDFAlignment( selectedAlign );
        			*/
        			
        			JOptionPane.showMessageDialog(null, "This operation may take a while.","Warning",2);
        			alignIdList = new String[1];
					alignIdList[0] = selectedAlign;
					alignBox.removeAllItems();
					alignBox.addItem(selectedAlign);
					onAlign.getRDFAlignmentMonoThread( selectedAlign );
					setFetchButton( false );
					
        		} 
	};
    });
        			
	alignImportButton = new JButton("Export",null);
	alignImportButton.setEnabled(false);
	alignImportButton.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent e) {
         		if (e.getSource() == alignImportButton) {
        			
        			if(selectedAlign == null) { 
				    	JOptionPane.showMessageDialog(null, "Choose an alignment ID from list!","Warning",2);
    					return;
        			}
				    //the name originated from the server is a URL so slashes are used 
				    //String []  sali = selectedAlign.split("/");
        			//String uniqueId = sali[sali.length-2].concat(sali[sali.length-1]);
        		    
        			String inputName = null;
        			
        			FileWriter out = null;
					 
					//export to local List
							
					String rdfalignStr = onAlign.getRDFAlignmentParsed( );// selectedAlign , alignImportButton, alignStoreButton, 
							                                            //serverAlignTrimButton, allAlignButton, alignFindButton, mapButton );
					 	
					String alignKey =  alignFolder.getAbsolutePath() + File.separator + getNewAlignId();
					
					alignIdList = new String[1];
					alignIdList[0] = selectedAlign;
	        	    alignBox.removeAllItems();
	        		alignBox.addItem( alignIdList[0] );
	        		
					String rdfPath =  alignKey + ".rdf";
					
					URIAlignment align = null;
					String owlalignStr = null;
					
					try {
						
						File rdfFile = new File( rdfPath );
				
						out = new FileWriter( rdfFile );
						out.write( rdfalignStr );
						out.flush();
						out.close();
						
						File file = new File(rdfPath);
						
						AlignmentParser ap = new AlignmentParser(0);
						ap.setEmbedded(true);
						align = (URIAlignment)ap.parse(file.toURI().toString());
						
						SWTInterface.alignmentTable.put( alignKey , (Alignment)align );
					
						String[] list  = offAlign.getAllAlign( );
						
						if(list != null) {
							localAlignIdList = new String[1];
							localAlignBox.removeAllItems();
							
							for(int i=0; i< list.length; i++){
							if(list[i].equals(alignKey)) {
								localAlignIdList[0]= list[i];
								localAlignBox.addItem(list[i]);
								break;
							}
						}
			
						if(localAlignIdList.length > 0) { 
							selectedLocalAlign = localAlignIdList[0];
						}
						}
						
						StringWriter htmlMessage = new StringWriter();
						AlignmentVisitor htmlV = new HTMLRendererVisitor(  new PrintWriter ( htmlMessage )  );
						 
						align.render( htmlV );
						
						if(htmlMessage.toString()==null || htmlMessage.toString().equals("")) {
							htmlView.setText( "" );
						} else {
							 
							//String htmlText = htmlMessage.toString();
							//if(htmlText.length() > 1000000)
							//	JOptionPane.showMessageDialog(null, "Too big alignment!","Warning",2);
							//htmlView.getEditorKit().createDefaultDocument();	
							htmlView.setText( htmlMessage.toString()  );
						}
					
						htmlView.setLocation( new Point( 0, 0 ) );
				       
						htmlView.setVisible(true);
						//get align from server, then  export it as owl onto
 						StringWriter owlMessage = new StringWriter();
						AlignmentVisitor owlV = new OWLAxiomsRendererVisitor(  new PrintWriter ( owlMessage )  );
						ObjectAlignment al = ObjectAlignment.toObjectAlignment( (URIAlignment)align );
						//BasicAlignment al =  (BasicAlignment)align;
						al.render( owlV );
			
						owlalignStr = owlMessage.toString();
						 
					}
					catch ( Exception ex ) { ex.printStackTrace();};
					
						
					if(owlalignStr==null)  {
						JOptionPane.showMessageDialog(null, "OWL alignment cannot be exported.","Warning",2);
						return;
					}
					
					try {
						 	
						inputName = JOptionPane.showInputDialog(null, "Enter a project name", "AlignmentProject");
    					
    					if (inputName==null || inputName.equals("")) return;
                                
						String[] projects = DatamodelPlugin.getDefault().getOntologyProjects();
	    				if(projects!=null) {
	    				boolean found = false;	
	    				
	    				for(int i=0; i < projects.length; i++) {
	    					 
	    					if(projects[i].equals(inputName)) { 
	    						found = true;break;
	    					}
	    				}
	    				
	    				if(!found) {
	    					 
	    					Properties proper = new Properties();
    						proper.put(IConfig.ONTOLOGY_LANGUAGE.toString(), IConfig.OntologyLanguage.OWL.toString());
    						new CreateProject(	inputName, DatamodelTypes.RAM, proper ).run();
	    				}
	    			    }
	    				
	    				String owlPath =  ontoFolder.getAbsolutePath() + File.separator + getNewAlignId() + ".owl";
						File owlFile = new File( owlPath );
						if (owlFile.exists()) owlFile.delete();
						
						out = new FileWriter( owlFile );
						out.write( owlalignStr );
					    out.flush();
						out.close();
						 
						try {
							ImportExportControl ieControl = new ImportExportControl();
							URI uris[] = new URI[1];
    	    				uris[0] = new File( owlPath).toURI();
							String[] importedModules = ieControl.importFileSystem(inputName, uris,  null);
					
							//ieControl.addOntologies2Project(importedModules, inputName);
						} catch (  ControlException ex ) { }
							 
					} 
					catch ( Exception ex ) { ex.printStackTrace();}
					 
        		}
		};
        });
	
	allAlignButton = new JButton("Fetch available alignments", null);
	allAlignButton.setEnabled(false);
	allAlignButton.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent e) {
        		if (e.getSource() == allAlignButton) {
        			
        			// for connecting to server
        			//onAlign = new OnlineAlign(selectedPort, selectedHost);
        			String[] list = null;
        			if(online) {
        				list = onAlign.getAllAlign();
        				if(list == null || list.length==0) {
        					JOptionPane.showMessageDialog(null, "Impossible connection!","Warning",2);
        					return;
        				}
        				   
        			//String[] list = getResultsFromAnswer( aa , "alid", null ); 
        			
        			alignIdList = new String[list.length];
        			alignBox.removeAllItems();
        							
        			for(int i=0; i< list.length; i++){
        						alignIdList[i]= list[i];
        						alignBox.addItem(list[i]);
        					}
        			
        					if(alignIdList.length > 0) { 
        						selectedAlign = alignIdList[0];
        					}
        	 			
        			String[] list1  = offAlign.getAllAlign( );
        			if(list1!=null) {
        					localAlignIdList = new String[list1.length];
    						localAlignBox.removeAllItems();
    							
    						for(int i=0; i< list1.length; i++){
    							localAlignIdList[i]= list1[i];
    							localAlignBox.addItem(list1[i]);
    						}
    			
    						if(localAlignIdList.length > 0) { 
    							selectedLocalAlign = localAlignIdList[0];
    						}
        			}
        			
        			fetchButton.setEnabled(true);
        			alignImportButton.setEnabled(false); 
        			alignStoreButton.setEnabled(false); 
        			serverAlignTrimButton.setEnabled(false);
         			
        			} 
        			else { //offline
        				list  = offAlign.getAllAlign( );
        				if(list!=null) {
        					localAlignIdList = new String[list.length];
    						localAlignBox.removeAllItems();
    							
    						for(int i=0; i< list.length; i++){
    							localAlignIdList[i]= list[i];
    							localAlignBox.addItem(list[i]);
    						}
    			
    						if(localAlignIdList.length > 0) { 
    							selectedLocalAlign = localAlignIdList[0];
    						}
        				}
        			}
        		}		
		};
        });
	
	alignFindButton = new JButton("Find an alignment for ontologies", null);
	alignFindButton.setEnabled(false);
	alignFindButton.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent e) {
        		if (e.getSource() == alignFindButton) {
        			
        			// for connecting to server
        			//onAlign = new OnlineAlign(selectedPort, selectedHost);
        			if(selectedOnto1 == null || selectedOnto2 == null ) 
					    	JOptionPane.showMessageDialog(null, "Choose two ontologies from lists!","Warning",2);
					else {      
					
					String[] list =  onAlign.findAlignForOntos(selectedOnto1, selectedOnto2);
	        		if(list == null || list.length==0) 
					    	JOptionPane.showMessageDialog(null, "No alignment is found!","Warning",2);
					else {  
        			//String[] list = getResultsFromAnswer(ao, "alid", null ); 
        			
        			alignIdList = new String[list.length];
        			alignBox.removeAllItems();
        							
        			for(int i=0; i< list.length; i++){
        				alignIdList[i]= list[i];
        				alignBox.addItem(list[i]);
        			}	
        			
        			if(alignIdList.length > 0) { 
        				selectedAlign = alignIdList[0];
        			}
        			
					}
					}
        			fetchButton.setEnabled(true);
        			alignImportButton.setEnabled(false); 
        			alignStoreButton.setEnabled(false); 
        			serverAlignTrimButton.setEnabled(false);
            	}
			
		};
        });
	
	serverAlignTrimButton = new JButton("Trim", null);
	serverAlignTrimButton.setEnabled(false);
	serverAlignTrimButton.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent e) {
        		if (e.getSource() == serverAlignTrimButton) {
        			
    					if(selectedAlign == null ) 
    				    	JOptionPane.showMessageDialog(null, "Choose an alignment ID from list!","Warning",2);
        				else {	   
        					String thres = null;
        					 
        					thres = JOptionPane.showInputDialog(null, "Enter a threshold ", "0.5");
        					
        					if (thres==null || thres.equals("")) return;
        					
    						String at = onAlign.trimAlign(selectedAlign, thres);
    						
    		        		if(at == null || at.equals("")) 
    						    	JOptionPane.showMessageDialog(null, "No alignment is obtained!","Warning",2);
    						else {  
  
    						alignIdList = new String[1];
        					alignBox.removeAllItems();
        					alignIdList[0]= at;
    						alignBox.addItem(at);              
    						selectedAlign = alignIdList[0]; 	
    						setFetchButton( true );
        					 
    						}
        				}
        		}
		};
        });
	
	localAlignTrimButton = new JButton("Trim", null);
	localAlignTrimButton.setEnabled(false);
	localAlignTrimButton.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent e) {
        		if (e.getSource() == localAlignTrimButton) {
        			
    					if(selectedLocalAlign == null ) 
    				    	JOptionPane.showMessageDialog(null, "Choose an alignment ID from list!","Warning",2);
        				else {
        					String thres = JOptionPane.showInputDialog(null, "Enter a threshold ", "0.5");
        					 
        					if (thres==null || thres.equals("")) return;
        					
    						String resId  = offAlign.trimAndExportAlign( new Double(thres), selectedLocalAlign );
    						
    						localAlignBox.removeAllItems();
    						localAlignIdList = new String[1];
    						
    						localAlignIdList[0] = resId;
    						
    	        			localAlignBox.addItem( resId );
    	        			 
    	        			selectedLocalAlign = localAlignIdList[0];
    					}
            	 
        		}
		};
    });
	
	//store an alignment on server or locally
	alignStoreButton = new JButton("Store", null);
	alignStoreButton.setEnabled(false);
	alignStoreButton.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent e) {
        		if (e.getSource() == alignStoreButton) {
        			
        			//for connecting to server
        			//onAlign = new OnlineAlign(selectedPort, selectedHost);
        			if(selectedAlign == null ) 
					    	JOptionPane.showMessageDialog(null, "Choose an alignment ID from list!","Warning",2);
					else {      
						
					String sto = onAlign.storeAlign(selectedAlign);  
					
		        	if(sto == null || sto.equals("") ) 
						    	JOptionPane.showMessageDialog(null, "Impossible connection!","Warning",2);
		        	else {
		        		alignIdList = new String[1];
    					alignBox.removeAllItems();
    							
    					alignIdList[0]= sto;
						alignBox.addItem(sto);
						                      
						selectedAlign = alignIdList[0]; 
						JOptionPane.showMessageDialog(null, "Stored alignment : "+ sto ,"Warning",2);
		        	}
		        	
		        	
					}
            	}
			
		};
        });
	
	ontoRefresh = new JButton("Refresh",null);
	ontoRefresh.setEnabled(false);
	ontoRefresh.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent e) {
        		if (e.getSource() == ontoRefresh) {
            		onto_proj = refreshOntoList(online);
			}
		};
        });
	
	goButton = new JButton("Accept",null);	
	goButton.setEnabled( true );
	goButton.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent e) {
		   if (e.getSource() == goButton) {
			   wserver  = serverField.getText();
			   if(wserver.equals(""))  
				   wserver = null;
			   wsmethod = methodField.getText();
			   if(wsmethod.equals("")) 
				   wsmethod = null;
			   matchDialog.dispose();
			   
			   resetActionButtons( false );
   			   
			   //onAlign.getAlignId( matchMethod, wserver, wsmethod, selectedOnto1, selectedOnto2 );
			   
			   String alignId = onAlign.getAlignIdMonoThread( matchMethod, wserver, wsmethod, selectedOnto1, selectedOnto2 );
			   //System.out.println("match from sji  finished ");
			   alignIdList = new String[1];
			   alignIdList[0] = alignId;
			   selectedAlign = alignIdList[0];
			   alignBox.removeAllItems();
			   alignBox.addItem(selectedAlign);
			   setFetchButton( true );
			    
		   } // getsource
		};
	});
		   
		   
	mapButton = new JButton("Match",null);	
	mapButton.setEnabled(false);
	mapButton.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent e) {
		   if (e.getSource() == mapButton) {
		       if (selectedOnto1 == null  || selectedOnto2 == null ) {
		       		JOptionPane.showMessageDialog(null, "Choose two ontologies from lists ","Warning",2);
		       		return;
		       }
		       
			   
			   if(online) {
				   		if (!selectedOnto1.startsWith("http://") || !selectedOnto2.startsWith("http://") ) {
				   			JOptionPane.showMessageDialog(null, "URLs for ontologies are required.  ","Warning",2);
				   			return;
				   		}
				   		if( matchMethod.equals("fr.inrialpes.exmo.align.service.WSAlignment")) {
				   			JOptionPane pa = new JOptionPane();
				   			matchDialog = pa.createDialog(null, "Parameters for matching");
				   			matchDialog.setSize(new Dimension(400,120));
	    				
				   			Container cont = matchDialog.getContentPane();
				   			Component[] comps = cont.getComponents();
				   			for(int i=0;i< comps.length ;i++) { cont.remove(comps[i]);}
				   			cont.add(goButton);
				   			cont.add(discardButton);
	    				
				   			JPanel jPane = new JPanel (new GridLayout (3, 2, 20, 10));
	    				      
				   			jPane.add(serverName); jPane.add(serverField);
				   			jPane.add(methodName); jPane.add(methodField);
				   			jPane.add(goButton); 	 
				   			jPane.add(discardButton);
	    			     
				   			matchDialog.setContentPane(jPane);
				   			matchDialog.setVisible(true);
				   		} else {
				   			resetActionButtons( false );
				   			String alignId = onAlign.getAlignIdMonoThread( matchMethod, wserver, wsmethod, selectedOnto1, selectedOnto2 );
							//onAlign.getAlignId( matchMethod, wserver, wsmethod, selectedOnto1, selectedOnto2 );
				   			alignIdList = new String[1];
							alignIdList[0] = alignId;
							selectedAlign = alignIdList[0];
							alignBox.removeAllItems();
							alignBox.addItem(selectedAlign);
							setFetchButton( true );
				   		}
				   		
					    //String answer = onAlign.getAlignId( matchMethod, selectedOnto1, selectedOnto2  );
						//if(answer==null || answer.equals(""))  {
						//	JOptionPane.showMessageDialog(null, "Alignment is not produced.","Warning",2);
						//	return;
						//}
						//alignIdList = new String[1];
						//alignIdList[0] = answer;
						//selectedAlign = alignIdList[0];
						//alignBox.removeAllItems();
						//alignBox.addItem(selectedAlign);
						//onAlign.getRDFAlignment( selectedAlign, alignImportButton, alignStoreButton, 
						//		                 serverAlignTrimButton, allAlignButton, alignFindButton, mapButton);
						 		 
				  }
				  else { //offline
						
					String resId  = offAlign.matchAndExportAlign( matchMethod, onto_proj.get(selectedOnto1), selectedOnto1, onto_proj.get(selectedOnto2), selectedOnto2);
					
					localAlignBox.removeAllItems();
					localAlignIdList = new String[1];
					
					//File f1 = new File(resId);
					localAlignIdList[0] =  resId;//"file:" + f1.getAbsolutePath();
					  
        			localAlignBox.addItem(localAlignIdList[0]);
        			 
        			selectedLocalAlign = localAlignIdList[0];
        			
        			//System.out.println("offline matching done. ");
        		    
        			 
				   } //else for ""offline"
			       } // getsource
		    
		};
	});
	
    //retrieve all available onto. in Ontology Navigator
    
    ontoLabel1 = new JLabel("Ontology 1           ");
    
    ontoBox1  = new JComboBox(ontoList);
    ontoBox1.setEditable( true );
    ontoBox1.setEnabled(false);
    ontoBox1.setMaximumRowCount(20);
    ontoBox1.addItemListener(new ItemListener() {
		public void itemStateChanged(ItemEvent event) {
		  if (event.getStateChange()==ItemEvent.SELECTED)
			selectedOnto1 = (String)ontoBox1.getSelectedItem();
		    
		}
	});
     
    ontoLabel2 = new JLabel("Ontology 2           "); 
    ontoBox2  = new JComboBox(ontoList);
    ontoBox2.setEditable( true );
    ontoBox2.setEnabled(false);
    ontoBox2.setMaximumRowCount(20);
    ontoBox2.addItemListener(new ItemListener() {
		public void itemStateChanged(ItemEvent event)
		{
		  if (event.getStateChange()==ItemEvent.SELECTED)
			//id = ontoBox2.getSelectedIndex();
			selectedOnto2 = (String)ontoBox2.getSelectedItem();   
		}
	});
    
	strat= new JLabel("Methods               ");
	strategy = new JComboBox(methodList);
	strategy.setEnabled(false);
	strategy.setMaximumRowCount(20);
	strategy.addItemListener(new ItemListener() {
		public void itemStateChanged(ItemEvent event)
		{
		  if (event.getStateChange()==ItemEvent.SELECTED)
			ind = strategy.getSelectedIndex();
		    matchMethod =  methodList[ind];
		}
	});
	
    this.setLayout (new BorderLayout());
    this.add (new JLabel ("Computing and managing ontology alignments"), BorderLayout.NORTH);
	
    phrases = createPhraseList( );
    phrases.setBorder(BorderFactory.createEmptyBorder (10, 5, 10, 5)  );
    
    JScrollPane top = new JScrollPane( phrases );
    top.setMaximumSize(top.getPreferredSize());
    _mainSplitter.setTopComponent( top );
    
    JScrollPane html = new JScrollPane( htmlView );
    html.setMaximumSize(html.getPreferredSize());
	_mainSplitter.setBottomComponent( html );
	
	_mainSplitter.setDividerLocation((int)top.getPreferredSize().getHeight());
	
	this.add(_mainSplitter,BorderLayout.CENTER); //Main Window of the Plugin
	
	//offline is default mode
	offlineInit( true );
	
   }
 
private JPanel createPhraseList ( ) {
    JPanel phrasePane = new JPanel (new GridLayout (0, 1, 0, 10));
      
     
    JPanel minusLabel 	= new JPanel (new FlowLayout(10));
    JPanel zeroLabel 	= new JPanel (new FlowLayout(10));
    JPanel oneLabel 	= new JPanel (new FlowLayout(10));
    JPanel twoLabel 	= new JPanel (new FlowLayout(10));
	JPanel threeLabel 	= new JPanel (new FlowLayout(10));
	JPanel fourLabel 	= new JPanel (new FlowLayout(10));
	JPanel four2Label 	= new JPanel (new FlowLayout(10));
	JPanel fiveLabel 	= new JPanel (new FlowLayout(10));
	JPanel sixLabel 	= new JPanel (new FlowLayout(10));
	JPanel eightLabel 	= new JPanel (new FlowLayout(10));
	JPanel nineLabel 	= new JPanel (new FlowLayout(10));

	minusLabel.add(onlineButton);
	minusLabel.add(offlineButton);
	 
	oneLabel.add(ontoLabel1);
	oneLabel.add(ontoBox1);
     
	 
	twoLabel.add(ontoLabel2);
	twoLabel.add(ontoBox2);
	
	twoLabel.add(ontoRefresh);
	
 	threeLabel.add(strat);
	threeLabel.add(strategy);
 	
	threeLabel.add(mapButton);
	
	fourLabel.add(alignLabel);
	fourLabel.add(alignBox);
	//fourLabel.add(fetchButton);
	fourLabel.add(alignImportButton);
	fourLabel.add(serverAlignTrimButton);
	fourLabel.add(alignStoreButton);
	fourLabel.add(fetchButton);
	
	four2Label.add(localAlignLabel);
	four2Label.add(localAlignBox);
	four2Label.add(localAlignImportButton);
	four2Label.add(localAlignTrimButton);
	four2Label.add(alignUploadButton);

	fiveLabel.add(alignFindButton);
	nineLabel.add(allAlignButton);
	//label_three.add(resButton);
	phrasePane.add(minusLabel);
 	//phrasePane.add(zeroLabel);
	
 	phrasePane.add(oneLabel);
	phrasePane.add(twoLabel);
	phrasePane.add(threeLabel);
	phrasePane.add(fourLabel);
	phrasePane.add(four2Label);
	phrasePane.add(nineLabel);
	phrasePane.add(fiveLabel);
	
    return phrasePane;
    
   }
 
//This function fetches all URL ontologies
private HashMap<String,String> refreshOntoList(boolean online) {
	HashMap<String,String>  vec = new HashMap<String,String>();
	OWLAPIOntologyFactory fact = new OWLAPIOntologyFactory();
	try {
		String[] projects = DatamodelPlugin.getDefault().getOntologyProjects();
		if(projects != null) {
		for(int i=0; i < projects.length; i++) {	 
			if(projects[i]!=null) {  
					//URI[] uris=  DatamodelPlugin.getDefault().getProjectOntologyFiles(projects[i]);
					//Kaon2Connection connection = DatamodelPlugin.getDefault().getKaon2Connection(projects[i]);
					//version 15 May
					OntologyManager connection = DatamodelPlugin.getDefault().getKaon2Connection(projects[i]);
					Set<String> strSet = connection.getAvailableOntologyURIs();
					String[] uris = (String[])strSet.toArray(new String[0]);
					if(online) {
						for(int k=0; k < uris.length; k++) {
							//get only http URL
							if(uris[k].startsWith("http://"))
							try {
								fact.loadOntology(new URI(uris[k]));
								vec.put(uris[k],projects[i]);
							} catch (Exception ex) {
							}
						}
					} else {
						for(int k=0; k < uris.length; k++) {
							vec.put(DatamodelPlugin.getDefault().getPhysicalOntologyUri(projects[i], uris[k]).toString(),projects[i]);
						}
					}
			}
		}
		} 
		else {
			//System.out.printf("No Ontology Project !" );
			return null;
		}
	} catch ( Exception ex ) { ex.printStackTrace();};
	
	ontoBox1.removeAllItems();
	ontoBox2.removeAllItems();
	
	String[] keys = (String[]) vec.keySet().toArray(new String[0]);
	
	ontoList = new String[ keys.length ];
	
	for(int j=0; j < keys.length; j++) {
		 
		ontoBox1.addItem(keys[j]);
		ontoBox2.addItem(keys[j]);
		ontoList[j] = keys[j];
		
		if(ontoList.length > 0) { 
			selectedOnto1 = ontoList[0];
			selectedOnto2 = ontoList[0];
		}
		
	}
	return vec;	 
}

 public void run() {		
		    
		initialize();
	}
 
 
 public static String[] getResultsFromAnswer( String answer, String type , String separator) {
 	Document doc=null;
 	String[] names=null;
 	//File message=null;
 	//System.out.println( "displaying XML ..." );
 	answer =   "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + answer;
 	//System.out.println( answer );
 	try {
 	File messageFile = new File(basicFolder.getAbsolutePath() + basicFolder.separator + "messageFile");
 	FileWriter out = new FileWriter(messageFile);
 	out.write( answer );
 	out.close();
 	
 	} catch (Exception e) {
         //compilerError(Compiler.XML, e.getMessage());
     }
 	
 	try {
 		//System.out.println( "Parsing for getting result: "+ type +" ... ");
 		doc = parse(new File(basicFolder.getAbsolutePath() + basicFolder.separator + "messageFile"));
 		
 		Element e = doc.getDocumentElement();
 		    
 		NodeList methods = e.getElementsByTagName(type);
 		names = new String[methods.getLength()];
 		//System.out.println( "length="+ methods.getLength());
 		
 	 
 		    for (int i=0; i< methods.getLength(); i++) {
 		      Node method = (Node) methods.item(i);
 		      //String nm = method.getAttribute("NAME");
 		      Node firstnode = method.getFirstChild();
   		      String nm = firstnode.getNodeValue();
 		      //System.out.println( "nm="+nm );
 		      if(nm!=null) { 
 		      if(separator!=null) 
 		    	 names[i]=nm.substring(nm.lastIndexOf(separator)+1,nm.length());
 		      else 
 		    	 names[i]=nm;
 		      //System.out.println( "name="+names[i] );
 		      }
 		    }
 		
 		 
 	
 	} catch (Exception e) {
         //compilerError(Compiler.XML, e.getMessage());
 		//System.out.println("getValue="+e.getMessage());
     }
 	return names;
 //System.out.println( answer );
 }
 
 public static Vector<String[]> getCorresFromAnswer( Alignment align ) {
	 
	 ArrayList<Cell> cells =  ((BasicAlignment)align).getArrayElements();
	 Vector<String[]> names = new Vector<String[]>(cells.size() + 1 );
	 
	 String[] corr = new String[4];
	 corr[0] = "Object1";
	  
	 corr[1] = "Relation";
	 corr[2] = "Strength";
	 corr[3] = "Object2";
	 
	 names.add(corr);
	 
	 for(int i=0; i<cells.size(); i++) {
		 try {
		  
			 corr = new String[4];
		 
			 corr[0] = ((BasicCell)cells.get(i)).getObject1().toString();
			 //by default : equivalence
			 
			 //corr[1] = cells.get(i).getRelation().toString();
			 corr[1] = "=";
			 corr[2] = (new Double(cells.get(i).getStrength())).toString();
			 corr[3] = ((BasicCell)cells.get(i)).getObject2().toString();
			 names.add(corr);
			 
		 }catch (Exception ex) {  ex.printStackTrace();  }
		 
	 }
	 return names;
	 
 }
 
 public static Vector<String[]> getCorresFromAnswer( String answer, String type ,String separator) {
 	Document doc=null;
 	Vector<String[]> names = new Vector<String[]>();
 	//File message=null;
 	//System.out.println( "displaying XML ..." );
 	answer =   "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + answer;
 	//System.out.println( answer );
 	try {
 	File messageFile = new File(basicFolder.getAbsolutePath() + File.separator + "messageFile");
 	FileWriter out = new FileWriter(messageFile);
 	out.write( answer );
 	out.close();
 	
 	} catch (Exception e) {
         //compilerError(Compiler.XML, e.getMessage());
     }
 	
 	try {
 		//System.out.println( "parsing ... ");
 		doc = parse(new File(basicFolder.getAbsolutePath() + File.separator + "messageFile"));
 		Element e = doc.getDocumentElement();
 		NodeList m = e.getElementsByTagName("table");    
 		Element cor = (Element)m.item(1);
 		NodeList methods = cor.getElementsByTagName(type);
 		
 		//System.out.println( "length lines ="+ methods.getLength());
 		
 		
 		for (int i=0; i< methods.getLength(); i++) {
    		    Element lns = (Element) methods.item(i);
    		      //String nm = method.getAttribute("NAME");
    		    NodeList cols = lns.getElementsByTagName("td");
    		    
    		    //System.out.println( "length cols=" +  cols.getLength());
    		    
    		    String[] tt = (String[]) new String[cols.getLength()];
    		    
    		    for (int j=0; j< cols.getLength(); j++) {
  		      Node col = (Node) cols.item(j);
  		      //String nm = method.getAttribute("NAME");
  		      
  		      Node firstnode = col.getFirstChild();
  		      String nm=null;
  		     
    		      if(firstnode!=null) { 
    		    	  nm = firstnode.getNodeValue();
    		      
  		          if(nm!=null) { 
  		    	  
  		          if(separator!=null)
  		        	  if (nm.lastIndexOf(separator) > 0 && nm.lastIndexOf(separator) < nm.length()-1)
  		        		  tt[j] = nm.substring(nm.lastIndexOf(separator)+1,nm.length());
  		        	  else tt[j] = nm; 
  		          else 
  		    	  tt[j] = nm;
  		          //System.out.println( "name="+tt[j] );
  		          }
    		      }
  		      
  		    }
    		    names.add(tt);
    		}
 	
 	} catch (Exception e) {
         //compilerError(Compiler.XML, e.getMessage());
 		//System.out.println("Error="+e.getMessage() + "xml parser prob.");
     }
 	
  
 	return names;
  
 }
 
 private static Document parse(File f) throws Exception {
     try {
       String uri = f.toURL().toString();
       return parse(uri);
     } catch (MalformedURLException ex) {
       //compilerError(Compiler.FILE, ex.getMessage());
     }
     return null;
   }

 private static Document parse(String uri) throws Exception {
     org.w3c.dom.Document doc = null;
     try {
 	javax.xml.parsers.DocumentBuilderFactory dbf =
 	    javax.xml.parsers.DocumentBuilderFactory.newInstance();
 	dbf.setValidating(false);
 	javax.xml.parsers.DocumentBuilder builder = dbf.newDocumentBuilder();
 	doc = builder.parse(uri);
     } catch (Exception e) {
 	//compilerError(Compiler.XML, e.getMessage());
     }
     return doc;
 }
 
}


