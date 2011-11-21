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
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.OutputStream;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.allcolor.yahp.converter.IHtmlToPdfTransformer.CConvertException;
//import org.apache.log4j.PropertyConfigurator;

/**
 * This class convert an HTML input to a PDF.
 *
 * @author Quentin Anciaux
 * @version 0.94
 */
public final class CYaHPConverter {
	static {
		try {
			System.setProperty("file.encoding", "utf-8");
		}
		catch (Exception ignore){}
	}
	/** an handle to a mutex object for thread safety */
	private static final CMutex countMutex = new CMutex();

	/** Number of active instance */
	private static int countInstance = 0;

	/** an handle to a mutex object for thread safety */
	private final CMutex mutex = new CMutex();

	/** an handle to the known transformers */
	private Map transformers = new HashMap();

	/**
	 * Creates a new CYaHPConverter object.
	 */
	public CYaHPConverter() {
		this(true);
	} // end CYaHPConverter()

	private final boolean useClassLoader;
	
	/**
	 * Creates a new CYaHPConverter object.
	 */
	public CYaHPConverter(boolean useClassLoader) {
		this.useClassLoader = useClassLoader;
		try {
			countMutex.acquire();
			countInstance++;
			init(useClassLoader);
		} // end try
		finally {
			try {
				countMutex.release();
			} // end try
			catch (final Exception ignore) {}
		} // end finally
	} // end CYaHPConverter()
	
	/**
	 * Delete the jar file from the temp directory Destroy the
	 * classloader
	 */
	public static void destroy() {
		try {
			countMutex.acquire();
			for (int i=0;i<fileToDeleteOnDestroy.size();i++) {
				File f = (File)fileToDeleteOnDestroy.get(i);
				if (f != null && f.exists())
					try{f.delete();}catch(Exception ignore){}
			}
			CClassLoader.destroy();
		} // end try
		finally {
			countInstance = 0;

			try {
				countMutex.release();
			} // end try
			catch (final Exception e) {}
		} // end finally
	} // end destroy()

	private static List fileToDeleteOnDestroy = new ArrayList();
	
	public static void registerFileToDeleteOnDestroy(File f) {
		fileToDeleteOnDestroy.add(f);
	}
	
	/**
	 * Convert the document pointed by url in a PDF file. This method
	 * is thread safe.
	 *
	 * @param url Url to the document
	 * @param size PDF Page size
	 * @param hf header-footer list
	 * @param out outputstream to render into
	 * @param fproperties properties map
	 *
	 * @throws CConvertException if an unexpected error occurs.
	 */
	public final void convertToPdf(
		final URL							 url,
		final IHtmlToPdfTransformer.PageSize size,
		final List							 hf,
		final OutputStream					 out,
		final Map							 fproperties) 
	throws CConvertException {
		Map		    properties = (fproperties != null)
			? fproperties
			: new HashMap();
		ClassLoader loader = Thread.currentThread()
									   .getContextClassLoader();
		int priority = Thread.currentThread().getPriority();

		try {
			Thread.currentThread()
					  .setContextClassLoader(new URLClassLoader(new URL[0],this.useClassLoader ? CClassLoader.getLoader(
						"/main") : this.getClass().getClassLoader()));
			Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

			String uri = url.toExternalForm();
			if (uri.indexOf("://") != -1) {
				String tmp = uri.substring(uri.indexOf("://")+3);
				if (tmp.indexOf('/') == -1) {
					uri += "/";
				}
			}
			uri = uri.substring(0, uri.lastIndexOf("/") + 1);

			if (uri.startsWith("file:/")) {
				uri = uri.substring(6);

				while (uri.startsWith("/")) {
					uri = uri.substring(1);
				} // end while

				uri = "file:///" + uri;
			} // end if

			try {
				IHtmlToPdfTransformer transformer = getTransformer(properties);
				transformer.transform(url.openStream(), uri, size, hf,
					properties, out);
			} // end try
			catch (final CConvertException e) {
				throw e;
			}
			catch (final Exception e) {
				throw new CConvertException(e.getMessage(),e);
			} // end catch
		} // end try
		finally {
			Thread.currentThread().setContextClassLoader(loader);
			Thread.currentThread().setPriority(priority);
		} // end finally
	} // end convertToPdf()

