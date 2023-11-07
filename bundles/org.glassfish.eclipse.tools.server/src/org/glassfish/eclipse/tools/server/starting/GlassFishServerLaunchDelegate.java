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

package org.glassfish.eclipse.tools.server.starting;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.eclipse.core.runtime.IStatus.ERROR;
import static org.eclipse.core.runtime.IStatus.OK;
import static org.eclipse.debug.core.DebugPlugin.ATTR_CAPTURE_OUTPUT;
import static org.eclipse.debug.core.ILaunchManager.DEBUG_MODE;
import static org.eclipse.debug.core.ILaunchManager.RUN_MODE;
import static org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants.ERR_INTERNAL_ERROR;
import static org.eclipse.jface.dialogs.MessageDialog.openError;
import static org.eclipse.wst.server.core.IServer.STATE_STARTED;
import static org.eclipse.wst.server.core.IServer.STATE_STARTING;
import static org.eclipse.wst.server.core.IServer.STATE_STOPPED;
import static org.eclipse.wst.server.core.ServerUtil.getServer;
import static org.glassfish.eclipse.tools.server.Messages.abortLaunchMsg;
import static org.glassfish.eclipse.tools.server.Messages.badGateway;
import static org.glassfish.eclipse.tools.server.Messages.canntCommunicate;
import static org.glassfish.eclipse.tools.server.Messages.checkVpnOrProxy;
import static org.glassfish.eclipse.tools.server.Messages.domainNotMatch;
import static org.glassfish.eclipse.tools.server.Messages.wrongUsernamePassword;
import static org.glassfish.eclipse.tools.server.GlassFishServerPlugin.SYMBOLIC_NAME;
import static org.glassfish.eclipse.tools.server.GlassFishServerPlugin.logError;
import static org.glassfish.eclipse.tools.server.GlassFishServerPlugin.logMessage;
import static org.glassfish.eclipse.tools.server.log.GlassFishConsoleManager.getStandardConsole;
import static org.glassfish.eclipse.tools.server.log.GlassFishConsoleManager.showConsole;
import static org.glassfish.eclipse.tools.server.sdk.server.ServerTasks.getDebugPort;
import static org.glassfish.eclipse.tools.server.sdk.server.ServerTasks.StartMode.DEBUG;
import static org.glassfish.eclipse.tools.server.sdk.server.ServerTasks.StartMode.START;
import static org.glassfish.eclipse.tools.server.sdk.utils.ServerUtils.GFV3_JAR_MATCHER;
import static org.glassfish.eclipse.tools.server.sdk.utils.ServerUtils.getJarName;
import static org.glassfish.eclipse.tools.server.sdk.utils.Utils.quote;
import static org.glassfish.eclipse.tools.server.utils.WtpUtil.load;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.RuntimeProcess;
import org.eclipse.jdt.launching.AbstractJavaLaunchConfigurationDelegate;
import org.eclipse.jdt.launching.AbstractVMInstall;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.model.ServerDelegate;
import org.glassfish.eclipse.tools.server.GlassFishServer;
import org.glassfish.eclipse.tools.server.deploying.GlassFishServerBehaviour;
import org.glassfish.eclipse.tools.server.exceptions.HttpPortUpdateException;
import org.glassfish.eclipse.tools.server.log.IGlassFishConsole;
import org.glassfish.eclipse.tools.server.sdk.admin.ResultProcess;
import org.glassfish.eclipse.tools.server.sdk.server.FetchLogPiped;
import org.glassfish.eclipse.tools.server.sdk.server.ServerTasks.StartMode;

/**
 * This class takes care of actually starting (launching) the GlassFish server.
 *
 * <p>
 * This class is registered in <code>plug-in.xml</code> in the
 * <code>org.eclipse.debug.core.launchConfigurationTypes</code> extension point.
 * </p>
 *
 */
public class GlassFishServerLaunchDelegate extends AbstractJavaLaunchConfigurationDelegate {

