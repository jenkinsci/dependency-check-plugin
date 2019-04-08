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
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * The DependencyCheckUpdateOnlyBuilder builder class provides the ability to invoke a DependencyCheck
 * NVD update only as a Jenkins build step. This class takes the configuration from the UI, creates
 * options from them and passes them to the DependencyCheckExecutor for the actual execution of the
 * DependencyCheck Engine.
 *
 * @author Steve Springett (steve.springett@owasp.org)
 */
@SuppressWarnings("unused")
public class DependencyCheckUpdateOnlyBuilder extends AbstractDependencyCheckBuilder {

    private static final long serialVersionUID = -1028800761683685381L;

    private final String datadir;


    @DataBoundConstructor // Fields in config.jelly must match the parameter names
    public DependencyCheckUpdateOnlyBuilder(String datadir) {
        this.datadir = datadir;
    }

    /**
     * Retrieves the data directory that DependencyCheck will use. This is a per-build config item.
     * This method must match the value in <tt>config.jelly</tt>.
     */
    public String getDatadir() {
        return datadir;
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
        final GlobalOptions globalOptions = generateGlobalOptions(build, listener);
        setGlobalOptions(globalOptions);
        super.perform(build, workspace, launcher, listener);
    }

    /**
     * Generate Options from build configuration preferences that will be passed to
     * the build step in DependencyCheck
     * @param build an AbstractBuild object
     * @return DependencyCheck Options
     */
    private JobOptions generateJobOptions(final Run<?, ?> build, final FilePath workspace, final TaskListener listener) {
        // Generate Options object with universal settings necessary for all Builder steps
        final JobOptions jobOptions = jobOptionsBuilder(build, workspace, listener, null);
        // Configure universal settings useful for all Builder steps
        configureDataDirectory(build, workspace, listener, jobOptions, this.getDescriptor().getGlobalDataDirectory(), datadir);
        // Begin configuration for Builder specific settings
        jobOptions.setAutoUpdate(true);
        jobOptions.setUpdateOnly(true);
        return jobOptions;
    }