	/**
	 * Convert the document in content in a PDF file. This method is
	 * thread safe.
	 *
	 * @param content the html document as a string
	 * @param size PDF Page size
	 * @param hf header-footer list
	 * @param furlForBase base url of the document, mandatory, must end
	 * 		  with a '/'
	 * @param out outputstream to render into
	 * @param fproperties properties map
	 *
	 * @throws CConvertException if an unexpected error occurs
	 */
	public final void convertToPdf(
		final String						 content,
		final IHtmlToPdfTransformer.PageSize size,
		final List							 hf,
		final String						 furlForBase,
		final OutputStream					 out,
		final Map							 fproperties)
	throws CConvertException {
		String urlForBase = furlForBase;
		Map    properties = (fproperties != null)
			? fproperties
			: new HashMap();

		if (urlForBase != null) {
			try {
				URL url = new URL(urlForBase);

				if (url == null) {
					throw new CConvertException(
						"urlForBase must be a valid URI.",null);
				} // end if
			} // end try
			catch (final Exception e) {
				throw new CConvertException(
					"urlForBase must be a valid URI.",null);
			} // end catch
			if (urlForBase.indexOf("://") != -1) {
				String tmp = urlForBase.substring(urlForBase.indexOf("://")+3);
				if (tmp.indexOf('/') == -1) {
					urlForBase += "/";
				}
			}
		} // end if

		ClassLoader loader = Thread.currentThread()
									   .getContextClassLoader();
		int priority = Thread.currentThread().getPriority();
		try {
			Thread.currentThread()
					  .setContextClassLoader(new URLClassLoader(new URL[0],this.useClassLoader ? CClassLoader.getLoader(
						"/main") : this.getClass().getClassLoader()));
			Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

			try {
				IHtmlToPdfTransformer transformer = getTransformer(properties);
				transformer.transform(new ByteArrayInputStream(
						content.getBytes("utf-8")), urlForBase, size, hf,
					properties, out);
			} // end try
			catch (final CConvertException e) {
				throw e;
			}
			catch (final Exception e) {
				throw new CConvertException(e.getMessage(),e);
			} // end catch
		} // end try
		finally {
			Thread.currentThread().setContextClassLoader(loader);
			Thread.currentThread().setPriority(priority);
		} // end finally
	} // end convertToPdf()

	/**
	 * If instance count fall to 0, Delete the jar file from the temp
	 * directory, Destroy the classloader by calling the destroy
	 * method
	 *
	 * @throws Throwable if an unexpected error occurs
	 */
	protected final void finalize()
		throws Throwable {
		super.finalize();

		try {
			countMutex.acquire();
			countInstance--;

			if (countInstance == 0) {
				destroy();
			} // end if
		} // end try
		finally {
			try {
				countMutex.release();
			} // end try
			catch (final Exception e) {}
		} // end finally
	} // end finalize()

