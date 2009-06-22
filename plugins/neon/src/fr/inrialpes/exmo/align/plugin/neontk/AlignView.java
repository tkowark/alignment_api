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

import org.eclipse.swt.SWT;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Set;
import java.util.Vector;

import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;


import org.eclipse.ui.part.ViewPart;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.ScrolledFormText;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.MessageDialog;

//import org.eclipse.jface.dialogs.MessageDialog;
import org.semanticweb.kaon2.api.OntologyManager;
import org.semanticweb.owl.align.Alignment;

import com.ontoprise.ontostudio.datamodel.DatamodelPlugin;

import fr.inrialpes.exmo.align.onto.owlapi10.OWLAPIOntologyFactory;
import fr.inrialpes.exmo.align.plugin.neontk.AlignFormLayoutFactory;
import fr.inrialpes.exmo.align.plugin.neontk.AlignFormSectionFactory;
import fr.inrialpes.exmo.align.plugin.neontk.OnlineAlign;
import fr.inrialpes.exmo.align.plugin.neontk.WSDialog;
import fr.inrialpes.exmo.align.plugin.neontk.OnlineDialog;
//import org.eclipse.swt.awt.SWT_AWT;
//import java.awt.BorderLayout;
//import javax.swing.JPanel;
//import java.awt.Frame;
//import org.eclipse.albireo.core.SwingControl;
 
