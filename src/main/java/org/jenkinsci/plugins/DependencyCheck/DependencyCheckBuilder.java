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
import hudson.PluginFirstClassLoader;
import hudson.PluginWrapper;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Hudson;
import hudson.remoting.Callable;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.owasp.dependencycheck.reporting.ReportGenerator;

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

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
    private final String suppressionFile;
    private final boolean isAutoupdateDisabled;
    private final boolean isVerboseLoggingEnabled;
    private final boolean includeHtmlReports;


    @DataBoundConstructor // Fields in config.jelly must match the parameter names
    public DependencyCheckBuilder(String scanpath, String outdir, String datadir, String suppressionFile,
                                  Boolean isAutoupdateDisabled, Boolean isVerboseLoggingEnabled,
                                  Boolean includeHtmlReports) {
        this.scanpath = scanpath;
        this.outdir = outdir;
        this.datadir = datadir;
        this.suppressionFile = suppressionFile;
        this.isAutoupdateDisabled = (isAutoupdateDisabled != null) && isAutoupdateDisabled;
        this.isVerboseLoggingEnabled = (isVerboseLoggingEnabled != null) && isVerboseLoggingEnabled;
        this.includeHtmlReports = (includeHtmlReports != null) && includeHtmlReports;
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
     * Retrieves the suppression file that DependencyCheck will use. This is a per-build config item.
     * This method must match the value in <tt>config.jelly</tt>.
     */
    public String getSuppressionFile() {
        return suppressionFile;
    }

    /**
     * Retrieves whether auto update should be disabled or not. This is a per-build config item.
     * This method must match the value in <tt>config.jelly</tt>.
     */
    public boolean isAutoupdateDisabled() {
        return isAutoupdateDisabled;
    }

    /**
     * Retrieves whether verbose logging is enabled or not. This is a per-build config item.
     * This method must match the value in <tt>config.jelly</tt>.
     */
    public boolean isVerboseLoggingEnabled() {
        return isVerboseLoggingEnabled;
    }

    /**
     * Retrieves whether HTML reports should be generated (in addition to the XML report) or not.
     * This is a per-build config item.
     * This method must match the value in <tt>config.jelly</tt>.
     */
    public boolean includeHtmlReports() {
        return includeHtmlReports;
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

        // Retrieve the PluginFirst classloader.
        PluginFirstClassLoader loader = (PluginFirstClassLoader)wrapper.classLoader;

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
        if (StringUtils.isBlank(outdir)) {
            outDirPath = build.getWorkspace();
        } else {
            outDirPath = new FilePath(build.getWorkspace(), substituteVariable(build, listener, outdir.trim()));
        }
        options.setOutputDirectory(outDirPath);

        if (StringUtils.isNotBlank(suppressionFile)) {
            options.setSuppressionFile(new FilePath(build.getWorkspace(), substituteVariable(build, listener, suppressionFile.trim())));
        }

        configureDataDirectory(build, listener, options);

        FilePath log = new FilePath(build.getWorkspace(), "dependency-check.log");
        //FilePath logLock = new FilePath(build.getWorkspace(), "dependency-check.log.lck");
        //deleteFilePath(log); // Uncomment to clear out the logs between builds
        //deleteFilePath(logLock);
        if (isVerboseLoggingEnabled) {
            options.setVerboseLoggingFile(log);
        }

        options.setDataMirroringType(this.getDescriptor().dataMirroringType);
        if (options.getDataMirroringType() != 0) {
            String cveUrl12Modified = this.getDescriptor().cveUrl12Modified;
            String cveUrl20Modified = this.getDescriptor().cveUrl20Modified;
            String cveUrl12Base = this.getDescriptor().cveUrl12Base;
            String cveUrl20Base = this.getDescriptor().cveUrl20Base;

            if (!StringUtils.isBlank(cveUrl12Modified) && !StringUtils.isBlank(cveUrl20Modified) &&
                    !StringUtils.isBlank(cveUrl12Base) && !StringUtils.isBlank(cveUrl20Base)) {
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

        // Nexus options
        options.setNexusAnalyzerEnabled(this.getDescriptor().isNexusAnalyzerEnabled);
        if (this.getDescriptor().isNexusAnalyzerEnabled && StringUtils.isNotBlank(this.getDescriptor().nexusUrl)) {
            try {
                options.setNexusUrl(new URL(this.getDescriptor().nexusUrl));
            } catch (MalformedURLException e) {
                // todo: need to log this or otherwise warn.
            }
        }

        options.setAutoUpdate(!isAutoupdateDisabled);

        if (includeHtmlReports) {
            options.setFormat(ReportGenerator.Format.ALL);
        }

        return options;
    }

    private boolean deleteFilePath(FilePath filePath) {
        try {
            if (filePath.exists()) {
                filePath.delete();
            }
            return true;
        } catch (Exception e) {
            return false;
        }
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
        if (StringUtils.isBlank(datadir)) {
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
            // datadir was specified.
            dataPath = new FilePath(build.getWorkspace(), substituteVariable(build, listener, datadir));
        }
        options.setDataDirectory(dataPath);
        return true;
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

        /**
         * Specifies the data mirroring type (scheme) to use
         */
        private int dataMirroringType;

        /**
         * Specifies the CVE 1.2 modified URL
         */
        private String cveUrl12Modified;

        /**
         * Specifies the CVE 2.0 modified URL
         */
        private String cveUrl20Modified;

        /**
         * Specifies the CVE 1.2 base URL
         */
        private String cveUrl12Base;

        /**
         * Specifies the CVE 2.0 base URL
         */
        private String cveUrl20Base;

        /**
         * Specifies if the Nexus analyzer should be enabled or not
         */
        private boolean isNexusAnalyzerEnabled;

        /**
         * Specifies the Nexus URL to use when enabled
         */
        private String nexusUrl;


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

        public FormValidation doCheckCveUrl12Modified(@QueryParameter String value) {
            return doCheckUrl(value);
        }

        public FormValidation doCheckCveUrl20Modified(@QueryParameter String value) {
            return doCheckUrl(value);
        }

        public FormValidation doCheckCveUrl12Base(@QueryParameter String value) {
            return doCheckUrl(value);
        }

        public FormValidation doCheckCveUrl20Base(@QueryParameter String value) {
            return doCheckUrl(value);
        }

        public FormValidation doCheckNexusUrl(@QueryParameter String value) {
            return doCheckUrl(value);
        }

        /**
         * Performs input validation when submitting the global config
         * @param value The value of the URL as specified in the global config
         * @return a FormValidation object
         */
        private FormValidation doCheckUrl(@QueryParameter String value) {
            if (StringUtils.isBlank(value))
                return FormValidation.ok();
            try {
                new URL(value);
            } catch (MalformedURLException e) {
                return FormValidation.error("The specified value is not a valid URL");
            }
            return FormValidation.ok();
        }

        /**
         * Takes the /apply/save step in the global config and saves the JSON data
         * @param req the request
         * @param formData the form data
         * @return a boolean
         * @throws FormException
         */
        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            dataMirroringType = formData.getInt("dataMirroringType");
            cveUrl12Modified = formData.getString("cveUrl12Modified");
            cveUrl20Modified = formData.getString("cveUrl20Modified");
            cveUrl12Base = formData.getString("cveUrl12Base");
            cveUrl20Base = formData.getString("cveUrl20Base");
            isNexusAnalyzerEnabled = formData.getBoolean("isNexusAnalyzerEnabled");
            nexusUrl = formData.getString("nexusUrl");
            save();
            return super.configure(req,formData);
        }

        /**
         * This method returns the global configuration for dataMirroringType.
         */
        public int getDataMirroringType() {
            return dataMirroringType;
        }

        /**
         * Returns the global configuration for CVE 1.2 modified URL.
         */
        public String getCveUrl12Modified() {
            return cveUrl12Modified;
        }

        /**
         * Returns the global configuration for CVE 2.0 modified URL.
         */
        public String getCveUrl20Modified() {
            return cveUrl20Modified;
        }

        /**
         * Returns the global configuration for CVE 1.2 base URL.
         */
        public String getCveUrl12Base() {
            return cveUrl12Base;
        }

        /**
         * Returns the global configuration for CVE 2.0 base URL.
         */
        public String getCveUrl20Base() {
            return cveUrl20Base;
        }

        /**
         * Returns the global configuration for enabling the Nexus analyzer
         */
        public boolean isNexusAnalyzerEnabled() {
            return isNexusAnalyzerEnabled;
        }

        /**
         * Returns the global configuration for the Nexus URL to use when enabled
         */
        public String getNexusUrl() {
            return nexusUrl;
        }

    }
}