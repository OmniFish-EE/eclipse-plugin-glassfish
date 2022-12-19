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

package fish.payara.eclipse.tools.server.sdk.server.config;

import java.util.HashMap;
import java.util.Map;

/**
 * JavaSE platforms supported by Glassfish.
 * <p/>
 *
 * @author Tomas Kraus, Peter Benedikovic
 */
public enum JavaSEPlatform {

    ////////////////////////////////////////////////////////////////////////////
    // Enum values //
    ////////////////////////////////////////////////////////////////////////////

    /** JavaSE 1.1. */
    v1_1,
    /** JavaSE 1.2. */
    v1_2,
    /** JavaSE 1.3. */
    v1_3,
    /** JavaSE 1.4. */
    v1_4,
    /** JavaSE 1.5. */
    v1_5,
    /** JavaSE 1.6. */
    v1_6,
    /** JavaEE 1.7. */
    v1_7,
    /** JavaEE 1.8. */
    v1_8;

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** GlassFish JavaEE platform enumeration length. */
    public static final int length = JavaSEPlatform.values().length;

    /** JavaEE platform version elements separator character. */
    public static final char SEPARATOR = '.';

    /** A <code>String</code> representation of v1_1 value. */
    static final String V1_1_STR = "1.1";

    /** A <code>String</code> representation of v1_2 value. */
    static final String V1_2_STR = "1.2";

    /** A <code>String</code> representation of v1_3 value. */
    static final String V1_3_STR = "1.3";

    /** A <code>String</code> representation of v1_4 value. */
    static final String V1_4_STR = "1.4";

    /** A <code>String</code> representation of v1_5 value. */
    static final String V1_5_STR = "1.5";

    /** A <code>String</code> representation of v1_6 value. */
    static final String V1_6_STR = "1.6";

    /** A <code>String</code> representation of v1_7 value. */
    static final String V1_7_STR = "1.7";

    /** A <code>String</code> representation of v1_8 value. */
    static final String V1_8_STR = "1.8";

    /**
     * Stored <code>String</code> values for backward <code>String</code> conversion.
     */
    private static final Map<String, JavaSEPlatform> stringValuesMap = new HashMap<>(values().length);

    // Initialize backward String conversion Map.
    static {
        for (JavaSEPlatform platform : JavaSEPlatform.values()) {
            stringValuesMap.put(platform.toString().toUpperCase(), platform);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Static methods //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Returns a <code>JavaSEPlatform</code> with a value represented by the specified
     * <code>String</code>. The <code>JavaSEPlatform</code> returned represents existing value only if
     * specified <code>String</code> matches any <code>String</code> returned by <code>toString</code>
     * method. Otherwise <code>null</code> value is returned.
     * <p>
     *
     * @param platformName Value containing <code>JavaSEPlatform</code> <code>toString</code>
     * representation.
     * @return <code>JavaSEPlatform</code> value represented by <code>String</code> or <code>null</code>
     * if value was not recognized.
     */
    public static JavaSEPlatform toValue(final String platformName) {
        if (platformName != null) {
            return (stringValuesMap.get(platformName.toUpperCase()));
        } else {
            return null;
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Convert JavaEE platform version value to <code>String</code>.
     * <p/>
     *
     * @return A <code>String</code> representation of the value of this object.
     */
    @Override
    public String toString() {
        switch (this) {
        case v1_1:
            return V1_1_STR;
        case v1_2:
            return V1_2_STR;
        case v1_3:
            return V1_3_STR;
        case v1_4:
            return V1_4_STR;
        case v1_5:
            return V1_5_STR;
        case v1_6:
            return V1_6_STR;
        case v1_7:
            return V1_7_STR;
        case v1_8:
            return V1_8_STR;
        // This is unrecheable. Being here means this class does not handle
        // all possible values correctly.
        default:
            throw new ServerConfigException(
                    ServerConfigException.INVALID_SE_PLATFORM_VERSION);
        }
    }
}
