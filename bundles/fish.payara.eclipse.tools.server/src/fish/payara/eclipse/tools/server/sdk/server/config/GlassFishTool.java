/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

/******************************************************************************
 * Copyright (c) 2018 Payara Foundation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package fish.payara.eclipse.tools.server.sdk.server.config;

/**
 * GlassFish tool.
 * <p/>
 *
 * @author Peter Benedikovic, Tomas Kraus
 */
public abstract class GlassFishTool {

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** Tools library directory (relative under GlassFish home). */
    private final String lib;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an instance of GlassFish tool.
     * <p/>
     *
     * @param lib Tools library directory (relative under GlassFish home).
     */
    public GlassFishTool(final String lib) {
        this.lib = lib;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters and setters //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get tools library directory (relative under GlassFish home).
     * <p/>
     *
     * @return Tools library directory (relative under GlassFish home).
     */
    public String getLib() {
        return lib;
    }

}
