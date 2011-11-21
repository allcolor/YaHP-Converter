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

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * This class is used to configure the class loader tree
 *
 * @author Quentin Anciaux
 * @version 0.94
 */
public final class CClassLoaderConfig {
    /** file -- loader */
    private Map filesMap = new HashMap();

    /** info -- loader */
    private Map loadersInfoMap = new HashMap();

    /**
     * add a file to the given loader
     *
     * @param loaderPath the path of the loader
     * @param file the jar file to add
     */
    public void addFile(
        String loaderPath,
        URL file
    ) {
        List list = (List) filesMap.get(loaderPath);

        if (list == null) {
            list = new ArrayList();
            filesMap.put(
                loaderPath,
                list
            );
        }

        list.add(file);
    }

    /**
     * add info about the given loader
     *
     * @param loaderPath the path of the loader
     * @param info the info to set
     */
    public void addLoaderInfo(
        String loaderPath,
        CLoaderInfo info
    ) {
        loadersInfoMap.put(
            loaderPath,
            info
        );
    }

    /**
     * return the file -- loader map
     *
     * @return the file -- loader map
     */
    public final Map getFilesMap() {
        return filesMap;
    }

    /**
     * return the info -- loader map
     *
     * @return the info -- loader map
     */
    public final Map getLoadersInfoMap() {
        return loadersInfoMap;
    }

    /**
     * represent info configuration for a loader
     *
     * @author Quentin Anciaux
     * @version 0.1
     */
    public static final class CLoaderInfo {
        /** is mandatory ? */
        private boolean mandatory;

        /** is alone ? */
        private boolean alone;

        /** is resource only ? */
        private boolean resourceOnly;

        /** is not forwarding to parent ? */
        private boolean doNotForwardToParent;
        
        /**
         * create a new info
         *
         * @param mandatory is mandatory ?
         * @param alone is alone ?
         * @param only is resource only ?
         * @param parent is not forwarding to parent ?
         */
        public CLoaderInfo(
            boolean mandatory,
            boolean alone,
            boolean only,
            boolean parent
        ) {
            super();
            this.mandatory = mandatory;
            this.alone = alone;
            resourceOnly = only;
            doNotForwardToParent = parent;
        }

        /**
         * is alone ?
         *
         * @return is alone ?
         */
        public final boolean isAlone() {
            return alone;
        }

        /**
         * is not forwarding to parent ?
         *
         * @return is not forwarding to parent ?
         */
        public final boolean isDoNotForwardToParent() {
            return doNotForwardToParent;
        }

        /**
         * is mandatory ?
         *
         * @return is mandatory ?
         */
        public final boolean isMandatory() {
            return mandatory;
        }

        /**
         * is resource only ?
         *
         * @return is resource only ?
         */
        public final boolean isResourceOnly() {
            return resourceOnly;
        }
    }
}