	/**
	 * initialize the classloader, and the transformer
	 */
	private final void init(boolean useClassLoader) {
		final CYaHPConverter converter = this;
		System.out.println("Initializing...");
		long time = System.currentTimeMillis();
		
		if (!useClassLoader) {
			System.out.println("init time: "+(System.currentTimeMillis()-time));
			return;
		}

		// if classloader is init return.
		if (CClassLoader.getRootLoader().isInit()) {
			return;
		} // end if
		// ensure the temporary files are delete on exit.
		Runtime.getRuntime().addShutdownHook(new Thread() {
				public void run() {
					try {
						converter.finalize();
					} // end try
					catch (final Throwable e) {}
				} // end run()
			} // end new
		);

		ClassLoader loader = this.getClass().getClassLoader();
		CClassLoaderConfig config = new CClassLoaderConfig();
		config.addLoaderInfo("/main",
			new CClassLoaderConfig.CLoaderInfo(true, true, false, false));
		config.addFile("/main", loader.getResource("itext-yahp.jar"));
		config.addFile("/main", loader.getResource("tidy-yahp.jar"));
		config.addFile("/main", loader.getResource("log4j-yahp.jar"));
		config.addFile("/main", loader.getResource("shanijar-yahp.jar"));
		config.addFile("/main", loader.getResource("xmlapi-yahp.jar"));
		config.addFile("/main", loader.getResource("yahp-internal.jar"));
		config.addFile("/main", loader.getResource("commonio-yahp.jar"));
		config.addFile("/main", loader.getResource("commonlog-yahp.jar"));
		config.addFile("/main", loader.getResource("core-renderer-yahp.jar"));
		config.addFile("/main", loader.getResource("jaxen-yahp.jar"));
		CClassLoader.init(config);
		try {
			URL url = CClassLoader.getRootLoader().getResource("log4j.properties");
			if (url != null) {
				ClassLoader cx = Thread.currentThread().getContextClassLoader();
				try {
					Thread.currentThread().setContextClassLoader(CClassLoader.getRootLoader());
					Class pc = CClassLoader.getRootLoader().loadClass("org.apache.log4j.PropertyConfigurator");
					Method configure = pc.getDeclaredMethod("configure", new Class[]{URL.class});
					configure.invoke(null, new Object[]{url});
				}
				finally {
					Thread.currentThread().setContextClassLoader(cx);
				}
			}
		}
		catch(Exception ignore){}
		System.out.println("init time: "+(System.currentTimeMillis()-time));
	} // end init()

	/**
	 * Get a transformer.
	 *
	 * @param properties The properties map.
	 *
	 * @return a transformer corresponding to the criterias found in
	 * 		   the properties map.
	 */
	private IHtmlToPdfTransformer getTransformer(final Map properties) {
		try {
			mutex.acquire();
			IHtmlToPdfTransformer transformer	    = null;
			String				  rendererClassName = IHtmlToPdfTransformer.DEFAULT_PDF_RENDERER;
	
			if ((properties != null) &&
					properties.containsKey(
						IHtmlToPdfTransformer.PDF_RENDERER_CLASS)) {
				rendererClassName = (String) properties.get(IHtmlToPdfTransformer.PDF_RENDERER_CLASS);
			} // end if
	
			if (transformers.containsKey(rendererClassName)) {
				transformer = (IHtmlToPdfTransformer) transformers.get(rendererClassName);
			} // end if
			else {
				ClassLoader bootStrap = this.useClassLoader ? CClassLoader.getLoader("/main") : this.getClass().getClassLoader();
	
				try {
					ClassLoader cx = Thread.currentThread().getContextClassLoader();
					try {
						Thread.currentThread().setContextClassLoader(this.useClassLoader ? CClassLoader.getRootLoader() : this.getClass().getClassLoader());
						transformer = (IHtmlToPdfTransformer) bootStrap.loadClass(rendererClassName)
																		   .newInstance();
						transformers.put(rendererClassName, transformer);
					}
					finally {
						Thread.currentThread().setContextClassLoader(cx);
					}
				} // end try
				catch (final Exception e) {
					e.printStackTrace();
					System.err.println(
						"SEVERE: Error while getting transformer '" +
						rendererClassName + "' ! : "+e.getMessage());
	
					return null;
				} // end catch
			} // end else
	
			return transformer;
		}
		finally {
			try {
				mutex.release();
			} // end try
			catch (final Exception e) {}
		} // end finally
		
	} // end getTransformer()
} // end CYaHPConverter
