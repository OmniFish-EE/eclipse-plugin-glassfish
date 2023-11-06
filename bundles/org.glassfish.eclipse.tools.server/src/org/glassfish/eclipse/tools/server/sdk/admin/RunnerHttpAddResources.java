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

package org.glassfish.eclipse.tools.server.sdk.admin;

import org.glassfish.eclipse.tools.server.GlassFishServer;

/**
 * Runner executes add-resources command via HTTP interface.
 * <p/>
 *
 * @author Peter Benedikovic, Tomas Kraus
 */
public class RunnerHttpAddResources extends RunnerHttp {

    ////////////////////////////////////////////////////////////////////////////
    // Static methods //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Builds add-resources query string for given command.
     * <p/>
     *
     * @param command GlassFish server administration command entity. <code>CommandAddResources</code>
     * instance is expected.
     * @return Add-resources query string for given command.
     */
    private static String query(Command command) {
        CommandAddResources cmd = (CommandAddResources) command;
        StringBuilder sb = new StringBuilder();
        sb.append("xml_file_name=");
        sb.append(cmd.xmlResFile.getAbsolutePath());
        if (cmd.target != null) {
            sb.append("&target=");
            sb.append(cmd.target);
        }
        return sb.toString();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Constructors //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of administration command executor using HTTP interface.
     * <p/>
     *
     * @param server GlassFish server entity object.
     * @param command GlassFish server administration command entity.
     */
    public RunnerHttpAddResources(final GlassFishServer server,
            final Command command) {
        super(server, command, query(command));
    }
}
