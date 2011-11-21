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

import java.beans.Introspector;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.net.URLStreamHandlerFactory;

import java.security.AccessControlContext;
import java.security.ProtectionDomain;

import java.sql.Driver;
import java.sql.DriverManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.LookAndFeel;
import javax.swing.UIManager;

/**
 * This is a custom tree classloader
 * 
 * @author Quentin Anciaux
 * @version 1.0
 */
public final class CClassLoader extends URLClassLoader {
	/** classloader namespace */
	public static final String CCLASSLOADER_NAMESPACE = "org.allcolor::CClassLoader.loadClass::";

	private static ThreadLocal contextLoader = new ThreadLocal();

	/** DEBUG log level */
	private static final int DEBUG = 1;

	/** FATAL log level */
	private static final int FATAL = 2;

	/** INFO log level */
	private static final int INFO = 0;

	/** use for logging */
	private static final Logger log = Logger.getLogger(CClassLoader.class
			.getName());

	/** contains all mandatory loaders */
	private static final Map mandatoryLoadersMap = new HashMap();

	/** name of the rootloader */
	public static final String ROOT_LOADER = "rootLoader";

	/** handle to the root loader */
	private static CClassLoader rootLoader = CClassLoader.createLoader(
			(CClassLoader.class.getClassLoader() != null) ? CClassLoader.class
					.getClassLoader() : ClassLoader.getSystemClassLoader(),
			CClassLoader.ROOT_LOADER);

	/**
	 * Ser the URLClassLoader that will return to calls to getURLs()
	 */
	private static volatile URLClassLoader urlLoader = null;

	protected void finalize() throws Throwable {
		System.out.println("Finalizing classloader : "+this.finalizepath);
		this.finalizepath = null;
	}
	
	/**
	 * Create a new loader
	 * 
	 * @param parent
	 *            a reference to the parent loader
	 * @param name
	 *            loader name
	 * 
	 * @return a new loader
	 */
	private static final CClassLoader createLoader(final ClassLoader parent,
			final String name) {
		try {
			return new CClassLoader(parent, name);
		} catch (final Exception ignore) {
			return null;
		}
	} // end createLoader()

	/**
	 * Creates and allocates a memory URL
	 * 
	 * @param entryName
	 *            name of the entry
	 * @param entry
	 *            byte array of the entry
	 * @return the created URL or null if an error occurs.
	 */
	public static URL createMemoryURL(final String entryName, final byte[] entry) {
		try {
			final Class c = ClassLoader.getSystemClassLoader().loadClass(
					"org.allcolor.yahp.converter.CMemoryURLHandler");
			final Method m = c.getDeclaredMethod("createMemoryURL",
					new Class[] { String.class, byte[].class });
			m.setAccessible(true);
			return (URL) m.invoke(null, new Object[] { entryName, entry });
		} catch (final Exception ignore) {
			ignore.printStackTrace();
			return null;
		}
	}
	
	private static final void clearMap(Map map) {
		for(Iterator it = map.entrySet().iterator();it.hasNext();) {
			Map.Entry e = (Map.Entry)it.next();
			Object value = e.getKey();
			if ((value != null)
					&& (value.getClass()
							.getClassLoader() != null)
					&& (value.getClass()
							.getClassLoader()
							.getClass() == CClassLoader.class)) {
				System.out
				.println("Resseting thread local "
						+ value);
				it.remove();
				continue;
			}
			if (value instanceof Map) {
				clearMap((Map)value);
			} else if (value instanceof List) {
				clearList((List)value);
			} else if (value instanceof Set) {
				clearSet((Set)value);
			}
			value = e.getValue();
			if ((value != null)
					&& (value.getClass()
							.getClassLoader() != null)
					&& (value.getClass()
							.getClassLoader()
							.getClass() == CClassLoader.class)) {
				System.out
				.println("Resseting thread local "
						+ value);
				it.remove();
				continue;
			}
			if (value instanceof Map) {
				clearMap((Map)value);
			} else if (value instanceof List) {
				clearList((List)value);
			} else if (value instanceof Set) {
				clearSet((Set)value);
			} else if (value instanceof Object[]) {
				clearArray((Object[])value);
			}
		}
	}
	
	private static final void clearArray(Object [] array) {
		for (int i=0;i<array.length;i++) {
			Object value = array[i];
			if ((value != null)
					&& (value.getClass()
							.getClassLoader() != null)
					&& (value.getClass()
							.getClassLoader()
							.getClass() == CClassLoader.class)) {
				System.out
				.println("Resseting thread local "
						+ value);
				array[i] = null;
				continue;
			}
			if (value instanceof Map) {
				clearMap((Map)value);
			} else if (value instanceof List) {
				clearList((List)value);
			} else if (value instanceof Set) {
				clearSet((Set)value);
			} else if (value instanceof Object[]) {
				clearArray((Object[])value);
			}
		}
	}
	
	private static final void clearList(List list) {
		for(Iterator it = list.iterator();it.hasNext();) {
			Object value = it.next();
			if ((value != null)
					&& (value.getClass()
							.getClassLoader() != null)
					&& (value.getClass()
							.getClassLoader()
							.getClass() == CClassLoader.class)) {
				System.out
				.println("Resseting thread local "
						+ value);
				it.remove();
				continue;
			}
			if (value instanceof Map) {
				clearMap((Map)value);
			} else if (value instanceof List) {
				clearList((List)value);
			} else if (value instanceof Set) {
				clearSet((Set)value);
			} else if (value instanceof Object[]) {
				clearArray((Object[])value);
			}
		}
	}

	private static final void clearSet(Set list) {
		for(Iterator it = list.iterator();it.hasNext();) {
			Object value = it.next();
			if ((value != null)
					&& (value.getClass()
							.getClassLoader() != null)
					&& (value.getClass()
							.getClassLoader()
							.getClass() == CClassLoader.class)) {
				System.out
				.println("Resseting thread local "
						+ value);
				it.remove();
				continue;
			}
			if (value instanceof Map) {
				clearMap((Map)value);
			} else if (value instanceof List) {
				clearList((List)value);
			} else if (value instanceof Set) {
				clearSet((Set)value);
			} else if (value instanceof Object[]) {
				clearArray((Object[])value);
			}
		}
	}

