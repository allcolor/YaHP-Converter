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
package org.allcolor.yahp.cl.converter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.allcolor.yahp.converter.IHtmlToPdfTransformer;
import org.allcolor.yahp.converter.IHtmlToPdfTransformer.CConvertException;
import org.allcolor.yahp.converter.IHtmlToPdfTransformer.CHeaderFooter;
import org.allcolor.yahp.converter.IHtmlToPdfTransformer.PageSize;

import com.lowagie.text.Meta;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfSignatureAppearance;
import com.lowagie.text.pdf.PdfStamper;
import com.lowagie.text.pdf.PdfWriter;

/**
 * Use iText to construct a complete pdf document from differents pdf parts.
 * Apply configured security policies on the resulting pdf.
 * 
 * @author Quentin Anciaux
 * @version 1.2.20b
 */
public class CDocumentReconstructor {
	/**
	 * return the itext security flags for encryption
	 * 
	 * @param properties
	 *            the converter properties
	 * 
	 * @return the itext security flags
	 */
	private static final int getSecurityFlags(final Map properties) {
		int securityType = 0;
		securityType = "true".equals(properties
				.get(IHtmlToPdfTransformer.PDF_ALLOW_PRINTING)) ? (securityType | PdfWriter.ALLOW_PRINTING)
				: securityType;
		securityType = "true".equals(properties
				.get(IHtmlToPdfTransformer.PDF_ALLOW_MODIFY_CONTENTS)) ? (securityType | PdfWriter.ALLOW_MODIFY_CONTENTS)
				: securityType;
		securityType = "true".equals(properties
				.get(IHtmlToPdfTransformer.PDF_ALLOW_COPY)) ? (securityType | PdfWriter.ALLOW_COPY)
				: securityType;
		securityType = "true".equals(properties
				.get(IHtmlToPdfTransformer.PDF_ALLOW_MODIFT_ANNOTATIONS)) ? (securityType | PdfWriter.ALLOW_MODIFY_ANNOTATIONS)
				: securityType;
		securityType = "true".equals(properties
				.get(IHtmlToPdfTransformer.PDF_ALLOW_FILLIN)) ? (securityType | PdfWriter.ALLOW_FILL_IN)
				: securityType;
		securityType = "true".equals(properties
				.get(IHtmlToPdfTransformer.PDF_ALLOW_SCREEN_READERS)) ? (securityType | PdfWriter.ALLOW_SCREENREADERS)
				: securityType;
		securityType = "true".equals(properties
				.get(IHtmlToPdfTransformer.PDF_ALLOW_ASSEMBLY)) ? (securityType | PdfWriter.ALLOW_ASSEMBLY)
				: securityType;
		securityType = "true".equals(properties
				.get(IHtmlToPdfTransformer.PDF_ALLOW_DEGRADED_PRINTING)) ? (securityType | PdfWriter.ALLOW_DEGRADED_PRINTING)
				: securityType;

		return securityType;
	} // end getSecurityFlags()

