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
import hudson.model.Cause;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.Builder;
import hudson.triggers.SCMTrigger;
import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildStep;
import org.apache.commons.lang.StringUtils;
import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;


public abstract class AbstractDependencyCheckBuilder extends Builder implements SimpleBuildStep, Serializable {

    private static final long serialVersionUID = -4931447003795862445L;

    private static final String OUT_TAG = "[" + DependencyCheckPlugin.PLUGIN_NAME + "] ";

    protected boolean skipOnScmChange;
    protected boolean skipOnUpstreamChange;
    protected boolean preserveBuildSuccessOnScanFailure;
    private Options options;


    /**
     * Set options to be used for the Builder step
     * @param options Options
     */
    void setOptions(Options options) {
        this.options = options;
    }

    /**
     * This method is called whenever the build step is executed.
     */
    @Override
    public void perform(@Nonnull final Run<?, ?> build,
                        @Nonnull final FilePath filePath,
                        @Nonnull final Launcher launcher,
                        @Nonnull final TaskListener listener) throws InterruptedException, IOException {

        // Determine if the build should be skipped or not
        if (isSkip(build, listener)) {
            build.setResult(Result.SUCCESS);
            return;
        }

        // Get the version of the plugin and print it out
        final PluginWrapper wrapper = Jenkins.getInstance().getPluginManager().getPlugin(DependencyCheckDescriptor.PLUGIN_ID);
        listener.getLogger().println(OUT_TAG + wrapper.getLongName() + " v" + wrapper.getVersion());

        final ClassLoader classLoader = wrapper.classLoader;

        final boolean isMaster = build.getExecutor().getOwner().getNode() == Jenkins.getInstance();

        // Node-agnostic execution of Dependency-Check
        boolean success;
        if (isMaster) {
            success = launcher.getChannel().call(new DependencyCheckExecutor(options, listener, classLoader));
        } else {
            success = launcher.getChannel().call(new DependencyCheckExecutor(options, listener));
        }
        if (success || preserveBuildSuccessOnScanFailure) {
            build.setResult(Result.SUCCESS);
        } else {
            build.setResult(Result.FAILURE);
        }
    }

    /**
     * Determine if the build should be skipped or not
     */
    private boolean isSkip(final Run<?, ?> build, final TaskListener listener) {
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
    boolean configureDataDirectory(final Run<?, ?> build, final FilePath workspace, final TaskListener listener,
                                             final Options options, final String globalDataDir, final String dataDir) {
        FilePath dataPath;
        if (StringUtils.isBlank(globalDataDir) && StringUtils.isBlank(dataDir)) {
            // datadir was not specified, so use the default 'dependency-check-data' directory
            // located in the builds workspace.
            dataPath = new FilePath(workspace, "dependency-check-data");
            try {
                if (!workspace.exists()) {
                    throw new IOException("Jenkins workspace directory not available. Once a build is complete, Jenkins may use the workspace to build something else, or remove it entirely.");
                }
            } catch (Exception e) {
                return false;
            }
        } else {
            if (!StringUtils.isBlank(dataDir)) {
                // job-specific datadir was specified. Override the global setting
                dataPath = new FilePath(workspace, PluginUtil.substituteVariable(build, listener, dataDir));
            } else {
                // use the global setting
                dataPath = new FilePath(workspace, PluginUtil.substituteVariable(build, listener, globalDataDir));
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
    Options optionsBuilder(final Run<?, ?> build, final FilePath workspace, final TaskListener listener,
                           final String outdir, final String tempPath, final boolean isQuickQueryTimestampEnabled) {
        final Options options = new Options();

        // Sets the DependencyCheck application name to the Jenkins display name. If a display name
        // was not defined, it will simply return the name of the build.
        options.setName(build.getParent().getDisplayName());

        // If the configured output directory is empty, set this builds output dir to the root of the projects workspace
        FilePath outDirPath;
        if (StringUtils.isBlank(outdir)) {
            outDirPath = workspace;
        } else {
            outDirPath = new FilePath(workspace, PluginUtil.substituteVariable(build, listener, outdir.trim()));
        }
        options.setOutputDirectory(outDirPath.getRemote());

        options.setWorkspace(workspace.getRemote());

        // If temp path has been specified, use it, otherwise Dependency-Check will default to the Java temp path
        if (StringUtils.isNotBlank(tempPath)) {
            options.setTempPath(new FilePath(new File(PluginUtil.substituteVariable(build, listener, tempPath))).getRemote());
        }

        options.setIsQuickQueryTimestampEnabled(isQuickQueryTimestampEnabled);

        return options;
    }

    void configureProxySettings(final Options options, final boolean isNvdProxyBypassed) {
        final ProxyConfiguration proxy = Jenkins.getInstance() != null ? Jenkins.getInstance().proxy : null;
        if (!isNvdProxyBypassed && proxy != null) {
            if (!StringUtils.isBlank(proxy.name)) {
                options.setProxyServer(proxy.name);
                options.setProxyPort(proxy.port);
            }
            if (!StringUtils.isBlank(proxy.noProxyHost)) {
                options.setNonProxyHosts(proxy.noProxyHost.replace("\n", ",").replace("\r", ""));
            }
            if (!StringUtils.isBlank(proxy.getUserName())) {
                options.setProxyUsername(proxy.getUserName());
            }
            if (!StringUtils.isBlank(proxy.getPassword())) {
                options.setProxyPassword(proxy.getPassword());
            }
        }
    }

    void configureDataMirroring(final Options options, final int dataMirroringType,
                                final String cveUrl12Modified, final String cveUrl20Modified,
                                final String cveUrl12Base, final String cveUrl20Base,
                                final String retireJsRepoJsUrl) {
        options.setDataMirroringType(dataMirroringType);
        if (options.getDataMirroringType() == -1 || options.getDataMirroringType() == 1) {
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
        if (options.getDataMirroringType() == -1 || options.getDataMirroringType() == 2) {
            if (!StringUtils.isBlank(retireJsRepoJsUrl)) {
                try {
                    options.setRetireJsRepoJsUrl(new URL(retireJsRepoJsUrl));
                } catch (MalformedURLException e) {
                    // todo: need to log this or otherwise warn.
                }
            }
        }
    }

    String toCommaSeparatedString(String input) {
        input = input.trim();
        input = input.replaceAll(" +", ",");
        return input;
    }

}
