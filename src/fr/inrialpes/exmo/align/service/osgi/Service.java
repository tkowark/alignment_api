/*
 * $Id$
 *
 * Copyright (C) INRIA, 2013
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

package fr.inrialpes.exmo.align.service.osgi;

import java.util.Set;
import java.util.Collection;
import java.net.URI;

import org.semanticweb.owl.align.Alignment;

import fr.inrialpes.exmo.align.service.msg.Message;

/**
 * So far, AServProtocol implements directly this service interface
 * No additional code is necessary.
 * However, it may be more useful to provide an implementation that
 * decodes all messages in Java objects.
 * This may be necessary as a second step.
 */
public interface Service {

    public Set<String> listmethods ();

    public Set<String> listrenderers();

    public Set<String> listservices();

    public Set<String> listevaluators();

    public Collection<Alignment> alignments();

    public Collection<URI> ontologies();

    public Collection<Alignment> alignments( URI uri1, URI uri2 );

    public String query( String query );

    public String serverURL();

    public String argline();

   /*********************************************************************
     * Basic protocol primitives
     *********************************************************************/

    public Message load( Message mess );

    public Message align( Message mess );

    public Message existingAlignments( Message mess );

    public Message findCorrespondences( Message mess );

    public Message find( Message mess );

    public Message translate( Message mess );

    public Message render( Message mess );

    /*********************************************************************
     * Extended protocol primitives
     *********************************************************************/

    public Message store( Message mess );

    //public Message erase( Message mess );

    public Message metadata( Message mess );

    /*********************************************************************
     * Extra alignment primitives
     *
     * All these primitives must create a new alignment and return its Id
     * There is no way an alignment server could modify an alignment
     *********************************************************************/

    public Message trim( Message mess );

    public Message harden( Message mess );

    public Message inverse( Message mess );

    public Message meet( Message mess );

    public Message join( Message mess );

    public Message compose( Message mess );

    public Message eval( Message mess );

    public Message diff( Message mess );

    public boolean storedAlignment( Message mess );

    /*********************************************************************
     * Network of alignment server implementation
     *********************************************************************/

    /*********************************************************************
     * Utilities: reaching and loading ontologies
     *********************************************************************/

    //public LoadedOntology reachable( URI uri );

}
