/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

/******************************************************************************
 * Copyright (c) 2018 Payara Foundation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package fish.payara.eclipse.tools.server.log;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledFuture;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

public abstract class AbstractPayaraConsole extends MessageConsole implements IPayaraConsole {

    protected List<LogReader> readers;
    protected MessageConsoleStream out;
    protected CountDownLatch latch;
    protected ILogFilter filter;
    protected ScheduledFuture<?> stopJobResult;

    public AbstractPayaraConsole(String name, ImageDescriptor imageDescriptor, ILogFilter filter) {
        super(name, imageDescriptor);
        this.filter = filter;
        this.out = newMessageStream();
    }

}
