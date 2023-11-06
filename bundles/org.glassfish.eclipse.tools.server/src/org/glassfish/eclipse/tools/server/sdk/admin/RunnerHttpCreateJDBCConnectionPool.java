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

import org.glassfish.eclipse.tools.server.GlassFishServer;

/**
 * GlassFish server create JDBC connection pool administration command execution using HTTP
 * interface.
 * <p/>
 * Contains code for create JDBC connection pool command. Class implements GlassFish server
 * administration functionality trough HTTP interface.
 * <p/>
 *
 * @author Tomas Kraus, Peter Benedikovic
 */
public class RunnerHttpCreateJDBCConnectionPool extends RunnerHttp {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Create JDBC connection pool command <code>connectionpoolid</code> parameter name.
     */
    private static final String CONN_POOL_ID_PARAM = "jdbc_connection_pool_id";

    /**
     * Create JDBC connection pool command <code>datasourceclassname</code> parameter name.
     */
    private static final String DS_CLASS_NAME_PARAM = "datasourceclassname";

    /**
     * Create JDBC connection pool command <code>restype</code> parameter name.
     */
    private static final String RESOURCE_TYPE_PARAM = "restype";

    /**
     * Create JDBC connection pool command <code>property</code> parameter name.
     */
    private static final String PROPERTY_PARAM = "property";

    ////////////////////////////////////////////////////////////////////////////
    // Static methods //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Builds create JDBC connection pool query string for given command.
     * <p/>
     * <code>QUERY :: "jdbc_connection_pool_id" '=' &lt;connectionPoolId&gt;<br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; ['&' "datasourceclassname" '=' &lt;datasourceclassname&gt; ]<br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; ['&' "restype" '=' &lt;restype&gt; ]<br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; ['&' "properties" '=' &lt;pname&gt; '=' &lt;pvalue&gt;
     * { ':' &lt;pname&gt; '=' &lt;pvalue&gt;} ]</code>
     * <p/>
     *
     * @param command GlassFish server administration command entity.
     * <code>CommandCreateJDBCConnectionPool</code> instance is expected.
     * @return Create JDBC connection pool query string for given command.
     */
    private static String query(final Command command) {
        String connectionPoolId;
        String dataSourceClassName;
        String resType;
        if (command instanceof CommandCreateJDBCConnectionPool) {
            connectionPoolId = ((CommandCreateJDBCConnectionPool) command).connectionPoolId;
            dataSourceClassName = ((CommandCreateJDBCConnectionPool) command).dataSourceClassName;
            resType = ((CommandCreateJDBCConnectionPool) command).resType;
        } else {
            throw new CommandException(
                    CommandException.ILLEGAL_COMAND_INSTANCE);
        }
        boolean isDataSourceClassName = dataSourceClassName != null
                && dataSourceClassName.length() > 0;
        boolean isResType = resType != null && resType.length() > 0;
        // Calculate StringBuilder initial length to avoid resizing
        StringBuilder sb = new StringBuilder(
                CONN_POOL_ID_PARAM.length() + 1 + connectionPoolId.length()
                        + (isDataSourceClassName
                                ? DS_CLASS_NAME_PARAM.length()
                                        + 1 + dataSourceClassName.length()
                                : 0)
                        + (isResType
                                ? RESOURCE_TYPE_PARAM.length() + 1 + resType.length()
                                : 0)
                        + queryPropertiesLength(
                                ((CommandCreateJDBCConnectionPool) command).properties,
                                PROPERTY_PARAM));
        // Build query string
        sb.append(CONN_POOL_ID_PARAM).append(PARAM_ASSIGN_VALUE);
        sb.append(connectionPoolId);
        if (isDataSourceClassName) {
            sb.append(PARAM_SEPARATOR).append(DS_CLASS_NAME_PARAM);
            sb.append(PARAM_ASSIGN_VALUE).append(dataSourceClassName);
        }
        if (isResType) {
            sb.append(PARAM_SEPARATOR).append(RESOURCE_TYPE_PARAM);
            sb.append(PARAM_ASSIGN_VALUE).append(resType);
        }
        queryPropertiesAppend(sb,
                ((CommandCreateJDBCConnectionPool) command).properties,
                PROPERTY_PARAM, true);
        return sb.toString();
    }

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
    public RunnerHttpCreateJDBCConnectionPool(final GlassFishServer server,
            final Command command) {
        super(server, command, query(command));
    }
}
