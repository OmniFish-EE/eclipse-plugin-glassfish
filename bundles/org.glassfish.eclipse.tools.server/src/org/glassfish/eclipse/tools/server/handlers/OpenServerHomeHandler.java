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

import static org.glassfish.eclipse.tools.server.GlassFishServerPlugin.logMessage;
import static org.glassfish.eclipse.tools.server.utils.URIHelper.getServerHomeURI;
import static org.glassfish.eclipse.tools.server.utils.URIHelper.showURI;
import static org.glassfish.eclipse.tools.server.utils.WtpUtil.load;

import org.eclipse.wst.server.core.IServer;
import org.glassfish.eclipse.tools.server.deploying.GlassFishServerBehaviour;

public class OpenServerHomeHandler extends AbstractGlassFishSelectionHandler {

    @Override
    public void processSelection(IServer server) {
        try {
            showURI(getServerHomeURI(load(server, GlassFishServerBehaviour.class).getGlassFishServerDelegate()));
        } catch (Exception e) {
            logMessage("Error opening folder in desktop " + e.getMessage());
        }
    }

}
