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

package org.glassfish.eclipse.tools.server.ui.properties;

import static org.eclipse.wst.server.core.IServer.PUBLISH_CLEAN;
import static org.eclipse.wst.server.core.IServer.STATE_STOPPED;
import static org.glassfish.eclipse.tools.server.GlassFishServer.ATTR_ADMIN;
import static org.glassfish.eclipse.tools.server.GlassFishServer.ATTR_ADMINPASS;
import static org.glassfish.eclipse.tools.server.GlassFishServer.ATTR_ADMINPORT;
import static org.glassfish.eclipse.tools.server.GlassFishServer.ATTR_DEBUG_PORT;
import static org.glassfish.eclipse.tools.server.GlassFishServer.ATTR_DOMAINPATH;
import static org.glassfish.eclipse.tools.server.GlassFishServer.ATTR_RESTART_PATTERN;
import static org.glassfish.eclipse.tools.server.GlassFishServer.getDefaultDomainDir;
import static org.glassfish.eclipse.tools.server.utils.Jobs.scheduleShortJob;
import static org.glassfish.eclipse.tools.server.utils.WtpUtil.load;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.ui.internal.editor.GlobalCommandManager;
import org.glassfish.eclipse.tools.server.GlassFishServer;
import org.glassfish.eclipse.tools.server.Status;
import org.glassfish.eclipse.tools.server.Status.Severity;
import org.glassfish.eclipse.tools.server.deploying.GlassFishServerBehaviour;
import org.glassfish.eclipse.tools.server.ui.wizards.GlassfishWizardResources;

/**
 * Properties that are being shown for the GlassFish / GlassFish server when e.g. the server is right
 * clicked in the Servers view and "Properties" is chosen from the context menu.
 *
 */
public class ServerPropertyPage extends PropertyPage {

    private IServerWorkingCopy serverWorkingCopy;

	protected Text serverName;

	protected Text serverHost;

	protected Text domainLocation;

	protected Text adminName;

	protected Text adminPassword;

	protected Text restartPattern;

	protected Spinner debugPort;

	protected Button keepSessions;

	protected Button jarDeploy;

	protected Button hotDeploy;

	protected Button attachDebuggerEarly;

	private GlassFishServer glassfishServer;

	public static int MAXIMUM_PORT = 999999;

    @Override
    protected Control createContents(Composite parent) {

        IServer server = (IServer) getElement();
        if (server instanceof IServerWorkingCopy) {
            serverWorkingCopy = (IServerWorkingCopy) server;
        } else {
            serverWorkingCopy = server.createWorkingCopy();
        }

        glassfishServer = load(serverWorkingCopy, GlassFishServer.class);

        Control control = createContent(glassfishServer, parent);
        refreshStatus();

        return control;

    }