	/**
	 * destroy the loader tree
	 */
	public static final void destroy() {
		if (CClassLoader.rootLoader == null) {
			return;
		}
		System.out.println("Destroying YAHP ClassLoader Tree");

		CClassLoader.urlLoader = null;

		try {
			Field f = Class.forName("java.lang.Shutdown").getDeclaredField(
					"hooks");
			f.setAccessible(true);
			ArrayList l = (ArrayList) f.get(null);
			for (Iterator it = l.iterator(); it.hasNext();) {
				Object o = it.next();
				if ((o != null)
						&& (o.getClass().getClassLoader() != null)
						&& (o.getClass().getClassLoader().getClass() == CClassLoader.class)) {
					it.remove();
				}
			}
		} catch (Throwable ignore) {
		}

		try {
			Field f = Class.forName("java.lang.ApplicationShutdownHooks")
					.getDeclaredField("hooks");
			f.setAccessible(true);
			IdentityHashMap l = (IdentityHashMap) f.get(null);
			for (Iterator it = l.entrySet().iterator(); it.hasNext();) {
				Entry e = (Entry) it.next();
				Thread o = (Thread) e.getKey();
				if ((o != null)
						&& (o.getClass().getClassLoader() != null)
						&& (o.getClass().getClassLoader().getClass() == CClassLoader.class)) {
					it.remove();
					continue;
				}
				o = (Thread) e.getValue();
				if ((o != null)
						&& (o.getClass().getClassLoader() != null)
						&& (o.getClass().getClassLoader().getClass() == CClassLoader.class)) {
					it.remove();
				}
			}
		} catch (Throwable ignore) {
		}

		try {
			if ((UIManager.getLookAndFeel() != null)
					&& (UIManager.getLookAndFeel().getClass().getClassLoader() != null)
					&& (UIManager.getLookAndFeel().getClass().getClassLoader()
							.getClass() == CClassLoader.class)) {
				UIManager.setLookAndFeel((LookAndFeel) null);
			}
			Field f = UIManager.class.getDeclaredField("currentLAFState");
			f.setAccessible(true);
			Object lafstate = f.get(null);
			if (lafstate != null) {
				Field fmultiUIDefaults = lafstate.getClass().getDeclaredField(
						"multiUIDefaults");
				fmultiUIDefaults.setAccessible(true);
				Object multiUIDefaults = fmultiUIDefaults.get(lafstate);
				Method clear = multiUIDefaults.getClass().getDeclaredMethod(
						"clear", (Class[]) null);
				clear.setAccessible(true);
				clear.invoke(multiUIDefaults, (Object[]) null);
				Field tbl = lafstate.getClass().getDeclaredField("tables");
				tbl.setAccessible(true);
				Hashtable[] tables = (Hashtable[]) tbl.get(lafstate);
				if (tables != null) {
					for (int i = 0; i < tables.length; i++) {
						Hashtable element = tables[i];
						if (element != null) {
							element.clear();
						}
					}
				}
			}
		} catch (Throwable ignore) {
		}

		try {
			Hashtable tb = UIManager.getDefaults();
			Object cl = tb.get("ClassLoader");
			if (cl.getClass() == CClassLoader.class) {
				tb.put("ClassLoader", CClassLoader.rootLoader.getParent());
			}
		} catch (Throwable ignore) {
		}

		Method logFactoryRelease = null;

		try {
			logFactoryRelease = CClassLoader.rootLoader.loadClass(
					"org.apache.commons.logging.LogFactory").getMethod(
					"release", new Class[] { ClassLoader.class });
		} catch (final Throwable ignore) {
		}

		CClassLoader.rootLoader._destroy(logFactoryRelease);
		CClassLoader.mandatoryLoadersMap.clear();
		CClassLoader.rootLoader = null;

		// deregister any sql driver loaded
		try {
			final List deregisterList = new ArrayList();
			for (final Enumeration it = DriverManager.getDrivers(); it
					.hasMoreElements();) {
				final Driver d = (Driver) it.nextElement();

				if ((d != null)
						&& (d.getClass().getClassLoader() != null)
						&& (d.getClass().getClassLoader().getClass() == CClassLoader.class)) {
					deregisterList.add(d);
				}
			}

			for (int i = 0; i < deregisterList.size(); i++) {
				final Driver driver = (Driver) deregisterList.get(i);
				DriverManager.deregisterDriver(driver);
			}
		} catch (final Throwable ignore) {
		}

		// stop dandling thread created with this classloader
		// tested only on sun jdk
		ThreadGroup tg = Thread.currentThread().getThreadGroup();
		while ((tg != null) && (tg.getParent() != null)) {
			tg = tg.getParent();
		}
		List ltg = new ArrayList();
		ltg.add(tg);
		CClassLoader.getThreadGroups(tg, ltg);
		for (int ii = 0; ii < ltg.size(); ii++) {
			try {
				final ThreadGroup g = (ThreadGroup) ltg.get(ii);
				final Field fthreads = ThreadGroup.class
						.getDeclaredField("threads");
				fthreads.setAccessible(true);

				final List toStop = new ArrayList();
				Object threads[] = null;

				if (fthreads.getType() == Vector.class) {
					// in gnu classpath
					threads = ((Vector) fthreads.get(g)).toArray();
				} else {
					// sun
					threads = (Object[]) fthreads.get(g);
				}

				for (int i = 0; i < threads.length; i++) {
					if (threads[i] == null) {
						continue;
					}
					if ((threads[i] != null)
							&& (((Thread) threads[i]).getContextClassLoader() != null)
							&& (((Thread) threads[i]).getContextClassLoader()
									.getClass() == CClassLoader.class)) {
						((Thread) threads[i]).setContextClassLoader(null);
					}
					if ((threads[i] != null)
							&& (threads[i].getClass().getClassLoader() != null)
							&& (threads[i].getClass().getClassLoader()
									.getClass() == CClassLoader.class)) {
						toStop.add((Thread) threads[i]);
					}

					// remove any object in threadLocal referring an object
					// loaded
					// by this classloader tree
					try {
						final Field fthreadLocals = Thread.class
								.getDeclaredField("threadLocals");
						fthreadLocals.setAccessible(true);

						final Object threadLocals = fthreadLocals
								.get(threads[i]);
						if (threadLocals != null) {
							final Field ftable = threadLocals.getClass()
									.getDeclaredField("table");
							ftable.setAccessible(true);

							final Object table[] = (Object[]) ftable
									.get(threadLocals);

							for (int kk = 0; kk < table.length; kk++) {
								final Object element = table[kk];
								if (element != null) {
									final Field fvalue = element.getClass()
											.getDeclaredField("value");
									fvalue.setAccessible(true);

									final Object value = fvalue.get(element);

									if ((value != null)
											&& (value.getClass()
													.getClassLoader() != null)
											&& (value.getClass()
													.getClassLoader()
													.getClass() == CClassLoader.class)) {
										fvalue.set(element, null);
									}
									if (value instanceof Map) {
										clearMap((Map)value);
									} else if (value instanceof List) {
										clearList((List)value);
									} else if (value instanceof Set) {
										clearSet((Set)value);
									} else if (value instanceof Object[]) {
										clearArray((Object[])value);
									}

									fvalue.setAccessible(false);
								}
							}
							ftable.setAccessible(false);
						}
						fthreadLocals.setAccessible(false);
					} catch (final Throwable ignore) {
						ignore.printStackTrace();
					}

					// remove any object in threadLocal referring an object
					// loaded
					// by this classloader tree
					try {
						final Field fthreadLocals = Thread.class
								.getDeclaredField("inheritableThreadLocals");
						fthreadLocals.setAccessible(true);

						final Object threadLocals = fthreadLocals
								.get(threads[i]);
						if (threadLocals != null) {
							final Field ftable = threadLocals.getClass()
									.getDeclaredField("table");
							ftable.setAccessible(true);

							final Object table[] = (Object[]) ftable
									.get(threadLocals);

							for (int kk = 0; kk < table.length; kk++) {
								final Object element = table[kk];
								if (element != null) {
									final Field fvalue = element.getClass()
											.getDeclaredField("value");
									fvalue.setAccessible(true);

									final Object value = fvalue.get(element);

									if ((value != null)
											&& (value.getClass()
													.getClassLoader() != null)
											&& (value.getClass()
													.getClassLoader()
													.getClass() == CClassLoader.class)) {
										fvalue.set(element, null);
									}
									if (value instanceof Map) {
										clearMap((Map)value);
									} else if (value instanceof List) {
										clearList((List)value);
									} else if (value instanceof Set) {
										clearSet((Set)value);
									} else if (value instanceof Object[]) {
										clearArray((Object[])value);
									}

									fvalue.setAccessible(false);
								}
							}

							ftable.setAccessible(false);
						}
						fthreadLocals.setAccessible(false);
					} catch (final Throwable ignore) {
						ignore.printStackTrace();
					}

					// remove any protection domain referring this loader tree
					try {
						final Field finheritedAccessControlContext = Thread.class
								.getDeclaredField("inheritedAccessControlContext");
						finheritedAccessControlContext.setAccessible(true);

						final Object inheritedAccessControlContext = finheritedAccessControlContext
								.get(threads[i]);

						if (inheritedAccessControlContext != null) {
							final Field fcontext = AccessControlContext.class
									.getDeclaredField("context");
							fcontext.setAccessible(true);

							final Object context[] = (Object[]) fcontext
									.get(inheritedAccessControlContext);

							if (context != null) {
								for (int k = 0; k < context.length; k++) {
									if (context[k] == null) continue;
									final Field fclassloader = ProtectionDomain.class
											.getDeclaredField("classloader");
									fclassloader.setAccessible(true);

									final Object classloader = fclassloader
											.get(context[k]);

									if ((classloader != null)
											&& (classloader.getClass() == CClassLoader.class)) {
										context[k] = null;
									}

									fclassloader.setAccessible(false);
								}
							}

							fcontext.setAccessible(false);
						}

						finheritedAccessControlContext.setAccessible(false);
					} catch (final Throwable ignore) {
						ignore.printStackTrace();
					}
				}

				fthreads.setAccessible(false);
				for (int i = 0; i < toStop.size(); i++) {
					try {
						final Thread t = (Thread) toStop.get(i);
						final Method stop = t.getClass().getMethod("stop",
								(Class[]) null);
						stop.invoke(t, (Object[]) null);
					} catch (final Throwable ignore) {
					}
				}
			} catch (final Throwable ignore) {
			}
		}
		try{CThreadContext.destroy();}catch(Throwable ignore){}
		System.runFinalization();
		System.gc();
		Introspector.flushCaches();
		System.out.println("Destroying YAHP ClassLoader Tree : done");
	} // end destroy()

