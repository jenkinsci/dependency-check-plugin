/*
 * This file is part of Dependency-Check Jenkins plugin.
 *
 * Dependency-Check is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * Dependency-Check is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Dependency-Check. If not, see http://www.gnu.org/licenses/.
 */
package org.jenkinsci.plugins.DependencyCheck;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.PluginWrapper;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Hudson;
import hudson.remoting.Callable;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.io.Serializable;

/**
 * The DependencyCheck builder class provides the ability to invoke a DependencyCheck build as
 * a Jenkins build step. This class takes the configuration from the UI, creates options from
 * them and passes them to the DependencyCheckExecutor for the actual execution of the
 * DependencyCheck Engine and ReportGenerator.
 *
 * @author Steve Springett (steve.springett@owasp.org)
 */
@SuppressWarnings("unused")
public class DependencyCheckBuilder extends Builder implements Serializable {

    private static final long serialVersionUID = 5594574614031769847L;

    private final String scanpath;
    private final String outdir;
    private final String datadir;
    private final boolean isAutoupdateDisabled;


    @DataBoundConstructor // Fields in config.jelly must match the parameter names
    public DependencyCheckBuilder(String scanpath, String outdir, String datadir, Boolean isAutoupdateDisabled) {
        this.scanpath = scanpath;
        this.outdir = outdir;
        this.datadir = datadir;
        this.isAutoupdateDisabled = (isAutoupdateDisabled != null) && isAutoupdateDisabled;
    }

    /**
     * Retrieves the path to scan. This is a per-build config item.
     * This method must match the value in <tt>config.jelly</tt>.
     */
    public String getScanpath() {
        return scanpath;
    }

    /**
     * Retrieves the output directory to write the report. This is a per-build config item.
     * This method must match the value in <tt>config.jelly</tt>.
     */
    public String getOutdir() {
        return outdir;
    }

    /**
     * Retrieves the data directory that DependencyCheck will use. This is a per-build config item.
     * This method must match the value in <tt>config.jelly</tt>.
     */
    public String getDatadir() {
        return datadir;
    }

    /**
     * Retrieves whether auto update should be disabled or not. This is a per-build config item.
     * This method must match the value in <tt>config.jelly</tt>.
     */
    public boolean isAutoupdateDisabled() {
        return isAutoupdateDisabled;
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

        String outtag = "[" + DependencyCheckPlugin.PLUGIN_NAME+"] ";

        // Check environment variable OWASP_DC_SKIP exists and is true. If so, skip Dependency-Check analysis
        boolean skip = false;
        try {
            skip = Boolean.parseBoolean(build.getEnvironment(listener).get("OWASP_DC_SKIP"));
        } catch (Exception e) { /* throw it away */ }
        if (skip) {
            listener.getLogger().println(outtag + "Environment variable OWASP_DC_SKIP is true. Skipping Dependency-Check analysis.");
            return true;
        }

        // Generate the Dependency-Check options - later used by DependencyCheckExecutor
        final Options options = generateOptions(build, listener);

        // Get the version of the plugin and print it out
        PluginWrapper wrapper = Hudson.getInstance().getPluginManager().getPlugin(DependencyCheckDescriptor.PLUGIN_ID);
        listener.getLogger().println(outtag + wrapper.getLongName() + " v" + wrapper.getVersion());

        // Retrieve the current Jenkins classloader
        ClassLoader loader = Hudson.getInstance().getPluginManager().uberClassLoader;

        // Setup the classpath necessary for Dependency-Check (only really affects when running on master node)
        Thread thread = Thread.currentThread();
        thread.setContextClassLoader(loader);

        // Node-agnostic execution of Dependency-Check
        return launcher.getChannel().call(new Callable<Boolean,IOException>() {
            public Boolean call() throws IOException {
                DependencyCheckExecutor executor = new DependencyCheckExecutor(options, listener);
                return executor.performBuild();
            }
        });
    }