	public Composite createContent(GlassFishServer glassfishServer, Composite parent) {

		GridLayout layout = new GridLayout(1, true);
		parent.setLayout(layout);
		parent.setLayoutData(new GridData(GridData.FILL_BOTH));

		Composite group = new Composite(parent, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		group.setLayout(layout);
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label label = new Label(group, SWT.NONE);
		label.setText(GlassfishWizardResources.serverName);
		GridData data = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_END);
		data.horizontalSpan = 2;
		label.setLayoutData(data);
		serverName = new Text(group, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		serverName.setLayoutData(data);
		serverName.setText(glassfishServer.getName());

		label = new Label(group, SWT.NONE);
		label.setText(GlassfishWizardResources.serverHost);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_END);
		data.horizontalSpan = 2;
		label.setLayoutData(data);
		serverHost = new Text(group, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		serverHost.setLayoutData(data);
		serverHost.setText(glassfishServer.getHost());

		label = new Label(group, SWT.NONE);
		label.setText(GlassfishWizardResources.domainPath);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_END);
		data.horizontalSpan = 2;
		label.setLayoutData(data);

		domainLocation = new Text(group, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		domainLocation.setLayoutData(data);
		domainLocation.setText(glassfishServer.getDomainPath());

		Button browse = new Button(group, SWT.PUSH);
		browse.setText(GlassfishWizardResources.browse);
		browse.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent se) {
				DirectoryDialog dialog = new DirectoryDialog(parent.getShell());
				dialog.setMessage(GlassfishWizardResources.selectInstallDir);
				String selectedDirectory = dialog.open();
				if (selectedDirectory != null && !selectedDirectory.isEmpty())
					domainLocation.setText(selectedDirectory);
			}
		});

		Button createDomain = new Button(group, SWT.PUSH);
		createDomain.setText(GlassfishWizardResources.newDomainCreateButton);
		createDomain.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent se) {

//				GlassFishRuntime runtime = (GlassFishRuntime) getServerRuntime().loadAdapter(GlassFishRuntime.class, null);
//				CreateGlassFishDomain domain = new CreateGlassFishDomain(parent.getShell(), glassfishServer, runtime);
//				domain.open();
//				String selectedDirectory = domain.getPath();
//				if (selectedDirectory != null && !selectedDirectory.isEmpty()) {
					domainLocation.setText("");
//				}
			}
		});

		label = new Label(group, SWT.HORIZONTAL | SWT.SEPARATOR);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_END);
		data.horizontalSpan = 2;
		data.verticalIndent = 10;
		label.setLayoutData(data);

		label = new Label(group, SWT.NONE);
		label.setText(GlassfishWizardResources.adminName);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_END);
		data.horizontalSpan = 2;
		label.setLayoutData(data);

		adminName = new Text(group, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		adminName.setLayoutData(data);
		adminName.setText(glassfishServer.getAdminUser());

		label = new Label(group, SWT.NONE);
		label.setText(GlassfishWizardResources.adminPassword);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_END);
		data.horizontalSpan = 2;
		label.setLayoutData(data);

		adminPassword = new Text(group, SWT.BORDER | SWT.PASSWORD);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		adminPassword.setLayoutData(data);
		adminPassword.setText(glassfishServer.getAdminPassword());

		label = new Label(group, SWT.NONE);
		label.setText(GlassfishWizardResources.debugPort);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_END);
		data.horizontalSpan = 2;
		label.setLayoutData(data);

		debugPort = new Spinner(group, SWT.BORDER);
		debugPort.setMinimum(0);
		debugPort.setMaximum(MAXIMUM_PORT);
		debugPort.setTextLimit((Integer.toString(MAXIMUM_PORT)).length());
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		debugPort.setLayoutData(data);
		debugPort.setSelection(glassfishServer.getDebugPort());

		label = new Label(group, SWT.HORIZONTAL | SWT.SEPARATOR);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_END);
		data.horizontalSpan = 2;
		data.verticalIndent = 10;
		label.setLayoutData(data);

		keepSessions = new Button(group, SWT.CHECK);
		keepSessions.setText(GlassfishWizardResources.keepSessions);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		keepSessions.setLayoutData(data);
		keepSessions.setSelection(glassfishServer.getKeepSessions());

		jarDeploy = new Button(group, SWT.CHECK);
		jarDeploy.setText(GlassfishWizardResources.jarDeploy);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		jarDeploy.setLayoutData(data);
		jarDeploy.setSelection(glassfishServer.getJarDeploy());

		label = new Label(group, SWT.HORIZONTAL | SWT.SEPARATOR);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_END);
		data.horizontalSpan = 2;
		data.verticalIndent = 10;
		label.setLayoutData(data);

		label = new Label(group, SWT.NONE);
		label.setText(GlassfishWizardResources.restartPattern);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_END);
		data.horizontalSpan = 2;
		label.setLayoutData(data);

		restartPattern = new Text(group, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		restartPattern.setLayoutData(data);
		restartPattern.setText(glassfishServer.getRestartPattern());
		restartPattern.addModifyListener(e -> glassfishServer.setRestartPattern(restartPattern.getText()));

		hotDeploy = new Button(group, SWT.CHECK);
		hotDeploy.setText(GlassfishWizardResources.enableHotDeploy);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		hotDeploy.setLayoutData(data);
		hotDeploy.setSelection(glassfishServer.getHotDeploy());

		attachDebuggerEarly = new Button(group, SWT.CHECK);
		attachDebuggerEarly.setText(GlassfishWizardResources.attachDebugEarly);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		attachDebuggerEarly.setLayoutData(data);
		attachDebuggerEarly.setSelection(glassfishServer.getAttachDebuggerEarly());
		return group;
	}


    private void refreshStatus() {
        Status status = validation();

        if (status.severity() == Severity.ERROR) {
            setMessage(status.message(), ERROR);
            setValid(false);
        } else if (status.severity() == Severity.WARNING) {
            setMessage(status.message(), WARNING);
            setValid(true);
        } else {
            setMessage(null, NONE);
            setValid(true);
        }

    }
    
    private Status validation() {
        return Status.createOkStatus();
    }

    // note that this is currently not working due to issue 140
    // public void propertyChange(PropertyChangeEvent evt) {
    // if (AbstractGlassfishServer.DOMAINUPDATE == evt.getPropertyName()) {
    // username.setText(glassfishServer.getAdminUser());
    // password.setText(glassfishServer.getAdminPassword());
    // adminServerPortNumber.setText(Integer.toString(glassfishServer.getAdminPort()));
    // serverPortNumber.setText(Integer.toString(glassfishServer.getPort()));
    // }
    // }


    @Override
    public boolean performCancel() {
        return super.performCancel();
    }

    @Override
    protected void performApply() {
        try {
            IServer server = serverWorkingCopy.save(true, new NullProgressMonitor());
            GlobalCommandManager.getInstance().reload(server.getId());

            serverWorkingCopy.setName(serverName.getText());
            serverWorkingCopy.setHost(serverHost.getText());
			glassfishServer.setDomainPath(domainLocation.getText());
            glassfishServer.setAdminUser(adminName.getText());
			glassfishServer.setAdminPassword(adminPassword.getText());
			glassfishServer.setDebugPort(debugPort.getSelection());
            glassfishServer.setKeepSessions(keepSessions.getSelection());
			glassfishServer.setJarDeploy(jarDeploy.getSelection());
            glassfishServer.setRestartPattern(restartPattern.getText());
			glassfishServer.setHotDeploy(hotDeploy.getSelection());
			glassfishServer.setAttachDebuggerEarly(attachDebuggerEarly.getSelection());

            scheduleShortJob("Update GlassFish server state", monitor -> {

                GlassFishServerBehaviour serverBehavior = null;

                try {
                    serverBehavior = load(server, GlassFishServerBehaviour.class);
                    serverBehavior.updateServerStatus();
                    serverBehavior.setGlassFishServerPublishState(PUBLISH_CLEAN);
                } catch (Exception e) {
                    if (serverBehavior != null) {
                        serverBehavior.setGlassFishServerState(STATE_STOPPED);
                    }
                }
            });
        } catch (CoreException e) {
            // no-op
            e.printStackTrace();
        }
    }

    @Override
    public boolean performOk() {
        performApply();
        return true;
    }

    @Override
    protected void performDefaults() {
        super.performDefaults();

        serverWorkingCopy.setAttribute(ATTR_ADMIN, "");
        serverWorkingCopy.setAttribute(ATTR_ADMINPASS, "");
        serverWorkingCopy.setAttribute(ATTR_DOMAINPATH, getDefaultDomainDir(serverWorkingCopy.getRuntime().getLocation()).toString());
        serverWorkingCopy.setAttribute(ATTR_ADMINPORT, "");
        serverWorkingCopy.setAttribute(ATTR_DEBUG_PORT, "");
        serverWorkingCopy.setAttribute(ATTR_RESTART_PATTERN, "");

//        model.refresh();
    }

}