	/**
	 * construct a pdf document from pdf parts.
	 * 
	 * @param files
	 *            list containing the pdf to assemble
	 * @param properties
	 *            converter properties
	 * @param fout
	 *            outputstream to write the new pdf
	 * @param base_url
	 *            base url of the document
	 * @param producer
	 *            producer of the pdf
	 * 
	 * @throws CConvertException
	 *             if an error occured while reconstruct.
	 */
	public static void reconstruct(final List files, final Map properties,
			final OutputStream fout, final String base_url,
			final String producer, final PageSize [] size, final List hf)
			throws CConvertException {
		OutputStream out = fout;
		OutputStream out2 = fout;
		boolean signed = false;
		OutputStream oldOut = null;
		File tmp = null;
		File tmp2 = null;
		try {
			tmp = File.createTempFile("yahp", "pdf");
			tmp2 = File.createTempFile("yahp", "pdf");
			oldOut = out;
			if ("true".equals(properties
					.get(IHtmlToPdfTransformer.USE_PDF_SIGNING))) {
				signed = true;
				out2 = new FileOutputStream(tmp2);
			} // end if
			else {
				out2 = oldOut;
			}
			out = new FileOutputStream(tmp);
			com.lowagie.text.Document document = null;
			PdfCopy writer = null;
			boolean first = true;

			
			Map mapSizeDoc = new HashMap();
			
			int totalPage = 0;
			
			for (int i = 0; i < files.size(); i++) {
				final File fPDF = (File) files.get(i);
				final PdfReader reader = new PdfReader(fPDF.getAbsolutePath());
				reader.consolidateNamedDestinations();

				final int n = reader.getNumberOfPages();

				if (first) {
					first = false;
					// step 1: creation of a document-object
					// set title/creator/author
					document = new com.lowagie.text.Document(reader
							.getPageSizeWithRotation(1));
					// step 2: we create a writer that listens to the document
					writer = new PdfCopy(document, out);
					// use pdf version 1.5
					writer.setPdfVersion(PdfWriter.VERSION_1_3);
					// compress the pdf
					writer.setFullCompression();

					// check if encryption is needed
					if ("true".equals(properties
							.get(IHtmlToPdfTransformer.USE_PDF_ENCRYPTION))) {
						final String password = (String) properties
								.get(IHtmlToPdfTransformer.PDF_ENCRYPTION_PASSWORD);
						final int securityType = CDocumentReconstructor
								.getSecurityFlags(properties);
						writer.setEncryption(PdfWriter.STANDARD_ENCRYPTION_128,
								password, null, securityType);
					} // end if

					final String title = (String) properties
							.get(IHtmlToPdfTransformer.PDF_TITLE);

					if (title != null) {
						document.addTitle(title);
					} // end if
					else if (base_url != null) {
						document.addTitle(base_url);
					} // end else if

					final String creator = (String) properties
							.get(IHtmlToPdfTransformer.PDF_CREATOR);

					if (creator != null) {
						document.addCreator(creator);
					} // end if
					else {
						document.addCreator(IHtmlToPdfTransformer.VERSION);
					} // end else

					final String author = (String) properties
							.get(IHtmlToPdfTransformer.PDF_AUTHOR);

					if (author != null) {
						document.addAuthor(author);
					} // end if

					final String sproducer = (String) properties
							.get(IHtmlToPdfTransformer.PDF_PRODUCER);

					if (sproducer != null) {
						document.add(new Meta("Producer", sproducer));
					} // end if
					else {
						document.add(new Meta("Producer", (IHtmlToPdfTransformer.VERSION + " - http://www.allcolor.org/YaHPConverter/ - " + producer)));
					} // end else

					// step 3: we open the document
					document.open();
				} // end if

				PdfImportedPage page;

				for (int j = 0; j < n;) {
					++j;
					totalPage++;
					mapSizeDoc.put(""+totalPage,""+i);
					page = writer.getImportedPage(reader, j);
					writer.addPage(page);
				} // end for
			} // end for

			document.close();
			out.flush();
			out.close();
			{
				final PdfReader reader = new PdfReader(tmp.getAbsolutePath());
				;
				final int n = reader.getNumberOfPages();
				final PdfStamper stp = new PdfStamper(reader, out2);
				int i = 0;
				BaseFont.createFont(BaseFont.HELVETICA,
						BaseFont.WINANSI, BaseFont.EMBEDDED);
				final CHtmlToPdfFlyingSaucerTransformer trans = new CHtmlToPdfFlyingSaucerTransformer();
				while (i < n) {
					i++;
					int indexSize = Integer.parseInt((String)mapSizeDoc.get(""+i));
					final int[] dsize = size[indexSize].getSize();
					final int[] dmargin = size[indexSize].getMargin();
					for (final Iterator it = hf.iterator(); it.hasNext();) {
						final CHeaderFooter chf = (CHeaderFooter) it.next();
						if (chf.getSfor().equals(CHeaderFooter.ODD_PAGES)
								&& (i % 2 == 0)) {
							continue;
						} else if (chf.getSfor().equals(
								CHeaderFooter.EVEN_PAGES)
								&& (i % 2 != 0)) {
							continue;
						}
						final String text = chf.getContent().replaceAll(
								"<pagenumber>", "" + i).replaceAll(
								"<pagecount>", "" + n);
						// text over the existing page
						final PdfContentByte over = stp.getOverContent(i);
						final ByteArrayOutputStream bbout = new ByteArrayOutputStream();
						if (chf.getType().equals(CHeaderFooter.HEADER)) {
							trans.transform(new ByteArrayInputStream(text.getBytes("utf-8")), base_url, new PageSize(dsize[0]-(dmargin[0]+dmargin[1]),dmargin[3]), new ArrayList(), properties, bbout);
						} else if (chf.getType().equals(CHeaderFooter.FOOTER)) {
							trans.transform(new ByteArrayInputStream(text.getBytes("utf-8")), base_url, new PageSize(dsize[0]-(dmargin[0]+dmargin[1]),dmargin[2]), new ArrayList(), properties, bbout);
						}
						final PdfReader readerHF = new PdfReader(
								bbout.toByteArray()
						);
						if (chf.getType().equals(CHeaderFooter.HEADER)) {
							over.addTemplate(stp.getImportedPage(readerHF, 1),dmargin[0],dsize[1] - dmargin[3]);
						} else if (chf.getType().equals(CHeaderFooter.FOOTER)) {
							over.addTemplate(stp.getImportedPage(readerHF, 1),dmargin[0],0);
						}
			            readerHF.close();
					}
				}
				stp.close();
			}
			try {
				out2.flush();
			}
			catch (Exception ignore) {}
			finally {
				try{out2.close();
				}catch(Exception ignore){}
			}
			if (signed) {

				final String keypassword = (String) properties
						.get(IHtmlToPdfTransformer.PDF_SIGNING_PRIVATE_KEY_PASSWORD);
				final String password = (String) properties
						.get(IHtmlToPdfTransformer.PDF_ENCRYPTION_PASSWORD);
				final String keyStorepassword = (String) properties
						.get(IHtmlToPdfTransformer.PDF_SIGNING_KEYSTORE_PASSWORD);
				final String privateKeyFile = (String) properties
						.get(IHtmlToPdfTransformer.PDF_SIGNING_PRIVATE_KEY_FILE);
				final String reason = (String) properties
						.get(IHtmlToPdfTransformer.PDF_SIGNING_REASON);
				final String location = (String) properties
						.get(IHtmlToPdfTransformer.PDF_SIGNING_LOCATION);
				final boolean selfSigned = !"false".equals(properties
						.get(IHtmlToPdfTransformer.USE_PDF_SELF_SIGNING));
				PdfReader reader = null;

				if (password != null) {
					reader = new PdfReader(tmp2.getAbsolutePath(), password
							.getBytes());
				} // end if
				else {
					reader = new PdfReader(tmp2.getAbsolutePath());
				} // end else

				final KeyStore ks = selfSigned ? KeyStore.getInstance(KeyStore
						.getDefaultType()) : KeyStore.getInstance("pkcs12");
				ks.load(new FileInputStream(privateKeyFile), keyStorepassword
						.toCharArray());

				final String alias = (String) ks.aliases().nextElement();
				final PrivateKey key = (PrivateKey) ks.getKey(alias,
						keypassword.toCharArray());
				final Certificate chain[] = ks.getCertificateChain(alias);
				final PdfStamper stp = PdfStamper.createSignature(reader,
						oldOut, '\0');

				if ("true".equals(properties
						.get(IHtmlToPdfTransformer.USE_PDF_ENCRYPTION))) {
					stp.setEncryption(PdfWriter.STANDARD_ENCRYPTION_128, password,
							null, CDocumentReconstructor
									.getSecurityFlags(properties));
				} // end if

				final PdfSignatureAppearance sap = stp.getSignatureAppearance();

				if (selfSigned) {
					sap.setCrypto(key, chain, null,
							PdfSignatureAppearance.SELF_SIGNED);
				} // end if
				else {
					sap.setCrypto(key, chain, null,
							PdfSignatureAppearance.WINCER_SIGNED);
				} // end else

				if (reason != null) {
					sap.setReason(reason);
				} // end if

				if (location != null) {
					sap.setLocation(location);
				} // end if

				stp.close();
				oldOut.flush();
			} // end if
		} // end try
		catch (final Exception e) {
			throw new CConvertException(
					"ERROR: An Exception occured while reconstructing the pdf document: "
							+ e.getMessage(), e);
		} // end catch
		finally {
			try {
				tmp.delete();
			} // end try
			catch (final Exception ignore) {
			}
			try {
				tmp2.delete();
			} // end try
			catch (final Exception ignore) {
			}
		} // end finally
	} // end reconstruct()
} // end CDocumentReconstructor
