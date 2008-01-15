package fr.inrialpes.exmo.align.plugin.neontk;

import org.eclipse.jface.action.IAction;
//import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
//import org.eclipse.jface.viewers.TreeSelection;
//import org.eclipse.swt.SWT;
//import org.eclipse.swt.SWTError;
//import org.eclipse.swt.browser.Browser;
//import org.eclipse.swt.browser.LocationEvent;
//import org.eclipse.swt.browser.LocationListener;
//import org.eclipse.swt.browser.ProgressEvent;
//import org.eclipse.swt.browser.ProgressListener;
//import org.eclipse.swt.browser.StatusTextEvent;
//import org.eclipse.swt.browser.StatusTextListener;
//import org.eclipse.swt.layout.GridData;
//import org.eclipse.swt.layout.GridLayout;
//import org.eclipse.swt.widgets.Display;
//import org.eclipse.swt.widgets.Event;
//import org.eclipse.swt.widgets.Label;
//import org.eclipse.swt.widgets.Listener;
//import org.eclipse.swt.widgets.MessageBox;
//import org.eclipse.swt.widgets.ProgressBar;
//import org.eclipse.swt.widgets.Shell;
//import org.eclipse.swt.widgets.Text;
//import org.eclipse.swt.widgets.ToolBar;
//import org.eclipse.swt.widgets.ToolItem;
//import org.eclipse.ui.IFolderLayout;
//import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
//import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.IViewPart;
 

 

/**
 * Our sample action implements workbench action delegate.
 * The action proxy will be created by the workbench and
 * shown in the UI. When the user tries to use the action,
 * this delegate will be created and execution will be 
 * delegated to it.
 * @see IWorkbenchWindowActionDelegate
 */
public class AlignAction implements IWorkbenchWindowActionDelegate {
	private IWorkbenchWindow window;
	//private Shell shell;
	//private Browser browser;
	private IViewPart view;
	
	/**
	 * The constructor.
	 */
	public AlignAction() {
		 
	 
	}

	/**
	 * The action has been activated. The argument of the
	 * method represents the 'real' action sitting
	 * in the workbench UI.
	 * @see IWorkbenchWindowActionDelegate#run
	 */
	public void run(IAction action) {
		IViewPart view = (IViewPart)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(View.ID);
		try{
		window.getActivePage().showView(View.ID);
		}
		catch(Exception e) {
			
		}
	}
	
    /**
	 * Selection in the workbench has been changed. We 
	 * can change the state of the 'real' action here
	 * if we want, but this can only happen after 
	 * the delegate has been created.
	 * @see IWorkbenchWindowActionDelegate#selectionChanged
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}

	/**
	 * We can use this method to dispose of any system
	 * resources we previously allocated.
	 * @see IWorkbenchWindowActionDelegate#dispose
	 */
	public void dispose() {
	}

	/**
	 * We will cache window object in order to
	 * be able to provide parent shell for the message dialog.
	 * @see IWorkbenchWindowActionDelegate#init
	 */
	public void init(IWorkbenchWindow window) {
		this.window = window;
		IViewPart view = (IViewPart)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(View.ID);
		try{
		window.getActivePage().showView(View.ID);
		}
		catch(Exception e) {
			
		}
		
	}
	
	private void initBrowser2() {
	     
	}
}