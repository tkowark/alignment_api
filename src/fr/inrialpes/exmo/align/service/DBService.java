/*
 * $Id$
 *
 * Copyright (C) XX, 2006
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
import org.semanticweb.owl.align.Alignment;

public interface DBService {
	public long store(Alignment alignment);
	public Alignment find(long id);
	public int connect(String password);                              // password in database
    // JE: to be added
    //public int init(String IPAdress, String id, String password);    // with userID, password in database
	public int close();
}
