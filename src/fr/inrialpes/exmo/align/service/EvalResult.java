/*
 * $Id$
 *
 * Copyright (C) INRIA Rhône-Alpes, 2006-2008
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package fr.inrialpes.exmo.align.service;

import org.semanticweb.owl.align.Parameters;

/**
 * Contains the messages that should be sent according to the protocol
 */

public class EvalResult extends Success {

    public EvalResult ( int surr, Message rep, String from, String to, String cont, Parameters param ) {
	super( surr, rep, from, to, cont, param );
    }
    public String HTMLString(){
	// This can only rely on a HTML output of the Evaluator
	return "Alignment ID: <a href=\"../html/retrieve?method=fr.inrialpes.exmo.align.impl.renderer.HTMLRendererVisitor&id="+getContent()+"\">"+getContent()+"</a>";
    }
    public String SOAPString(){
	// This can only rely on a XML output of the Evaluator
	// We have one through the write( PrintWriter ) method
	// This should go to content
	//return "<id>"+surrogate+"</id>"+"<sender>"+sender+"</sender>" + "<receiver>"+receiver+"</receiver>" + "<in-reply-to>" + inReplyTo+ "</in-reply-to>" + "<alid>" + content + "</alid>";	
	return "<id>"+surrogate+"</id>"+"<in-reply-to>"+inReplyTo+"</in-reply-to><alid>"+content+"</alid>";	
    }

}
