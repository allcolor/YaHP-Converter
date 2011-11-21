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

package org.allcolor.yahp.cl.converter.utils;

import org.allcolor.xml.parser.CDocumentBuilderFactory;
import org.allcolor.xml.parser.CShaniDomParser;
import org.allcolor.xml.parser.dom.CEntityCoDec;

import org.allcolor.yahp.converter.CMutex;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Entity;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.InputSource;

import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import java.net.URL;
import java.net.URLConnection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;


/**
 * A text validation class.
 *
 * @author Quentin Anciaux
 * @version 0.94
 */
public final class CPadawan {
    /** handle to the singleton */
    private static CPadawan handle = null;

    /** html entity list */
    private static final String entity[][] =
        new String[][] {
            {
                "&#0034;",
                "quot"
            },
            {
                "&#0038;",
                "amp"
            },
            {
                "&#0039;",
                "apos"
            },
            {
                "&#0060;",
                "lt"
            },
            {
                "&#0062;",
                "gt"
            },
            {
                "&#0338;",
                "OElig"
            },
            {
                "&#0339;",
                "oelig"
            },
            {
                "&#0352;",
                "Scaron"
            },
            {
                "&#0353;",
                "scaron"
            },
            {
                "&#0376;",
                "Yuml"
            },
            {
                "&#0710;",
                "circ"
            },
            {
                "&#0732;",
                "tilde"
            },
            {
                "&#8194;",
                "ensp"
            },
            {
                "&#8195;",
                "emsp"
            },
            {
                "&#8201;",
                "thinsp"
            },
            {
                "&#8204;",
                "zwnj"
            },
            {
                "&#8205;",
                "zwj"
            },
            {
                "&#8206;",
                "lrm"
            },
            {
                "&#8207;",
                "rlm"
            },
            {
                "&#8211;",
                "ndash"
            },
            {
                "&#8212;",
                "mdash"
            },
            {
                "&#8216;",
                "lsquo"
            },
            {
                "&#8217;",
                "rsquo"
            },
            {
                "&#8218;",
                "sbquo"
            },
            {
                "&#8220;",
                "ldquo"
            },
            {
                "&#8221;",
                "rdquo"
            },
            {
                "&#8222;",
                "bdquo"
            },
            {
                "&#8224;",
                "dagger"
            },
            {
                "&#8225;",
                "Dagger"
            },
            {
                "&#8240;",
                "permil"
            },
            {
                "&#8249;",
                "lsaquo"
            },
            {
                "&#8250;",
                "rsaquo"
            },
            {
                "&#8364;",
                "euro"
            },
            {
                "&#0402;",
                "fnof"
            },
            {
                "&#0913;",
                "Alpha"
            },
            {
                "&#0914;",
                "Beta"
            },
            {
                "&#0915;",
                "Gamma"
            },
            {
                "&#0916;",
                "Delta"
            },
            {
                "&#0917;",
                "Epsilon"
            },
            {
                "&#0918;",
                "Zeta"
            },
            {
                "&#0919;",
                "Eta"
            },
            {
                "&#0920;",
                "Theta"
            },
            {
                "&#0921;",
                "Iota"
            },
            {
                "&#0922;",
                "Kappa"
            },
            {
                "&#0923;",
                "Lambda"
            },
            {
                "&#0924;",
                "Mu"
            },
            {
                "&#0925;",
                "Nu"
            },
            {
                "&#0926;",
                "Xi"
            },
            {
                "&#0927;",
                "Omicron"
            },
            {
                "&#0928;",
                "Pi"
            },
            {
                "&#0929;",
                "Rho"
            },
            {
                "&#0931;",
                "Sigma"
            },
            {
                "&#0932;",
                "Tau"
            },
            {
                "&#0933;",
                "Upsilon"
            },
            {
                "&#0934;",
                "Phi"
            },
            {
                "&#0935;",
                "Chi"
            },
            {
                "&#0936;",
                "Psi"
            },
            {
                "&#0937;",
                "Omega"
            },
            {
                "&#0945;",
                "alpha"
            },
            {
                "&#0946;",
                "beta"
            },
            {
                "&#0947;",
                "gamma"
            },
            {
                "&#0948;",
                "delta"
            },
            {
                "&#0949;",
                "epsilon"
            },
            {
                "&#0950;",
                "zeta"
            },
            {
                "&#0951;",
                "eta"
            },
            {
                "&#0952;",
                "theta"
            },
            {
                "&#0953;",
                "iota"
            },
            {
                "&#0954;",
                "kappa"
            },
            {
                "&#0955;",
                "lambda"
            },
            {
                "&#0956;",
                "mu"
            },
            {
                "&#0957;",
                "nu"
            },
            {
                "&#0958;",
                "xi"
            },
            {
                "&#0959;",
                "omicron"
            },
            {
                "&#0960;",
                "pi"
            },
            {
                "&#0961;",
                "rho"
            },
            {
                "&#0962;",
                "sigmaf"
            },
            {
                "&#0963;",
                "sigma"
            },
            {
                "&#0964;",
                "tau"
            },
            {
                "&#0965;",
                "upsilon"
            },
            {
                "&#0966;",
                "phi"
            },
            {
                "&#0967;",
                "chi"
            },
            {
                "&#0968;",
                "psi"
            },
            {
                "&#0969;",
                "omega"
            },
            {
                "&#0977;",
                "thetasym"
            },
            {
                "&#0978;",
                "upsih"
            },
            {
                "&#0982;",
                "piv"
            },
            {
                "&#8226;",
                "bull"
            },
            {
                "&#8230;",
                "hellip"
            },
            {
                "&#8242;",
                "prime"
            },
            {
                "&#8243;",
                "Prime"
            },
            {
                "&#8254;",
                "oline"
            },
            {
                "&#8260;",
                "frasl"
            },
            {
                "&#8472;",
                "weierp"
            },
            {
                "&#8465;",
                "image"
            },
            {
                "&#8476;",
                "real"
            },
            {
                "&#8482;",
                "trade"
            },
            {
                "&#8501;",
                "alefsym"
            },
            {
                "&#8592;",
                "larr"
            },
            {
                "&#8593;",
                "uarr"
            },
            {
                "&#8594;",
                "rarr"
            },
            {
                "&#8595;",
                "darr"
            },
            {
                "&#8596;",
                "harr"
            },
            {
                "&#8629;",
                "crarr"
            },
            {
                "&#8656;",
                "lArr"
            },
            {
                "&#8657;",
                "uArr"
            },
            {
                "&#8658;",
                "rArr"
            },
            {
                "&#8659;",
                "dArr"
            },
            {
                "&#8660;",
                "hArr"
            },
            {
                "&#8704;",
                "forall"
            },
            {
                "&#8706;",
                "part"
            },
            {
                "&#8707;",
                "exist"
            },
            {
                "&#8709;",
                "empty"
            },
            {
                "&#8711;",
                "nabla"
            },
            {
                "&#8712;",
                "isin"
            },
            {
                "&#8713;",
                "notin"
            },
            {
                "&#8715;",
                "ni"
            },
            {
                "&#8719;",
                "prod"
            },
            {
                "&#8721;",
                "sum"
            },
            {
                "&#8722;",
                "minus"
            },
            {
                "&#8727;",
                "lowast"
            },
            {
                "&#8730;",
                "radic"
            },
            {
                "&#8733;",
                "prop"
            },
            {
                "&#8734;",
                "infin"
            },
            {
                "&#8736;",
                "ang"
            },
            {
                "&#8743;",
                "and"
            },
            {
                "&#8744;",
                "or"
            },
            {
                "&#8745;",
                "cap"
            },
            {
                "&#8746;",
                "cup"
            },
            {
                "&#8747;",
                "int"
            },
            {
                "&#8756;",
                "there4"
            },
            {
                "&#8764;",
                "sim"
            },
            {
                "&#8773;",
                "cong"
            },
            {
                "&#8776;",
                "asymp"
            },
            {
                "&#8800;",
                "ne"
            },
            {
                "&#8801;",
                "equiv"
            },
            {
                "&#8804;",
                "le"
            },
            {
                "&#8805;",
                "ge"
            },
            {
                "&#8834;",
                "sub"
            },
            {
                "&#8835;",
                "sup"
            },
            {
                "&#8836;",
                "nsub"
            },
            {
                "&#8838;",
                "sube"
            },
            {
                "&#8839;",
                "supe"
            },
            {
                "&#8853;",
                "oplus"
            },
            {
                "&#8855;",
                "otimes"
            },
            {
                "&#8869;",
                "perp"
            },
            {
                "&#8901;",
                "sdot"
            },
            {
                "&#8968;",
                "lceil"
            },
            {
                "&#8969;",
                "rceil"
            },
            {
                "&#8970;",
                "lfloor"
            },
            {
                "&#8971;",
                "rfloor"
            },
            {
                "&#9001;",
                "lang"
            },
            {
                "&#9002;",
                "rang"
            },
            {
                "&#9674;",
                "loz"
            },
            {
                "&#9824;",
                "spades"
            },
            {
                "&#9827;",
                "clubs"
            },
            {
                "&#9829;",
                "hearts"
            },
            {
                "&#9830;",
                "diams"
            },
            {
                "&#0160;",
                "nbsp"
            },
            {
                "&#0161;",
                "iexcl"
            },
            {
                "&#0162;",
                "cent"
            },
            {
                "&#0163;",
                "pound"
            },
            {
                "&#0164;",
                "curren"
            },
            {
                "&#0165;",
                "yen"
            },
            {
                "&#0166;",
                "brvbar"
            },
            {
                "&#0167;",
                "sect"
            },
            {
                "&#0168;",
                "uml"
            },
            {
                "&#0169;",
                "copy"
            },
            {
                "&#0170;",
                "ordf"
            },
            {
                "&#0171;",
                "laquo"
            },
            {
                "&#0172;",
                "not"
            },
            {
                "&#0173;",
                "shy"
            },
            {
                "&#0174;",
                "reg"
            },
            {
                "&#0175;",
                "macr"
            },
            {
                "&#0176;",
                "deg"
            },
            {
                "&#0177;",
                "plusmn"
            },
            {
                "&#0178;",
                "sup2"
            },
            {
                "&#0179;",
                "sup3"
            },
            {
                "&#0180;",
                "acute"
            },
            {
                "&#0181;",
                "micro"
            },
            {
                "&#0182;",
                "para"
            },
            {
                "&#0183;",
                "middot"
            },
            {
                "&#0184;",
                "cedil"
            },
            {
                "&#0185;",
                "sup1"
            },
            {
                "&#0186;",
                "ordm"
            },
            {
                "&#0187;",
                "raquo"
            },
            {
                "&#0188;",
                "frac14"
            },
            {
                "&#0189;",
                "frac12"
            },
            {
                "&#0190;",
                "frac34"
            },
            {
                "&#0191;",
                "iquest"
            },
            {
                "&#0192;",
                "Agrave"
            },
            {
                "&#0193;",
                "Aacute"
            },
            {
                "&#0194;",
                "Acirc"
            },
            {
                "&#0195;",
                "Atilde"
            },
            {
                "&#0196;",
                "Auml"
            },
            {
                "&#0197;",
                "Aring"
            },
            {
                "&#0198;",
                "AElig"
            },
            {
                "&#0199;",
                "Ccedil"
            },
            {
                "&#0200;",
                "Egrave"
            },
            {
                "&#0201;",
                "Eacute"
            },
            {
                "&#0202;",
                "Ecirc"
            },
            {
                "&#0203;",
                "Euml"
            },
            {
                "&#0204;",
                "Igrave"
            },
            {
                "&#0205;",
                "Iacute"
            },
            {
                "&#0206;",
                "Icirc"
            },
            {
                "&#0207;",
                "Iuml"
            },
            {
                "&#0208;",
                "ETH"
            },
            {
                "&#0209;",
                "Ntilde"
            },
            {
                "&#0210;",
                "Ograve"
            },
            {
                "&#0211;",
                "Oacute"
            },
            {
                "&#0212;",
                "Ocirc"
            },
            {
                "&#0213;",
                "Otilde"
            },
            {
                "&#0214;",
                "Ouml"
            },
            {
                "&#0215;",
                "times"
            },
            {
                "&#0216;",
                "Oslash"
            },
            {
                "&#0217;",
                "Ugrave"
            },
            {
                "&#0218;",
                "Uacute"
            },
            {
                "&#0219;",
                "Ucirc"
            },
            {
                "&#0220;",
                "Uuml"
            },
            {
                "&#0221;",
                "Yacute"
            },
            {
                "&#0222;",
                "THORN"
            },
            {
                "&#0223;",
                "szlig"
            },
            {
                "&#0224;",
                "agrave"
            },
            {
                "&#0225;",
                "aacute"
            },
            {
                "&#0226;",
                "acirc"
            },
            {
                "&#0227;",
                "atilde"
            },
            {
                "&#0228;",
                "auml"
            },
            {
                "&#0229;",
                "aring"
            },
            {
                "&#0230;",
                "aelig"
            },
            {
                "&#0231;",
                "ccedil"
            },
            {
                "&#0232;",
                "egrave"
            },
            {
                "&#0233;",
                "eacute"
            },
            {
                "&#0234;",
                "ecirc"
            },
            {
                "&#0235;",
                "euml"
            },
            {
                "&#0236;",
                "igrave"
            },
            {
                "&#0237;",
                "iacute"
            },
            {
                "&#0238;",
                "icirc"
            },
            {
                "&#0239;",
                "iuml"
            },
            {
                "&#0240;",
                "eth"
            },
            {
                "&#0241;",
                "ntilde"
            },
            {
                "&#0242;",
                "ograve"
            },
            {
                "&#0243;",
                "oacute"
            },
            {
                "&#0244;",
                "ocirc"
            },
            {
                "&#0245;",
                "otilde"
            },
            {
                "&#0246;",
                "ouml"
            },
            {
                "&#0247;",
                "divide"
            },
            {
                "&#0248;",
                "oslash"
            },
            {
                "&#0249;",
                "ugrave"
            },
            {
                "&#0250;",
                "uacute"
            },
            {
                "&#0251;",
                "ucirc"
            },
            {
                "&#0252;",
                "uuml"
            },
            {
                "&#0253;",
                "yacute"
            },
            {
                "&#0254;",
                "thorn"
            },
            {
                "&#0255;",
                "yuml"
            }
        };

