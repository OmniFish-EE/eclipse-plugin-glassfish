/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

/******************************************************************************
 * Copyright (c) 2019-2020 XXXXX Foundation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.glassfish.eclipse.tools.server.starting;

import static org.glassfish.eclipse.tools.server.GlassFishServerPlugin.logMessage;
import static org.glassfish.eclipse.tools.server.log.GlassFishConsoleManager.getStandardConsole;
import static org.glassfish.eclipse.tools.server.log.GlassFishConsoleManager.getStartupProcessConsole;
import static org.glassfish.eclipse.tools.server.log.GlassFishConsoleManager.showConsole;
import static org.glassfish.eclipse.tools.server.sdk.server.ServerTasks.getDebugPort;
import static org.glassfish.eclipse.tools.server.sdk.server.ServerTasks.startServer;
import static org.glassfish.eclipse.tools.server.sdk.server.ServerTasks.StartMode.DEBUG;
import static org.glassfish.eclipse.tools.server.starting.GlassFishServerLaunchDelegate.WORK_STEP;

import java.util.concurrent.Callable;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.glassfish.eclipse.tools.server.deploying.GlassFishServerBehaviour;
import org.glassfish.eclipse.tools.server.exceptions.GlassFishLaunchException;
import org.glassfish.eclipse.tools.server.log.IPayaraConsole;
import org.glassfish.eclipse.tools.server.sdk.GlassFishIdeException;
import org.glassfish.eclipse.tools.server.sdk.admin.ResultProcess;
import org.glassfish.eclipse.tools.server.sdk.server.FetchLogSimple;
import org.glassfish.eclipse.tools.server.sdk.server.ServerTasks.StartMode;

public class GlassFishStartJob implements Callable<ResultProcess> {

    private GlassFishServerBehaviour payaraServerBehaviour;
    private StartupArgsImpl args;
    private StartMode mode;
    private ILaunchConfiguration configuration;
    private ILaunch launch;
    private IProgressMonitor monitor;

    public GlassFishStartJob(GlassFishServerBehaviour payaraServerBehaviour, StartupArgsImpl args, StartMode mode, ILaunchConfiguration configuration, ILaunch launch, IProgressMonitor monitor) {
        super();
        this.payaraServerBehaviour = payaraServerBehaviour;
        this.args = args;
        this.mode = mode;
        this.configuration = configuration;
        this.launch = launch;
        this.monitor = monitor;
    }

    @Override
    public ResultProcess call() throws Exception {

        boolean earlyAttach = payaraServerBehaviour.getGlassFishServerDelegate().getAttachDebuggerEarly();

        // Create the process that starts the server
        ResultProcess process = startPayara(earlyAttach);

        Process payaraProcess = process.getValue().getProcess();

        // Read process std output to prevent process'es blocking
        IPayaraConsole startupConsole = startLogging(payaraProcess);

        IPayaraConsole filelogConsole = getStandardConsole(payaraServerBehaviour.getGlassFishServerDelegate());

        synchronized (payaraServerBehaviour) {

            boolean attached = false;
            boolean hasLogged = false;
            boolean hasLoggedPayara = false;

            // Query the process status in a loop

            check_server_status: while (true) {

                switch (payaraServerBehaviour.getServerStatus(false)) {
                    case STOPPED_NOT_LISTENING:
                        try {
                            if (payaraProcess.isAlive()) {

                                // Server is not (yet) listening.
                                // Check if we need to attach the debugger for it to continue.
                                // This happens when the server is started in debug with halt on start

                                if (earlyAttach && mode == DEBUG && !attached) {
                                    try {
                                        payaraServerBehaviour.attach(launch, configuration.getWorkingCopy(), null, getDebugPort(process));
                                        checkMonitorAndProgress(monitor, WORK_STEP);
                                        attached = true;
                                    } catch (CoreException e) {
                                        // Process may not have reached the point where it waits for a remote connection
                                        logMessage(e.getMessage());
                                    }
                                }
                            } else {
                                int exitCode = payaraProcess.exitValue();

                                if (exitCode != 0) {
                                    // Something bad happened, show user startup console

                                    logMessage("launch failed with exit code " + exitCode);
                                    showConsole(startupConsole);

                                    throw new GlassFishLaunchException("Launch process failed with exit code " + exitCode);
                                }
                            }

                        } catch (IllegalThreadStateException e) { // still running, keep waiting
                        }

                        break;
                    case RUNNING_PROXY_ERROR:
                        startupConsole.stopLogging();
                        payaraProcess.destroy();

                        throw new GlassFishLaunchException(
                            "BAD GATEWAY response code returned. Check your proxy settings. Killing startup process.",
                            payaraProcess);
                    case RUNNING_CREDENTIAL_PROBLEM:
                        startupConsole.stopLogging();
                        payaraProcess.destroy();
                        AdminCredentialsDialog.open(payaraServerBehaviour.getServer());

                        throw new GlassFishLaunchException("Wrong user name or password. Killing startup process.",
                            payaraProcess);
                    case RUNNING_DOMAIN_MATCHING:
                        startupConsole.stopLogging();
                        break check_server_status;
                    default:
                        break;
                }

                // Wait for notification when server state changes
                try {
                    checkMonitor(monitor);

                    // Limit waiting so we can check process exit code again
                    payaraServerBehaviour.wait(500);

                    if (!hasLogged && (startupConsole.hasLogged() || filelogConsole.hasLogged())) {
                        // Something has been logged meaning the JVM of the target
                        // process is activated. Could be JVM logging first
                        // like "waiting for connection", or the first log line of Payara starting
                        hasLogged = true;
                        checkMonitorAndProgress(monitor, WORK_STEP / 4);
                    }

                    if (!hasLoggedPayara && filelogConsole.hasLoggedPayara()) {

                        // A Payara logline has been written, meaning Payara is now starting up.
                        hasLoggedPayara = true;
                        checkMonitorAndProgress(monitor, WORK_STEP / 4);
                    }

                } catch (InterruptedException e) {
                    startupConsole.stopLogging();
                    payaraProcess.destroy();
                    throw e;
                }
            }
        }

        return process;
    }

    private ResultProcess startPayara(boolean earlyAttach) throws GlassFishLaunchException {
        try {
            // Process the arguments and call the CommandStartDAS command which will initiate
            // starting the Payara server
            return startServer(payaraServerBehaviour.getGlassFishServerDelegate(), args, mode, earlyAttach);
        } catch (GlassFishIdeException e) {
            throw new GlassFishLaunchException("Exception in startup library.", e);
        }
    }

    private IPayaraConsole startLogging(Process payaraProcess) {
        IPayaraConsole startupConsole = getStartupProcessConsole(payaraServerBehaviour.getGlassFishServerDelegate(), payaraProcess);

        startupConsole.startLogging(
                new FetchLogSimple(payaraProcess.getInputStream()),
                new FetchLogSimple(payaraProcess.getErrorStream()));

        return startupConsole;
    }

    private void checkMonitor(IProgressMonitor monitor) throws InterruptedException {
        if (monitor.isCanceled()) {
            throw new InterruptedException();
        }
    }

    private void checkMonitorAndProgress(IProgressMonitor monitor, int work) throws InterruptedException {
        checkMonitor(monitor);
        monitor.worked(work);
    }

}
