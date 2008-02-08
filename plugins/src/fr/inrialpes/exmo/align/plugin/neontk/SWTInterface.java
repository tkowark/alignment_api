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
import java.io.FileWriter;
import java.net.MalformedURLException;
//import java.io.FileReader;
//import java.io.IOException;

//import java.net.MalformedURLException;
//import java.net.URL;
//import java.net.URI;
import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.awt.event.ActionEvent;

import java.util.Vector;
//import java.io.PrintWriter;
 
//import javax.swing.*;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.JFileChooser;
import javax.swing.JTextArea;
import javax.swing.JButton;
import javax.swing.JOptionPane;
//import javax.swing.SwingConstants;
import javax.swing.JScrollPane;
import javax.swing.BorderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
 
//import org.eclipse.core.resources.IProject;
import com.ontoprise.ontostudio.datamodel.DatamodelPlugin;
//import com.ontoprise.ontostudio.gui.navigator.module.ModuleTreeElement;
import com.ontoprise.ontostudio.io.ImportExportControl;
//import com.ontoprise.ontostudio.gui.commands.CreateProject;
import com.ontoprise.ontostudio.gui.navigator.project.ProjectControl;
//import org.semanticweb.kaon2.api.formatting.OntologyFileFormat;
//import org.eclipse.core.runtime.IProgressMonitor;
 
 
public class SWTInterface extends JPanel {
	 
	private static JSplitPane _mainSplitter = new JSplitPane (JSplitPane.VERTICAL_SPLIT);

	//private URL SOAPUrl = null;
	//private String SOAPAction = null;
 
	JLabel hostName, portName;
	JComboBox hostList, portList;
	String selectedHost =  null;
	String selectedPort =  null;
	String selectedOnto1 =  null;
	String selectedOnto2 =  null;
	String selectedNeOnOnto1 =  null;
	String selectedNeOnOnto2 =  null;
	
	String[] hostNames = new String[2];
	String[] ports = new String[2];
	int hostInd, portInd;
	int alignId = 0;
	
	//Procalign procalign = null;
	//Parameters p = new BasicParameters();

	public JComponent phrases, phrases1, Res=null;
    public JTextField fileName1, fileName2, treshold;
    public SWTInterface frame;
    public JFileChooser open;
    //public JLabel explain;
    public JPanel pane;
    public JButton openbutton1, openbutton2, mapButton, resButton, ontoRefresh,
    		connButton, offlineButton, onlineButton, disconnButton ;
    public JPanel pane2;
	public Component result=null;
	public JTextArea jan=null;
    JComboBox strategy, renderer, ontoBox1, ontoBox2;
    JLabel strat, render, ontoLabel1, ontoLabel2;
    //boolean autointerations=false, efficients=true;
    //String aux_tres;
    public String [] aux = new String[5];
    public String [] methodList = new String[0];
    public String [] ontoList = new String[0];
    public String [] NeOnOntoList = new String[0]; 
    public String [] rendererList = null;
    public String [] alignList = null;
    public Vector	 corrList = new Vector();
    public String [] aservArgs = null;
    public String [] aservArgRetrieve = null;
    public String [] aservArgAlign = null;
    public String [] files = new String[2];
    int ind=0, rend=0, count=0;
	
    boolean online = false;
	//boolean chosenPort = false;
    String defaultHost = "aserv.inrialpes.fr";
    String defaultPort = "8089";
    String matchMethod = "fr.inrialpes.exmo.align.impl.method.NameEqAlignment";
	String alignProject = "AlignmentProject";
	public OnlineAlign   onAlign = null;
	public OfflineAlign  offAlign = null;
    
