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
import org.allcolor.xml.parser.dom.ANode.CNamespace;
import org.allcolor.xml.parser.dom.ADocument;
import org.allcolor.xml.parser.dom.CDom2HTMLDocument;
import org.allcolor.yahp.converter.IHtmlToPdfTransformer;
import org.allcolor.yahp.converter.IHtmlToPdfTransformer.PageSize;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;


/**
 * This class handle the {'http://www.allcolor.org/xmlns/yahp','pb'}
 * tag and cut document in multiples documents according to it.
 *
 * @author Quentin Anciaux
 * @version 0.94
 */
public class CDocumentCut {
	
	public static class DocumentAndSize {
		final Document doc;
		final PageSize size;
		public Document getDoc() {
			return doc;
		}
		public PageSize getSize() {
			return size;
		}
		public DocumentAndSize(Document doc, PageSize size) {
			super();
			this.doc = doc;
			this.size = size;
		}
	}
	
	private static PageSize getPageSize(String sSize,PageSize size) {
		if (sSize == null || "".equals(sSize.trim())) {
			return size;
		}
		try {
			Field f = IHtmlToPdfTransformer.class.getDeclaredField(sSize);
			if (f != null) {
				return (PageSize)f.get(null);
			}
		}
		catch (Exception ignore) {
			//
		}
		try {
			String [] array = sSize.split(",");
			if (array.length == 2) {
				return new PageSize(Double.parseDouble(array[0]),Double.parseDouble(array[1]));
			}
			if (array.length == 3) {
				return new PageSize(Double.parseDouble(array[0]),Double.parseDouble(array[1]),Double.parseDouble(array[2]));
			}
			if (array.length == 6) {
				return new PageSize(Double.parseDouble(array[0]),Double.parseDouble(array[1]),Double.parseDouble(array[2]),
						Double.parseDouble(array[3]),Double.parseDouble(array[4]),Double.parseDouble(array[5]));
			}
		}
		catch (Exception ignore) {
			//
		}
		return size;
	}
	