    /** use for thread safety access */
    private static final CMutex mutex = new CMutex();

    /** last modification date of the config file */
    private long lastModified = -1;

    /** contains known filters */
    private Map mapFilter = new HashMap();

    /** handle to entity declaration */
    private String entityDecl = null;

    /**
     * Replace the input message using the rules 'type' found in the xml
     * configuration file
     *
     * @param message message to replace
     * @param type rule type
     *
     * @return the replaced message
     */
    public final String replaceExpr(
        String message,
        String type
    ) {
        message = decode(message);

        List mapFilter = getFilter(type);

        if (mapFilter == null)
            return message;

        Iterator it = mapFilter.iterator();

        while (it.hasNext()) {
            Object value[] = (Object[]) it.next();
            String replacement = (String) value[0];
            List vFilter = (List) value[1];

            if ((vFilter.size() == 0) && (replacement.equals("[VALIDATE]"))) {
                message = validate(
                        message,
                        type
                    );

                continue;
            }

            for (int j = 0; j < vFilter.size(); j++) {
                Pattern filter = (Pattern) vFilter.get(j);
                Matcher match = filter.matcher(message);

                while (match.find()) {
                    message = match.replaceAll(replacement);
                    match = filter.matcher(message);
                }
            }
        }

        return message;
    }

