/*
 * Copyright (C) 2007 by Quentin Anciaux
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

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is a context bound to a thread
 * 
 * @author Quentin Anciaux
 * @version 0.94
 */
public final class CThreadContext {
	/** A thread context is thread bound */
	private static final Map contextLocal = new HashMap();

	/** Contains attributes set in this context */
	private final Map valueMap = new HashMap();

	/** mutex for thread safety */
	private final CMutex mutex = new CMutex();

	/**
	 * Return a thread context
	 * 
	 * @return a thread context
	 */
	public static final CThreadContext getInstance() {
		SoftReference ref = (SoftReference)CThreadContext.contextLocal
				.get(Thread.currentThread());
		CThreadContext context = null;
		if ((ref == null) || (ref.get() == null)) {
			context = new CThreadContext();
			ref = new SoftReference(context);
			CThreadContext.contextLocal.put(Thread.currentThread(), ref);
		} else {
			context = (CThreadContext)ref.get();
		}
		return context;
	}

	public static void destroy() {
		CThreadContext.contextLocal.clear();
	}

	/**
	 * Get the value with the given key from this context
	 * 
	 * @param key
	 *            the key to lookup
	 * 
	 * @return the value with the given key from this context
	 */
	public final Object get(String key) {
		try {
			this.mutex.acquire();

			return this.valueMap.get(key);
		} finally {
			try {
				this.mutex.release();
			} catch (Exception ignore) {
			}
		}
	}

	/**
	 * Set a value in this context
	 * 
	 * @param key
	 *            the key to save the value on
	 * @param value
	 *            the value to set
	 */
	public final void set(String key, Object value) {
		try {
			this.mutex.acquire();
			if (value == null) {
				this.valueMap.remove(key);
			} else {
				this.valueMap.put(key, value);
			}
		} finally {
			try {
				this.mutex.release();
			} catch (Exception ignore) {
			}
		}
	}
}