    private static final int MONITOR_TOTAL_WORK = 1000;
    public static final int WORK_STEP = 200;
    private static final IStatus DEBUG_STATUS = new Status(OK, SYMBOLIC_NAME, "Debugging");

    private static final ExecutorService asyncJobsService = Executors.newCachedThreadPool();

    @Override
    public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {

        logMessage("in GlassFish launch");

        monitor.beginTask("Starting GlassFish", MONITOR_TOTAL_WORK);

        IServer server = getServer(configuration);
        if (server == null) {
            abort("missing Server");
        }

        GlassFishServerBehaviour serverBehavior = load(server, GlassFishServerBehaviour.class);
        GlassFishServer serverAdapter = load(server, GlassFishServer.class);

        serverBehavior.setLaunch(launch);

        try {
            checkMonitorAndProgress(monitor, WORK_STEP);
        } catch (InterruptedException e1) {
            return;
        }

        // Find out if our server is already running and ready
        boolean isRunning = isRunning(serverBehavior);

        // If server is running and the mode is debug, try to attach the debugger
        if (isRunning) {

            logMessage("Server is already started!");

            if (DEBUG_MODE.equals(mode)) {
                try {
                    serverBehavior.attach(launch, configuration.getWorkingCopy(), monitor);
                } catch (CoreException e) {
                    Display.getDefault().asyncExec(() -> openError(
                            Display.getDefault().getActiveShell(),
                            "Error",
                            "Error attaching to GlassFish Server. Please make sure the server is started in debug mode."));

                    logError("Not able to attach debugger, running in normal mode", e);

                    serverBehavior.setGlassFishServerMode(RUN_MODE);

                    throw e;
                }

                serverBehavior.setGlassFishServerStatus(DEBUG_STATUS);
            }
        }

        try {
            if (serverAdapter.isRemote()) {
                if (!isRunning) {
                    abort("GlassFish Remote Servers cannot be start from this machine.");
                }
            } else {
                if (!isRunning) {
                    startDASAndTarget(serverAdapter, serverBehavior, configuration, launch, mode, monitor);
                }
            }

        } catch (InterruptedException e) {
            getStandardConsole(serverAdapter).stopLogging(3);
            logError("Server start interrupted.", e);

            serverBehavior.setGlassFishServerState(STATE_STOPPED);
            abort("Unable to start server due interruption.");
        } catch (CoreException e) {
            getStandardConsole(serverAdapter).stopLogging(3);
            serverBehavior.setGlassFishServerState(STATE_STOPPED);
            throw e;
        } finally {
            monitor.done();
        }

        serverBehavior.setGlassFishServerMode(mode);
    }

    public ResultProcess launchServer(GlassFishServerBehaviour serverBehavior, StartupArgsImpl glassfishStartArguments, StartMode launchMode, IProgressMonitor monitor, ILaunchConfiguration configuration, ILaunch launch) throws TimeoutException, InterruptedException, ExecutionException, HttpPortUpdateException {
        serverBehavior.setGlassFishServerState(STATE_STARTING);

        ResultProcess process = waitForGlassFishStarted(
            serverBehavior,
            asyncJobsService.submit(new GlassFishStartJob(
                serverBehavior,
                glassfishStartArguments, launchMode,
                configuration, launch, monitor
                )),
            monitor);

        serverBehavior.updateHttpPort();

        return process;
    }

    private ResultProcess waitForGlassFishStarted(GlassFishServerBehaviour serverBehavior, Future<ResultProcess> futureProcess, IProgressMonitor monitor) throws TimeoutException, InterruptedException, ExecutionException {
        long endTime = System.currentTimeMillis() + (serverBehavior.getServer().getStartTimeout() * 1000);

        while (System.currentTimeMillis() < endTime) {

            try {
                return futureProcess.get(500, MILLISECONDS);
            } catch (TimeoutException e) {
                if (monitor.isCanceled()) {
                    futureProcess.cancel(true);
                    // TODO: check if GlassFish indeed stopped and if not explicitly give stop command
                    serverBehavior.serverStateChanged(STATE_STOPPED);
                    serverBehavior.setGlassFishServerState(STATE_STOPPED);
                    throw new OperationCanceledException();
                }
            }
        }

        throw new TimeoutException("Timeout while waiting for GlassFish to start");
    }

