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

package fish.payara.eclipse.tools.server.ui.resources;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * For converting NetBeans DB Urls to their component parts or back. NOTE: This is a copy of the
 * file used in the NB V3 plugin, though it's in a different package there
 * (org.netbeans.modules.glassfish.eecommon.api).
 *
 * @author Peter Williams
 */
public class UrlData {

    // Replaced Unicode safe character classes with ASCII equivalents to
    // avoid JDK bug 5088563, for which a fix is not available on Mac as of
    // this writing - !PW 7/31/08
    private static String DBURL_PATTERN = "((?:[^:\\n]+:){2,3})" + // prefix (mandatory)
            "(?://|@|([^ \\t]+)@)" + // alternate db name (optional)
            "([^:;/\\\\ \\t]+)" + // hostname (mandatory)
            "(?:\\\\([^ \\t:/;]+)|)" + // instance name (optional)
            "(?::([0-9]+)|)" + // port (optional)
            "(?:(?:/|:)([^:/;?&]+)|)((?:(?:;|\\?|&|:)" + // database (optional)
            "(?:[^;&]+)|)+)"; // delimited properties (optional)

    // Indices for regular expression match capture blocks
    private static final int DB_PREFIX = 0;
    private static final int DB_ALT_DBNAME = 1;
    private static final int DB_HOST = 2;
    private static final int DB_INSTANCE_NAME = 3;
    private static final int DB_PORT = 4;
    private static final int DB_PRIMARY_DBNAME = 5;
    private static final int DB_PROPERTIES = 6;
    private static final int NUM_PARTS = 7;

    private static Pattern urlPattern = Pattern.compile(DBURL_PATTERN);

    private final String url;
    private final String[] parts = new String[NUM_PARTS];
    private final Map<String, String> props = new LinkedHashMap<>();

    public UrlData(String newUrl) {
        url = newUrl;
        parseUrl();
    }

    public UrlData(final String prefix, final String host, final String port,
            final String dbname, String sid) {
        this(prefix, host, port, dbname, null, null, parseProperties(sid));
    }

    public UrlData(final String prefix, final String host, final String port,
            final String dbname, Map<String, String> properties) {
        this(prefix, host, port, dbname, null, null, properties);
    }

    public UrlData(final String prefix, final String host, final String port,
            final String dbname, final String altdbname, final String instancename,
            Map<String, String> properties) {

        parts[DB_PREFIX] = prefix;
        parts[DB_ALT_DBNAME] = altdbname;
        parts[DB_HOST] = host;
        parts[DB_INSTANCE_NAME] = instancename;
        parts[DB_PORT] = port;
        parts[DB_PRIMARY_DBNAME] = dbname;
        parts[DB_PROPERTIES] = null; // reconstruct?
        props.putAll(properties);
        url = constructUrl();
    }

    private void parseUrl() {
        Logger.getLogger("glassfish.javaee").log(Level.FINEST, "Parsing DB Url: " + url);
        Matcher matcher = urlPattern.matcher(url);
        if (matcher.matches()) {
            for (int i = 1; i <= matcher.groupCount(); i++) {
                String part = matcher.group(i);
                Logger.getLogger("glassfish.javaee").log(Level.FINEST, "    Part " + i + " is " + part);
                parts[i - 1] = part;
            }
            props.putAll(parseProperties(parts[DB_PROPERTIES]));
        } else {
            Logger.getLogger("glassfish.javaee").log(Level.FINE, "Url parsing failed for " + url);
        }
    }

    private static Map<String, String> parseProperties(final String data) {
        Map<String, String> result = new LinkedHashMap<>();
        if (data != null && data.length() > 0) {
            String[] properties = data.split("[;?&:]");
            for (String propertie : properties) {
                if (propertie != null && propertie.length() > 0) {
                    int eqIndex = propertie.indexOf("=");
                    if (eqIndex != -1) {
                        result.put(propertie.substring(0, eqIndex), propertie.substring(eqIndex + 1));
                    } else {
                        // not sure if we should keep this. Possibly invalid property
                        result.put(propertie, "");
                    }
                }
            }
        }
        return result;
    }

    /**
     * package protected for testing purposes only.
     */
    String constructUrl() {
        StringBuilder builder = new StringBuilder(256);
        builder.append(parts[DB_PREFIX]);

        if (notEmpty(parts[DB_ALT_DBNAME])) {
            builder.append(parts[DB_ALT_DBNAME]);
            builder.append('@');
        } else if ("jdbc:oracle:thin:".equals(parts[DB_PREFIX])) {
            builder.append('@');
        } else {
            // most formats
            builder.append("//"); // NOI18N
        }

        builder.append(parts[DB_HOST]);

        if (notEmpty(parts[DB_INSTANCE_NAME])) {
            builder.append('\\');
            builder.append(parts[DB_INSTANCE_NAME]);
        }

        if (notEmpty(parts[DB_PORT])) {
            builder.append(':'); // NOI18N
            builder.append(parts[DB_PORT]);
        }

        if (notEmpty(parts[DB_PRIMARY_DBNAME])) {
            if ("jdbc:oracle:thin:".equals(parts[DB_PREFIX])) {
                builder.append(':'); // NOI18N
            } else {
                builder.append('/'); // NOI18N
            }
            builder.append(parts[DB_PRIMARY_DBNAME]);
        }

        char propertyInitialSeparator = ';';
        char propertySeparator = ';';
        if ("jdbc:mysql:".equals(parts[DB_PREFIX])) {
            propertyInitialSeparator = '?';
            propertySeparator = '&';
        } else if ("jdbc:informix-sqli:".equals(parts[DB_PREFIX])) {
            propertyInitialSeparator = ':';
        }

        Set<Map.Entry<String, String>> entries = props.entrySet();
        Iterator<Map.Entry<String, String>> entryIterator = entries.iterator();
        if (entryIterator.hasNext()) {
            builder.append(propertyInitialSeparator);
            Map.Entry<String, String> entry = entryIterator.next();
            builder.append(entry.getKey());
            String value = entry.getValue();
            if (notEmpty(value)) {
                builder.append('=');
                builder.append(value);
            }
        }

        while (entryIterator.hasNext()) {
            builder.append(propertySeparator);
            Map.Entry<String, String> entry = entryIterator.next();
            builder.append(entry.getKey());
            String value = entry.getValue();
            if (notEmpty(value)) {
                builder.append('=');
                builder.append(value);
            }
        }

        return builder.toString();
    }

    public String getUrl() {
        return url;
    }

    public String getPrefix() {
        return parts[DB_PREFIX];
    }

    public String getHostName() {
        return parts[DB_HOST];
    }

    public String getPort() {
        return parts[DB_PORT];
    }

    public String getInstanceName() {
        return parts[DB_INSTANCE_NAME];
    }

    public String getDatabaseName() {
        String dbname = parts[DB_PRIMARY_DBNAME];
        if (dbname == null) {
            dbname = parts[DB_ALT_DBNAME];
            if (dbname == null) {
                dbname = props.get("databaseName");
                if (dbname == null) {
                    dbname = props.get("databasename");
                    if (dbname == null) {
                        dbname = props.get("SID");
                        if (dbname == null) {
                            dbname = props.get("database name");
                        }
                    }
                }
            }
        }
        return dbname;
    }

    public String getAlternateDBName() {
        return parts[DB_ALT_DBNAME];
    }

    public String getSid() {
        return props.get("SID");
    }

    public Map<String, String> getProperties() {
        return Collections.unmodifiableMap(props);
    }

    private static boolean notEmpty(String s) {
        return s != null && s.length() > 0;
    }

}
