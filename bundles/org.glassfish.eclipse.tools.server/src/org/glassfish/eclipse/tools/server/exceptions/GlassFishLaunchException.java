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

package org.glassfish.eclipse.tools.server.exceptions;

public class GlassFishLaunchException extends Exception {

    private static final long serialVersionUID = -3931653934641477601L;

    private Process glassfishProcess;

    public GlassFishLaunchException() {
        super();
    }

    public GlassFishLaunchException(String message, Throwable cause) {
        this(message, cause, null);
    }

    public GlassFishLaunchException(String message, Process gfProcess) {
        this(message, null, gfProcess);
    }

    public GlassFishLaunchException(String message, Throwable cause, Process glassfishProcess) {
        super(message, cause);
        this.glassfishProcess = glassfishProcess;
    }

    public GlassFishLaunchException(String message) {
        this(message, null, null);
    }

    public GlassFishLaunchException(Throwable cause) {
        this(null, cause, null);
    }

    public Process getStartedProcess() {
        return glassfishProcess;
    }

}
