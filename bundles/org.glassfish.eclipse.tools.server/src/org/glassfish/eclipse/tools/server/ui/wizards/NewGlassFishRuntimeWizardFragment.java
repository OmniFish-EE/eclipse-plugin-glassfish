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

package org.glassfish.eclipse.tools.server.ui.wizards;

import static org.eclipse.wst.server.core.TaskModel.TASK_RUNTIME;
import static org.glassfish.eclipse.tools.server.Messages.duplicateRuntimeName;
import static org.glassfish.eclipse.tools.server.ui.wizards.GlassfishWizardResources.wzdRuntimeDescription;
import static org.glassfish.eclipse.tools.server.utils.NamingUtils.createUniqueRuntimeName;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.launching.AbstractVMInstall;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wst.server.core.IRuntimeWorkingCopy;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.TaskModel;
import org.eclipse.wst.server.core.internal.RuntimeWorkingCopy;
import org.eclipse.wst.server.ui.wizard.IWizardHandle;
import org.eclipse.wst.server.ui.wizard.WizardFragment;
import org.glassfish.eclipse.tools.server.GlassFishRuntime;
import org.glassfish.eclipse.tools.server.GlassFishServer;
import org.glassfish.eclipse.tools.server.GlassFishServerPlugin;
import org.glassfish.eclipse.tools.server.exceptions.UniqueNameNotFound;
import org.glassfish.eclipse.tools.server.utils.JdkFilter;

import static org.eclipse.osgi.util.NLS.bind;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import static org.eclipse.wst.server.core.ServerCore.getRuntimes;
import static org.eclipse.wst.server.core.ServerCore.getServers;

/**
 * This wizard fragment plugs-in the wizard flow when
 * <code>Servers -> New Server -> GlassFish -> GlassFish</code> is selected and
 * subsequently the <code>next</code> button is pressed when no runtime exists
 * yet, or the <code>add</code> button next to
 * <code>Server runtime environment</code> is pressed.
 *
 * <p>
 * This fragment essentially causes the screen with <code>Name</code>,
 * <code>GlassFish location</code>, <code>Java Location</code> etc to be rendered.
 *
 */
@SuppressWarnings("restriction")
public class NewGlassFishRuntimeWizardFragment extends WizardFragment {

	private Text serverName;

	private Text serverLocation;

	private Combo jrecombo;

	private List<IVMInstall> installedJREs;

	private String[] jreNames;

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.wst.server.ui.wizard.WizardFragment#hasComposite()
	 */
	@Override
	public boolean hasComposite() {
		return true;
	}

