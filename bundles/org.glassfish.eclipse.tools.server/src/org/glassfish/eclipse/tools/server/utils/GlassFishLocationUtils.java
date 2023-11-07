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

package org.glassfish.eclipse.tools.server.utils;

import static java.util.Collections.emptyList;
import static org.glassfish.eclipse.tools.server.internal.ManifestUtil.readManifestEntry;

import java.io.File;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tools.ant.DirectoryScanner;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
import org.eclipse.wst.common.project.facet.core.runtime.IRuntime;
import org.eclipse.wst.common.project.facet.core.runtime.IRuntimeComponent;
import org.glassfish.eclipse.tools.server.internal.SystemLibraries;

/**
 * Series of utils related to the location where GlassFish is installed.
 *
 * <p>
 * Primarily supplies the version and the libraries associated with the GlassFish location.
 *
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */
public final class GlassFishLocationUtils {

    public static final String DEFAULT_LIBRARIES = "default";
    public static final String ALL_LIBRARIES = "all";

    private static final Pattern VERSION_PATTERN = Pattern.compile("([0-9]\\.[0-9]+(\\.[0-9])?(\\.[0-9])?)(\\..*)?.*");

    // Defined as:
    // <extension point="org.eclipse.wst.common.project.facet.core.runtimes">
    // <runtime-component-type id="glassfish.runtime"/>
    private static final String RUNTIME_COMPONENT_ID = "glassfish.runtime";

    private static final Map<File, SoftReference<GlassFishLocationUtils>> CACHE = new HashMap<>();

    private final Version version;
    private final Map<String, List<File>> libraries;


    // #### static factory / finder methods


    public static synchronized GlassFishLocationUtils find(IJavaProject project) {
        if (project != null) {
            return find(project.getProject());
        }

        return null;
    }

    public static synchronized GlassFishLocationUtils find(IProject project) {
        if (project != null) {
            IFacetedProject facetedProject = null;

            try {
                facetedProject = ProjectFacetsManager.create(project);
            } catch (CoreException e) {
                // Intentionally ignored. If project isn't faceted or another error occurs,
                // all that matters is that the GlassFish install is not found, which is signaled by null
                // return.
            }

            return find(facetedProject);
        }

        return null;
    }

    public static synchronized GlassFishLocationUtils find(IFacetedProject project) {
        if (project != null) {
            IRuntime primary = project.getPrimaryRuntime();

            if (primary != null) {
                GlassFishLocationUtils glassfishLocation = find(primary);

                if (glassfishLocation != null) {
                    return glassfishLocation;
                }

                for (IRuntime runtime : project.getTargetedRuntimes()) {
                    if (runtime != primary) {
                        glassfishLocation = find(runtime);

                        if (glassfishLocation != null) {
                            return glassfishLocation;
                        }
                    }
                }
            }
        }

        return null;
    }

    public static synchronized GlassFishLocationUtils find(IRuntime runtime) {
        if (runtime != null) {
            for (IRuntimeComponent component : runtime.getRuntimeComponents()) {
                GlassFishLocationUtils glassfishLocation = find(component);

                if (glassfishLocation != null) {
                    return glassfishLocation;
                }
            }
        }

        return null;
    }

    public static synchronized GlassFishLocationUtils find(IRuntimeComponent component) {
        if (component != null && component.getRuntimeComponentType().getId().equals(RUNTIME_COMPONENT_ID)) {
            String location = component.getProperty("location");

            if (location != null) {
                return find(new File(location));
            }
        }

        return null;
    }

    public static synchronized GlassFishLocationUtils find(File location) {

        // Lazily cleanup cache keys
        for (Iterator<Map.Entry<File, SoftReference<GlassFishLocationUtils>>> itr = CACHE.entrySet().iterator(); itr.hasNext();) {
            if (itr.next().getValue().get() == null) {
                itr.remove();
            }
        }

        GlassFishLocationUtils glassfishLocation = null;

        if (location != null) {
            SoftReference<GlassFishLocationUtils> glassfishLocationReference = CACHE.get(location);

            if (glassfishLocationReference != null) {
                glassfishLocation = glassfishLocationReference.get();
            }

            if (glassfishLocation == null) {
                try {
                    glassfishLocation = new GlassFishLocationUtils(location);
                } catch (IllegalArgumentException e) {
                    return null;
                }

                CACHE.put(location, new SoftReference<>(glassfishLocation));
            }
        }

        return glassfishLocation;
    }



