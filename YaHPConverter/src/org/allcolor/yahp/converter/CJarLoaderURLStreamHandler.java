package org.allcolor.yahp.converter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Hashtable;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class CJarLoaderURLStreamHandler extends URLStreamHandler {
	/**
	 * This class will clean the preload map entry upon GC
	 * 
	 * @author Quentin Anciaux
	 * @version 0.1
	 */
	public static class CGCCleaner {
		/** preload entry map key */
		String name;

		/** Encapsulated reference object */
		Object ref;

		/**
		 * Creates a new CGCCleaner object.
		 * 
		 * @param name
		 *            preload entry map key
		 * @param ref
		 *            The object to encapsulate
		 */
		CGCCleaner(final String name, final Object ref) {
			this.name = name;
			this.ref = ref;
		}

		/**
		 * Upon GC remove the entry from the preload map
		 * 
		 * @throws Throwable
		 *             shouldn't happen
		 */
		public void finalize() throws Throwable {
			super.finalize();
			if (CJarLoaderURLStreamHandler.handler.preload
					.containsKey(this.name)) {
				CJarLoaderURLStreamHandler.handler.preload.remove(this.name);
			}
		} // end finalize()

		/**
		 * return the reference object
		 * 
		 * @return the reference object
		 */
		public Object get() {
			return this.ref;
		} // end get()
	} // end CGCCleaner

	private static CJarLoaderURLStreamHandler handler = new CJarLoaderURLStreamHandler();

	public static CJarLoaderURLStreamHandler getInstance() {
		return CJarLoaderURLStreamHandler.handler;
	}

	/** preload byte class map */
	private Map preload = new Hashtable();

	public CJarLoaderURLStreamHandler() {
	}

	/**
	 * add a class byte representation in the preload map
	 * 
	 * @param path
	 *            path to the class inside the jar
	 * @param array
	 *            byte representation
	 */
	public void addClassPreload(final String path, final byte array[]) {
		try {
			this.preload.put(path, new SoftReference(
					new CGCCleaner(path, array)));
		} catch (Exception ignore) {
		}
	} // end addClassPreload()

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
			ByteArrayOutputStream bOut = new ByteArrayOutputStream();

			byte buffer[] = new byte[2048];

			int iNbByteRead = -1;

			while ((iNbByteRead = in.read(buffer)) != -1) {
				bOut.write(buffer, 0, iNbByteRead);
			} // end while

			return bOut.toByteArray();
		} // end try
		catch (final IOException ioe) {
			return null;
		} // end catch
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
			URLConnection uc = urlToResource.openConnection();
			uc.setUseCaches(false);
			in = uc.getInputStream();

			return loadByteArray(in);
		} // end try
		catch (final IOException ioe) {
			return null;
		} // end catch
		finally {
			try {
				in.close();
			} // end try
			catch (final Exception ignore) {
			}
		} // end finally
	} // end loadByteArray()
	
	protected URLConnection openConnection(URL u) throws IOException {
		String up = u.toExternalForm();
		String path = u.getPath();
		if (path.startsWith("/")) {
			path = path.substring(1);
		}
		if (this.preload.containsKey(up)) {
			SoftReference ref = (SoftReference)this.preload.get(up);
			CGCCleaner cleaner = (CGCCleaner)ref.get();

			if (cleaner == null) {
				try {
					this.preload.remove(up);
				} catch (Exception ignore) {
				}
			} // end if
			else {
				byte array[] = (byte[]) cleaner.get();

				return new CByteArrayUrlConnection(u, array);
			} // end else
		} // end if
		URL file = new URL(
				new String(CBASE64Codec.decode(u.getHost()), "utf-8"));
		JarInputStream in = new JarInputStream(file.openStream());
		JarEntry entry = null;

		while ((entry = in.getNextJarEntry()) != null) {
			if (entry.getName().equals(path)) {
				break;
			} // end if
			else {
				URL newU = new URL("yahpjarloader://" + u.getHost() + "/"
						+ entry.getName());
				this.addClassPreload(newU.toExternalForm(), loadByteArray(in));
			} // end else if
		} // end while

		if (entry == null) {
			in.close();
			throw new IOException("Resource not found ! : " + path + " - "
					+ file.toExternalForm());
		} // end if
		else {
			byte array[] = loadByteArray(in);
			in.close();

			return new CByteArrayUrlConnection(u, array);
		} // end else
	}

}
