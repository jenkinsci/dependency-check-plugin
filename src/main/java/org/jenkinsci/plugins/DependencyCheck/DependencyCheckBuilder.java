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
import hudson.maven.AbstractMavenProject;
import hudson.maven.MavenModule;
import hudson.maven.MavenModuleSet;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * The DependencyCheck builder class provides the ability to invoke a DependencyCheck build as
 * a Jenkins build step. This class takes the configuration from the UI, creates options from
 * them and passes them to the DependencyCheckExecutor for the actual execution of the
 * DependencyCheck Engine and ReportGenerator.
 *
 * @author Steve Springett (steve.springett@owasp.org)
 */
@SuppressWarnings("unused")
public class DependencyCheckBuilder extends AbstractDependencyCheckBuilder implements Serializable {

    private static final long serialVersionUID = 5594574614031769847L;

    private final String scanpath;
    private final String outdir;
    private final String datadir;
    private final String suppressionFile;
    private final String zipExtensions;
    private final boolean isAutoupdateDisabled;
    private final boolean isVerboseLoggingEnabled;
    private final boolean includeHtmlReports;
    private final boolean useMavenArtifactsScanPath;


    @DataBoundConstructor // Fields in config.jelly must match the parameter names
    public DependencyCheckBuilder(String scanpath, String outdir, String datadir, String suppressionFile,
                                  String zipExtensions, Boolean isAutoupdateDisabled, Boolean isVerboseLoggingEnabled,
                                  Boolean includeHtmlReports, Boolean skipOnScmChange, Boolean skipOnUpstreamChange,
                                  Boolean useMavenArtifactsScanPath) {
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
        this.useMavenArtifactsScanPath = (useMavenArtifactsScanPath != null) && useMavenArtifactsScanPath;
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
     * Retrieves whether Maven artifacts from the build should be used as the scan path.
     * This is a per-build config item.
     * This method must match the value in <tt>config.jelly</tt>.
     */
    public boolean areMavenArtifactsUsedForScanPath() {
        return useMavenArtifactsScanPath;
    }

    /**
     * Convenience method that determines if the project is a Maven project.
     * @param clazz The projects class
     */
    public boolean isMaven(Class<? extends AbstractProject> clazz) {
        return MavenModuleSet.class.isAssignableFrom(clazz) || MavenModule.class.isAssignableFrom(clazz);
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

       final Options options = generateOptions(build, listener);
       setOptions(options);
       return super.perform(build, launcher, listener);
    }

    /**
     * Generate Options from build configuration preferences that will be passed to
     * the build step in DependencyCheck
     * @param build an AbstractBuild object
     * @return DependencyCheck Options
     */
    private Options generateOptions(AbstractBuild build, BuildListener listener) {
        // Generate Options object with universal settings necessary for all Builder steps
        final Options options = optionsBuilder(build, listener, outdir, isVerboseLoggingEnabled, this.getDescriptor().getTempPath());

        // Configure universal settings useful for all Builder steps
        configureDataDirectory(build, listener, options, datadir);
        configureDataMirroring(options, this.getDescriptor().getDataMirroringType(),
                this.getDescriptor().getCveUrl12Modified(), this.getDescriptor().getCveUrl20Modified(),
                this.getDescriptor().getCveUrl12Base(), this.getDescriptor().getCveUrl20Base());
        configureProxySettings(options, this.getDescriptor().getIsNvdProxyBypassed());

        // Begin configuration for Builder specific settings

        // SUPPRESSION FILE
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

        // If specified to use Maven artifacts as the scan path - get them and populate the options
        if (useMavenArtifactsScanPath && build.getProject() instanceof AbstractMavenProject) {
            options.setUseMavenArtifactsScanPath(true);
            final ArrayList<String> artifacts = determineMavenArtifacts(build, listener);
            options.setScanPath(artifacts);
        } else {
            options.setUseMavenArtifactsScanPath(false);
            // Support for multiple scan paths in a single analysis
            for (String tmpscanpath : scanpath.split(",")) {
                final FilePath filePath = new FilePath(build.getWorkspace(), substituteVariable(build, listener, tmpscanpath.trim()));
                options.addScanPath(filePath.getRemote());
            }
        }


        // Enable/Disable Analyzers
        options.setJarAnalyzerEnabled(this.getDescriptor().isJarAnalyzerEnabled);
        options.setJavascriptAnalyzerEnabled(this.getDescriptor().isJavascriptAnalyzerEnabled);
        options.setPythonAnalyzerEnabled(this.getDescriptor().isPythonAnalyzerEnabled);
        options.setArchiveAnalyzerEnabled(this.getDescriptor().isArchiveAnalyzerEnabled);
        options.setAssemblyAnalyzerEnabled(this.getDescriptor().isAssemblyAnalyzerEnabled);
        options.setNuspecAnalyzerEnabled(this.getDescriptor().isNuspecAnalyzerEnabled);
        options.setNexusAnalyzerEnabled(this.getDescriptor().isNexusAnalyzerEnabled);
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

        options.setAutoUpdate(!isAutoupdateDisabled);

        if (includeHtmlReports) {
            options.setFormat(ReportGenerator.Format.ALL);
        }

        return options;
    }

    private ArrayList<String> determineMavenArtifacts(AbstractBuild build, BuildListener listener) {
        final ArrayList<String> artifacts = new ArrayList<String>();
        try {
            if (build.getProject() instanceof MavenModuleSet) {
                final MavenModuleSet mavenModuleSet = (MavenModuleSet) build.getProject();
                for (MavenModule module : mavenModuleSet.getModules()) {
                    final FilePath artifactsText = new FilePath(
                            new FilePath(
                                    new File(build.getRootDir()
                                            + File.separator
                                            + module.getModuleName().toFileSystemName()
                                            + File.separator
                                            + "archive")
                            ),
                            "artifacts.txt");

                    final BufferedReader br = new BufferedReader(new InputStreamReader(artifactsText.read()));
                    String line;
                    while ((line = br.readLine()) != null) {
                        final FilePath artifact = new FilePath(new File(line));
                        if (artifact.exists()) {
                            artifacts.add(artifact.getRemote());
                        }
                    }
                    br.close();
                }
            } else if (build.getProject() instanceof MavenModule) {
                final MavenModule mavenModule = (MavenModule) build.getProject();
                final FilePath artifactsText = new FilePath(
                        new FilePath(
                                new File(build.getRootDir()
                                        + File.separator
                                        + mavenModule.getModuleName().toFileSystemName()
                                        + File.separator
                                        + "archive")
                        ),
                        "artifacts.txt");

                final BufferedReader br = new BufferedReader(new InputStreamReader(artifactsText.read()));
                String line;
                while ((line = br.readLine()) != null) {
                    final FilePath artifact = new FilePath(new File(line));
                    if (artifact.exists()) {
                        artifacts.add(artifact.getRemote());
                    }
                }
                br.close();

            }
        } catch (IOException e) {
            listener.getLogger().println(e);
        } catch (InterruptedException e) {
            listener.getLogger().println(e);
        }
        return artifacts;
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
         * Specifies if the Javascript analyzer should be enabled or not
         */
        private boolean isJavascriptAnalyzerEnabled = true;

        /**
         * Specifies if the Python analyzer should be enabled or not
         */
        private boolean isPythonAnalyzerEnabled = true;

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
         * Specifies the full path to the temporary directory
         */
        private String tempPath;

        public DescriptorImpl() {
            super(DependencyCheckBuilder.class);
            load();
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types 
            return true;
        }

        public boolean isMaven(Class<? extends AbstractProject> clazz) {
            return MavenModuleSet.class.isAssignableFrom(clazz) || MavenModule.class.isAssignableFrom(clazz);
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

        public FormValidation doCheckTempPath(@QueryParameter String value) {
            return doCheckPath(value);
        }

        /**
         * Performs input validation when submitting the global config
         * @param value The value of the URL as specified in the global config
         * @return a FormValidation object
         */
        private FormValidation doCheckUrl(@QueryParameter String value) {
            if (StringUtils.isBlank(value)) {
                return FormValidation.ok();
            }
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
            if (StringUtils.isBlank(value)) {
                return FormValidation.ok();
            }
            try {
                final FilePath filePath = new FilePath(new File(value));
                filePath.exists();
            } catch (Exception e) {
                return FormValidation.error("The specified value is not a valid path");
            }
            return FormValidation.ok();
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
            isJavascriptAnalyzerEnabled = formData.getBoolean("isJavascriptAnalyzerEnabled");
            isPythonAnalyzerEnabled = formData.getBoolean("isPythonAnalyzerEnabled");
            isArchiveAnalyzerEnabled = formData.getBoolean("isArchiveAnalyzerEnabled");
            isAssemblyAnalyzerEnabled = formData.getBoolean("isAssemblyAnalyzerEnabled");
            isCentralAnalyzerEnabled = formData.getBoolean("isCentralAnalyzerEnabled");
            isNuspecAnalyzerEnabled = formData.getBoolean("isNuspecAnalyzerEnabled");
            isNexusAnalyzerEnabled = formData.getBoolean("isNexusAnalyzerEnabled");
            nexusUrl = formData.getString("nexusUrl");
            isNexusProxyBypassed = formData.getBoolean("isNexusProxyBypassed");
            monoPath = formData.getString("monoPath");
            tempPath = formData.getString("tempPath");
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
         * Returns the global configuration for enabling the Javascript analyzer.
         */
        public boolean getIsJavascriptAnalyzerEnabled() {
            return isJavascriptAnalyzerEnabled;
        }

        /**
         * Returns the global configuration for enabling the Python analyzer.
         */
        public boolean getIsPythonAnalyzerEnabled() {
            return isPythonAnalyzerEnabled;
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
         * Returns the global configuration for the path to the temporary directory.
         */
        public String getTempPath() {
            return tempPath;
        }

    }
}
