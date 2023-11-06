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

package org.glassfish.eclipse.tools.server.sdk.data;

import java.io.File;
import java.util.Set;

import org.glassfish.eclipse.tools.server.sdk.server.config.JavaEEProfile;
import org.glassfish.eclipse.tools.server.sdk.server.config.JavaEESet;
import org.glassfish.eclipse.tools.server.sdk.server.config.ModuleType;

/**
 * GlassFish JavaEE configuration entity.
 * <p/>
 *
 * @author Peter Benedikovic, Tomas Kraus
 */
public class GlassFishJavaEEConfig {

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** Supported module types. */
    private final Set<ModuleType> modules;

    /** Supported JavaEE profiles. */
    private final Set<JavaEEProfile> profiles;

    /** Highest JavaEE specification version implemented. */
    private final String version;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an instance of avaEE configuration entity using JavaEE set for GlassFish features
     * configuration as source of instance content.
     * <p/>
     *
     * @param javaEEconfig Container of GlassFish JavaEE features configuration.
     * @param classpathHome Classpath search prefix.
     */
    public GlassFishJavaEEConfig(
            final JavaEESet javaEEconfig, final File classpathHome) {
        modules = javaEEconfig.moduleTypes(classpathHome);
        profiles = javaEEconfig.profiles(classpathHome);
        version = javaEEconfig.getVersion();
        javaEEconfig.reset();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters and setters //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get highest JavaEE specification version implemented.
     * <p/>
     *
     * @return Highest JavaEE specification version implemented.
     */
    public String getVersion() {
        return version;
    }

    /**
     * Get supported JavaEE profiles.
     * <p/>
     *
     * @return Supported JavaEE profiles.
     */
    public Set<JavaEEProfile> getProfiles() {
        return profiles;
    }

    /**
     * Get supported module types.
     * <p/>
     *
     * @return Supported module types.
     */
    public Set<ModuleType> getModuleTypes() {
        return modules;
    }

}
