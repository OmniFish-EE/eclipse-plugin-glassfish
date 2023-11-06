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

import static org.glassfish.eclipse.tools.server.log.AbstractLogFilter.GlassfishLogFields.DATETIME;
import static org.glassfish.eclipse.tools.server.log.AbstractLogFilter.GlassfishLogFields.LEVEL;
import static org.glassfish.eclipse.tools.server.log.AbstractLogFilter.GlassfishLogFields.MESSAGE;

import java.util.Formatter;

import org.glassfish.eclipse.tools.server.log.AbstractLogFilter.GlassfishLogFields;
import org.glassfish.eclipse.tools.server.log.AbstractLogFilter.ILogFormatter;

public class LogFormatterSimple implements ILogFormatter {

    private GlassfishLogFields[] fields;
    private String format;
    private StringBuilder logRecordBuilder = new StringBuilder(1024);

    public LogFormatterSimple() {
        format = "%s|%s: %s";
        fields = new GlassfishLogFields[] { DATETIME, LEVEL, MESSAGE };
    }

    public LogFormatterSimple(String delimeter, GlassfishLogFields[] fields) {
        this.fields = fields;
    }

    @Override
    public String formatLogRecord(LogRecord record) {
        logRecordBuilder.setLength(0);
        Formatter logRecorFormatter = new Formatter(logRecordBuilder);
        logRecorFormatter.format(format, record.getRecordFieldValues(fields));
        logRecorFormatter.close();

        return logRecordBuilder.toString();
    }

}
