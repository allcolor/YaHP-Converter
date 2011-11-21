package org.allcolor.yahp.converter;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.Hashtable;
import java.util.Map;

public class CYaHPURLStreamHandlerFactory implements URLStreamHandlerFactory {
	private Map mapHandler = new Hashtable();

	private URLStreamHandlerFactory oldFactory = null;

	public CYaHPURLStreamHandlerFactory(URLStreamHandlerFactory oldFactory) {
		this.oldFactory = oldFactory;
		this.registerURLStreamHandler("yahpjarloader", CJarLoaderURLStreamHandler
				.getInstance());
		this
				.registerURLStreamHandler("yahpmemory", CMemoryURLHandler
						.getInstance());
	}

	public URLStreamHandler createURLStreamHandler(String protocol) {
		URLStreamHandler handler = (URLStreamHandler)this.mapHandler.get(protocol);
		if (handler != null) {
			return handler;
		}
		if (this.oldFactory == null) {
			try {
				String packageProperty = System
						.getProperty("java.protocol.handler.pkgs");
				String[] listPackages = packageProperty.split(",");
				for (int i=0;i<listPackages.length;i++) { 
					String element = listPackages[i];
					String clsName = element + "." + protocol + ".Handler";
					Class cls = null;
					try {
						cls = Class.forName(clsName);
					} catch (ClassNotFoundException e) {
						ClassLoader cl = ClassLoader.getSystemClassLoader();
						if (cl != null) {
							cls = cl.loadClass(clsName);
						}
					}
					if (cls != null) {
						return (URLStreamHandler) cls.newInstance();
					}
				}
			} catch (Exception e) {
			}
			return null;
		} else {
			return this.oldFactory.createURLStreamHandler(protocol);
		}
	}

	public void registerURLStreamHandler(String protocol,
			URLStreamHandler handler) {
		this.mapHandler.put(protocol, handler);
	}

}
