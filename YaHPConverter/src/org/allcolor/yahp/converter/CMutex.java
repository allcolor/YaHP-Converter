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

import java.lang.ref.WeakReference;

import java.util.Arrays;
import java.util.Comparator;

/**
 * This is a simple mutex (Mutual Exclusion) class, which permits to synchronize
 * Thread access to some parts of the code. Example usage : CMutex m = new
 * CMutex(); private void mymethod() { m.acquire(); try { do something... }
 * finally { m.release(); } }
 * 
 * @author Quentin Anciaux
 * @version 0.94
 */
public final class CMutex {
	/**
	 * This class is used to sort the mutex when acquiring multiple
	 * 
	 * @author Quentin Anciaux
	 * @version 1
	 */
	private static final class CMutexComparator implements Comparator {
		/**
		 * Compare two mutex
		 * 
		 * @param o1
		 *            mutex 1
		 * @param o2
		 *            mutex 2
		 * 
		 * @return id value difference
		 */
		public int compare(Object o1, Object o2) {
			CMutex m1 = (CMutex) o1;
			CMutex m2 = (CMutex) o2;

			return m1.id - m2.id;
		}
	}

	/** used for serial access to the static id (sid) */
	private static final Object IDLOCK = new Object();

	/** used to create unique id */
	private static int sid = 0;

	/**
	 * Acquires an array of mutex
	 * 
	 * @param mutex
	 *            the mutex array to acquire
	 */
	public static final void acquireMultiple(CMutex mutex[]) {
		Arrays.sort(mutex, new CMutexComparator());

		for (int i = 0; i < mutex.length; i++) {
			mutex[i].acquire();
		}
	}

	/**
	 * Acquires an array of mutex
	 * 
	 * @param timeout
	 *            maximum time to wait
	 * @param mutex
	 *            the mutex array to acquire
	 * 
	 * @throws RuntimeException
	 *             if timeout is reached
	 */
	public static final void acquireMultiple(long timeout, CMutex mutex[])
			throws RuntimeException {
		Arrays.sort(mutex, new CMutexComparator());
		for (int i = 0; i < mutex.length; i++) {
			try {
				mutex[i].acquire(timeout);
			} catch (RuntimeException re) {
				// release successfuly acquired mutex.
				for (int j = i - 1; j >= 0; j--) {
					if (mutex[j].isOwner()) {
						mutex[j].release();
					}
				}
				throw re;
			}
		}
	}

	/**
	 * Releases an array of mutex previously acquired
	 * 
	 * @param mutex
	 *            the mutex array to release
	 */
	public static final void releaseMultiple(CMutex mutex[]) {
		Arrays.sort(mutex, new CMutexComparator());

		for (int i = mutex.length - 1; i >= 0; i--) {
			mutex[i].release();
		}
	}

	/**
	 * number of time the owner has acquired this mutex
	 */
	private int acquire = 0;

	/** id of the mutex, used for sorting */
	private int id = -1;

	/** used to implement the locking mechanism */
	private final Object LOCK = new Object();

	/** owner of the mutex */
	private WeakReference owner = null;

	/**
	 * Creates a new CMutex object.
	 */
	public CMutex() {
		synchronized (CMutex.IDLOCK) {
			this.id = CMutex.sid++;
		}
	}

	/**
	 * Acquires this mutex
	 */
	public final void acquire() {
		boolean cont = true;

		while (cont) {
			try {
				cont = false;
				this.acquire(-1);
			} catch (RuntimeException te) {
				cont = true;
			}
		}
	}

	/**
	 * Acquires this mutex
	 * 
	 * @param timeout
	 *            maximum time to wait for acquiring
	 * 
	 * @throws RuntimeException
	 *             if timeout is reached and no acquire was possible.
	 */
	public final void acquire(long timeout) throws RuntimeException {
		synchronized (this.LOCK) {
			long start = System.currentTimeMillis();

			while (this.owner != null) {
				if (this.owner.get() == Thread.currentThread()) {
					this.acquire++;

					return;
				}

				try {
					if (timeout == -1) {
						this.LOCK.wait();
					} else {
						this.LOCK.wait(timeout);
					}
				} catch (InterruptedException e) {
					//
				}

				long end = System.currentTimeMillis();

				if ((this.owner != null) && (timeout != -1)
						&& ((end - start) >= timeout)) {
					throw new RuntimeException("TimeoutException");
				}
			}

			this.owner = new WeakReference(Thread.currentThread());
			this.acquire++;
		}
	}

	/**
	 * return true if Thread.currentThread is the owner of the mutex.
	 * 
	 * @return true if Thread.currentThread is the owner of the mutex.
	 */
	public final boolean isOwner() {
		synchronized (this.LOCK) {
			return ((this.owner != null) && (this.owner.get() == Thread
					.currentThread()));
		}
	}

	/**
	 * Releases the mutex
	 * 
	 * @throws IllegalMonitorStateException
	 *             If currentThread is not the owner.
	 */
	public final void release() {
		synchronized (this.LOCK) {
			if ((this.owner != null)
					&& (this.owner.get() == Thread.currentThread())) {
				this.acquire--;

				if (this.acquire == 0) {
					this.owner = null;
					this.LOCK.notify();
				}

				return;
			}

			if ((this.owner != null) && (this.owner.get() == null)) {
				this.owner = null;
				this.acquire = 0;
				this.LOCK.notify();
			}

			throw new IllegalMonitorStateException(Thread.currentThread()
					.getName()
					+ " is not the owner of the lock. "
					+ ((this.owner != null) ? ((Thread) this.owner.get())
							.getName() : "nobody") + " is the owner.");
		}
	}
}