	@Override
	public Composite createComposite(Composite parent, IWizardHandle handle) {
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout grid = new GridLayout(2, false);
		grid.marginWidth = 0;
		container.setLayout(grid);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));

		handle.setImageDescriptor(GlassFishServerPlugin.getImageDescriptor(GlassFishServerPlugin.GF_WIZARD));
		handle.setTitle(getTitle());
		handle.setDescription(getDescription());
		createContent(container, handle);
		return container;
	}

	public void createContent(Composite parent, IWizardHandle handle) {
		GlassFishRuntime glassfishRuntime = (GlassFishRuntime) getServerRuntime().loadAdapter(GlassFishRuntime.class, null);

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
		serverName.setText(getServerRuntime().getName());
		serverName.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				getServerRuntime().setName(serverName.getText().trim());
				validate(handle);
			}
		});
		
		label = new Label(group, SWT.NONE);
		label.setText(GlassfishWizardResources.glassfishLocation);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_END);
		data.horizontalSpan = 2;
		label.setLayoutData(data);

		serverLocation = new Text(group, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		serverLocation.setLayoutData(data);
		if(getServerRuntime().getLocation() != null) {
			serverLocation.setText(getServerRuntime().getLocation().toPortableString());
		}
		serverLocation.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				getServerRuntime().setLocation(new Path(serverLocation.getText().trim()));
				validate(handle);
			}
		});

		Button browse = new Button(group, SWT.PUSH);
		browse.setText(GlassfishWizardResources.browse);
		browse.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent se) {
				DirectoryDialog dialog = new DirectoryDialog(parent.getShell());
				dialog.setMessage(GlassfishWizardResources.selectInstallDir);
				String selectedDirectory = dialog.open();
				if (selectedDirectory != null && !selectedDirectory.isEmpty()) {
					serverLocation.setText(selectedDirectory);
					getServerRuntime().setLocation(new Path(serverLocation.getText().trim()));
				}
			}
		});

		// JDK location
		JdkFilter jdkFilter = glassfishRuntime.getVersion() == null ? null
				: new JdkFilter(glassfishRuntime.getJavaVersionConstraint());
		updateJREs(jdkFilter);
		label = new Label(group, SWT.NONE);
		label.setText(GlassfishWizardResources.installedJRE);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_END);
		data.horizontalSpan = 2;
		label.setLayoutData(data);

		jrecombo = new Combo(group, SWT.DROP_DOWN | SWT.READ_ONLY);
		jrecombo.setItems(jreNames);
		setDefaultJREComboText();
		data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		jrecombo.setLayoutData(data);
		jrecombo.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int sel = jrecombo.getSelectionIndex();
				IVMInstall vmInstall = null;
				if (sel > 0) {
					vmInstall = installedJREs.get(sel - 1);
				}
				GlassFishRuntime glassfishRuntime = (GlassFishRuntime) getServerRuntime().loadAdapter(GlassFishRuntime.class, null);
				glassfishRuntime.setVMInstall(vmInstall);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});

		Button button = SWTUtil.createButton(group, GlassfishWizardResources.installedJREs);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String currentVM = jrecombo.getText();
				if (showPreferencePage(parent)) {
					JdkFilter jdkFilter = glassfishRuntime.getVersion() == null ? null
							: new JdkFilter(glassfishRuntime.getJavaVersionConstraint());
					updateJREs(jdkFilter);
					jrecombo.setItems(jreNames);
					jrecombo.setText(currentVM);
					if (jrecombo.getSelectionIndex() == -1)
						jrecombo.select(0);
				}
			}
		});
	}
	
	private void setDefaultJREComboText() {
		GlassFishRuntime glassfishRuntime = (GlassFishRuntime) getServerRuntime().loadAdapter(GlassFishRuntime.class, null);
		if (glassfishRuntime != null && glassfishRuntime.getVMInstall() != null) {
			String selectedJRE = glassfishRuntime.getVMInstall().getName();

			int defaultIndex = -1;
			for (int i = 0; i < jreNames.length; i++) {
				if (jreNames[i].equals(selectedJRE)) {
					defaultIndex = i;
					break;
				}
			}

			if (defaultIndex != -1) {
				jrecombo.select(defaultIndex);
			}
		}
	}

	// TODO: now simply adds all JREs
	protected void updateJREs(JdkFilter jdkFilter) {
		// get all installed JVMs
		installedJREs = new ArrayList<>();
		IVMInstallType[] vmInstallTypes = JavaRuntime.getVMInstallTypes();
		int size = vmInstallTypes.length;
		for (int i = 0; i < size; i++) {
			IVMInstall[] vmInstalls = vmInstallTypes[i].getVMInstalls();
			int size2 = vmInstalls.length;
			for (int j = 0; j < size2; j++) {
//				if (jdkFilter == null || jdkFilter.allows(vmInstalls[j])) {
				installedJREs.add(vmInstalls[j]);
//				}
			}
		}

		// get names
		size = installedJREs.size();
		jreNames = new String[size + 1];
		jreNames[0] = GlassfishWizardResources.runtimeDefaultJRE;
		for (int i = 0; i < size; i++) {
			IVMInstall vmInstall = installedJREs.get(i);
			jreNames[i + 1] = vmInstall.getName();
		}
	}

	private boolean internal(final IVMInstall jvm) {
		if (jvm instanceof AbstractVMInstall) {
			final String internal = ((AbstractVMInstall) jvm).getAttribute("internal");
			return "true".equals(internal);
		}

		return false;
	}

	protected boolean showPreferencePage(Composite parent) {
		String id = "org.eclipse.jdt.debug.ui.preferences.VMPreferencePage";

		// should be using the following API, but it only allows a single preference
		// page instance.
		// see bug 168211 for details
		// PreferenceDialog dialog =
		// PreferencesUtil.createPreferenceDialogOn(getShell(), id, new String[] { id },
		// null);
		// return (dialog.open() == Window.OK);

		PreferenceManager manager = PlatformUI.getWorkbench().getPreferenceManager();
		IPreferenceNode node = manager.find("org.eclipse.jdt.ui.preferences.JavaBasePreferencePage").findSubNode(id);
		PreferenceManager manager2 = new PreferenceManager();
		manager2.addToRoot(node);
		PreferenceDialog dialog = new PreferenceDialog(parent.getShell(), manager2);
		dialog.create();
		return (dialog.open() == Window.OK);
	}

	private String getServerName() {
		if (getServer() != null && getServer().getRuntime() != null)
			return getServer().getRuntime().getRuntimeType().getName();
		return null;
	}

	private IServerWorkingCopy getServer() {
		return (IServerWorkingCopy) getTaskModel().getObject(TaskModel.TASK_SERVER);
	}

	private IRuntimeWorkingCopy getServerRuntime() {
		return (IRuntimeWorkingCopy) getTaskModel().getObject(TASK_RUNTIME);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.wst.server.ui.wizard.WizardFragment#isComplete()
	 */
	@Override
	public boolean isComplete() {
		return validate(null);
	}

	private GlassFishServer getGlassFishServer() {
		GlassFishServer glassFishServer = getServer().getAdapter(GlassFishServer.class);
		if (glassFishServer == null)
			glassFishServer = (GlassFishServer) getServer().loadAdapter(GlassFishServer.class, null);
		return glassFishServer;
	}

	protected String getTitle() {
		return ((IRuntimeWorkingCopy) getTaskModel().getObject(TASK_RUNTIME)).getRuntimeType().getName();
	}

	protected String getDescription() {
		return wzdRuntimeDescription;
	}

	@Override
	public void setTaskModel(TaskModel taskModel) {
		super.setTaskModel(taskModel);
		if (getTaskModel().getObject(TASK_RUNTIME) instanceof RuntimeWorkingCopy) {
			IRuntimeWorkingCopy runtime = (IRuntimeWorkingCopy) getTaskModel().getObject(TASK_RUNTIME);
			if (runtime.getOriginal() == null) {
				try {
					runtime.setName(createUniqueRuntimeName(runtime.getRuntimeType().getName()));
				} catch (UniqueNameNotFound e) {
					// Set the type name and let the user handle validation error
					runtime.setName(runtime.getRuntimeType().getName());
				}
			}
		}
	}

	@Override
	public void performFinish(IProgressMonitor monitor) throws CoreException {
		super.performFinish(monitor);

		if (getTaskModel().getObject(TASK_RUNTIME) instanceof RuntimeWorkingCopy) {
			RuntimeWorkingCopy runtime = (RuntimeWorkingCopy) getTaskModel().getObject(TASK_RUNTIME);
			runtime.save(true, monitor);
			runtime.dispose();
		}
	}

	@Override
	public void performCancel(final IProgressMonitor monitor) throws CoreException {
		super.performCancel(monitor);
		if (getTaskModel().getObject(TASK_RUNTIME) instanceof RuntimeWorkingCopy) {
			RuntimeWorkingCopy runtime = (RuntimeWorkingCopy) getTaskModel().getObject(TASK_RUNTIME);
			runtime.dispose();
		}
	}

	protected boolean validate(IWizardHandle wizard) {
		boolean valid = true;
		if (wizard != null) {
			wizard.setMessage(null, IMessageProvider.NONE);
		}
		GlassFishRuntime glassfishRuntime = (GlassFishRuntime) getServerRuntime().loadAdapter(GlassFishRuntime.class, null);
		IStatus status = glassfishRuntime.validateServerLocation(new Path(serverLocation.getText()));
		if (status.getSeverity() > 0) {
			valid = false;
			if (wizard != null) {
				wizard.setMessage(status.getMessage(), IMessageProvider.ERROR);
			}
		}
		if (glassfishRuntime.getVMInstall() == null) {
			if (wizard != null) {
				wizard.setMessage("JRE path is not valid", IMessageProvider.ERROR);
			}
			valid = false;
		}
		if (getServerRuntime().getName() == null || getServerRuntime().getName().isBlank()) {
			if (wizard != null) {
				wizard.setMessage("Runtime name is not valid", IMessageProvider.ERROR);
			}
			valid = false;
		} else {
			IRuntime thisRuntime = glassfishRuntime.getRuntime();

			if (thisRuntime instanceof IRuntimeWorkingCopy) {
				thisRuntime = ((IRuntimeWorkingCopy) thisRuntime).getOriginal();
			}

			for (final IRuntime runtime : getRuntimes()) {
				if (runtime != thisRuntime && getServerRuntime().getName().equals(runtime.getName())) {
					wizard.setMessage(bind(duplicateRuntimeName, getServerRuntime().getName()), IMessageProvider.ERROR);
					valid = false;
				}
			}

		}
		if (wizard != null) {
			wizard.update();
		}
		return valid;
	}

	protected String UniqueServerNameValidationService(String name, IServerWorkingCopy thisServerWorkingCopy) {
		String duplicateServerName = "Server name %s is already in use";

		if (!name.isEmpty()) {
//                final IServerWorkingCopy thisServerWorkingCopy = name.element().adapt(IServerWorkingCopy.class);
			final IServer thisServer = thisServerWorkingCopy.getOriginal();

			for (final IServer server : getServers()) {
				if (server != thisServer && name.equals(server.getName())) {
					return String.format(duplicateServerName, name);
				}
			}
		}

		return null;
	}

}
