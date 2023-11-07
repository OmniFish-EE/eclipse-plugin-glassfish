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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.glassfish.eclipse.tools.server.GlassFishServer;

/**
 * Command runner for retrieving list of web services from server.
 * <p>
 *
 * @author Tomas Kraus, Peter Benedikovic
 */
public class RunnerHttpListWebServices extends RunnerHttp {

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * GlassFish administration command result containing server resources.
     * <p/>
     * Result instance life cycle is started with submitting task into <code>ExecutorService</code>'s
     * queue. method <code>call()</code> is responsible for correct <code>TaskState</code> and
     * receiveResult value handling.
     */
    @SuppressWarnings("FieldNameHidesFieldInSuperclass")
    ResultList<String> result;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of administration command executor using HTTP interface.
     * <p/>
     *
     * @param server GlassFish server entity object.
     * @param command GlassFish server administration command entity.
     */
    public RunnerHttpListWebServices(final GlassFishServer server,
            final Command command) {
        super(server, command);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Implemented Abstract Methods //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Create <code>ResultList</code> object corresponding to server log command execution value to be
     * returned.
     */
    @Override
    protected ResultList<String> createResult() {
        return result = new ResultList<>();
    }

    /**
     * Extracts result value from internal <code>Manifest</code> object. Value of <i>message</i>
     * attribute in <code>Manifest</code> object is stored as <i>value</i> into
     * <code>ResultString</code> result object.
     * <p/>
     *
     * @return true if result was extracted correctly. <code>null</code> <i>message</i>value is
     * considered as failure.
     */
    @Override
    protected boolean processResponse() {
        if (manifest == null) {
            return false;
        }
        result.value = new ArrayList<>();
        Map<String, String> filter = new HashMap<>();
        for (String k : manifest.getEntries().keySet()) {
            if (!k.contains("address:/") || k.contains("address:/wsat-wsat") || k.contains("address:/__wstx-services")) {
                continue;
            }
            String a = k.replaceFirst(".* address:/", "").replaceFirst("\\. .*", ""); // NOI18N
            if (filter.containsKey(a)) {
                continue;
            }
            filter.put(a, a);
            result.value.add(a);
        }
        return true;
    }

}
