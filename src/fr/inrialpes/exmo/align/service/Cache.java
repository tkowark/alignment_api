/*
 * $Id$
 *
 * Copyright (C) Seungkeun Lee, 2006
 * Copyright (C) INRIA Rhône-Alpes, 2006-2007
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

import java.net.URI;
import java.util.Set;
import java.sql.SQLException;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Parameters;

public interface Cache {
    void init( Parameters p ) throws SQLException;
    Alignment getMetadata( String id ) throws Exception;
    Alignment getAlignment( String id ) throws Exception;
    Set getAlignments( URI uri );
    Set getAlignments( URI uri1, URI uri2 );
    String recordNewAlignment( Alignment alignment, boolean force );
    void storeAlignment( String id ) throws Exception;
}
