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

package fish.payara.eclipse.tools.server.ui.serverview.dynamicnodes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import fish.payara.eclipse.tools.server.PayaraServer;
import fish.payara.eclipse.tools.server.PayaraServerPlugin;
import fish.payara.eclipse.tools.server.serverview.AppDesc;
import fish.payara.eclipse.tools.server.utils.NodesUtils;

/**
 * This node shows is the root node below which the dynamically retrieved deployed applications
 * reside.
 *
 * <p>
 * The following depicts this element in the "Servers" views:
 * </p>
 * <p>
 *
 * <pre>
 * Payara 5 [domain1]
 * |- GlassFish Management
 *     |-Resources
 *     |-Deployed Applications *
 *     |-Deployed Web Services
 * |- [WTP managed application]
 * </pre>
 * </p>
 *
 * <p>
 * Note this is a (potentially) different list from the one that WTP keeps.
 * </p>
 *
 * <p>
 * Payara / GlassFish is dynamically queried for this list, hence it can only be retrieved for a
 * running server.
 * </p>
 *
 */
public class DeployedApplicationsNode extends TreeNode {

    PayaraServer server;
    ApplicationNode[] deployedapps;

    public DeployedApplicationsNode(PayaraServer server) {
        super("Deployed Applications", null, null);
        this.server = server;
    }

    public PayaraServer getServer() {
        return server;
    }

    @Override
    public Object[] getChildren() {

        ArrayList<ApplicationNode> appsList = new ArrayList<>();

        if (deployedapps == null) {

            try {

                if (server == null) {
                    deployedapps = appsList.toArray(new ApplicationNode[appsList.size()]);
                    return deployedapps;
                }

                try {
                    Map<String, List<AppDesc>> appMap = NodesUtils.getApplications(server, null);

                    for (Entry<String, List<AppDesc>> entry : appMap.entrySet()) {

                        List<AppDesc> apps = entry.getValue();

                        for (AppDesc app : apps) {
                            ApplicationNode t = new ApplicationNode(this, server, app);
                            appsList.add(t);
                        }

                    }

                } catch (Exception ex) {
                    PayaraServerPlugin.logError("get Applications is failing=", ex); //$NON-NLS-1$
                }

            } catch (Exception e) {

            }

            this.deployedapps = appsList.toArray(new ApplicationNode[appsList.size()]);

        }

        return this.deployedapps;

    }

    public void refresh() {
        deployedapps = null;
    }

}