	/**
	 * Return a list of classes and jar files
	 * 
	 * @param current
	 *            currently inspected file
	 * @param list
	 *            list of url to classes and jar files
	 * @return list of url to classes and jar files
	 */
	private static List getClasses(final File current, final List list) {
		if (current.isDirectory()) {
			try {
				list.add(current.toURI().toURL());
			} catch (final Exception ignore) {
			}

			final File children[] = current.listFiles();

			for (int i = 0; i < children.length; i++) {
				final File element = children[i];
				CClassLoader.getClasses(element, list);
			}
		} else if (current.isFile()) {
			final String name = current.getName();

			if (name.endsWith(".class")) {
				try {
					list.add(current.toURI().toURL());
				} catch (final Exception ignore) {
				}
			} else if (name.endsWith(".jar")) {
				try {
					list.add(current.toURI().toURL());
				} catch (final Exception ignore) {
				}
			}
		}

		return list;
	} // end getClasses()

	public static final ClassLoader getContextLoader() {
		return CClassLoader.contextLoader.get() != null ? (CClassLoader) ((WeakReference) CClassLoader.contextLoader
				.get()).get()
				: null;
	}

	/**
	 * return the loader with the given path
	 * 
	 * @param fpath
	 *            the path to lookup
	 * 
	 * @return the loader with the given path
	 */
	public static final CClassLoader getLoader(final String fpath) {
		String path = fpath;
		CClassLoader currentLoader = CClassLoader.rootLoader;

		if (path == null) {
			return CClassLoader.getRootLoader();
		}

		if (path.startsWith(CClassLoader.ROOT_LOADER)) {
			path = path.substring(10);
		}

		final StringTokenizer tokenizer = new StringTokenizer(path, "/", false);

		while (tokenizer.hasMoreTokens()) {
			final String name = tokenizer.nextToken();
			currentLoader = currentLoader.getLoaderByName(name);
		}

		return currentLoader;
	} // end getLoader()

	/**
	 * return the rootloader
	 * 
	 * @return the rootloader
	 */
	public static final CClassLoader getRootLoader() {
		if (CClassLoader.rootLoader == null) {
			CClassLoader.rootLoader = CClassLoader.createLoader(
					CClassLoader.class.getClassLoader(),
					CClassLoader.ROOT_LOADER);
		}
		return CClassLoader.rootLoader;
	} // end getRootLoader()

	private static void getThreadGroups(ThreadGroup tg, List ltg) {
		try {
			final Field fgroups = ThreadGroup.class.getDeclaredField("groups");
			fgroups.setAccessible(true);
			ThreadGroup[] array = (ThreadGroup[]) fgroups.get(tg);
			if (array != null) {
				for (int i = 0; i < array.length; i++) {
					ThreadGroup element = array[i];
					if (!ltg.contains(element)) {
						CClassLoader.getThreadGroups(element, ltg);
						ltg.add(element);
					}
				}
			}
		} catch (Throwable ignore) {
		}
	}

	/**
	 * Return a list of jar and classes located in path
	 * 
	 * @param path
	 *            the path in which to search
	 * @return a list of jar and classes located in path
	 */
	public static URL[] getURLs(final String path) {
		final File topDir = new File(path);
		final List list = new ArrayList();
		CClassLoader.getClasses(topDir, list);

		final URL ret[] = new URL[list.size() + 1];

		for (int i = 0; i < list.size(); i++) {
			ret[i] = (URL) list.get(i);
		}

		try {
			ret[list.size()] = topDir.toURI().toURL();
		} catch (final Exception ignore) {
		}

		return ret;
	} // end getURLs()

	/**
	 * initialize the loaders hierarchy
	 * 
	 * @param config
	 *            the loaders config object
	 */
	public static final void init(final CClassLoaderConfig config) {
		CClassLoader.installURLStreamHandlerFactory();
		if (CClassLoader.getRootLoader().isInit()) {
			return;
		}

		// initialize the loaders default parameters.
		for (final Iterator it = config.getLoadersInfoMap().entrySet()
				.iterator(); it.hasNext();) {
			final Map.Entry entry = (Map.Entry) it.next();
			final String loaderPath = (String) entry.getKey();
			final CClassLoaderConfig.CLoaderInfo info = (CClassLoaderConfig.CLoaderInfo) entry
					.getValue();
			final CClassLoader loader = CClassLoader.getLoader(loaderPath);
			loader.config = config;
			loader.booAlone = info.isAlone();
			loader.booDoNotForwardToParent = info.isDoNotForwardToParent();
			loader.booMandatory = info.isMandatory();

			if (loader.isMandatory()) {
				CClassLoader.mandatoryLoadersMap.put(loader.getPath(), loader);
			}

			loader.booResourceOnly = info.isResourceOnly();
		}
		// lookup classes/resources in filesystem
		for (final Iterator it = config.getFilesMap().entrySet().iterator(); it
				.hasNext();) {
			final Map.Entry entry = (Map.Entry) it.next();
			final String loaderPath = (String) entry.getKey();
			final List list = (List) entry.getValue();
			final CClassLoader loader = CClassLoader.getLoader(loaderPath);

			for (final Iterator f = list.iterator(); f.hasNext();) {
				final Object obj = f.next();
				if (obj instanceof URL) {
					final URL file = (URL) obj;
					String name = file.toString();
					final int index = name.indexOf(loaderPath);
					if (index != -1) {
						name = name.substring(index + loaderPath.length());
					}
					if (name.endsWith(".jar")) {
						loader.addResource(name, file);
						loader.readDirectories(file);
					} else {
						loader.addResource(name, file);
						if (!loader.booResourceOnly && name.endsWith(".class")) {
							name = name.substring(0, name.lastIndexOf('.'));
							name = name.replace('\\', '/');
							name = name.replace('/', '.');
							loader.addClass(name, file);
						} else if (name.startsWith("native/")) {
							String system = name.substring(7);
							system = system.substring(0, system.indexOf('/'));
							if (!loader.dllMap.containsKey(system)) {
								loader.dllMap.put(system, file);
							}
							if (!loader.resourcesMap.containsKey(name)) {
								loader.resourcesMap.put(name, file);
							} else {
								final Object to = loader.resourcesMap.get(name);
								if (to instanceof URL) {
									final URL uo = (URL) to;
									final List l = new ArrayList();
									l.add(uo);
									l.add(file);
									loader.resourcesMap.put(name, l);
								} else if (to instanceof List) {
									final List uo = (List) to;
									uo.add(file);
									loader.resourcesMap.put(name, uo);
								}
							}
						}
					}
				}
			}
		}

		// set init state
		CClassLoader.setInit(CClassLoader.getRootLoader());
	} // end init()

