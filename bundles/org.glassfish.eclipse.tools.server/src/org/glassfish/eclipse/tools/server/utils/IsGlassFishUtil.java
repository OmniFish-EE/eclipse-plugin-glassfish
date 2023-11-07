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

package org.glassfish.eclipse.tools.server.utils;

import org.eclipse.wst.common.project.facet.core.runtime.IRuntime;
import org.eclipse.wst.common.project.facet.core.runtime.IRuntimeComponent;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerType;

/**
 * Set of utility methods to help determining whether something constitutes "GlassFish" or "GlassFish".
 *
 *
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class IsGlassFishUtil {

    public static boolean isGlassFish(org.eclipse.wst.server.core.IRuntime runtime) {
        if (runtime != null) {
            return runtime.getRuntimeType().getId().equals("glassfish.runtime");
        }

        return false;
    }

    public static boolean isGlassFish(IRuntime runtime) {
        if (runtime != null) {
            for (IRuntimeComponent component : runtime.getRuntimeComponents()) {
                return isGlassFish(component);
            }
        }

        return false;
    }

    public static boolean isGlassFish(IRuntimeComponent component) {
        if (component != null) {
            return component.getRuntimeComponentType().getId().equals("glassfish.runtime");
        }

        return false;
    }

    public static boolean isGlassFish(IServer server) {
        if (server != null) {
            return isGlassFish(server.getServerType());
        }

        return false;
    }


    public static boolean isGlassFish(IServerType type) {
        if (type != null) {
            return type.getId().equals("glassfish.server");
        }

        return false;
    }
}