    public void initialize() {

	//selectedHost = "aserv.inrialpes.fr";
	//selectedPort = "8089";
	
    offlineButton = new JButton("Offline",null);
    offlineButton.addActionListener(new ActionListener(){
    public void actionPerformed(ActionEvent e) {
    		if (e.getSource() == offlineButton) {
    			
    			online = false;
    			offAlign = new OfflineAlign();
    			//openbutton1.setEnabled(true);
				//openbutton2.setEnabled(true);
    			ontoBox1.setEnabled(true);
				ontoBox2.setEnabled(true);
				ontoRefresh.setEnabled(true);
				strategy.setEnabled(true);
				mapButton.setEnabled(true);
				onlineButton.setEnabled(true);
				
				offlineButton.setEnabled(false);
				disconnButton.setEnabled(false);
				hostList.setEnabled(false);
				portList.setEnabled(false);
				connButton.setEnabled(false);
				
				strategy.removeAllItems();
				
				methodList = new String[9];
				methodList[0] = "fr.inrialpes.exmo.align.impl.method.NameEqAlignment";
				strategy.addItem("NameEqAlignment");
				methodList[1] = "fr.inrialpes.exmo.align.impl.method.StringDistAlignment";
				strategy.addItem("StringDistAlignment");
				methodList[2] = "fr.inrialpes.exmo.align.impl.method.SMOANameAlignment";
				strategy.addItem("SMOANameAlignment");
				methodList[3] = "fr.inrialpes.exmo.align.impl.method.SubsDistNameAlignment";
				strategy.addItem("SubsDistNameAlignment");
				methodList[4] = "fr.inrialpes.exmo.align.impl.method.StrucSubsDistAlignment";
				strategy.addItem("StrucSubsDistAlignment");
				methodList[5] = "fr.inrialpes.exmo.align.impl.method.NameAndPropertyAlignment";
				strategy.addItem("NameAndPropertyAlignment");
				methodList[6] = "fr.inrialpes.exmo.align.impl.method.ClassStructAlignment";
				strategy.addItem("ClassStructAlignment");
				methodList[7] = "fr.inrialpes.exmo.align.impl.method.EditDistNameAlignment";
				strategy.addItem("EditDistNameAlignment");
				
				try {
					
					
					//IProject project = DatamodelPlugin.getDefault().getProject(alignProject);
					//if(project==null)
						ProjectControl.getDefault().createNewOntologyProject(alignProject, new String[0]);
				} 
				catch ( Exception ex ) { ex.printStackTrace(); };
				 
    		}
    	
        };
    	});
    	
    onlineButton = new JButton("Online",null);
    onlineButton.addActionListener(new ActionListener(){
    public void actionPerformed(ActionEvent e) {
    		if (e.getSource() == onlineButton) {
    			online = true;
    			
    			
				offlineButton.setEnabled(true);
				hostList.setEnabled(true);
				portList.setEnabled(true);
				connButton.setEnabled(true);
				
				onlineButton.setEnabled(false);
				disconnButton.setEnabled(false);
				ontoBox1.setEnabled(false);
				ontoBox2.setEnabled(false);
				ontoRefresh.setEnabled(false);
				//openbutton1.setEnabled(false);
				//openbutton2.setEnabled(false);
				strategy.setEnabled(false);
				mapButton.setEnabled(false);
    		}
    	
        };
    	});
    	
    disconnButton = new JButton("Disconnect",null);
    disconnButton.setEnabled(false);
    disconnButton.addActionListener(new ActionListener(){
    public void actionPerformed(ActionEvent e) {
    		if (e.getSource() == disconnButton) {
    			
    			offlineButton.setEnabled(true);
    			onlineButton.setEnabled(true);
    			
    			online = false;
    			ontoBox1.setEnabled(false);
				ontoBox2.setEnabled(false);
				ontoRefresh.setEnabled(false);
    			//openbutton1.setEnabled(false);
				//openbutton2.setEnabled(false);
				strategy.setEnabled(false);
				mapButton.setEnabled(false);
				disconnButton.setEnabled(false);
				hostList.setEnabled(false);
				portList.setEnabled(false);
				connButton.setEnabled(false);
    		}
    	
        };
    	});
    	
	hostNames[0] = defaultHost;
	selectedHost = hostNames[0];
	ports[0] 	 = defaultPort;
	selectedPort = ports[0];
	
	hostName = new JLabel( "Host name" );
	
	hostList = new JComboBox( hostNames );
	hostList.setEnabled(false);
	hostList.setEditable( true );
	hostList.setMaximumRowCount(20);
	hostList.addItemListener(new ItemListener() {
	public void itemStateChanged(ItemEvent event)
	{
	  if (event.getStateChange()==ItemEvent.SELECTED)
		hostInd  = hostList.getSelectedIndex();
	    selectedHost = hostNames[hostInd];	   
	    
	}
	});
	
	portName = new JLabel("Port");
	portList = new JComboBox( ports );
	portList.setEnabled(false);
	portList.setEditable( true );
	portList.setMaximumRowCount(20);
	portList.addItemListener(new ItemListener() {
	public void itemStateChanged(ItemEvent event) {
	  if (event.getStateChange()==ItemEvent.SELECTED)
		portInd  = portList.getSelectedIndex();
	    selectedPort = ports[portInd];	 
	     
	}
	});
	
	connButton = new JButton("Connect",null);
	connButton.setEnabled(false);
	connButton.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent e) {
        		if (e.getSource() == connButton) {
        			// for connecting to server 
        			onAlign = new OnlineAlign(selectedPort, selectedHost);
        			
        			 
        			String[] list = getResultsFromAnswer( onAlign.getMethods(), "method", "." ); 
        		 
        			
        			methodList = new String[list.length];
        			strategy.removeAllItems();
        							
        			for(int i=0; i< list.length; i++){
        				if(selectedHost.equals(defaultHost)) {
        							    methodList[i]= "fr.inrialpes.exmo.align.impl.method." + list[i];
        								strategy.addItem(list[i]);
        				}
        								
        			}	
        			//openbutton1.setEnabled(true);
        							//openbutton2.setEnabled(true);
        			ontoBox1.setEnabled(true);
        			ontoBox2.setEnabled(true);
        			strategy.setEnabled(true);
        			ontoRefresh.setEnabled(true);
        			mapButton.setEnabled(true);
        			disconnButton.setEnabled(true);
        							
        			hostList.setEnabled(false);
        			portList.setEnabled(false);
        			connButton.setEnabled(false);
        							
        			try {
        			//IProject project = DatamodelPlugin.getDefault().getProject(alignProject);
        			//if(project == null)
        			ProjectControl.getDefault().createNewOntologyProject(alignProject, new String[0]);
        			}   catch ( Exception ex ) { ex.printStackTrace(); };
        							
            	}
			
		};
        });
	
	/*
	open= new JFileChooser();
	ExampleFileFilter filter = new ExampleFileFilter();
    	filter.addExtension("owl");
    	filter.setDescription("Protégé Files");
    	open.setFileFilter(filter);
	openbutton1 = new JButton("Open Ontology 1",null);
	openbutton1.setEnabled(false);
	openbutton1.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent e) {
        		if (e.getSource() == openbutton1) {
				int returnVal = open.showOpenDialog(frame);
            			if (returnVal == JFileChooser.APPROVE_OPTION) {
                		File file = open.getSelectedFile();
				String end = file.getAbsolutePath();
				//if(fileName1.getText()==null || fileName1.getText().equals("")) 
				fileName1.setText(end);
				
				//aux[0] = fileName1.getText();
		 
            			}
			}
		};
        });
	
	openbutton2 = new JButton("Open Ontology 2",null);
	openbutton2.setEnabled(false);
	openbutton2.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent e) {
        		if (e.getSource() == openbutton2) {
            			int returnVal2 = open.showOpenDialog(frame);
            			if (returnVal2 == JFileChooser.APPROVE_OPTION) {
				File file = open.getSelectedFile();
				String end2 = file.getAbsolutePath();	
				
				//if(fileName2.getText()==null || fileName2.getText().equals("")) 
			    fileName2.setText(end2);
				 
				//aux[1] = fileName2.getText();
		 
            	}
			}
		};
        });
	*/
	
	ontoRefresh = new JButton("Refresh",null);
	ontoRefresh.setEnabled(false);
	ontoRefresh.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent e) {
        		if (e.getSource() == ontoRefresh) {
            			 
			    refreshOntoList();
            	
			}
		};
        });
	
	
	mapButton = new JButton("Match",null);	
	mapButton.setEnabled(false);
	mapButton.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent e) {
		   if (e.getSource() == mapButton) {
		       if (aux[0]=="" || aux[1]==""){JOptionPane.showMessageDialog(null, "Where are the ontologies?","Warning",2);}
			   else {
				   
				  
				  if(online == false) {
					  
					  
					  String res  = offAlign.matchAndExportAlign(alignId++, matchMethod, selectedNeOnOnto1, selectedNeOnOnto2, alignProject);
					  corrList = getCorresFromAnswer( res, "tr", "#" );
					  
					  System.out.println("corrList Size="+corrList.size());
					  
					  pane2.removeAll();
					  
					  for(int i=0;i< corrList.size();i++) {
								String[] li= (String [])corrList.get(i); 
								String n = "";
								for(int j=0;j<li.length;j++) 
								{ 
									String sep = "";
									if(n.equals("")) sep = "==>"; else if(n.equals("=")) sep = ""; else sep = "----";  
							
									if (li[j]!=null)
										n = n + sep+ li[j];
								}
							
								System.out.println("line= ="+ n);
								JTextArea header1= new JTextArea(n);
							
								pane2.add(header1);
						}
					 		       
					}
				  else { //online
					
				   
					
					String answer = onAlign.getAlignId( matchMethod, selectedOnto1, selectedOnto2  );
					if(answer==null)  JOptionPane.showMessageDialog(null, "Alignment is not produced.","Warning",2);
					else {
						
					
					alignList = getResultsFromAnswer( answer, "alid", null );
					if(alignList[0].equals("null"))  JOptionPane.showMessageDialog(null, "Alignment is not produced.","Warning",2);
					else {
					//retrieve alignment for storing in OWL file
					String owlalignStr = onAlign.getOWLAlignment( alignList[0] );
					
					//extract id from "alid" 
					String []  sali = alignList[0].split("/");
					String uniqueId = sali[sali.length-2].concat(sali[sali.length-1]);
						
					//Store it
					FileWriter out = null;
						
					try {
							File owlFile = new File( uniqueId + ".owl");
							out = new FileWriter( owlFile );
							out.write( owlalignStr );
							//out.write( answer );
							out.close();
							 
							
							//Redo : Have to create a list of imported alig. ontos	
							
							ImportExportControl ieControl = new ImportExportControl();	
							String[] importedModules = ieControl.importFileSystem(alignProject, uniqueId + ".owl", null, null);
							ieControl.addOntologies2Project(importedModules, alignProject);
							 
					}
					catch ( Exception ex ) { ex.printStackTrace(); System.out.println("problem  of align. retrieve");};
					
					//retrieve alignment for displaying
					String res = onAlign.getHTMLAlignment( alignList[0] );
					corrList = getCorresFromAnswer( res, "tr", "#" );	
							
					System.out.println("corrList Size="+corrList.size());
							
					pane2.removeAll();
					for(int i=0;i< corrList.size();i++) {
								
									String[] li= (String [])corrList.get(i); 
									String n = "";
									for(int j=0;j<li.length;j++) 
									{ 
										String sep = "";
										if(n.equals("")) sep = "==>"; else if(n.equals("=")) sep = ""; else sep = "----";  
								
										if (li[j]!=null)
											n = n + sep+ li[j];
									}
								
									System.out.println("line= "+ n);
									JTextArea header1= new JTextArea(n);
								
									pane2.add(header1);
							}
							
					}
					}
						    
				   } // else for ""online"
			       }
		  }
		};
	});
	
	/*
	resButton = new JButton("Result",null);	
	resButton.setEnabled(false);
	resButton.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == resButton) {
				{
				resButton.setEnabled(false);
				try {
					
					if (count > 0)
						pane2.remove(result); 
					//File f = new File("Cut.txt");
					File f = new File("Result.txt");
					jan= openFile(f);
					result= jan;
					//JTextArea header1= new JTextArea("Alignment Method: "+aux[2]+"\n"+"Renderer:"+aux[3]);		
					//pane2.add(header1);
				 
					pane2.add(result);
					
						count=1;
				   }catch(Exception err) {
     					err.printStackTrace();
     					}
				}
			}
		};
	});
	
	
	fileName1 = new JTextField(40);
        fileName1.setEnabled(true);
        fileName1.setHorizontalAlignment(SwingConstants.LEFT);
        fileName1.addActionListener(new ActionListener(){
    		public void actionPerformed(ActionEvent e) {
            		if (e.getSource() == fileName1) {
    				 
            		fileName1.selectAll();
    				//aux[0] = fileName1.getText();
    		 
    			}
    		};
            });
	
	
	fileName2 = new JTextField(40);
        fileName2.setEnabled(true);
        fileName2.setHorizontalAlignment(SwingConstants.LEFT);
	
        fileName2.addActionListener(new ActionListener(){
    		public void actionPerformed(ActionEvent e) {
            		if (e.getSource() == fileName2) {
    				 
    				fileName2.selectAll();
    				//aux[1] = fileName2.getText();
    		 
    			}
    		};
            });
    */
	
    //retrieve all available onto. in Ontology Navigator
   
     
    ontoLabel1 = new JLabel("Ontology 1 :");
    ontoBox1  = new JComboBox(ontoList);
    ontoBox1.setEnabled(false);
    ontoBox1.setMaximumRowCount(20);
    ontoBox1.addItemListener(new ItemListener() {
		public void itemStateChanged(ItemEvent event)
		{
	      int id = 0;
		  if (event.getStateChange()==ItemEvent.SELECTED)
			id = ontoBox1.getSelectedIndex();
		    
		    selectedOnto1 =  ontoList[id];
		    selectedNeOnOnto1 =  NeOnOntoList[id];
		    
		   
		    System.out.println("Onto1 selected =" + selectedOnto1);
		}
	});
    
    ontoLabel2 = new JLabel("Ontology 2 :");
    ontoBox2  = new JComboBox(ontoList);
    ontoBox2.setEnabled(false);
    ontoBox2.setMaximumRowCount(20);
    ontoBox2.addItemListener(new ItemListener() {
		public void itemStateChanged(ItemEvent event)
		{
	      int id = 0;
		  if (event.getStateChange()==ItemEvent.SELECTED)
			id = ontoBox2.getSelectedIndex();
		    
		    selectedOnto2 =  ontoList[id];
		    selectedNeOnOnto2 =  NeOnOntoList[id];
		    
		   
		    System.out.println("Onto2 selected =" + selectedOnto2);
		}
	});
    
    
	strat= new JLabel("Alignment methods");
	strategy = new JComboBox(methodList);
	strategy.setEnabled(false);
	strategy.setMaximumRowCount(20);
	strategy.addItemListener(new ItemListener() {
		public void itemStateChanged(ItemEvent event)
		{
		  if (event.getStateChange()==ItemEvent.SELECTED)
			ind = strategy.getSelectedIndex();
		    
		    matchMethod =  methodList[ind];
		    aux[2] =  methodList[ind];
		   
		    System.out.println("method selected ="+aux[2]);
		}
	});
	

    setLayout (new BorderLayout());
    add (new JLabel ("Alignment between two ontologies"), BorderLayout.NORTH);
    createFirstPane();//
     //createSecondPane();//
    createThirdPane();//
     
     //"ComponentFactory" In protege.jar
	 //JScrollPane top = ComponentFactory.createScrollPane(phrases);
    JScrollPane top = new JScrollPane(phrases);
     
    _mainSplitter.setTopComponent(top);//up panel
	 //JScrollPane bot = ComponentFactory.createScrollPane(Res);
    JScrollPane bot = new JScrollPane(Res);
	_mainSplitter.setBottomComponent(bot); //under panel
	_mainSplitter.setDividerLocation((int)top.getPreferredSize().getHeight());
	top.setMaximumSize(top.getPreferredSize());
	bot.setMaximumSize(bot.getPreferredSize());
	
	add(_mainSplitter,BorderLayout.CENTER); //Main Window of the Plugin
	
   }
 