    // #### GlassFishLocation instance methods

    private GlassFishLocationUtils(File location) {
        checkLocationIsValid(location);

        File glassfishLocation = location;

        File gfApiJar = new File(glassfishLocation, "modules/glassfish-api.jar");

        if (!gfApiJar.exists()) {
            glassfishLocation = new File(glassfishLocation, "glassfish");

            gfApiJar = new File(glassfishLocation, "modules/glassfish-api.jar");

            if (!gfApiJar.exists()) {
                throw new IllegalArgumentException();
            }
        }

        if (!gfApiJar.isFile()) {
            throw new IllegalArgumentException();
        }

        version = readGlassFishVerionFromAPIJar(gfApiJar);
        libraries = readLibraryFilesFromGlassFishLocation(glassfishLocation, version);
    }

    public Version version() {
        return version;
    }

    public List<File> getLibraries(String libraryGroup) {
        return libraries.get(libraryGroup);
    }



    // #### Private methods

    private Version readGlassFishVerionFromAPIJar(File gfApiJar) {
        String versionString;
        try {
            versionString = readManifestEntry(gfApiJar, "Bundle-Version");
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }

        Matcher versionMatcher = VERSION_PATTERN.matcher(versionString);

        if (!versionMatcher.matches()) {
            throw new IllegalArgumentException();
        }

        return new Version(versionMatcher.group(1));
    }

    /**
     * Gets the relative file name patterns for the system libraries corresponding to the given GlassFish
     * version, and turns these into a list of actual files for the given GlassFish location on disk.
     *
     * @param glassfishLocation location where GlassFish is installed
     * @param glassfishVersion version of GlassFish for which libraries are to be retrieved
     *
     * @return list of system libraries as actual files
     */
    private Map<String, List<File>> readLibraryFilesFromGlassFishLocation(File glassfishLocation, Version glassfishVersion) {
        Map<String, List<File>> librariesPerVariant = new HashMap<>();

        librariesPerVariant.put(
            DEFAULT_LIBRARIES,
            readLibrariesByPattern(glassfishLocation, SystemLibraries.getLibraryIncludesByVersion(glassfishVersion)));

        librariesPerVariant.put(
            ALL_LIBRARIES,
            readLibrariesByPattern(glassfishLocation, new String[] {"**/*.jar"}, new String[] {"**/osgi-cache/**"}));

        return librariesPerVariant;
    }

    private List<File> readLibrariesByPattern(File glassfishLocation, String[] inclusionPattern) {
        return readLibrariesByPattern(glassfishLocation, inclusionPattern, null);
    }

    private List<File> readLibrariesByPattern(File glassfishLocation, String[] inclusionPattern, String[] exclusionPattern) {
        if (inclusionPattern == null) {
            return emptyList();
        }

        File parentFolderToLocation = glassfishLocation.getParentFile();

        // Use a directory scanner to resolve the wildcards and obtain an expanded
        // list of relative files.
        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setBasedir(parentFolderToLocation);
        scanner.setIncludes(inclusionPattern);
        if (exclusionPattern != null) {
            scanner.setExcludes(exclusionPattern);
        }
        scanner.scan();

        // Turn the expanded, but still relative, string based paths into absolute files.
        List<File> libraries = new ArrayList<>();
        for (String libraryRelativePath : scanner.getIncludedFiles()) {
            libraries.add(new File(parentFolderToLocation, libraryRelativePath));
        }

        return libraries;

    }

    private void checkLocationIsValid(File location) {
        if (location == null || !location.exists() || !location.isDirectory()) {
            throw new IllegalArgumentException();
        }
    }

}
