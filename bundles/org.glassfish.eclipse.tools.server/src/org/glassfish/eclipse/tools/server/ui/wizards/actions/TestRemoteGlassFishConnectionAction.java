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

package org.glassfish.eclipse.tools.server.ui.wizards.actions;

import static org.eclipse.core.runtime.IStatus.ERROR;
import static org.eclipse.core.runtime.IStatus.INFO;
import static org.eclipse.wst.common.frameworks.internal.dialog.ui.MessageDialog.openMessage;
import static org.glassfish.eclipse.tools.server.Messages.versionsNotMatching;
import static org.glassfish.eclipse.tools.server.GlassFishServerPlugin.SYMBOLIC_NAME;
import static org.glassfish.eclipse.tools.server.ServerStatus.RUNNING_CONNECTION_ERROR;
import static org.glassfish.eclipse.tools.server.ServerStatus.RUNNING_CREDENTIAL_PROBLEM;
import static org.glassfish.eclipse.tools.server.ServerStatus.RUNNING_DOMAIN_MATCHING;
import static org.glassfish.eclipse.tools.server.ServerStatus.RUNNING_PROXY_ERROR;
import static org.glassfish.eclipse.tools.server.ServerStatus.RUNNING_REMOTE_NOT_SECURE;
import static org.glassfish.eclipse.tools.server.ServerStatus.STOPPED_NOT_LISTENING;
import static org.glassfish.eclipse.tools.server.utils.ServerStatusHelper.checkServerStatus;
import static org.glassfish.eclipse.tools.server.utils.WtpUtil.load;

import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Display;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.glassfish.eclipse.tools.server.GlassFishRuntime;
import org.glassfish.eclipse.tools.server.GlassFishServer;
import org.glassfish.eclipse.tools.server.ServerStatus;
import org.glassfish.eclipse.tools.server.deploying.GlassFishServerBehaviour;

/**
 * Action that's available on the new server wizard that allows testing the connection to a remote
 * server.
 *
 * <p>
 * Note that is only available for remote servers, not for local servers.
 * </p>
 *
 */
@SuppressWarnings("restriction")
public class TestRemoteGlassFishConnectionAction {

    protected Object run(IServerWorkingCopy wc) {
        GlassFishServer glassfishServer = load(wc, GlassFishServer.class);

        ServerStatus serverStatus = checkServerStatus(glassfishServer);

        if (!serverStatus.equals(RUNNING_DOMAIN_MATCHING)) {
            StringBuilder errorMessage = new StringBuilder();
            errorMessage.append("Cannot communicate with ")
                    .append(glassfishServer.getServer().getHost())
                    .append(":")
                    .append(glassfishServer.getAdminPort())
                    .append(" remote server.");

            // Give some hints
            if (serverStatus.equals(STOPPED_NOT_LISTENING)) {
                errorMessage.append(" Is it up?");
            } else if (serverStatus.equals(RUNNING_REMOTE_NOT_SECURE)) {
                errorMessage.append(" Is it secure? (Hint: run asadmin enable-secure-admin)");
            } else if (serverStatus.equals(RUNNING_CREDENTIAL_PROBLEM)) {
                errorMessage.append(" Wrong user name or password. Check your credentials.");
            } else if (serverStatus.equals(RUNNING_PROXY_ERROR)) {
                errorMessage.append(" Check your proxy settings.");
            } else if (serverStatus.equals(RUNNING_CONNECTION_ERROR)) {
                // Add all possible hints
                errorMessage.append(" Is it up?")
                        .append(" Is it secure? (Hint: run asadmin enable-secure-admin)");
            }

            openMessage(
                    Display.getDefault().getActiveShell(),
                    "Error", "Error connecting to remote server",
                    new Status(ERROR, SYMBOLIC_NAME, errorMessage.toString()));

        } else {

            // Check server version

            String remoteServerVersion = GlassFishServerBehaviour.getVersion(glassfishServer);
            String thisServerVersion = wc.getRuntime()
                    .getAdapter(GlassFishRuntime.class)
                    .getVersion()
                    .toString();

            int n = thisServerVersion.indexOf(".X");
            if (n > 0) {
                thisServerVersion = thisServerVersion.substring(0, n + 1);
            }

            if (remoteServerVersion != null && remoteServerVersion.indexOf(thisServerVersion) < 0) {

                openMessage(
                        Display.getDefault().getActiveShell(),
                        "Error",
                        versionsNotMatching,
                        new Status(ERROR, SYMBOLIC_NAME, "The remote server version is " + remoteServerVersion));

            } else {

                // Everything seems to be OK
                openMessage(
                        Display.getDefault().getActiveShell(),
                        "Connection successful",
                        "Connection to server was successful",
                        new Status(INFO, SYMBOLIC_NAME, "Connection to server was successful"));
            }
        }

        return null;

    }

}
