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

package org.glassfish.eclipse.tools.server.handlers;

import static org.eclipse.ui.browser.IWorkbenchBrowserSupport.LOCATION_BAR;
import static org.eclipse.ui.browser.IWorkbenchBrowserSupport.NAVIGATION_BAR;
import static org.glassfish.eclipse.tools.server.GlassFishServerPlugin.logMessage;
import static org.glassfish.eclipse.tools.server.utils.URIHelper.getServerAdminURI;
import static org.glassfish.eclipse.tools.server.utils.WtpUtil.load;

import org.eclipse.ui.PlatformUI;
import org.eclipse.wst.server.core.IServer;
import org.glassfish.eclipse.tools.server.deploying.GlassFishServerBehaviour;

public class ViewAdminConsoleHandler extends AbstractGlassFishSelectionHandler {

    @Override
    public void processSelection(IServer server) {
        try {
            PlatformUI.getWorkbench()
                    .getBrowserSupport()
                    .createBrowser(
                            LOCATION_BAR | NAVIGATION_BAR,
                            null, null, null)
                    .openURL(
                            getServerAdminURI(
                                    load(server, GlassFishServerBehaviour.class).getGlassFishServerDelegate())
                            .toURL());

        } catch (Exception e) {
            logMessage("Error opening browser: " + e.getMessage());
        }
    }

}
