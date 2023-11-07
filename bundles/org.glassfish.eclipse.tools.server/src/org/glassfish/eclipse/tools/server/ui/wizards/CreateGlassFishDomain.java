package org.glassfish.eclipse.tools.server.ui.wizards;

import static org.eclipse.core.runtime.IStatus.INFO;
import static org.eclipse.debug.core.ILaunchManager.RUN_MODE;
import static org.glassfish.eclipse.tools.server.GlassFishServerPlugin.SYMBOLIC_NAME;

import java.io.File;
import java.util.Arrays;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jdt.internal.launching.LaunchingPlugin;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
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
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.glassfish.eclipse.tools.server.GlassFishRuntime;
import org.glassfish.eclipse.tools.server.GlassFishServer;
import org.glassfish.eclipse.tools.server.GlassFishServerPlugin;

/**
 * Invoked when the user clicks on the "new domain" icon next to the "Domain Path"
 * input field in the "new server" wizard.
 *
 */

public class CreateGlassFishDomain extends MessageDialog {
	
	public static int MAXIMUM_PORT = 999999;
	public static int DEFAULT_PORT = 8000;
	public static String DEFAULT_DOMAIN = "domain1";

	private Text domainName;
	private Text domainDir;
	private Spinner portBase;
	private Label message;
	private ProgressBar progressBar;
	private GlassFishServer glassfishServer;
	private GlassFishRuntime runtime;
	private String path;

	public CreateGlassFishDomain(Shell parentShell, GlassFishServer glassfishServer, GlassFishRuntime runtime) {
		super(parentShell, GlassfishWizardResources.newDomainTitle,
				GlassFishServerPlugin.getImage(GlassFishServerPlugin.GF_SERVER_IMG),
				GlassfishWizardResources.newDomainDescription, CONFIRM,
				new String[] { GlassfishWizardResources.newDomainCreateButton, IDialogConstants.CANCEL_LABEL }, 0);
		this.glassfishServer = glassfishServer;
		this.runtime = runtime;
	}

	public String getPath() {
		return path;
	}

	@Override
	protected void buttonPressed(int buttonId) {
		setReturnCode(buttonId);
		if (buttonId != 0 || execute()) {
			close();
		}
	}
	
	protected boolean domainNameValidation() {
		String name = domainName.getText();
		if (name != null && name.trim().length() > 0) {

			if (name.indexOf(' ') > 0) {
				setMessage("Invalid value for domain name."); //$NON-NLS-1$
				return false;
			}
			File domainsDir = new File(domainDir.getText(), name);
			if (domainsDir.exists()) {
				setMessage("A domain already exists at the specified location."); //$NON-NLS-1$
				return false;
			}
			return true;
		}
		
		return false;
	}