    /**
     * Initialize the class
     */
    private final void init() {
        InputStream in = null;

        try {
            URL url =
                this.getClass()
                    .getClassLoader()
                    .getResource("padawan.conf.xml");

            if (url != null) {
                URLConnection conn = url.openConnection();
                in = conn.getInputStream();

                long newModified = conn.getLastModified();

                if ((lastModified != newModified) && (newModified != 0)) {
                    lastModified = newModified;
                    Document xmlDoc =
                        CDocumentBuilderFactory.newParser()
                                               .newDocumentBuilder()
                                               .parse(in);
                    parseDocument(xmlDoc);
                }
            }
        } catch (Exception ignore) {
            ignore.printStackTrace();
        } finally {
            try {
                in.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * Return an instance of the padawan
     *
     * @return may the force be with you !
     */
    public static final CPadawan getInstance() {
        try {
            mutex.acquire();

            CPadawan toReturn =
                (handle == null)
                ? (handle = new CPadawan())
                : handle;
            toReturn.init();

            return toReturn;
        } finally {
            try {
                mutex.release();
            } catch (Exception ignore) {
            }
        }
    }

    /**
     * return the filters of type 'type'
     *
     * @param type type of filters to get
     *
     * @return the filters of type 'type'
     */
    private final List getFilter(String type) {
	    	try {
	    		mutex.acquire();
	        return (List) mapFilter.get(type + ".filter");
	    	}
	    	finally {
	    		try{mutex.release();}catch(Throwable ignore){}
	    	}
    }

    /**
     * return the tag list of type 'type'
     *
     * @param type type of tag list to get
     *
     * @return the tag list of type 'type'
     */
    private final List getTags(String type) {
	    	try {
	    		mutex.acquire();
	        return (List) mapFilter.get(type + ".tag");
	    	}
	    	finally {
	    		try{mutex.release();}catch(Throwable ignore){}
	    	}
    }

    /**
     * return the merge list of type 'type'
     *
     * @param type type of merge list to get
     *
     * @return the merge list of type 'type'!
     */
    private final List getMerge(String type) {
	    	try {
	    		mutex.acquire();
	        return (List) mapFilter.get(type + ".merge");
	    	}
	    	finally {
	    		try{mutex.release();}catch(Throwable ignore){}
	    	}
    }

    /**
     * parse the configuration file
     *
     * @param currentNode current analysed node
     */
    private final void parseDocument(Node currentNode) {
	    	try {
	    		mutex.acquire();
	        String nodeName = currentNode.getNodeName();
	
	        if ((currentNode.getNodeType() == Node.ELEMENT_NODE)
	                && (nodeName.equals("type"))
	        ) {
	            String typeName = ((Element) currentNode).getAttribute("name");
	            mapFilter.put(
	                typeName + ".filter",
	                new ArrayList()
	            );
	            mapFilter.put(
	                typeName + ".tag",
	                new ArrayList()
	            );
	            mapFilter.put(
	                typeName + ".merge",
	                new ArrayList()
	            );
	        }
	
	        if ((currentNode.getNodeType() == Node.ELEMENT_NODE)
	                && (currentNode.getParentNode() != null)
	        ) {
	            if (nodeName.equals("filter")) {
	                String typeName =
	                    ((Element) currentNode.getParentNode()
	                                          .getParentNode()).getAttribute(
	                        "name"
	                    );
	                ArrayList vMapFilter =
	                    (ArrayList) mapFilter.get(typeName + ".filter");
	                ArrayList vFilter = new ArrayList();
	                int iOrder =
	                    ((Element) currentNode).getAttribute("order")
	                     .equals("")
	                    ? vMapFilter.size()
	                    : Integer.parseInt(
	                        ((Element) currentNode).getAttribute("order")
	                    );
	                String replacement = "";
	                NodeList nl = currentNode.getChildNodes();
	
	                for (int i = 0; i < nl.getLength(); i++) {
	                    Node childNode = nl.item(i);
	
	                    if (childNode.getNodeName()
	                                     .equals("replacement")) {
	                        NodeList nl2 = childNode.getChildNodes();
	
	                        for (int j = 0; j < nl2.getLength(); j++) {
	                            Node child = nl2.item(j);
	
	                            if (child.getNodeType() == Node.TEXT_NODE) {
	                                replacement =
	                                    replacement + child.getNodeValue();
	                            }
	                        }
	                    } else if (childNode.getNodeName()
	                                            .equals("match")) {
	                        String match = "";
	                        NodeList nl2 = childNode.getChildNodes();
	
	                        for (int j = 0; j < nl2.getLength(); j++) {
	                            Node child = nl2.item(j);
	
	                            if (child.getNodeType() == Node.TEXT_NODE) {
	                                match = match + child.getNodeValue();
	                            }
	                        }
	
	                        Pattern p = null;
	
	                        try {
	                            p = Pattern.compile(
	                                    match,
	                                    Pattern.MULTILINE | Pattern.DOTALL
	                                );
	                            vFilter.add(p);
	                        } catch (Exception ignore) {
	                        }
	                    }
	                }
	
	                vMapFilter.add(
	                    iOrder,
	                    new Object[] {
	                        replacement,
	                        vFilter
	                    }
	                );
	            } else if ((nodeName.equals("tag"))
	                    && (
	                        currentNode.getParentNode().getNodeName().equals(
	                            "tags"
	                        )
	                    )
	            ) {
	                String typeName =
	                    ((Element) currentNode.getParentNode()
	                                          .getParentNode()
	                                          .getParentNode()).getAttribute(
	                        "name"
	                    );
	                String tagName = ((Element) currentNode).getAttribute("name");
	                ArrayList vMapTags =
	                    (ArrayList) mapFilter.get(typeName + ".tag");
	                ArrayList vAttributes = new ArrayList();
	                NodeList nl = currentNode.getChildNodes();
	
	                for (int i = 0; i < nl.getLength(); i++) {
	                    Node childNode = nl.item(i);
	
	                    if (childNode.getNodeName()
	                                     .equals("attribute")) {
	                        vAttributes.add(
	                            (String) ((Element) childNode).getAttribute("name")
	                        );
	                    }
	                }
	
	                vMapTags.add(new Object[] {
	                        tagName,
	                        vAttributes
	                    }
	                );
	            } else if (nodeName.equals("source_attribute")) {
	                String typeName =
	                    ((Element) currentNode.getParentNode()
	                                          .getParentNode()
	                                          .getParentNode()
	                                          .getParentNode()).getAttribute(
	                        "name"
	                    );
	                String tagName =
	                    ((Element) currentNode.getParentNode()).getAttribute(
	                        "name"
	                    );
	                String attribName =
	                    ((Element) currentNode).getAttribute("name");
	                String srcAttribNameInDest =
	                    ((Element) currentNode).getAttribute("name_in_destination");
	                ArrayList vMapMerge =
	                    (ArrayList) mapFilter.get(typeName + ".merge");
	                String separator = null;
	                String destAttribName = null;
	                ArrayList match = new ArrayList();
	                ArrayList value = new ArrayList();
	                NodeList nl = currentNode.getChildNodes();
	
	                for (int i = 0; i < nl.getLength(); i++) {
	                    Node childNode = nl.item(i);
	
	                    if (childNode.getNodeName()
	                                     .equals("destination_attribute")) {
	                        destAttribName =
	                            ((Element) childNode).getAttribute("name");
	                    } else if (childNode.getNodeName()
	                                            .equals("separator")) {
	                        separator = ((Element) childNode).getAttribute("value");
	                    } else if (childNode.getNodeName()
	                                            .equals("replace")) {
	                        match.add(((Element) childNode).getAttribute("match"));
	                        value.add(((Element) childNode).getAttribute("value"));
	                    }
	                }
	
	                vMapMerge.add(
	                    new Object[] {
	                        tagName,
	                        attribName,
	                        srcAttribNameInDest,
	                        destAttribName,
	                        separator,
	                        match,
	                        value
	                    }
	                );
	            }
	        }
	
	        NodeList nl = currentNode.getChildNodes();
	
	        for (int i = 0; i < nl.getLength(); i++) {
	            parseDocument(nl.item(i));
	        }
	    	}
	    	finally {
	    		try{mutex.release();}catch(Throwable ignore){}
	    	}
    }

    /**
     * validate for xml the string in using the rules type 'type'
     *
     * @param in the string to validate
     * @param type rule type
     *
     * @return the validated string
     */
    public final String validate(
        String in,
        String type
    ) {
    	    CShaniDomParser parser = new CShaniDomParser();
        Document doc =
            parser.parse(
                new StringReader(in),
                getTags(type),
                getMerge(type)
            );

        if (doc != null)
            return doc.toString();
        else
            return in;
    }

    /**
     * Create the padawan doctype
     *
     * @return the padawan doctype
     *
     * @throws UnsupportedEncodingException should not happen
     */
    public final String createEntityDeclaration()
        throws UnsupportedEncodingException {
        StringBuffer buffer = new StringBuffer();
        buffer.append("<!DOCTYPE padawan [\n");

        for (int i = 0; i < entity.length; i++) {
            buffer.append(
                "<!ENTITY " + entity[i][1] + " '" + entity[i][0] + "' >\n"
            );
        }

        buffer.append("]>\n");

        return buffer.toString();
    }

    /**
     * return the padawan doctype
     *
     * @return the padawan doctype
     */
    public final String getEntityDeclaration() {
        if (entityDecl == null)
            try {
                entityDecl = createEntityDeclaration();
            } catch (Exception e) {
                entityDecl = null;
            }

        return entityDecl;
    }

    /**
     * decode entity in the string
     *
     * @param toDecode the string to decode
     *
     * @return the decoded string
     */
    public final String decode(String toDecode) {
        CEntityCoDec codec = new CEntityCoDec(new HashMap());

        return codec.decode(toDecode);
    }

    /**
     * encode entity in the string
     *
     * @param toEncode the string to encode
     *
     * @return the encoded string
     */
    public final String encode(String toEncode) {
        return CEntityCoDec.encode(toEncode);
    }

    /**
     * A replace string method
     *
     * @param toBeReplaced string to replace
     * @param toReplace regex to match
     * @param replacement string replacement for each match
     *
     * @return the replaced string
     */
    public final String stringReplace(
        String toBeReplaced,
        String toReplace,
        String replacement
    ) {
        Pattern pattern = Pattern.compile(toReplace);
        Matcher match = pattern.matcher(toBeReplaced);

        while (match.find()) {
            toBeReplaced = match.replaceAll(replacement);
            match = pattern.matcher(toBeReplaced);
        }

        return toBeReplaced;
    }

    /**
     * rewrite the given xmlfile
     *
     * @param xmlFile xml file as a string
     * @param aloneTags a string array of empty tags
     *
     * @return the rewrited xml.
     */
    public final String rewriteXml(
        String xmlFile,
        String aloneTags[]
    ) {
        return rewriteXml(
            xmlFile,
            aloneTags,
            false
        );
    }

    /**
     * rewrite the given xmlfile
     *
     * @param xmlFile xml file as a string
     * @param aloneTags a string array of empty tags
     * @param outputXmlHeader true to output xml header
     *
     * @return the rewrited xml.
     */
    public final String rewriteXml(
        String xmlFile,
        String aloneTags[],
        boolean outputXmlHeader
    ) {
        try {
            DocumentBuilderFactory fact = CDocumentBuilderFactory.newParser();
            Document xmlDoc =
                fact.newDocumentBuilder()
                    .parse(new InputSource(new StringReader(xmlFile)));

            return rewriteXml(
                xmlDoc,
                aloneTags,
                outputXmlHeader
            );
        } catch (Exception e) {
            e.printStackTrace();

            return xmlFile;
        }
    }

    /**
     * rewrite the given xmlfile
     *
     * @param out the writer to write the xml
     * @param xmlFile xml file as a string
     * @param aloneTags a string array of empty tags
     */
    public final void rewriteXml(
        Writer out,
        String xmlFile,
        String aloneTags[]
    ) {
        rewriteXml(
            out,
            xmlFile,
            aloneTags,
            false
        );
    }

    /**
     * rewrite the given xmlfile
     *
     * @param out the writer to write the xml
     * @param xmlFile xml file as a string
     * @param aloneTags a string array of empty tags
     * @param outputXmlHeader true to output xml header
     */
    public final void rewriteXml(
        Writer out,
        String xmlFile,
        String aloneTags[],
        boolean outputXmlHeader
    ) {
        try {
            DocumentBuilderFactory fact = CDocumentBuilderFactory.newParser();
            Document xmlDoc =
                fact.newDocumentBuilder()
                    .parse(new InputSource(new StringReader(xmlFile)));
            rewriteXml(
                out,
                xmlDoc,
                aloneTags,
                outputXmlHeader
            );
        } catch (Exception e) {
            e.printStackTrace();

            return;
        }
    }

    /**
     * rewrite the given xmlfile
     *
     * @param xmlDoc xml file as a dom node
     * @param aloneTags a string array of empty tags
     *
     * @return the rewrited xml.
     */
    public final String rewriteXml(
        Node xmlDoc,
        String aloneTags[]
    ) {
        return rewriteXml(
            xmlDoc,
            aloneTags,
            false
        );
    }

    /**
     * rewrite the given xmlfile
     *
     * @param xmlDoc xml file as a dom node
     * @param aloneTags a string array of empty tags
     * @param outputXmlHeader true to output xml header
     *
     * @return the rewrited xml.
     */
    public final String rewriteXml(
        Node xmlDoc,
        String aloneTags[],
        boolean outputXmlHeader
    ) {
        try {
            StringWriter sOut = new StringWriter();
            rewriteXml(
                sOut,
                xmlDoc,
                aloneTags,
                outputXmlHeader
            );

            return sOut.toString();
        } catch (Exception e) {
            e.printStackTrace();

            return null;
        }
    }

    /**
     * rewrite the given xmlfile
     *
     * @param out the writer to write the xml
     * @param xmlDoc xml file as a dom node
     * @param aloneTags a string array of empty tags
     */
    public final void rewriteXml(
        Writer out,
        Node xmlDoc,
        String aloneTags[]
    ) {
        rewriteXml(
            out,
            xmlDoc,
            aloneTags,
            false
        );
    }

    /**
     * rewrite the given xmlfile
     *
     * @param out the writer to write the xml
     * @param xmlDoc xml file as a dom node
     * @param aloneTags a string array of empty tags
     * @param outputXmlHeader true to output xml header
     */
    public final void rewriteXml(
        Writer out,
        Node xmlDoc,
        String aloneTags[],
        boolean outputXmlHeader
    ) {
        try {
            if (outputXmlHeader)
                out.write("<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n");

            renderXMLRecurs(
                new PrintWriter(out),
                xmlDoc,
                aloneTags
            );
        } catch (Exception e) {
            e.printStackTrace();

            return;
        }
    }

    /**
     * rewrite the given xmlfile
     *
     * @param out the writer to write the xml
     * @param node current writed node
     * @param aloneTags a string array of empty tags
     */
    private final void renderXMLRecurs(
        PrintWriter out,
        Node node,
        String aloneTags[]
    ) {
        if (node.getNodeType() == Node.DOCUMENT_TYPE_NODE) {
            DocumentType dc = (DocumentType) node;

            if ((dc.getPublicId() != null) && (dc.getSystemId() != null)) {
                out.println(
                    "<!DOCTYPE " + dc.getName() + " PUBLIC \""
                    + dc.getPublicId() + "\" \"" + dc.getSystemId() + "\">"
                );
            } else {
                String entityDecl = "";
                NamedNodeMap nnm = dc.getEntities();

                for (int i = (nnm.getLength() - 1); i >= 0; i--) {
                    if (i == (nnm.getLength() - 1))
                        out.print(" ");

                    Entity ent = (Entity) nnm.item(i);

                    if (ent.getNotationName() != null) {
                        entityDecl =
                            "<!ENTITY " + ent.getNodeName() + " '"
                            + ent.getNotationName() + "'>\n";
                    }
                }

                if (entityDecl.length() > 0)
                    out.println(
                        "<!DOCTYPE " + dc.getName() + " [\n" + entityDecl
                        + "]>"
                    );
            }
        } else if (node.getNodeType() == Node.ELEMENT_NODE) {
            out.print("<");
            out.print(node.getNodeName());

            NamedNodeMap nnm = ((Element) node).getAttributes();

            for (int i = (nnm.getLength() - 1); i >= 0; i--) {
                if (i == (nnm.getLength() - 1))
                    out.print(" ");

                Attr attr = (Attr) nnm.item(i);
                out.print(attr.getName());
                out.print("=\"");
                out.print(escapeAttribute(attr.getValue()));

                if (i > 0)
                    out.print("\" ");
                else
                    out.print("\"");
            }

            if (isAlone(
                        node.getNodeName(),
                        aloneTags
                    )) {
                out.print("/>");

                return;
            }

            out.print(">");
        } else if (node.getNodeType() == Node.TEXT_NODE) {
            try {
                out.print(escape(node.getNodeValue()));
            } catch (Exception ignore) {
            }
        }

        boolean booCDATAopen = false;
        boolean booCOMMENTopen = false;
        NodeList nl = node.getChildNodes();

        for (int i = 0; i < nl.getLength(); i++) {
            Node child = nl.item(i);

            if (child.getNodeType() == Node.CDATA_SECTION_NODE) {
                if (!booCDATAopen) {
                    booCDATAopen = true;
                    out.print("<![CDATA[");
                }

                out.print(
                    (child.getNodeValue() == null)
                    ? ""
                    : child.getNodeValue().trim()
                );

                continue;
            }

            if (child.getNodeType() == Node.COMMENT_NODE) {
                if (!booCOMMENTopen) {
                    booCOMMENTopen = true;

                    if (node.getNodeName()
                                .equals("script"))
                        out.print("//<!--");
                    else
                        out.print("<!--");
                }

                out.print(
                    (child.getNodeValue() == null)
                    ? ""
                    : child.getNodeValue()
                );

                continue;
            }

            if (booCDATAopen) {
                booCDATAopen = false;
                out.print("]]>");
            }

            if (booCOMMENTopen) {
                booCOMMENTopen = false;

                if (node.getNodeName()
                            .equals("script"))
                    out.print("//-->");
                else
                    out.print("-->");
            }

            renderXMLRecurs(
                out,
                child,
                aloneTags
            );
        }

        if (booCDATAopen) {
            out.print("]]>");
        }

        if (booCOMMENTopen) {
            if (node.getNodeName()
                        .equals("script"))
                out.print("//-->\n");
            else
                out.print("-->\n");
        }

        if (node.getNodeType() == Node.ELEMENT_NODE) {
            out.print("</");
            out.print(node.getNodeName());
            out.print(">");
        }
    }

    /**
     * return true if the given tag name is an empty tag
     *
     * @param tagName tag name to lookup
     * @param aloneTags a string array of empty tags
     *
     * @return true if the given tag name is an empty tag
     */
    private final boolean isAlone(
        String tagName,
        String aloneTags[]
    ) {
        if (aloneTags == null)
            return false;

        if (tagName == null)
            return false;

        for (int i = 0; i < aloneTags.length; i++) {
            if (tagName.equalsIgnoreCase(aloneTags[i]))
                return true;
        }

        return false;
    }

    /**
     * Escape the given string to be a valid content for an xml attribute
     *
     * @param in the string to escape
     *
     * @return the escaped string
     */
    public final String escapeAttribute(String in) {
        in = escape(in);
        in = in.replaceAll(
                "\"",
                "&quot;"
            );

        return in;
    }

    /**
     * Escape the given string to be a valid content for an xml text node
     * content
     *
     * @param in the string to escape
     *
     * @return the escaped string
     */
    public final String escape(String in) {
        if (in == null)
            return "";

        if (in.trim()
                  .equals(""))
            return in;

        if (in.trim()
                  .length() == 0)
            return in;

        in = in.replaceAll(
                "&",
                "&amp;"
            );
        in = in.replaceAll(
                "<",
                "&lt;"
            );
        in = in.replaceAll(
                ">",
                "&gt;"
            );

        StringBuffer result = new StringBuffer();
        char chars[] = in.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            if ((chars[i] >= 127) && (chars[i] < 160)) {
                chars[i] = 32;
                result.append(chars[i]);
            } else if (chars[i] == 160) {
                result.append("&#160;");
            } else if (chars[i] == 9) {
                result.append(" ");
            } else if ((chars[i] < 32) && (chars[i] != 9) && (chars[i] != 10)
                    && (chars[i] != 13)
            ) {
                chars[i] = 32;
                result.append(chars[i]);
            } else {
                result.append(chars[i]);
            }
        }

        in = result.toString();

        return in;
    }
}