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
package org.allcolor.yahp.sample;
import org.allcolor.yahp.converter.CYaHPConverter;
import org.allcolor.yahp.converter.IHtmlToPdfTransformer;

import java.io.File;
import java.io.FileOutputStream;

import java.net.URL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A simple example to convert an URL pointing to an html file into a
 * PDF.
 *
 * @author Quentin Anciaux
 * @version 0.91
 */
public class CSimpleConversion {
	/** An handle to a yahp converter */
	private static CYaHPConverter converter = new CYaHPConverter();

	/**
	 * Start the Simple Conversion tool
	 *
	 * @param args startup arguments
	 *
	 * @throws Exception if an error occured while converting. exit
	 * 		   status = 0 if all is ok.
	 */
	public static void main(final String args[])
		throws Exception {
		if (hasParameter(args,"--help") ||
			hasParameter(args,"-h")) {
			showUsage(null);
		}
		String  url				 = getParameter(args, "--url");
		String  outfile			 = getParameter(args, "--out");
		String  fontPath	 	 = getParameter(args, "--fontpath");
		String  password		 = getParameter(args, "--password");
		String  keystorePath     = getParameter(args, "--ks");
		String  keystorePassword = getParameter(args, "--kspassword");
		String  keyPassword		 = getParameter(args, "--keypassword");
		String  cryptReason		 = getParameter(args, "--cryptreason");
		String  cryptLocation    = getParameter(args, "--cryptlocation");
		
		if (outfile == null) {
			showUsage("--out file must exists !");
		} // end if

		try {
			new URL(url);
		} // end try
		catch (final Exception e) {
			showUsage("--url must be a valid URL !");
		} // end catch
		try {
			File fout = new File(outfile);

			List			 headerFooterList = new ArrayList();

			FileOutputStream out = new FileOutputStream(fout);
			System.out.println("before conversion");

			Map properties = new HashMap();

			headerFooterList.add(new IHtmlToPdfTransformer.CHeaderFooter(
					"<table width=\"100%\"><tbody><tr><td align=\"left\">Generated with YaHPConverter.</td><td align=\"right\">Page <pagenumber>/<pagecount></td></tr></tbody></table>",
					IHtmlToPdfTransformer.CHeaderFooter.HEADER));
			headerFooterList.add(new IHtmlToPdfTransformer.CHeaderFooter(
					"© 2011 Quentin Anciaux",
					IHtmlToPdfTransformer.CHeaderFooter.FOOTER));
			
			properties.put(IHtmlToPdfTransformer.PDF_RENDERER_CLASS,
					IHtmlToPdfTransformer.FLYINGSAUCER_PDF_RENDERER);
			if (fontPath != null)
				properties.put(IHtmlToPdfTransformer.FOP_TTF_FONT_PATH, fontPath);

			if (password != null) {
				System.out.println(password);
				properties.put(IHtmlToPdfTransformer.USE_PDF_ENCRYPTION,
					"true");
				properties.put(IHtmlToPdfTransformer.PDF_ALLOW_SCREEN_READERS,
					"true");
				properties.put(IHtmlToPdfTransformer.PDF_ENCRYPTION_PASSWORD,
					password);

				if (keystorePath != null) {
					properties.put(IHtmlToPdfTransformer.USE_PDF_SIGNING,
						"true");
					properties.put(IHtmlToPdfTransformer.PDF_SIGNING_PRIVATE_KEY_FILE,
						keystorePath);
					properties.put(IHtmlToPdfTransformer.PDF_SIGNING_KEYSTORE_PASSWORD,
						keystorePassword);
					properties.put(IHtmlToPdfTransformer.PDF_SIGNING_PRIVATE_KEY_PASSWORD,
						keyPassword);

					if (cryptReason != null) {
						properties.put(IHtmlToPdfTransformer.PDF_SIGNING_REASON,
							cryptReason);
					} // end if

					if (cryptLocation != null) {
						properties.put(IHtmlToPdfTransformer.PDF_SIGNING_LOCATION,
							cryptLocation);
					} // end if
				} // end if
			} // end if

			converter.convertToPdf(new URL(url),
				IHtmlToPdfTransformer.A4P, headerFooterList, out,
				properties);
			System.out.println("after conversion");
			out.flush();
			out.close();
		} // end try
		catch (final Throwable t) {
			t.printStackTrace();
			System.err.println("An error occurs while converting '" +
				url + "' to '" + outfile + "'. Cause : " +
				t.getMessage());
			System.exit(-1);
		} // end catch

		System.exit(0);
	} // end main()

	/**
	 * Return the value of the given parameter if set
	 *
	 * @param args startup arguments
	 * @param name parameter name
	 *
	 * @return the value of the given parameter if set or null
	 */
	private static String getParameter(
		final String args[],
		final String name) {
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals(name)) {
				if ((i + 1) < args.length) {
					return args[i + 1];
				} // end if

				break;
			} // end if
		} // end for

		return null;
	} // end getParameter()

	/**
	 * return true if the given parameter is on the command line
	 *
	 * @param args startup arguments
	 * @param name parameter name
	 *
	 * @return true if the given parameter is on the command line
	 */
	private static boolean hasParameter(
		final String args[],
		final String name) {
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals(name)) {
				return true;
			} // end if
		} // end for

		return false;
	} // end hasParameter()

	/**
	 * Show the usage of the tool
	 *
	 * @param message An error message
	 */
	private static void showUsage(final String message) {
		if (message != null) {
			System.out.println(message);
		} // end if

		System.out.println(
			"Usage :\n\tjava -cp yahp-sample.jar:yahp.jar org.allcolor.yahp.sample.CSimpleConversion" +
			" --url [http|file]://myuri --out /path/to.pdf [font options] [renderer options] [security options] [--help|-h]");
		System.out.println("\t[font options]:");
		System.out.println("\t\t[--fontpath directory where TTF font files are located]");
		System.out.println("\t[renderer options]:");
		System.out.println("\t(Default renderer use Flying Saucer XHTML renderer. no option.)");
		System.out.println("\t[security options]:");
		System.out.println("\t\t[--password password]");
		System.out.println("\t\t[--ks keystore file path]");
		System.out.println("\t\t[--kspassword keystore file password]");
		System.out.println("\t\t[--keypassword private key password]");
		System.out.println("\t\t[--cryptreason reason]");
		System.out.println("\t\t[--cryptlocation location]");
		if (message != null) {
			System.exit(-2);
		} else {
			System.exit(0);
		}
	} // end showUsage()
} // end CSimpleConversion