	/**
	 * Cut the given document
	 *
	 * @param doc the document to cut
	 *
	 * @return an array of document
	 */
	public static DocumentAndSize [] cut(final Document doc,final PageSize size) {
		NodeList nl = doc.getElementsByTagNameNS("http://www.allcolor.org/xmlns/yahp",
				"pb");
		
		if (nl.getLength() == 0) {
			// see if someone forget namespace decl ?
			nl = doc.getElementsByTagName("yahp:pb");
		}

		// if no pagebreak found, return the given document.
		if (nl.getLength() == 0) {
			return new DocumentAndSize [] { new DocumentAndSize(doc,size) };
		} // end if

		// get the start-end offset of all page breaks.
		PbDocument pbdocs[] = getPbDocs(nl);

		// create as much document as they are pages.
		DocumentAndSize array[] = new DocumentAndSize[pbdocs.length];
		for (int i = 0; i < pbdocs.length; i++) {
			PbDocument pbdoc = pbdocs[i];

			// get start and end offset
			Element pbstart = (Element) pbdoc.getPbStart();
			Element pbend = (Element) pbdoc.getPbEnd();
			
			PageSize tmpSize = getPageSize(pbstart == null ? null : pbstart.getAttribute("size"),size);

			// create a new doc and set the URI
			ADocument ndoc = new CDom2HTMLDocument();
			ndoc.setDocumentURI(doc.getDocumentURI());

			CNamespace xmlnsdef = new CNamespace((pbend != null)
					? pbend.getPrefix()
					: pbstart.getPrefix(),
					"http://www.allcolor.org/xmlns/yahp");
			ndoc.getNamespaceList().add(xmlnsdef);

			if (pbend == null) {
				// from pbstart to the end of the document.
				// create the container node
				Element parentPb = (Element) ndoc.adoptNode(pbstart.getParentNode()
																	   .cloneNode(false));

				// copy all next siblings
				Node Sibling = pbstart.getNextSibling();

				while (Sibling != null) {
					Node node = ndoc.adoptNode(Sibling.cloneNode(true));
					// add to container node
					parentPb.appendChild(node);
					Sibling = Sibling.getNextSibling();
				} // end while

				// copy parent node of the start page break
				Node parent   = pbstart.getParentNode();
				Node nextNode = null;
				Node ppr	  = null;

				while (parent != null) {
					if (parent.getNodeType() == Node.ELEMENT_NODE) {
						// copy the node
						Node node = ndoc.adoptNode(parent.cloneNode(
									false));

						if (nextNode != null) {
							// append the node to the container
							node.appendChild(nextNode);

							if (!("body".equals(ppr.getNodeName()))) {
								// append next sibling of the previous container
								Sibling = ppr.getNextSibling();

								while (Sibling != null) {
									Node n = ndoc.adoptNode(Sibling.cloneNode(
												true));
									node.appendChild(n);
									Sibling = Sibling.getNextSibling();
								} // end while
							} // end if

							nextNode = node;
						} // end if
						else {
							nextNode = parentPb;
						} // end else

						ppr = parent;
					} // end if

					parent = parent.getParentNode();
				} // end while

				ndoc.appendChild(nextNode);
			} // end if
			else {
				Element parentPb   = (Element) ndoc.adoptNode(pbend.getParentNode()
																	   .cloneNode(false));
				Node    Sibling    = pbend.getPreviousSibling();
				boolean hasbeendes = false;

				while (!hasbeendes && (Sibling != null)) {
					if (isDescendant(pbstart, Sibling)) {
						hasbeendes = true;

						break;
					} // end if

					Node node = ndoc.adoptNode(Sibling.cloneNode(true));

					if (parentPb.getChildNodes().getLength() == 0) {
						parentPb.appendChild(node);
					} // end if
					else {
						parentPb.insertBefore(node,
							parentPb.getFirstChild());
					} // end else

					Sibling = Sibling.getPreviousSibling();
				} // end while
				
				if (hasbeendes && pbstart != Sibling) {
					Node c  = ndoc.adoptNode(Sibling.cloneNode(
								true));
					((Element)c).setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:yahp", "http://www.allcolor.org/xmlns/yahp");
					Node pb = ((Element) c).getElementsByTagNameNS("http://www.allcolor.org/xmlns/yahp",
							"pb").item(0);
					Node p  = pb;
					Node pp = pb.getParentNode()
									.getPreviousSibling();

					while (p != null) {
						Node tmp = p.getPreviousSibling();
						p.getParentNode()
							 .removeChild(p);
						p = tmp;
					} // end while

					while (pp != null) {
						Node tmp = pp.getPreviousSibling();

						if (tmp == null) {
							Node ppz = pp.getParentNode();

							if (ppz.getPreviousSibling() != null) {
								tmp = ppz.getPreviousSibling();
							} // end if
							else {
								Node prev = null;

								while (prev == null) {
									ppz = ppz.getParentNode();

									if (ppz == null) {
										break;
									} // end if

									prev = ppz.getPreviousSibling();
								} // end while

								if (prev != null) {
									tmp = prev;
								} // end if
							} // end else
						} // end if

						pp.getParentNode()
							  .removeChild(pp);
						pp = tmp;
					} // end while

					if (parentPb.getChildNodes()
								.getLength() == 0) {
						parentPb.appendChild(c);
					} // end if
					else {
						parentPb.insertBefore(c,
								parentPb.getFirstChild());
					} // end else
				} // end if				
				

				Node parent		  = pbend.getParentNode();
				Node previousNode = null;
				Node ppr		  = null;

				while (parent != null) {
					if (parent.getNodeType() == Node.ELEMENT_NODE) {
						if (previousNode != null) {
							Node node = ndoc.adoptNode(parent.cloneNode(
									false));
							node.appendChild(previousNode);

							if (!("body".equals(ppr.getNodeName()))) {
								Sibling = ppr.getPreviousSibling();

								while (!hasbeendes &&
										(Sibling != null)) {
									if (isDescendant(pbstart, Sibling)) {
										// here special handling need to be
										// taken.
										if (pbstart != Sibling) {
											Node c  = ndoc.adoptNode(Sibling.cloneNode(
														true));
											Node pb = ((Element) c).getElementsByTagNameNS("http://www.allcolor.org/xmlns/yahp",
													"pb").item(0);
											Node p  = pb;
											Node pp = pb.getParentNode()
															.getPreviousSibling();

											while (p != null) {
												Node tmp = p.getPreviousSibling();
												p.getParentNode()
													 .removeChild(p);
												p = tmp;
											} // end while

											while (pp != null) {
												Node tmp = pp.getPreviousSibling();

												if (tmp == null) {
													Node ppz = pp.getParentNode();

													if (ppz.getPreviousSibling() != null) {
														tmp = ppz.getPreviousSibling();
													} // end if
													else {
														Node prev = null;

														while (prev == null) {
															ppz = ppz.getParentNode();

															if (ppz == null) {
																break;
															} // end if

															prev = ppz.getPreviousSibling();
														} // end while

														if (prev != null) {
															tmp = prev;
														} // end if
													} // end else
												} // end if

												pp.getParentNode()
													  .removeChild(pp);
												pp = tmp;
											} // end while

											if (node.getChildNodes()
														.getLength() == 0) {
												node.appendChild(c);
											} // end if
											else {
												node.insertBefore(c,
													node.getFirstChild());
											} // end else
										} // end if

										hasbeendes = true;

										break;
									} // end if

									Node n = ndoc.adoptNode(Sibling.cloneNode(
												true));

									if (node.getChildNodes().getLength() == 0) {
										node.appendChild(n);
									} // end if
									else {
										node.insertBefore(n,
											node.getFirstChild());
									} // end else

									Sibling = Sibling.getPreviousSibling();
								} // end while
							} // end if

							previousNode = node;
						} // end if
						else {
							previousNode = parentPb;
						} // end else

						ppr = parent;
					} // end if

					parent = parent.getParentNode();
				} // end while

				ndoc.appendChild(previousNode);
			} // end else

			// copy header
			copyHeader(doc, ndoc);
			array[i] = new DocumentAndSize(ndoc,tmpSize);
		} // end for

		return array;
	} // end cut()

