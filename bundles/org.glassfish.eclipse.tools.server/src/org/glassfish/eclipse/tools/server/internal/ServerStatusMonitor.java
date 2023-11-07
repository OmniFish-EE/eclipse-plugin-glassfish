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

package org.glassfish.eclipse.tools.server.internal;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.glassfish.eclipse.tools.server.ServerStatus.NOT_DEFINED;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import org.glassfish.eclipse.tools.server.GlassFishServer;
import org.glassfish.eclipse.tools.server.ServerStatus;
import org.glassfish.eclipse.tools.server.utils.ServerStatusHelper;

public class ServerStatusMonitor implements Runnable {

    private static final int DEFAULT_DELAY_IN_SEC = 5;

    private ScheduledExecutorService scheduler;
    private GlassFishServer server;
    private int delay;
    private ScheduledFuture<?> scheduledTask;

    private volatile ServerStatus status = NOT_DEFINED;
    private CopyOnWriteArrayList<ServerStateListener> listeners;

    private ServerStatusMonitor(GlassFishServer server) {
        this(server, DEFAULT_DELAY_IN_SEC);
    }

    private ServerStatusMonitor(GlassFishServer server, ServerStateListener... listeners) {
        this(server, DEFAULT_DELAY_IN_SEC, listeners);
    }

    private ServerStatusMonitor(GlassFishServer server, int checkInterval, ServerStateListener... listeners) {
        this.server = server;
        this.delay = checkInterval;
        this.listeners = new CopyOnWriteArrayList<>(listeners);
    }

    public static ServerStatusMonitor getInstance(GlassFishServer server) {
        return new ServerStatusMonitor(server);
    }

    public static ServerStatusMonitor getInstance(GlassFishServer server, ServerStateListener... listeners) {
        return new ServerStatusMonitor(server, listeners);
    }

    public static ServerStatusMonitor getInstance(GlassFishServer server, int checkInterval) {
        return new ServerStatusMonitor(server, checkInterval);
    }

    public void start() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduledTask = scheduler.scheduleWithFixedDelay(this, 0, delay, SECONDS);
    }

    public void stop() {
        scheduledTask.cancel(true);
        scheduler.shutdown();
    }

    @Override
    public void run() {
        status = ServerStatusHelper.checkServerStatus(server);
        notifyListeners(status);
    }

    public ServerStatus getServerStatus() {
        return getServerStatus(false);
    }

    public ServerStatus getServerStatus(boolean forceUpdate) {
        if (forceUpdate) {
            try {
                scheduler.submit(this).get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
            }
        }

        return status;
    }

    public void registerServerStatusListener(ServerStateListener listener) {
        listeners.add(listener);
    }

    private void notifyListeners(ServerStatus newStatus) {
        for (ServerStateListener listener : listeners) {
            listener.serverStatusChanged(newStatus);
        }
    }



}
