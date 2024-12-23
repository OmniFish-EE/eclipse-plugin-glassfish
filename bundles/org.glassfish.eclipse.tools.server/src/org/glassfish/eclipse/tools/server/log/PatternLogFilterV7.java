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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.glassfish.eclipse.tools.server.GlassFishServerPlugin;

public class PatternLogFilterV7 extends AbstractLogFilter {

    private static final Pattern logPattern = Pattern.compile("^\\[([^\\]]*)\\]\\s+" + // date-time
            "\\[([^\\]]*)\\]\\s+" + // server name
            "\\[([^\\]]*)\\]\\s+" + // level name
            "\\[([^\\]]*)\\]\\s+" + // version
            "\\[([^\\]]*)\\]\\s+" + // class info
            "\\[([^\\]]*)\\]\\s+" + // thread info
            "\\[([^\\]]*)\\]\\s+" + // level value
            "\\[{2}\\s*(.+)\\]{2}\\s$", Pattern.DOTALL); // log message

    private static final Pattern endOfMessagePattern = Pattern.compile("^//s*[^//]]{2}\\s$"); // log message

    private boolean hasProcessedGlassFish;

    @Override
    public boolean hasProcessedGlassFish() {
        return hasProcessedGlassFish;
    }

    PatternLogFilterV7() {
        super();
    }

    @Override
    public String process(String line) {
        String result = null;
        if (line.equals(logRecordDelimeter)) {
            Matcher m = logPattern.matcher(buffer);
            if (m.matches()) {
                record.setTime(m.group(1));
                record.setLevel(levelResolver.resolve(m.group(3)));
                record.setVersion(m.group(2));
                record.setClassInfo(m.group(5));
                record.setThreadInfo(m.group(6));
                record.setMessage(m.group(8));
                result = formatter.formatLogRecord(record);
                reset();
                hasProcessedGlassFish = true;
            } else if (!isReadingUserMessage()) {
                GlassFishServerPlugin.logMessage("Log record that does not match expected format detected!");
                GlassFishServerPlugin.logMessage(buffer.toString());
            }
        } else {
            buffer.append(line);
            buffer.append('\n');
        }
        return result;
    }

    @Override
    protected boolean isReadingUserMessage() {
        return !endOfMessagePattern.matcher(buffer).matches();
    }

}
