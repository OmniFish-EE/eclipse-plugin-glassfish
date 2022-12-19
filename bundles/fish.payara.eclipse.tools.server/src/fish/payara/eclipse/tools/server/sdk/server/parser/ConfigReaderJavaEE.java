/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

/******************************************************************************
 * Copyright (c) 2018-2022 Payara Foundation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package fish.payara.eclipse.tools.server.sdk.server.parser;

import java.util.LinkedList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import fish.payara.eclipse.tools.server.sdk.server.config.JavaEESet;
import fish.payara.eclipse.tools.server.sdk.server.config.ServerConfigException;

/**
 * Java EE configuration XML element reader.
 * <p/>
 *
 * @author Peter Benedikovic, Tomas Kraus
 */
public class ConfigReaderJavaEE extends ConfigReaderJava {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** <code>javaee</code> XML element name. */
    static final String NODE = "javaee";

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** <code>profile</code> XML element reader. */
    private final JavaEEProfileReader profileReader;

    /** <code>module</code> XML element reader. */
    private final JavaEEModuleReader moduleReader;

    /** <code>check</code> XML element reader. */
    private final JavaEEProfileCheckReader checkReader;

    /** JavaEE set for GlassFish features configuration read from XML. */
    JavaEESet javaEE;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an instance of Java EE configuration XML element reader.
     * <p/>
     *
     * @param pathPrefix Tree parser path prefix to be prepended before current XML element.
     */
    ConfigReaderJavaEE(final String pathPrefix) {
        super(pathPrefix, NODE);
        profileReader = new JavaEEProfileReader(path);
        moduleReader = new JavaEEModuleReader(path);
        checkReader = new JavaEEProfileCheckReader(path);
        javaEE = null;
    }

    ////////////////////////////////////////////////////////////////////////////
    // XML reader methods //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Provide paths to listen on.
     * <p/>
     * Sets readers for <code>javaee</code> element and it's content.
     * <p/>
     *
     * @return Paths that the reader listens to.
     */
    @Override
    public List<TreeParser.Path> getPathsToListen() {
        LinkedList<TreeParser.Path> paths = new LinkedList<>();
        paths.add(new TreeParser.Path(path, this));
        paths.add(new TreeParser.Path(profileReader.getPath(), profileReader));
        paths.add(new TreeParser.Path(moduleReader.getPath(), moduleReader));
        paths.addAll(checkReader.getPathsToListen());
        return paths;
    }

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
        super.readAttributes(qname, attributes);
    }

    /**
     * Finish <code>javaee</code> element processing.
     * <p/>
     *
     * @param qname Current XML element name.
     * @throws ServerConfigException when more than one <code>javaee</code> XML elements were found.
     */
    @Override
    public void endNode(final String qname) throws SAXException {
        if (NODE.equals(qname)) {
            if (javaEE != null) {
                throw new SAXException(
                        "Multiple " + NODE + " XML element is not allowed.");
            }
            javaEE = new JavaEESet(
                    getModules(), getProfiles(), getChecks(), getVersion());
            reset();
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters and setters //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get profiles retrieved from XML elements.
     * <p/>
     *
     * @return Profiles retrieved from XML elements.
     */
    private List<JavaEEProfileReader.Profile> getProfiles() {
        return profileReader.getProfiles();
    }

    /**
     * Get modules retrieved from XML elements.
     * <p/>
     *
     * @return Modules sets retrieved from XML elements.
     */
    private List<JavaEEModuleReader.Module> getModules() {
        return moduleReader.getModules();
    }

    /**
     * Get Java EE platform checks retrieved from XML elements.
     * <p/>
     *
     * @return Java EE platform checks retrieved from XML elements.
     */
    private List<JavaEEProfileCheckReader.Check> getChecks() {
        return checkReader.getChecks();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Reset this XML element reader.
     */
    @Override
    void reset() {
        super.reset();
        profileReader.reset();
        moduleReader.reset();
        checkReader.reset();
    }

}
