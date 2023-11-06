/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

/******************************************************************************
 * Copyright (c) 2018-2022 XXXXX Foundation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.glassfish.eclipse.tools.server.sdk.server.parser;

import java.util.LinkedList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * <code>file</code> library configuration XML element reader.
 * <p/>
 *
 * @author Peter Benedikovic, Tomas Kraus
 */
public class PathReader extends AbstractReader {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** <code>file</code> XML element name. */
    static final String NODE = "file";

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** Paths retrieved from XML elements. */
    private List<String> paths = new LinkedList<>();

    ////////////////////////////////////////////////////////////////////////////
    // Constructors //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an instance of Java EE platform check configuration XML element reader.
     * <p/>
     *
     * @param pathPrefix Tree parser path prefix to be prepended before current XML element.
     */
    PathReader(final String pathPrefix) {
        super(pathPrefix, NODE);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Tree parser methods //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Process attributes from current XML element.
     * <p/>
     *
     * @param qname Not used.
     * @param attributes List of XML attributes.
     * @throws SAXException When any problem occurs.
     */
    @Override
    public void readAttributes(final String qname, final Attributes attributes)
            throws SAXException {
        paths.add(attributes.getValue("path"));
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters and setters //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get paths retrieved from XML elements.
     * <p/>
     *
     * @return Paths sets retrieved from XML elements.
     */
    public List<String> getPaths() {
        return paths;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Reset this XML element reader.
     */
    public void reset() {
        paths = new LinkedList<>();
    }

}