	/**
	 * Install a custom URLStreamHandlerFactory which handle nested jar loading,
	 * and memory url. The installation is done only if necessary.
	 * 
	 */
	private static void installURLStreamHandlerFactory() {
		synchronized (URL.class) {
			try {
				try {
					new URL("yahpjarloader://test/test");
					return;
				}
				catch (MalformedURLException e) {}
				if (CClassLoader.class.getClassLoader() != ClassLoader
						.getSystemClassLoader()) {
					try {
						Class c = ClassLoader
								.getSystemClassLoader()
								.loadClass(
										"org.allcolor.yahp.converter.CYaHPURLStreamHandlerFactory");
						if (c != null) {
							return;
						}
					} catch (Throwable ignore) {
					}
				}
				final Field factory = URL.class.getDeclaredField("factory");
				factory.setAccessible(true);
				final URLStreamHandlerFactory oldFactory = (URLStreamHandlerFactory) factory
						.get(null);
				if ((oldFactory == null)
						|| (oldFactory.getClass().getName().indexOf(
								"CYaHPURLStreamHandlerFactory") == -1)) {
					synchronized (factory) {
						System.out
								.println("Installing new URLStreamHandlerFactory...");
						final Method defineClass = ClassLoader.class
								.getDeclaredMethod("defineClass", new Class[] {
										String.class, byte[].class, int.class,
										int.class });
						defineClass.setAccessible(true);
						final byte[] bauc = CClassLoader
								.loadByteArray(CClassLoader.class
										.getClassLoader()
										.getResource(
												"org/allcolor/yahp/converter/CByteArrayUrlConnection.class"));
						final byte[] crypt = CClassLoader
								.loadByteArray(CClassLoader.class
										.getClassLoader()
										.getResource(
												"org/allcolor/yahp/converter/CCryptoUtils.class"));
						final byte[] b64c = CClassLoader
								.loadByteArray(CClassLoader.class
										.getClassLoader()
										.getResource(
												"org/allcolor/yahp/converter/CBASE64Codec.class"));
						final byte[] muh = CClassLoader
								.loadByteArray(CClassLoader.class
										.getClassLoader()
										.getResource(
												"org/allcolor/yahp/converter/CMemoryURLHandler.class"));
						final byte[] juhgc = CClassLoader
								.loadByteArray(CClassLoader.class
										.getClassLoader()
										.getResource(
												"org/allcolor/yahp/converter/CJarLoaderURLStreamHandler$CGCCleaner.class"));
						final byte[] juh = CClassLoader
								.loadByteArray(CClassLoader.class
										.getClassLoader()
										.getResource(
												"org/allcolor/yahp/converter/CJarLoaderURLStreamHandler.class"));
						final byte[] hf = CClassLoader
								.loadByteArray(CClassLoader.class
										.getClassLoader()
										.getResource(
												"org/allcolor/yahp/converter/CYaHPURLStreamHandlerFactory.class"));
						defineClass
								.invoke(
										ClassLoader.getSystemClassLoader(),
										new Object[] {
												"org.allcolor.yahp.converter.CByteArrayUrlConnection",
												bauc, new Integer(0),
												new Integer(bauc.length) });
						defineClass.invoke(ClassLoader.getSystemClassLoader(),
								new Object[] {
										"org.allcolor.yahp.converter.CCryptoUtils",
										crypt, new Integer(0),
										new Integer(crypt.length) });
						defineClass.invoke(ClassLoader.getSystemClassLoader(),
								new Object[] {
										"org.allcolor.yahp.converter.CBASE64Codec",
										b64c, new Integer(0),
										new Integer(b64c.length) });
						defineClass
								.invoke(
										ClassLoader.getSystemClassLoader(),
										new Object[] {
												"org.allcolor.yahp.converter.CMemoryURLHandler",
												muh, new Integer(0),
												new Integer(muh.length) });
						defineClass
								.invoke(
										ClassLoader.getSystemClassLoader(),
										new Object[] {
												"org.allcolor.yahp.converter.CJarLoaderURLStreamHandler$CGCCleaner",
												juhgc, new Integer(0),
												new Integer(juhgc.length) });
						defineClass
								.invoke(
										ClassLoader.getSystemClassLoader(),
										new Object[] {
												"org.allcolor.yahp.converter.CJarLoaderURLStreamHandler",
												juh, new Integer(0),
												new Integer(juh.length) });
						final Class c = (Class) defineClass
								.invoke(
										ClassLoader.getSystemClassLoader(),
										new Object[] {
												"org.allcolor.yahp.converter.CYaHPURLStreamHandlerFactory",
												hf, new Integer(0),
												new Integer(hf.length) });
						final Constructor cons = c
								.getConstructor(new Class[] { URLStreamHandlerFactory.class });
						final URLStreamHandlerFactory fact = (URLStreamHandlerFactory) cons
								.newInstance(new Object[] { oldFactory });
						factory.set(null, fact);
						System.out
								.println("Installing new URLStreamHandlerFactory DONE.");
					}
				}
			} catch (final Throwable ignore) {
				ignore.printStackTrace();
			}
		}
	}

	/**
	 * load the given inputstream in a byte array
	 * 
	 * @param in
	 *            the stream to load
	 * 
	 * @return a byte array
	 */
	public static final byte[] loadByteArray(final InputStream in) {
		try {
			final ByteArrayOutputStream bOut = new ByteArrayOutputStream();

			final byte buffer[] = new byte[2048];

			int iNbByteRead = -1;

			while ((iNbByteRead = in.read(buffer)) != -1) {
				bOut.write(buffer, 0, iNbByteRead);
			}

			return bOut.toByteArray();
		} catch (final IOException ioe) {
			return null;
		}
	} // end loadByteArray()

	/**
	 * load the given url in a byte array
	 * 
	 * @param urlToResource
	 *            url to load
	 * 
	 * @return a byte array
	 */
	public static final byte[] loadByteArray(final URL urlToResource) {
		InputStream in = null;

		try {
			final URLConnection uc = urlToResource.openConnection();
			uc.setUseCaches(false);
			in = uc.getInputStream();

			return CClassLoader.loadByteArray(in);
		} catch (final IOException ioe) {
			return null;
		} finally {
			try {
				in.close();
			} catch (final Exception ignore) {
			}
		}
	} // end loadByteArray()

	/**
	 * load multiple resources of same name
	 * 
	 * @param loader
	 *            current lookup loader
	 * @param URLList
	 *            list of found resources
	 * @param resourceName
	 *            name to match
	 */
	private static final void loadResources(final CClassLoader loader,
			final List URLList, final String resourceName) {
		final List l = loader.getPrivateResource(resourceName);

		if (l != null) {
			URLList.addAll(l);
		}

		final List loaderList = new ArrayList();

		for (final Iterator it = loader.childrenMap.entrySet().iterator(); it
				.hasNext();) {
			final Entry entry = (Entry) it.next();
			loaderList.add(entry.getValue());
		}

		for (int i = 0; i < loaderList.size(); i++) {
			final Object element = loaderList.get(i);
			final CClassLoader child = (CClassLoader) element;
			CClassLoader.loadResources(child, URLList, resourceName);
		}
	} // end loadResources()

	/**
	 * log the message
	 * 
	 * @param Message
	 *            message to log
	 * @param level
	 *            log level (INFO,DEBUG,FATAL)
	 */
	private static final void log(final String Message, final int level) {
		if ((level == CClassLoader.INFO)
				&& CClassLoader.log.isLoggable(Level.INFO)) {
			CClassLoader.log.info(Message);
		} else if ((level == CClassLoader.DEBUG)
				&& CClassLoader.log.isLoggable(Level.FINE)) {
			CClassLoader.log.fine(Message);
		} else if ((level == CClassLoader.FATAL)
				&& CClassLoader.log.isLoggable(Level.SEVERE)) {
			CClassLoader.log.severe(Message);
		}
	} // end log()

