/*
 * $Id$
 *
 * Copyright (C) INRIA Rhône-Alpes, 2007-2008
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

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.ui.ISharedImages;
//import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;
//import org.eclipse.ui.part.PluginDropAdapter;
//import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.part.ViewPart;
import java.util.ArrayList;

import com.ontoprise.ontostudio.gui.navigator.SelectionTransfer;
//import com.ontoprise.ontostudio.gui.instanceview.InstanceViewContentProvider;
//import com.ontoprise.ontostudio.ontovisualize2.ModuleContentProvider;
import com.ontoprise.ontostudio.gui.navigator.module.ModuleTreeElement;
//import com.ontoprise.ontostudio.gui.navigator.MainTreeDataProvider;
//import com.ontoprise.ontostudio.gui.TreeExtensionHandler;
//import com.ontoprise.ontostudio.datamodel.natures.OntologyProjectNature;


public class NavigationView extends ViewPart {
	public static final String ID = "fr.inrialpes.exmo.align.plugin.neontk.navigationView";
	private TreeViewer viewer;
	//private ITreeDataProvider viewer;
	 
	/**
     * This is a callback that will allow us to create the viewer and initialize
     * it.
     */
	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent);
		viewer.setContentProvider( new ViewContentProvider() );
		//viewer.setContentProvider( new MainTreeDataProvider() );
		//viewer.setLabelProvider( new ViewLabelProvider() );
		//viewer.setInput( createDummyModel() );
        createDropSupport();     
	}
	
	class ViewLabelProvider extends LabelProvider {

		public String getText(Object obj) {
			return obj.toString();
		}
		public Image getImage(Object obj) {
			String imageKey = ISharedImages.IMG_OBJ_ELEMENT;
			if (obj instanceof TreeParent)
			   imageKey = ISharedImages.IMG_OBJ_FOLDER;
			return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
		}
	}
	
	class TreeObject {
		private String name;
		private TreeParent parent;
		
		public TreeObject(String name) {
			this.name = name;
		}
		public String getName() {
			return name;
		}
		public void setParent(TreeParent parent) {
			this.parent = parent;
		}
		public TreeParent getParent() {
			return parent;
		}
		public String toString() {
			return getName();
		}
	}
	
	class TreeParent extends TreeObject {
		private ArrayList<TreeObject> children;
		public TreeParent(String name) {
			super(name);
			children = new ArrayList<TreeObject>();
		}
		public void addChild(TreeObject child) {
			children.add(child);
			child.setParent(this);
		}
		public void removeChild(TreeObject child) {
			children.remove(child);
			child.setParent(null);
		}
		public TreeObject[] getChildren() {
			return (TreeObject[]) children.toArray(new TreeObject[children.size()]);
		}
		public boolean hasChildren() {
			return children.size()>0;
		}
	}

	class ViewContentProvider implements IStructuredContentProvider, 
	   ITreeContentProvider {
		
				public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}	

		public void dispose() {
		}

		public Object[] getElements(Object parent) {
			return getChildren(parent);
		}

		public Object getParent(Object child) {
			if (child instanceof TreeObject) {
				return ((TreeObject)child).getParent();
			}
			return null;
		}

		public Object[] getChildren(Object parent) {
			if (parent instanceof TreeParent) {
				return ((TreeParent)parent).getChildren();
			}
			return new Object[0];
		}

		public boolean hasChildren(Object parent) {
		if (parent instanceof TreeParent)
			return ((TreeParent)parent).hasChildren();
		return false;
	}
	}

	
	private TreeObject createDummyModel() {
        TreeObject to1 = new TreeObject("Inbox");
        TreeObject to2 = new TreeObject("Drafts");
        TreeObject to3 = new TreeObject("Sent");
        TreeParent p1 = new TreeParent("me@this.com");
        p1.addChild(to1);
        p1.addChild(to2);
        p1.addChild(to3);

        TreeObject to4 = new TreeObject("Inbox");
        TreeParent p2 = new TreeParent("other@aol.com");
        p2.addChild(to4);

        TreeParent root = new TreeParent("");
        root.addChild(p1);
        root.addChild(p2);
        return root;
    }
	
	private void createDropSupport() {
        int operations = DND.DROP_MOVE;
        Transfer[] transferTypes = new Transfer[] {
                SelectionTransfer.getInstance()};
        
        //System.out.println("drop support ... ");
        
        //drop support for adding ontology from Ontology Navigator
        viewer.addDropSupport(operations, transferTypes,
                new ViewerDropAdapter(viewer){
        		//new PluginDropAdapter(viewer){
                     //@Override
                     public boolean performDrop(Object data) {
                    	 System.out.println("check 1 ...");
                         if (data instanceof IStructuredSelection) {
                        	 System.out.println("check 2 ...");
                             Object[] elems = ((IStructuredSelection) data).toArray();
                             for (int i = 0; i < elems.length; i++) {
                                 if (elems[i] instanceof TreeItem) {
                                     TreeItem ti = (TreeItem) elems[i];
                                     System.out.println("reading tree ...");
                                     if (ti.getData() instanceof ModuleTreeElement) {
                                    	 
                                         ModuleTreeElement modElement = (ModuleTreeElement) ti.getData();
                                         //NavigationView target = (NavigationView)getCurrentTarget();
                                         //if (target == null){
                                        	 //target = (NavigationView) viewer;
                                        	 //target = (NavigationView)getViewer().getInput();
                                        	 //System.out.println("current viewer null ");
                                         //}
                                         //else System.out.println("current viewer non null ");
                                         System.out.println("Set Element");
                                         viewer.setInput(modElement);
                                         //System.out.println("Id=" + modElement.getModuleId());
                                         //TreeObject root = new TreeObject(modElement);
                                         //viewer.setInput(createDummyModel());
                                         //viewer.add(target, modElement);
                                         //viewer.reveal(modElement);
                                         //return true;
                                     }
                                 }
                             }
                         }
                        return false;
                     }
                     
                     //@Override
                     public boolean validateDrop(Object target, int operation, TransferData transferType) {
                        // check for Drop from Ontology Navigator
                        if (SelectionTransfer.getInstance().isSupportedType(transferType)) {
                            this.setFeedbackEnabled(false);
                            System.out.println(" dropped ok ");
                            return true;
                        }
                        
                        System.out.println("no drop ");
                        
                        return false;
                     }
                });

    }
	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}
}