public class AlignView extends ViewPart 
	implements SelectionListener, Listener {
		
		public static final String ID = "fr.inrialpes.exmo.align.plugin.neontk.alignView";	
		private Combo  methods, renderer, localAlignBox, ontoBox1, ontoBox2,  serverAlignBox;
		private Button cancelButton, discardButton,  resButton, onto1Refresh, onto2Refresh,
					   localImportButton, serverImportButton, uploadButton, 
					   localTrimButton, serverTrimButton, connButton, goButton, offlineButton, onlineButton;
		private Button storeButton,  matchButton, findButton, findAllButton, fetchButton;
		//private String selectedProject = null;
		//private Section ontoSelectSection;
		//private Section alignViewSection;
		
		private String selectedOnto1, selectedOnto2,  selectedLocalAlign, selectedServerAlign;
		private String[] ontoList = new String[0];
		private String[]  methodList = new String[0];
		public HashMap<String,String> ontoByProj = new HashMap<String,String>(0); 
		//String selectedLocalAlign =  null;
		//String selectedAlign =  null;
		
		public static Hashtable<String,Alignment>  alignmentTable = new Hashtable<String,Alignment>();
		static String [] forUniqueness = new String[0];
		static int alignId = 0;
		
		Composite composite = null;
		int width = 700;
		int buttonWidth = 150;
		int buttonHeight = 30;
		
		boolean online = false;

		String selectedHost = "aserv.inrialpes.fr"; 
		String selectedPort = "80"; 
		
		String selectedMethod = "fr.inrialpes.exmo.align.impl.method.NameEqAlignment";
	    String wserver = "http://kameleon.ijs.si/ontolight/ontolight.asmx";
	    String wsmethod="";
	    
	    
		String alignProject = "AlignmentProject";
		
		public OnlineAlign   onlineAlign  = null;
		public OfflineAlign  offlineAlign = null;
		public File ontoFolder = null;
		public File alignFolder = null;
		public static File basicFolder = null;
		
		@Override
		public void createPartControl(final Composite parent) {
			FormToolkit formToolkit = new FormToolkit(Display.getCurrent());
			ScrolledForm scrolledForm = formToolkit.createScrolledForm(parent);		
			Composite body = scrolledForm.getBody();
			body.setLayout(AlignFormLayoutFactory.createFormTableWrapLayout(false,1));
			composite = formToolkit.createComposite(body);
			composite.setLayout(AlignFormLayoutFactory.createFormPaneTableWrapLayout(false, 1));
			composite.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
			
			createOntologyChoosePart(composite, formToolkit);		
			createAlignmentPart(composite, formToolkit);
			//createOntologyInfoSection(composite, formToolkit);		
			//createMUPSSection(composite, formToolkit);	
			//createInconsistencyResultSection(composite, formToolkit);
			//this.refreshProjectList();
			IWorkspaceRoot root =  org.eclipse.core.resources.ResourcesPlugin.getWorkspace().getRoot();
		    IPath location = root.getLocation();
		    String  path = location.toOSString();
		     
		    
		    //ontoFolder = new File(path + location.SEPARATOR + "onto");
		    ontoFolder = new File(path + location.SEPARATOR + "align");
		    
		    if (!ontoFolder.exists()) ontoFolder.mkdir();
		    basicFolder = new File(path + location.SEPARATOR );
		    alignFolder = new File(path + location.SEPARATOR + "align");
		    if (!alignFolder.exists()) alignFolder.mkdir();
		    
		    offlineInit( true );
		    
			refreshOntoList( true );
		}
		
		private void createOntologyChoosePart(final Composite parent,
				final FormToolkit toolkit){
			
			String sectionTitle = "Input";		
			Composite client ;
			int columns = 6;
			int textWidth = 600;
			int textHeight = 20;
			GridData gd = new GridData();
			gd.widthHint = textWidth;
			gd.heightHint = textHeight;
					
			client = AlignFormSectionFactory.createGridExpandableSection(
					toolkit, parent, sectionTitle, columns, true);
			Section ontoSelectSection = AlignFormSectionFactory.getGridExpendableSection();		
			ontoSelectSection.setExpanded(true);
			
			onlineButton = new Button(client, SWT.PUSH);
			onlineButton.setText("Online");
			onlineButton.setSize(buttonWidth, buttonHeight);
			onlineButton.addSelectionListener(this);
			onlineButton.setEnabled( true );
			
			offlineButton = new Button(client, SWT.PUSH);
			offlineButton.setText("Offline");
			offlineButton.setSize(buttonWidth, buttonHeight);
			offlineButton.addSelectionListener(this);
			offlineButton.setEnabled( false );
			
			Label dummy1 = new Label(client, SWT.NONE);
			Label dummy2 = new Label(client, SWT.NONE);
			Label dummy3 = new Label(client, SWT.NONE);
			Label dummy4 = new Label(client, SWT.NONE);
 
			// Choose ontology 1	
			Label onto1 = new Label(client, SWT.NONE);		
			onto1.setText("Ontology 1 ");			
			ontoBox1 = new Combo(client, SWT.DROP_DOWN | SWT.READ_ONLY  );
			ontoBox1.setLayoutData( gd );
			ontoBox1.setEnabled(true);
			ontoBox1.addSelectionListener(this);
			onto1Refresh = new Button(client, SWT.PUSH);
			onto1Refresh.setText("Refresh");
			onto1Refresh.setSize(buttonWidth, buttonHeight);
			onto1Refresh.addSelectionListener(this);
			onto1Refresh.setEnabled(true);
			Label dummy5 = new Label(client, SWT.NONE);
			Label dummy6 = new Label(client, SWT.NONE);
			Label dummy7 = new Label(client, SWT.NONE);
			
			//Choose ontology 2
			Label onto2 = new Label(client, SWT.NONE);
			onto2.setText("Ontology 2 ");		
			ontoBox2 = new Combo(client, SWT.DROP_DOWN | SWT.READ_ONLY );
			ontoBox2.setLayoutData(gd);
			ontoBox2.setEnabled(true);
			ontoBox2.addSelectionListener(this);
			onto2Refresh = new Button(client, SWT.PUSH);
			onto2Refresh.setText("Refresh");
			onto2Refresh.setSize(buttonWidth, buttonHeight);
			onto2Refresh.addSelectionListener(this);
			onto2Refresh.setEnabled(true);
			Label dummy8 = new Label(client, SWT.NONE);
			Label dummy9 = new Label(client, SWT.NONE);
			Label dummy10 = new Label(client, SWT.NONE);
			
			//methods
			Label methodLabel = new Label(client, SWT.NONE);
			methodLabel.setText("Methods ");		
			methods = new Combo(client, SWT.DROP_DOWN | SWT.READ_ONLY );
			methods.setLayoutData(gd);
			methods.setEnabled(true);
			methods.addSelectionListener(this);
			
			//match
			matchButton = new Button(client, SWT.PUSH);
			matchButton.setText("Match");
			matchButton.setSize(buttonWidth, buttonHeight);
			matchButton.addSelectionListener(this);
			matchButton.setEnabled(true);
			
			Label dummy11 = new Label(client, SWT.NONE);
			Label dummy12 = new Label(client, SWT.NONE);
			Label dummy13 = new Label(client, SWT.NONE);
			
			//server alignment list
			Label serverAlignLabel  = new Label(client, SWT.NONE );
			serverAlignLabel.setText("Server alignments");
			
			serverAlignBox =  new Combo(client, SWT.DROP_DOWN | SWT.READ_ONLY );
			serverAlignBox.setLayoutData(gd);
			serverAlignBox.setEnabled( false );
			serverAlignBox.addSelectionListener(this);
			
			
			//import
			serverImportButton = new Button(client, SWT.PUSH);
			serverImportButton.setText("Import");
			serverImportButton.setSize(buttonWidth, buttonHeight);
			serverImportButton.addSelectionListener(this);
			serverImportButton.setEnabled(false);
			
			//trim
			serverTrimButton = new Button(client, SWT.PUSH);
			serverTrimButton.setText("Trim");
			serverTrimButton.setSize(buttonWidth, buttonHeight);
			serverTrimButton.addSelectionListener(this);
			serverTrimButton.setEnabled(false);
			
			//store
			storeButton = new Button(client, SWT.PUSH);
			storeButton.setText("Store");
			storeButton.setSize(buttonWidth, buttonHeight);
			storeButton.addSelectionListener(this);
			storeButton.setEnabled(false);
			
			//fetch
			fetchButton = new Button(client, SWT.PUSH);
			fetchButton.setText("Fetch");
			fetchButton.setSize(buttonWidth, buttonHeight);
			fetchButton.addSelectionListener(this);
			fetchButton.setEnabled(false);
			
			//local alignment list
			Label localAlignLabel  = new Label(client, SWT.NONE );
			localAlignLabel.setText("Local alignments");
			
			localAlignBox =  new Combo(client, SWT.DROP_DOWN | SWT.READ_ONLY );
			localAlignBox.setLayoutData(gd);
			localAlignBox.setEnabled(false);
			localAlignBox.addSelectionListener(this);
			
			//local import
			localImportButton = new Button(client, SWT.PUSH);
			localImportButton.setText("Import");
			localImportButton.setSize(buttonWidth, buttonHeight);
			localImportButton.addSelectionListener(this);
			localImportButton.setEnabled(false);
			
			//local trim
			localTrimButton = new Button(client, SWT.PUSH);
			localTrimButton.setText("Trim");
			localTrimButton.setSize(buttonWidth, buttonHeight);
			localTrimButton.addSelectionListener(this);
			localTrimButton.setEnabled(false);
			
			//upload
			uploadButton = new Button(client, SWT.PUSH);
			uploadButton.setText("Upload");
			uploadButton.setSize(buttonWidth, buttonHeight);
			uploadButton.addSelectionListener(this);
			uploadButton.setEnabled(false);
			
			Label dummy14 = new Label(client, SWT.NONE);
			 
			Label dummy15 = new Label(client, SWT.NONE);
			Label dummy16 = new Label(client, SWT.NONE);
			Label dummy17 = new Label(client, SWT.NONE);
			Label dummy18 = new Label(client, SWT.NONE);
			Label dummy19 = new Label(client, SWT.NONE);
			Label dummy20 = new Label(client, SWT.NONE);
			
			Label dummy21 = new Label(client, SWT.NONE);
			
			//find
			findButton = new Button(client, SWT.PUSH);
			findButton.setText("Find alignments for ontologies");
			findButton.setSize(buttonWidth, buttonHeight);
			findButton.addSelectionListener(this);
			findButton.setEnabled(false);
			 
			Label dummy22 = new Label(client, SWT.NONE);
			Label dummy23 = new Label(client, SWT.NONE);
			Label dummy24 = new Label(client, SWT.NONE);
			Label dummy25 = new Label(client, SWT.NONE);
			Label dummy26 = new Label(client, SWT.NONE);
			
			//find all
			findAllButton = new Button(client, SWT.PUSH);
			findAllButton.setText("Find all alignments from server");
			findAllButton.setSize(buttonWidth, buttonHeight);
			findAllButton.addSelectionListener(this);
			findAllButton.setEnabled(false);
			
			toolkit.paintBordersFor(client);
		}
		
		private void createAlignmentPart(final Composite parent,
				final FormToolkit toolkit){
			
			String sectionTitle = "View Alignment";
	
			//int columns = 1;
			//int textWidth = 800;
			//int textHeight = 20;
			//FillLayout fl = new FillLayout();
			//Composite client   = new ScrolledFormText( parent, true );
			//fl.widthHint = textWidth;
			//fl.heightHint = textHeight;
			//String tx = "<html><body>This is Unicode HTML content from memory</body></html>";
			Composite client = AlignFormSectionFactory.createHtmlSection(
					toolkit, parent, sectionTitle); 	
			//client = AlignFormSectionFactory.createGridExpandableSection(
					//toolkit, parent, sectionTitle, columns, true);
			//alignViewSection = AlignFormSectionFactory.getGridExpendableSection();		
			//alignViewSection = AlignFormSectionFactory.getSimpleSection();
			//alignViewSection.setExpanded( true );
			
			//Composite client   = new ScrolledFormText( parent, true );
			
			client.setLayout(new FillLayout());
		    client.setSize(960, 720);
		    client.setLocation(0, 0);
		    
		    Browser browser = new Browser(client, SWT.BORDER);
		    browser.setText("<html><body>This is Unicode HTML content from memory</body></html>");
		    
			toolkit.paintBordersFor(client);
		}

		public void handleEvent(Event e) { 
			
		}
		
		public void widgetSelected(SelectionEvent e) {
			if ( e.getSource().equals( this.onlineButton ) ) {
				//this.onlineButton.setExpanded(true);
				OnlineDialog onDialog = new OnlineDialog( getSite().getShell() );
				onDialog.setInput("aserv.inrialpes.fr");
				onDialog.open();
				if ( !(onDialog.getInput() == null) && !onDialog.getInput().equals("")) { 
						online = true;
						selectedHost = onDialog.getInput();
						onlineAlign = new OnlineAlign(selectedPort, selectedHost );
		    			 
	        			String list[] = onlineAlign.getMethods();
	        			if(list == null || list.length ==0) { 
	        				MessageDialog.openError(this.getSite().getShell(), "Error message",
	        						"Impossible connection!");
	        			    return;
	        			}
					    if(localAlignBox.getItems()==null || localAlignBox.getItems().length ==0) {
					    	setButtons( 3 ); //no localList, no server list
					    	 
					    } else {
					    	setButtons( 6 ); //with localList, no server list
					    }
					    selectedOnto1 = null;
					    selectedOnto2 = null;
					    selectedServerAlign = null;
					    selectedLocalAlign = null;
	        			methods.removeAll();
	        			selectedMethod = list[0];				
	        			methods.setItems( list );
	    				methods.select(0);
	    				methods.redraw(); 	
					}
				 
			} else if ( e.getSource().equals( this.offlineButton ) ) {
				offlineInit( false );
				
			} else if (e.getSource() == onto1Refresh) {			
				ontoByProj = this.refreshOntoList( online );
				
			} else if (e.getSource() == onto2Refresh) {
				ontoByProj = this.refreshOntoList( online );
			} else if (e.getSource() == matchButton) {
				
			   if (selectedOnto1 == null  || selectedOnto2 == null ) {
					MessageDialog.openError(this.getSite().getShell(), "Error message",
							"Choose two ontologies from lists! "); 
		       		return;
		       }
		       
			   if( online ) {
				   		if (!selectedOnto1.startsWith("http://") || !selectedOnto2.startsWith("http://") ) {
				   			MessageDialog.openError(this.getSite().getShell(), "Error message", "URLs for ontologies are required.");
				   			return;
				   		}
				   		if( selectedMethod.equals("fr.inrialpes.exmo.align.service.WSAlignment")) {
				   			
				   			WSDialog pa = new WSDialog( getSite().getShell() );
				   			if( pa.getServerInput() == null || pa.getServerInput().equals("")) {
				   				MessageDialog.openError(this.getSite().getShell(), "Error message", "No server!");
					   			return;
				   			} else {
				   			   wserver = pa.getServerInput();
				   			   wsmethod = pa.getMethodInput();
				   			   String alignId = onlineAlign.getAlignIdMonoThread( selectedMethod, wserver, wsmethod, selectedOnto1, selectedOnto2 );
				 			   String[] list = new String[1];
				 			   list[0] = alignId;
				 			   selectedServerAlign = alignId;
				 			   serverAlignBox.removeAll();
				 			   serverAlignBox.setItems(list);
				 			   serverAlignBox.select(0);
				 			   serverAlignBox.redraw(); 
				   			}
				   				
				   		} else {
				   			//resetActionButtons( false );
				   			String alignId = onlineAlign.getAlignIdMonoThread( selectedMethod, wserver, wsmethod, selectedOnto1, selectedOnto2 );
							//onAlign.getAlignId( matchMethod, wserver, wsmethod, selectedOnto1, selectedOnto2 );
				   			String[] list = new String[1];
							list[0] = alignId;
							selectedServerAlign = list[0];
							serverAlignBox.removeAll();
							serverAlignBox.select(0);
							serverAlignBox.redraw();
							 
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
						
					String resId  = offlineAlign.matchAndExportAlign( selectedMethod, ontoByProj.get(selectedOnto1), selectedOnto1, ontoByProj.get(selectedOnto2), selectedOnto2);
					
					localAlignBox.removeAll();
					String[] list = new String[1];
					
					//File f1 = new File(resId);
					list[0] =  resId;//"file:" + f1.getAbsolutePath();
					  
        			localAlignBox.setItems(list);
        			selectedLocalAlign = list[0];
        			localAlignBox.select(0);
        			localAlignBox.redraw();
        			
				  } //offline
			   } else if (e.getSource() == localImportButton) {//matchButton
				   
			   } else if ( e.getSource().equals( this.findAllButton ) ) {
				   
				   String[] onList = null;
				   String[] offList = null;
				   
       			   if(online) {
       					onList = onlineAlign.getAllAlign();
       					if(onList == null || onList.length==0) {
       						MessageDialog.openError(this.getSite().getShell(), "Error message", "No available alignment"); 
       						return;
       					}
       					//serverAlignBox.setEnabled( true );
       					serverAlignBox.removeAll();
       					serverAlignBox.setItems(onList);
       					selectedServerAlign = onList[0]; 
       					serverAlignBox.select(0);
       					serverAlignBox.redraw();
       			 
       					offList  = offlineAlign.getAllAlign( );
       					if( offList != null && offList.length > 0 ) {
       					   //localAlignBox.setEnabled( true );
   						   localAlignBox.removeAll();
   						   localAlignBox.setItems( offList );
   						   selectedLocalAlign = offList[0];
   						   localAlignBox.select(0);
   						   localAlignBox.redraw();
       					}
       			  }
       			   
       			  if( onList.length > 0  && (offList.length > 0) )   {
       				setButtons( 7 );
       			  }
       			  else if( onList.length == 0 && offList.length > 0 )   
       				setButtons( 6 );
       			  else if( onList.length == 0 && offList.length == 0 )   
       				setButtons( 3 );
       			  else if( onList.length >0 && offList.length == 0 )    {
       				//System.out.println("OK ");
       				setButtons( 8 );	
       			  }    			
			   } else if ( e.getSource().equals( this.findButton ) ) {
				   String[] onList = null;
				   if(online) {
					   if(selectedOnto1 == null || selectedOnto1.equals("") || selectedOnto2 == null || selectedOnto2.equals("")) {
     						MessageDialog.openError(this.getSite().getShell(), "Error message", "Please choose ontologies."); 
     						return;
     					}
      					onList = onlineAlign.findAlignForOntos( selectedOnto1, selectedOnto2);
      					if(onList == null || onList.length==0) {
      						MessageDialog.openError(this.getSite().getShell(), "Error message", "No available alignment."); 
      						return;
      					}
      					serverAlignBox.setEnabled( true );
      					serverAlignBox.removeAll();
      					serverAlignBox.setItems(onList);
      					selectedServerAlign = onList[0]; 
      					serverAlignBox.select(0);
      					serverAlignBox.redraw();
      			  }
			   }
		}
			 
		
		void offlineInit(boolean init) {
			online = false;
			methods.removeAll();
			serverAlignBox.removeAll();
			localAlignBox.removeAll();
			ontoBox1.removeAll();
			ontoBox2.removeAll();
			
		 	String[] methodList = new String[8];
			methodList[0] = "fr.inrialpes.exmo.align.impl.method.NameEqAlignment";
				 
			methodList[1] = "fr.inrialpes.exmo.align.impl.method.StringDistAlignment";
				 
			methodList[2] = "fr.inrialpes.exmo.align.impl.method.SMOANameAlignment";
				 
			methodList[3] = "fr.inrialpes.exmo.align.impl.method.SubsDistNameAlignment";
				 
			methodList[4] = "fr.inrialpes.exmo.align.impl.method.StrucSubsDistAlignment";
				 
			methodList[5] = "fr.inrialpes.exmo.align.impl.method.NameAndPropertyAlignment";
				 
			methodList[6] = "fr.inrialpes.exmo.align.impl.method.ClassStructAlignment";
				 
			methodList[7] = "fr.inrialpes.exmo.align.impl.method.EditDistNameAlignment";
			
			selectedMethod = methodList[0];
			methods.removeAll();
			methods.setItems( methodList );
			methods.select(0);
			methods.redraw();
			
			System.out.println( "alignFolder=" + alignFolder );
			
			offlineAlign = new OfflineAlign( alignFolder, ontoFolder );
			
			//initButtons( false ); 
			
			if( init ) 
				offlineAlign.getAllAlignFromFiles();
			
			String[] list = offlineAlign.getAllAlign();
			localAlignBox.removeAll();
 		
			if(list != null && list.length > 0) {	
				forUniqueness = new String[list.length];
				 
				for(int i=0; i< list.length; i++){
					System.out.println( "filename=" + list[i] );
					File f = new File(list[i]);
					forUniqueness[i] = f.getName();
				}
				localAlignBox.setItems( list ); 
				selectedLocalAlign = list[0];
				setButtons( 1 );
				
			} else {
				setButtons( 0 );
			}
		}
		
		void setButtons( int status  ) {
			switch (status) {
			case  0 : // activate offline no localList
				onlineButton.setEnabled( true );
				offlineButton.setEnabled( false );
				findButton.setEnabled(false);
				findAllButton.setEnabled(false);
				
				serverAlignBox.setEnabled( false );
				serverImportButton.setEnabled( false );
				serverTrimButton.setEnabled( false );
				storeButton.setEnabled( false );
				fetchButton.setEnabled( false );
				uploadButton.setEnabled( false );
				
				localAlignBox.setEnabled( false );
				localImportButton.setEnabled( false );
				localTrimButton.setEnabled( false );
				break;
				
			case  1 : // activate offline with localList
				onlineButton.setEnabled( true );
				offlineButton.setEnabled( false );
				findButton.setEnabled(false);
				findAllButton.setEnabled(false);
				
				serverAlignBox.setEnabled( false );
				serverImportButton.setEnabled( false );
				serverTrimButton.setEnabled( false );
				storeButton.setEnabled( false );
				fetchButton.setEnabled( false );
				uploadButton.setEnabled( false );
				
				localAlignBox.setEnabled( true );
				localImportButton.setEnabled( true );
				localTrimButton.setEnabled( true );
				break;
				
				case  2 : // activate online no list
					onlineButton.setEnabled( false );
					offlineButton.setEnabled( true );
					findButton.setEnabled(true);
					findAllButton.setEnabled(true);
					
					serverAlignBox.setEnabled( false );
					serverImportButton.setEnabled( false );
					serverTrimButton.setEnabled( false );
					storeButton.setEnabled( false );
					fetchButton.setEnabled( false );
					uploadButton.setEnabled( false );
					
					
					localAlignBox.setEnabled( false );
					localImportButton.setEnabled( false );
					localTrimButton.setEnabled( false );
					break;
					
				case  3 : // activate online no localList (no server List)
					onlineButton.setEnabled( false );
					offlineButton.setEnabled( true );
					findButton.setEnabled(true);
					findAllButton.setEnabled(true);
					
					serverAlignBox.setEnabled( false );
					serverImportButton.setEnabled( false );
					serverTrimButton.setEnabled( false );
					storeButton.setEnabled( false );
					fetchButton.setEnabled( false );
					uploadButton.setEnabled( false );
					
					
					localAlignBox.setEnabled( false );
					localImportButton.setEnabled( false );
					localTrimButton.setEnabled( false );
					break;
				
				case  4 : // activate online align. not available
					onlineButton.setEnabled( false );
					offlineButton.setEnabled( true );
					findButton.setEnabled(true);
					findAllButton.setEnabled(true);
					
					serverAlignBox.setEnabled( true );
					serverImportButton.setEnabled( true );
					serverTrimButton.setEnabled( false );
					storeButton.setEnabled( false );
					fetchButton.setEnabled( true );
					uploadButton.setEnabled( true );
					
					
					localAlignBox.setEnabled( true );
					localImportButton.setEnabled( true );
					localTrimButton.setEnabled( true );
					break;
				
				case  5 : // activate online align available
					onlineButton.setEnabled( false );
					offlineButton.setEnabled( true );
					findButton.setEnabled(true);
					findAllButton.setEnabled(true);
					
					serverAlignBox.setEnabled( true );
					serverImportButton.setEnabled( true );
					serverTrimButton.setEnabled( true );
					storeButton.setEnabled( true );
					fetchButton.setEnabled( true );
					uploadButton.setEnabled( true );
					
					
					localAlignBox.setEnabled( true );
					localImportButton.setEnabled( true );
					localTrimButton.setEnabled( true );
					break;
					
				case  6 : // activate online  with localList (no serverList)
					onlineButton.setEnabled( false );
					offlineButton.setEnabled( true );
					findButton.setEnabled( true );
					findAllButton.setEnabled( true );
					
					serverAlignBox.setEnabled( false );
					serverImportButton.setEnabled( false );
					serverTrimButton.setEnabled( false );
					storeButton.setEnabled( false );
					fetchButton.setEnabled( false );
					uploadButton.setEnabled( true );
					
					localAlignBox.setEnabled( true );
					localImportButton.setEnabled( true );
					localTrimButton.setEnabled( true );
					break;
 				
				case  7 : // activate online  with both Lists
					onlineButton.setEnabled( false );
					offlineButton.setEnabled( true );
					findButton.setEnabled( true );
					findAllButton.setEnabled( true );
					
					serverAlignBox.setEnabled( true );
					serverImportButton.setEnabled( false );
					serverTrimButton.setEnabled( false );
					storeButton.setEnabled( false );
					fetchButton.setEnabled( true );
					uploadButton.setEnabled( true );
					
					localAlignBox.setEnabled( true );
					localImportButton.setEnabled( true );
					localTrimButton.setEnabled( true );
					break;
					
				case  8 : // activate online  with server list but no local list
					onlineButton.setEnabled( false );
					offlineButton.setEnabled( true );
					findButton.setEnabled( true );
					findAllButton.setEnabled( true );
					
					serverAlignBox.setEnabled( true );
					serverImportButton.setEnabled( false );
					serverTrimButton.setEnabled( false );
					storeButton.setEnabled( false );
					fetchButton.setEnabled( true );
					uploadButton.setEnabled( true );
					
					localAlignBox.setEnabled( false );
					localImportButton.setEnabled( false );
					localTrimButton.setEnabled( false );
					break;
				default:
					break;
			}
		}
		
		
		private HashMap<String,String> refreshOntoList(boolean online) {
			HashMap<String,String>  vec = new HashMap<String,String>();
			OWLAPIOntologyFactory fact = new OWLAPIOntologyFactory();
			try {
				String[] projects = DatamodelPlugin.getDefault().getOntologyProjects();
				if(projects != null) {
				for(int i=0; i < projects.length; i++) {	 
					if(projects[i]!=null) {  
							 
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
			
			ontoBox1.removeAll();
			ontoBox2.removeAll();
			
			String[] keys = (String[]) vec.keySet().toArray(new String[0]);
			
			if(keys.length>0){
				ontoList = new String[ keys.length ];
				ontoBox1.setItems(keys);
				ontoBox1.select(0);
				ontoBox1.redraw();
				selectedOnto1 = keys[0];
				ontoBox2.setItems(keys);
				ontoBox2.select(0);
				ontoBox2.redraw();
				selectedOnto2 = keys[0];
			}
			
			return vec;	 
		}
		@Override
		public void setFocus() {
			// TODO Auto-generated method stub
			
		}
		public void widgetDefaultSelected(SelectionEvent e) {
			// TODO Auto-generated method stub
			
		}	
}
