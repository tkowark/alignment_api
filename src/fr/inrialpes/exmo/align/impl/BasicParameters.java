/*
 * $Id$
 *
 * Copyright (C) INRIA Rhône-Alpes, 2004
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 */

package fr.inrialpes.exmo.align.impl; 

// import java classes
import java.util.Hashtable;
import java.util.Enumeration;

import org.semanticweb.owl.align.Parameters;

/**
  *
 * @author Jérôme Euzenat
 * @version $Id$ 
 */

public class BasicParameters implements Parameters {
 
  /** The list of unlinked out  XML_Port */
  Hashtable parameters = null;
    
  public BasicParameters() {
    parameters = new Hashtable();
  }
  
  public void setParameter(String name, Object value){
    parameters.put((Object)name,value);
  }

  public void unsetParameter(String name){
    parameters.remove((Object)name);
  }

  public Object getParameter(String name){
    return parameters.get((Object)name);
  }

  public Enumeration getNames(){
    return parameters.keys();
  }

    public void write(){
	System.out.println("<?xml version='1.0' ?>");
	System.out.println("<Parameters>");
	for (Enumeration e = parameters.keys(); e.hasMoreElements();) {
	    String k = (String)e.nextElement();
	    System.out.println("  <param name='"+k+"'>"+parameters.get(k)+"</param>");
	}
	System.out.println("</Parameters>");
    }

    public static Parameters read(){
	Parameters p = new BasicParameters();
	// open the stream
	// parse it
	// fill the structure
	return p;
    }
}
