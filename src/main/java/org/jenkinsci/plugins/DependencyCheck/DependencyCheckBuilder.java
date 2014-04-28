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

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.PluginWrapper;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Cause;
import hudson.model.Hudson;
import hudson.remoting.Callable;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.triggers.SCMTrigger;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.owasp.dependencycheck.reporting.ReportGenerator;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

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
    private final String zipExtensions;
    private final boolean isAutoupdateDisabled;
    private final boolean isVerboseLoggingEnabled;
    private final boolean includeHtmlReports;
    private final boolean skipOnScmChange;
    private final boolean skipOnUpstreamChange;

    private static final String OUT_TAG = "[" + DependencyCheckPlugin.PLUGIN_NAME+"] ";


    @DataBoundConstructor // Fields in config.jelly must match the parameter names
    public DependencyCheckBuilder(String scanpath, String outdir, String datadir, String suppressionFile,
                                  String zipExtensions, Boolean isAutoupdateDisabled, Boolean isVerboseLoggingEnabled,
                                  Boolean includeHtmlReports, Boolean skipOnScmChange, Boolean skipOnUpstreamChange) {
        this.scanpath = scanpath;
        this.outdir = outdir;
        this.datadir = datadir;
        this.suppressionFile = suppressionFile;
        this.zipExtensions = zipExtensions;
        this.isAutoupdateDisabled = (isAutoupdateDisabled != null) && isAutoupdateDisabled;
        this.isVerboseLoggingEnabled = (isVerboseLoggingEnabled != null) && isVerboseLoggingEnabled;
        this.includeHtmlReports = (includeHtmlReports != null) && includeHtmlReports;
        this.skipOnScmChange = (skipOnScmChange != null) && skipOnScmChange;
        this.skipOnUpstreamChange = (skipOnUpstreamChange != null) && skipOnUpstreamChange;
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
     * Retrieves a comma-separated list of file extensions to treat as ZIP format. This is a per-build config item.
     * This method must match the value in <tt>config.jelly</tt>.
     */
    public String getZipExtensions() {
        return zipExtensions;
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
     * Retrieves whether execution of the builder should be skipped if triggered by an SCM change.
     * This is a per-build config item.
     * This method must match the value in <tt>config.jelly</tt>.
     */
    public boolean skipOnScmChange() {
        return skipOnScmChange;
    }

    /**
     * Retrieves whether execution of the builder should be skipped if triggered by an Upstream change.
     * This is a per-build config item.
     * This method must match the value in <tt>config.jelly</tt>.
     */
    public boolean skipOnUpstreamChange() {
        return skipOnUpstreamChange;
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

        // Generate the Dependency-Check options - later used by DependencyCheckExecutor
        final Options options = generateOptions(build, listener);

        // Get the version of the plugin and print it out
        PluginWrapper wrapper = Hudson.getInstance().getPluginManager().getPlugin(DependencyCheckDescriptor.PLUGIN_ID);
        listener.getLogger().println(OUT_TAG + wrapper.getLongName() + " v" + wrapper.getVersion());

        final ClassLoader loader = wrapper.classLoader;
        final boolean isMaster = (build.getBuiltOn() == Hudson.getInstance());

        // Node-agnostic execution of Dependency-Check
        if (isMaster) {
            return launcher.getChannel().call(new Callable<Boolean, IOException>() {
                public Boolean call() throws IOException {
                    DependencyCheckExecutor executor = new DependencyCheckExecutor(options, listener, loader);
                    return executor.performBuild();
                }
            });
        } else {
            return launcher.getChannel().call(new Callable<Boolean, IOException>() {
                public Boolean call() throws IOException {
                    DependencyCheckExecutor executor = new DependencyCheckExecutor(options, listener);
                    return executor.performBuild();
                }
            });
        }
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
        List<Cause> causes = build.getCauses();
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
            try {
                // Try to set the suppression file as a URL
                options.setSuppressionFile(new URL(suppressionFile.trim()));
            } catch (MalformedURLException e) {
                // If the format is not a valid URL, set it as a FilePath type
                options.setSuppressionFile(new FilePath(build.getWorkspace(), substituteVariable(build, listener, suppressionFile.trim())));
            }
        }

        if (StringUtils.isNotBlank(zipExtensions)) {
            options.setZipExtensions(toCommaSeparatedString(zipExtensions));
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
            options.addScanPath(filePath);
        }

        // Nexus options
        options.setNexusAnalyzerEnabled(this.getDescriptor().isNexusAnalyzerEnabled);
        if (this.getDescriptor().isNexusAnalyzerEnabled && StringUtils.isNotBlank(this.getDescriptor().nexusUrl)) {
            try {
                options.setNexusUrl(new URL(this.getDescriptor().nexusUrl));
            } catch (MalformedURLException e) {
                // todo: need to log this or otherwise warn.
            }
            options.setNexusProxyBypassed(this.getDescriptor().isNexusProxyBypassed);
        }

        // Only set the Mono path if running on non-Windows systems.
        if (!SystemUtils.IS_OS_WINDOWS && StringUtils.isNotBlank(this.getDescriptor().monoPath)) {
            options.setMonoPath(new FilePath(new File(this.getDescriptor().monoPath)));
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

    private String toCommaSeparatedString(String input) {
        input = input.trim();
        input = input.replaceAll(" +", ",");
        return input;
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

        /**
         * Specifies if the Nexus analyzer should bypass any proxy defined in Jenkins
         */
        private boolean isNexusProxyBypassed;

        /**
         * Specifies the full path and filename to the Mono binary
         */
        private String monoPath;

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

        public FormValidation doCheckZipExtensions(@QueryParameter String value) {
            if (StringUtils.isNotBlank(value) && !value.matches("(\\w+)(,\\s*\\w+)*")) {
                return FormValidation.error("Please enter an extension, or a comma-separated list of extensions.");
            }
            return FormValidation.ok();
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

        public FormValidation doCheckMonoPath(@QueryParameter String value) {
            return doCheckPath(value);
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
         * Performs input validation when submitting the global config
         * @param value The value of the path as specified in the global config
         * @return a FormValidation object
         */
        private FormValidation doCheckPath(@QueryParameter String value) {
            if (StringUtils.isBlank(value))
                return FormValidation.ok();
            try {
                FilePath filePath = new FilePath(new File(value));
                filePath.exists();
            } catch (Exception e) {
                return FormValidation.error("The specified value is not a valid path");
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
            isNexusProxyBypassed = formData.getBoolean("isNexusProxyBypassed");
            monoPath = formData.getString("monoPath");
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
        public boolean getIsNexusAnalyzerEnabled() {
            return isNexusAnalyzerEnabled;
        }

        /**
         * Returns the global configuration for the Nexus URL to use when enabled
         */
        public String getNexusUrl() {
            return nexusUrl;
        }

        /**
         * Returns the global configuration to determine if the Nexus analyzer should bypass any proxy defined in Jenkins
         */
        public boolean getIsNexusProxyBypassed() {
            return isNexusProxyBypassed;
        }

        /**
         * Returns the global configuration for the path and filename for the mono binary
         */
        public String getMonoPath() {
            return monoPath;
        }

    }
}