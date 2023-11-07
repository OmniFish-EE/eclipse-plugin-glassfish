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

package org.glassfish.eclipse.tools.server.log;

public interface ILogFilter {

    /**
     * Resets log filter after reading complete log record.
     */
    void reset();

    /**
     * Processes read line.
     *
     * @param line - mustn't contain new line character
     * @return Complete log record or null if the read line haven't completed the log record.
     */
    String process(String line);

    default boolean hasProcessedGlassFish() {
        return false;
    }
}
