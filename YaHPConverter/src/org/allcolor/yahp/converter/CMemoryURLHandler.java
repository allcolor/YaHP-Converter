package org.allcolor.yahp.converter;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Hashtable;
import java.util.Map;

public class CMemoryURLHandler extends URLStreamHandler {
	private static CMemoryURLHandler handler = new CMemoryURLHandler();
	public static CMemoryURLHandler getInstance() {
		return handler;
	}
	private static Map mapMem = new Hashtable();

	protected static URL createMemoryURL(String entryName, byte[] entry)
			throws MalformedURLException {
		if (!entryName.startsWith("/")) {
			entryName = "/" + entryName;
		}
		URL url = new URL("yahpmemory", "localhost", -1, entryName,
				handler);
		CMemoryURLHandler.mapMem.put(url.toExternalForm(), entry);
		return url;
	}

	protected static void releaseMemoryURL(URL u) {
		CMemoryURLHandler.mapMem.remove(u.toExternalForm());
	}

	protected URLConnection openConnection(URL u) throws IOException {
		byte[] array = null;
		array = (byte[])CMemoryURLHandler.mapMem.get(u.toExternalForm());
		if (array == null) {
			throw new IOException("resource " + u.toExternalForm()
					+ " not found !");
		}
		return new CByteArrayUrlConnection(u, array);
	}

}
