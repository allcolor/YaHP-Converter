/*
 * Copyright (C) 2005 by Quentin Anciaux
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Library General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Library General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 *	@author Quentin Anciaux
 */
package org.allcolor.yahp.converter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.net.URL;
import java.net.URLConnection;


/**
 * DOCUMENT ME!
 *
 * @author Quentin Anciaux
 * @version v0.94
 */
public class CByteArrayUrlConnection
	extends URLConnection {
	/** DOCUMENT ME! */
	private InputStream in;

	public CByteArrayUrlConnection(
			final URL	   url,
			byte [] in) {
			super(url);
			this.in	   = new ByteArrayInputStream(in);
		} // end CByteArrayUrlConnection()

	/**
	 * DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 *
	 * @throws IOException DOCUMENT ME!
	 */
	public InputStream getInputStream()
		throws IOException {
		return in;
	} // end getInputStream()

	/**
	 * DOCUMENT ME!
	 *
	 * @throws IOException DOCUMENT ME!
	 */
	public void connect()
		throws IOException {
	} // end connect()
} // end CByteArrayUrlConnection
