/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

/******************************************************************************
 * Copyright (c) 2018-2023 XXXXX Foundation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.glassfish.eclipse.tools.server.facets.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.glassfish.eclipse.tools.server.facets.IGlassFishDeploymentDescriptor;

abstract class AbstractGlassFishDeploymentDescriptor implements
        IGlassFishDeploymentDescriptor {

    /**
     * Created new deployment descriptor if it's not already there.
     *
     */
    @Override
    public final void store(IProgressMonitor monitor) throws CoreException {
        if (isPossibleToCreate()) {
            prepareDescriptor();
            save();
        }
    }

    protected abstract void save();

    protected abstract void prepareDescriptor();

    protected abstract boolean isPossibleToCreate();

}