public void createFirstPane () {
     phrases = createPhraseList();
     phrases.setBorder(BorderFactory.createEmptyBorder (10, 5, 10, 5)  );
   }
 
private JPanel createPhraseList () {
    JPanel phrasePane = new JPanel (new GridLayout (0, 1, 0, 10));
      
     
    JPanel minusLabel 	= new JPanel (new FlowLayout(10));
    JPanel zeroLabel 	= new JPanel (new FlowLayout(10));
    //JPanel label_port 	= new JPanel (new FlowLayout(10));
	
    JPanel oneLabel 	= new JPanel (new FlowLayout(10));
    JPanel twoLabel 	= new JPanel (new FlowLayout(10));
	JPanel threeLabel 	= new JPanel (new FlowLayout(10));

	minusLabel.add(onlineButton);
	minusLabel.add(offlineButton);
	
	zeroLabel.add(hostList);
	
	zeroLabel.add(hostName);
	zeroLabel.add(hostList);
	zeroLabel.add(portName);
	zeroLabel.add(portList);
	zeroLabel.add(connButton);
	zeroLabel.add(disconnButton);
	
	//oneLabel.add(openbutton1);
	//oneLabel.add(fileName1);
	oneLabel.add(ontoLabel1);
	oneLabel.add(ontoBox1);

	//twoLabel.add(openbutton2);
	//twoLabel.add(fileName2);
	twoLabel.add(ontoLabel2);
	twoLabel.add(ontoBox2);
	twoLabel.add(ontoRefresh);
	
 	threeLabel.add(strat);
	threeLabel.add(strategy);
 	
	threeLabel.add(mapButton);
	
	//label_three.add(resButton);
	phrasePane.add(minusLabel);
 	phrasePane.add(zeroLabel);
	
 	phrasePane.add(oneLabel);
	phrasePane.add(twoLabel);
	phrasePane.add(threeLabel);
	
    return phrasePane;
    
   }
 

