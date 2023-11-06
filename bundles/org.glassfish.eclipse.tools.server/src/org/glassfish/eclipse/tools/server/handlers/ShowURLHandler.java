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

import java.net.URL;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PlatformUI;

public class ShowURLHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        String url = event.getParameter("org.glassfish.eclipse.tools.server.commands.urlParam");

        // This should not happen
        if (url == null) {
            return null;
        }

        try {
            PlatformUI.getWorkbench()
                    .getBrowserSupport()
                    .createBrowser(LOCATION_BAR | NAVIGATION_BAR, null, null, null)
                    .openURL(new URL(url));
        } catch (Exception e) {
            logMessage("Error opening browser: " + e.getMessage());
        }

        return null;
    }

}