	/**
	 * Release a previously allocated memory URL
	 * 
	 * @param u
	 *            the URL to release memory
	 */
	public static void releaseMemoryURL(final URL u) {
		try {
			final Class c = ClassLoader.getSystemClassLoader().loadClass(
					"org.allcolor.yahp.converter.CMemoryURLHandler");
			final Method m = c.getDeclaredMethod("releaseMemoryURL",
					new Class[] { URL.class });
			m.setAccessible(true);
			m.invoke(null, new Object[] { u });
		} catch (final Exception ignore) {
			ignore.printStackTrace();
		}
	}

	public static final void setContextLoader(ClassLoader contextLoader) {
		CClassLoader.contextLoader.set(new WeakReference(contextLoader));
	}

	/**
	 * set the init flag of all loaders
	 * 
	 * @param loader
	 *            the current loader
	 */
	private static final void setInit(final CClassLoader loader) {
		loader.booInit = true;

		for (final Iterator it = loader.getChildLoader(); it.hasNext();) {
			final Map.Entry entry = (Map.Entry) it.next();
			CClassLoader.setInit((CClassLoader) entry.getValue());
		}
	} // end setInit()

	/**
	 * Set the urlloader used to resolved getURLs
	 * 
	 * @param urlLoader
	 *            the urlloader used to resolved getURLs
	 */
	public static void setURLClassLoader(final URLClassLoader urlLoader) {
		CClassLoader.urlLoader = urlLoader;
	}

	/**
	 * test if the logging level is enabled
	 * 
	 * @param level
	 *            the logging level to test
	 * 
	 * @return true if enabled
	 */
	private static final boolean sl(final int level) {
		if ((level == CClassLoader.INFO)
				&& CClassLoader.log.isLoggable(Level.INFO)) {
			return true;
		} else if ((level == CClassLoader.DEBUG)
				&& CClassLoader.log.isLoggable(Level.FINE)) {
			return true;
		} else if ((level == CClassLoader.FATAL)
				&& CClassLoader.log.isLoggable(Level.SEVERE)) {
			return true;
		}

		return false;
	} // end sl()

	/** is alone ? */
	private boolean booAlone = false;

	/** is not forwarding to parent ? */
	private boolean booDoNotForwardToParent = false;

	/** is initializing ? */
	private boolean booInit = false;

	/** is mandatory ? */
	private boolean booMandatory = false;

	/** is resource only ? */
	private boolean booResourceOnly = false;

	private final Map cacheMap = new HashMap();

	/** contains reference to children loaders */
	private final Map childrenMap = new HashMap();

	/** known classes by this loader */
	private final Map classesMap = new HashMap();

	/** reference to the loader config object */
	private CClassLoaderConfig config;

	/**
	 * Contains a name-URL of dll/.so mapping for this loader
	 */
	private final Map dllMap = new HashMap();

	/** loader name */
	private String name = null;

	/** loader path */
	private String path = null;

	private String finalizepath = null;

	/** known resources by this loader */
	private final Map resourcesMap = new HashMap();

	/**
	 * Create a new loader
	 * 
	 * @param parent
	 *            a reference to the parent loader
	 * @param name
	 *            loader name
	 * 
	 * @throws Exception
	 *             should not happen
	 */
	private CClassLoader(final ClassLoader parent, final String name)
			throws Exception {
		super(new URL[0], parent);
		this.name = name;
		this.path = this.nGetLoaderPath();
	} // end CClassLoader()

	/**
	 * Destroy instance variables.
	 * 
	 * @param logFactoryRelease
	 *            method to release commons logging
	 */
	private final void _destroy(final Method logFactoryRelease) {
		for (final Iterator it = this.childrenMap.entrySet().iterator(); it
				.hasNext();) {
			final Map.Entry entry = (Map.Entry) it.next();
			final CClassLoader loader = (CClassLoader) entry.getValue();
			loader._destroy(logFactoryRelease);
			it.remove();
		}

		try {
			// remove ref from commons logging
			logFactoryRelease.invoke(null, new Object[] { this });
		} catch (final Exception e) {
		}

		try {
			// reset parent to system class loader
			final Field parent = ClassLoader.class.getDeclaredField("parent");
			parent.setAccessible(true);
			parent.set(this, ClassLoader.getSystemClassLoader());
			parent.setAccessible(false);
		} catch (final Throwable ignore) {
		}

		this.classesMap.clear();
		for (final Iterator it = this.dllMap.entrySet().iterator(); it
				.hasNext();) {
			final Object element = it.next();
			final Map.Entry entry = (Map.Entry) element;
			if (entry.getValue() instanceof File) {
				((File) entry.getValue()).delete();
			}
		}
		this.cacheMap.clear();
		this.dllMap.clear();
		this.resourcesMap.clear();
		this.config = null;
		this.name = null;
		this.finalizepath = this.path;
		this.path = null;
		//classes
		System.runFinalization();
		System.gc();
	} // end _destroy()

	/**
	 * add a class to known class
	 * 
	 * @param className
	 *            class name
	 * @param urlToClass
	 *            url to class file
	 */
	public final void addClass(final String className, final URL urlToClass) {
		if ((className == null) || (urlToClass == null)) {
			return;
		}

		if (!this.classesMap.containsKey(className)) {
			this.classesMap.put(className, urlToClass);
		}
	} // end addClass()

	/**
	 * add a resource
	 * 
	 * @param resouceName
	 *            name of the resource to add
	 * @param urlToResource
	 *            url to the resource
	 */
	public final void addResource(final String resouceName,
			final URL urlToResource) {
		if ((urlToResource == null) || (resouceName == null)) {
			return;
		}
		if (this.resourcesMap.containsKey(resouceName.replace('\\', '/'))) {
			final Object to = this.resourcesMap.get(resouceName.replace('\\',
					'/'));
			if (to instanceof URL) {
				final URL uo = (URL) to;
				final List l = new ArrayList();
				l.add(uo);
				this.resourcesMap.put(resouceName.replace('\\', '/'), l);
			} else if (to instanceof List) {
				final List uo = (List) to;
				uo.add(urlToResource);
				this.resourcesMap.put(resouceName.replace('\\', '/'), uo);
			}
		} else {
			this.resourcesMap
					.put(resouceName.replace('\\', '/'), urlToResource);
		}
	} // end addResource()

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.ClassLoader#findClass(java.lang.String)
	 */

