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

package org.glassfish.eclipse.tools.server.ui;

import static org.eclipse.jface.resource.ImageDescriptor.createFromURL;
import static org.eclipse.wst.server.core.IServer.STATE_UNKNOWN;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerLifecycleListener;
import org.eclipse.wst.server.core.internal.ResourceManager;
import org.eclipse.wst.server.core.internal.UpdateServerJob;
import org.glassfish.eclipse.tools.server.GlassFishServer;

/**
 * This is used as the OSGi bundle activator, as well as the central place to get images from.
 */
@SuppressWarnings("restriction")
public class GlassFishToolsUIPlugin extends AbstractUIPlugin {

    public static final String PLUGIN_ID = "org.glassfish.eclipse.tools.server.ui";

    public static final String EAR_MODULE_IMG = "ear.img";
    public static final String EJB_MODULE_IMG = "ejb.img";
    public static final String GF_SERVER_IMG = "gf-server.img";
    public static final String LOG_FILE_IMG = "log-file.img";
    public static final String WEB_MODULE_IMG = "web.img";
    public static final String WEBSERVICE_IMG = "webservice.img";
    public static final String RESOURCES_IMG = "resources.img";
    public static final String GF_WIZARD = "wizard.img";

    private static GlassFishToolsUIPlugin instance;

    public GlassFishToolsUIPlugin() {
        instance = this;
        addServerLifecycleListener();
    }

    /**
     * Start a UpdateServerJob to update status when GF server is added
     */
    private void addServerLifecycleListener() {

        IServerLifecycleListener serverLifecycleListener = new IServerLifecycleListener() {
            @Override
            public void serverAdded(IServer server) {
                if (server.loadAdapter(GlassFishServer.class, new NullProgressMonitor()) != null) {
                    if (server.getServerState() == STATE_UNKNOWN) {
                        UpdateServerJob job = new UpdateServerJob(new IServer[] { server });
                        job.schedule();
                    }
                }
            }

            @Override
            public void serverChanged(IServer server) {
            }

            @Override
            public void serverRemoved(IServer server) {
            }
        };

        ResourceManager.getInstance().addServerLifecycleListener(serverLifecycleListener);
    }

    public static final GlassFishToolsUIPlugin getInstance() {
        return instance;
    }

    public static Image getImg(String key) {
        return getInstance().getImageRegistry().get(key);
    }

    @Override
    protected void initializeImageRegistry(ImageRegistry reg) {
        super.initializeImageRegistry(reg);

        reg.put(EAR_MODULE_IMG, createFromURL(getBundle().getEntry("icons/obj16/ear.gif")));
        reg.put(EJB_MODULE_IMG, createFromURL(getBundle().getEntry("icons/obj16/ejb_module.gif")));
        reg.put(GF_SERVER_IMG, createFromURL(getBundle().getEntry("icons/obj16/glassfish-blue.png")));
        reg.put(LOG_FILE_IMG, createFromURL(getBundle().getEntry("icons/obj16/logfile.png")));
        reg.put(WEB_MODULE_IMG, createFromURL(getBundle().getEntry("icons/obj16/web_module.gif")));
        reg.put(WEBSERVICE_IMG, createFromURL(getBundle().getEntry("icons/obj16/webservice.png")));
        reg.put(RESOURCES_IMG, createFromURL(getBundle().getEntry("icons/obj16/resources.gif")));
        reg.put(GF_WIZARD, createFromURL(getBundle().getEntry("icons/wizard75x66.png")));
    }

}
