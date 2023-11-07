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

package org.glassfish.eclipse.tools.server.log;

import static java.io.File.separator;
import static org.glassfish.eclipse.tools.server.log.AbstractLogFilter.createFilter;

import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.glassfish.eclipse.tools.server.GlassFishServer;

/**
 * This factory class enforces certain rules regarding GlassFish consoles.
 *
 * <ol>
 *     <li>There is only one standard GlassFish console.</li>
 *     <li>A user can trigger showing the server log file console that shows the whole server.log file. </li>
 *     <li>A startup process console exists during the startup process of GlassFish. Unless the startup
 *         does not fail it will not be shown to the user.</li>
 * </ol>
 *
 * @author Peter Benedikovic
 *
 */
public class GlassFishConsoleManager {

    private static IConsoleManager manager = ConsolePlugin.getDefault().getConsoleManager();

    public static IGlassFishConsole showConsole(IGlassFishConsole console) {
        manager.addConsoles(new IConsole[] { console });
        manager.showConsoleView(console);
        return console;
    }

    /**
     * Returns standard console for specified server. For each server there is only one console. It
     * reads information from server.log file but only newly added lines.
     *
     * @param server
     * @return
     */
    public static IGlassFishConsole getStandardConsole(GlassFishServer server) {
        String consoleID = createStandardConsoleName(server);
        IGlassFishConsole gfConsole = findConsole(consoleID);
        if (gfConsole == null) {
            gfConsole = new GlassFishConsole(consoleID, AbstractLogFilter.createFilter(server));
        }

        return gfConsole;
    }

    /**
     * Returns console for showing contents of the whole server.log file. For the same server.log file
     * there is only one console at the time.
     *
     * @param server
     * @return
     */
    public static IGlassFishConsole getServerLogFileConsole(GlassFishServer server) {
        String consoleID = createServerLogConsoleName(server);
        IGlassFishConsole gfConsole = findConsole(consoleID);
        if (gfConsole == null) {
            gfConsole = new GlassFishConsole(consoleID, createFilter(server));
        }

        return gfConsole;
    }

    /**
     * Creates new startup process console. There should be only one for a particular GlassFish server.
     *
     * @param server
     * @return
     */
    public static IGlassFishConsole getStartupProcessConsole(GlassFishServer server, Process launchProcess) {
        String consoleID = createStartupProcessConsoleName(server);
        IGlassFishConsole glassfishConsole = findConsole(consoleID);
        if (glassfishConsole == null) {
            glassfishConsole = new GlassFishStartupConsole(consoleID, new NoOpFilter());
        }

        return glassfishConsole;
    }

    public static void removeServerLogFileConsole(GlassFishServer server) {
        String consoleID = createServerLogConsoleName(server);
        IGlassFishConsole glassfishConsole = findConsole(consoleID);
        if (glassfishConsole != null) {
            manager.removeConsoles(new IConsole[] { glassfishConsole });
        }
    }

    private static String createServerLogConsoleName(GlassFishServer server) {
        return server.isRemote() ? server.getServer().getName()
                : server.getDomainsFolder() + separator + server.getDomainName() + separator + "logs"
                        + separator + "server.log";
    }

    private static String createStartupProcessConsoleName(GlassFishServer server) {
        return server.getServer().getName() + " startup process";
    }

    private static String createStandardConsoleName(GlassFishServer server) {
        return server.getServer().getName();
    }

    private static IGlassFishConsole findConsole(String name) {
        IConsole[] existing = manager.getConsoles();

        for (IConsole element : existing) {
            if (name.equals(element.getName())) {
                return (IGlassFishConsole) element;
            }
        }

        return null;
    }

}
