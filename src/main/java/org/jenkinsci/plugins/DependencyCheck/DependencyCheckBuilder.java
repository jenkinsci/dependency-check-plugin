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
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.owasp.dependencycheck.reporting.ReportGenerator;
import javax.annotation.Nonnull;
import java.io.IOException;
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
public class DependencyCheckBuilder extends AbstractDependencyCheckBuilder {

    private static final long serialVersionUID = 5594574614031769847L;

    private final String scanpath;
    private final String outdir;
    private final String datadir;
    private final String suppressionFile;
    private final String hintsFile;
    private final String zipExtensions;
    private final boolean isAutoupdateDisabled;
    private final boolean includeHtmlReports;
    private final boolean includeJsonReports;
    private final boolean includeCsvReports;


    @DataBoundConstructor // Fields in config.jelly must match the parameter names
    public DependencyCheckBuilder(String scanpath, String outdir, String datadir, String suppressionFile,
				  String hintsFile, String zipExtensions, Boolean isAutoupdateDisabled,
				  Boolean includeHtmlReports, Boolean includeVulnReports, Boolean includeJsonReports,
                  Boolean includeCsvReports, Boolean skipOnScmChange, Boolean skipOnUpstreamChange,
                  Boolean preserveBuildSuccessOnScanFailure) {
        this.scanpath = scanpath;
        this.outdir = outdir;
        this.datadir = datadir;
        this.suppressionFile = suppressionFile;
        this.hintsFile = hintsFile;
        this.zipExtensions = zipExtensions;
        this.isAutoupdateDisabled = (isAutoupdateDisabled != null) && isAutoupdateDisabled;
        this.includeHtmlReports = (includeHtmlReports != null) && includeHtmlReports;
        this.includeJsonReports = (includeJsonReports != null) && includeJsonReports;
        this.includeCsvReports = (includeCsvReports != null) && includeCsvReports;
        this.skipOnScmChange = (skipOnScmChange != null) && skipOnScmChange;
        this.skipOnUpstreamChange = (skipOnUpstreamChange != null) && skipOnUpstreamChange;
        this.preserveBuildSuccessOnScanFailure = (preserveBuildSuccessOnScanFailure != null) && preserveBuildSuccessOnScanFailure;
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
     * Retrieves the hints file that DependencyCheck will use. This is a per-build config item.
     * This method must match the value in <tt>config.jelly</tt>.
     */
    public String getHintsFile() {
        return hintsFile;
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
    public boolean getIsAutoupdateDisabled() {
        return isAutoupdateDisabled;
    }

    /**
     * Retrieves whether HTML reports should be generated (in addition to the XML report) or not.
     * This is a per-build config item.
     * This method must match the value in <tt>config.jelly</tt>.
     */
    public boolean getIncludeHtmlReports() {
        return includeHtmlReports;
    }

    /**
     * Retrieves whether JSON reports should be generated (in addition to the XML report) or not.
     * This is a per-build config item.
     * This method must match the value in <tt>config.jelly</tt>.
     */
    public boolean getIncludeJsonReports() {
        return includeJsonReports;
    }

    /**
     * Retrieves whether CSV reports should be generated (in addition to the XML report) or not.
     * This is a per-build config item.
     * This method must match the value in <tt>config.jelly</tt>.
     */
    public boolean getIncludeCsvReports() {
        return includeCsvReports;
    }

    /**
     * Retrieves whether execution of the builder should be skipped if triggered by an SCM change.
     * This is a per-build config item.
     * This method must match the value in <tt>config.jelly</tt>.
     */
    public boolean getSkipOnScmChange() {
        return skipOnScmChange;
    }

    /**
     * Retrieves whether execution of the builder should be skipped if triggered by an Upstream change.
     * This is a per-build config item.
     * This method must match the value in <tt>config.jelly</tt>.
     */
    public boolean getSkipOnUpstreamChange() {
        return skipOnUpstreamChange;
    }

    /**
     * Retrieves whether build result should set to failure on core execution failure or not.
     * This is a per-build config item.
     * This method must match the value in <tt>config.jelly</tt>.
     */
    public boolean getPreserveBuildSuccessOnScanFailure() {
        return preserveBuildSuccessOnScanFailure;
    }

    /**
     * This method is called whenever the DependencyCheck build step is executed.
     */
   @Override
    public void perform(@Nonnull final Run<?, ?> build,
                        @Nonnull final FilePath workspace,
                        @Nonnull final Launcher launcher,
                        @Nonnull final TaskListener listener) throws InterruptedException, IOException {

       final JobOptions jobOptions = generateJobOptions(build, workspace, listener);
       final GlobalOptions globalOptions = generateGlobalOptions(build, workspace, listener);
       setJobOptions(jobOptions);
       setGlobalOptions(globalOptions);
       super.perform(build, workspace, launcher, listener);
    }

    /**
     * Generate Options from build configuration preferences that will be passed to
     * the build step in DependencyCheck
     * @return DependencyCheck Options
     */
    private JobOptions generateJobOptions(final Run<?, ?> build, final FilePath workspace, final TaskListener listener) {
        // Generate Options object with universal settings necessary for all Builder steps
        final JobOptions jobOptions = jobOptionsBuilder(build, workspace, listener, outdir);
        // Configure universal settings useful for all Builder steps
        configureDataDirectory(build, workspace, listener, jobOptions, this.getDescriptor().getGlobalDataDirectory(), datadir);
        // Support for multiple scan paths in a single analysis
        for (String tmpscanpath : scanpath.split(",")) {
            final FilePath filePath = new FilePath(workspace, PluginUtil.substituteVariable(build, listener, tmpscanpath.trim()));
            jobOptions.addScanPath(filePath.getRemote());
        }
        // SUPPRESSION FILE
        if (StringUtils.isNotBlank(suppressionFile)) {
            String tmpSuppressionFile = PluginUtil.substituteVariable(build, listener, suppressionFile.trim());
            try {
                // Try to set the suppression file as a URL
                jobOptions.setSuppressionFile(new URL(tmpSuppressionFile).toExternalForm());
            } catch (MalformedURLException e) {
                // If the format is not a valid URL, set it as a FilePath type
                jobOptions.setSuppressionFile(new FilePath(workspace, tmpSuppressionFile).getRemote());
            }
        }
        // HINTS FILE
        if (StringUtils.isNotBlank(hintsFile)) {
            String tmpHintsFile = PluginUtil.substituteVariable(build, listener, hintsFile.trim());
            try {
                // Try to set the hints file as a URL
                jobOptions.setHintsFile(new URL(tmpHintsFile).toExternalForm());
            } catch (MalformedURLException e) {
                // If the format is not a valid URL, set it as a FilePath type
                jobOptions.setHintsFile(new FilePath(workspace, tmpHintsFile).getRemote());
            }
        }
        if (StringUtils.isNotBlank(zipExtensions)) {
            jobOptions.setZipExtensions(toCommaSeparatedString(zipExtensions));
        }
        jobOptions.setAutoUpdate(!isAutoupdateDisabled);
        if (includeHtmlReports) {
            jobOptions.addFormat(ReportGenerator.Format.HTML);
        }
        if (includeJsonReports) {
            jobOptions.addFormat(ReportGenerator.Format.JSON);
        }
        if (includeCsvReports) {
            jobOptions.addFormat(ReportGenerator.Format.CSV);
        }
        return jobOptions;
    }

    /**
     * Generate Options from build configuration preferences that will be passed to
     * the build step in DependencyCheck
     * @return DependencyCheck Options
     */
    private GlobalOptions generateGlobalOptions(final Run<?, ?> build, final FilePath workspace, final TaskListener listener) {
        // Generate Options object with universal settings necessary for all Builder steps
        final GlobalOptions globalOptions = globalOptionsBuilder(build, listener, this.getDescriptor().getTempPath(), this.getDescriptor().isQuickQueryTimestampEnabled);
        configureDataMirroring(globalOptions, this.getDescriptor().getDataMirroringType(),
                this.getDescriptor().getCveUrl12Modified(), this.getDescriptor().getCveUrl20Modified(),
                this.getDescriptor().getCveUrl12Base(), this.getDescriptor().getCveUrl20Base(),
                this.getDescriptor().getRetireJsRepoJsUrl()
        );
        configureProxySettings(globalOptions, this.getDescriptor().getIsNvdProxyBypassed());
	    // SETUP DB CONNECTION
        if (StringUtils.isNotBlank(this.getDescriptor().dbconnstr)) {
            globalOptions.setDbconnstr(this.getDescriptor().dbconnstr);
        }
        if (StringUtils.isNotBlank(this.getDescriptor().dbdriver)) {
            globalOptions.setDbdriver(this.getDescriptor().dbdriver);
        }
        if (StringUtils.isNotBlank(this.getDescriptor().dbpath)) {
            globalOptions.setDbpath(this.getDescriptor().dbpath);
        }
        if (StringUtils.isNotBlank(this.getDescriptor().dbuser)) {
            globalOptions.setDbuser(this.getDescriptor().dbuser);
        }
        if (StringUtils.isNotBlank(this.getDescriptor().dbpassword)) {
            globalOptions.setDbpassword(this.getDescriptor().dbpassword);
        }
        // Enable/Disable Analyzers
        globalOptions.setJarAnalyzerEnabled(this.getDescriptor().isJarAnalyzerEnabled);
        globalOptions.setNodePackageAnalyzerEnabled(this.getDescriptor().isNodePackageAnalyzerEnabled);
        globalOptions.setNodeAuditAnalyzerEnabled(this.getDescriptor().isNodeAuditAnalyzerEnabled);
        globalOptions.setRetireJsAnalyzerEnabled(this.getDescriptor().isRetireJsAnalyzerEnabled);
        globalOptions.setComposerLockAnalyzerEnabled(this.getDescriptor().isComposerLockAnalyzerEnabled);
        globalOptions.setPythonDistributionAnalyzerEnabled(this.getDescriptor().isPythonDistributionAnalyzerEnabled);
        globalOptions.setPythonPackageAnalyzerEnabled(this.getDescriptor().isPythonPackageAnalyzerEnabled);
        globalOptions.setRubyBundlerAuditAnalyzerEnabled(this.getDescriptor().isRubyBundlerAuditAnalyzerEnabled);
        globalOptions.setRubyGemAnalyzerEnabled(this.getDescriptor().isRubyGemAnalyzerEnabled);
        globalOptions.setCocoaPodsAnalyzerEnabled(this.getDescriptor().isCocoaPodsAnalyzerEnabled);
        globalOptions.setSwiftPackageManagerAnalyzerEnabled(this.getDescriptor().isSwiftPackageManagerAnalyzerEnabled);
        globalOptions.setArchiveAnalyzerEnabled(this.getDescriptor().isArchiveAnalyzerEnabled);
        globalOptions.setAssemblyAnalyzerEnabled(this.getDescriptor().isAssemblyAnalyzerEnabled);
        globalOptions.setMsBuildProjectAnalyzerEnabled(this.getDescriptor().isMsBuildProjectAnalyzerEnabled);
        globalOptions.setNuGetConfigAnalyzerEnabled(this.getDescriptor().isNuGetConfigAnalyzerEnabled);
        globalOptions.setNuspecAnalyzerEnabled(this.getDescriptor().isNuspecAnalyzerEnabled);
        globalOptions.setNexusAnalyzerEnabled(this.getDescriptor().isNexusAnalyzerEnabled);
        globalOptions.setArtifactoryAnalyzerEnabled(this.getDescriptor().isArtifactoryAnalyzerEnabled);
        globalOptions.setAutoconfAnalyzerEnabled(this.getDescriptor().isAutoconfAnalyzerEnabled);
        globalOptions.setCmakeAnalyzerEnabled(this.getDescriptor().isCmakeAnalyzerEnabled);
        globalOptions.setOpensslAnalyzerEnabled(this.getDescriptor().isOpensslAnalyzerEnabled);
        // Nexus options
        if (this.getDescriptor().isNexusAnalyzerEnabled && StringUtils.isNotBlank(this.getDescriptor().nexusUrl)) {
            try {
                globalOptions.setNexusUrl(new URL(this.getDescriptor().nexusUrl));
            } catch (MalformedURLException e) {
                // todo: need to log this or otherwise warn.
            }
            globalOptions.setNexusProxyBypassed(this.getDescriptor().isNexusProxyBypassed);
        }

        // Maven Central options
        globalOptions.setCentralAnalyzerEnabled(this.getDescriptor().isCentralAnalyzerEnabled);
        try {
            globalOptions.setCentralUrl(new URL("http://search.maven.org/solrsearch/select"));
        } catch (MalformedURLException e) {
            // todo: need to log this or otherwise warn.
        }

        // Artifactory options
        if (this.getDescriptor().isArtifactoryAnalyzerEnabled && StringUtils.isNotBlank(this.getDescriptor().artifactoryUrl)) {
            try {
                globalOptions.setArtifactoryUrl(new URL(this.getDescriptor().artifactoryUrl));
            } catch (MalformedURLException e) {
                // todo: need to log this or otherwise warn.
            }
            globalOptions.setArtifactoryProxyBypassed(this.getDescriptor().isArtifactoryProxyBypassed);
            globalOptions.setArtifactoryApiToken(this.getDescriptor().artifactoryApiToken);
            globalOptions.setArtifactoryApiUsername(this.getDescriptor().artifactoryApiUsername);
            globalOptions.setArtifactoryBearerToken(this.getDescriptor().artifactoryBearerToken);
        }

        // Only set the Mono path if running on non-Windows systems.
        if (!SystemUtils.IS_OS_WINDOWS && StringUtils.isNotBlank(this.getDescriptor().monoPath)) {
            globalOptions.setMonoPath(this.getDescriptor().monoPath);
        }

        if (StringUtils.isNotBlank(this.getDescriptor().bundleAuditPath)) {
            globalOptions.setBundleAuditPath(this.getDescriptor().bundleAuditPath);
        }
        return globalOptions;
    }

    /**
     * A Descriptor Implementation.
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
    @Extension @Symbol("dependencyCheckAnalyzer") // This indicates to Jenkins that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        /**
         * Specifies the data mirroring type (scheme) to use
         */
        private int dataMirroringType;

        /**
         * Specifies the custom database connection string
         */
        private String dbconnstr;

        /**
         * Specifies the custom database driver name
         */
        private String dbdriver;

        /**
         * Specifies the custom database driver path
         */
        private String dbpath;

        /**
         * Specifies the custom database login user
         */
        private String dbuser;

        /**
         * Specifies the custom database login password
         */
        private String dbpassword;

        /**
         * Specifies that Jenkins web proxy settings should be ignored (for NVD or any other internet request).
         */
        private boolean isNvdProxyBypassed = false;

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
         * Specifies the URL to the Javascript feed for Retire.js
         */
        private String retireJsRepoJsUrl;

        /**
         * Specifies if the Jar analyzer should be enabled or not
         */
        private boolean isJarAnalyzerEnabled = true;

        /**
         * Specifies if the Node Package analyzer should be enabled or not
         */
        private boolean isNodePackageAnalyzerEnabled = true;

        /**
         * Specifies if the Node Audit analyzer should be enabled or not
         */
        private boolean isNodeAuditAnalyzerEnabled = true;

        /**
         * Specifies if the RetireJS analyzer should be enabled or not
         */
        private boolean isRetireJsAnalyzerEnabled = true;

        /**
         * Specifies if the PHP Composer.lock analyzer should be enabled or not
         */
        private boolean isComposerLockAnalyzerEnabled = true;

        /**
         * Specifies if the Python distribution analyzer should be enabled or not
         */
        private boolean isPythonDistributionAnalyzerEnabled = true;

        /**
         * Specifies if the Python package analyzer should be enabled or not
         */
        private boolean isPythonPackageAnalyzerEnabled = true;

        /**
         * Specifies if the Ruby Bundler Audit analyzer should be enabled or not
         */
        private boolean isRubyBundlerAuditAnalyzerEnabled = false;

        /**
         * Specifies if the Ruby Gem analyzer should be enabled or not
         */
        private boolean isRubyGemAnalyzerEnabled = true;

        /**
         * Specifies if the CocoaPods analyzer should be enabled or not
         */
        private boolean isCocoaPodsAnalyzerEnabled = true;

        /**
         * Specifies if the Swift Package Manager analyzer should be enabled or not
         */
        private boolean isSwiftPackageManagerAnalyzerEnabled = true;

        /**
         * Specifies if the archive analyzer should be enabled or not
         */
        private boolean isArchiveAnalyzerEnabled = true;

        /**
         * Specifies if the assembly analyzer should be enabled or not
         */
        private boolean isAssemblyAnalyzerEnabled = true;

        /**
         * Specifies if the NuSpec analyzer should be enabled or not
         */
        private boolean isNuspecAnalyzerEnabled = true;

        /**
         * Specifies if the Maven Central analyzer should be enabled or not
         */
        private boolean isCentralAnalyzerEnabled = true;

        /**
         * Specifies if the Nexus analyzer should be enabled or not
         */
        private boolean isNexusAnalyzerEnabled = false;

        /**
         * Specifies if the MS Build Project analyzer should be enabled or not
         */
        private boolean isMsBuildProjectAnalyzerEnabled = true;

        /**
         * Specifies if the NuGet Config analyzer should be enabled or not
         */
        private boolean isNuGetConfigAnalyzerEnabled = true;

        /**
         * Specifies if the autoconf analyzer should be enabled or not
         */
        private boolean isAutoconfAnalyzerEnabled = true;

        /**
         * Specifies if the cmake analyzer should be enabled or not
         */
        private boolean isCmakeAnalyzerEnabled = true;

        /**
         * Specifies if the OpenSSL analyzer should be enabled or not
         */
        private boolean isOpensslAnalyzerEnabled = true;

        /**
         * Specifies the Nexus URL to use when enabled
         */
        private String nexusUrl;

        /**
         * Specifies if the Nexus analyzer should bypass any proxy defined in Jenkins
         */
        private boolean isNexusProxyBypassed;

        /**
         * Specifies if the Artifactory analyzer should be enabled or not
         */
        private boolean isArtifactoryAnalyzerEnabled = false;

        /**
         * Specifies the Artifactory URL to use when enabled
         */
        private String artifactoryUrl;

        /**
         * Specifies if the Artifactory analyzer should bypass any proxy defined in Jenkins
         */
        private boolean isArtifactoryProxyBypassed;

        /**
         * Specifies the Artifactory API token to use when enabled
         */
        private String artifactoryApiToken;

        /**
         * Specifies the Artifactory API username to use when enabled
         */
        private String artifactoryApiUsername;

        /**
         * Specifies the Artifactory bearer token to use when enabled
         */
        private String artifactoryBearerToken;

        /**
         * Specifies the full path and filename to the Mono binary
         */
        private String monoPath;

        /**
         * Specifies the full path and filename to the bundle-audit binary
         */
        private String bundleAuditPath;

        /**
         * Specifies the full path to the temporary directory
         */
        private String tempPath;

        /**
         * Specifies the global data directory
         */
        private String globalDataDirectory;

        /**
         * Specifies if QuickQuery is enabled or not. If enabled, HTTP HEAD will be used.
         */
        private boolean isQuickQueryTimestampEnabled = true;

        public DescriptorImpl() {
            super(DependencyCheckBuilder.class);
            load();
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types
            return true;
        }

        /**
         * This name is used on the build configuration screen.
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
            return PluginUtil.doCheckUrl(value);
        }

        public FormValidation doCheckCveUrl20Modified(@QueryParameter String value) {
            return PluginUtil.doCheckUrl(value);
        }

        public FormValidation doCheckCveUrl12Base(@QueryParameter String value) {
            return PluginUtil.doCheckUrl(value);
        }

        public FormValidation doCheckCveUrl20Base(@QueryParameter String value) {
            return PluginUtil.doCheckUrl(value);
        }

        public FormValidation doCheckRetireJsRepoJsUrl(@QueryParameter String value) {
            return PluginUtil.doCheckUrl(value);
        }

        public FormValidation doCheckNexusUrl(@QueryParameter String value) {
            return PluginUtil.doCheckUrl(value);
        }

        public FormValidation doCheckArtifactoryUrl(@QueryParameter String value) {
            return PluginUtil.doCheckUrl(value);
        }

        public FormValidation doCheckMonoPath(@QueryParameter String value) {
            return PluginUtil.doCheckPath(value);
        }

        public FormValidation doCheckTempPath(@QueryParameter String value) {
            return PluginUtil.doCheckPath(value);
        }

        /**
         * Takes the /apply/save step in the global config and saves the JSON data.
         * @param req the request
         * @param formData the form data
         * @return a boolean
         * @throws FormException
         */
        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            dataMirroringType = formData.getInt("dataMirroringType");
            isNvdProxyBypassed = formData.getBoolean("isNvdProxyBypassed");
            cveUrl12Modified = formData.getString("cveUrl12Modified");
            cveUrl20Modified = formData.getString("cveUrl20Modified");
            cveUrl12Base = formData.getString("cveUrl12Base");
            cveUrl20Base = formData.getString("cveUrl20Base");
            retireJsRepoJsUrl = formData.getString("retireJsRepoJsUrl");
            isJarAnalyzerEnabled = formData.getBoolean("isJarAnalyzerEnabled");
            isNodePackageAnalyzerEnabled = formData.getBoolean("isNodePackageAnalyzerEnabled");
            isNodeAuditAnalyzerEnabled = formData.getBoolean("isNodeAuditAnalyzerEnabled");
            isRetireJsAnalyzerEnabled = formData.getBoolean("isRetireJsAnalyzerEnabled");
            isComposerLockAnalyzerEnabled = formData.getBoolean("isComposerLockAnalyzerEnabled");
            isPythonDistributionAnalyzerEnabled = formData.getBoolean("isPythonDistributionAnalyzerEnabled");
            isPythonPackageAnalyzerEnabled = formData.getBoolean("isPythonPackageAnalyzerEnabled");
            isRubyBundlerAuditAnalyzerEnabled = formData.getBoolean("isRubyBundlerAuditAnalyzerEnabled");
            isRubyGemAnalyzerEnabled = formData.getBoolean("isRubyGemAnalyzerEnabled");
            isCocoaPodsAnalyzerEnabled = formData.getBoolean("isCocoaPodsAnalyzerEnabled");
            isSwiftPackageManagerAnalyzerEnabled = formData.getBoolean("isSwiftPackageManagerAnalyzerEnabled");
            isArchiveAnalyzerEnabled = formData.getBoolean("isArchiveAnalyzerEnabled");
            isAssemblyAnalyzerEnabled = formData.getBoolean("isAssemblyAnalyzerEnabled");
            isCentralAnalyzerEnabled = formData.getBoolean("isCentralAnalyzerEnabled");
            isNuspecAnalyzerEnabled = formData.getBoolean("isNuspecAnalyzerEnabled");
            isNexusAnalyzerEnabled = formData.getBoolean("isNexusAnalyzerEnabled");
            isMsBuildProjectAnalyzerEnabled = formData.getBoolean("isMsBuildProjectAnalyzerEnabled");
            isNuGetConfigAnalyzerEnabled = formData.getBoolean("isNuGetConfigAnalyzerEnabled");
            isArtifactoryAnalyzerEnabled = formData.getBoolean("isArtifactoryAnalyzerEnabled");
            artifactoryUrl = formData.getString("artifactoryUrl");
            isArtifactoryProxyBypassed = formData.getBoolean("isArtifactoryProxyBypassed");
            artifactoryApiToken = formData.getString("artifactoryApiToken");
            artifactoryApiUsername = formData.getString("artifactoryApiUsername");
            artifactoryBearerToken = formData.getString("artifactoryBearerToken");
            isAutoconfAnalyzerEnabled = formData.getBoolean("isAutoconfAnalyzerEnabled");
            isCmakeAnalyzerEnabled = formData.getBoolean("isCmakeAnalyzerEnabled");
            isOpensslAnalyzerEnabled = formData.getBoolean("isOpensslAnalyzerEnabled");
            nexusUrl = formData.getString("nexusUrl");
            isNexusProxyBypassed = formData.getBoolean("isNexusProxyBypassed");
            monoPath = formData.getString("monoPath");
            bundleAuditPath = formData.getString("bundleAuditPath");
            dbconnstr = formData.getString("dbconnstr");
            dbdriver = formData.getString("dbdriver");
            dbpath = formData.getString("dbpath");
            dbuser = formData.getString("dbuser");
            dbpassword = formData.getString("dbpassword");
            tempPath = formData.getString("tempPath");
            globalDataDirectory = formData.getString("globalDataDirectory");
            isQuickQueryTimestampEnabled = formData.getBoolean("isQuickQueryTimestampEnabled");
            save();
            return super.configure(req, formData);
        }

        /**
         * This method returns the global configuration for dataMirroringType.
         */
        public int getDataMirroringType() {
            return dataMirroringType;
        }

        /**
         * Returns the global configuration to determine if Jenkins web proxy settings should be ignored.
         */
        public boolean getIsNvdProxyBypassed() {
            return isNvdProxyBypassed;
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
         * Returns the global configuration for the URL to the Javascript feed for Retire.js.
         */
        public String getRetireJsRepoJsUrl() {
            return retireJsRepoJsUrl;
        }

        /**
         * Returns the global configuration for enabling the Jar analyzer.
         */
        public boolean getIsJarAnalyzerEnabled() {
            return isJarAnalyzerEnabled;
        }

        /**
         * Returns the global configuration for enabling the Node Package analyzer.
         */
        public boolean getIsNodePackageAnalyzerEnabled() {
            return isNodePackageAnalyzerEnabled;
        }

        /**
         * Returns the global configuration for enabling the Node Audit analyzer.
         */
        public boolean getIsNodeAuditAnalyzerEnabled() {
            return isNodeAuditAnalyzerEnabled;
        }

        /**
         * Returns the global configuration for enabling the RetireJS analyzer.
         */
        public boolean getIsRetireJsAnalyzerEnabled() {
            return isRetireJsAnalyzerEnabled;
        }

        /**
         * Returns the global configuration for enabling the PHP Composer.lock analyzer.
         */
        public boolean getIsComposerLockAnalyzerEnabled() {
            return isComposerLockAnalyzerEnabled;
        }

        /**
         * Returns the global configuration for enabling the Python distribution analyzer.
         */
        public boolean getIsPythonDistributionAnalyzerEnabled() {
            return isPythonDistributionAnalyzerEnabled;
        }

        /**
         * Returns the global configuration for enabling the Python package analyzer.
         */
        public boolean getIsPythonPackageAnalyzerEnabled() {
            return isPythonPackageAnalyzerEnabled;
        }

        /**
         * Returns the global configuration for enabling the Ruby Bundler Audit analyzer.
         */
        public boolean getIsRubyBundlerAuditAnalyzerEnabled() {
            return isRubyBundlerAuditAnalyzerEnabled;
        }

        /**
         * Returns the global configuration for enabling the Ruby Gem analyzer.
         */
        public boolean getIsRubyGemAnalyzerEnabled() {
            return isRubyGemAnalyzerEnabled;
        }

        /**
         * Returns the global configuration for enabling the CocoaPods analyzer.
         */
        public boolean getIsCocoaPodsAnalyzerEnabled() {
            return isCocoaPodsAnalyzerEnabled;
        }

        /**
         * Returns the global configuration for enabling the Swift Package Manager analyzer.
         */
        public boolean getIsSwiftPackageManagerAnalyzerEnabled() {
            return isSwiftPackageManagerAnalyzerEnabled;
        }

        /**
         * Returns the global configuration for enabling the Archive analyzer.
         */
        public boolean getIsArchiveAnalyzerEnabled() {
            return isArchiveAnalyzerEnabled;
        }

        /**
         * Returns the global configuration for enabling the Assembly analyzer.
         */
        public boolean getIsAssemblyAnalyzerEnabled() {
            return isAssemblyAnalyzerEnabled;
        }

        /**
         * Returns the global configuration for enabling the NuSpec analyzer.
         */
        public boolean getIsNuspecAnalyzerEnabled() {
            return isNuspecAnalyzerEnabled;
        }

        /**
         * Returns the global configuration for enabling the Maven Central analyzer.
         */
        public boolean getIsCentralAnalyzerEnabled() {
            return isCentralAnalyzerEnabled;
        }

        /**
         * Returns the global configuration for enabling the Nexus analyzer.
         */
        public boolean getIsNexusAnalyzerEnabled() {
            return isNexusAnalyzerEnabled;
        }

        /**
         * Returns the global configuration for enabling the MS Build Project analyzer.
         */
        public boolean getIsMsBuildProjectAnalyzerEnabled() {
            return isMsBuildProjectAnalyzerEnabled;
        }

        /**
         * Returns the global configuration for enabling the NuGet Config analyzer.
         */
        public boolean getIsNuGetConfigAnalyzerEnabled() {
            return isNuGetConfigAnalyzerEnabled;
        }

        /**
         * Returns the global configuration for enabling the autoconf analyzer.
         */
        public boolean getIsAutoconfAnalyzerEnabled() {
            return isAutoconfAnalyzerEnabled;
        }

        /**
         * Returns the global configuration for enabling the cmake analyzer.
         */
        public boolean getIsCmakeAnalyzerEnabled() {
            return isCmakeAnalyzerEnabled;
        }

        /**
         * Returns the global configuration for enabling the OpenSSL analyzer.
         */
        public boolean getIsOpensslAnalyzerEnabled() {
            return isOpensslAnalyzerEnabled;
        }

        /**
         * Returns the global configuration for the Nexus URL to use when enabled.
         */
        public String getNexusUrl() {
            return nexusUrl;
        }

        /**
         * Returns the global configuration to determine if the Nexus analyzer should bypass any proxy defined in Jenkins.
         */
        public boolean getIsNexusProxyBypassed() {
            return isNexusProxyBypassed;
        }

        /**
         * Returns the global configuration for enabling the Artifactory analyzer.
         */
        public boolean getIsArtifactoryAnalyzerEnabled() {
            return isArtifactoryAnalyzerEnabled;
        }

        /**
         * Returns the global configuration for the Artifactory URL to use when enabled.
         */
        public String getArtifactoryUrl() {
            return artifactoryUrl;
        }

        /**
         * Returns the global configuration to determine if the Artifactory analyzer should bypass any proxy defined in Jenkins.
         */
        public boolean getIsArtifactoryProxyBypassed() {
            return isArtifactoryProxyBypassed;
        }

        /**
         * Returns the global configuration for the Artifactory API token to use when enabled.
         */
        public String getArtifactoryApiToken() {
            return artifactoryApiToken;
        }

        /**
         * Returns the global configuration for the Artifactory API username to use when enabled.
         */
        public String getArtifactoryApiUsername() {
            return artifactoryApiUsername;
        }

        /**
         * Returns the global configuration for the Artifactory bearer token to use when enabled.
         */
        public String getArtifactoryBearerToken() {
            return artifactoryBearerToken;
        }

        /**
         * Returns the global configuration for the path and filename for the mono binary.
         */
        public String getMonoPath() {
            return monoPath;
        }

        /**
         * Returns the global configuration for the path and filename for the bundle-audit binary.
         */
        public String getBundleAuditPath() {
            return bundleAuditPath;
        }

        /**
         * Returns the global configuration for the path to the temporary directory.
         */
        public String getTempPath() {
            return tempPath;
        }

        /**
         * Returns the global configuration for the path to the data directory.
         */
        public String getGlobalDataDirectory() {
            return globalDataDirectory;
        }

        /**
         * Retrieves the database connection string that DependencyCheck will use. This is a per-build config item.
         * This method must match the value in <tt>config.jelly</tt>.
         */
        public String getDbconnstr() {
            return dbconnstr;
        }

        /**
         * Retrieves the database driver name that DependencyCheck will use. This is a per-build config item.
         * This method must match the value in <tt>config.jelly</tt>.
         */
        public String getDbdriver() {
            return dbdriver;
        }

        /**
         * Retrieves the database driver path that DependencyCheck will use. This is a per-build config item.
         * This method must match the value in <tt>config.jelly</tt>.
         */
        public String getDbpath() {
            return dbpath;
        }

        /**
         * Retrieves the database user that DependencyCheck will use. This is a per-build config item.
         * This method must match the value in <tt>config.jelly</tt>.
         */
        public String getDbuser() {
            return dbuser;
        }

        /**
         * Retrieves the database password that DependencyCheck will use. This is a per-build config item.
         * This method must match the value in <tt>config.jelly</tt>.
         */
        public String getDbpassword() {
            return dbpassword;
        }

       /**
         * Returns if QuickQuery is enabled or not. If enabled, HTTP HEAD will be used.
         */
        public boolean getIsQuickQueryTimestampEnabled() {
            return isQuickQueryTimestampEnabled;
        }

    }
}
