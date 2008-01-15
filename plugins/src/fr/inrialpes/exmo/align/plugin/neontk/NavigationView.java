package fr.inrialpes.exmo.align.plugin.neontk;
import java.util.ArrayList;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
//import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.ontoprise.ontostudio.gui.navigator.SelectionTransfer;
import com.ontoprise.ontostudio.gui.navigator.module.ModuleTreeElement;


public class NavigationView extends ViewPart {
	public static final String ID = "fr.inrialpes.exmo.align.plugin.neontk.navigationView";
	private TreeViewer viewer;
	 
	/**
     * This is a callback that will allow us to create the viewer and initialize
     * it.
     */
	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent);
        createDropSupport();     
		 
	}
    
	private void createDropSupport() {
        int operations = DND.DROP_MOVE;
        Transfer[] transferTypes = new Transfer[] {
                SelectionTransfer.getInstance()};

        // drop support for adding ontology from Ontology Navigator
        viewer.addDropSupport(operations, transferTypes,
                new ViewerDropAdapter(viewer){
                     @Override
                     public boolean performDrop(Object data) {
                         if (data instanceof IStructuredSelection) {
                             Object[] elems = ((IStructuredSelection) data).toArray();
                             for (int i = 0; i < elems.length; i++) {
                                 if (elems[i] instanceof TreeItem) {
                                     TreeItem ti = (TreeItem) elems[i];
                                     if (ti.getData() instanceof ModuleTreeElement) {
                                         ModuleTreeElement modElement = (ModuleTreeElement) ti.getData();
//                                         _tv.setInput(modElement.getModuleId());
                                         return true;
                                     }
                                 }
                             }
                         }
                        return false;
                     }
                     
                     @Override
                     public boolean validateDrop(Object target, int operation, TransferData transferType) {
                        // check for Drop from Ontology Navigator
                        if (SelectionTransfer.getInstance().isSupportedType(transferType)) {
                            this.setFeedbackEnabled(false);
                            return true;
                        }
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