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

package fish.payara.eclipse.tools.server.internal;

import static fish.payara.eclipse.tools.server.ServerStatus.RUNNING_DOMAIN_MATCHING;
import static fish.payara.eclipse.tools.server.ServerStatus.STOPPED_DOMAIN_NOT_MATCHING;
import static fish.payara.eclipse.tools.server.ServerStatus.STOPPED_NOT_LISTENING;
import static org.eclipse.wst.server.core.IServer.STATE_STARTED;
import static org.eclipse.wst.server.core.IServer.STATE_STARTING;
import static org.eclipse.wst.server.core.IServer.STATE_STOPPED;
import static org.eclipse.wst.server.core.IServer.STATE_STOPPING;
import static org.eclipse.wst.server.core.IServer.STATE_UNKNOWN;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.wst.server.core.IServer;

import fish.payara.eclipse.tools.server.ServerStatus;

public class PayaraStateResolver {

    private static final Map<ServerStatus, Map<Integer, Integer>> matrix = new HashMap<>();

    private static final int DEFAULT_ACTION = Integer.MAX_VALUE;

    // This is a decision matrix for finding the correct new state based
    // on the current state and status
    static {
        HashMap<Integer, Integer> m = new HashMap<>();

        // NOT_DEFINED
        m.put(DEFAULT_ACTION, STATE_UNKNOWN);
        matrix.put(ServerStatus.NOT_DEFINED, m);
        m = new HashMap<>();

        // RUNNING_CONNECTION_ERROR
        m.put(DEFAULT_ACTION, STATE_STOPPED);
        m.put(IServer.STATE_STARTING, STATE_STARTING);
        matrix.put(ServerStatus.RUNNING_CONNECTION_ERROR, m);
        m = new HashMap<>();

        // RUNNING_PROXY_ERROR
        m.put(DEFAULT_ACTION, STATE_STOPPED);
        matrix.put(ServerStatus.RUNNING_PROXY_ERROR, m);
        m = new HashMap<>();

        // RUNNING_CREDENTIAL_ERROR
        m.put(DEFAULT_ACTION, STATE_STOPPED);
        matrix.put(ServerStatus.RUNNING_CREDENTIAL_PROBLEM, m);
        m = new HashMap<>();

        // STOPPED_DOMAIN_NOT_MATCHING
        m.put(DEFAULT_ACTION, STATE_STOPPED);
        matrix.put(STOPPED_DOMAIN_NOT_MATCHING, m);
        m = new HashMap<>();

        // STOPPED_NOT_LISTENING
        m.put(DEFAULT_ACTION, STATE_STOPPED);
        m.put(STATE_STARTING, STATE_STARTING);
        matrix.put(STOPPED_NOT_LISTENING, m);
        m = new HashMap<>();

        // RUNNING_DOMAIN_MATCHING
        m.put(DEFAULT_ACTION, STATE_STARTED);
        m.put(IServer.STATE_STOPPING, STATE_STOPPING);
        matrix.put(RUNNING_DOMAIN_MATCHING, m);
    }

    public int resolve(ServerStatus status, int actualState) {
        Integer state = matrix.get(status).get(actualState);

        if (state == null) {
            state = matrix.get(status).get(DEFAULT_ACTION);
        }

        return state;
    }

}