    /**
     * Generate Options from build configuration preferences that will be passed to
     * the build step in DependencyCheck
     * @param build an AbstractBuild object
     * @return DependencyCheck Options
     */
    private Options generateOptions(AbstractBuild build, BuildListener listener) {
        Options options = new Options();

        // Sets the DependencyCheck application name to the Jenkins display name. If a display name
        // was not defined, it will simply return the name of the build.
        options.setName(build.getProject().getDisplayName());

        // If the configured output directory is empty, set this builds output dir to the root of the projects workspace
        FilePath outDirPath;
        if (StringUtils.isEmpty(outdir)) {
            outDirPath = build.getWorkspace();
        } else {
            outDirPath = new FilePath(build.getWorkspace(), substituteVariable(build, listener, outdir.trim()));
        }
        options.setOutputDirectory(outDirPath);

        configureDataDirectory(build, listener, options);

        // Support for multiple scan paths in a single analysis
        for (String tmpscanpath : scanpath.split(",")) {
            FilePath filePath = new FilePath(build.getWorkspace(), substituteVariable(build, listener, tmpscanpath.trim()));
            try {
                if (filePath.exists()) {
                    options.addScanPath(filePath);
                }
            } catch (Exception e) {
                // throw it away
            }
        }

        options.setAutoUpdate(!isAutoupdateDisabled);

        //todo: add proxy support
        return options;
    }

    /**
     * By default, DependencyCheck will place the 'data' directory in the same directory
     * as the DependencyCheck JAR. We need to overwrite these settings and account for
     * the fact that in a multi-node Jenkins cluster, a centralized data directory may
     * not be possible. Therefore, a subdirectory in the builds workspace is used.
     *
     * @return A boolean indicating if any errors occurred during the validation process
     */
    private boolean configureDataDirectory(AbstractBuild build, BuildListener listener, Options options) {
        FilePath dataPath;
        if (StringUtils.isEmpty(datadir)) {
            // datadir was not specified, so use the default 'dependency-check-data' directory
            // located in the builds workspace.
            dataPath = new FilePath(build.getWorkspace(), "dependency-check-data");
        } else {
            // datadir was specified. Use it, but ensure the path is relative to the builds
            // workspace by removing any path separators.
            dataPath = new FilePath(build.getWorkspace(), substituteVariable(build, listener, datadir.replaceAll("^[/|\\\\]", "")));
        }

        FilePath cpePath = new FilePath(dataPath, "cpe");
        FilePath cvePath = new FilePath(dataPath, "cve");
        try {
            if (build.getWorkspace() == null || !build.getWorkspace().exists())
                throw new IOException("Jenkins workspace directory not available. Once a build is complete, Jenkins may use the workspace to build something else, or remove it entirely.");

            options.setDataDirectory(dataPath);
            options.setCpeDataDirectory(cpePath);
            options.setCveDataDirectory(cvePath);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Replace a Jenkins environment variable in the form ${name} contained in the
     * specified String with the value of the matching environment variable.
     */
    private String substituteVariable(AbstractBuild build, BuildListener listener, String parameterizedValue) {
        try {
            if (parameterizedValue != null && parameterizedValue.contains("${")) {
                int start = parameterizedValue.indexOf("${");
                int end = parameterizedValue.indexOf("}", start);
                String parameter = parameterizedValue.substring(start + 2, end);
                String value = build.getEnvironment(listener).get(parameter);
                if (value == null) {
                    throw new IllegalStateException(parameter);
                }
                String substitutedValue = parameterizedValue.substring(0, start) + value + (parameterizedValue.length() > end + 1 ? parameterizedValue.substring(end + 1) : "");
                if (end > 0) // recursively substitute variables
                    return substituteVariable(build, listener, substitutedValue);
                else
                    return parameterizedValue;
            } else {
                return parameterizedValue;
            }
        } catch (Exception e) {
            return parameterizedValue;
        }
    }

    /**
     * A Descriptor Implementation
     */
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    /**
     * Descriptor for {@link DependencyCheckBuilder}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     * <p/>
     * <p/>
     * See <tt>src/main/resources/org/jenkinsci/plugins/DependencyCheck/DependencyCheckBuilder/*.jelly</tt>
     * for the actual HTML fragment for the configuration screen.
     */
    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        public DescriptorImpl() {
            super(DependencyCheckBuilder.class);
            load();
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types 
            return true;
        }

        /**
         * This name is used on the build configuration screen
         */
        public String getDisplayName() {
            return Messages.Builder_Name();
        }
    }
}