	protected final Class findClass(final String name)
			throws ClassNotFoundException {
		try {
			if (name == null) {
				return null;
			}

			if (name.startsWith("java.") || name.startsWith("sun.reflect")) {
				return ClassLoader.getSystemClassLoader().loadClass(name);
			}

			final String searchClass = name.replace('.', '/') + ".class";
			final CThreadContext context = CThreadContext.getInstance();
			final String cpName = CClassLoader.CCLASSLOADER_NAMESPACE + name;

			List lPriorLoader = (List) context.get(cpName);

			if (lPriorLoader == null) {
				lPriorLoader = new Vector();
				context.set(cpName, lPriorLoader);
			}

			if (lPriorLoader.contains(this.toString())) {
				return null;
			}

			lPriorLoader.add(this.toString());

			if (CClassLoader.sl(CClassLoader.DEBUG)) {
				CClassLoader.log("Searching " + name + " in " + this.getPath(),
						CClassLoader.DEBUG);
			}

			final WeakReference cache = (WeakReference) this.cacheMap.get(name);
			if (cache != null) {
				final Class c = (Class) cache.get();
				if (c != null) {
					return c;
				}
			}

			// first check if this class as a mandatory loader
			if (!this.booAlone) {
				CClassLoader loader = null;

				for (final Iterator it = CClassLoader.mandatoryLoadersMap
						.entrySet().iterator(); it.hasNext();) {
					final Entry entry = (Entry) it.next();
					loader = (CClassLoader) entry.getValue();

					if (loader.classesMap.containsKey(searchClass)) {
						if (loader != this) {
							break;
						}
						loader = null;
						break;
					}
					loader = null;
				}

				if (loader != null) {
					final Class c = loader.findClass(name);
					if (c != null) {
						return c;
					}
				}
			}

			// second search in this repository
			byte buffer[] = null;

			final URL urlToResource = (URL) this.classesMap.get(searchClass);

			if (urlToResource != null) {
				buffer = CClassLoader.loadByteArray(urlToResource);
			}

			if (buffer != null) {
				if (CClassLoader.sl(CClassLoader.DEBUG)) {
					CClassLoader.log(
							"Loaded " + name + " in " + this.getPath(),
							CClassLoader.DEBUG);
				}

				Class c = null;
				c = this.findLoadedClass(name);

				if (c == null) {
					try {
						c = this.defineClass(name, buffer, 0, buffer.length);
					} catch (final LinkageError e) {
						c = this.findLoadedClass(name);

						if (c == null) {
							throw new ClassNotFoundException(name
									+ " was not found !");
						}
					}
				}

				if (c != null) {
					return c;
				}
			}

			// then search in each children
			final List tmpLoader = new ArrayList();

			for (final Iterator it = this.childrenMap.entrySet().iterator(); it
					.hasNext();) {
				final Entry entry = (Entry) it.next();
				final CClassLoader child = (CClassLoader) entry.getValue();

				if (lPriorLoader.contains(child.toString())) {
					continue;
				}

				tmpLoader.add(child);
			}

			for (final Iterator it = tmpLoader.iterator(); it.hasNext();) {
				final Object element = it.next();
				final CClassLoader child = (CClassLoader) element;
				final Class c = child.findClass(name);

				if (c != null) {
					return c;
				}
			}

			// then follow to parents
			if ((this != CClassLoader.getRootLoader())
					&& !this.booDoNotForwardToParent) {
				if (lPriorLoader.contains(this.getParent().toString())) {
					return null;
				} else {
					if (this.getParent() instanceof CClassLoader) {
						return ((CClassLoader) this.getParent())
								.findClass(name);
					} else {
						return this.getParent().loadClass(name);
					}
				}
			} else {
				try {
					ClassLoader contextLoader = CClassLoader.getContextLoader();
					if (contextLoader == null) {
						contextLoader = CClassLoader.getRootLoader()
								.getParent();
					}
					final Class c = contextLoader.loadClass(name);
					if (c != null) {
						return c;
					}
				} catch (Throwable e) {
					final Class c = CClassLoader.getRootLoader().getParent()
							.loadClass(name);
					if (c != null) {
						return c;
					}
				}
				throw new ClassNotFoundException(name);
			}
		} catch (ClassNotFoundException e) {
			throw e;
		} catch (NoClassDefFoundError e) {
			throw e;
		} catch (Throwable e) {
			throw new ClassNotFoundException(name, e);
		}
	} // end findClass()

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.ClassLoader#findLibrary(java.lang.String)
	 */