	public boolean execute() {
		if (!domainNameValidation()) {
			return false;
		}
		
		File asadmin = new File(new File(glassfishServer.getServerHome(), "bin"),
				Platform.getOS().equals(Platform.OS_WIN32) ? "asadmin.bat" : "asadmin");
		
		if (asadmin.exists()) {
			String javaExecutablePath = asadmin.getAbsolutePath();
			String[] cmdLine = new String[] { 
					javaExecutablePath, 
					"create-domain", "--nopassword=true", 
					"--portbase", String.valueOf(portBase.getSelection()), 
					"--domaindir", domainDir.getText(), domainName.getText() };

			Process realProcess = null;

			try {
				final StringBuilder output = new StringBuilder();
				final StringBuilder errOutput = new StringBuilder();
				output.append(Arrays.toString(cmdLine) + "\n");

				// Set AS_JAVA location which will be used to run asadmin
				String envp[] = new String[1];
				envp[0] = "AS_JAVA=" + runtime.getVMInstall().getInstallLocation().getPath();

				realProcess = DebugPlugin.exec(cmdLine, null, envp);
				IProcess eclipseProcess = DebugPlugin.newProcess(new Launch(null, RUN_MODE, null), realProcess, "GlassFish asadmin"); //$NON-NLS-1$

				// Log output
				eclipseProcess.getStreamsProxy().getOutputStreamMonitor().addListener((text, monitor) -> output.append(text));

				eclipseProcess.getStreamsProxy()
				              .getErrorStreamMonitor()
						      .addListener((text, monitor) -> errOutput.append(text));
				
				setMessage("");
				
				for (int i = 0; i < 600; i++) {
					// Wait no more than 30 seconds (600 * 50 milliseconds)
					if (eclipseProcess.isTerminated()) {
						GlassFishServerPlugin.getInstance()
						                     .getLog()
						                     .log(new Status(
					                    		 INFO, SYMBOLIC_NAME, 1, 
					                    		 output.toString() + "\n" + errOutput.toString(), null));
						break;
					}
					try {
						Thread.sleep(50);
						progressBar.setSelection(i);
					} catch (InterruptedException e) {
					}
				}

				File domainFile = new File(domainDir.getText(), domainName.getText());
				if (!domainFile.exists()) {
					setMessage("Error in creating the GlassFish Server domain");
					return false;
				}
			} catch (CoreException ioe) {
				LaunchingPlugin.log(ioe);
				setMessage(ioe.getMessage());
				return false;
			} finally {
				if (realProcess != null) {
					realProcess.destroy();
				}
			}
		}
		
		path = domainDir.getText() + File.separator + domainName.getText();
		return true;
	}

	private void setMessage(String text) {
		message.setText(text);
		message.setVisible(!text.isEmpty());
		progressBar.setVisible(text.isEmpty());
	}

	@Override
	protected Control createCustomArea(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout grid = new GridLayout(2, false);
		grid.marginWidth = 0;
		container.setLayout(grid);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));

		GridLayout layout = new GridLayout(1, true);
		container.setLayout(layout);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));

		Composite group = new Composite(container, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		group.setLayout(layout);
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label label = new Label(group, SWT.NONE);
		label.setText(GlassfishWizardResources.domainName);
		GridData data = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_END);
		data.horizontalSpan = 2;
		label.setLayoutData(data);

		domainName = new Text(group, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		domainName.setLayoutData(data);
		domainName.setText(DEFAULT_DOMAIN);

		label = new Label(group, SWT.NONE);
		label.setText(GlassfishWizardResources.domainDir);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_END);
		data.horizontalSpan = 2;
		label.setLayoutData(data);

		domainDir = new Text(group, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		domainDir.setLayoutData(data);
		domainDir.setText(glassfishServer.getDomainsFolder());

		Button browse = new Button(group, SWT.PUSH);
		browse.setText(GlassfishWizardResources.browse);
		browse.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent se) {
				DirectoryDialog dialog = new DirectoryDialog(parent.getShell());
				dialog.setMessage(GlassfishWizardResources.selectInstallDir);
				String selectedDirectory = dialog.open();
				if (selectedDirectory != null && !selectedDirectory.isEmpty())
					domainDir.setText(selectedDirectory);
			}
		});

		label = new Label(group, SWT.NONE);
		label.setText(GlassfishWizardResources.portBase);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_END);
		data.horizontalSpan = 2;
		label.setLayoutData(data);

		portBase = new Spinner(group, SWT.BORDER);
		portBase.setMinimum(0);
		portBase.setMaximum(MAXIMUM_PORT);
		portBase.setTextLimit((Integer.toString(MAXIMUM_PORT)).length());
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		portBase.setLayoutData(data);
		portBase.setSelection(DEFAULT_PORT);

		message = new Label(group, SWT.NONE);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_END);
		data.horizontalSpan = 2;
		message.setLayoutData(data);
		message.setForeground(parent.getShell().getDisplay().getSystemColor(SWT.COLOR_RED));
		progressBar = new ProgressBar(group, SWT.HORIZONTAL);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		progressBar.setLayoutData(data);
		progressBar.setMinimum(0);
		progressBar.setMaximum(100);
		progressBar.setVisible(false);
		
		return container;
	}

}
