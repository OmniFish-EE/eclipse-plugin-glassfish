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

import static org.eclipse.wst.server.core.IServer.STATE_STARTED;
import static org.glassfish.eclipse.tools.server.utils.WtpUtil.load;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.wst.server.core.IServer;
import org.glassfish.eclipse.tools.server.GlassFishServer;

public class GlassFishStateTester extends PropertyTester {

    @Override
    public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
        IServer server = (IServer) receiver;

        if (property.equals("isRunning")) {
            return (server.getServerState() == STATE_STARTED);
        }

        if (property.equals("isRemote")) {
            GlassFishServer glassfishServer = load(server, GlassFishServer.class);

            if (glassfishServer != null) {
                return glassfishServer.isRemote();
            }
        }

        if (property.equals("isWSLInstance")) {
            GlassFishServer glassfishServer = load(server, GlassFishServer.class);

            if (glassfishServer != null) {
                return glassfishServer.isWSLInstance();
            }
        }

        return false;
    }

}