public void createThirdPane(){
Res = createListCharac();
Res.setBorder(BorderFactory.createEmptyBorder (10, 5, 10, 5)  );
}

private JPanel createListCharac () {
	pane2= new JPanel(new GridLayout (0, 1, 0, 10));
return pane2;

}

private void refreshOntoList() {
	
	Vector<String> vec = new Vector<String>();
	
	try {
		
		String[] projects = DatamodelPlugin.getDefault().getOntologyProjects();
		for(int i=0; i < projects.length; i++) {
	    if( ! projects[i].equals(alignProject)) {
	    	NeOnOntoList = DatamodelPlugin.getDefault().getProjectOntologies(projects[i]);
	    	for(int j=0; j < NeOnOntoList.length; j++) {
	    		//System.out.printf("Project Onto = " + st[j] );
	    		vec.add(NeOnOntoList[j]);
	    	}
	    }
		}
		 
	} catch ( Exception ex ) { ex.printStackTrace();};
	
	ontoBox1.removeAllItems();
	ontoBox2.removeAllItems();
	
	ontoList = new String[ vec.size() ];
	for(int j=0; j < vec.size(); j++) {
		
		String str = vec.get(j);
		//rethink !!!
		String str1 = str.substring(1, str.length() - 1);
		int ins = str1.lastIndexOf('/');
		String str2 = str1.substring(0, ins + 1);
		String str3 = str1.substring(ins + 4, str1.length());
		String st = str2.concat(str3);
		
		ontoBox1.addItem(st);
		ontoBox2.addItem(st);
		ontoList[j] = st;
		if(ontoList.length > 0) { 
			selectedOnto1 = ontoList[0];
			selectedOnto2 = ontoList[0];
			selectedNeOnOnto1 = NeOnOntoList[0];
			selectedNeOnOnto2 = NeOnOntoList[0];
		}
		
	}
	 
}

 public void run() {		
		    
		initialize();
	}
 
 
 public static String[] getResultsFromAnswer( String answer, String type ,String separator) {
 	Document doc=null;
 	String[] names=null;
 	//File message=null;
 	System.out.println( "displaying XML ..." );
 	answer =   "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + answer;
 	System.out.println( answer );
 	try {
 	File messageFile = new File("messageFile");
 	FileWriter out = new FileWriter(messageFile);
 	out.write( answer );
 	out.close();
 	
 	} catch (Exception e) {
         //compilerError(Compiler.XML, e.getMessage());
     }
 	
 	try {
 		System.out.println( "parsing ... ");
 		doc = parse(new File("messageFile"));
 		Element e = doc.getDocumentElement();
 		    
 		NodeList methods = e.getElementsByTagName(type);
 		names = new String[methods.getLength()];
 		System.out.println( "length="+ methods.getLength());
 		
 	 
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
 		      System.out.println( "name="+names[i] );
 		      }
 		    }
 		
 		 
 	
 	} catch (Exception e) {
         //compilerError(Compiler.XML, e.getMessage());
 		//System.out.println("getValue="+e.getMessage());
     }
 	return names;
 //System.out.println( answer );
 }
 
 
 public static Vector getCorresFromAnswer( String answer, String type ,String separator) {
 	Document doc=null;
 	Vector names= new Vector();
 	//File message=null;
 	//System.out.println( "displaying XML ..." );
 	answer =   "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + answer;
 	//System.out.println( answer );
 	try {
 	File messageFile = new File("messageFile");
 	FileWriter out = new FileWriter(messageFile);
 	out.write( answer );
 	out.close();
 	
 	} catch (Exception e) {
         //compilerError(Compiler.XML, e.getMessage());
     }
 	
 	try {
 		//System.out.println( "parsing ... ");
 		doc = parse(new File("messageFile"));
 		Element e = doc.getDocumentElement();
 		NodeList m = e.getElementsByTagName("table");    
 		Element cor = (Element)m.item(1);
 		NodeList methods = cor.getElementsByTagName(type);
 		
 		System.out.println( "length lines ="+ methods.getLength());
 		//rethink of this constant
 		
 		
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
 		System.out.println("Error="+e.getMessage());
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




