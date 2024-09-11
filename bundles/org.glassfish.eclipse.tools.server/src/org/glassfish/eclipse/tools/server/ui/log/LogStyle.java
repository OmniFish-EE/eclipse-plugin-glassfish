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

package org.glassfish.eclipse.tools.server.ui.log;

import static org.glassfish.eclipse.tools.server.preferences.PreferenceConstants.ENABLE_COLORS_CONSOLE;

import java.util.Arrays;
import java.util.logging.Level;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.LineStyleEvent;
import org.eclipse.swt.custom.LineStyleListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.glassfish.eclipse.tools.server.GlassFishServerPlugin;
import org.glassfish.eclipse.tools.server.log.AbstractLogFilter;
import org.glassfish.eclipse.tools.server.log.LevelResolver;

public class LogStyle implements LineStyleListener, IPropertyChangeListener {
    Display display = Display.getCurrent();

    IPreferenceStore store = GlassFishServerPlugin.getInstance().getPreferenceStore();
    boolean colorInConsole = store.getBoolean(ENABLE_COLORS_CONSOLE);
    String warningLocalizedName, severeLocalizedName;

    // private IDocument document;

    public LogStyle(IDocument document) {
        store.addPropertyChangeListener(this);
        AbstractLogFilter.ILevelResolver levelResolver = new LevelResolver();
        warningLocalizedName = levelResolver.resolve(Level.WARNING.getName());
        severeLocalizedName = levelResolver.resolve(Level.SEVERE.getName());
    }

    @Override
    public void lineGetStyle(LineStyleEvent event) {
        StyleRange styleRange = null;
        String buf = event.lineText;
        int start;

        if (colorInConsole) {
            if ((start = buf.indexOf(warningLocalizedName)) != -1) {
                styleRange = new StyleRange();
                styleRange.start = event.lineOffset + start;
                styleRange.length = warningLocalizedName.length()-1;
                styleRange.foreground = display.getSystemColor(SWT.COLOR_DARK_YELLOW);
            } else if ((start = buf.indexOf(severeLocalizedName)) != -1) {
                // Mark severe error and exception stack trace as error color
                styleRange = new StyleRange();
                String errorColorName = org.eclipse.jface.preference.JFacePreferences.ERROR_COLOR;
                styleRange.foreground = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getColorRegistry()
                        .get(errorColorName);
                styleRange.start = event.lineOffset + start;
                styleRange.length = severeLocalizedName.length()-1;
                styleRange.fontStyle = SWT.BOLD;
            } else if ((start = buf.indexOf("FATAL")) != -1) {
                styleRange = new StyleRange();
                String errorColorName = org.eclipse.jface.preference.JFacePreferences.ERROR_COLOR;
                styleRange.foreground = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getColorRegistry()
                        .get(errorColorName);
                styleRange.start = event.lineOffset + start;
                styleRange.length = 4;
                styleRange.fontStyle = SWT.BOLD;
            }

            if (styleRange != null) {
                StyleRange[] styles;
                if (event.styles != null) {
                    styles = Arrays.copyOf(event.styles, event.styles.length + 1);
                } else {
                    styles = new StyleRange[1];
                }
                styles[styles.length - 1] = styleRange;

                // Set the styles for the line
                event.styles = styles;
            }
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if (event.getProperty().equals(ENABLE_COLORS_CONSOLE)) {
            colorInConsole = store.getBoolean(ENABLE_COLORS_CONSOLE);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        store.removePropertyChangeListener(this);
        super.finalize();
    }
}