    @Override
    protected void abort(String message, Throwable exception, int code) throws CoreException {
        throw new CoreException(new Status(ERROR, SYMBOLIC_NAME, code, message, exception));
    }

    // #### Private methods

    private void startDASAndTarget(GlassFishServer serverAdapter, GlassFishServerBehaviour serverBehavior,
            ILaunchConfiguration configuration, ILaunch launch, String mode, IProgressMonitor monitor)
            throws CoreException, InterruptedException {

        File bootstrapJar = getJarName(serverAdapter.getServerInstallationDirectory(), GFV3_JAR_MATCHER);
        if (bootstrapJar == null) {
            abort("bootstrap jar not found");
        }

        // TODO which java to use? for now ignore the one from launch config
        AbstractVMInstall/* IVMInstall */ vm = (AbstractVMInstall) serverBehavior.getRuntimeDelegate().getVMInstall();

        if (vm == null || vm.getInstallLocation() == null) {
            abort("Invalid Java VM location for server " + serverAdapter.getName());
        }

        StartupArgsImpl startArgs = new StartupArgsImpl();
        startArgs.setJavaHome(vm.getInstallLocation().getAbsolutePath());

        // Program & VM args
        String programArgs = getProgramArguments(configuration);
        String vmArgs = getVMArguments(configuration);

        StartMode startMode = DEBUG_MODE.equals(mode) ? DEBUG : START;
        addJavaOptions(serverAdapter, mode, startArgs, vmArgs);
        startArgs.addGlassfishArgs(programArgs);
        startArgs.addGlassfishArgs("--domain " + serverAdapter.getDomainName());
        startArgs.addGlassfishArgs("--domaindir " + quote(serverAdapter.getDomainPath()));

        setDefaultSourceLocator(launch, configuration);

        checkMonitorAndProgress(monitor, WORK_STEP / 2);

        startLogging(serverAdapter, serverBehavior);

        ResultProcess process = null;
        Process glassfishProcess = null;

        try {
            process = launchServer(serverBehavior, startArgs, startMode, monitor, configuration, launch);
            glassfishProcess = process.getValue().getProcess();
            launch.setAttribute(ATTR_CAPTURE_OUTPUT, "false");

            new RuntimeProcess(launch, glassfishProcess, "GlassFish Application Server", null);
        } catch (TimeoutException e) {
            abort("Unable to start server on time.", e);
        } catch (ExecutionException e) {
            abort("Unable to start server due following issues:", e.getCause());
        } catch (HttpPortUpdateException e) {
            abort("Unable to update http port. Server shut down.", e);
        }

        try {
            checkMonitorAndProgress(monitor, WORK_STEP);
        } catch (InterruptedException e) {
            killProcesses(glassfishProcess);
        }

        setDefaultSourceLocator(launch, configuration);

        if (DEBUG_MODE.equals(mode) && !serverBehavior.getGlassFishServerDelegate().getAttachDebuggerEarly()) {
            try {
                serverBehavior.attach(launch, configuration.getWorkingCopy(), monitor, getDebugPort(process));
                checkMonitorAndProgress(monitor, WORK_STEP);
            } catch (IllegalArgumentException e) {
                killProcesses(glassfishProcess);
                abort("Server run in debug mode but the debug port couldn't be determined!", e);
            }
        }
    }

    private void addJavaOptions(GlassFishServer serverAdapter, String mode, StartupArgsImpl args, String vmArgs) {
        if (DEBUG_MODE.equals(mode)) {
            args.addJavaArgs(vmArgs);
            int debugPort = serverAdapter.getDebugPort();
            if (debugPort != -1) {
                // Debug port was specified by user, use it
                args.addJavaArgs(serverAdapter.getDebugOptions(debugPort));
            }
        } else {
            args.addJavaArgs(ignoreDebugArgs(vmArgs));
        }
    }