	protected String findLibrary(final String libname) {
		if (!this.isInit()) {
			if (CClassLoader.sl(CClassLoader.DEBUG)) {
				CClassLoader
						.log("Not initialized, forward to old loader "
								+ libname + " in " + this.getPath(),
								CClassLoader.DEBUG);
			}

			return super.findLibrary(libname);
		}
		final String system = CSystem.getName() + "/" + libname;
		final CThreadContext context = CThreadContext.getInstance();
		final String cpName = CClassLoader.CCLASSLOADER_NAMESPACE + system;
		try {
			List lPriorLoader = (List) context.get(cpName);

			if (lPriorLoader == null) {
				lPriorLoader = new Vector();
				context.set(cpName, lPriorLoader);
			}

			if (lPriorLoader.contains(this.toString())) {
				return null;
			}

			lPriorLoader.add(this.toString());

			if (!this.booAlone) {
				CClassLoader loader = null;

				for (final Iterator it = CClassLoader.mandatoryLoadersMap
						.entrySet().iterator(); it.hasNext();) {
					final Entry entry = (Entry) it.next();
					loader = (CClassLoader) entry.getValue();

					if (loader.dllMap.containsKey(system)) {
						if (loader != this) {
							break;
						}
						loader = null;
						break;
					}
					loader = null;
				}

				if (loader != null) {
					final String c = loader.findLibrary(libname);
					context.set(cpName, null);
					return c;
				}
			}
			String path = null;
			final Object obj = this.dllMap.get(system);
			if (obj instanceof URL) {
				final URL urlToResource = (URL) obj;
				final byte[] buffer = CClassLoader.loadByteArray(urlToResource);
				final File nfile = File.createTempFile(libname
						+ this.hashCode(), ".so");
				final FileOutputStream fout = new FileOutputStream(nfile);
				fout.write(buffer);
				fout.close();
				this.dllMap.put(system, nfile);
				path = nfile.getAbsolutePath();
			} else if (obj instanceof File) {
				final File nfile = (File) obj;
				path = nfile.getAbsolutePath();
			}
			if (path != null) {
				context.set(cpName, null);
				return path;
			}

			// then search in each children
			final List tmpLoader = new ArrayList();

			for (final Iterator it = this.childrenMap.entrySet().iterator(); it
					.hasNext();) {
				final Entry entry = (Entry) it.next();
				final CClassLoader child = (CClassLoader) entry.getValue();

				if (lPriorLoader.contains(child.toString())) {
					continue;
				}

				tmpLoader.add(child);
			}

			for (final Iterator it = tmpLoader.iterator(); it.hasNext();) {
				final Object element = it.next();
				final CClassLoader child = (CClassLoader) element;
				final String c = child.findLibrary(libname);

				if (c != null) {
					return c;
				}
			}

			// then follow to parents
			if ((this != CClassLoader.getRootLoader())
					&& !this.booDoNotForwardToParent) {
				if (lPriorLoader.contains(this.getParent().toString())) {
					return null;
				} else {
					if (this.getParent() instanceof CClassLoader) {
						final CClassLoader parent = (CClassLoader) this
								.getParent();
						return parent.findLibrary(libname);
					} else {
						return super.findLibrary(libname);
					}
				}
			} else {
				final String c = super.findLibrary(libname);
				context.set(cpName, null);
				return c;
			}
		} catch (final IOException e) {
			return super.findLibrary(libname);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.ClassLoader#findResource(java.lang.String)
	 */

	public final URL findResource(final String fname) {
		if (!this.isInit()) {
			if (CClassLoader.sl(CClassLoader.DEBUG)) {
				CClassLoader.log("Not initialized, forward to old loader "
						+ fname + " in " + this.getPath(), CClassLoader.DEBUG);
			}

			if ((Thread.currentThread().getContextClassLoader() != null)
					&& (Thread.currentThread().getContextClassLoader() != this)) {
				return Thread.currentThread().getContextClassLoader()
						.getResource(fname);
			}

			return CClassLoader.getRootLoader().getParent().getResource(fname);
		}

		String name = fname;

		if (name.startsWith("/")) {
			name = name.substring(1);
		}

		final CThreadContext context = CThreadContext.getInstance();
		final String cpName = CClassLoader.CCLASSLOADER_NAMESPACE + name;
		List lPriorLoader = (List) context.get(cpName);

		if (lPriorLoader == null) {
			lPriorLoader = new Vector();
			context.set(cpName, lPriorLoader);
		}

		if (lPriorLoader.contains(this.toString())) {
			return null;
		}

		lPriorLoader.add(this.toString());

		if (CClassLoader.sl(CClassLoader.DEBUG)) {
			CClassLoader.log("Searching " + name + " in " + this.getPath(),
					CClassLoader.DEBUG);
		}

		// first check if this resource as a mandatory loader
		URL urlToResource = null;

		if (!this.booAlone) {
			CClassLoader loader = null;

			for (final Iterator it = CClassLoader.mandatoryLoadersMap
					.entrySet().iterator(); it.hasNext();) {
				final Entry entry = (Entry) it.next();
				loader = (CClassLoader) entry.getValue();

				if (loader.resourcesMap.containsKey(name)) {
					if (loader != this) {
						break;
					}
					loader = null;
					break;
				}
				loader = null;
			}

			if (loader != null) {
				urlToResource = loader.getResource(name);
			}

			if (urlToResource != null) {
				context.set(cpName, null);

				return urlToResource;
			}
		}

		final Object to = this.resourcesMap.get(name);
		if (to instanceof URL) {
			// second search in this repository
			urlToResource = (URL) to;
		} else if (to instanceof List) {
			final List l = (List) to;
			urlToResource = (URL) l.get(0);
		}

		if (urlToResource != null) {
			if (CClassLoader.sl(CClassLoader.DEBUG)) {
				CClassLoader.log("Loaded " + name + " in " + this.getPath(),
						CClassLoader.DEBUG);
			}

			context.set(cpName, null);

			return urlToResource;
		}

		// then search in each children
		final List tmpLoader = new ArrayList();

		for (final Iterator it = this.childrenMap.entrySet().iterator(); it
				.hasNext();) {
			final Entry entry = (Entry) it.next();
			final CClassLoader child = (CClassLoader) entry.getValue();

			if (lPriorLoader.contains(child.toString())) {
				continue;
			}

			tmpLoader.add(child);
		}

		for (final Iterator it = tmpLoader.iterator(); it.hasNext();) {
			final Object element = it.next();
			final CClassLoader child = (CClassLoader) element;
			urlToResource = child.getResource(name);

			if (urlToResource != null) {
				return urlToResource;
			}
		}

		// then follow to parents
		if ((this != CClassLoader.getRootLoader())
				&& !this.booDoNotForwardToParent) {
			if (lPriorLoader.contains(this.getParent().toString())) {
				return null;
			} else {
				if (this.getParent() == null) {
					return null;
				}

				return this.getParent().getResource(name);
			}
		} else {
			urlToResource = CClassLoader.getRootLoader().getParent()
					.getResource(name);
			context.set(cpName, null);

			return urlToResource;
		}
	} // end findResource()

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.ClassLoader#findResources(java.lang.String)
	 */

	public final Enumeration findResources(final String name)
			throws IOException {
		final CClassLoader loader = CClassLoader.getRootLoader();
		final List URLList = new ArrayList();

		try {
			CClassLoader.loadResources(loader, URLList, name);
		} finally {
			try {

			} catch (final Exception ignore) {
			}
		}

		try {
			final Enumeration eu = CClassLoader.getRootLoader().getParent()
					.getResources(name);
			while (eu.hasMoreElements()) {
				URLList.add(eu.nextElement());
			}
		} catch (final Throwable ignore) {
		}

		final Iterator it = URLList.iterator();

		return new Enumeration() {
			Iterator internalIt = it;

			public boolean hasMoreElements() {
				return this.internalIt.hasNext();
			} // end hasMoreElements()

			public Object nextElement() {
				return this.internalIt.next();
			} // end nextElement()
		} // end new
		;
	} // end findResources()

	/**
	 * return an iterator on children loaders
	 * 
	 * @return an iterator on children loaders
	 */
	public final Iterator getChildLoader() {
		return this.childrenMap.entrySet().iterator();
	} // end getChildLoader()

	/**
	 * return a copy of the known classes
	 * 
	 * @return a copy of the known classes
	 */
	public final Map getClassesMap() {
		return Collections.unmodifiableMap(this.classesMap);
	} // end getClassesMap()

	/**
	 * Return the child loader with the given name
	 * 
	 * @param name
	 *            name of the child to get
	 * 
	 * @return the child loader with the given name
	 */
	private final CClassLoader getLoaderByName(final String name) {
		try {

			CClassLoader loader = (CClassLoader) this.childrenMap.get(name);

			if (loader == null) {
				loader = CClassLoader.createLoader(this, name);
				this.childrenMap.put(name, loader);
			}

			return loader;
		} finally {
			try {

			} catch (final Exception ignore) {
			}
		}
	} // end getLoaderByName()

	/**
	 * Returns the name.
	 * 
	 * @return Returns the name.
	 */
	public final String getName() {
		return this.name;
	} // end getName()

	/**
	 * Returns the path.
	 * 
	 * @return Returns the path.
	 */
	public final String getPath() {
		return this.path;
	} // end getPath()

	/**
	 * get the resource with the given name
	 * 
	 * @param name
	 *            name of the resource to get
	 * 
	 * @return the resource with the given name
	 */
	private final List getPrivateResource(final String name) {
		try {
			final Object to = this.resourcesMap.get(name);
			final List list = new ArrayList();
			if (to instanceof URL) {
				list.add((URL) to);
				return list;
			} else if (to instanceof List) {
				final List l = (List) to;
				for (int i = 0; i < l.size(); i++) {
					list.add((URL) l.get(i));
				}
				return list;
			} else {
				return null;
			}
		} finally {
			try {

			} catch (final Exception ignore) {
			}
		}
	} // end getPrivateResource()

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.ClassLoader#getResource(java.lang.String)
	 */

	public final URL getResource(final String name) {
		return this.findResource(name);
	} // end getResource()

	public final InputStream getResourceAsStream(final String name) {
		try {
			return this.getResource(name).openStream();
		} catch (final Exception ioe) {
			return null;
		}
	} // end getResourceAsStream()

	/*
	 * 
	 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!! This method is only possible in 1.5 vm upward.
	 * SHAME ON SUN
	 * 
	public Enumeration getResources(String name) throws IOException {
		if (this != CClassLoader.getRootLoader()) {
			return CClassLoader.getRootLoader().getResources(name);
		}
		final CThreadContext context = CThreadContext.getInstance();
		Object obj = context.get(CClassLoader.CCLASSLOADER_NAMESPACE + name
				+ this.getPath());
		if (obj != null) {
			return new Vector().elements();
		}
		context
				.set(CClassLoader.CCLASSLOADER_NAMESPACE + name
						+ this.getPath(), "");
		try {
			return this.findResources(name);
		} finally {
			context.set(CClassLoader.CCLASSLOADER_NAMESPACE + name
					+ this.getPath(), null);
		}
	}
	*/

	/**
	 * return a copy of the known resources
	 * 
	 * @return a copy of the known resources
	 */
	public final Map getResourcesMap() {
		return Collections.unmodifiableMap(this.resourcesMap);
	} // end getResourcesMap()

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.net.URLClassLoader#getURLs()
	 */

	public URL[] getURLs() {
		return CClassLoader.urlLoader != null ? CClassLoader.urlLoader
				.getURLs() : new URL[0];
	} // end getURLs()

	/**
	 * Returns the booAlone.
	 * 
	 * @return Returns the booAlone.
	 */
	public final boolean isAlone() {
		return this.booAlone;
	} // end isAlone()

	/**
	 * return booDoNotForwardToParent
	 * 
	 * @return booDoNotForwardToParent
	 */
	public final boolean isForwardingToParent() {
		return !this.booDoNotForwardToParent;
	} // end isForwardingToParent()

	/**
	 * Returns the booInit.
	 * 
	 * @return Returns the booInit.
	 */
	public final boolean isInit() {
		return this.booInit;
	} // end isInit()

	/**
	 * Returns the booMandatory.
	 * 
	 * @return Returns the booMandatory.
	 */
	public final boolean isMandatory() {
		return this.booMandatory;
	} // end isMandatory()

	/**
	 * Returns the booResourceOnly.
	 * 
	 * @return Returns the booResourceOnly.
	 */
	public final boolean isResourceOnly() {
		return this.booResourceOnly;
	} // end isResourceOnly()

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.ClassLoader#loadClass(java.lang.String, boolean)
	 */

	protected final Class loadClass(final String name, final boolean resolve)
			throws ClassNotFoundException {
		final CThreadContext context = CThreadContext.getInstance();
		try {
			Class c = null;
			final WeakReference cache = (WeakReference) this.cacheMap.get(name);
			if (cache != null) {
				c = (Class) cache.get();
			}

			if (c == null) {
				// First, check if the class has already been loaded
				c = this.findLoadedClass(name);
				if (c == null) {
					c = this.findClass(name);
					this.cacheMap.put(name, new WeakReference(c));
				}
			}
			if (resolve) {
				this.resolveClass(c);
			}

			return c;
		} finally {
			context.set(CClassLoader.CCLASSLOADER_NAMESPACE + name, null);
			try {

			} catch (final Exception ignore) {
			}
		}
	} // end loadClass()

	/**
	 * calculate the loader path
	 * 
	 * @return the calculated path
	 */
	private final String nGetLoaderPath() {
		ClassLoader currentLoader = this;
		final StringBuffer buffer = new StringBuffer();
		buffer.append(this.name);

		while ((currentLoader = currentLoader.getParent()) != null) {
			if (currentLoader.getClass() == CClassLoader.class) {
				buffer.insert(0, "/");
				buffer.insert(0, ((CClassLoader) currentLoader).name);
			} else {
				break;
			}
		}

		return buffer.toString();
	} // end nGetLoaderPath()

	/**
	 * analyse the content of the given jar file
	 * 
	 * @param jarFile
	 *            the jar to analise
	 */
	private final void readDirectories(final URL jarFile) {
		JarInputStream jarIn = null;

		try {
			if (!jarFile.getPath().endsWith(".jar")) {
				return;
			}
			if (CClassLoader.sl(CClassLoader.DEBUG)) {
				CClassLoader.log("opening jar : " + jarFile.toExternalForm(),
						CClassLoader.DEBUG);
			}
			jarIn = new JarInputStream(jarFile.openStream());
			JarEntry jarEntry = null;

			while ((jarEntry = jarIn.getNextJarEntry()) != null) {
				if (jarEntry.isDirectory()) {
					continue;
				}

				final URL url = new URL("yahpjarloader://"
						+ CBASE64Codec.encode(
								jarFile.toExternalForm().getBytes("utf-8"))
								.replaceAll("\n", "") + "/"
						+ jarEntry.getName());

				if (CClassLoader.sl(CClassLoader.DEBUG)) {
					CClassLoader.log("found entry : " + url.toString(),
							CClassLoader.DEBUG);
				}

				if (jarEntry.getName().endsWith(".class")) {
					if (!this.classesMap.containsKey(jarEntry.getName())) {
						if (!this.booResourceOnly) {
							this.classesMap.put(jarEntry.getName(), url);
						}
					}

					if (this.resourcesMap.containsKey(jarEntry.getName())) {
						final Object to = this.resourcesMap.get(jarEntry
								.getName());
						if (to instanceof URL) {
							final URL uo = (URL) to;
							final List l = new ArrayList();
							l.add(uo);
							l.add(url);
							this.resourcesMap.put(jarEntry.getName(), l);
						} else if (to instanceof List) {
							final List uo = (List) to;
							uo.add(url);
							this.resourcesMap.put(jarEntry.getName(), uo);
						}
					} else {
						this.resourcesMap.put(jarEntry.getName(), url);
					}
				} else if (jarEntry.getName().startsWith("native/")) {
					String system = jarEntry.getName().substring(7);
					system = system.substring(0, system.indexOf('/'));
					if (!this.dllMap.containsKey(system)) {
						this.dllMap.put(system, url);
					}
					if (this.resourcesMap.containsKey(jarEntry.getName())) {
						final Object to = this.resourcesMap.get(jarEntry
								.getName());
						if (to instanceof URL) {
							final URL uo = (URL) to;
							final List l = new ArrayList();
							l.add(uo);
							l.add(url);
							this.resourcesMap.put(jarEntry.getName(), l);
						} else if (to instanceof List) {
							final List uo = (List) to;
							uo.add(url);
							this.resourcesMap.put(jarEntry.getName(), uo);
						}
					} else {
						this.resourcesMap.put(jarEntry.getName(), url);
					}
				} else {
					if (this.resourcesMap.containsKey(jarEntry.getName())) {
						final Object to = this.resourcesMap.get(jarEntry
								.getName());
						if (to instanceof URL) {
							final URL uo = (URL) to;
							final List l = new ArrayList();
							l.add(uo);
							l.add(url);
							this.resourcesMap.put(jarEntry.getName(), l);
						} else if (to instanceof List) {
							final List uo = (List) to;
							uo.add(url);
							this.resourcesMap.put(jarEntry.getName(), uo);
						}
					} else {
						this.resourcesMap.put(jarEntry.getName(), url);
					}
				}
			}

			if (CClassLoader.sl(CClassLoader.DEBUG)) {
				CClassLoader.log("opening jar : "
						+ jarFile.getFile().toString() + " done.",
						CClassLoader.DEBUG);
			}
		} catch (final MalformedURLException mue) {
			mue.printStackTrace();

			if (CClassLoader.sl(CClassLoader.FATAL)) {
				CClassLoader.log(mue.getMessage(), CClassLoader.FATAL);
			}
		} catch (final IOException ioe) {
			ioe.printStackTrace();

			if (CClassLoader.sl(CClassLoader.FATAL)) {
				CClassLoader.log(ioe.getMessage(), CClassLoader.FATAL);
			}
		} catch (final Exception e) {
			e.printStackTrace();

			if (CClassLoader.sl(CClassLoader.FATAL)) {
				CClassLoader.log(e.getMessage(), CClassLoader.FATAL);
			}
		} finally {
			try {
				jarIn.close();
				jarIn = null;
			} catch (final Exception e) {
			}
		}
	} // end readDirectories()

	/**
	 * reload this loader
	 */
	public final void reload() {
		this.reload(this.config);
	} // end reload()

	/**
	 * reload this loader
	 * 
	 * @param config
	 *            a loader config object
	 */
	public final void reload(final CClassLoaderConfig config) {
		if (this == CClassLoader.getRootLoader()) {
			return;
		}

		if (config == null) {
			return;
		}

		final CClassLoader parent = ((CClassLoader) this.getParent());
		parent.removeLoader(this.name);

		if (this.isMandatory()) {
			CClassLoader.mandatoryLoadersMap.remove(this.getPath());
		}

		final CClassLoader newLoader = CClassLoader.getLoader(parent.getPath()
				+ "/" + this.name);
		newLoader.config = this.config;
		newLoader.booAlone = this.booAlone;
		newLoader.booMandatory = this.booMandatory;
		newLoader.booResourceOnly = this.booResourceOnly;
		newLoader.booDoNotForwardToParent = this.booDoNotForwardToParent;

		final List list = (List) config.getFilesMap().get(this.path);

		for (final Iterator f = list.iterator(); f.hasNext();) {
			final URL file = (URL) f.next();
			newLoader.readDirectories(file);
		}

		final Iterator it = this.childrenMap.keySet().iterator();

		while (it.hasNext()) {
			final CClassLoader child = (CClassLoader) this.childrenMap.get(it
					.next());
			child.reload();
		}

		this._destroy(null);

		if (newLoader.isMandatory()) {
			CClassLoader.mandatoryLoadersMap
					.put(newLoader.getPath(), newLoader);
		}

		newLoader.booInit = true;
	} // end reload()

	/**
	 * remove the given loader from this loader
	 * 
	 * @param loaderToRemove
	 *            name of the loader to remove. NOT NULL.
	 * 
	 * @return the removed loader or null.
	 * 
	 * @throws NullPointerException
	 *             if loaderToRemove is null or a zero length/blank string
	 */
	public final CClassLoader removeLoader(final String loaderToRemove) {
		if (loaderToRemove.trim().length() == 0) {
			throw new NullPointerException();
		}

		return (CClassLoader) this.childrenMap.remove(loaderToRemove);
	} // end removeLoader()

	/**
	 * remove the given resource name from known resource
	 * 
	 * @param resourceName
	 *            name of the resource to remove
	 */
	public final void removeResource(final String resourceName) {
		if (resourceName == null) {
			return;
		}

		this.resourcesMap.remove(resourceName.replace('\\', '/'));
	} // end removeResource()

	public String toString() {
		return this.getPath();
	}
} // end CClassLoader

