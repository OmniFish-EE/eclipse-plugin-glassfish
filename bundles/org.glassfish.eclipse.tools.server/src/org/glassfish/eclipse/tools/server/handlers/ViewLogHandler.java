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

package org.glassfish.eclipse.tools.server.handlers;

import static org.glassfish.eclipse.tools.server.GlassFishServerPlugin.logMessage;
import static org.glassfish.eclipse.tools.server.log.GlassFishConsoleManager.getServerLogFileConsole;
import static org.glassfish.eclipse.tools.server.log.GlassFishConsoleManager.removeServerLogFileConsole;
import static org.glassfish.eclipse.tools.server.log.GlassFishConsoleManager.showConsole;
import static org.glassfish.eclipse.tools.server.utils.WtpUtil.load;

import org.eclipse.wst.server.core.IServer;
import org.glassfish.eclipse.tools.server.GlassFishServer;
import org.glassfish.eclipse.tools.server.ServerStatus;
import org.glassfish.eclipse.tools.server.log.IGlassFishConsole;
import org.glassfish.eclipse.tools.server.sdk.server.FetchLogPiped;

public class ViewLogHandler extends AbstractGlassFishSelectionHandler {

    @Override
    public void processSelection(IServer server) {
        try {
            GlassFishServer serverAdapter = load(server, GlassFishServer.class);

            if (serverAdapter.isRemote()) {
                if (!serverAdapter.getServerBehaviourAdapter().getServerStatus(true).equals(ServerStatus.RUNNING_DOMAIN_MATCHING)) {
                    showMessageDialog();
                    return;
                }

                removeServerLogFileConsole(serverAdapter);
            }

            IGlassFishConsole console = getServerLogFileConsole(serverAdapter);
            showConsole(getServerLogFileConsole(serverAdapter));

            if (!console.isLogging()) {
                console.startLogging(FetchLogPiped.create(serverAdapter, false));
            }

        } catch (Exception e) {
            logMessage("Error opening log: " + e.getMessage());

        }
    }

}
