/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

/******************************************************************************
 * Copyright (c) 2018-2023 Payara Foundation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.glassfish.eclipse.tools.server.sdk.admin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;

import org.glassfish.eclipse.tools.server.GlassFishServer;
import org.glassfish.eclipse.tools.server.sdk.GlassFishIdeException;
import org.glassfish.eclipse.tools.server.sdk.logging.Logger;

/**
 * Command runner for deploying directory or file.
 * <p>
 * <p/>
 *
 * @author Tomas Kraus, Peter Benedikovic
 */
public class RunnerRestDeploy extends RunnerRest {

	private static final String NEWLINE = "\r\n";

	private String multipartBoundary = Long.toHexString(System.currentTimeMillis());

	/** Holding data for command execution. */
	@SuppressWarnings("FieldNameHidesFieldInSuperclass")
	final CommandDeploy command;

	/**
	 * Constructs an instance of administration command executor using REST
	 * interface.
	 * <p/>
	 *
	 * @param server  GlassFish server entity object.
	 * @param command GlassFish server administration command entity.
	 */
	public RunnerRestDeploy(final GlassFishServer server, final Command command) {
		super(server, command);
		this.command = (CommandDeploy) command;
	}

	////////////////////////////////////////////////////////////////////////////
	// Implemented Abstract Methods //
	////////////////////////////////////////////////////////////////////////////

	@Override
	protected void prepareHttpConnection(HttpURLConnection conn) throws CommandException {
		super.prepareHttpConnection(conn);
		if (!command.dirDeploy) {
			conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + multipartBoundary);
		}
	}

	/**
	 * Handle sending data to server using HTTP command interface.
	 * <p/>
	 * This is based on reading the code of <code>CLIRemoteCommand.java</code> from
	 * the server's code repository. Since some asadmin commands need to send
	 * multiple files, the server assumes the input is a ZIP stream.
	 */
	@Override
	protected void handleSend(HttpURLConnection hconn) throws IOException {
		// InputStream istream = getInputStream();
		if (command.path == null) {
			throw new GlassFishIdeException("The path attribute of deploy command" + " has to be non-empty!");
		}
		String path = command.path.getAbsolutePath();
		if (command.dockerInstance && command.hostPath != null && !command.hostPath.isEmpty()
				&& command.containerPath != null && !command.containerPath.isEmpty()) {
			try {
				Path relativePath = Paths.get(command.hostPath).relativize(command.path.toPath());
				path = Paths.get(command.containerPath, relativePath.toString()).toString();
				if (command.containerPath.startsWith("/")) {
					path = path.replace("\\", "/");
				}
			} catch (IllegalArgumentException ex) {
				throw new CommandException(CommandException.DOCKER_HOST_APPLICATION_PATH);
			}
		}
		if (command.wslInstance) {
            // Replace backslashes with forward slashes
            path = path.replace("\\", "/");
            // Add "mnt" prefix and drive letter
            path = "/mnt/" + path.substring(0, 1).toLowerCase() + path.substring(2);
        }
		OutputStreamWriter wr = new OutputStreamWriter(hconn.getOutputStream());
		if (!command.dirDeploy) {
			writeParam(wr, "path", path);
			if (command.name != null) {
				writeParam(wr, "name", command.name);
			}
			if (command.contextRoot != null) {
				writeParam(wr, "contextroot", command.contextRoot);
			}
			if (command.target != null) {
				writeParam(wr, "target", command.target);
			}

			writeBinaryFile(wr, hconn.getOutputStream(), command.path);
			wr.append("--" + multipartBoundary + "--").append(NEWLINE);
		} else {
			wr.write("path=" + command.path.toString());
			if (command.name != null) {
				wr.write("&");
				wr.write("name=" + command.name);
			}
			if (command.contextRoot != null) {
				wr.write("&");
				wr.write("contextroot=" + command.name);
			}
			if (command.target != null) {
				wr.write("&");
				wr.write("target=" + command.target);
			}
		}

		wr.close();
	}

	private void writeParam(OutputStreamWriter writer, String paramName, String paramValue) throws IOException {
		writer.append("--" + multipartBoundary).append(NEWLINE);
		writer.append("Content-Disposition: form-data; name=\"").append(paramName).append("\"").append(NEWLINE);
		writer.append("Content-Type: text/plain;").append(NEWLINE);
		writer.append(NEWLINE);
		writer.append(paramValue).append(NEWLINE).flush();
	}

	private void writeBinaryFile(OutputStreamWriter writer, OutputStream output, File file) throws IOException {
		writer.append("--" + multipartBoundary).append(NEWLINE);
		// writer.append("Content-Disposition: form-data; name=\"warFile\"; filename=\""
		// + file.getAbsolutePath() + "\"").append(NEWLINE);
		writer.append("Content-Type: application/octet-stream").append(NEWLINE);
		writer.append("Content-Transfer-Encoding: binary").append(NEWLINE);
		writer.append(NEWLINE).flush();

		InputStream input = null;
		try {
			input = new FileInputStream(file);
			byte[] buffer = new byte[1024 * 1024];
			for (int length; (length = input.read(buffer)) > 0;) {
				output.write(buffer, 0, length);
			}
			output.flush(); // Important! Output cannot be closed. Close of writer will close output as
							// well.
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException ex) {
				}
			}
		}
		writer.append(NEWLINE).flush();
	}
	////////////////////////////////////////////////////////////////////////////
	// Fake Getters //
	////////////////////////////////////////////////////////////////////////////

	/**
	 * Set the content-type of information sent to the server. Returns
	 * <code>application/zip</code> for file deployment and <code>null</code> (not
	 * set) for directory deployment.
	 *
	 * @return content-type of data sent to server via HTTP POST
	 */
	@Override
	public String getContentType() {
		return command.dirDeploy ? null : "application/zip";
	}

	// /**
	// * Provide the lastModified date for data source whose
	// * <code>InputStream</code> is returned by getInputStream.
	// * <p/>
	// * @return String format of long integer from lastModified date of source.
	// */
	// @Override
	// public String getLastModified() {
	// return Long.toString(command.path.lastModified());
	// }
	/**
	 * Get <code>InputStream</code> object for deployed file.
	 * <p/>
	 *
	 * @return <code>InputStream</code> object for deployed file or
	 *         <code>null</code> for directory deployment.
	 */
	public InputStream getInputStream() {
		if (command.dirDeploy) {
			return null;
		} else {
			try {
				return new FileInputStream(command.path);
			} catch (FileNotFoundException fnfe) {
				Logger.log(Level.INFO, command.path.getPath(), fnfe);
				return null;
			}
		}
	}

}
