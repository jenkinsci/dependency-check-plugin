/*
 * This file is part of Dependency-Check Jenkins plugin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jenkinsci.plugins.DependencyCheck;

import hudson.FilePath;
import hudson.Launcher;
import hudson.PluginWrapper;
import hudson.ProxyConfiguration;
import hudson.maven.MavenModule;
import hudson.maven.MavenModuleSet;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Cause;
import hudson.tasks.Builder;
import hudson.triggers.SCMTrigger;
import jenkins.model.Jenkins;
import jenkins.security.MasterToSlaveCallable;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;


public abstract class AbstractDependencyCheckBuilder extends Builder implements Serializable {

    private static final long serialVersionUID = -4931447003795862445L;

    protected static final String OUT_TAG = "[" + DependencyCheckPlugin.PLUGIN_NAME + "] ";

    protected boolean skipOnScmChange;
    protected boolean skipOnUpstreamChange;
    private Options options;


    /**
     * Set options to be used for the Builder step
     * @param options Options
     */
    public void setOptions(Options options) {
        this.options = options;
    }

    /**
     * This method is called whenever the DependencyCheck build step is executed.
     *
     * @param build    A Build object
     * @param launcher A Launcher object
     * @param listener A BuildListener object
     * @return A true or false value indicating if the build was successful or if it failed
     */
    @Override
    public boolean perform(final AbstractBuild build, final Launcher launcher, final BuildListener listener)
            throws InterruptedException, IOException {

        // Determine if the build should be skipped or not
        if (isSkip(build, listener)) {
            return true;
        }

        // Get the version of the plugin and print it out
        final PluginWrapper wrapper = Jenkins.getInstance().getPluginManager().getPlugin(DependencyCheckDescriptor.PLUGIN_ID);
        listener.getLogger().println(OUT_TAG + wrapper.getLongName() + " v" + wrapper.getVersion());

        final ClassLoader classLoader = wrapper.classLoader;
        final boolean isMaster = (build.getBuiltOn() == Jenkins.getInstance());

        // Node-agnostic execution of Dependency-Check
        if (isMaster) {
            return launcher.getChannel().call(new MasterToSlaveCallable<Boolean, IOException>() {
                public Boolean call() throws IOException {
                    final DependencyCheckExecutor executor = new DependencyCheckExecutor(options, listener, classLoader);
                    return executor.performBuild();
                }
            });
        } else {
            return launcher.getChannel().call(new MasterToSlaveCallable<Boolean, IOException>() {
                public Boolean call() throws IOException {
                    final DependencyCheckExecutor executor = new DependencyCheckExecutor(options, listener);
                    return executor.performBuild();
                }
            });
        }
    }

    /**
     * Convenience method that determines if the project is a Maven project.
     * @param clazz The projects class
     *
     * @deprecated will be removed in a future version
     */
    @Deprecated
    public boolean isMaven(Class<? extends AbstractProject> clazz) {
        return MavenModuleSet.class.isAssignableFrom(clazz) || MavenModule.class.isAssignableFrom(clazz);
    }

    /**
     * Determine if the build should be skipped or not
     */
    private boolean isSkip(AbstractBuild build, BuildListener listener) {
        boolean skip = false;

        // Determine if the OWASP_DC_SKIP environment variable is set to true
        try {
            skip = Boolean.parseBoolean(build.getEnvironment(listener).get("OWASP_DC_SKIP"));
        } catch (Exception e) { /* throw it away */ }


        // Why was this build triggered? Get the causes and find out.
        @SuppressWarnings("unchecked")
        final List<Cause> causes = build.getCauses();
        for (Cause cause: causes) {
            // Skip if the build is configured to skip on SCM change and the cause of the build was an SCM trigger
            if (skipOnScmChange && cause instanceof SCMTrigger.SCMTriggerCause) {
                skip = true;
            }
            // Skip if the build is configured to skip on Upstream change and the cause of the build was an Upstream trigger
            if (skipOnUpstreamChange && cause instanceof Cause.UpstreamCause) {
                skip = true;
            }
        }

        // Log a message if being skipped
        if (skip) {
            listener.getLogger().println(OUT_TAG + "Skipping Dependency-Check analysis.");
        }

        return skip;
    }

    /**
     * By default, DependencyCheck will place the 'data' directory in the same directory
     * as the DependencyCheck JAR. We need to overwrite these settings and account for
     * the fact that in a multi-node Jenkins cluster, a centralized data directory may
     * not be possible. Therefore, a subdirectory in the builds workspace is used.
     *
     * @return A boolean indicating if any errors occurred during the validation process
     */
    protected boolean configureDataDirectory(AbstractBuild build, BuildListener listener, Options options,
                                             String globalDataDir, String dataDir) {
        FilePath dataPath;
        if (StringUtils.isBlank(globalDataDir) && StringUtils.isBlank(dataDir)) {
            // datadir was not specified, so use the default 'dependency-check-data' directory
            // located in the builds workspace.
            dataPath = new FilePath(build.getWorkspace(), "dependency-check-data");
            try {
                if (build.getWorkspace() == null || !build.getWorkspace().exists()) {
                    throw new IOException("Jenkins workspace directory not available. Once a build is complete, Jenkins may use the workspace to build something else, or remove it entirely.");
                }
            } catch (Exception e) {
                return false;
            }
        } else {
            if (!StringUtils.isBlank(dataDir)) {
                // job-specific datadir was specified. Override the global setting
                dataPath = new FilePath(build.getWorkspace(), substituteVariable(build, listener, dataDir));
            } else {
                // use the global setting
                dataPath = new FilePath(build.getWorkspace(), substituteVariable(build, listener, globalDataDir));
            }
        }
        options.setDataDirectory(dataPath.getRemote());
        return true;
    }

    /**
     * Generate Options from build configuration preferences that will be passed to
     * the build step in DependencyCheck
     * @param build an AbstractBuild object
     * @return DependencyCheck Options
     */
    protected Options optionsBuilder(AbstractBuild build, BuildListener listener,
                                     String outdir, boolean isVerboseLoggingEnabled,
                                     String tempPath, boolean isQuickQueryTimestampEnabled) {
        final Options options = new Options();

        // Sets the DependencyCheck application name to the Jenkins display name. If a display name
        // was not defined, it will simply return the name of the build.
        options.setName(build.getProject().getDisplayName());

        // If the configured output directory is empty, set this builds output dir to the root of the projects workspace
        FilePath outDirPath;
        if (StringUtils.isBlank(outdir)) {
            outDirPath = build.getWorkspace();
        } else {
            outDirPath = new FilePath(build.getWorkspace(), substituteVariable(build, listener, outdir.trim()));
        }
        options.setOutputDirectory(outDirPath.getRemote());

        // LOGGING
        final FilePath log = new FilePath(build.getWorkspace(), "dependency-check.log");
        if (isVerboseLoggingEnabled) {
            options.setVerboseLoggingFile(log.getRemote());
        }

        options.setWorkspace(build.getWorkspace().getRemote());

        // If temp path has been specified, use it, otherwise Dependency-Check will default to the Java temp path
        if (StringUtils.isNotBlank(tempPath)) {
            options.setTempPath(new FilePath(new File(substituteVariable(build, listener, tempPath))).getRemote());
        }

        options.setIsQuickQueryTimestampEnabled(isQuickQueryTimestampEnabled);

        return options;
    }

    protected void configureProxySettings(Options options, boolean isNvdProxyBypassed) {
        final ProxyConfiguration proxy = Jenkins.getInstance() != null ? Jenkins.getInstance().proxy : null;
        if (!isNvdProxyBypassed && proxy != null) {
            if (!StringUtils.isBlank(proxy.name)) {
                options.setProxyServer(proxy.name);
                options.setProxyPort(proxy.port);
            }
            if (!StringUtils.isBlank(proxy.getUserName())) {
                options.setProxyUsername(proxy.getUserName());
            }
            if (!StringUtils.isBlank(proxy.getPassword())) {
                options.setProxyPassword(proxy.getPassword());
            }
        }
    }

    protected void configureDataMirroring(Options options, int dataMirroringType,
                                          String cveUrl12Modified, String cveUrl20Modified,
                                          String cveUrl12Base, String cveUrl20Base) {
        options.setDataMirroringType(dataMirroringType);
        if (options.getDataMirroringType() != 0) {
            if (!StringUtils.isBlank(cveUrl12Modified) && !StringUtils.isBlank(cveUrl20Modified)
                    && !StringUtils.isBlank(cveUrl12Base) && !StringUtils.isBlank(cveUrl20Base)) {
                try {
                    options.setCveUrl12Modified(new URL(cveUrl12Modified));
                    options.setCveUrl20Modified(new URL(cveUrl20Modified));
                    options.setCveUrl12Base(new URL(cveUrl12Base));
                    options.setCveUrl20Base(new URL(cveUrl20Base));
                } catch (MalformedURLException e) {
                    // todo: need to log this or otherwise warn.
                }
            }
        }
    }

    /**
     * Replace a Jenkins environment variable in the form ${name} contained in the
     * specified String with the value of the matching environment variable.
     */
    protected String substituteVariable(AbstractBuild build, BuildListener listener, String parameterizedValue) {
        try {
            if (parameterizedValue != null && parameterizedValue.contains("${")) {
                final int start = parameterizedValue.indexOf("${");
                final int end = parameterizedValue.indexOf("}", start);
                final String parameter = parameterizedValue.substring(start + 2, end);
                final String value = build.getEnvironment(listener).get(parameter);
                if (value == null) {
                    throw new IllegalStateException(parameter);
                }
                final String substitutedValue = parameterizedValue.substring(0, start) + value + (parameterizedValue.length() > end + 1 ? parameterizedValue.substring(end + 1) : "");
                if (end > 0) { // recursively substitute variables
                    return substituteVariable(build, listener, substitutedValue);
                } else {
                    return parameterizedValue;
                }
            } else {
                return parameterizedValue;
            }
        } catch (Exception e) {
            return parameterizedValue;
        }
    }

    protected String toCommaSeparatedString(String input) {
        input = input.trim();
        input = input.replaceAll(" +", ",");
        return input;
    }

}
