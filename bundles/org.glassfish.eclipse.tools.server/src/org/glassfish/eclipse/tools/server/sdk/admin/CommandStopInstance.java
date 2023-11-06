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

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import org.glassfish.eclipse.tools.server.GlassFishServer;
import org.glassfish.eclipse.tools.server.sdk.GlassFishIdeException;

/**
 * Payara Server Stop Instance Command Entity.
 * <p/>
 * Holds data for command. Objects of this class are created by API user.
 * <p/>
 *
 * @author Tomas Kraus, Peter Benedikovic
 */
@RunnerHttpClass(runner = RunnerHttpTarget.class)
@RunnerRestClass(runner = RunnerRestStopInstance.class)
public class CommandStopInstance extends CommandTarget {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** Command string for stop-instance command. */
    private static final String COMMAND = "stop-instance";

    /** Error message for administration command execution exception . */
    private static final String ERROR_MESSAGE = "Instance stop failed.";

    ////////////////////////////////////////////////////////////////////////////
    // Static methods //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Stops server instance.
     * <p/>
     *
     * @param server GlassFish server entity.
     * @param target Instance name.
     * @return Stop instance task response.
     * @throws GlassFishIdeException When error occurred during administration command execution.
     */
    public static ResultString stopInstance(GlassFishServer server, String target) throws GlassFishIdeException {
        try {
            return ServerAdmin.<ResultString>exec(server, new CommandStopInstance(target)).get();
        } catch (InterruptedException | ExecutionException | CancellationException ie) {
            throw new GlassFishIdeException(ERROR_MESSAGE, ie);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Constructors //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of GlassFish server stop-instance command entity.
     * <p/>
     *
     * @param target Target GlassFish instance.
     */
    public CommandStopInstance(String target) {
        super(COMMAND, target);
    }

}