	/**
	 * return true if n is a descendant of ref
	 *
	 * @param n node to test
	 * @param ref reference node
	 *
	 * @return true if n is a descendant of ref
	 */
	private static boolean isDescendant(
		final Node n,
		final Node ref) {
		if (ref == null) {
			return false;
		} // end if

		if (n == ref) {
			return true;
		} // end if

		NodeList nl = ref.getChildNodes();

		for (int i = 0; i < nl.getLength(); i++) {
			boolean result = isDescendant(n, nl.item(i));

			if (result) {
				return result;
			} // end if
		} // end for

		return false;
	} // end isDescendant()

	/**
	 * Return the pagebreaks
	 *
	 * @param nl pagebreaks nodelist
	 *
	 * @return an array of subdoc composed of space between pb.
	 */
	private static PbDocument [] getPbDocs(final NodeList nl) {
		List    list   = new ArrayList();
		Element prevpb = null;

		for (int i = 0; i < nl.getLength(); i++) {
			Element    pb    = (Element) nl.item(i);
			PbDocument pbdoc = new PbDocument(prevpb, pb);
			list.add(pbdoc);
			prevpb = pb;

			if (i == (nl.getLength() - 1)) {
				pbdoc = new PbDocument(pb, null);
				list.add(pbdoc);
			} // end if
		} // end for

		PbDocument array[] = new PbDocument[list.size()];

		for (int i = 0; i < list.size(); i++) {
			array[i] = (PbDocument) list.get(i);
		} // end for

		return array;
	} // end getPbDocs()

	/**
	 * Copy the html header from doc to ndoc
	 *
	 * @param doc source of html header
	 * @param ndoc destination
	 */
	private static void copyHeader(
		final Document doc,
		final Document ndoc) {
		NodeList headnl = doc.getElementsByTagName("head");

		if (headnl.getLength() > 0) {
			Element head = (Element) headnl.item(0);
			ndoc.getDocumentElement()
					.insertBefore(ndoc.adoptNode(head.cloneNode(true)),
					ndoc.getDocumentElement().getFirstChild());
		} // end if
	} // end copyHeader()

	/**
	 * Represend a part of a document between two pagebreak.
	 *
	 * @author Quentin Anciaux
	 * @version 0.94
	 */
	private static class PbDocument {
		/** end pagebreak */
		Element pbEnd;

		/** start pagebreak */
		Element pbStart;

		/**
		 * Creates a new PbDocument object.
		 *
		 * @param pbStart DOCUMENT ME!
		 * @param pbEnd DOCUMENT ME!
		 */
		public PbDocument(
			final Element pbStart,
			final Element pbEnd) {
			this.pbStart     = pbStart;
			this.pbEnd		 = pbEnd;
		} // end PbDocument()

		/**
		 * return the end pagebreak
		 *
		 * @return the end pagebreak
		 */
		public Element getPbEnd() {
			return pbEnd;
		} // end getPbEnd()

		/**
		 * return the start pagebreak
		 *
		 * @return the start pagebreak
		 */
		public Element getPbStart() {
			return pbStart;
		} // end getPbStart()
	} // end PbDocument
} // end CDocumentCut
