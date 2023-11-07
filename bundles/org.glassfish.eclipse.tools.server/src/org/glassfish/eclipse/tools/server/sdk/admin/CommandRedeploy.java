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

package org.glassfish.eclipse.tools.server.sdk.admin;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * GlassFish Server Redeploy Command Entity.
 * <p>
 * Holds data for command. Objects of this class are created by API user.
 * <p>
 *
 * @author Tomas Kraus, Peter Benedikovic, Gaurav Gupta
 */
@RunnerHttpClass(runner = RunnerHttpRedeploy.class)
@RunnerRestClass(runner = RunnerRestDeploy.class)
public class CommandRedeploy extends CommandTargetName {

	////////////////////////////////////////////////////////////////////////////
	// Class attributes //
	////////////////////////////////////////////////////////////////////////////

	/** Command string for deploy command. */
	private static final String COMMAND = "redeploy";

	////////////////////////////////////////////////////////////////////////////
	// Instance attributes //
	////////////////////////////////////////////////////////////////////////////

	/** Deployed application context root. */
	final String contextRoot;

	/** Deployment properties. */
	final Map<String, String> properties;

	/** Deployment libraries. */
	final File[] libraries;

	/** Keep state. */
	final boolean keepState;

	/** Hot Deploy. */
	final boolean hotDeploy;

	/** Metadata Changed. */
	final boolean metadataChanged;

	/** Source files Changed. */
	final List<String> sourcesChanged;

	////////////////////////////////////////////////////////////////////////////
	// Constructors //
	////////////////////////////////////////////////////////////////////////////

	/**
	 * Constructs an instance of GlassFish server deploy command entity.
	 * <p/>
	 *
	 * @param name            Name of module/cluster/instance to modify.
	 * @param target          Target GlassFish instance or cluster where
	 *                        <code>name</code> is stored.
	 * @param contextRoot     Deployed application context root.
	 * @param properties      Deployment properties.
	 * @param libraries       Deployment libraries.
	 * @param keepState       Keep state.
	 * @param hotDeploy       Hot Deploy.
	 * @param metadataChanged Metadata Changed.
	 * @param sourcesChanged  Sources Changed.
	 */
	public CommandRedeploy(final String name, final String target, final String contextRoot,
			final Map<String, String> properties, final File[] libraries, final boolean keepState,
			final boolean hotDeploy, final boolean metadataChanged, final List<String> sourcesChanged) {
		super(COMMAND, name, target);
		this.contextRoot = contextRoot;
		this.properties = properties;
		this.libraries = libraries;
		this.keepState = keepState;
		this.hotDeploy = hotDeploy;
		this.metadataChanged = metadataChanged;
		this.sourcesChanged = sourcesChanged;
	}

}
