/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

/******************************************************************************
 * Copyright (c) 2018 XXXXX Foundation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.glassfish.eclipse.tools.server.utils;

import static org.glassfish.eclipse.tools.server.utils.JdtUtil.validateJvm;

import org.eclipse.jdt.launching.IVMInstall;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class JdkFilter implements Filter<IVMInstall> {
    private final VersionConstraint versionConstraint;

    public JdkFilter(final VersionConstraint versionConstraint) {
        if (versionConstraint == null) {
            throw new IllegalArgumentException();
        }

        this.versionConstraint = versionConstraint;
    }

    @Override

    public boolean allows(final IVMInstall jvm) {
        return validateJvm(jvm).jdk().version(this.versionConstraint).result().ok();
    }

}
