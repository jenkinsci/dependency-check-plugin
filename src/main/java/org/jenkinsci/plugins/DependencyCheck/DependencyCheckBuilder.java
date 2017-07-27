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
    private final boolean isFailOnErrorDisabled;

    @DataBoundConstructor // Fields in config.jelly must match the parameter names
    public DependencyCheckBuilder(String scanpath, String outdir, String datadir, String suppressionFile,
				  String hintsFile, String zipExtensions, Boolean isAutoupdateDisabled,
				  Boolean includeHtmlReports, Boolean includeJsonReports, Boolean includeCsvReports,
                  Boolean skipOnScmChange, Boolean skipOnUpstreamChange, Boolean isFailOnErrorDisabled) {
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
        this.isFailOnErrorDisabled = (isFailOnErrorDisabled != null) && isFailOnErrorDisabled;
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
     * Retrieves whether build failure on error should be disabled or not.
     * This is a per-build config item.
     * This method must match the value in <tt>config.jelly</tt>.
     */
    public boolean getIsFailOnErrorDisabled() { return isFailOnErrorDisabled; }

    /**
     * This method is called whenever the DependencyCheck build step is executed.
     */
   @Override
    public void perform(@Nonnull final Run<?, ?> build,
                        @Nonnull final FilePath workspace,
                        @Nonnull final Launcher launcher,
                        @Nonnull final TaskListener listener) throws InterruptedException, IOException {

       final Options options = generateOptions(build, workspace, listener);
       setOptions(options);
       super.perform(build, workspace, launcher, listener);
    }

    /**
     * Generate Options from build configuration preferences that will be passed to
     * the build step in DependencyCheck
     * @return DependencyCheck Options
     */
    private Options generateOptions(final Run<?, ?> build, final FilePath workspace, final TaskListener listener) {
        // Generate Options object with universal settings necessary for all Builder steps
        final Options options = optionsBuilder(build, workspace, listener, outdir, this.getDescriptor().getTempPath(), this.getDescriptor().isQuickQueryTimestampEnabled);

        // Configure universal settings useful for all Builder steps
        configureDataDirectory(build, workspace, listener, options, this.getDescriptor().getGlobalDataDirectory(), datadir);
        configureDataMirroring(options, this.getDescriptor().getDataMirroringType(),
                this.getDescriptor().getCveUrl12Modified(), this.getDescriptor().getCveUrl20Modified(),
                this.getDescriptor().getCveUrl12Base(), this.getDescriptor().getCveUrl20Base());
        configureProxySettings(options, this.getDescriptor().getIsNvdProxyBypassed());

	    // SETUP DB CONNECTION
        if (StringUtils.isNotBlank(this.getDescriptor().dbconnstr)) {
            options.setDbconnstr(this.getDescriptor().dbconnstr);
        }
        if (StringUtils.isNotBlank(this.getDescriptor().dbdriver)) {
            options.setDbdriver(this.getDescriptor().dbdriver);
        }
        if (StringUtils.isNotBlank(this.getDescriptor().dbpath)) {
            options.setDbpath(this.getDescriptor().dbpath);
        }
        if (StringUtils.isNotBlank(this.getDescriptor().dbuser)) {
            options.setDbuser(this.getDescriptor().dbuser);
        }
        if (StringUtils.isNotBlank(this.getDescriptor().dbpassword)) {
            options.setDbpassword(this.getDescriptor().dbpassword);
        }

        // Begin configuration for Builder specific settings

        // SUPPRESSION FILE
        if (StringUtils.isNotBlank(suppressionFile)) {
            String tmpSuppressionFile = PluginUtil.substituteVariable(build, listener, suppressionFile.trim());
            try {
                // Try to set the suppression file as a URL
                options.setSuppressionFile(new URL(tmpSuppressionFile).toExternalForm());
            } catch (MalformedURLException e) {
                // If the format is not a valid URL, set it as a FilePath type
                options.setSuppressionFile(new FilePath(workspace, tmpSuppressionFile).getRemote());
            }
        }

        // HINTS FILE
        if (StringUtils.isNotBlank(hintsFile)) {
            String tmpHintsFile = PluginUtil.substituteVariable(build, listener, hintsFile.trim());
            try {
                // Try to set the hints file as a URL
                options.setHintsFile(new URL(tmpHintsFile).toExternalForm());
            } catch (MalformedURLException e) {
                // If the format is not a valid URL, set it as a FilePath type
                options.setHintsFile(new FilePath(workspace, tmpHintsFile).getRemote());
            }
        }

        if (StringUtils.isNotBlank(zipExtensions)) {
            options.setZipExtensions(toCommaSeparatedString(zipExtensions));
        }

        // Support for multiple scan paths in a single analysis
        for (String tmpscanpath : scanpath.split(",")) {
            final FilePath filePath = new FilePath(workspace, PluginUtil.substituteVariable(build, listener, tmpscanpath.trim()));
            options.addScanPath(filePath.getRemote());
        }

        // Enable/Disable Analyzers
        options.setJarAnalyzerEnabled(this.getDescriptor().isJarAnalyzerEnabled);
        options.setNodeJsAnalyzerEnabled(this.getDescriptor().isNodeJsAnalyzerEnabled);
        options.setNspAnalyzerEnabled(this.getDescriptor().isNspAnalyzerEnabled);
        options.setComposerLockAnalyzerEnabled(this.getDescriptor().isComposerLockAnalyzerEnabled);
        options.setPythonDistributionAnalyzerEnabled(this.getDescriptor().isPythonDistributionAnalyzerEnabled);
        options.setPythonPackageAnalyzerEnabled(this.getDescriptor().isPythonPackageAnalyzerEnabled);
        options.setRubyBundlerAuditAnalyzerEnabled(this.getDescriptor().isRubyBundlerAuditAnalyzerEnabled);
        options.setRubyGemAnalyzerEnabled(this.getDescriptor().isRubyGemAnalyzerEnabled);
        options.setCocoaPodsAnalyzerEnabled(this.getDescriptor().isCocoaPodsAnalyzerEnabled);
        options.setSwiftPackageManagerAnalyzerEnabled(this.getDescriptor().isSwiftPackageManagerAnalyzerEnabled);
        options.setArchiveAnalyzerEnabled(this.getDescriptor().isArchiveAnalyzerEnabled);
        options.setAssemblyAnalyzerEnabled(this.getDescriptor().isAssemblyAnalyzerEnabled);
        options.setNuspecAnalyzerEnabled(this.getDescriptor().isNuspecAnalyzerEnabled);
        options.setNexusAnalyzerEnabled(this.getDescriptor().isNexusAnalyzerEnabled);
        options.setAutoconfAnalyzerEnabled(this.getDescriptor().isAutoconfAnalyzerEnabled);
        options.setCmakeAnalyzerEnabled(this.getDescriptor().isCmakeAnalyzerEnabled);
        options.setOpensslAnalyzerEnabled(this.getDescriptor().isOpensslAnalyzerEnabled);
        // Nexus options
        if (this.getDescriptor().isNexusAnalyzerEnabled && StringUtils.isNotBlank(this.getDescriptor().nexusUrl)) {
            try {
                options.setNexusUrl(new URL(this.getDescriptor().nexusUrl));
            } catch (MalformedURLException e) {
                // todo: need to log this or otherwise warn.
            }
            options.setNexusProxyBypassed(this.getDescriptor().isNexusProxyBypassed);
        }

        // Maven Central options
        options.setCentralAnalyzerEnabled(this.getDescriptor().isCentralAnalyzerEnabled);
        try {
            options.setCentralUrl(new URL("http://search.maven.org/solrsearch/select"));
        } catch (MalformedURLException e) {
            // todo: need to log this or otherwise warn.
        }

        // Only set the Mono path if running on non-Windows systems.
        if (!SystemUtils.IS_OS_WINDOWS && StringUtils.isNotBlank(this.getDescriptor().monoPath)) {
            options.setMonoPath(this.getDescriptor().monoPath);
        }

        if (StringUtils.isNotBlank(this.getDescriptor().bundleAuditPath)) {
            options.setBundleAuditPath(this.getDescriptor().bundleAuditPath);
        }

        options.setAutoUpdate(!isAutoupdateDisabled);
        options.setFailOnError(!isFailOnErrorDisabled);

        if (includeHtmlReports) {
            options.addFormat(ReportGenerator.Format.HTML);
        }
        if (includeJsonReports) {
            options.addFormat(ReportGenerator.Format.JSON);
        }
        if (includeCsvReports) {
            options.addFormat(ReportGenerator.Format.CSV);
        }

        return options;
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
    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
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
         * Specifies if to download the NVD data feeds the proxy defined in Jenkins should be bypassed.
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
         * Specifies if the Jar analyzer should be enabled or not
         */
        private boolean isJarAnalyzerEnabled = true;

        /**
         * Specifies if the NSP analyzer should be enabled or not
         */
        private boolean isNspAnalyzerEnabled = true;

        /**
         * Specifies if the Node.js analyzer should be enabled or not
         */
        private boolean isNodeJsAnalyzerEnabled = true;

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
        private boolean isRubyBundlerAuditAnalyzerEnabled = true;

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

        public FormValidation doCheckNexusUrl(@QueryParameter String value) {
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
            isJarAnalyzerEnabled = formData.getBoolean("isJarAnalyzerEnabled");
            isNodeJsAnalyzerEnabled = formData.getBoolean("isNodeJsAnalyzerEnabled");
            isNspAnalyzerEnabled = formData.getBoolean("isNspAnalyzerEnabled");
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
         * Returns the global configuration to determine if downloading the NVD data feeds shall bypass any proxy defined in Jenkins.
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
         * Returns the global configuration for enabling the Jar analyzer.
         */
        public boolean getIsJarAnalyzerEnabled() {
            return isJarAnalyzerEnabled;
        }

        /**
         * Returns the global configuration for enabling the Node.js analyzer.
         */
        public boolean getIsNodeJsAnalyzerEnabled() {
            return isNodeJsAnalyzerEnabled;
        }

        /**
         * Returns the global configuration for enabling the NSP analyzer.
         */
        public boolean getIsNspAnalyzerEnabled() {
            return isNspAnalyzerEnabled;
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