    /**
     * Generate Options from build configuration preferences that will be passed to
     * the build step in DependencyCheck
     * @param build an AbstractBuild object
     * @return DependencyCheck Options
     */
    private GlobalOptions generateGlobalOptions(final Run<?, ?> build, final TaskListener listener) {
        // Generate Options object with universal settings necessary for all Builder steps
        final GlobalOptions globalOptions = globalOptionsBuilder(build, listener, this.getDescriptor().getTempPath(), this.getDescriptor().getIsQuickQueryTimestampEnabled());

        configureDataMirroring(globalOptions, this.getDescriptor().getDataMirroringType(),
                this.getDescriptor().getCveUrl12Modified(), this.getDescriptor().getCveUrl20Modified(),
                this.getDescriptor().getCveUrl12Base(), this.getDescriptor().getCveUrl20Base(),
                this.getDescriptor().getRetireJsRepoJsUrl());
        configureProxySettings(globalOptions, this.getDescriptor().getIsNvdProxyBypassed());

        // SETUP DB CONNECTION
        if (StringUtils.isNotBlank(this.getDescriptor().getDbconnstr())) {
            globalOptions.setDbconnstr(this.getDescriptor().getDbconnstr());
        }
        if (StringUtils.isNotBlank(this.getDescriptor().getDbdriver())) {
            globalOptions.setDbdriver(this.getDescriptor().getDbdriver());
        }
        if (StringUtils.isNotBlank(this.getDescriptor().getDbpath())) {
            globalOptions.setDbpath(this.getDescriptor().getDbpath());
        }
        if (StringUtils.isNotBlank(this.getDescriptor().getDbuser())) {
            globalOptions.setDbuser(this.getDescriptor().getDbuser());
        }
        if (StringUtils.isNotBlank(this.getDescriptor().getDbpassword())) {
            globalOptions.setDbpassword(this.getDescriptor().getDbpassword());
        }
        // Retire.js has to be enabled in order for the datasource to perform the update
        globalOptions.setRetireJsAnalyzerEnabled(this.getDescriptor().getIsRetireJsAnalyzerEnabled());
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
     * Descriptor for {@link DependencyCheckUpdateOnlyBuilder}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     * <p/>
     * <p/>
     * See <tt>src/main/resources/org/jenkinsci/plugins/DependencyCheck/DependencyCheckUpdateOnlyBuilder/*.jelly</tt>
     * for the actual HTML fragment for the configuration screen.
     */
    @Extension @Symbol("dependencyCheckUpdateOnly") // This indicates to Jenkins that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        private DependencyCheckBuilder.DescriptorImpl globalDcDescriptor = null;

        private void init() {
            if (globalDcDescriptor == null) {
                globalDcDescriptor = (DependencyCheckBuilder.DescriptorImpl) Jenkins.getInstance().getDescriptor(DependencyCheckBuilder.class);
            }
        }

        /**
         * Default constructor. Obtains the Descriptor used in DependencyCheckBuilder as this contains
         * the global Dependency-Check Jenkins plugin configuration.
         */
        public DescriptorImpl() {
            super(DependencyCheckUpdateOnlyBuilder.class);
            load();
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types
            return true;
        }

        /**
         * This name is used on the build configuration screen.
         */
        @Override
        public String getDisplayName() {
            return Messages.Builder_UpdateOnly_Name();
        }

        /**
         * This method returns the global configuration for dataMirroringType.
         */
        public int getDataMirroringType() {
            init();
            return globalDcDescriptor.getDataMirroringType();
        }

        /**
         * Returns the global configuration to determine if Jenkins web proxy settings should be ignored.
         */
        public boolean getIsNvdProxyBypassed() {
            init();
            return globalDcDescriptor.getIsNvdProxyBypassed();
        }

        /**
         * Returns the global configuration for CVE 1.2 modified URL.
         */
        public String getCveUrl12Modified() {
            init();
            return globalDcDescriptor.getCveUrl12Modified();
        }

        /**
         * Returns the global configuration for CVE 2.0 modified URL.
         */
        public String getCveUrl20Modified() {
            init();
            return globalDcDescriptor.getCveUrl20Modified();
        }

        /**
         * Returns the global configuration for CVE 1.2 base URL.
         */
        public String getCveUrl12Base() {
            init();
            return globalDcDescriptor.getCveUrl12Base();
        }

        /**
         * Returns the global configuration for CVE 2.0 base URL.
         */
        public String getCveUrl20Base() {
            init();
            return globalDcDescriptor.getCveUrl20Base();
        }

        /**
         * Returns the global configuration for the URL to the Javascript feed for Retire.js.
         */
        public String getRetireJsRepoJsUrl() {
            init();
            return globalDcDescriptor.getRetireJsRepoJsUrl();
        }

        /**
         * Returns the global configuration for the path to the temporary directory.
         */
        public String getTempPath() {
            init();
            return globalDcDescriptor.getTempPath();
        }

        /**
         * Returns the global configuration for the path to the data directory.
         */
        public String getGlobalDataDirectory() {
            init();
            return globalDcDescriptor.getGlobalDataDirectory();
        }

        /**
         * Retrieves the database connection string that DependencyCheck will use. This is a per-build config item.
         * This method must match the value in <tt>config.jelly</tt>.
         */
        public String getDbconnstr() {
            init();
            return globalDcDescriptor.getDbconnstr();
        }

        /**
         * Retrieves the database driver name that DependencyCheck will use. This is a per-build config item.
         * This method must match the value in <tt>config.jelly</tt>.
         */
        public String getDbdriver() {
            init();
            return globalDcDescriptor.getDbdriver();
        }

        /**
         * Retrieves the database driver path that DependencyCheck will use. This is a per-build config item.
         * This method must match the value in <tt>config.jelly</tt>.
         */
        public String getDbpath() {
            init();
            return globalDcDescriptor.getDbpath();
        }

        /**
         * Retrieves the database user that DependencyCheck will use. This is a per-build config item.
         * This method must match the value in <tt>config.jelly</tt>.
         */
        public String getDbuser() {
            init();
            return globalDcDescriptor.getDbuser();
        }

        /**
         * Retrieves the database password that DependencyCheck will use. This is a per-build config item.
         * This method must match the value in <tt>config.jelly</tt>.
         */
        public String getDbpassword() {
            init();
            return globalDcDescriptor.getDbpassword();
        }

        /**
         * Returns if QuickQuery is enabled or not. If enabled, HTTP HEAD will be used.
         */
        public boolean getIsQuickQueryTimestampEnabled() {
            init();
            return globalDcDescriptor.getIsQuickQueryTimestampEnabled();
        }

        /**
         * Returns the global configuration for enabling the RetireJS analyzer.
         */
        public boolean getIsRetireJsAnalyzerEnabled() {
            init();
            return globalDcDescriptor.getIsRetireJsAnalyzerEnabled();
        }

    }

}
