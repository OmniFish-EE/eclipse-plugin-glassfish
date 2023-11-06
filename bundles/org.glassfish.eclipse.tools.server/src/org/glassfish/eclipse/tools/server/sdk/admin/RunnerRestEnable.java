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

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;

import org.glassfish.eclipse.tools.server.GlassFishServer;

/**
 * Command runner for enabling a disabling the application.
 * <p>
 * <p/>
 *
 * @author Tomas Kraus, Peter Benedikovic
 */
public class RunnerRestEnable extends RunnerRest {

    /**
     * Constructs an instance of administration command executor using REST interface.
     * <p/>
     *
     * @param server GlassFish server entity object.
     * @param command GlassFish server administration command entity.
     */
    public RunnerRestEnable(final GlassFishServer server,
            final Command command) {
        super(server, command);
    }

    @Override
    protected void handleSend(HttpURLConnection hconn) throws IOException {
        CommandTargetName commandApp = (CommandTargetName) command;
        String target = commandApp.target;
        OutputStreamWriter wr = new OutputStreamWriter(hconn.getOutputStream());
        StringBuilder data = new StringBuilder();
        data.append("component=").append(commandApp.name);
        if (target != null) {
            data.append("&target=").append(commandApp.target);
        }

        wr.write(data.toString());
        wr.close();
    }
}
