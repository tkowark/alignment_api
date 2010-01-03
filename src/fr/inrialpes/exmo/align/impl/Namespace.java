/*
 * $Id$
 *
* Copyright (C) 2006 Digital Enterprise Research Insitute (DERI) Innsbruck
 * Copyright (C) 2005 Digital Enterprise Research Insitute (DERI) Galway
 * Sourceforge version 1.2 - 2008 - then NamespaceDefs.java
 * Copyright (C) INRIA, 2008-2009
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

package fr.inrialpes.exmo.align.impl;

import java.util.HashMap;
import java.util.Map;

// TODO: get shortcut by uri

// JE: for extendibility purposes, it would be useful that this class be something
// else than an enum class.

public enum Namespace {
    // JE: added align as shortcut
    // JE: But latter suppressed because, "" may be used for default namespace
    // JE2009: I one use print(DEF), then it is not needed...
	//ALIGNMENT("http://knowledgeweb.semanticweb.org/heterogeneity/alignment", "align"),
    ALIGNMENT("http://knowledgeweb.semanticweb.org/heterogeneity/alignment", "align", true),
	ALIGNSVC("http://exmo.inrialpes.fr/align/service","alignsvc",true),
	// JE2009: is this never used??
	//OMWG("http://ns.inria.org/edoal/0.9", "omwg", true),
	EDOAL("http://ns.inria.org/edoal/1.0/", "edoal", true),
	DUBLIN_CORE("http://purl.org/dc/elements/1.1/", "dc", false),
	RDF_SCHEMA("http://www.w3.org/2000/01/rdf-schema#", "rdfs", false),
	XSD("http://www.w3.org/2001/XMLSchema#", "xsd", false),
	RDF("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf", false),
	WSML_CORE("http://www.wsmo.org/wsml/wsml-syntax/wsml-core", "wsml", true),
	WSML_FLIGHT("http://www.wsmo.org/wsml/wsml-syntax/wsml-flight", "wsml", true),
	WSML_RULE("http://www.wsmo.org/wsml/wsml-syntax/wsml-rule", "wsml", true),
	WSML_DL("http://www.wsmo.org/wsml/wsml-syntax/wsml-dl", "wsml", true),
	WSML_FULL("http://www.wsmo.org/wsml/wsml-syntax/wsml-full", "wsml", true);

	public final String uri;

	public final String shortCut;

	/**
	 * records if a sharp must be concatenated to the namespace
	 */
	private final boolean addSharp;

	private String prefix;

	private static final Map<String, Namespace> register = new HashMap<String, Namespace>();

	Namespace(final String sUri, final String sShort, final boolean sharp ) {
	    uri = sUri;
	    shortCut = sShort;
	    addSharp = sharp;
	    if ( addSharp )
		prefix = uri+"#";
	    else
		prefix = uri;
	}

	public String getUri() {
		return uri;
	}

	public String getUriPrefix() {
		return prefix;
	}

	public String getShortCut() {
		return shortCut;
	}

	public boolean getSharp() {
		return addSharp;
	}

	/**
	 * Determines a namespace instance depending on it's url.
	 * 
	 * @param url
	 *            the url of the namespace.
	 * @return the namespace instance, or null, if no mathing namespace could be
	 *         found.
	 */
	public static Namespace getNSByUri(final String url) {
	    Namespace result = null;
	    if (register.size() <= 0) {
		for ( Namespace ns : Namespace.values() ) {
		    register.put(ns.getUri(), ns);
		    if ( ns.getUri().equals( url ) ) result = ns;
		}
	    } else { result = register.get(url); }
	    return result;
	}
}
