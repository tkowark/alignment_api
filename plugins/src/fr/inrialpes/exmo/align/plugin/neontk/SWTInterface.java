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

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URI;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;

import javax.swing.*;

import org.semanticweb.owl.align.Parameters;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.semanticweb.owl.align.*;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Parameters;

import fr.inrialpes.exmo.align.impl.BasicParameters;
import fr.inrialpes.exmo.align.impl.OntologyCache;
import fr.inrialpes.exmo.align.impl.renderer.OWLAxiomsRendererVisitor;
import fr.inrialpes.exmo.align.impl.renderer.HTMLRendererVisitor;
 
public class SWTInterface extends JPanel {
	 
	private static JSplitPane _mainSplitter = new JSplitPane (JSplitPane.VERTICAL_SPLIT);

	private URL SOAPUrl = null;
	private String SOAPAction = null;
 
	 
	JLabel hostName, portName;
	JComboBox hostList, portList;
	String selectedHost =  null;
	String selectedPort = null;
	String matching_method = "fr.inrialpes.exmo.align.impl.method.NameEqAlignment";
	String[] hostNames = new String[2];
	String[] ports = new String[2];
	int hostInd, portInd;

	AlignmentClient ws = null;
	//Procalign procalign = null;
	//Parameters p = new BasicParameters();

	public JComponent phrases, phrases1, Res=null;
    public JTextField fileName, fileName2, treshold;
    public SWTInterface frame;
    public JFileChooser open;
    public JLabel explain;
    public JPanel pane;
    public JButton openbutton1, openbutton2, mapButton, resButton, 
    		connButton, offlineButton, onlineButton, disconnButton ;
    public JPanel pane2;
	public Component result=null;
	public JTextArea jan=null;
    JComboBox strategy, renderer, iterations, autoint, boxeff, boxscen;
    JLabel strat, render, iterat, auto, effic, scen, tres;
    boolean autointerations=false, efficients=true;
    String aux_tres;
    public String [] aux = new String[5];
    public String [] methodList = new String[0];
    public String [] rendererList = null;
    public String [] alignList = null;
    public Vector 	 corrList = new Vector();
    public String [] aservArgs = null;
    public String [] aservArgRetrieve = null;
    public String [] aservArgAlign = null;
    public String [] files = new String[2];
    int ind=0, rend=0, iter=0, aut=0, eff=0, retscen=0, count=0;
	
    boolean online = false;
	//boolean chosenPort = false;
    
