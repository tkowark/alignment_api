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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.swt.awt.SWT_AWT;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import java.awt.Frame;
 
public class View extends ViewPart {

	public static final String ID = "fr.inrialpes.exmo.align.plugin.neontk.view";
	 	
	public void setFocus() {
	}
	
	public void createPartControl(Composite parent) {	
		final Composite composite = new Composite(parent, SWT.EMBEDDED);
		final Frame f = SWT_AWT.new_Frame(composite);
	 
		JPanel panel = new JPanel(new BorderLayout());
		f.add(panel);
		SWTInterface  lo= new SWTInterface();
		lo.run();
		f.add(lo);	
	}
	
}