    private String ignoreDebugArgs(String vmArgs) {
        StringBuilder args = new StringBuilder(vmArgs.length());

        for (String vmArgument : vmArgs.split("\\s")) {
            if ("-Xdebug".equalsIgnoreCase(vmArgument) || (vmArgument.startsWith("-agentlib")) || vmArgument.startsWith("-Xrunjdwp")) {
                break;
            }
            args.append(vmArgument);
            args.append(" ");
        }

        return args.toString();
    }

    private void checkMonitorAndProgress(IProgressMonitor monitor, int work) throws InterruptedException {
        if (monitor.isCanceled()) {
            throw new InterruptedException();
        }

        monitor.worked(work);
    }

    private boolean isRunning(GlassFishServerBehaviour serverBehavior) throws CoreException {
        IServer thisServer = serverBehavior.getServer();

        for (IServer server : ServerCore.getServers()) {

            if (server != thisServer && server.getServerState() == STATE_STARTED) {
                ServerDelegate delegate = load(server, ServerDelegate.class);
                if (delegate instanceof GlassFishServer) {
                    GlassFishServer runingGfServer = (GlassFishServer) delegate;

                    if (runingGfServer.isRemote()) {
                        continue;
                    }

                    GlassFishServer thisGfServer = (GlassFishServer) (load(thisServer, ServerDelegate.class));
                    if (runingGfServer.getPort() == thisGfServer.getPort()
                            || runingGfServer.getAdminPort() == thisGfServer.getAdminPort()) {
                        abort(canntCommunicate, new RuntimeException(domainNotMatch));
                        return false;
                    }
                }
            }
        }

        switch (serverBehavior.getServerStatus(true)) {
            case RUNNING_CONNECTION_ERROR:
                abort(canntCommunicate, new RuntimeException(abortLaunchMsg + domainNotMatch + checkVpnOrProxy));
                break;
            case RUNNING_CREDENTIAL_PROBLEM:
                AdminCredentialsDialog.open(thisServer);
                abort(canntCommunicate, new RuntimeException(abortLaunchMsg + wrongUsernamePassword));
                break;
            case RUNNING_DOMAIN_MATCHING:
                return true;
            case RUNNING_PROXY_ERROR:
                abort(canntCommunicate, new RuntimeException(abortLaunchMsg + badGateway));
                break;
            case STOPPED_DOMAIN_NOT_MATCHING:
                abort(canntCommunicate, new RuntimeException(domainNotMatch));
                break;
            case STOPPED_NOT_LISTENING:
                return false;
            default:
                break;
        }

        return false;
    }

    private void startLogging(GlassFishServer serverAdapter, GlassFishServerBehaviour serverBehavior) {
        try {
            PlatformUI.getWorkbench().getDisplay().asyncExec(() -> {
                File logFile = new File(serverAdapter.getDomainPath() + "/logs/server.log"); //$NON-NLS-1$
                try {
                    logFile.createNewFile();
                } catch (Exception e) {
                    // File probably exists
                    e.printStackTrace();
                }

                IGlassFishConsole console = getStandardConsole(serverAdapter);
                showConsole(console);
                if (!console.isLogging()) {
                    console.startLogging(FetchLogPiped.create(serverAdapter, true));
                }
            });
        } catch (Exception e) {
            logError("page.showView", e);
        }
    }



    private void killProcesses(Process... processes) {
        for (Process process : processes) {
            if (process != null) {
                process.destroy();
            }
        }
    }

    private void abort(String message) throws CoreException {
        throw new CoreException(new Status(ERROR, SYMBOLIC_NAME, ERR_INTERNAL_ERROR, message, null));
    }

    private void abort(String message, Throwable exception) throws CoreException {
        throw new CoreException(new Status(ERROR, SYMBOLIC_NAME, ERR_INTERNAL_ERROR, message, exception));
    }

}