    public void initialize() {

	 
	//aux[2] = "fr.inrialpes.exmo.align.impl.method.NameEqAlignment";
	//aux[3] = "fr.inrialpes.exmo.align.impl.renderer.SKOSRendererVisitor";
	//String text = "Plugin to Mapping between Ontologies.                                                                        ";
	
	//selectedHost = "aserv.inrialpes.fr";
	//selectedPort = "8089";
	
    offlineButton = new JButton("Offline",null);
    offlineButton.addActionListener(new ActionListener(){
    public void actionPerformed(ActionEvent e) {
    		if (e.getSource() == offlineButton) {
    			
    			online = false;
    			openbutton1.setEnabled(true);
				openbutton2.setEnabled(true);
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
				
				//methodList[8] = "fr.inrialpes.exmo.align.impl.method.JWNLAlignment";
				//strategy.addItem("JWNLAlignment");
				
				 
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
				openbutton1.setEnabled(false);
				openbutton2.setEnabled(false);
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
    			openbutton1.setEnabled(false);
				openbutton2.setEnabled(false);
				strategy.setEnabled(false);
				mapButton.setEnabled(false);
				disconnButton.setEnabled(false);
				hostList.setEnabled(false);
				portList.setEnabled(false);
				connButton.setEnabled(false);
    		}
    	
        };
    	});
    	
	hostNames[0] = "aserv.inrialpes.fr";
	selectedHost = hostNames[0];
	ports[0] = "8089";
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
        			 ws = new AlignmentClient(selectedPort, selectedHost);
        			
        				 	aservArgs = new String[4];
        				    
        					aservArgs[0]="-o";
        					//File anwser = new File ("anwser.txt");
        					aservArgs[1]="answer.txt";
        				 
        					aservArgs[2]="list";
        					aservArgs[3]="methods";
        				     
        					try {
        						// Read parameters
        						 
        						Parameters params = ws.readParameters( aservArgs );
        						
        						// Create the SOAP message
        						String message = ws.createMessage( params );
        						  
        						System.out.println("URL SOAP :"+ws.SOAPUrl+ ",  Action:"+ ws.SOAPAction);
        						System.out.println("Message :"+ message);
        						
        						// Send message
        						String answer = ws.sendMessage( message, params );
        						if(ws.connected == false)
        							JOptionPane.showMessageDialog(null, "Impossible Connection !","Warning",2);
        						else{
        							// Displays it
        							String[] list =  getResultsFromAnswer( answer, "method", "." );
        							methodList = new String[list.length];
        							strategy.removeAllItems();
        							for(int i=0; i< list.length; i++){
        								if(selectedHost.equals("aserv.inrialpes.fr")) {
        							    methodList[i]= "fr.inrialpes.exmo.align.impl.method." + list[i];
        								strategy.addItem(list[i]);
        								}
        							}
        						
        							openbutton1.setEnabled(true);
        							openbutton2.setEnabled(true);
        							strategy.setEnabled(true);
        							mapButton.setEnabled(true);
        							disconnButton.setEnabled(true);
        							
        							hostList.setEnabled(false);
        							portList.setEnabled(false);
        							connButton.setEnabled(false);
        							
        						}
        						 
        					}
        					catch ( Exception ex ) { ex.printStackTrace(); };
        				
            	}
			
		};
        });
	
	
	open= new JFileChooser();
	ExampleFileFilter filter = new ExampleFileFilter();
    	filter.addExtension("owl");
    	filter.setDescription("Protégé Files");
    	open.setFileFilter(filter);
	openbutton1 = new JButton("Open Ontology 1...",null);
	openbutton1.setEnabled(false);
	openbutton1.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent e) {
        		if (e.getSource() == openbutton1) {
				int returnVal = open.showOpenDialog(frame);
            			if (returnVal == JFileChooser.APPROVE_OPTION) {
                		File file = open.getSelectedFile();
				String end = file.getAbsolutePath();
				fileName.setText(end);
				aux[0]=end;
		 
            			}
			}
		};
        });
	
	openbutton2 = new JButton("Open Ontology 2...",null);
	openbutton2.setEnabled(false);
	openbutton2.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent e) {
        		if (e.getSource() == openbutton2) {
            			int returnVal2 = open.showOpenDialog(frame);
            			if (returnVal2 == JFileChooser.APPROVE_OPTION) {
				File file = open.getSelectedFile();
				String end2 = file.getAbsolutePath();	
				fileName2.setText(end2);
				aux[1]=end2;
		 
            	}
			}
		};
        });
	
	mapButton = new JButton("MAP",null);	
	mapButton.setEnabled(false);
	mapButton.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent e) {
		   if (e.getSource() == mapButton) {
		       if (aux[0]=="" || aux[1]==""){JOptionPane.showMessageDialog(null, "Where are the ontologies?","Warning",2);}
			   else {
				   	 
				  String[] fileNames = new String[2];
				  String[] physicalURIs = new String[2];
				  Vector uris = new Vector(); 
				  fileNames[0] = aux[0];
				  fileNames[1] = aux[1];
				   
				  System.out.println("onto 1"+ fileNames[0]);
				  System.out.println("onto 2"+ fileNames[1]);
				  
				  for (int i = 0; i< fileNames.length; i++) {
						if (fileNames[i].startsWith("http://")==true) {
							
							physicalURIs[i] = fileNames[i];
						} else {
							
							physicalURIs[i] = "file:"+ fileNames[i];
							 
							}
				  }
				  
				  try {
					  uris.add( new URI(physicalURIs[0]) );
					  uris.add( new URI(physicalURIs[1]) );
				  }
				  catch  ( Exception ex ) { ex.printStackTrace(); };
				  
					 
				  
				  if(online == false) {
					  
				      //OWLOntology onto1 = null;
				      //OWLOntology onto2 = null;
				      Parameters p = new BasicParameters();
				      AlignmentProcess A1 = null;
					  
				      try {
					  //OWLRDFParser parser = new OWLRDFParser();
					  //parser.setConnection(OWLManager.getOWLConnection());
					  //onto1 =  parser.parseOntology((URI)uris.get(0));
					  //onto2 =  parser.parseOntology((URI)uris.get(1));
					  
					  try {
					      // Create alignment object
					      Object[] mparams = {};
					      Class alignmentClass = Class.forName(matching_method);
					      Class[] cparams = {};
					      java.lang.reflect.Constructor alignmentConstructor = alignmentClass.getConstructor(cparams);
					      A1 = (AlignmentProcess)alignmentConstructor.newInstance(mparams);
					      A1.init( (URI)uris.get(0), (URI)uris.get(1), (OntologyCache)null );
					  } catch (Exception ex) {
					      System.err.println("Cannot create alignment "+matching_method+"\n"+ex.getMessage());
					      throw ex;
					  }

					  A1.align((Alignment)null,p);

					  //for storing
					  FileWriter owlF = new FileWriter(new File( "align.owl"));
					  AlignmentVisitor V = new OWLAxiomsRendererVisitor(  new PrintWriter ( owlF )  );
							     
					  A1.render(V);
					  owlF.close();
					  
					  
					  //for displaying
					  FileWriter htmlF = new FileWriter( "align.html" );
					  AlignmentVisitor V1 = new HTMLRendererVisitor(
							    new PrintWriter ( htmlF ) );
					  
					  A1.render(V1);
					  htmlF.close();
					  
					  String htmlString = fileToString(new File ("align.html"));
					  
					  System.out.println("htmlString =" + htmlString);
					  
					  corrList = getCorresFromAnswer( htmlString, "tr", "#" );
					  
					  } catch ( Exception ex ) { ex.printStackTrace(); };
					  
					  
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
				  else {
					
				    aservArgAlign = new String[6];			
					aservArgAlign[0]="-o";
					//File anwser = new File ("anwser.txt");
					aservArgAlign[1]="answer.txt";
				 
					aservArgAlign[2]="match";
					
					//aservArgAlign[3]= "fr.inrialpes.exmo.align.impl.method.StringDistAlignment";
					aservArgAlign[3]= matching_method;
					aservArgAlign[4]= physicalURIs[0];
					aservArgAlign[5]= physicalURIs[1];
				     
					try {
						// Read parameters
						 
						Parameters params = ws.readParameters( aservArgAlign );
						
						// Create the SOAP message
						String message = ws.createMessage( params );
						  
						System.out.println("URL SOAP :"+ws.SOAPUrl+ ",  Action:"+ ws.SOAPAction);
						System.out.println("Message :"+ message);
						
						// Send message
						String answer = ws.sendMessage( message, params );
						 
						// Displays it
							System.out.println("alignId="+answer);
							alignList = getResultsFromAnswer( answer, "alid", null );
						 
						
					}
					catch ( Exception ex ) { ex.printStackTrace(); };
					
					//retrieve alignment for storing in OWL file
					
					aservArgRetrieve = new String[5];
					aservArgRetrieve[0] = "-o";
					//File anwser = new File ("anwser.txt");
					aservArgRetrieve[1] = "answer.txt";
				 
					aservArgRetrieve[2] = "retrieve";
					//System.out.println("alignIdList="+alignList[0]);
					aservArgRetrieve[3]= alignList[0];
					aservArgRetrieve[4]="fr.inrialpes.exmo.align.impl.renderer.OWLAxiomsRendererVisitor";
					//aservArgRetrieve[4]="fr.inrialpes.exmo.align.impl.renderer.HTMLRendererVisitor";
					//aservArgAlign[4]=aux[1];
				     
					try {
						// Read parameters
						 
						Parameters params = ws.readParameters( aservArgRetrieve );
						
						// Create the SOAP message
						String message = ws.createMessage( params );
						  
						System.out.println("URL SOAP :"+ws.SOAPUrl+ ",  Action:"+ ws.SOAPAction);
						System.out.println("Message :"+ message);
						
						// Send message
						String answer = ws.sendMessage( message, params );
						 
						 
						// Cut SOAP header 
						
						String []  cutResult = answer.split("result");
						
							String str = "";
						
							for(int i= 0; i< cutResult.length; i++){
								if(i >= 1 && i <= cutResult.length -2)
								str = str + cutResult[i];
							}
						
						String strResult = str.substring(1, str.length() - 3);
						
						//Store it
						 
				    	
						Document doc=null;
						FileWriter out = null;
				    	
						try {
								File owlFile = new File( "align.owl");
								out = new FileWriter( owlFile );
								out.write( strResult );
								out.close();
							} catch (Exception ex) {}
						
						
				    	
					    }
						catch ( Exception ex ) { ex.printStackTrace(); System.out.println("problem  of align. retrieve");};
					
						//retrieve alignment for displaying
						
						aservArgRetrieve = new String[5];
						aservArgRetrieve[0] = "-o";
						//File anwser = new File ("anwser.txt");
						aservArgRetrieve[1] = "answer.txt";
					 
						aservArgRetrieve[2] = "retrieve";
						//System.out.println("alignIdList="+alignList[0]);
						aservArgRetrieve[3]= alignList[0];
						//aservArgRetrieve[4]="fr.inrialpes.exmo.align.impl.renderer.OWLAxiomsRendererVisitor";
						aservArgRetrieve[4]="fr.inrialpes.exmo.align.impl.renderer.HTMLRendererVisitor";
						//aservArgAlign[4]=aux[1];
						
						try {
							// Read parameters
							 
							Parameters params = ws.readParameters( aservArgRetrieve );
							
							// Create the SOAP message
							String message = ws.createMessage( params );
							  
							System.out.println("URL SOAP :"+ws.SOAPUrl+ ",  Action:"+ ws.SOAPAction);
							System.out.println("Message :"+ message);
							
							// Send message
							String answer = ws.sendMessage( message, params );
							 
							  
							corrList = getCorresFromAnswer( answer, "tr", "#" );
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
							catch ( Exception ex ) { ex.printStackTrace(); System.out.println("problem  of align. retrieve");};
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
	*/
	
	fileName = new JTextField(60);
        fileName.setEnabled(true);
        fileName.setHorizontalAlignment(SwingConstants.LEFT);
	
	
	fileName2 = new JTextField(60);
        fileName2.setEnabled(true);
        fileName2.setHorizontalAlignment(SwingConstants.LEFT);
	
        
	strat= new JLabel("Alignment methods");
	strategy = new JComboBox(methodList);
	strategy.setEnabled(false);
	strategy.setMaximumRowCount(20);
	strategy.addItemListener(new ItemListener() {
		public void itemStateChanged(ItemEvent event)
		{
		  if (event.getStateChange()==ItemEvent.SELECTED)
			ind = strategy.getSelectedIndex();
		    
		    matching_method =  methodList[ind];
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
     //phrases_pane = new JPanel (new GridLayout (0, 1, 0, 10));
     /*
     String [] modes = PluginModes.getModes();
	
     //for (int i = 0; i < 2; i++) {
     for (int i = 0; i < 1; i++) {
       JPanel phrases_Panel = new JPanel (new BorderLayout ());
       phrases_Panel.add(new JLabel(""), BorderLayout.WEST);
       phrases_Panel.add(createPhraseLabel (modes[i]), BorderLayout.CENTER);
       phrases_pane.add(phrases_Panel);
     }
    */
     
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
	
	oneLabel.add(openbutton1);
	oneLabel.add(fileName);

	twoLabel.add(openbutton2);
	twoLabel.add(fileName2);
	 
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

private JComponent createPhraseLabel (String mode) {
	JLabel label = new JLabel ();
       String fontFamily = label.getFont().getFamily();
       String labelText = ""+mode + PluginModes.getDetails(mode)+" ";
       	label.setText(labelText);
       return label;
   }

private JTextArea openFile(File f){
        JTextArea texto = new JTextArea();
	int i=0;
   try{
       Font fon= new Font("Font",Font.BOLD,15);
       FileReader rd = new FileReader(f);
       i = rd.read();
       String ret="";
         while(i!=-1){
              ret = ret+(char)i;
              i = rd.read();
         }
	texto.setText(ret);
	texto.setFont(fon);
	
       }catch(IOException e){
        System.out.println(e.getMessage());
         }
  return texto;
 }

private String fileToString(File f){
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

private Document parse(File f) throws Exception {
    try {
      String uri = f.toURL().toString();
      return parse(uri);
    } catch (MalformedURLException ex) {
      //compilerError(Compiler.FILE, ex.getMessage());
    }
    return null;
  }

private Document parse(String uri) throws Exception {
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

public Vector getCorresFromAnswer( String answer, String type ,String separator) {
	Document doc=null;
	Vector names= new Vector();
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

public String[] getResultsFromAnswer( String answer, String type ,String separator) {
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
 public void run() {		
		    
		initialize();
	}
 
 
}




