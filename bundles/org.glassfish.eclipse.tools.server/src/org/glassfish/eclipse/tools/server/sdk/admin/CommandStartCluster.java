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

package org.glassfish.eclipse.tools.server.sdk.admin;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.glassfish.eclipse.tools.server.GlassFishServer;
import org.glassfish.eclipse.tools.server.sdk.GlassFishIdeException;

/**
 * GlassFish Server Start Cluster Command Entity.
 * <p/>
 * Holds data for command. Objects of this class are created by API user.
 * <p/>
 *
 * @author Tomas Kraus, Peter Benedikovic
 */
@RunnerHttpClass(runner = RunnerHttpTarget.class)
@RunnerRestClass(runner = RunnerRestStartCluster.class)
public class CommandStartCluster extends CommandTarget {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** Command string for start-cluster command. */
    private static final String COMMAND = "start-cluster";

    /** Error message for administration command execution exception . */
    private static final String ERROR_MESSAGE = "Cluster start failed.";

    ////////////////////////////////////////////////////////////////////////////
    // Static methods //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Starts cluster.
     * <p/>
     *
     * @param server GlassFish server entity.
     * @param target Cluster name.
     * @return Start cluster task response.
     * @throws GlassFishIdeException When error occurred during administration command execution.
     */
    public static ResultString startCluster(GlassFishServer server,
            String target) throws GlassFishIdeException {
        Command command = new CommandStartCluster(target);
        Future<ResultString> future = ServerAdmin.<ResultString>exec(server, command);
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException
                | CancellationException ie) {
            throw new GlassFishIdeException(ERROR_MESSAGE, ie);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Constructors //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of GlassFish server start-cluster command entity.
     * <p/>
     *
     * @param target Target GlassFish cluster.
     */
    public CommandStartCluster(String target) {
        super(COMMAND, target);
    }

}
